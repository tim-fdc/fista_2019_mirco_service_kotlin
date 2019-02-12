package com.freiheit.fista.coroutines

fun readHello(): String {
    Thread.sleep(200)
    return "Hello"
}

fun readWorld(): String {
    Thread.sleep(1000)
    return "World"
}

fun readHelloWorld(): String {
    return "${readHello()}, ${readWorld()}"
}

fun main() {
    print(readHelloWorld())
}