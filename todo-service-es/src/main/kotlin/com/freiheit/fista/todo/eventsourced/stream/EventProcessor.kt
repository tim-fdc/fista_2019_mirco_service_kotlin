package com.freiheit.fista.todo.eventsourced.stream

import com.freiheit.fista.todo.eventsourced.aggregate
import com.freiheit.fista.todo.eventsourced.model.Event
import com.freiheit.fista.todo.eventsourced.model.Todo
import com.freiheit.fista.todo.eventsourced.persistence.EventStore
import com.freiheit.fista.todo.eventsourced.persistence.TodoStore

object EventProcessor : Processor<Event> {

    override operator fun invoke(event: Event) {
        val todo = (EventStore.load(event.todoId) ?: emptyList())
            .sortedBy { it.seqNum }
            .take(event.seqNum)
            .plus(event)
            .aggregate() as? Todo.Created
            ?: return

        TodoStore.save(todo.id, todo)
    }
}