package com.freiheit.fista.todo

/**
 * Generic interfaces to load, save and drop data from a datastore.
 */
interface IStore<V> {
    fun load(key: String): V?
    fun save(key: String, value: V)
    fun delete(key: String): Boolean
    fun drop()
}

/**
 * Default implementation of [IStore]. Uses a [MutableMap] as actual data source.
 */
class InMemoryStore<V> : IStore<V> {
    private val db = mutableMapOf<String, V>()

    override fun load(key: String) = db[key]

    override fun save(key: String, value: V) {
        db[key] = value
    }

    override fun delete(key: String): Boolean {
        return db.remove(key) != null
    }

    override fun drop() = db.clear()
}

object TodoStore : IStore<Todo> by InMemoryStore()
