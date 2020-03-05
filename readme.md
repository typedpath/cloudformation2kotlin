# Cloudformation2Kotlin
This project offers a Kotlin DSL for AWS cloudformation. Kotlin brings the advantages of type safety, IDE autocomplete and the ability to embed code within templates.  This is a work in progress - please provide feedback!  

## Building
<pre>
cd cloudformation2kotlin
gradle publishToMavenLocal
</pre>
This will take the aws json schema definitions (copied from https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/cfn-resource-specification.html) convert them to kotlin and bundle them into a maven artifact along with some helper code. 
It also includes some classes create manually because I cant find a schema definition for them e.g. __IamPolicy__ and serverless resources such as __AWS::Serverless::Function__.  There are also some properties that are mapped to __Any?__ because I havent mapped the schema manually yet.  

## Using Code from Gradle
In build.gradle.kts
```gradle
repositories {
    mavenCentral()
}

dependencies {
    compile("com.typedpath:cloudformation2kotlin:1.0.0")
}

```

## Running tests
make sure the default aws user in ~/.aws has admin permissions!
<pre>cd cloudformationsamples
gradle clean build -x test
gradle build
</pre>
This will run the junit tests, creating cloud cloudformation stacks from (kotlin) templates and testing the resources in the created stacks. These are the tests (the xxTemplate.kt files are the source files, the xxxTemplate.yaml files are generated ): 


| Test | Template | Test Description |
| --- | --- | ----- |
| [S3Test.kt](cloudformation2kotlinsamples/tests/src/test/kotlin/com/typedpath/awscloudformation/test/s3/S3Test.kt) | [S3PublicReadableCloudFormationTemplate.kt](cloudformation2kotlinsamples/tests/src/test/kotlin/com/typedpath/awscloudformation/test/s3/S3PublicReadableCloudFormationTemplate.kt) [S3PublicReadableCloudFormationTemplate.yaml](docs/templates/S3PublicReadableCloudFormationTemplate.yaml) |write to s3 bucket in stack , read from s3 bucket |
| [LambdaTest.kt](cloudformation2kotlinsamples/tests/src/test/kotlin/com/typedpath/awscloudformation/test/lambda/LambdaTest.kt) | [LambdaCloudFormationTemplate.kt](cloudformation2kotlinsamples/tests/src/test/kotlin/com/typedpath/awscloudformation/test/lambda/LambdaCloudFormationTemplate.kt) [LambdaCloudFormationTemplate.yaml](docs/templates/LambdaCloudFormationTemplate.yaml) | call lambda in stack |
| [UnzipFunctionTest.kt](cloudformation2kotlinsamples/tests/src/test/kotlin/com/typedpath/awscloudformation/test/unziplambda/UnzipFunctionTest.kt) | [UnzipS3FunctionTemplate.kt](cloudformation2kotlinsamples/tests/src/test/kotlin/com/typedpath/awscloudformation/test/unziplambda/UnzipS3FunctionTemplate.kt) [UnzipS3FunctionTemplate.yaml](docs/templates/UnzipS3FunctionTemplate.yaml)| create a stack with an s3 bucket and an unzipping lambda function, upload a zip file and unzip it with the lambda function |
| [PipelineTest.kt](cloudformation2kotlinsamples/tests/src/test/kotlin/com/typedpath/awscloudformation/test/pipeline/PipelineTest.kt) | [PipelineCloudFormationTemplate.kt](cloudformation2kotlinsamples/tests/src/test/kotlin/com/typedpath/awscloudformation/test/pipeline/PipelineCloudFormationTemplate.kt) [PipelineCloudFormationTemplate.yaml](docs/templates/PipelineCloudFormationTemplate.yaml) | create stack with 4 stage pipeline + code repository, checkin code and test lambda created by pipeline |
| [LambdaServerlessTest.kt](cloudformation2kotlinsamples/tests/src/test/kotlin/com/typedpath/awscloudformation/test/serverless/LambdaServerlessTest.kt) | [LambdaServerlessTemplate.kt](cloudformation2kotlinsamples/tests/src/test/kotlin/com/typedpath/awscloudformation/test/serverless/LambdaServerlessTemplate.kt) [LambdaServerlessTemplate.yaml](docs/templates/LambdaServerlessTemplate.yaml)| create a SAM stack containing a lambda and call the lambda |
| [ServerlessBackendApiTest.kt](cloudformation2kotlinsamples/tests/src/test/kotlin/com/typedpath/awscloudformation/test/serverless/ServerlessBackendApiTest.kt) | [ServerlessBackendApiTemplate.kt](cloudformation2kotlinsamples/tests/src/test/kotlin/com/typedpath/awscloudformation/test/serverless/ServerlessBackendApiTemplate.kt) [ServerlessBackendApiTemplate.yaml](docs/templates/ServerlessBackendApiTemplate.yaml), [ServerlessBackendApiRefactoredTemplate.kt](cloudformation2kotlinsamples/tests/src/test/kotlin/com/typedpath/awscloudformation/test/serverless/ServerlessBackendApiRefactoredTemplate.kt) ( templates functionally equivalent)  | create a SAM stack implementing a REST api with API gateway, lambda functions and dynamo db.  Test with http put, get and d)lete calls   |
|[S3ObjectCreatedEventTest.kt](cloudformation2kotlinsamples/tests/src/test/kotlin/com/typedpath/awscloudformation/test/serverless/S3ObjectCreatedEventTest.kt)| [S3ObjectCreatedEventTemplate.kt](cloudformation2kotlinsamples/tests/src/test/kotlin/com/typedpath/awscloudformation/test/serverless/S3ObjectCreatedEventTemplate.kt) [S3ObjectCreatedEventTemplate.yaml](docs/templates/S3ObjectCreatedEventTemplate.yaml) | create a SAM stack containing an s3 bucket with lambda triggered by object create events. The lambda code is in a seperate kotlin project "lambdas/s3objectcreated" and adds a tag on creation. Write to the s3 bucket and check the tag has been created.  |
|[AuroraServerlessTest.kt](cloudformation2kotlinsamples/tests/src/test/kotlin/com/typedpath/awscloudformation/test/serverless/AuroraServerlessTest.kt)| [AuroraServerlessTemplate.kt](cloudformation2kotlinsamples/tests/src/test/kotlin/com/typedpath/awscloudformation/test/serverless/AuroraServerlessTemplate.kt)  [AuroraServerlessTemplate.yaml](docs/templates/AuroraServerlessTemplate.yaml) | create a stack containing an aurora serverless rds cluster. Using rds data client api create a database, table insert a row and read the row back  |
|[AuroraBackendApiTest.kt](cloudformation2kotlinsamples/tests/src/test/kotlin/com/typedpath/awscloudformation/test/serverless/typesafebackendapi/AuroraBackendApiTest.kt) |[AuroraBackendApiTemplate.kt](cloudformation2kotlinsamples/tests/src/test/kotlin/com/typedpath/awscloudformation/test/serverless/typesafebackendapi/AuroraBackendApiTemplate.kt) [AuroraBackendApiTemplate.yaml](docs/templates/AuroraBackendApiTemplate.yaml) | Create a stack containing an aurora serverless rds cluster fronted by a REST api providing a generic typesafe CRUD service over a relational database.  More details [here](cloudformation2kotlinsamples/tests/src/test/kotlin/com/typedpath/awscloudformation/test/serverless/typesafebackendapi/readme.md)   |


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