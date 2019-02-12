package com.freiheit.fista.coroutines

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class HelloFailedException : Exception("Exception: Hello Failed")

private suspend fun hello(): String {
    delay(500)
    throw HelloFailedException()
}

private suspend fun world(): String? = try {
    delay(1000)
    "world"
} catch (e: Exception) {
    println("Exception: world failed")
    e.printStackTrace()
    null
}

private suspend fun helloWorld(): String = coroutineScope {
    try {
        val hello = async { hello() }
        val world = async { world() }
        return@coroutineScope "${hello.await()}, ${world.await()}"
    } catch (e: Exception) {
        println("${e.message}")
        throw e
    }
}


fun main(): Unit = runBlocking {
    println(helloWorld())
}
