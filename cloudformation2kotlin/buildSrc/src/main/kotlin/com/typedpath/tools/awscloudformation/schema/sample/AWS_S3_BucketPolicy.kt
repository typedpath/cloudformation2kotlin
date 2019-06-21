
package com.typedpath.awscloudformation.schema.sample
// created on Fri Jun 07 14:09:10 BST 2019 by fun jsonSchema2Kotlin(kotlin.String, kotlin.String, jdk.nashorn.api.scripting.ScriptObjectMirror, kotlin.collections.List<kotlin.collections.Map.Entry<kotlin.String, jdk.nashorn.api.scripting.ScriptObjectMirror>>, kotlin.Boolean): kotlin.Pair<kotlin.String, kotlin.String>
            
class AWS_S3_BucketPolicy(
  val bucket: String,
  val policyDocument: com.typedpath.awscloudformation.IamPolicy): com.typedpath.awscloudformation.Resource ()  {
  override fun getResourceType() = "AWS::S3::BucketPolicy"
  // Properties:



}
fun aWS_S3_BucketPolicy(bucket: String,policyDocument: com.typedpath.awscloudformation.IamPolicy, init: AWS_S3_BucketPolicy.()
    -> Unit
): AWS_S3_BucketPolicy = AWS_S3_BucketPolicy( bucket, policyDocument).apply(init)

