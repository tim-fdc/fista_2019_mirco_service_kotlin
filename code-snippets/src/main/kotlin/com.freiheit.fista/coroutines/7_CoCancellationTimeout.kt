package com.freiheit.fista.coroutines

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout


suspend fun helloWithTimeout(): String = withTimeout(300) {
    delay(500)
    return@withTimeout "hello"
}

suspend fun worldWithTimeout(): String = withTimeout(500) {
    try {
        delay(1000)
        return@withTimeout "world"
    } catch (e: Exception) {
        println("world failed")
        e.printStackTrace()
        throw e
    }
}

suspend fun helloWorldWithTimeout(): String = coroutineScope {
    val hello = async { helloWithTimeout() }
    val world = async { worldWithTimeout() }
    return@coroutineScope "${hello.await()}, ${world.await()}"
}


fun main(): Unit = runBlocking {
    println(helloWorldWithTimeout())
}
