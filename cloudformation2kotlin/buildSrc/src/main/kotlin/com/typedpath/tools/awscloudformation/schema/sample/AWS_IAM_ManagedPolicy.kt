
package com.typedpath.awscloudformation.schema.sample
// created on Fri Jun 07 14:09:10 BST 2019 by fun jsonSchema2Kotlin(kotlin.String, kotlin.String, jdk.nashorn.api.scripting.ScriptObjectMirror, kotlin.collections.List<kotlin.collections.Map.Entry<kotlin.String, jdk.nashorn.api.scripting.ScriptObjectMirror>>, kotlin.Boolean): kotlin.Pair<kotlin.String, kotlin.String>
            
class AWS_IAM_ManagedPolicy(
  val policyDocument: com.typedpath.awscloudformation.IamPolicy): com.typedpath.awscloudformation.Resource ()  {
  override fun getResourceType() = "AWS::IAM::ManagedPolicy"
  // Properties:
  var description: String? = null
  var groups: List<String>? = null
  var managedPolicyName: String? = null
  var path: String? = null
  var roles: List<String>? = null
  var users: List<String>? = null



}
fun aWS_IAM_ManagedPolicy(policyDocument: com.typedpath.awscloudformation.IamPolicy, init: AWS_IAM_ManagedPolicy.()
    -> Unit
): AWS_IAM_ManagedPolicy = AWS_IAM_ManagedPolicy( policyDocument).apply(init)

