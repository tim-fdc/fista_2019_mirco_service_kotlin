package com.freiheit.fista

/***
 * Objects are, like in Scala, singletons.
 * Notice: An object has no constructor.
 *
 * @DO: Use them for Singletons!
 *
 */
object MySingletonService {
    /**
     * Port can be a value here, because it gets initialized in init {...}
     * @DO: Try to avoid using init too much. Assign values as early as possible.
     */
    val port: Int

    init {
        port = 8080
    }

    //objects can have functions!
    fun getData(): CustomerData {
        return CustomerData(name = "max")
    }
}

/**
 * Service having a companion object, which is an extension of the concept of “object”.
 * An object that is a companion to a particular class, and thus has access to it’s private level methods and properties.
 *
 * Using the companion object adds consistency to the language design, whereas “static” is not consistent with the
 * rest of the language design.
 *
 * A companion can be called like static methods in Java, for example, MyService.create().
 */
open class MyService(val port: Int = 8080) {
    companion object {
        /**
         * @DONT: default here and in the ctor. Stick to one default!
         */
        fun create(port: Int = 8081): MyService {
            return MyService(port)
        }
    }
}

/**
 * Possible to create singletons from classes. Notice the empty implementation "{}" at the end.
 */
val mySingletonService = object : MyService(System.getProperty("PORT")!!.toInt()) {}

object MySingletonServiceAsObject : MyService(System.getProperty("PORT")!!.toInt())