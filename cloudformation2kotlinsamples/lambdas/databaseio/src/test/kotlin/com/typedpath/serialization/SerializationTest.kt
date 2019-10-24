package com.typedpath.serialization

import com.typedpath.testdomain.*
import org.junit.Assert
import org.junit.Test
import java.util.*

//import events.S3PutJ

class SerializationTest {

@Test
fun testInOut() {
    println("here ")
    val person = Person("Andrew", "Eldritch", Address("addr1", "addr2", "AL5 8HJ"))
    val team = Team("team1", listOf(person))
    val object2IdIn = mutableMapOf<Any, UUID>()
    val jsonMapping = toJsonInsertUuidsTypeInfo(team, null, false, object2IdIn)
    println("${jsonMapping} ${object2IdIn}")
    val object2IdOut = mutableMapOf<Any, UUID>()
    val teamOut = toObject(jsonMapping, Team::class, object2IdOut) as Team
    println("$teamOut")
    println(object2IdIn.map{
        "${it.value} -> ${it.key.javaClass.simpleName}"
    }.joinToString(",\r\n"))
    ///TODO implement object diff
    Assert.assertEquals(person.firstName, teamOut.members[0].firstName)
    Assert.assertEquals(person.lastName, teamOut.members[0].lastName)
    Assert.assertEquals(person.address.address1, teamOut.members[0].address.address1)
    Assert.assertEquals(team.name, teamOut.name)
}


}

fun main(args: Array<String>) {
    /*val inputData = mutableMapOf<String, Any>()
    val records = mutableListOf<S3PutJ.Items>()
    inputData.put("records", records)
    val record = S3PutJ.Items()
    records.add(record)
    record.setAwsRegion("us_east_1")

    val s3Put = deserialize<S3PutJ>(S3PutJ::class, inputData)
    println("result: ${s3Put.records}")
*/
}
