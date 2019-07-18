package com.typedpath.awscloudformation.test

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.model.InvocationType
import com.amazonaws.services.lambda.model.InvokeRequest
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


class UnzipFunctionTest {

    val strDateTime = DateTimeFormatter.ofPattern("ddMMyyyy-HHmmss").format(LocalDateTime.now())

    private fun createZip(internalFilename: String, internalContent: String): ByteArray {
        val s = "hello world"
        val baos = ByteArrayOutputStream()
        try {
            ZipOutputStream(baos).use { zos ->
                val entry = ZipEntry(internalFilename)
                zos.putNextEntry(entry)
                zos.write(internalContent.toByteArray())
                zos.closeEntry()
            }
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }
        return baos.toByteArray()
    }

//testunzipbucket16072019-181502
    //testStack16072019-181502-lambdaFunction-O1G30JS7W6M8

    fun uploadUnzipDownload(credentialsProvider: AWSCredentialsProvider, region: Regions,
                                    testBucketName: String, functionName: String) {
        val s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(region)
                .build()
        val zipFileName = "ithink$strDateTime.zip"
        val destinationFolder = "ithink$strDateTime"
        val internalFilename = "ithink.txt"
        val textData = "i think therefore IBM"
        val metadata = ObjectMetadata()
        metadata.setContentType("application/blob")
        metadata.addUserMetadata("x-amz-meta-title", zipFileName)
        val zipBytes = createZip(internalFilename, textData)
        val inputStream = ByteArrayInputStream(zipBytes)
        //println("""putting text "$textData" to "$fileObjKeyName"  in bucket $bucketName """)
        s3Client.putObject(testBucketName, zipFileName, inputStream, metadata)

        val client = AWSLambdaClientBuilder.defaultClient()
        val request = InvokeRequest()
        request.functionName = functionName
        request.invocationType = InvocationType.RequestResponse.toString()
        request.payload = ByteBuffer.wrap("""{"bucketName": "$testBucketName", "key": "$zipFileName", "destinationFolder":"$destinationFolder"}""".toByteArray())
        val result = client.invoke(request)
        val strResponse = String(result.payload.array())
        System.out.println(strResponse)
        Assert.assertTrue(strResponse.contains("success"))

        val s3Object = s3Client.getObject(testBucketName, "$destinationFolder/$internalFilename")
        val data = s3Object.objectContent.readBytes()
        val strData = String(data)
        println("read $strData")
        Assert.assertEquals(textData, strData)

    }


    @Test
    fun lambda() {


        val functionName = """testunziplambda$strDateTime"""
        val testBucketName = """testunzipbucket$strDateTime"""

        val testTemplate = UnzipS3FunctionTemplate(functionName, testBucketName)
        val strStackName = """testStack$strDateTime"""

        val region = Regions.US_EAST_1

        test(testTemplate, strStackName, region, false) { credentialsProvider ->
            println("""*********testing testing credentials $credentialsProvider*************""")
            try {
                System.out.println("created stack - running unzip test!")
                uploadUnzipDownload(credentialsProvider, region, testBucketName, functionName)
            } catch (e: Exception) {
                e.printStackTrace()
                error("" + e.message)
                throw RuntimeException("failed unzip test", e)
            }
        }
    }
}


fun main(args: Array<String>) {
    val bucketName="testunzipbucket16072019-233350"
    val functionName ="testunziplambda16072019-233350"
    //UnzipFunctionTest().uploadUnzipDownload(ProfileCredentialsProvider(), Regions.US_EAST_1, bucketName, functionName )
    UnzipFunctionTest().lambda()

}