
package com.typedpath.awscloudformation.schema.sample
// created on Fri Jun 07 14:09:10 BST 2019 by fun jsonSchema2Kotlin(kotlin.String, kotlin.String, jdk.nashorn.api.scripting.ScriptObjectMirror, kotlin.collections.List<kotlin.collections.Map.Entry<kotlin.String, jdk.nashorn.api.scripting.ScriptObjectMirror>>, kotlin.Boolean): kotlin.Pair<kotlin.String, kotlin.String>
            
class AWS_IAM_Policy(
  val policyDocument: com.typedpath.awscloudformation.IamPolicy,
  val policyName: String): com.typedpath.awscloudformation.Resource ()  {
  override fun getResourceType() = "AWS::IAM::Policy"
  // Properties:
  var groups: List<String>? = null
  var roles: List<String>? = null
  var users: List<String>? = null



}
fun aWS_IAM_Policy(policyDocument: com.typedpath.awscloudformation.IamPolicy,policyName: String, init: AWS_IAM_Policy.()
    -> Unit
): AWS_IAM_Policy = AWS_IAM_Policy( policyDocument, policyName).apply(init)

