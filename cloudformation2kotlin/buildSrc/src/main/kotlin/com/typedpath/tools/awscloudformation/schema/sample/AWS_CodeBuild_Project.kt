
package com.typedpath.awscloudformation.schema.sample
// created on Fri Jun 07 14:09:10 BST 2019 by fun jsonSchema2Kotlin(kotlin.String, kotlin.String, jdk.nashorn.api.scripting.ScriptObjectMirror, kotlin.collections.List<kotlin.collections.Map.Entry<kotlin.String, jdk.nashorn.api.scripting.ScriptObjectMirror>>, kotlin.Boolean): kotlin.Pair<kotlin.String, kotlin.String>
            
class AWS_CodeBuild_Project(
  val source: Source,
  val artifacts: Artifacts,
  val serviceRole: String,
  val environment: Environment): com.typedpath.awscloudformation.Resource ()  {
  override fun getResourceType() = "AWS::CodeBuild::Project"
  // Properties:
  var description: String? = null
  var vpcConfig: VpcConfig? = null
  var secondarySources: List<Source>? = null
  var encryptionKey: String? = null
  var triggers: ProjectTriggers? = null
  var secondaryArtifacts: List<Artifacts>? = null
  var name: String? = null
  var badgeEnabled: Boolean? = null
  var logsConfig: LogsConfig? = null
  var queuedTimeoutInMinutes: Int? = null
  var tags: List<Tag>? = null
  var timeoutInMinutes: Int? = null
  var cache: ProjectCache? = null
  fun arnAttribute() = Attribute(this, "Arn");




class ProjectCache(
  val type: String) {
   fun getResourceType() = "AWS::CodeBuild::Project.ProjectCache"
  // Properties:
  var modes: List<String>? = null
  var location: String? = null



}
fun projectCache(type: String, init: ProjectCache.()
    -> Unit
): ProjectCache = ProjectCache( type).apply(init)



class Artifacts(
  val type: String) {
   fun getResourceType() = "AWS::CodeBuild::Project.Artifacts"
  // Properties:
  var path: String? = null
  var artifactIdentifier: String? = null
  var overrideArtifactName: Boolean? = null
  var packaging: String? = null
  var encryptionDisabled: Boolean? = null
  var location: String? = null
  var name: String? = null
  var namespaceType: String? = null



}
fun artifacts(type: String, init: Artifacts.()
    -> Unit
): Artifacts = Artifacts( type).apply(init)



class GitSubmodulesConfig(
  val fetchSubmodules: Boolean) {
   fun getResourceType() = "AWS::CodeBuild::Project.GitSubmodulesConfig"
  // Properties:



}
fun gitSubmodulesConfig(fetchSubmodules: Boolean, init: GitSubmodulesConfig.()
    -> Unit
): GitSubmodulesConfig = GitSubmodulesConfig( fetchSubmodules).apply(init)



class Source(
  val type: String) {
   fun getResourceType() = "AWS::CodeBuild::Project.Source"
  // Properties:
  var reportBuildStatus: Boolean? = null
  var auth: SourceAuth? = null
  var sourceIdentifier: String? = null
  var buildSpec: String? = null
  var gitCloneDepth: Int? = null
  var gitSubmodulesConfig: GitSubmodulesConfig? = null
  var insecureSsl: Boolean? = null
  var location: String? = null



}
fun source(type: String, init: Source.()
    -> Unit
): Source = Source( type).apply(init)



class EnvironmentVariable(
  val value: String,
  val name: String) {
   fun getResourceType() = "AWS::CodeBuild::Project.EnvironmentVariable"
  // Properties:
  var type: String? = null



}
fun environmentVariable(value: String,name: String, init: EnvironmentVariable.()
    -> Unit
): EnvironmentVariable = EnvironmentVariable( value, name).apply(init)



class LogsConfig() {
   fun getResourceType() = "AWS::CodeBuild::Project.LogsConfig"
  // Properties:
  var cloudWatchLogs: CloudWatchLogsConfig? = null
  var s3Logs: S3LogsConfig? = null



}
fun logsConfig( init: LogsConfig.()
    -> Unit
): LogsConfig = LogsConfig().apply(init)



class Environment(
  val type: String,
  val image: String,
  val computeType: String) {
   fun getResourceType() = "AWS::CodeBuild::Project.Environment"
  // Properties:
  var environmentVariables: List<EnvironmentVariable>? = null
  var privilegedMode: Boolean? = null
  var imagePullCredentialsType: String? = null
  var registryCredential: RegistryCredential? = null
  var certificate: String? = null



}
fun environment(type: String,image: String,computeType: String, init: Environment.()
    -> Unit
): Environment = Environment( type, image, computeType).apply(init)



class ProjectTriggers() {
   fun getResourceType() = "AWS::CodeBuild::Project.ProjectTriggers"
  // Properties:
  var filterGroups: List<FilterGroup>? = null
  var webhook: Boolean? = null



}
fun projectTriggers( init: ProjectTriggers.()
    -> Unit
): ProjectTriggers = ProjectTriggers().apply(init)



class CloudWatchLogsConfig(
  val status: String) {
   fun getResourceType() = "AWS::CodeBuild::Project.CloudWatchLogsConfig"
  // Properties:
  var groupName: String? = null
  var streamName: String? = null



}
fun cloudWatchLogsConfig(status: String, init: CloudWatchLogsConfig.()
    -> Unit
): CloudWatchLogsConfig = CloudWatchLogsConfig( status).apply(init)



class VpcConfig() {
   fun getResourceType() = "AWS::CodeBuild::Project.VpcConfig"
  // Properties:
  var subnets: List<String>? = null
  var vpcId: String? = null
  var securityGroupIds: List<String>? = null



}
fun vpcConfig( init: VpcConfig.()
    -> Unit
): VpcConfig = VpcConfig().apply(init)



class SourceAuth(
  val type: String) {
   fun getResourceType() = "AWS::CodeBuild::Project.SourceAuth"
  // Properties:
  var resource: String? = null



}
fun sourceAuth(type: String, init: SourceAuth.()
    -> Unit
): SourceAuth = SourceAuth( type).apply(init)



class WebhookFilter(
  val pattern: String,
  val type: String) {
   fun getResourceType() = "AWS::CodeBuild::Project.WebhookFilter"
  // Properties:
  var excludeMatchedPattern: Boolean? = null



}
fun webhookFilter(pattern: String,type: String, init: WebhookFilter.()
    -> Unit
): WebhookFilter = WebhookFilter( pattern, type).apply(init)



class Tag(
  val value: String,
  val key: String) {
   fun getResourceType() = "Tag"
  // Properties:



}
fun tag(value: String,key: String, init: Tag.()
    -> Unit
): Tag = Tag( value, key).apply(init)



class RegistryCredential(
  val credential: String,
  val credentialProvider: String) {
   fun getResourceType() = "AWS::CodeBuild::Project.RegistryCredential"
  // Properties:



}
fun registryCredential(credential: String,credentialProvider: String, init: RegistryCredential.()
    -> Unit
): RegistryCredential = RegistryCredential( credential, credentialProvider).apply(init)



class FilterGroup() {
   fun getResourceType() = "AWS::CodeBuild::Project.FilterGroup"
  // Properties:



}
fun filterGroup( init: FilterGroup.()
    -> Unit
): FilterGroup = FilterGroup().apply(init)



class S3LogsConfig(
  val status: String) {
   fun getResourceType() = "AWS::CodeBuild::Project.S3LogsConfig"
  // Properties:
  var encryptionDisabled: Boolean? = null
  var location: String? = null



}
fun s3LogsConfig(status: String, init: S3LogsConfig.()
    -> Unit
): S3LogsConfig = S3LogsConfig( status).apply(init)


}
fun aWS_CodeBuild_Project(source: AWS_CodeBuild_Project.Source,artifacts: AWS_CodeBuild_Project.Artifacts,serviceRole: String,environment: AWS_CodeBuild_Project.Environment, init: AWS_CodeBuild_Project.()
    -> Unit
): AWS_CodeBuild_Project = AWS_CodeBuild_Project( source, artifacts, serviceRole, environment).apply(init)

