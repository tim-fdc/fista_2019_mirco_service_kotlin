package com.freiheit.fista

/**
 * Some simple class.
 */
class User(val name: String)

/**
 * Some simple class with private ctor and field
 */
class SystemUser private constructor(private val accessLevel: String, val name: String) {
    constructor (name: String) : this(accessLevel = "HIDDEN", name = name)
}

/**
 * Notice: You need to add open to a class in order to inherit from it.
 * Notice: You need to add open to a function in order to override it.
 *
 * @DO: Only add open to classes and functions if really needed.
 * + Decision to introduce inheritance is explicit! You can even write a comment why the class is open!
 */
open class Widget(val key: String) {
    open fun jsonKey() = "{\"jsonKey\": \"$key\"}"
    fun printKey() = println(jsonKey())
}

class Circle(key: String, val radius: Double) : Widget(key) {
    override fun jsonKey() = "{\"jsonKey\": \"my-$key\"}"
}

/**
 * Unlike Java, Kotlin infers types
 */
val someString = "kotlinKnowsItIsAString"
val sub = someString.substring(3)

/**
 * @DO: Make type explicit if the variable is exposed, e.g., API.
 */
val myLastName: String = "bar"

/**
 * You must always define the return type of functions, unless...
 */
fun returnSomething(str: String): String {
    return str
}

/**
 * ...your function can be a single expression.
 */
fun returnSomethingSingleExpression(str: String) = if (str.startsWith("r")) str else str + 1



/**
 * Kotlin has interfaces and they can have functions.
 *
 * Notice: No need to add open to interfaces or interface functions in order to implement them or override their
 * functions.
 */
interface IWidget {
    fun jsonKey() = "{\"jsonKey\": \"IWidget\"}"
}

/**
 * Implement an interface.
 */
class Square : IWidget

/**
 * Implement class and interface.
 *
 * Notice: Like Java, Kotlin has no multi-inheritance of classes. Also like Java, you can extend a class and implement
 * several interfaces.
 */
class CirclingSquareWidget : IWidget, Widget(key = "MyIAndMoreWidget") {
    /**
     * NOTICE: Unlike Java override is a keyword
     */
    override fun jsonKey(): String {
        //You will have to make which super explicit by providing the type.
        //Reason: name clashes.
        //Kotlin also has super.
        return super<IWidget>.jsonKey()
    }
}

val listWithoutType: List<String> = listOf("A", "B")
val listWithType = listOf<String>("A", "B")

fun typeInferenceWillFail() {
    var listWithoutTypeVar = listOf("A", "B")
    // does not work, because List<String> != List<Int>
//    listWithoutTypeVar = listOf(1, 2)
}
