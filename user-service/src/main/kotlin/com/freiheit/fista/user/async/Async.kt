package com.freiheit.fista.user.async

import com.freiheit.fista.user.GrpcMappableException
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.AbstractStub
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Make sure all calls have a timeout.
 */
private val DEFAULT_TIME_OUT: Duration = Duration.ofMillis(1000)
private val logger = LoggerFactory.getLogger(GrpcAsyncResponseHandler::class.java)

/**
 * The "root" scope that is created for each GRPC request.
 */
class GrpcRequestScope(private val requestContext: RequestContext = RequestContext()) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = requestContext
}

/**
 * Please notice that this function is not suspendable, because its usage should not be limited to suspendable functions
 * only. It creates a new scope [GrpcRequestScope] and dispatch calls to the [GrpcAsyncResponseHandler].
 */
fun <T> processGrpcRequest(
    responseObserver: StreamObserver<T>,
    timeOutInMillis: Long = DEFAULT_TIME_OUT.toMillis(),
    block: suspend () -> T
): Job {
    val responseHandler = GrpcAsyncResponseHandler(responseObserver)

    val scope = GrpcRequestScope(RequestContext())
    return scope.async {
        responseHandler.launchWithTimeout(timeOutInMillis) {
            try {
                responseHandler.onSuccess(block())
            } catch (e: Throwable) {
                logger.error("Failed to execute response", e)
                responseHandler.onError(e)
            }
        }
    }
}

/**
 * This handler links the result of executing Coroutines to GRPC's [StreamObserver]. This class is not needed if
 * [processGrpcRequest] can be suspendable.
 */
private class GrpcAsyncResponseHandler<in T>(private val responseObserver: StreamObserver<T>) {
    fun onSuccess(result: T) {
        responseObserver.onNext(result)
        responseObserver.onCompleted()
    }

    fun onError(e: Throwable) = when (e) {
        is StatusRuntimeException -> responseObserver.onError(e)
        is GrpcMappableException -> responseObserver.onError(e.toGrpcException())
        else -> responseObserver.onError(Status.INTERNAL.withCause(e).asException())
    }

    suspend fun launchWithTimeout(timeOutMillis: Long, block: suspend CoroutineScope.() -> Unit) = try {
        withTimeout(timeOutMillis, block)
    } catch (e: CancellationException) {
        responseObserver.onError(Status.DEADLINE_EXCEEDED.withDescription("time out").asException())
    }
}

/**
 * Coroutines are passed around threads and their execution can be suspended at suspension points. For each [GrpcRequestScope],
 * which is a [CoroutineScope], this is the context of the coroutine. The context contains data such as a [CallTreeId].
 * A [CallTreeId] must be passed along the scope and should be present in each function call, for example, to pass
 * it to subsequent calls. Logging [CallTreeId] is a good practice for error analysis.
 */
class RequestContext(val callTreeId: CallTreeId = CallTreeId.new()) : AbstractCoroutineContextElement(Key) {
    object Key : CoroutineContext.Key<RequestContext>
}

/**
 * Little extension (https://kotlinlang.org/docs/reference/extensions.html) to easily access the [RequestContext].
 * Please notice a function that needs the context has to switch into the coroutineScope.
 */
fun CoroutineScope.requestContext(): RequestContext {
    val requestContext = coroutineContext[RequestContext.Key]
    return requestContext ?: throw IllegalStateException("No RequestContext found")
}

/**
 * A simple ID that is passed along the calls and represents one request tree.
 */
data class CallTreeId(val id: String) {
    companion object {
        fun new(): CallTreeId {
            return CallTreeId(id = UUID.randomUUID().toString())
        }
    }
}

/**
 * Used by clients to make suspending GRPC calls.
 */
suspend inline fun <T : AbstractStub<T>, reified R> T.suspendingUnaryCallObserver(
    crossinline block: T.(StreamObserver<R>) -> Unit
): R = suspendCancellableCoroutine { cont: CancellableContinuation<R> ->
    block(SuspendingUnaryObserver(cont))
}

class SuspendingUnaryObserver<RespT>(@Volatile @JvmField var cont: Continuation<RespT>?) : StreamObserver<RespT> {
    override fun onNext(value: RespT) {
        cont?.resume(value)
    }

    override fun onError(t: Throwable) {
        cont?.resumeWithException(t)
        cont = null
    }

    override fun onCompleted() {
        cont = null
    }
}