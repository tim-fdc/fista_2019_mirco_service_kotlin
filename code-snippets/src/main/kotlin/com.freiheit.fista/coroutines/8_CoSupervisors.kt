package com.freiheit.fista.coroutines

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope

private suspend fun hello(): String {
    delay(500)
    throw Exception("hello failed")
}

private suspend fun world(): String {
    try {
        delay(1000)
        return "world"
    } catch (e: Exception) {
        println("world failed due to ${e.message}")
        throw e
    }
}

val handler = CoroutineExceptionHandler { _, exception ->
    println("Caught original $exception")
}

/**
 * Prevents a cancellation of hello to propagate to world. Uses a [supervisorScope] to [launch] [hello].
 */
private suspend fun helloWorld(): String = coroutineScope {
    var hello: String? = null

    supervisorScope {
        launch(handler) {
            hello = hello()
        }
    }

    val world = async { world() }
    return@coroutineScope "$hello, ${world.await()}"
}


/**
 * This is an alternative solution that does not use [launch] and directly calls the suspend function. In this case
 * exceptions need to be caught to prevent them from propagating.
 */
private suspend fun helloWorldNoLaunch(): String = coroutineScope {
    val hello: String? = supervisorScope {
        try {
            return@supervisorScope hello()
        } catch (e: Exception) {
            e.printStackTrace()
            return@supervisorScope ""
        }
    }

    val world = async { world() }
    return@coroutineScope "$hello, ${world.await()}"
}

fun main(): Unit = runBlocking {
    println(helloWorld())
    println(helloWorldNoLaunch())
}
