
package com.typedpath.awscloudformation.schema.sample
// created on Fri Jun 07 14:09:10 BST 2019 by fun jsonSchema2Kotlin(kotlin.String, kotlin.String, jdk.nashorn.api.scripting.ScriptObjectMirror, kotlin.collections.List<kotlin.collections.Map.Entry<kotlin.String, jdk.nashorn.api.scripting.ScriptObjectMirror>>, kotlin.Boolean): kotlin.Pair<kotlin.String, kotlin.String>
            
class AWS_IAM_Role(
  val assumeRolePolicyDocument: com.typedpath.awscloudformation.IamPolicy): com.typedpath.awscloudformation.Resource ()  {
  override fun getResourceType() = "AWS::IAM::Role"
  // Properties:
  var managedPolicyArns: List<String>? = null
  var maxSessionDuration: Int? = null
  var path: String? = null
  var permissionsBoundary: String? = null
  var policies: List<Policy>? = null
  var roleName: String? = null
  fun arnAttribute() = Attribute(this, "Arn");
  fun roleIdAttribute() = Attribute(this, "RoleId");




class Policy(
  val policyDocument: com.typedpath.awscloudformation.IamPolicy,
  val policyName: String) {
   fun getResourceType() = "AWS::IAM::Role.Policy"
  // Properties:



}
fun policy(policyDocument: com.typedpath.awscloudformation.IamPolicy,policyName: String, init: Policy.()
    -> Unit
): Policy = Policy( policyDocument, policyName).apply(init)


}
fun aWS_IAM_Role(assumeRolePolicyDocument: com.typedpath.awscloudformation.IamPolicy, init: AWS_IAM_Role.()
    -> Unit
): AWS_IAM_Role = AWS_IAM_Role( assumeRolePolicyDocument).apply(init)

