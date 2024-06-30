package com.typedpath.awscloudformation.test.appsync

import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.model.InvocationType
import com.amazonaws.services.lambda.model.InvokeRequest
import com.typedpath.awscloudformation.CloudFormationTemplate
import com.typedpath.awscloudformation.test.TemplateFactory
import com.typedpath.awscloudformation.test.lambda.LambdaCloudFormationTemplate
import com.typedpath.awscloudformation.test.lambda.LambdaTest
import com.typedpath.awscloudformation.test.serverless.AuroraServerlessTemplate
import com.typedpath.awscloudformation.test.util.createStack
import org.junit.Assert
import org.junit.Test
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AppSyncTest : TemplateFactory {

val strDateTime = DateTimeFormatter.ofPattern("ddMMyyyy-HHmmss").format(LocalDateTime.now())
val functionName = """testAppSync$strDateTime"""
val returnMessage = """hello from $functionName"""

override fun createTemplate() : CloudFormationTemplate {
    return AppsyncCloudFormationTemplate()
}

@Test
fun appsync() {

    val strStackName = """appsync$strDateTime"""

    val region = Regions.US_EAST_1

    createStack(createTemplate(), strStackName, region, false) { credentialsProvider, outputs ->
        println("""*********testing testing credentials $credentialsProvider*************""")
        try {
            val arn = outputs.filter { it.outputKey.equals(AppsyncCloudFormationTemplate::arn.name) }.firstOrNull()
            if (arn == null) {
                throw java.lang.RuntimeException("cant find secretArn in ${outputs}")
            }
            System.out.println("hello appsync api arn=${arn.outputValue}")
        } catch (e: Exception) {
            e.printStackTrace()
            error("" + e.message)
            throw RuntimeException("failed s3 createStack", e)
        }
    }
}
}

fun main(args: Array<String>) {
}