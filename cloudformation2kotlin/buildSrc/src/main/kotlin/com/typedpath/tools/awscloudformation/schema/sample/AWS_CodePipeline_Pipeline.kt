
package com.typedpath.awscloudformation.schema.sample
// created on Mon Jun 10 17:06:30 BST 2019 by fun jsonSchema2Kotlin(kotlin.String, kotlin.String, jdk.nashorn.api.scripting.ScriptObjectMirror, kotlin.collections.List<kotlin.collections.Map.Entry<kotlin.String, jdk.nashorn.api.scripting.ScriptObjectMirror>>, kotlin.Boolean): kotlin.Pair<kotlin.String, kotlin.String>

class AWS_CodePipeline_Pipeline(
  val roleArn: String,
  val stages: List<StageDeclaration>): com.typedpath.awscloudformation.Resource ()  {
  override fun getResourceType() = "AWS::CodePipeline::Pipeline"
  // Properties:
  var artifactStore: ArtifactStore? = null
  var artifactStores: List<ArtifactStoreMap>? = null
  var disableInboundStageTransitions: List<StageTransition>? = null
  var name: String? = null
  var restartExecutionOnUpdate: Boolean? = null
  fun versionAttribute() = Attribute(this, "Version");




  class ActionDeclaration(
    val actionTypeId: ActionTypeId,
    val name: String) {
    fun getResourceType() = "AWS::CodePipeline::Pipeline.ActionDeclaration"
    // Properties:
    var configuration: com.typedpath.awscloudformation.PipelineStageActionConfiguration? = null
    var inputArtifacts: List<InputArtifact>? = null
    var outputArtifacts: List<OutputArtifact>? = null
    var region: String? = null
    var roleArn: String? = null
    var runOrder: Int? = null



  }
  fun actionDeclaration(actionTypeId: ActionTypeId,name: String, init: ActionDeclaration.()
  -> Unit
  ): ActionDeclaration = ActionDeclaration( actionTypeId, name).apply(init)



  class ActionTypeId(
    val category: String,
    val owner: String,
    val provider: String,
    val version: String) {
    fun getResourceType() = "AWS::CodePipeline::Pipeline.ActionTypeId"
    // Properties:



  }
  fun actionTypeId(category: String,owner: String,provider: String,version: String, init: ActionTypeId.()
  -> Unit
  ): ActionTypeId = ActionTypeId( category, owner, provider, version).apply(init)



  class ArtifactStore(
    val location: String,
    val type: String) {
    fun getResourceType() = "AWS::CodePipeline::Pipeline.ArtifactStore"
    // Properties:
    var encryptionKey: EncryptionKey? = null



  }
  fun artifactStore(location: String,type: String, init: ArtifactStore.()
  -> Unit
  ): ArtifactStore = ArtifactStore( location, type).apply(init)



  class ArtifactStoreMap(
    val artifactStore: ArtifactStore,
    val region: String) {
    fun getResourceType() = "AWS::CodePipeline::Pipeline.ArtifactStoreMap"
    // Properties:



  }
  fun artifactStoreMap(artifactStore: ArtifactStore,region: String, init: ArtifactStoreMap.()
  -> Unit
  ): ArtifactStoreMap = ArtifactStoreMap( artifactStore, region).apply(init)



  class BlockerDeclaration(
    val name: String,
    val type: String) {
    fun getResourceType() = "AWS::CodePipeline::Pipeline.BlockerDeclaration"
    // Properties:



  }
  fun blockerDeclaration(name: String,type: String, init: BlockerDeclaration.()
  -> Unit
  ): BlockerDeclaration = BlockerDeclaration( name, type).apply(init)



  class EncryptionKey(
    val id: String,
    val type: String) {
    fun getResourceType() = "AWS::CodePipeline::Pipeline.EncryptionKey"
    // Properties:



  }
  fun encryptionKey(id: String,type: String, init: EncryptionKey.()
  -> Unit
  ): EncryptionKey = EncryptionKey( id, type).apply(init)



  class InputArtifact(
    val name: String) {
    fun getResourceType() = "AWS::CodePipeline::Pipeline.InputArtifact"
    // Properties:



  }
  fun inputArtifact(name: String, init: InputArtifact.()
  -> Unit
  ): InputArtifact = InputArtifact( name).apply(init)



  class OutputArtifact(
    val name: String) {
    fun getResourceType() = "AWS::CodePipeline::Pipeline.OutputArtifact"
    // Properties:



  }
  fun outputArtifact(name: String, init: OutputArtifact.()
  -> Unit
  ): OutputArtifact = OutputArtifact( name).apply(init)



  class StageDeclaration(
    val actions: List<ActionDeclaration>,
    val name: String) {
    fun getResourceType() = "AWS::CodePipeline::Pipeline.StageDeclaration"
    // Properties:
    var blockers: List<BlockerDeclaration>? = null



  }
  fun stageDeclaration(actions: List<ActionDeclaration>,name: String, init: StageDeclaration.()
  -> Unit
  ): StageDeclaration = StageDeclaration( actions, name).apply(init)



  class StageTransition(
    val reason: String,
    val stageName: String) {
    fun getResourceType() = "AWS::CodePipeline::Pipeline.StageTransition"
    // Properties:



  }
  fun stageTransition(reason: String,stageName: String, init: StageTransition.()
  -> Unit
  ): StageTransition = StageTransition( reason, stageName).apply(init)


}
fun aWS_CodePipeline_Pipeline(roleArn: String,stages: List<AWS_CodePipeline_Pipeline.StageDeclaration>, init: AWS_CodePipeline_Pipeline.()
-> Unit
): AWS_CodePipeline_Pipeline = AWS_CodePipeline_Pipeline( roleArn, stages).apply(init)

