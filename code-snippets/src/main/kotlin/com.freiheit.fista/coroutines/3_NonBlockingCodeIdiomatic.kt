package com.freiheit.fista.coroutines

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking


/**
 * What is async?
 * - async is a builder to construct a coroutine
 * - per default, async fires immediately (use async(start = CoroutineStart.LAZY)) to change this behaviour.
 * - async returns a [Deferred] which also propagates exceptions
 */
val helloIdiomatic: Deferred<String> = GlobalScope.async {
    delay(2000L)
    println("done hello")
    return@async "Hello"
}

val worldIdiomatic: Deferred<String> = GlobalScope.async {
    delay(1000L)
    println("done world")
    return@async "World"
}

fun main(): Unit = runBlocking {
    println("${helloIdiomatic.await()}, ${worldIdiomatic.await()}")
}