package com.freiheit.fista.todo

class UserId(val id: String)

sealed class Todo(val id: String, val data: Data) {
    data class Data(val assignee: UserId, val description: String)
    sealed class Active(id: String, data: Data) : Todo(id, data) {
        class Open(id: String, data: Data) : Active(id, data)
        class InProgress(id: String, data: Data, val startedAt: Long) : Active(id, data)
    }

    sealed class Closed(id: String, data: Data) : Todo(id, data) {
        class Completed(id: String, data: Data, val startedAt: Long, val completedAt: Long = System.currentTimeMillis()) :
            Closed(id, data)
    }
}

typealias OpenTodo = Todo.Active.Open
typealias InProgressTodo = Todo.Active.InProgress
typealias CompletedTodo = Todo.Closed.Completed