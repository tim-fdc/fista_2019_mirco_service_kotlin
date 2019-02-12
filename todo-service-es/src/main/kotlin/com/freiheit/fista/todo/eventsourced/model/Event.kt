package com.freiheit.fista.todo.eventsourced.model

data class Event(
    val todoId: String,
    val payload: Payload,
    val status: Status,
    val seqNum: Int
) {
    sealed class Status {
        object Success : Status()
        data class Failure(val reason: Reason) : Status() {
            enum class Reason { NOT_FOUND, ALREADY_CREATED, INACTIVE, UNKNOWN }
        }
    }

    sealed class Payload {
        data class TodoCreated(val assignee: UserId, val description: String) : Payload()
        data class TodoAssigned(val assignee: UserId) : Payload()
        object MarkedCompleted : Payload()
    }
}