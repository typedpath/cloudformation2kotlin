<h1>Cloudformation2Kotlin</h1>
This project offers a Kotlin DSL for AWS cloudformation. Kotlin brings the advantages of type safety, IDE autocomplete and the ability to embed code within templates.  This is a work in progress - please provide feedback!  

<h2>Building</h2>
<pre>
cd cloudformation2kotlin
gradle publishToMavenLocal
</pre>
This will take the aws json schema definitions (copied from https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/cfn-resource-specification.html) convert them to kotlin and bundle them into a maven artifact along with some helper code. Currently not all the schemas are converted - only a subset in directory cloudformation2kotlin/**/us-east1_active/

<h2>Running tests</h2>
make sure the default aws user in ~/.aws has admin permissions 
<pre>cd cloudformationsamples
gradle build
</pre>
This will run the junit tests, creating cloud cloudformation stacks from (kotlin) templates and testing the resources in the created stacks 


| Test | Template | Test Description |
| --- | --- | ----- |
| s3Test.kt | S3PublicReadableCloudFormationTemplate.kt |write to s3 bucket in stack , read from s3 bucket |
| LambdaTest.kt | LambdaCloudFormationTemplate.kt | call lambda in stack |
| UnzipFunctionTest.kt | UnzipS3FunctionTemplate.kt | create a stack with an s3 bucket and an unzipping lambda function, upload a zip file and unzip it with the lambda function |
| PipelineTest.kt | PipelineCloudFormationTemplate.kt | create stack with 4 stage pipeline + code repository, checkin code and test lambda created by pipeline |

<h2>Templates</h2>
The examples listed above create templates by extension e.g.:
<pre>class S3PublicReadableCloudFormationTemplate(bucketName: String) : CloudFormationTemplate() {...</pre>
templates declared in this way will have a cloudformation resource for every property that extends com.typedpath.awscloudformation.Resource. Properties in superclasses will be included. 
In the case of S3PublicReadableCloudFormationTemplate.kt these 2 property declarations map to an s3 bucket and a s3 bucket policy in the stack:
<pre>
   val s3Bucket = AWS_S3_Bucket() . . . . 
   val s3BucketPolicy = AWS_S3_BucketPolicy(ref(s3Bucket), policyDocument) . . . .
</pre>
Templates are mapped to yaml with the <i>toYaml</i> function e.g.:
<pre>val strTemplate = toYaml(S3PublicReadableCloudFormationTemplate(bucketName))</pre>
In this example this will give:
<pre>
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
. . . . 

</pre>


