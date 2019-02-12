package com.freiheit.fista.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.Executors
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * It is possible to define a dedicated [CoroutineScope] tailored to your business needs.
 */
class RequestScope(private val requestContext: RequestContext = RequestContext()) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = requestContext
}

/**
 * The [RequestContext] is passed to the [RequestScope] as context element. It contains call related data such
 * as requestIds, CallTreeIds, TraceIds, etc. The context is always passed between coroutines.
 *
 * Each context element needs to implement [AbstractCoroutineContextElement] and define a dedicated key to access
 * the element.
 */
class RequestContext(val requestId: String = UUID.randomUUID().toString()) : AbstractCoroutineContextElement(Key) {
    // this is necessary to mark this as an coroutine context
    //
    // launch(requestContext) /* <- requires Request context to be a CoroutineContext */  {...}
    object Key : CoroutineContext.Key<RequestContext>
}

/**
 * Extension to simplify accessing the coroutine from a [CoroutineScope].
 */
fun CoroutineScope.requestId(): String {
    val requestContext = coroutineContext[RequestContext.Key]
    return requestContext?.requestId ?: throw IllegalStateException("No request ID present")
}

/**
 * Whenever you need to call external resources via blocking IO, offload the work to a dedicated pool.
 */
val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

/**
 * Simulates requesting article data from an external service.
 */
suspend fun requestArticleData() = withContext(dispatcher) {
    println(requestId())
    return@withContext """
        {"articleId": "12345"}
    """.trimIndent()
}

fun main() {
    val scope = RequestScope()
    val result = scope.async {
        return@async requestArticleData()
    }

    runBlocking {
        println(result.await())
    }
}