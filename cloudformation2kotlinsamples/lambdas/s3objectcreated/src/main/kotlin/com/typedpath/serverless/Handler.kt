package com.typedpath.serverless

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.typedpath.serialization.TypedNode
import com.typedpath.serialization.getFromOriginSingleValue
import com.typedpath.serverless.S3Put.Items.S3 as S3
import com.amazonaws.services.s3.model.ObjectTagging
import com.amazonaws.services.s3.model.SetObjectTaggingRequest
import com.amazonaws.services.s3.model.*
import java.util.ArrayList
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.s3.AmazonS3Client


//import org.apache.logging.log4j.LogManager

class Handler : RequestHandler<Any, ApiGatewayResponse> {
    override fun handleRequest(input: Any, context: Context): ApiGatewayResponse {
        //LOG.info("received: " + input.keys.toString())
        try {
            println("receivedx a ${input::class} :: $input")
            val s3Node = TypedNode<S3Put>()(S3Put::records)[0](S3Put.Items::s3)
            val s3: Any? = getFromOriginSingleValue(s3Node, input)

            println("s3 ==  ${s3}")

            val bucketNameNode0 = TypedNode<S3>() po S3::bucket po S3.Bucket::name
            val bucketName: Any? = getFromOriginSingleValue(bucketNameNode0, s3) as String
            println("bucketName: ${bucketName}")

            // TODO swithc to this style
            //val bucketName2 = getFromOriginSingleValue<S3Put, String?>(inputData) {
            //    it(S3Put::records) [0] po S3Put.Items::s3 po S3Put.Items.S3::bucket po S3Put.Items.S3.Bucket::name
            //}

            val keyNameNode = TypedNode<S3>() po S3::object_ po S3.Object::key
            val keyName: Any? = getFromOriginSingleValue(keyNameNode, s3) as String
            println("keyName: ${keyName}")


            val newTags = ArrayList<Tag>()
            newTags.add(Tag("path", "$bucketName/$keyName"))
            println("creating s3 client")
            val s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                    .withRegion(Regions.US_EAST_1)
                    .build()
            println("setting a tag")
            val tagResult = s3Client.setObjectTagging(SetObjectTaggingRequest(bucketName.toString(), keyName.toString(), ObjectTagging(newTags)))
            println("set tag ${tagResult.toString()}" )

        } catch (tw: Throwable) {
            println("${tw} ${tw.message}")
        }


        return ApiGatewayResponse.build {
            statusCode = 200
            objectBody = HelloResponse("xxxGo Go Serverless v1.x! Your Kotlin function executed successfully!", input)
            headers = mapOf("X-Powered-By" to "AWS Lambda & serverless")
        }
    }

    companion object {
        //private val LOG = LogManager.getLogger(Handler::class.java)
    }
}
