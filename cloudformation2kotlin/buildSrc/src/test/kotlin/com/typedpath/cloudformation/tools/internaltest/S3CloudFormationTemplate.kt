package com.typedpath.cloudformation.tools.internaltest

import com.typedpath.awscloudformation.CloudFormationTemplate
import com.typedpath.awscloudformation.IamPolicy
import com.typedpath.awscloudformation.schema.sample.AWS_S3_Bucket
import com.typedpath.awscloudformation.schema.sample.AWS_S3_BucketPolicy
import com.typedpath.awscloudformation.toYaml

class S3CloudFormationTemplate : CloudFormationTemplate() {

  val s3Bucket = AWS_S3_Bucket().apply {
    //TODO inject enumeration
    accessControl = "PublicRead"
    websiteConfiguration = websiteConfiguration {
      indexDocument = "index.html"
      errorDocument = "error.html"
    }
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
  println( toYaml(S3CloudFormationTemplate()))
}