package com.typedpath.testdomain

import java.util.*

// TODO
// plan
// share 1 domain
// review spring cloud ??
// implement really basic save
// implement a search

// support send object to insert
// OR send insert instructions
// SETUP
//    create database
//    create schema
// CRUD
//    insert
//



/*interface DatabaseService {
    fun createDatabase(schemaName: String)
    fun createTable(schemaName: String, tableName: String)
    fun executeInsert(sql: String)
}*/

interface Storer {
    fun save(o: Any, object2Id: Map<Any, UUID>)
    fun load(id: UUID, strClass: String): Map<UUID, Any>
    //requires a search !
}
















