package com.freiheit.fista.todo.eventsourced.model

class UserId(val id: String)

sealed class Todo {
    data class Data(val assignee: UserId, val description: String)

    object Blank : Todo()

    sealed class Created(val id: String, val data: Data) : Todo() {
        sealed class Active(id: String, data: Data) : Created(id, data) {
            class Open(id: String, data: Data) : Active(id, data)
            class InProgress(id: String, data: Data, val startedAt: Long = System.currentTimeMillis()) : Active(id, data)
        }

        sealed class Closed(id: String, data: Data) : Created(id, data) {
            class Completed(id: String, data: Data, val startedAt: Long, val completedAt: Long = System.currentTimeMillis()) :
                Created(id, data)
        }
    }
}

typealias OpenTodo = Todo.Created.Active.Open
typealias InProgressTodo = Todo.Created.Active.InProgress
typealias CompletedTodo = Todo.Created.Closed.Completed