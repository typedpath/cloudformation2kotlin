package com.typedpath.testdomain

import org.junit.Test


class SerializeTest {


    class Person (val firstName: String, val lastName: String)

    @Test
    fun toJsonInsertUuidsTest() {

        val testObject = Person("hewo", "tickle")
        println(toJsonInsertUuids(testObject))

    }

}
