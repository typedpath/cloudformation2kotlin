package com.typedpath.aurora

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.rds.AmazonRDSClientBuilder
import com.amazonaws.services.rds.model.ModifyDBClusterRequest
import com.amazonaws.services.rdsdata.AWSRDSData
import com.amazonaws.services.rdsdata.AWSRDSDataClient
import com.amazonaws.services.rdsdata.model.ExecuteStatementRequest
import com.typedpath.testdomain.*
import jdk.nashorn.api.scripting.ScriptObjectMirror
import java.lang.StringBuilder
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

//enables rds data REST interface
fun enableHttpEndpoint(region: Regions, credentials: AWSCredentialsProvider, dbClusterIdentifier: String) {
    println("enableHttpEndpoint $region $dbClusterIdentifier")
    val client = AmazonRDSClientBuilder.standard().withRegion(region)
            .withCredentials(credentials).build()

    client.modifyDBCluster(ModifyDBClusterRequest()
            .withDBClusterIdentifier(dbClusterIdentifier)
            .withEnableHttpEndpoint(true))

}

/**
 implements an object relational mapping using aurora
 json arguments are from Serialize.kt which adds in extra fields to help
 such as db ids and db action (insert ot update), object type
 */
class AuroraStorer(val dbClusterArn: String, val secretsArn: String, val schemaName: String) {
    private val TYPE_FIELD = "type"
    //TODO make this "binary(16)" to shrink size and make sequential
    private val ID_FIELD_TYPE = "text"
    private val DB_ID_FIELD = ID_FIELD

    private fun sqlValue(json: Any): String {
        return if (json is ScriptObjectMirror) """"${json.get("_id")}""""
        else return """"$json""""
    }

    fun saveJson(strJson: String) {
        println("saving a $strJson")
        saveJson(stringToJson(strJson))
    }

    fun saveJson(json: ScriptObjectMirror, rdsDataIn: AWSRDSData? = null, requestIn: ExecuteStatementRequest? = null) {

        val rdsData = if (rdsDataIn == null) AWSRDSDataClient.builder().build() else rdsDataIn
        val request = if (requestIn == null) warmUp(rdsData) else requestIn
        request.withDatabase(schemaName)
        //TODO map parent containers
        //TODO get a list of field names
        val columnNames = json.keys
                .filter { !it.equals(TYPE_FIELD) && !it.equals(ACTION_FIELD) && !(json.get(it) is ScriptObjectMirror && (json.get(it) as ScriptObjectMirror).containsKey("0")) }

        val action = json.get(ACTION_FIELD)

        if (INSERT_ACTION.equals(action)) {
            val sql = """insert into ${json.get(TYPE_FIELD)} (${columnNames.joinToString(", ")}) values (${
            columnNames.map { """${sqlValue(json.get(it)!!)}""" }.joinToString(", ")
            })"""
            println("executing sql $sql")
            request.withSql(sql)
        } else if (UPDATE_ACTION.equals(action)) {
            val sql = """update ${json.get(TYPE_FIELD)} set ${
            columnNames.filter { !it.equals(ID_FIELD) }.map { """ $it = ${sqlValue(json.get(it)!!)}""" }.joinToString(", ")
            } where $ID_FIELD='${json.get(ID_FIELD)}'"""
            println("executing sql $sql")
            request.withSql(sql)
        }

        rdsData.executeStatement(request)
        json.values.filter { it is ScriptObjectMirror }
                .map { it as ScriptObjectMirror }
                .forEach {
                    if (!it.containsKey("0")) {
                        saveJson(it as ScriptObjectMirror, rdsData, request)
                    } else {
                        it.values.forEach { saveJson(it as ScriptObjectMirror, rdsData, request) }
                    }
                }
    }

    fun delete(rootClass: KClass<*>, id: UUID, rdsDataIn: AWSRDSData? = null, requestIn: ExecuteStatementRequest? = null) {
        val rdsData = if (rdsDataIn == null) AWSRDSDataClient.builder().build() else rdsDataIn
        val request = if (requestIn == null) warmUp(rdsData) else requestIn
        request.withDatabase(schemaName)
        val sql = """delete from ${rootClass.simpleName} where $DB_ID_FIELD="$id" """
        request.withSql(sql)
        println("executing $sql")
        rdsData.executeStatement(request)
    }

    fun loadMultiAsJsonDeep(rootClass: KClass<*>, strWhere: String = ""): String {
        val rdsData = AWSRDSDataClient.builder().build()
        val request = warmUp(rdsData)
        request.withDatabase(schemaName)
        val sql = """ select $DB_ID_FIELD from ${rootClass.simpleName} $strWhere """
        println("executing $sql")
        return """[
         ${rdsData.executeStatement(request.withSql(sql)).records.map {
            loadAsJsonDeep(rootClass, UUID.fromString(it.get(0).stringValue), rdsData, request)
        }.joinToString(",\r\n")
        }] """
    }

    private fun isListProperty(p: KProperty1<*, *>) = (p.returnType.classifier as KClass<*>).isSubclassOf(List::class)

    fun loadAsJsonDeep(rootClass: KClass<*>, id: UUID, rdsDataIn: AWSRDSData? = null, requestIn: ExecuteStatementRequest? = null): String {
        val rdsData = if (rdsDataIn == null) AWSRDSDataClient.builder().build() else rdsDataIn
        val request = if (requestIn == null) warmUp(rdsData) else requestIn
        request.withDatabase(schemaName)
        //build selectable columns
        val columns = mutableListOf(DB_ID_FIELD)
        val column2ComplexProperty = mutableMapOf<String, KProperty1<*, *>>()
        val column2ListProperty = mutableMapOf<String, KProperty1<*, *>>()
        rootClass.memberProperties.forEach {
            if (isListProperty(it)) {
                column2ListProperty.put(it.name, it)
            } else if (isComplex(it)) {
                column2ComplexProperty.put(it.name, it)
                columns.add(it.name)
            } else {
                columns.add(it.name)
            }
        }
        val sql = """select ${columns.joinToString(", ")} from ${rootClass.simpleName} where $DB_ID_FIELD="$id" """
        request.withSql(sql)
        println("executing $sql")
        val result = rdsData.executeStatement(request)
        val sb = StringBuilder()
        sb.append("{")
        val record = result.records[0]
        println("found ${result.records.size}  with columns ${columns}  metadata:${result.columnMetadata}")
        for (i in 0..record.size - 1) {
            if (i > 0) sb.append(",")
            val columnName = columns[i]
            var theValue = record.get(i).stringValue
            if (column2ComplexProperty.containsKey(columnName)) {
                val subClass = column2ComplexProperty.get(columnName)!!.returnType!!.classifier as KClass<*>
                theValue = loadAsJsonDeep(subClass, UUID.fromString(theValue), rdsDataIn, requestIn)
            } else {
                theValue = """"$theValue""""
            }
            sb.append(""" "${columns[i]}":${theValue} """)
        }
        column2ListProperty.forEach {
            val childClass = it.value.returnType.arguments.get(0).type!!.classifier as KClass<*>
            val subJson = loadMultiAsJsonDeep(childClass, """where ${rootClass.simpleName!!.decapitalize()} = "$id" """)
            sb.append(""", "${it.key}": $subJson""")
        }

        sb.append("}")
        return sb.toString()
    }

    private fun isComplex(pClass: KClass<*>) = !pClass.qualifiedName!!.startsWith("kotlin")
    private fun isComplex(p: KProperty1<*, *>) = p.returnType.classifier is KClass<*> && isComplex(p.returnType.classifier as KClass<*>)

    private fun columnType(p: KProperty1<*, *>): String {
        return if (isComplex(p)) {
            ID_FIELD_TYPE
        } else {
            "text"
        }
    }

    //should really use ordered uuid https://www.percona.com/blog/2014/12/19/store-uuid-optimized-way/
    private fun tableCreate(theClass: KClass<*>, parentClass: KClass<*>?): String {
        val sb = StringBuilder()
        sb.append("create table ${theClass.simpleName} (")
        sb.append("$DB_ID_FIELD $ID_FIELD_TYPE NOT NULL, ")
        // filter out 1 to many member properties
        sb.append(theClass.memberProperties
                .filter {
                    !(it.returnType.classifier as KClass<*>).isSubclassOf(List::class)
                }
                .map { "${it.name} ${columnType(it)}" }.joinToString(", "))
        // if the referring member property in the parent class is a 1 to many add a parent ref
        if (parentClass != null) {
            parentClass.memberProperties.filter {
                val pType = (it.returnType.classifier as KClass<*>)
                pType.isSubclassOf(List::class) && (it.returnType.arguments.get(0).type!!.classifier as KClass<*>) == theClass
            }.forEach {
                sb.append(", ${parentClass.simpleName!!.decapitalize()} ${ID_FIELD_TYPE}")
            }
        }

        sb.append(")")
        return sb.toString()
    }

    private fun warmUp(rdsData: AWSRDSData): ExecuteStatementRequest {
        val maxTime: Long = 20000
        val request = ExecuteStatementRequest()
                .withResourceArn(dbClusterArn)
                .withSecretArn(secretsArn)
                .withSql("select count(*) from information_schema.tables")
        val startTime = System.currentTimeMillis()
        var complete = false
        while (!complete && maxTime > (System.currentTimeMillis()-startTime)) {
            try {
                rdsData.executeStatement(request)
                complete = true
            } catch (ex: Exception) {
                println("just warming up $ex")
                try {Thread.sleep(100)}
                catch (ex: Exception) {}
            }
        }
        return request
    }

    fun createSchema(rootClass: KClass<*>, createDb: Boolean = true) {
        val classesForTables = mutableSetOf<KClass<*>>()
        val relationShipProperties = mutableSetOf<KProperty1<*, *>>()
        val class2parent = mutableMapOf<KClass<*>, KClass<*>>()
        val isViaList = mutableSetOf<KClass<*>>()
        // for each related class create table
        crawl(rootClass) { pClass, theProperty, context, isList ->
            if (isComplex(pClass)) {
                relationShipProperties.add(theProperty)
            }
            if (!classesForTables.contains(pClass) && isComplex(pClass)) {
                classesForTables.add(pClass)
                if (!context.isEmpty()) {
                    class2parent.put(pClass, context.peek())
                    if (isList) isViaList.add(pClass)
                }
                true
            } else {
                false
            }
        }
        classesForTables.add(rootClass)

        val rdsData = AWSRDSDataClient.builder()
                .build()
        warmUp(rdsData)

        var request = ExecuteStatementRequest()
                .withResourceArn(dbClusterArn)
                .withSecretArn(secretsArn)

        if (createDb) rdsData.executeStatement(request.withSql("create database $schemaName"))
        request.withDatabase(schemaName)
        classesForTables.forEach {
            val strTable = tableCreate(it, class2parent.get(it))
            println("${it.simpleName} => ${strTable};")
            println(" ${it.simpleName} - isList:${isViaList.contains(it)} - parent-> ${class2parent.get(it)}")
            if (createDb) rdsData.executeStatement(request.withSql(strTable))
        }
        relationShipProperties.forEach {
            println("${it}->not creating relationship table assuming contained !")
        }

    }
}
