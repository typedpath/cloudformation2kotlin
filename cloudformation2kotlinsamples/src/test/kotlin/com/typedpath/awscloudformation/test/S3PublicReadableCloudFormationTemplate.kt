package com.typedpath.awscloudformation.test

import com.typedpath.awscloudformation.CloudFormationTemplate
import com.typedpath.awscloudformation.IamPolicy
import com.typedpath.awscloudformation.schema.AWS_S3_Bucket
import com.typedpath.awscloudformation.schema.AWS_S3_BucketPolicy
import com.typedpath.awscloudformation.toYaml
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class S3PublicReadableCloudFormationTemplate(bucketName: String) : CloudFormationTemplate() {

  val s3Bucket = AWS_S3_Bucket().apply {
    //TODO inject enumeration
    accessControl = "PublicRead"
    websiteConfiguration = websiteConfiguration {
      indexDocument = "index.html"
      errorDocument = "error.html"
    }
    this.bucketName = bucketName//
  }

  val policyDocument = IamPolicy().apply {
    statement {
      effect = IamPolicy.EffectType.Allow
      principal = mapOf(
        Pair(IamPolicy.PrincipalType.AWS, listOf("*"))
      )
      action("s3:GetObject")
      resource +=join("", listOf("arn:aws:s3:::", ref(s3Bucket), "/*"))
    }
  }

  val s3BucketPolicy = AWS_S3_BucketPolicy(ref(s3Bucket), policyDocument)

}

fun main(args: Array<String>) {
  println( toYaml(S3PublicReadableCloudFormationTemplate(
          """testhost-${(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now()))}""")))
}