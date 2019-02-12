package com.freiheit.fista

fun main(args: Array<String>) {
    val numbers = listOf(1, 2, 3)
    val mutableNumbers = mutableListOf(1, 2, 3)

    // will this fail?
    val squareNumbers = mapOf("1" to 1, "4" to 2, "9" to 3)
    println(squareNumbers)

    val binaryNumbers = setOf(0, 1, 0, 1)
    println(binaryNumbers)

    val offByOnes = numbers.map { it + 1 }

    println("incrList $offByOnes")

    numbers.findLast { it % 2 == 1 }
    numbers.filter { it % 2 == 1 }
    (1..1000).reduce { a, b -> a + b }
    mapOf("1" to 1, "2" to 2, "3" to 3).map { v -> v.value }
}

/**
 * Lambdas are declared with curlies
 */
val lambda = { s: String -> s[69] }

/**
 * Single argument lambdas can omit giving [it] a name
 */
private val foo: (String) -> Char = { it[69] }

/**
 * Functions are first class citizens and can be passed around like other objects
 */
private fun times(count: Int = 1, block: () -> Unit) = (0..count).forEach { block() }

/**
 * If a function is passed as the last argument, it can be defined outside of the parentheses
 */
private val times69 = times(69) { print("foo") }