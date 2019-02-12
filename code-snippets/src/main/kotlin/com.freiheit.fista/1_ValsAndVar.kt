package com.freiheit.fista

/**
 * Kotlin has val, var, and const. val is an immutable variable, var is mutable and const is constant, but
 * only works for simple types such as [Int] or [String].
 *
 * @DO: use val whenever possible, because val is immutable.
 * @DO: use const whenever possible, because it is a compiler optimization.
 */
fun valIsImmutable() {
    println("val is immutable...")
    val a: Int = 5

    // re-assignment Does not work
    // a = 5

    val immuList = listOf("hello", "kotlin")
    println("immuList $immuList")

    val mutList = mutableListOf("1")

    // addind items to an immutable list, returns a new list
    val immuList1 = immuList.plus("I am immutable")
    println("immuList1 $immuList1")
}

const val DEFAULT_PORT: Int = 8080

/**
 * @DO: Initialize values globally if they are global. See below make them even static.
 * @DONT: Global variables, because somebody may change it!
 *
 * Notice: The scope is the current file, i.e., they can be imported via file name.
 */
val immuList = listOf("hello", "kotlin")
private val privateImmuList = listOf("hello", "kotlin")

//DONT!
var varString = "hello"

fun main() {
    valIsImmutable()
}