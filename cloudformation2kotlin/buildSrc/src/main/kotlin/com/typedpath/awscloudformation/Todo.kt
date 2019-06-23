package com.typedpath.awscloudformation

fun main(args: Array<String>) {
  println(
    """
// TODO integration test - in cloudformationsample project
// TODO move copied code from package internal
// TODO tidy s3maven deploy - implement non snapshot rule / add s3 bucket bootstrap
// TODO binary distribution - jfrog ?
// TODO more generic pipeline
// TODO implement dependsOn
// TODO add pipeline validation rules
// TODO code this: https://docs.aws.amazon.com/codepipeline/latest/userguide/reference-pipeline-structure.html
// TODO - are these workable in AWS_S3_Bucket: Id="MyPolicy", Sid = "PublicReadForGetBucketObjects" ?
// TODO by extension out of order problem
// TODO strategy for pseudo parameters
// TODO implement Description
// TODO complete all json files
// TODO differentiate between regions
// TODO eliminate file copies in build
// TODO add json files to source sets so kotlin main src doesnt need any files
// DONE implement s3 maven deploy
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