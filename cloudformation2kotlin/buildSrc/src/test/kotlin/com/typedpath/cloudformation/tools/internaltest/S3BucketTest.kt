package com.typedpath.cloudformation.tools.internaltest

import com.typedpath.awscloudformation.*
import com.typedpath.awscloudformation.schema.sample.AWS_S3_Bucket
import com.typedpath.awscloudformation.schema.sample.AWS_S3_BucketPolicy
import com.typedpath.awscloudformation.schema.sample.aWS_S3_Bucket
import com.typedpath.awscloudformation.schema.sample.aWS_S3_BucketPolicy
import java.util.*

class S3BucketTest {

  fun s3Bucket() {

    val bucket = s3HostingBucket()

    val template =
      cloudFormationTemplate {
        resource("bucket1", bucket)
        resource("bucketPolicy", s3HostingBucketPolicy(this, bucket))
      }
    println(toYaml(template))
  }

  fun s3HostingBucketPolicy(host: CloudFormationTemplate, s3Bucket: AWS_S3_Bucket) : AWS_S3_BucketPolicy {
    val policyDocument = iamPolicy {
//TODO - are these workable
//        Id="MyPolicy"
//        Sid = "PublicReadForGetBucketObjects"
      statement {
        effect = IamPolicy.EffectType.Allow
        principal = mapOf(
          Pair(IamPolicy.PrincipalType.AWS, listOf("*"))
        )
        action("s3:GetObject")
        resource += host.join ( "", listOf("arn:aws:s3:::", host.ref(s3Bucket), "/*"))
      }
    }
    return aWS_S3_BucketPolicy(host.ref(s3Bucket), policyDocument) {
    }
  }
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

  fun s3HostingBucketTest() {
    val s3Bucket = s3HostingBucket()
    val template = cloudFormationTemplate {
      resource("s3HostingBucket", s3Bucket)
      resource ("s3BucketPolicy", s3HostingBucketPolicy(this, s3Bucket))
    }
    //println(template.asYaml())
    println(toYaml(template))
  }
}

fun main (args: Array<String>) {
  S3BucketTest().s3HostingBucketTest()
  //var jsObject =
  //  mapOf(Pair("!Join", listOf("''", listOf("arn:aws:s3:::", "!Ref s3HostingBucket", "/*"))))
  //println(toYamlString(jsObject))
}

/*


  Properties:
      PolicyDocument:
        Id: MyPolicy
        Version: 2012-10-17
        Statement:
        - Sid: PublicReadForGetBucketObjects
          effect: Allow
          principal: '*'
          action: 's3:GetObject'
          resource: !Join
          - ''
          - - 'arn:aws:s3:::'
            - !Ref S3Bucket
            - / *
      Bucket: !Ref S3Bucket
 */
