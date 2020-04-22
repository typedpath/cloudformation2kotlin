package com.typedpath.awscloudformation.test.serverless.typesafebackendapi

import com.amazonaws.regions.Regions
import com.typedpath.awscloudformation.test.util.*
import com.typedpath.awscloudformation.toYaml
import org.junit.Test
import com.typedpath.aurora.AuroraStorer
import com.typedpath.aurora.enableHttpEndpoint
import com.typedpath.awscloudformation.serverlessschema.ServerlessCloudformationTemplate
import com.typedpath.awscloudformation.test.TemplateFactory
import com.typedpath.awscloudformation.test.uploadCodeToS3
import com.typedpath.awscloudformation.test.withoutextension.getOrCreateTestArtifactS3BucketName
import com.typedpath.testdomain.*
import org.junit.Assert

import java.lang.RuntimeException

import java.util.UUID

fun initialiseDb(secretArn: String, dbClusterArn: String, schemaName: String, createDb: Boolean = true) {
    val storer = AuroraStorer(dbClusterArn, secretArn, schemaName)
    storer.createSchema(Team::class, createDb)
}

fun testAuroraServerlessApi(baseUrl: String) {
    val saver = Saver(baseUrl)
    //just a warm up
    try {
        saver.retrieve(Team::class, UUID.randomUUID())
    } catch (ex: Exception) {
        System.out.println("just a warm up $ex")
    }

    val id2InputTeam = mutableMapOf<UUID, Team>()
    //save some sample objects
    for (i in 1..2) {
        val team = createSampleTeam(Integer(i))
        val id = saver.save(team)
        id2InputTeam.put(id, team)
        println("testing retrieve ${Team::class} $id")
        val teamOut = saver.retrieve(Team::class, id)
        println("retrieved $teamOut")
        Assert.assertEquals(team.members.get(0).address.address2, teamOut.members.get(0).address.address2)
    }
    //retrieve the sample object
    val teamsOut = saver.retrieveMulti(Team::class, id2InputTeam.keys.toList())
    println("retrieved teams:\r\n$teamsOut")
    id2InputTeam.forEach { entry ->
        //do the retrieved values match ?
        val teamOut = teamsOut.filter { entry.key.equals(saver.id(it)) }.firstOrNull()
        println("matching team ${entry.value} with multi team out ${teamOut} ${entry.key}")
        Assert.assertEquals(entry.value.members.get(0).address.address2, teamOut!!.members.get(0).address.address2)
    }
    //delete a sample object
    saver.delete(id2InputTeam.values.first())
    val teamsOut2 = saver.retrieveMulti(Team::class, id2InputTeam.keys.toList())
    //are there 1 less sample object retrieved ?
    Assert.assertEquals(teamsOut.size - 1, teamsOut2.size)

    val entryToAmend = id2InputTeam.entries.last()
    val newFirstName = "Moddy"
    val newLastName = "Fied"
    val personToAmend = entryToAmend.value.members.get(0)
    personToAmend.firstName = newFirstName
    personToAmend.lastName = newLastName
    //modify a sampleobject
    saver.save(entryToAmend.value, setOf(personToAmend))

    val retrievedAmendedTeam = saver.retrieve(Team::class, entryToAmend.key)
    // has  the modification taken ?
    Assert.assertEquals(retrievedAmendedTeam.members.get(0).lastName, newLastName)

}


class AuroraBackendApiTest : TemplateFactory {

    val region = Regions.US_EAST_1
    val dbName = "testdb${defaultCurrentDateTimePattern().replace("-", "")}"
    val codeRelativeLocation = "lambdas/databaseio/build/libs/databaseio-fat-testonly.jar"
    val credentialsProvider = defaultCredentialsProvider()
    val codeBucketName = getOrCreateTestArtifactS3BucketName(region, credentialsProvider)
    val bucketNamePrefix = defaultCurrentDateTimePattern() + "rdsaccess"
    val schemaName = "testschema"

    // based on this https://aws.amazon.com/blogs/database/using-the-data-api-to-interact-with-an-amazon-aurora-serverless-mysql-database/

    fun insertCodeUriIn() = uploadCodeToS3("../$codeRelativeLocation"
            , region, codeBucketName, codeRelativeLocation, credentialsProvider)


    override fun createTemplate(): ServerlessCloudformationTemplate {
        return AuroraBackendApiTemplate(dbName, "admin", "s3://serverless-testutils-artifact-bucket-us-east-128082019-151241/lambdas/databaseio/build/libs/databaseio-fat-testonly.jar", bucketNamePrefix, schemaName)
    }

    @Test
    fun deployAndTest() {

        val template = AuroraBackendApiTemplate(dbName, "admin", insertCodeUriIn(), bucketNamePrefix, schemaName)
        val stackName = defaultStackName(template.javaClass)
        println(toYaml(template))

        createStack(template, stackName, region, false) { credentialsProvider, outputs ->
            println("made it :-)")
            outputs.forEach {
                println("${it.outputKey}=>${it.outputValue}")
            }
            val databaseArn = outputs.filter { it.outputKey.equals(AuroraBackendApiTemplate::databaseArn.name) }.firstOrNull()
            if (databaseArn == null) {
                throw RuntimeException("cant find databaseUrn in ${outputs}")
            }
            val secretArn = outputs.filter { it.outputKey.equals(AuroraBackendApiTemplate::secretArn.name) }.firstOrNull()
            if (secretArn == null) {
                throw RuntimeException("cant find secretArn in ${outputs}")
            }
            val dbClusterIdentifier = outputs.filter { it.outputKey.equals(AuroraBackendApiTemplate::dbClusterIdentifier.name) }.firstOrNull()
            if (dbClusterIdentifier == null) {
                throw RuntimeException("cant find dbClusterIdentifier in ${outputs}")
            }
            val apiUrl = outputs.filter { it.outputKey.equals(AuroraBackendApiTemplate::apiUrl.name) }.firstOrNull()
            if (apiUrl == null) {
                throw RuntimeException("cant find apiUrl in ${outputs}")
            }
            //this cant be done in the template
            enableHttpEndpoint(region, credentialsProvider, dbClusterIdentifier.outputValue)
            // try calling function
            initialiseDb(secretArn.outputValue, databaseArn.outputValue, schemaName)
            //testDbApi(secretArn.outputValue, databaseArn.outputValue, "testx")
            testAuroraServerlessApi(apiUrl.outputValue)

        }

    }

}

fun mainx(args: Array<String>) {
    //com-typedpath-awscloudformation-test-serverles-db-1hoamwalltjfp.cluster-c4pff6tta7g4.us-east-1.rds.amazonaws.com
                                   //https://hj27anqr49.execute-api.us-east-1.amazonaws.com/Prod
    testAuroraServerlessApi("https://hj27anqr49.execute-api.us-east-1.amazonaws.com/Prod/resource/")
}