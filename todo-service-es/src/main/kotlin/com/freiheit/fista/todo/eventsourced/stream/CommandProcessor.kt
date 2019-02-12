package com.freiheit.fista.todo.eventsourced.stream

import com.freiheit.fista.todo.eventsourced.aggregate
import com.freiheit.fista.todo.eventsourced.model.Command
import com.freiheit.fista.todo.eventsourced.model.Event
import com.freiheit.fista.todo.eventsourced.model.Event.Status.Failure
import com.freiheit.fista.todo.eventsourced.model.Event.Status.Failure.Reason
import com.freiheit.fista.todo.eventsourced.model.Event.Status.Success
import com.freiheit.fista.todo.eventsourced.model.Todo
import com.freiheit.fista.todo.eventsourced.persistence.EventQueue
import com.freiheit.fista.todo.eventsourced.persistence.EventStore

object CommandProcessor :
    Processor<Command> {

    override operator fun invoke(command: Command) {
        val event = process(command)
        EventStore.save(event.todoId, event)
        EventQueue.send(event)
    }

    private fun process(command: Command): Event {
        val events = EventStore.load(command.todoId) ?: emptyList()
        val todo = events.aggregate()

        return Event(
            todoId = command.todoId,
            payload = command.getEventPayload(),
            status = validate(command, todo),
            seqNum = events.size
        )
    }

    private fun Command.getEventPayload(): Event.Payload = when (payload) {
        is Command.Payload.CreateTodo -> Event.Payload.TodoCreated(payload.assignee, payload.description)
        is Command.Payload.AssignTodo -> Event.Payload.TodoAssigned(payload.assignee)
        is Command.Payload.MarkCompleted -> Event.Payload.MarkedCompleted
    }

    private fun validate(command: Command, todo: Todo): Event.Status = when (command.payload) {
        is Command.Payload.CreateTodo -> when (todo) {
            is Todo.Blank -> Success
            is Todo.Created -> Failure(Reason.ALREADY_CREATED)
        }
        is Command.Payload.AssignTodo -> when (todo) {
            is Todo.Blank -> Failure(Reason.NOT_FOUND)
            is Todo.Created.Active -> Success
            is Todo.Created.Closed.Completed -> Failure(Reason.INACTIVE)
        }
        is Command.Payload.MarkCompleted -> when (todo) {
            is Todo.Blank -> Failure(Reason.NOT_FOUND)
            is Todo.Created -> Success
        }
    }
}