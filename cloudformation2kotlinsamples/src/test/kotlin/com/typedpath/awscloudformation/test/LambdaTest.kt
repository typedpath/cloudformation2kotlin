package com.typedpath.awscloudformation.test

import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.model.InvocationType
import com.amazonaws.services.lambda.model.InvokeRequest
import org.junit.Assert
import org.junit.Test
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class LambdaTest {

    @Test
    fun lambda() {

        val strDateTime = DateTimeFormatter.ofPattern("ddMMyyyy-HHmmss").format(LocalDateTime.now())

        val functionName = """testlambda$strDateTime"""

        val returnMessage = """hello from $functionName"""

        val jsCode = """exports.handler = function(event, context) {
var responseData = {Message: 'Hello'};
console.log(responseData);
context.succeed('$returnMessage')
};"""

        val testTemplate = LambdaCloudFormationTemplate(functionName, jsCode)
        val strStackName = """testStack$strDateTime"""

        val region = Regions.US_EAST_1

        test(testTemplate, strStackName, region, false) { credentialsProvider ->
            println("""*********testing testing credentials $credentialsProvider*************""")
            try {
                val client = AWSLambdaClientBuilder.defaultClient()
                val request = InvokeRequest()
                request.functionName = functionName
                request.invocationType = InvocationType.RequestResponse.toString()
                request.payload = ByteBuffer.wrap("""{"message": "hello"}""".toByteArray())
                val result  = client.invoke(request)
                val strResponse = String(result.payload.array())
                System.out.println(strResponse)
                Assert.assertTrue(strResponse.contains(returnMessage))
                //System.out.println("hello")
            } catch (e: Exception) {
                e.printStackTrace()
                error(""+e.message)
                throw RuntimeException("failed s3 test", e)
            }
        }
    }
}

fun main(args: Array<String>) {
    LambdaTest().lambda()
}