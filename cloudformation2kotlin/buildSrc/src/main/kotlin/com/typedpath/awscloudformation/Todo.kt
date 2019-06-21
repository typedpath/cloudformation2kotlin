package com.typedpath.awscloudformation

fun main(args: Array<String>) {
  println(
    """
// TODO binary distribution
// https://tech.asimio.net/2018/06/27/Using-an-AWS-S3-Bucket-as-your-Maven-Repository.html
// https://docs.gradle.org/current/userguide/publishing_maven.html
//  (1) introduce maven install to gradle
//  (2) copy local directory structure to public s3
//  (3) try sample project
//  (4) look for source for spring
// TODO more generic pipeline
// TODO implement dependsOn
// TODO add pipeline validation rules
// TODO code this: https://docs.aws.amazon.com/codepipeline/latest/userguide/reference-pipeline-structure.html
// TODO autocopy tests
// TODO move copied code from package internal
// TODO - are these workable in AWS_S3_Bucket: Id="MyPolicy", Sid = "PublicReadForGetBucketObjects" ?
// TODO integration test
// TODO by extension out of order problem
// TODO strategy for pseudo parameters
// TODO implement Description
// DONE TODO implement Output
// DONE TODO implement this style S3CloudFormationTemplate : CloudFormationTemplate
// DONE review this if (parentType.endsWith("ActionDeclaration") && name.equals("configuration")) {
// DONE TODO pipeline by extension - add pipeline object
// DONE TODO pipeline by extension complete
// DONE TODO implement attributes
// DONE TODO lower case CloudFormationTemplate, Parameter and Resource properties

    """.trimIndent()


  )
}