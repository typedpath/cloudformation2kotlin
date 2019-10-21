package com.typedpath.awscloudformation.test.serverless

import com.sun.jndi.cosnaming.IiopUrl
import com.typedpath.aurora.AuroraStorer
import com.typedpath.testdomain.*
import java.util.*
import kotlin.reflect.full.memberProperties

fun main(args: Array<String>) {
    val secretArn ="arn:aws:secretsmanager:us-east-1:950651224730:secret:testdb21oct19115211-AuroraUserSecret-hEzh6g"
    val dbClusterArn = "arn:aws:rds:us-east-1:950651224730:cluster:com-typedpath-awscloudformation-test-serverles-db-rp63y8k1mlve"
    var apiUrl = "https://5wuakdalq2.execute-api.us-east-1.amazonaws.com/Prod/resource/"
    val schemaName = "testschema"
    val storer = AuroraStorer(dbClusterArn, secretArn, schemaName)
    //val json = storer.loadAsJsonDeep(Team::class, UUID.fromString("ffa98ee3-ff99-42ea-8884-8a0eb563a659"))
    //println(json)
    /*
    resolve this :
    executing select _id, members, name from Team where id="c8efe9eb-69c9-4adb-8e04-10fb5538ae81"

14:09:03
Unknown column 'members' in 'field list' (Service: AWSRDSData; Status Code: 400; Error Code: BadRequestException; Request ID: d72e3a34-1602-4822-a30f-2ebbb4f411a9): com.amazonaws.services.rdsdata.model.BadRequestException com.amazonaws.services.rdsdata.model.BadRequestException: Unknown column 'members' in 'field list' (Service: AWSRDSData; Status Code: 400; Error Code: BadRequestException; Reques

     */


    //initialiseDb(secretArn, dbClusterArn, schemaName)
    //val json = toJsonInsertUuidsTypeInfo(createSampleTeam(Integer(3)), mutableMapOf())
    //storer.saveJson(json)
    testAuroraServerlessApi(apiUrl)

}

