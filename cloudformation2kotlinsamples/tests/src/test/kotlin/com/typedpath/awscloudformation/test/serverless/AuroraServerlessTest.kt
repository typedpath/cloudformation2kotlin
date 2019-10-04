package com.typedpath.awscloudformation.test.serverless

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.rds.AmazonRDSClientBuilder
import com.amazonaws.services.rds.model.ModifyDBClusterRequest
import com.typedpath.awscloudformation.test.util.*
import com.typedpath.awscloudformation.toYaml
import org.junit.Test
import com.amazonaws.services.rdsdata.model.ExecuteStatementRequest
import com.amazonaws.services.rdsdata.AWSRDSDataClient
import com.typedpath.awscloudformation.serverlessschema.ServerlessCloudformationTemplate
import com.typedpath.awscloudformation.test.TemplateFactory
import org.junit.Assert
import java.lang.RuntimeException

private fun enableHttpEndpoint(region: Regions, credentials: AWSCredentialsProvider, dbClusterIdentifier: String) {
    println("enableHttpEndpoint $region $dbClusterIdentifier")
    val client = AmazonRDSClientBuilder.standard().withRegion(region)
            .withCredentials(credentials).build()

    client.modifyDBCluster(ModifyDBClusterRequest()
            .withDBClusterIdentifier(dbClusterIdentifier)
            .withEnableHttpEndpoint(true))

}

private fun testDb(secretUrn: String, dbClusterUrn: String, dbName: String) {
    val rdsData = AWSRDSDataClient.builder()
            .build()
    //warm up
    try {
        rdsData.executeStatement(ExecuteStatementRequest()
                .withResourceArn(dbClusterUrn)
                .withSecretArn(secretUrn)
                .withSql("select * from information_schema.tables"))
    } catch (ex: Exception) {
        println("just warming up $ex")
    }

    var request = ExecuteStatementRequest()
            .withResourceArn(dbClusterUrn)
            .withSecretArn(secretUrn)

    rdsData.executeStatement(request.withSql("create database $dbName"))
    request.withDatabase(dbName)
    val tablename = "test"
    rdsData.executeStatement(request.withSql("create table $dbName.$tablename (name text)"))
    val testValue = "hello $dbName world"
    rdsData.executeStatement(request.withSql("insert into $dbName.$tablename values ( '$testValue' )"))
    val result = rdsData.executeStatement(request.withSql("select * from $dbName.$tablename"))
    val returnedValue = result.records[0].get(0).stringValue
    println("record result 0 ${returnedValue}")
    Assert.assertEquals(testValue, returnedValue)
}

class AuroraServerlessTest : TemplateFactory{

    val region = Regions.US_EAST_1
    val dbName = "testdb${defaultCurrentDateTimePattern().replace("-", "")}"

    // based on this https://aws.amazon.com/blogs/database/using-the-data-api-to-interact-with-an-amazon-aurora-serverless-mysql-database/

    override fun createTemplate(): ServerlessCloudformationTemplate {
        return AuroraServerlessTemplate(dbName, "admin")
    }

        @Test
    fun db() {

        val stackName = defaultStackName(AuroraServerlessTemplate::class.java)
        val template = createTemplate()
        println(toYaml(template))

        createStack(template, stackName, region, false) { credentialsProvider, outputs ->
            println("made it :-)")
            outputs.forEach {
                println("${it.outputKey}=>${it.outputValue}")
            }
            val databaseArn = outputs.filter { it.outputKey.equals(AuroraServerlessTemplate::databaseArn.name) }.firstOrNull()
            if (databaseArn == null) {
                throw RuntimeException("cant find databaseUrn in ${outputs}")
            }
            val secretArn = outputs.filter { it.outputKey.equals(AuroraServerlessTemplate::secretArn.name) }.firstOrNull()
            if (secretArn == null) {
                throw RuntimeException("cant find secretArn in ${outputs}")
            }
            val dbClusterIdentifier = outputs.filter { it.outputKey.equals(AuroraServerlessTemplate::dbClusterIdentifier.name) }.firstOrNull()
            if (dbClusterIdentifier == null) {
                throw RuntimeException("cant find dbClusterIdentifier in ${outputs}")
            }
            //this cant be done in the template
            enableHttpEndpoint(region, credentialsProvider, dbClusterIdentifier.outputValue)
            testDb(secretArn.outputValue, databaseArn.outputValue, "testx")

        }

    }

}