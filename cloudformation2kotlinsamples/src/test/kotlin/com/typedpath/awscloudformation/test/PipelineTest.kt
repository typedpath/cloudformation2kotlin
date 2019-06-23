package com.typedpath.awscloudformation.test

import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
import org.junit.Assert
import org.junit.Test
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class PipelineTest {

    private fun readLine(url: URL): String = (BufferedReader(InputStreamReader(url.openStream()))).readLine()

    @Test
    fun pipleline() {

        val strDateTime = (DateTimeFormatter.ofPattern("ddMMMyy-HHMMss")).format(LocalDateTime.now())

        val defaultReponame = "testrepo$strDateTime"

        val testTemplate = PipelineCloudFormationTemplate(defaultReponame)
        val strStackName = """testStack$strDateTime"""

        val region = Regions.US_EAST_1

        test(testTemplate, strStackName, region, true) { credentialsProvider ->
            println("""*********testing testing credentials $credentialsProvider*************""")
            try {
                // add files to the code source
                // wait for build artifict
                // wait for deployment
                // test deployment
                //

            } catch (e: Exception) {
                error(""+e.message)
                throw RuntimeException("failed s3 test", e)
            }
        }
    }
}

fun main(args: Array<String>) {
    PipelineTest().pipleline()
}