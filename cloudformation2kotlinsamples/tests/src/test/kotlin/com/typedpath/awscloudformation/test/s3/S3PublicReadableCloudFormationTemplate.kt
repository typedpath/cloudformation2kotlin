package com.typedpath.awscloudformation.test.s3

import com.typedpath.awscloudformation.CloudFormationTemplate
import com.typedpath.awscloudformation.schema.AWS_S3_Bucket
import com.typedpath.awscloudformation.schema.AWS_S3_BucketPolicy
import com.typedpath.awscloudformation.toYaml
import com.typedpath.iam2kotlin.IamPolicy
import com.typedpath.iam2kotlin.resources.s3.S3Action
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class S3PublicReadableCloudFormationTemplate(bucketName: String) : CloudFormationTemplate() {

  val s3Bucket = AWS_S3_Bucket {
    //TODO inject enumeration
    accessControl = "PublicRead"
    websiteConfiguration = AWS_S3_Bucket.WebsiteConfiguration {
      indexDocument = "index.html"
      errorDocument = "error.html"
    }
    this.bucketName = bucketName//
  }

  val policyDocument = IamPolicy {
    statement {
      effect = IamPolicy.EffectType.Allow
      principal = mutableMapOf(
        Pair(IamPolicy.PrincipalType.AWS, listOf("*"))
      )
      action(  S3Action.GetObject)
      resource ( S3Action.GetObject.byBucketnameKeyname(bucketName, "*"))
    }
  }

  val s3BucketPolicy = AWS_S3_BucketPolicy(ref(s3Bucket), policyDocument)

}

fun main(args: Array<String>) {
  println( toYaml(S3PublicReadableCloudFormationTemplate(
          """testhost-${(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now()))}""")))
}