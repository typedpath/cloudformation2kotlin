package com.typedpath.awscloudformation.test.serverless

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectTaggingRequest
import com.amazonaws.services.s3.model.GetObjectTaggingResult
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.Tag
import com.typedpath.awscloudformation.test.TemplateFactory
import com.typedpath.awscloudformation.test.uploadBlobToS3
import com.typedpath.awscloudformation.test.util.createStack
import com.typedpath.awscloudformation.test.util.defaultCredentialsProvider
import com.typedpath.awscloudformation.test.util.defaultCurrentDateTimePattern
import com.typedpath.awscloudformation.test.util.defaultStackName
import com.typedpath.awscloudformation.test.withoutextension.getOrCreateTestArtifactS3BucketName
import com.typedpath.awscloudformation.toYaml
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.net.URL
import java.nio.charset.Charset
import java.nio.file.Paths

fun uploadFileCheckTag(bucketName: String, credentialsProvider: AWSCredentialsProvider, region: Regions) {
    try {
        val s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(region)
                .build()
        val fileObjKeyName = "ithink${java.lang.System.currentTimeMillis()}.txt"
        val textData = "i think therefore IBM"
        val metadata = ObjectMetadata()
        metadata.setContentType("application/blob")
        metadata.addUserMetadata("x-amz-meta-title", fileObjKeyName)
        val inputStream = ByteArrayInputStream(textData.toByteArray(Charset.defaultCharset()))
        println("""putting text "$textData" to "$fileObjKeyName"  in bucket $bucketName """)
        s3Client.putObject(bucketName, fileObjKeyName, inputStream, metadata)

        val timeout: Long = 10000
        var tag: Tag? = null
        val startTime = System.currentTimeMillis()
        while ((System.currentTimeMillis() - startTime) < timeout && tag == null) {
            // Retrieve the object's tags.
            println("retrieving tags")
            val getTaggingRequest: GetObjectTaggingRequest = GetObjectTaggingRequest(bucketName, fileObjKeyName);
            val getTagsResult: GetObjectTaggingResult = s3Client.getObjectTagging(getTaggingRequest);
            if (getTagsResult.tagSet.size > 0) {
                tag = getTagsResult.tagSet[0]
                break
            }
            try {
                Thread.sleep(200)
            } catch (ex: Exception) {
            }
        }

        println("tag0={tag0} ${tag!!.key} ${tag.value}")
        Assert.assertEquals(tag!!.key, "path")
        Assert.assertEquals(tag!!.value, "$bucketName/$fileObjKeyName")

//            s3Client.deleteObject(bucketName, fileObjKeyName)

    } catch (e: Exception) {
        error("" + e.message)
        throw RuntimeException("failed s3 createStack", e)
    }
}

class S3ObjectCreatedEventTest : TemplateFactory {
    val region = Regions.US_EAST_1

    val credentialsProvider = defaultCredentialsProvider()
    val codeBucketName = getOrCreateTestArtifactS3BucketName(region, credentialsProvider)
    //val codePackageName = "codepackage_api_backend.zip"
    val codeRelativeLocation = "lambdas/s3objectcreated/build/libs/s3objectcreated-fat-testonly.jar"

    //this value is for auto documentation only
    var codeSourceUri = "s3://serverless-testutils-artifact-bucket-us-east-1sample/lambdas/example/example.jar"

    val bucketNamePrefix = defaultCurrentDateTimePattern() + "s3createEventTest"

    override fun createTemplate(): S3ObjectCreatedEventTemplate {
        return  S3ObjectCreatedEventTemplate(codeSourceUri, "test" + bucketNamePrefix)
    }


    fun uploadCodeToS3(strFile: String, region: Regions, bucketName: String, bucketKey: String, credentialsProvider: AWSCredentialsProvider): String {
        val path = Paths.get(strFile)
        val theFile = path.toFile()
        println("loading code from ${theFile.absolutePath}")
        val inputStream = FileInputStream(path.toFile())
        println("found ${inputStream.available()} bytes in ${path.toAbsolutePath()}")
        return uploadBlobToS3(region, bucketName, bucketKey, inputStream, credentialsProvider)
    }


    @Test
    fun objectCreated() {

        codeSourceUri = uploadCodeToS3("../$codeRelativeLocation"
                , region, codeBucketName, codeRelativeLocation, credentialsProvider)

        val template = createTemplate()
        val lambdaStackName = defaultStackName(template)
        println(toYaml(template))

        createStack(template, lambdaStackName, region, false) { credentialsProvider, outputs ->
            println("we made it !!")
            val bucketName = template.getBucketName()
            uploadFileCheckTag(bucketName, credentialsProvider, region)
        }
    }
}


fun mainyyy(args: Array<String>): Unit {
    val bucketName = "test22sep19-141950s3createeventtest-get-object"
//    println(toYaml(S3ObjectCreatedEventTemplate("mycodeUri", "mytestbuckket")))
    println(uploadFileCheckTag(bucketName, defaultCredentialsProvider(), Regions.US_EAST_1))
}
