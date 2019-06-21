
package com.typedpath.awscloudformation.schema.sample
// created on Fri Jun 07 14:09:10 BST 2019 by fun jsonSchema2Kotlin(kotlin.String, kotlin.String, jdk.nashorn.api.scripting.ScriptObjectMirror, kotlin.collections.List<kotlin.collections.Map.Entry<kotlin.String, jdk.nashorn.api.scripting.ScriptObjectMirror>>, kotlin.Boolean): kotlin.Pair<kotlin.String, kotlin.String>
            
class AWS_S3_Bucket(): com.typedpath.awscloudformation.Resource ()  {
  override fun getResourceType() = "AWS::S3::Bucket"
  // Properties:
  var accelerateConfiguration: AccelerateConfiguration? = null
  var accessControl: String? = null
  var analyticsConfigurations: List<AnalyticsConfiguration>? = null
  var bucketEncryption: BucketEncryption? = null
  var bucketName: String? = null
  var corsConfiguration: CorsConfiguration? = null
  var inventoryConfigurations: List<InventoryConfiguration>? = null
  var lifecycleConfiguration: LifecycleConfiguration? = null
  var loggingConfiguration: LoggingConfiguration? = null
  var metricsConfigurations: List<MetricsConfiguration>? = null
  var notificationConfiguration: NotificationConfiguration? = null
  var publicAccessBlockConfiguration: PublicAccessBlockConfiguration? = null
  var replicationConfiguration: ReplicationConfiguration? = null
  var tags: List<Tag>? = null
  var versioningConfiguration: VersioningConfiguration? = null
  var websiteConfiguration: WebsiteConfiguration? = null
  fun arnAttribute() = Attribute(this, "Arn");
  fun domainNameAttribute() = Attribute(this, "DomainName");
  fun dualStackDomainNameAttribute() = Attribute(this, "DualStackDomainName");
  fun regionalDomainNameAttribute() = Attribute(this, "RegionalDomainName");
  fun websiteURLAttribute() = Attribute(this, "WebsiteURL");




class AbortIncompleteMultipartUpload(
  val daysAfterInitiation: Int) {
   fun getResourceType() = "AWS::S3::Bucket.AbortIncompleteMultipartUpload"
  // Properties:



}
fun abortIncompleteMultipartUpload(daysAfterInitiation: Int, init: AbortIncompleteMultipartUpload.()
    -> Unit
): AbortIncompleteMultipartUpload = AbortIncompleteMultipartUpload( daysAfterInitiation).apply(init)



class AccelerateConfiguration(
  val accelerationStatus: String) {
   fun getResourceType() = "AWS::S3::Bucket.AccelerateConfiguration"
  // Properties:



}
fun accelerateConfiguration(accelerationStatus: String, init: AccelerateConfiguration.()
    -> Unit
): AccelerateConfiguration = AccelerateConfiguration( accelerationStatus).apply(init)



class AccessControlTranslation(
  val owner: String) {
   fun getResourceType() = "AWS::S3::Bucket.AccessControlTranslation"
  // Properties:



}
fun accessControlTranslation(owner: String, init: AccessControlTranslation.()
    -> Unit
): AccessControlTranslation = AccessControlTranslation( owner).apply(init)



class AnalyticsConfiguration(
  val id: String,
  val storageClassAnalysis: StorageClassAnalysis) {
   fun getResourceType() = "AWS::S3::Bucket.AnalyticsConfiguration"
  // Properties:
  var prefix: String? = null
  var tagFilters: List<TagFilter>? = null



}
fun analyticsConfiguration(id: String,storageClassAnalysis: StorageClassAnalysis, init: AnalyticsConfiguration.()
    -> Unit
): AnalyticsConfiguration = AnalyticsConfiguration( id, storageClassAnalysis).apply(init)



class BucketEncryption(
  val serverSideEncryptionConfiguration: List<ServerSideEncryptionRule>) {
   fun getResourceType() = "AWS::S3::Bucket.BucketEncryption"
  // Properties:



}
fun bucketEncryption(serverSideEncryptionConfiguration: List<ServerSideEncryptionRule>, init: BucketEncryption.()
    -> Unit
): BucketEncryption = BucketEncryption( serverSideEncryptionConfiguration).apply(init)



class CorsConfiguration(
  val corsRules: List<CorsRule>) {
   fun getResourceType() = "AWS::S3::Bucket.CorsConfiguration"
  // Properties:



}
fun corsConfiguration(corsRules: List<CorsRule>, init: CorsConfiguration.()
    -> Unit
): CorsConfiguration = CorsConfiguration( corsRules).apply(init)



class CorsRule(
  val allowedMethods: List<String>,
  val allowedOrigins: List<String>) {
   fun getResourceType() = "AWS::S3::Bucket.CorsRule"
  // Properties:
  var allowedHeaders: List<String>? = null
  var exposedHeaders: List<String>? = null
  var id: String? = null
  var maxAge: Int? = null



}
fun corsRule(allowedMethods: List<String>,allowedOrigins: List<String>, init: CorsRule.()
    -> Unit
): CorsRule = CorsRule( allowedMethods, allowedOrigins).apply(init)



class DataExport(
  val destination: Destination,
  val outputSchemaVersion: String) {
   fun getResourceType() = "AWS::S3::Bucket.DataExport"
  // Properties:



}
fun dataExport(destination: Destination,outputSchemaVersion: String, init: DataExport.()
    -> Unit
): DataExport = DataExport( destination, outputSchemaVersion).apply(init)



class Destination(
  val bucketArn: String,
  val format: String) {
   fun getResourceType() = "AWS::S3::Bucket.Destination"
  // Properties:
  var bucketAccountId: String? = null
  var prefix: String? = null



}
fun destination(bucketArn: String,format: String, init: Destination.()
    -> Unit
): Destination = Destination( bucketArn, format).apply(init)



class EncryptionConfiguration(
  val replicaKmsKeyID: String) {
   fun getResourceType() = "AWS::S3::Bucket.EncryptionConfiguration"
  // Properties:



}
fun encryptionConfiguration(replicaKmsKeyID: String, init: EncryptionConfiguration.()
    -> Unit
): EncryptionConfiguration = EncryptionConfiguration( replicaKmsKeyID).apply(init)



class FilterRule(
  val name: String,
  val value: String) {
   fun getResourceType() = "AWS::S3::Bucket.FilterRule"
  // Properties:



}
fun filterRule(name: String,value: String, init: FilterRule.()
    -> Unit
): FilterRule = FilterRule( name, value).apply(init)



class InventoryConfiguration(
  val destination: Destination,
  val enabled: Boolean,
  val id: String,
  val includedObjectVersions: String,
  val scheduleFrequency: String) {
   fun getResourceType() = "AWS::S3::Bucket.InventoryConfiguration"
  // Properties:
  var optionalFields: List<String>? = null
  var prefix: String? = null



}
fun inventoryConfiguration(destination: Destination,enabled: Boolean,id: String,includedObjectVersions: String,scheduleFrequency: String, init: InventoryConfiguration.()
    -> Unit
): InventoryConfiguration = InventoryConfiguration( destination, enabled, id, includedObjectVersions, scheduleFrequency).apply(init)



class LambdaConfiguration(
  val event: String,
  val function: String) {
   fun getResourceType() = "AWS::S3::Bucket.LambdaConfiguration"
  // Properties:
  var filter: NotificationFilter? = null



}
fun lambdaConfiguration(event: String,function: String, init: LambdaConfiguration.()
    -> Unit
): LambdaConfiguration = LambdaConfiguration( event, function).apply(init)



class LifecycleConfiguration(
  val rules: List<Rule>) {
   fun getResourceType() = "AWS::S3::Bucket.LifecycleConfiguration"
  // Properties:



}
fun lifecycleConfiguration(rules: List<Rule>, init: LifecycleConfiguration.()
    -> Unit
): LifecycleConfiguration = LifecycleConfiguration( rules).apply(init)



class LoggingConfiguration() {
   fun getResourceType() = "AWS::S3::Bucket.LoggingConfiguration"
  // Properties:
  var destinationBucketName: String? = null
  var logFilePrefix: String? = null



}
fun loggingConfiguration( init: LoggingConfiguration.()
    -> Unit
): LoggingConfiguration = LoggingConfiguration().apply(init)



class MetricsConfiguration(
  val id: String) {
   fun getResourceType() = "AWS::S3::Bucket.MetricsConfiguration"
  // Properties:
  var prefix: String? = null
  var tagFilters: List<TagFilter>? = null



}
fun metricsConfiguration(id: String, init: MetricsConfiguration.()
    -> Unit
): MetricsConfiguration = MetricsConfiguration( id).apply(init)



class NoncurrentVersionTransition(
  val storageClass: String,
  val transitionInDays: Int) {
   fun getResourceType() = "AWS::S3::Bucket.NoncurrentVersionTransition"
  // Properties:



}
fun noncurrentVersionTransition(storageClass: String,transitionInDays: Int, init: NoncurrentVersionTransition.()
    -> Unit
): NoncurrentVersionTransition = NoncurrentVersionTransition( storageClass, transitionInDays).apply(init)



class NotificationConfiguration() {
   fun getResourceType() = "AWS::S3::Bucket.NotificationConfiguration"
  // Properties:
  var lambdaConfigurations: List<LambdaConfiguration>? = null
  var queueConfigurations: List<QueueConfiguration>? = null
  var topicConfigurations: List<TopicConfiguration>? = null



}
fun notificationConfiguration( init: NotificationConfiguration.()
    -> Unit
): NotificationConfiguration = NotificationConfiguration().apply(init)



class NotificationFilter(
  val s3Key: S3KeyFilter) {
   fun getResourceType() = "AWS::S3::Bucket.NotificationFilter"
  // Properties:



}
fun notificationFilter(s3Key: S3KeyFilter, init: NotificationFilter.()
    -> Unit
): NotificationFilter = NotificationFilter( s3Key).apply(init)



class PublicAccessBlockConfiguration() {
   fun getResourceType() = "AWS::S3::Bucket.PublicAccessBlockConfiguration"
  // Properties:
  var blockPublicAcls: Boolean? = null
  var blockPublicPolicy: Boolean? = null
  var ignorePublicAcls: Boolean? = null
  var restrictPublicBuckets: Boolean? = null



}
fun publicAccessBlockConfiguration( init: PublicAccessBlockConfiguration.()
    -> Unit
): PublicAccessBlockConfiguration = PublicAccessBlockConfiguration().apply(init)



class QueueConfiguration(
  val event: String,
  val queue: String) {
   fun getResourceType() = "AWS::S3::Bucket.QueueConfiguration"
  // Properties:
  var filter: NotificationFilter? = null



}
fun queueConfiguration(event: String,queue: String, init: QueueConfiguration.()
    -> Unit
): QueueConfiguration = QueueConfiguration( event, queue).apply(init)



class RedirectAllRequestsTo(
  val hostName: String) {
   fun getResourceType() = "AWS::S3::Bucket.RedirectAllRequestsTo"
  // Properties:
  var protocol: String? = null



}
fun redirectAllRequestsTo(hostName: String, init: RedirectAllRequestsTo.()
    -> Unit
): RedirectAllRequestsTo = RedirectAllRequestsTo( hostName).apply(init)



class RedirectRule() {
   fun getResourceType() = "AWS::S3::Bucket.RedirectRule"
  // Properties:
  var hostName: String? = null
  var httpRedirectCode: String? = null
  var protocol: String? = null
  var replaceKeyPrefixWith: String? = null
  var replaceKeyWith: String? = null



}
fun redirectRule( init: RedirectRule.()
    -> Unit
): RedirectRule = RedirectRule().apply(init)



class ReplicationConfiguration(
  val role: String,
  val rules: List<ReplicationRule>) {
   fun getResourceType() = "AWS::S3::Bucket.ReplicationConfiguration"
  // Properties:



}
fun replicationConfiguration(role: String,rules: List<ReplicationRule>, init: ReplicationConfiguration.()
    -> Unit
): ReplicationConfiguration = ReplicationConfiguration( role, rules).apply(init)



class ReplicationDestination(
  val bucket: String) {
   fun getResourceType() = "AWS::S3::Bucket.ReplicationDestination"
  // Properties:
  var accessControlTranslation: AccessControlTranslation? = null
  var account: String? = null
  var encryptionConfiguration: EncryptionConfiguration? = null
  var storageClass: String? = null



}
fun replicationDestination(bucket: String, init: ReplicationDestination.()
    -> Unit
): ReplicationDestination = ReplicationDestination( bucket).apply(init)



class ReplicationRule(
  val destination: ReplicationDestination,
  val prefix: String,
  val status: String) {
   fun getResourceType() = "AWS::S3::Bucket.ReplicationRule"
  // Properties:
  var id: String? = null
  var sourceSelectionCriteria: SourceSelectionCriteria? = null



}
fun replicationRule(destination: ReplicationDestination,prefix: String,status: String, init: ReplicationRule.()
    -> Unit
): ReplicationRule = ReplicationRule( destination, prefix, status).apply(init)



class RoutingRule(
  val redirectRule: RedirectRule) {
   fun getResourceType() = "AWS::S3::Bucket.RoutingRule"
  // Properties:
  var routingRuleCondition: RoutingRuleCondition? = null



}
fun routingRule(redirectRule: RedirectRule, init: RoutingRule.()
    -> Unit
): RoutingRule = RoutingRule( redirectRule).apply(init)



class RoutingRuleCondition() {
   fun getResourceType() = "AWS::S3::Bucket.RoutingRuleCondition"
  // Properties:
  var httpErrorCodeReturnedEquals: String? = null
  var keyPrefixEquals: String? = null



}
fun routingRuleCondition( init: RoutingRuleCondition.()
    -> Unit
): RoutingRuleCondition = RoutingRuleCondition().apply(init)



class Rule(
  val status: String) {
   fun getResourceType() = "AWS::S3::Bucket.Rule"
  // Properties:
  var abortIncompleteMultipartUpload: AbortIncompleteMultipartUpload? = null
  var expirationDate: java.util.Date? = null
  var expirationInDays: Int? = null
  var id: String? = null
  var noncurrentVersionExpirationInDays: Int? = null
  var noncurrentVersionTransition: NoncurrentVersionTransition? = null
  var noncurrentVersionTransitions: List<NoncurrentVersionTransition>? = null
  var prefix: String? = null
  var tagFilters: List<TagFilter>? = null
  var transition: Transition? = null
  var transitions: List<Transition>? = null



}
fun rule(status: String, init: Rule.()
    -> Unit
): Rule = Rule( status).apply(init)



class S3KeyFilter(
  val rules: List<FilterRule>) {
   fun getResourceType() = "AWS::S3::Bucket.S3KeyFilter"
  // Properties:



}
fun s3KeyFilter(rules: List<FilterRule>, init: S3KeyFilter.()
    -> Unit
): S3KeyFilter = S3KeyFilter( rules).apply(init)



class ServerSideEncryptionByDefault(
  val sSEAlgorithm: String) {
   fun getResourceType() = "AWS::S3::Bucket.ServerSideEncryptionByDefault"
  // Properties:
  var kMSMasterKeyID: String? = null



}
fun serverSideEncryptionByDefault(sSEAlgorithm: String, init: ServerSideEncryptionByDefault.()
    -> Unit
): ServerSideEncryptionByDefault = ServerSideEncryptionByDefault( sSEAlgorithm).apply(init)



class ServerSideEncryptionRule() {
   fun getResourceType() = "AWS::S3::Bucket.ServerSideEncryptionRule"
  // Properties:
  var serverSideEncryptionByDefault: ServerSideEncryptionByDefault? = null



}
fun serverSideEncryptionRule( init: ServerSideEncryptionRule.()
    -> Unit
): ServerSideEncryptionRule = ServerSideEncryptionRule().apply(init)



class SourceSelectionCriteria(
  val sseKmsEncryptedObjects: SseKmsEncryptedObjects) {
   fun getResourceType() = "AWS::S3::Bucket.SourceSelectionCriteria"
  // Properties:



}
fun sourceSelectionCriteria(sseKmsEncryptedObjects: SseKmsEncryptedObjects, init: SourceSelectionCriteria.()
    -> Unit
): SourceSelectionCriteria = SourceSelectionCriteria( sseKmsEncryptedObjects).apply(init)



class SseKmsEncryptedObjects(
  val status: String) {
   fun getResourceType() = "AWS::S3::Bucket.SseKmsEncryptedObjects"
  // Properties:



}
fun sseKmsEncryptedObjects(status: String, init: SseKmsEncryptedObjects.()
    -> Unit
): SseKmsEncryptedObjects = SseKmsEncryptedObjects( status).apply(init)



class StorageClassAnalysis() {
   fun getResourceType() = "AWS::S3::Bucket.StorageClassAnalysis"
  // Properties:
  var dataExport: DataExport? = null



}
fun storageClassAnalysis( init: StorageClassAnalysis.()
    -> Unit
): StorageClassAnalysis = StorageClassAnalysis().apply(init)



class TagFilter(
  val key: String,
  val value: String) {
   fun getResourceType() = "AWS::S3::Bucket.TagFilter"
  // Properties:



}
fun tagFilter(key: String,value: String, init: TagFilter.()
    -> Unit
): TagFilter = TagFilter( key, value).apply(init)



class TopicConfiguration(
  val event: String,
  val topic: String) {
   fun getResourceType() = "AWS::S3::Bucket.TopicConfiguration"
  // Properties:
  var filter: NotificationFilter? = null



}
fun topicConfiguration(event: String,topic: String, init: TopicConfiguration.()
    -> Unit
): TopicConfiguration = TopicConfiguration( event, topic).apply(init)



class Transition(
  val storageClass: String) {
   fun getResourceType() = "AWS::S3::Bucket.Transition"
  // Properties:
  var transitionDate: java.util.Date? = null
  var transitionInDays: Int? = null



}
fun transition(storageClass: String, init: Transition.()
    -> Unit
): Transition = Transition( storageClass).apply(init)



class VersioningConfiguration(
  val status: String) {
   fun getResourceType() = "AWS::S3::Bucket.VersioningConfiguration"
  // Properties:



}
fun versioningConfiguration(status: String, init: VersioningConfiguration.()
    -> Unit
): VersioningConfiguration = VersioningConfiguration( status).apply(init)



class WebsiteConfiguration() {
   fun getResourceType() = "AWS::S3::Bucket.WebsiteConfiguration"
  // Properties:
  var errorDocument: String? = null
  var indexDocument: String? = null
  var redirectAllRequestsTo: RedirectAllRequestsTo? = null
  var routingRules: List<RoutingRule>? = null



}
fun websiteConfiguration( init: WebsiteConfiguration.()
    -> Unit
): WebsiteConfiguration = WebsiteConfiguration().apply(init)



class Tag(
  val key: String,
  val value: String) {
   fun getResourceType() = "Tag"
  // Properties:



}
fun tag(key: String,value: String, init: Tag.()
    -> Unit
): Tag = Tag( key, value).apply(init)


}
fun aWS_S3_Bucket( init: AWS_S3_Bucket.()
    -> Unit
): AWS_S3_Bucket = AWS_S3_Bucket().apply(init)

