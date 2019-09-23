# Cloudformation2Kotlin
This project offers a Kotlin DSL for AWS cloudformation. Kotlin brings the advantages of type safety, IDE autocomplete and the ability to embed code within templates.  This is a work in progress - please provide feedback!  

## Building
<pre>
cd cloudformation2kotlin
gradle publishToMavenLocal
</pre>
This will take the aws json schema definitions (copied from https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/cfn-resource-specification.html) convert them to kotlin and bundle them into a maven artifact along with some helper code. Currently not all the schemas are converted - only a subset in directory cloudformation2kotlin/**/us-east1_active/
It also includes some classes create manually because I can find a schema definition for them e.g. IamPolicy and serverless resources such as __AWS::Serverless::Function__  

## Running tests
make sure the default aws user in ~/.aws has admin permissions!
<pre>cd cloudformationsamples
gradle clean build -x test
gradle build
</pre>
This will run the junit tests, creating cloud cloudformation stacks from (kotlin) templates and testing the resources in the created stacks. These are the tests: 


| Test | Template | Test Description |
| --- | --- | ----- |
| s3Test.kt | S3PublicReadableCloudFormationTemplate.kt |write to s3 bucket in stack , read from s3 bucket |
| LambdaTest.kt | LambdaCloudFormationTemplate.kt | call lambda in stack |
| UnzipFunctionTest.kt | UnzipS3FunctionTemplate.kt | create a stack with an s3 bucket and an unzipping lambda function, upload a zip file and unzip it with the lambda function |
| PipelineTest.kt | PipelineCloudFormationTemplate.kt | create stack with 4 stage pipeline + code repository, checkin code and test lambda created by pipeline |
| LambdaServerlessTest.kt | LambdaServerlessTemplate.kt | create a SAM stack containing a lambda and call the lambda |
| ServerlessBackendApiTest.kt | ServerlessBackendApiTemplate.kt, ServerlessBackendApiRefactoredTemplate.kt ( templates functionally equivalent)  | create a SAM stack implementing a REST api with API gateway, lambda functions and dynamo db.  Test with http put, get and delete calls   |
|S3ObjectCreatedEventTest.kt| S3ObjectCreatedEventTemplate | create a SAM stack containing an s3 bucket with lambda triggered by object create events. The lambda code is in a seperate kotlin project "lambdas/s3objectcreated" and adds a tag on creation. Write to the s3 bucket and check the tag has been created.  |

## Templates
The examples listed above create templates by extension e.g.:
```kotlin
class S3PublicReadableCloudFormationTemplate(bucketName: String) : CloudFormationTemplate() {...
```
templates declared in this way will have a cloudformation resource for every property that extends __com.typedpath.awscloudformation.Resource__. Properties in superclasses will be included. 
In the case of __S3PublicReadableCloudFormationTemplate.kt__ these 2 property declarations map to an s3 bucket and a s3 bucket policy in the stack:
```kotlin
   val s3Bucket = AWS_S3_Bucket() . . . . 
   val s3BucketPolicy = AWS_S3_BucketPolicy(ref(s3Bucket), policyDocument) . . . .
```
Templates are mapped to yaml with the __toYaml__ function e.g.:
```kotlin
val strTemplate = toYaml(S3PublicReadableCloudFormationTemplate(bucketName))
```
In this example this will give:
```yaml
AWSTemplateFormatVersion: '2010-09-09'
Resources:
  s3Bucket:
    Type: 'AWS::S3::Bucket'
    Properties:
. . . .
s3BucketPolicy:
    Type: 'AWS::S3::BucketPolicy'
    Properties:
      Bucket: !Ref s3Bucket
      PolicyDocument:
        Statement:
          - Action:
              - 's3:GetObject'
            Effect: Allow
. . . .
```
Note the use of helper function <i>ref(s3Bucket)</i>) - this makes it difficult to create an invalid cloudformation reference.

The property definition for policyDocument is:
```kotlin
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
```
The constructor for __IamPolicy__ has no arguments because there are no mandatory properties.  Non mandatory properties are specified in the __apply__ block. Note also that __policyDocument__ does not appear in the top level of the yaml template, it is inlined where it is referenced.  This is because it is not a cloud formation resource i.e. not an instance of  __com.typedpath.awscloudformation.Resource__.  Class __IamPolicy__ was created manually (unlike Kotlin resource definiton classes) because I couldnt find an amazon supplied schema (json or otherwise) for it.  