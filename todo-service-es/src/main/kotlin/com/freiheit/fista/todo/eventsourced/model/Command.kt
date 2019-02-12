package com.freiheit.fista.todo.eventsourced.model

data class Command(val todoId: String, val payload: Payload) {
    sealed class Payload {
        data class CreateTodo(val assignee: UserId, val description: String) : Payload()
        data class AssignTodo(val assignee: UserId) : Payload()
        object MarkCompleted : Payload()
    }
}