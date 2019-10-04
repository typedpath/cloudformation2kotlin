package com.typedpath.awscloudformation.test.lambda

import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.model.InvocationType
import com.amazonaws.services.lambda.model.InvokeRequest
import com.typedpath.awscloudformation.CloudFormationTemplate
import com.typedpath.awscloudformation.test.TemplateFactory
import com.typedpath.awscloudformation.test.util.createStack
import org.junit.Assert
import org.junit.Test
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class LambdaTest  : TemplateFactory {

    val strDateTime = DateTimeFormatter.ofPattern("ddMMyyyy-HHmmss").format(LocalDateTime.now())
    val functionName = """testlambda$strDateTime"""
    val returnMessage = """hello from $functionName"""

    override fun createTemplate() : CloudFormationTemplate {

        val jsCode = """exports.handler = function(event, context) {
var responseData = {Message: 'Hello'};
console.log(responseData);
context.succeed('$returnMessage')
};"""
        return LambdaCloudFormationTemplate(functionName, jsCode)
    }

    @Test
    fun lambda() {

        val strStackName = """lambdaTestStack$strDateTime"""

        val region = Regions.US_EAST_1

        createStack(createTemplate(), strStackName, region, false) { credentialsProvider, outputs ->
            println("""*********testing testing credentials $credentialsProvider*************""")
            try {
                val client = AWSLambdaClientBuilder.defaultClient()
                val request = InvokeRequest()
                request.functionName = functionName
                request.invocationType = InvocationType.RequestResponse.toString()
                request.payload = ByteBuffer.wrap("""{"message": "hello"}""".toByteArray())
                val result = client.invoke(request)
                val strResponse = String(result.payload.array())
                System.out.println(strResponse)
                Assert.assertTrue(strResponse.contains(returnMessage))
                //System.out.println("hello")
            } catch (e: Exception) {
                e.printStackTrace()
                error("" + e.message)
                throw RuntimeException("failed s3 createStack", e)
            }
        }
    }
}

fun main(args: Array<String>) {
    LambdaTest().lambda()
}