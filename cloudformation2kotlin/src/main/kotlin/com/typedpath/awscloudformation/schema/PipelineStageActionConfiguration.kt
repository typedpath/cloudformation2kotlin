package com.typedpath.awscloudformation.schema

import com.typedpath.awscloudformation.schema.AWS_CodePipeline_Pipeline

//see https://docs.aws.amazon.com/codepipeline/latest/userguide/reference-pipeline-structure.html#action-requirements

open class PipelineStageActionConfiguration

class CodeBuildActionConfiguration(val projectName: String) : PipelineStageActionConfiguration() {
  val pollForSourceChanges: Boolean?=null
}

class CodeCommitActionConfiguration(
  val repositoryName: String,
  val branchName: String
) : PipelineStageActionConfiguration() {
  val pollForSourceChanges: Boolean?=null
}

class CodeDeployActionConfiguration (
  val applicationName: String,
  val deploymentGroupName: String
)

enum class CodePipelineActionTypeIdCategory{
Source, Build, Test, Deploy, Approval, Invoke
}

enum class CodePipelineActionTypeIdOwner{
  AWS
}

fun actionTypeId(category: CodePipelineActionTypeIdCategory,
                 owner: CodePipelineActionTypeIdOwner,
                 provider: CodePipelineActionProvider
                 ) : AWS_CodePipeline_Pipeline.ActionTypeId =
  AWS_CodePipeline_Pipeline.ActionTypeId(category.name, owner.name, provider.toString(), "1")


enum class CodePipelineActionProvider(val typeDescription: String){
  //SOurce
  S3("S3")/*also deploy*/, CodeCommit("CodeCommit"),
  GitHub("GitHub"), AmazonECR("Amazon ECR"),
  CodeBuild("CodeBuild");
  override fun toString() = typeDescription
  //Build
/*  CodeBuild/*also test*/, CustomCloudBees, CustomJenkins/*also test*/, CustomTeamCity,
  //Test
  AWSDeviceFarm, CustomBlazeMeter, ThirdPartyGhostInspector,
  ThirdPartyMicroFocusStormRunnerLoad,
  ThirdPartyNouvola,
  ThirdPartyRunscope,
  CloudFormation,
  CodeDeploy,
  AmazonECS,
  ElasticBeanstalk,
  AWSOpsWorks,
  AWSServiceCatalog,
  AmazonAlexa,
  CustomXebiaLabs,
  //Approval
  	Manual,
  //Invoke
  	AWSLambda*/
}




