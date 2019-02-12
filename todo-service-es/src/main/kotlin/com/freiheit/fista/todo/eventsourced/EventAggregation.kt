package com.freiheit.fista.todo.eventsourced

import com.freiheit.fista.todo.eventsourced.model.CompletedTodo
import com.freiheit.fista.todo.eventsourced.model.Event
import com.freiheit.fista.todo.eventsourced.model.Event.Payload.MarkedCompleted
import com.freiheit.fista.todo.eventsourced.model.Event.Payload.TodoAssigned
import com.freiheit.fista.todo.eventsourced.model.Event.Payload.TodoCreated
import com.freiheit.fista.todo.eventsourced.model.InProgressTodo
import com.freiheit.fista.todo.eventsourced.model.OpenTodo
import com.freiheit.fista.todo.eventsourced.model.Todo

fun List<Event>.aggregate(): Todo = sortedBy { it.seqNum }
    .fold(Todo.Blank as Todo) { acc, event ->
        with(EventAggregation) { acc.apply(event) }
    }

object EventAggregation {
    fun Todo.apply(event: Event): Todo {
        if (event.status is Event.Status.Failure) {
            return this
        }

        return when (event.payload) {
            is TodoCreated -> when (this) {
                is Todo.Blank -> create(event.todoId, event.payload)
                is Todo.Created -> null
            }
            is TodoAssigned -> when (this) {
                is Todo.Created.Active -> assign(event.payload)
                is Todo.Blank,
                is Todo.Created.Closed.Completed -> null
            }
            is MarkedCompleted -> when (this) {
                is Todo.Created -> markCompleted()
                is Todo.Blank -> null
            }
        } ?: this.also { println("Cannot apply ${event::class.simpleName} to ${it::class.simpleName}") }
    }

    private fun Todo.Blank.create(id: String, payload: TodoCreated): Todo.Created = run {
        OpenTodo(
            id = id,
            data = Todo.Data(
                assignee = payload.assignee,
                description = payload.description
            )
        )
    }

    private fun Todo.Created.Active.assign(payload: TodoAssigned): Todo.Created.Active = when (this) {
        is OpenTodo -> OpenTodo(
            id = id,
            data = data.copy(assignee = payload.assignee)
        )
        is InProgressTodo -> InProgressTodo(
            id = id,
            data = data.copy(assignee = payload.assignee),
            startedAt = startedAt
        )
    }

    private fun Todo.Created.markCompleted(): Todo.Created.Closed.Completed = when (this) {
        is OpenTodo -> CompletedTodo(
            id = id,
            data = data,
            startedAt = System.currentTimeMillis()
        )
        is InProgressTodo -> CompletedTodo(
            id = id,
            data = data,
            startedAt = startedAt
        )
        is CompletedTodo -> this
    }
}