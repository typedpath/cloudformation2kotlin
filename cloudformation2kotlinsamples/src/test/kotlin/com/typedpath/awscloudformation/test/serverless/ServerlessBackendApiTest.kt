package com.typedpath.awscloudformation.test.serverless

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
import com.typedpath.awscloudformation.test.defaultCredentialsProvider
import com.typedpath.awscloudformation.test.defaultStackName
import com.typedpath.awscloudformation.test.test
import com.typedpath.awscloudformation.test.withoutextension.getOrCreateTestArtifactS3BucketName
import com.typedpath.awscloudformation.test.zipResourceDirectory
import com.typedpath.awscloudformation.toYaml
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPut
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

// reimplements and tests this https://github.com/awslabs/serverless-application-model/tree/master/examples/2016-10-31/api_backend
class ServerlessBackendApiTest {

    val region = Regions.US_EAST_1


    fun testApi(baseUrl: String) {
        val resourceId = UUID.randomUUID()
        val url = "$baseUrl${resourceId}"

        println("testing $url")

        val contentIn = """{ "greeting":"hewo from $resourceId" }"""

        val httpclient = HttpClients.createDefault();

        val httpPut = HttpPut(url)
        httpPut.setEntity(StringEntity(contentIn, ContentType.APPLICATION_JSON));
        val putResponse = httpclient.execute(httpPut);
        println("put response.statusLine ${putResponse.statusLine}")
        Assert.assertEquals(200, putResponse.statusLine.statusCode)

        val getResponse = httpclient.execute(HttpGet(url))
        val contentOut = String(getResponse.entity.content.readBytes())
        println("get response.statusLine ${getResponse.statusLine} ${contentOut}")
        Assert.assertEquals(contentIn, contentOut)

        val deleteResponse = httpclient.execute(HttpDelete(url))
        println("delete response.statusLine ${deleteResponse.statusLine} ")
        Assert.assertEquals(200, deleteResponse.statusLine.statusCode)

        val getResponse2 = httpclient.execute(HttpGet(url))
        val contentOut2 = String(getResponse2.entity.content.readBytes())
        println("get response.statusLine ${getResponse2.statusLine} ${contentOut2}")
        Assert.assertEquals(404, getResponse2.statusLine.statusCode)

    }

    fun addSource(bucketName: String, keyName: String,
                  credentialsProvider: AWSCredentialsProvider, sourceDirectory: String) {
        val s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(region)
                .build()
        // create a zip of the source and upload

        val baos = ByteArrayOutputStream()
        zipResourceDirectory(sourceDirectory, baos)
        val metadata = ObjectMetadata()
        metadata.setContentType("application/blob")
        metadata.addUserMetadata("x-amz-meta-title", keyName)
        val inputStream = ByteArrayInputStream(baos.toByteArray())
        s3Client.putObject(bucketName, keyName, inputStream, metadata)
    }

    @Test
    fun backendApi() {

        val credentialsProvider = defaultCredentialsProvider()

        val bucketName = getOrCreateTestArtifactS3BucketName(region, credentialsProvider)
        val codePackageName = "codepackage_api_backend.zip"

        addSource(bucketName, codePackageName, credentialsProvider, "serverless/api_backend/src")

        val template = ServerlessBackendApiTemplate("s3://$bucketName/$codePackageName")
        val lambdaStackName = defaultStackName(template)
        println(toYaml(template))

        test(template, lambdaStackName, region, false) { credentialsProvider, outputs ->
            println("we made it !!")
            Assert.assertTrue("outputs should be size 1 but was ${if (outputs==null) "null" else outputs.size.toString()}", outputs != null && outputs.size == 1)
            val output = outputs.get(0)
            println("${output.outputKey} => ${output.outputValue} ")
            Assert.assertEquals(output.outputKey, "ApiUrl")
            val apiUrl = output.outputValue;
            testApi(apiUrl)
        }
    }
}


fun mainx(args: Array<String>) {
    val baseUrl = "https://kycp0anj1k.execute-api.us-east-1.amazonaws.com/Prod/resource/"
    (ServerlessBackendApiTest()).testApi(baseUrl)
}
