package com.typedpath.awscloudformation.test.serverless

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.rds.AmazonRDSClientBuilder
import com.amazonaws.services.rds.model.ModifyDBClusterRequest
import com.typedpath.awscloudformation.test.util.*
import com.typedpath.awscloudformation.toYaml
import org.junit.Test
import com.typedpath.aurora.AuroraStorer
import com.typedpath.awscloudformation.serverlessschema.ServerlessCloudformationTemplate
import com.typedpath.awscloudformation.test.TemplateFactory
import com.typedpath.awscloudformation.test.uploadCodeToS3
import com.typedpath.awscloudformation.test.withoutextension.getOrCreateTestArtifactS3BucketName
import com.typedpath.testdomain.*
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPut
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.junit.Assert
import java.lang.RuntimeException
import java.net.URLEncoder
import java.util.*
import org.apache.http.util.EntityUtils
import java.util.UUID



private fun enableHttpEndpoint(region: Regions, credentials: AWSCredentialsProvider, dbClusterIdentifier: String) {
    println("enableHttpEndpoint $region $dbClusterIdentifier")
    val client = AmazonRDSClientBuilder.standard().withRegion(region)
            .withCredentials(credentials).build()

    client.modifyDBCluster(ModifyDBClusterRequest()
            .withDBClusterIdentifier(dbClusterIdentifier)
            .withEnableHttpEndpoint(true))

}

fun initialiseDb(secretArn: String, dbClusterArn: String, schemaName: String, createDb: Boolean = true) {
   val storer = AuroraStorer(dbClusterArn, secretArn, schemaName)
    storer.createSchema(Team::class, createDb)
}

fun testAuroraServerlessApi(baseUrl: String) {
    val resourceId = UUID.randomUUID()
    val putUrl = "${baseUrl}"

    println("testing  $putUrl")


    var httpclient = HttpClients.createDefault()

    val httpPut = HttpPut(putUrl)

    val uuids = mutableListOf<UUID>()

    val  object2Id =   mutableMapOf<Any, UUID>()
    for (i in 1 .. 2) {
        //val address = Address("addr1", "addr2", "al23ql")
        //val person =  Person("david", "bowie",address)
        val team = createSampleTeam(Integer(1))
        val json = toJsonInsertUuidsTypeInfo(team, object2Id)
        httpPut.setEntity(StringEntity(json, ContentType.APPLICATION_JSON))
        val putResponse = httpclient.execute(httpPut)
        println("put response.statusLine ${putResponse.statusLine}")
        Assert.assertEquals(200, putResponse.statusLine.statusCode)
        val uuid =  object2Id.get(team)
        //TODO fix this
        val getUrl = "${baseUrl}${URLEncoder.encode(Team::class.qualifiedName)}/${URLEncoder.encode(uuid.toString())}"

        val httpGet = HttpGet(getUrl)
        println("testing get $getUrl")
        httpclient = HttpClients.createDefault()
        val getResponse = httpclient.execute(httpGet)
        println("get response.statusLine ${getResponse.statusLine}")
        Assert.assertEquals(200, getResponse.statusLine.statusCode)
//   alos could       val contentOut2 = String(getResponse2.entity.content.readBytes())
        val responseString = EntityUtils.toString(getResponse.entity, "UTF-8")
        println("get response.entity.content ${responseString}")
        uuids.add(uuid!!)
    }
    val deleteUrl = "${baseUrl}${Team::class.qualifiedName}/${uuids[0]}"
    val httpDelete = HttpDelete(deleteUrl)
    println("testing delete $deleteUrl")
    val deleteResponse = httpclient.execute(httpDelete)
    Assert.assertEquals(200, deleteResponse.statusLine.statusCode)

    val httpGetMulti = HttpGet("${baseUrl}${URLEncoder.encode(Team::class.qualifiedName)}")
    val getMultiResponse = httpclient.execute(httpGetMulti)
    println("get multi response.statusLine ${getMultiResponse.statusLine}")
    Assert.assertEquals(200, getMultiResponse.statusLine.statusCode)
//   also could val contentOut2 = String(getResponse2.entity.content.readBytes())
    val responseString = EntityUtils.toString(getMultiResponse.entity, "UTF-8")
    println("get multi response content ${responseString}")

}

class AuroraBackendApiTest : TemplateFactory{

    val region = Regions.US_EAST_1
    val dbName = "testdb${defaultCurrentDateTimePattern().replace("-", "")}"
    val codeRelativeLocation = "lambdas/databaseio/build/libs/databaseio-fat-testonly.jar"
    val credentialsProvider = defaultCredentialsProvider()
    val codeBucketName = getOrCreateTestArtifactS3BucketName(region, credentialsProvider)
    val bucketNamePrefix = defaultCurrentDateTimePattern() + "rdsaccess"
    val schemaName = "testschema"

    // based on this https://aws.amazon.com/blogs/database/using-the-data-api-to-interact-with-an-amazon-aurora-serverless-mysql-database/

    val insertCodeUriIn = uploadCodeToS3("../$codeRelativeLocation"
         , region, codeBucketName, codeRelativeLocation, credentialsProvider)


    override fun createTemplate(): ServerlessCloudformationTemplate {
        return AuroraBackendApiTemplate(dbName, "admin", insertCodeUriIn, bucketNamePrefix, schemaName)
    }

    @Test
    fun deployAndTest() {

        val template = createTemplate()
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