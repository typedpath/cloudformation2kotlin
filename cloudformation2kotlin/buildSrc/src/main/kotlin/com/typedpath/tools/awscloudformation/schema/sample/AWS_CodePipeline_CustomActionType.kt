
package com.typedpath.awscloudformation.schema.sample
// created on Sun Jun 09 18:43:13 BST 2019 by fun jsonSchema2Kotlin(kotlin.String, kotlin.String, jdk.nashorn.api.scripting.ScriptObjectMirror, kotlin.collections.List<kotlin.collections.Map.Entry<kotlin.String, jdk.nashorn.api.scripting.ScriptObjectMirror>>, kotlin.Boolean): kotlin.Pair<kotlin.String, kotlin.String>
            
class AWS_CodePipeline_CustomActionType(
  val category: String,
  val inputArtifactDetails: ArtifactDetails,
  val outputArtifactDetails: ArtifactDetails,
  val provider: String): com.typedpath.awscloudformation.Resource ()  {
  override fun getResourceType() = "AWS::CodePipeline::CustomActionType"
  // Properties:
  var configurationProperties: List<ConfigurationProperties>? = null
  var settings: Settings? = null
  var version: String? = null




class ArtifactDetails(
  val maximumCount: Int,
  val minimumCount: Int) {
   fun getResourceType() = "AWS::CodePipeline::CustomActionType.ArtifactDetails"
  // Properties:



}
fun artifactDetails(maximumCount: Int,minimumCount: Int, init: ArtifactDetails.()
    -> Unit
): ArtifactDetails = ArtifactDetails( maximumCount, minimumCount).apply(init)



class ConfigurationProperties(
  val key: Boolean,
  val name: String,
  val required: Boolean,
  val secret: Boolean) {
   fun getResourceType() = "AWS::CodePipeline::CustomActionType.ConfigurationProperties"
  // Properties:
  var description: String? = null
  var queryable: Boolean? = null
  var type: String? = null



}
fun configurationProperties(key: Boolean,name: String,required: Boolean,secret: Boolean, init: ConfigurationProperties.()
    -> Unit
): ConfigurationProperties = ConfigurationProperties( key, name, required, secret).apply(init)



class Settings() {
   fun getResourceType() = "AWS::CodePipeline::CustomActionType.Settings"
  // Properties:
  var entityUrlTemplate: String? = null
  var executionUrlTemplate: String? = null
  var revisionUrlTemplate: String? = null
  var thirdPartyConfigurationUrl: String? = null



}
fun settings( init: Settings.()
    -> Unit
): Settings = Settings().apply(init)


}
fun aWS_CodePipeline_CustomActionType(category: String,inputArtifactDetails: AWS_CodePipeline_CustomActionType.ArtifactDetails,outputArtifactDetails: AWS_CodePipeline_CustomActionType.ArtifactDetails,provider: String, init: AWS_CodePipeline_CustomActionType.()
    -> Unit
): AWS_CodePipeline_CustomActionType = AWS_CodePipeline_CustomActionType( category, inputArtifactDetails, outputArtifactDetails, provider).apply(init)

