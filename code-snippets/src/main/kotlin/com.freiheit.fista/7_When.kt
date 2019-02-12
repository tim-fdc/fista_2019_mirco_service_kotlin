package com.freiheit.fista

import kotlin.random.Random

/**
 * The [when] keyword can be used as a multi-branch if
 */
fun multiBranch() {
    when {
        false -> print("this is never true")
        1 == 2 -> print("we broke the math")
        "ABC".contains("B") -> print("B is part of ABC")
        1 / 0 is Number -> print("this better never be reached")
        else -> print("I don't know what to believe anymore")
    }
}

/**
 * Using [when] you can return values
 */
val result = when {
    //@DONT: if this is a long running operation, because result is a value.
    Random.nextInt() % 69 == 0 -> "We got lucky!"
    else -> "Try again :C"
}


/**
 * [sealed] classes allow restricted class hierarchies
 */
sealed class MyType {
    object A : MyType()
    data class B(val someValue: Any) : MyType() {
        fun getTypeDescription() = "the type is B!"
    }
    sealed class C: MyType() {
        object C1: C()
        object C2: C()
//        object B1: B("Inheritance from sealed classes is restricted to the scope!")
    }

}
class D(val someValue: Any) : MyType()

// Sealed classes are abstract and cannot be instantiated
//val impossible = MyType()

/**
 * [sealed] classes are like enums on steroids
 *
 * @DO Use the [when] statement to switch over sealed types
 * @DONT Define an else branch if the logic does not explicitly require it,
 *       otherwise you will loose the safety
 */
private fun someFunction(value: MyType): String {
    return when (value) {
        MyType.A -> "the type is A!"    // match by reference
        is MyType.B -> {                // match by type
            value.getTypeDescription()  // smart-cast
        }
        MyType.C.C1 -> "the type is C1!"
        MyType.C.C2 -> "the type is C2!"
        is D -> "The when expression on sealed types has to be exhaustive!"
    }
}

/**
 * @DO Use [sealed] classes for state modelling
 *
 * Notice: Together with the [when] statements you can create state machines,
 *         which makes your code hard to break by preventing it from getting
 *         into insensible or unhandled states.
 */
sealed class Cart {
    object Blank: Cart()
    data class Active (val id: String, val items: MutableList<Any>)
    data class Submitted(val id: String, val items: List<Any>)
}


/**
 * [is] and [as] can be used for type checks and casts
 *
 * @DO Use the [is] keyword for type checking and smart-casting
 * @DONT Use the [as] keyword, especially without making the result optional
 */
private fun cast(value: MyType): Boolean {
    (value as? MyType.B)?.getTypeDescription() // Does something if value could be casted

    (value as MyType.B).getTypeDescription() // This can throw!

    return value is MyType.C
}