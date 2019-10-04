package com.typedpath.awscloudformation.test.serverless

import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.model.InvocationType
import com.amazonaws.services.lambda.model.InvokeRequest
import com.typedpath.awscloudformation.CloudFormationTemplate
import com.typedpath.awscloudformation.test.TemplateFactory
import com.typedpath.awscloudformation.test.pipeline.PipelineCloudFormationTemplate
import com.typedpath.awscloudformation.test.util.addSource
import com.typedpath.awscloudformation.test.util.createStack
import com.typedpath.awscloudformation.test.util.defaultCredentialsProvider
import com.typedpath.awscloudformation.test.util.defaultStackName
import com.typedpath.awscloudformation.test.withoutextension.getOrCreateTestArtifactS3BucketName
import com.typedpath.awscloudformation.toYaml
import org.junit.Assert
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import java.nio.ByteBuffer


class LambdaServerlessTest : TemplateFactory {

    val region = Regions.US_EAST_1
    val strDateTime = (DateTimeFormatter.ofPattern("ddMMMyy-HHmmss")).format(LocalDateTime.now()).toLowerCase()
    val credentialsProvider = defaultCredentialsProvider()
    val bucketName =   getOrCreateTestArtifactS3BucketName(region, credentialsProvider) //"$name$strDateTime"
    val functionName = "f${this.javaClass.simpleName}$strDateTime"
    val codePackageName = "codepackage.zip"


    override fun createTemplate(): CloudFormationTemplate {
        return  LambdaServerlessTemplate("s3://$bucketName/$codePackageName", functionName)
    }

    // reimplements this https://github.com/awslabs/serverless-application-model/blob/master/examples/2016-10-31/hello_world/template.yaml
    @Test
    fun basic() {
        addSource(bucketName, codePackageName, credentialsProvider, "serverless/basic/src", region)

        val lambdaTemplate = createTemplate()
        val lambdaStackName = defaultStackName(lambdaTemplate)
        println(toYaml(lambdaTemplate))

        createStack(lambdaTemplate, lambdaStackName, region, false) { credentialsProvider, outputs ->
            val client = AWSLambdaClientBuilder.defaultClient()
            val request = InvokeRequest()
            val returnMessage = "Hello World"
            request.functionName = functionName
            request.invocationType = InvocationType.RequestResponse.toString()
            request.payload = ByteBuffer.wrap("""{"message": "hello"}""".toByteArray())
            val result = client.invoke(request)
            val strResponse = String(result.payload.array())
            System.out.println(strResponse)
            Assert.assertTrue(strResponse.contains(returnMessage))
        }

    }

}