package com.typedpath.testdomain

class Person(var firstName: String,  var lastName: String, val address: Address)

class Address(val address1: String, val address2: String, val postcode: String)

class Team(val name: String, val members: List<Person>)


fun createSampleTeam(seed: Integer) : Team =
    Team("team$seed", listOf(Person("firstName1", "firstName2",
            Address("addr1_$seed", "addr2", "AA1${seed}AA")
            ), Person("firstName1${(seed)}b", "firstName2",
            Address("addr1_${(seed)}b", "addr2", "AA1${(seed)}bAA")
    )))
