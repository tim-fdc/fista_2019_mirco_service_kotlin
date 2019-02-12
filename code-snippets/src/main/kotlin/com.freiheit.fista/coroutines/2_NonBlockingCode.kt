package com.freiheit.fista.coroutines

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    /**
     * What is launch?
     * - launch runs a new coroutine in background (fire and forget)
     * - launch is a so called builder
     *
     * Notice: Avoid usage of GlobalScope. Let's fix this later.
     */
    val job: Job = GlobalScope.launch {
        // non-blocking delay for 1 second (default time unit is ms)
        delay(1000L)
        // print after delay
        println("World!")
    }

    // main thread continues while coroutine is delayed (no thread is blocked!)
    println("Hello,")

    runBlocking {
        job.join()

    }
}