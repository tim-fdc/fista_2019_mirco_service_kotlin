package com.freiheit.fista.todo.eventsourced.persistence

import com.freiheit.fista.todo.eventsourced.model.Event
import com.freiheit.fista.todo.eventsourced.model.Todo

/**
 * Generic interfaces to load, save and drop data from a datastore.
 */
interface IStore<V, R> {
    fun load(key: String): R?
    fun save(key: String, value: V)
    fun drop()
}

/**
 * Default implementation of [IStore]. Uses a [MutableMap] as actual data source.
 */
class SingleValueInMemoryStore<V> : IStore<V, V> {
    private val db = mutableMapOf<String, V>()

    override fun load(key: String) = db[key]

    override fun save(key: String, value: V) {
        db[key] = value
    }

    override fun drop() = db.clear()
}

class CollectionInMemoryStore<V> : IStore<V, List<V>> {
    private val db = mutableMapOf<String, MutableList<V>>()

    override fun load(key: String) = db[key]?.toList()

    override fun save(key: String, value: V) {
        db[key]?.add(value)
    }

    override fun drop() = db.clear()
}

object TodoStore : IStore<Todo.Created, Todo.Created> by SingleValueInMemoryStore()
object EventStore : IStore<Event, List<Event>> by CollectionInMemoryStore()