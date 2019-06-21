
package com.typedpath.awscloudformation.schema.sample
// created on Sun Jun 09 18:43:13 BST 2019 by fun jsonSchema2Kotlin(kotlin.String, kotlin.String, jdk.nashorn.api.scripting.ScriptObjectMirror, kotlin.collections.List<kotlin.collections.Map.Entry<kotlin.String, jdk.nashorn.api.scripting.ScriptObjectMirror>>, kotlin.Boolean): kotlin.Pair<kotlin.String, kotlin.String>
            
class AWS_CodePipeline_Webhook(
  val authenticationConfiguration: WebhookAuthConfiguration,
  val filters: List<WebhookFilterRule>,
  val authentication: String,
  val targetPipeline: String,
  val targetAction: String,
  val targetPipelineVersion: Int): com.typedpath.awscloudformation.Resource ()  {
  override fun getResourceType() = "AWS::CodePipeline::Webhook"
  // Properties:
  var name: String? = null
  var registerWithThirdParty: Boolean? = null
  fun urlAttribute() = Attribute(this, "Url");




class WebhookAuthConfiguration() {
   fun getResourceType() = "AWS::CodePipeline::Webhook.WebhookAuthConfiguration"
  // Properties:
  var allowedIPRange: String? = null
  var secretToken: String? = null



}
fun webhookAuthConfiguration( init: WebhookAuthConfiguration.()
    -> Unit
): WebhookAuthConfiguration = WebhookAuthConfiguration().apply(init)



class WebhookFilterRule(
  val jsonPath: String) {
   fun getResourceType() = "AWS::CodePipeline::Webhook.WebhookFilterRule"
  // Properties:
  var matchEquals: String? = null



}
fun webhookFilterRule(jsonPath: String, init: WebhookFilterRule.()
    -> Unit
): WebhookFilterRule = WebhookFilterRule( jsonPath).apply(init)


}
fun aWS_CodePipeline_Webhook(authenticationConfiguration: AWS_CodePipeline_Webhook.WebhookAuthConfiguration,filters: List<AWS_CodePipeline_Webhook.WebhookFilterRule>,authentication: String,targetPipeline: String,targetAction: String,targetPipelineVersion: Int, init: AWS_CodePipeline_Webhook.()
    -> Unit
): AWS_CodePipeline_Webhook = AWS_CodePipeline_Webhook( authenticationConfiguration, filters, authentication, targetPipeline, targetAction, targetPipelineVersion).apply(init)

