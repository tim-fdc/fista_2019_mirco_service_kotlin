package com.freiheit.fista.todo.eventsourced.stream

import com.freiheit.fista.todo.eventsourced.persistence.Queue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class StreamProcessor<V>(private val stream: Queue<out V>, private val process: Processor<in V>) : CoroutineScope {
    override val coroutineContext: CoroutineContext = SupervisorJob() + Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    fun startProcessing() = launch {
        while (isActive) {
            val value = stream.poll()
            withContext(NonCancellable) {
                process(value)
            }
        }
    }
}

typealias Processor<V> = (V) -> Unit