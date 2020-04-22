package com.typedpath.awscloudformation.test.serverless

import com.typedpath.iam2kotlin.IamPolicy
import com.typedpath.awscloudformation.LambdaRuntime
import com.typedpath.awscloudformation.schema.AWS_S3_Bucket
import com.typedpath.awscloudformation.serverlessschema.AWS_Serverless_Function
import com.typedpath.awscloudformation.serverlessschema.ServerlessCloudformationTemplate
import com.typedpath.iam2kotlin.resources.s3.S3Action

//based on https://github.com/awslabs/serverless-application-model/tree/master/examples/apps/s3-get-object
/**
adds a lambda trigger to an S3 bucket object create
 **/
class S3ObjectCreatedEventTemplate(codeUriIn: String, val bucketNamePrefix: String) : ServerlessCloudformationTemplate() {

    val bucket1 = AWS_S3_Bucket {
        bucketName = getBucketName()
    }

    /**
     * this is necessary because the SAM built in S3FullAccessPolicy doesnt allow tag access
     */
    val s3Policy = IamPolicy {
        statement {
            effect = IamPolicy.EffectType.Allow
            action(S3Action.PutObjectTagging)
            action(S3Action.GetObject)
            action(S3Action.GetObjectAcl)
            action(S3Action.GetObjectVersion)
            action(S3Action.PutObject)
            action(S3Action.PutObjectAcl)
            action(S3Action.DeleteObject)
            resource(S3Action.PutObjectTagging.byBucketnameKeyname(getBucketName(), "*"))
        }
        statement {
            effect = IamPolicy.EffectType.Allow
            action(S3Action.ListBucket)
            action(S3Action.GetBucketLocation)
            action(S3Action.GetLifecycleConfiguration)
            action(S3Action.PutLifecycleConfiguration)
            resource(S3Action.ListBucket.byBucketname(getBucketName()))
        }
    }

    val s3getobjectFunction = AWS_Serverless_Function("com.typedpath.serverless.Handler", LambdaRuntime.Java8.id) {
                description = "An Amazon S3 trigger that retrieves metadata for the object that has\n" +
                        "    been updated."
                codeUri = codeUriIn
                functionName = getFunctionName()
                memorySize = 1024
                timeout = 30
                //policy(AWS_Serverless_Function.S3FullAccessPolicy(getBucketName(), this@S3ObjectCreatedEventTemplate))
                policy(s3Policy)
                event("BucketEvent1", AWS_Serverless_Function.S3Event(bucket1, this@S3ObjectCreatedEventTemplate).apply {
                    event("s3:ObjectCreated:*")
                })
            }

    fun getBucketName() = "${bucketNamePrefix}-get-object".toLowerCase()
    fun getFunctionName() = "${getBucketName()}-function"

}