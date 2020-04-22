package com.typedpath.awscloudformation.test

import com.typedpath.awscloudformation.CloudFormationTemplate
import com.typedpath.awscloudformation.schema.AWS_S3_Bucket
import com.typedpath.awscloudformation.toYaml
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class S3BucketTestSample {

  @Test
  fun s3Bucket() {

    val template =
      CloudFormationTemplate {
        resource("bucket1", AWS_S3_Bucket {
          bucketName = "testbucketxx" + SimpleDateFormat("yyMMddHHmmss").format(Date())
        })
      }
    println(toYaml(template))
  }

  /*fun s3HostingBucketPolicy(host: CloudFormationTemplate, s3Bucket: AWS_S3_Bucket) : AWS_S3_BucketPolicy {
    return AWS_S3_BucketPolicy(host.ref(s3Bucket))
  }*/

  fun s3HostingBucket() : AWS_S3_Bucket {
    return AWS_S3_Bucket {
      //TODO inject enumeration
      accessControl = "PublicRead"
      websiteConfiguration = AWS_S3_Bucket.WebsiteConfiguration {
        indexDocument = "index.html"
        errorDocument = "error.html"
      }
//  TODO this property is not in the S3 spec json !?!?!?!          deletionPolicy = "Retain"

    }
  }


}