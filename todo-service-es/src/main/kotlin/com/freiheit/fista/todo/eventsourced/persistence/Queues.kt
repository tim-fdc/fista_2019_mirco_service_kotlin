package com.freiheit.fista.todo.eventsourced.persistence

import com.freiheit.fista.todo.eventsourced.model.Command
import com.freiheit.fista.todo.eventsourced.model.Event
import kotlinx.coroutines.delay
import java.util.concurrent.LinkedBlockingDeque

interface Queue<V> {
    suspend fun poll(): V
    fun send(value: V)
}

class InMemoryQueue<V> : Queue<V> {
    private val queue = LinkedBlockingDeque<V>()

    override suspend fun poll(): V {
        return queue.poll()
            ?: delay(500).run { poll() }
    }

    override fun send(value: V) {
        queue.add(value)
    }
}

object CommandQueue : Queue<Command> by InMemoryQueue()
object EventQueue : Queue<Event> by InMemoryQueue()