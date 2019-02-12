package com.freiheit.fista

/**
 * Kotlin has default values for functions and ctors. It als supports named parameters <3.
 *
 * @DONT: Define defaults, because you can. It get's hard to understand why something is working out of the box.
 * @DONT Do not create default instances of "complex" components such as services or DAOs. These components usually
 * handle a lot of work, do the heavy lifting and as such may be the source of many bugs. Make their creation, their
 * existence, as explicit as possible.
 *
 * @DO: Use named parameters when calling a function or class having many parameters, because it increases the
 * readability in particular during code reviews.
 * @DO: Initialize collections empty, because you avoid nullable collections.
 */
class Dept(val name: String, val subDept: Dept? = null, val employees: List<String> = emptyList())

// needed below...
class SearchParameter(val title: String?, val dept: Dept?)

fun main() {
    val nullString: String? = null

    // bang bang !! throws NPE if nullString is null
    try {
        val a: String? = nullString?.plus("test")
        if (a!!.isNotEmpty()) {
            println("length > 0")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // create an instance without new
    val searchParameter = SearchParameter(title = "Coffee Makers <3", dept = Dept("Bean makers", subDept = null))

    val name = searchParameter.dept?.name
    val name2 = searchParameter.dept?.subDept?.name

    println("name: $name, name2: $name2")

    // systemValue is of type String! (platform type).
    // type is defined as String! := (String? or String) (wtf?!)
    val systemValue: String? = System.getProperty("KOTLIN_IS_GREAT")
    println(systemValue?.length)
}
