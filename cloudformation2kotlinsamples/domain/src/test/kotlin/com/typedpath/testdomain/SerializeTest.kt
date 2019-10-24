package com.typedpath.testdomain

import org.junit.Test
import java.util.*


class SerializeTest {


    //class Person (val firstName: String, val lastName: String)

    @Test
    fun toJsonInsertUuidsTest() {
        val testObject = createSampleTeam(Integer(4))//Person("hewo", "tickle")
        val object2Id = mutableMapOf<Any, UUID>()
        println(toJsonInsertUuidsTypeInfo(testObject, null, false, object2Id))
        val dirty = mutableSetOf<Any>()
        dirty.add(testObject.members.get(0).address)
        println(toJsonInsertUuidsTypeInfo(testObject, null, false, object2Id, dirty))

    }

}
