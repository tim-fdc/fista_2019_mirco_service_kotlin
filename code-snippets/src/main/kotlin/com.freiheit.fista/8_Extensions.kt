package com.freiheit.fista

fun Any?.extension() = "You can call this function on all objects and null from anywhere"

/**
 * Extensions are static functions that can be called directly on the extended object
 */
private fun String.shuffle(): String = TODO()
private val shuffled = "ABC".shuffle()

/**
 * @DONT Monkey patch foreign objects
 *  - Kotlin provides a huge set of useful extensions on Java objects already
 *  - Your extension might collide with future implementations in the stdlib
 */
fun String.capitalize(): String = TODO()
val lowerCase = "filterEvents".capitalize()

/**
 * @DO Use extension in a very restricted scope
 * @DONT Leak narrow-use-case extensions into the global scope
 */

data class ArticleId(val value: String) {
    private fun String.isValidId() = matches("[0-9]".toRegex())
}

// Everyone can see and use this
fun String.toPort(): Int = toInt()

/**
 * @DO Use extensions to decorate scope specific logic to models
 *     (if you and your team feel like it)
 */
data class DumbModel(val version: Long, val payload: Map<String, Any>)

object Logic {
    private fun DumbModel.process(): Any = payload.values.reduce { acc, value -> "$acc $value" }

    fun exec(data: DumbModel) = data.process()
}