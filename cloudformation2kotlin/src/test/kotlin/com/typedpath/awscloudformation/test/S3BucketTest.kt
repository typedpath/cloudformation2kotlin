package com.typedpath.awscloudformation.test

import com.typedpath.awscloudformation.cloudFormationTemplate
import com.typedpath.awscloudformation.schema.AWS_S3_Bucket
import com.typedpath.awscloudformation.schema.aWS_S3_Bucket
import com.typedpath.awscloudformation.toYaml
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

//TODO update this from internla test
class S3BucketTest {

  @Test
  fun s3Bucket() {

    val template =
      cloudFormationTemplate {
        resource("bucket1", aWS_S3_Bucket {
          bucketName = "testbucketxx" + SimpleDateFormat("yyMMddHHmmss").format(Date())
        })
      }
    println(toYaml(template))
  }

  /*fun s3HostingBucketPolicy(host: CloudFormationTemplate, s3Bucket: AWS_S3_Bucket) : AWS_S3_BucketPolicy {
    return AWS_S3_BucketPolicy(host.ref(s3Bucket))
  }*/

  fun s3HostingBucket() : AWS_S3_Bucket {
    return aWS_S3_Bucket {
      //TODO inject enumeration
      accessControl = "PublicRead"
      websiteConfiguration = websiteConfiguration {
        indexDocument = "index.html"
        errorDocument = "error.html"
      }
//  TODO thus property is not in the S3 spec json !?!?!?!          deletionPolicy = "Retain"

    }
  }

  @Test
  fun s3HostingBucketTest() {

    val template = cloudFormationTemplate {


    }

    println(toYaml(template))


  }


}