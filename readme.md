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
| LambdaTest.kt | LambdaCloudFormationTemplate | call lambda in stack |
| PipelineTest | PipelineCloudFormationTemplate | create stack with 4 stage pipeline + code repository, checkin code and test lambda created by pipeline |