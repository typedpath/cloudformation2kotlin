package com.typedpath.awscloudformation.test.serverless

import com.typedpath.awscloudformation.IamPolicy
import com.typedpath.awscloudformation.LambdaRuntime
import com.typedpath.awscloudformation.schema.AWS_S3_Bucket
import com.typedpath.awscloudformation.serverlessschema.AWS_Serverless_Function
import com.typedpath.awscloudformation.serverlessschema.ServerlessCloudformationTemplate

//based on https://github.com/awslabs/serverless-application-model/tree/master/examples/apps/s3-get-object
/**
 adds a lambda trigger to an S3 bucket object create
 **/
class S3ObjectCreatedEventTemplate(codeUriIn: String, val bucketNamePrefix: String) : ServerlessCloudformationTemplate() {

    val bucket1 = AWS_S3_Bucket().apply {
        bucketName = getBucketName()
    }

    /**
     * this is necessary because the SAM built in S3FullAccessPolicy doesnt allow tag access
     */
    val s3Policy = IamPolicy() .apply {
        statement {
            effect = IamPolicy.EffectType.Allow
            action += "s3:PutObjectTagging"
            action += "s3:GetObject"
            action += "s3:GetObjectAcl"
            action += "s3:GetObjectVersion"
            action += "s3:PutObject"
            action += "s3:PutObjectAcl"
            action += "s3:DeleteObject"
            resource +="arn:aws:s3:::${getBucketName()}/*"
        }
        statement {
            effect = IamPolicy.EffectType.Allow
            action += "s3:ListBucket"
            action += "s3:GetBucketLocation"
            action += "s3:GetLifecycleConfiguration"
            action += "s3:PutLifecycleConfiguration"
            resource +="arn:aws:s3:::${getBucketName()}"
        }
    }

    val s3getobjectFunction = AWS_Serverless_Function("com.typedpath.serverless.Handler", LambdaRuntime.Java8.id)
        .apply {
            description = "An Amazon S3 trigger that retrieves metadata for the object that has\n" +
                    "    been updated."
            codeUri= codeUriIn
            functionName = getFunctionName()
            memorySize=1024
            timeout =30
            //policy(AWS_Serverless_Function.S3FullAccessPolicy(getBucketName(), this@S3ObjectCreatedEventTemplate))
            policy(s3Policy)
            event("BucketEvent1", AWS_Serverless_Function.S3Event(bucket1, this@S3ObjectCreatedEventTemplate).apply {
                event("s3:ObjectCreated:*")
            })
    }

    fun getBucketName() = "${bucketNamePrefix}-get-object".toLowerCase()
    fun getFunctionName() = "${getBucketName()}-function"

}