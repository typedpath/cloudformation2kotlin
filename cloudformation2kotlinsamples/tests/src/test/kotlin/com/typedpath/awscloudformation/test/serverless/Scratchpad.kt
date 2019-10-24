package com.typedpath.awscloudformation.test.serverless

import com.typedpath.awscloudformation.test.serverless.typesafebackendapi.testAuroraServerlessApi

/**
 * this is a place to run aribitary code
 */
fun main(args: Array<String>) {
    val secretArn ="arn:aws:secretsmanager:us-east-1:950651224730:secret:testdb21oct19115211-AuroraUserSecret-hEzh6g"
    val dbClusterArn = "arn:aws:rds:us-east-1:950651224730:cluster:com-typedpath-awscloudformation-test-serverles-db-rp63y8k1mlve"
    val schemaName = "testschema"
    var apiUrl = "https://888bctluz0.execute-api.us-east-1.amazonaws.com/Prod/resource/"
/*    val storer = AuroraStorer(dbClusterArn, secretArn, schemaName)

    val teamIn = createSampleTeam(Integer(5))
    val object2Id = mutableMapOf<Any, UUID>()
    val json = toJsonInsertUuidsTypeInfo(teamIn, null, false, object2Id)
    storer.saveJson(json)
    val teamId = object2Id.get(teamIn)!!
    val jsonOut = storer.loadAsJsonDeep(Team::class, teamId)
    //unmapped query result
    val teamOut = toObject(jsonOut, Team::class,  mutableMapOf()) as Team
    Assert.assertEquals(teamIn.members.get(0).address.address1, teamOut.members.get(0).address.address1)

    teamIn.members.get(0).firstName = "moddy"
    val dirty = setOf(teamIn.members.get(0))
    val jsonMod = toJsonInsertUuidsTypeInfo(teamIn, null, false, object2Id, dirty)
    storer.saveJson(jsonMod)
    val jsonOutMod = storer.loadAsJsonDeep(Team::class, teamId)
    val teamOutMod = toObject(jsonOutMod, Team::class,  mutableMapOf()) as Team
    Assert.assertEquals(teamIn.members.get(0).firstName, teamOutMod.members.get(0).firstName)
*/

    //val json = storer.loadAsJsonDeep(Team::class, UUID.fromString("ffa98ee3-ff99-42ea-8884-8a0eb563a659"))

    // val json = storer.loadAsJsonDeep(Team::class, UUID.fromString("2c0351b6-e08a-438a-b53c-2182e34ae3c8"))
    //println(json)
    //val teamOut = toObject(json, Team::class,  mutableMapOf())
    //initialiseDb(secretArn, dbClusterArn, schemaName)
    //val json = toJsonInsertUuidsTypeInfo(createSampleTeam(Integer(3)), mutableMapOf())
    //storer.saveJson(json)
    testAuroraServerlessApi(apiUrl)

}

