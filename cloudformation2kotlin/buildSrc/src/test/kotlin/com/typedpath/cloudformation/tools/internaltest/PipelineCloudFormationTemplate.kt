package com.typedpath.cloudformation.tools.internaltest

import com.typedpath.awscloudformation.*
import com.typedpath.awscloudformation.schema.sample.*

class PipelineCloudFormationTemplate : CloudFormationTemplate() {

  val sourceBundleArtifactName = "sourceBundle"
  val buildArtifactName = "buildArtifact"

  // parameters

  val sourceBranchName = CloudFormationTemplate.Parameter(
    ParameterType.STRING,
    "source branch name"
  ).apply {
    default = "master"
  }

  val codeCommitRepoName = CloudFormationTemplate.Parameter(
    ParameterType.STRING,
    "repository name"
  ).apply {
    default = "mymind"
  }

  //outputs
  val ArtifactsBucket: Output by lazy {
    Output(
      ref(artifactsBucket)
//    join("-", listOf(refCurrentStack(), refCurrentRegion()))
    ).apply {
      export = Output.Export(join("-", listOf(refCurrentStack(), refCurrentRegion(), "ArtifactsBucket")))
    }
  }

  // resources
  val pipeLineBucket = AWS_S3_Bucket()

  val artifactsBucket =
    AWS_S3_Bucket().apply {
      deletionPolicy = Resource.DeletionPolicyValue.Delete
      tags = listOf(
        AWS_S3_Bucket.Tag("Name", join("-", listOf(refCurrentStack(), "ArtifactsBucket")))
      )
      versioningConfiguration = AWS_S3_Bucket.VersioningConfiguration("Enabled")

    }

  val codeBuildPolicyDocument = iamPolicy() {
    statement {
      //TODO make these constants
      action += "logs:CreateLogGroup"
      action += "logs:CreateLogStream"
      action += "logs:PutLogEvents"
      effect = IamPolicy.EffectType.Allow
      resource += "*"
    }
    statement {
      action += "codecommit:GitPull"
      effect = IamPolicy.EffectType.Allow
      resource += join(":", listOf("arn", "aws", "codecommit", refCurrentRegion(), refCurrentAccountId(), "mymind"))
    }
    statement {
      action += "s3:PutObject"
      action += "s3:GetObject"
      action += "s3:GetObjectVersion"
      effect = IamPolicy.EffectType.Allow
      resource += join("", listOf("arn:aws:s3:::", ref(artifactsBucket), "/*"))
    }
  }

  val codeBuildPolicy by lazy {
    AWS_IAM_Policy(codeBuildPolicyDocument, join("-", listOf(refCurrentStack(), "CodeBuildPolicy"))).apply {
      roles = listOf(ref(codeBuildRole))
    }
  }

  val assumeRolePolicyDocument = IamPolicy().apply {
    statement {
      action += "sts:AssumeRole"
      effect = IamPolicy.EffectType.Allow
      principal = mapOf(
        Pair(
          IamPolicy.PrincipalType.Service,
          listOf("codebuild.amazonaws.com", "codepipeline.amazonaws.com")
        )
      )
    }
  }

  val codeBuildRole = AWS_IAM_Role(assumeRolePolicyDocument).apply {
    path = "/"
    roleName = join("-", listOf(refCurrentStack(), "CodeBuild"))
  }

  //TODO Description, DependsOn - what is the relationship with by lazy ?
  val codeBuildProject = AWS_CodeBuild_Project(
    AWS_CodeBuild_Project.Source("CODECOMMIT").apply {
      //why is location an optional ?
      location = join("", listOf("https://git-codecommit.", refCurrentRegion(), ".amazonaws.com/v1/repos/", "mymind"))
    },
    AWS_CodeBuild_Project.Artifacts("S3").apply {
      encryptionDisabled = true
      location = ref(artifactsBucket)
      name = "mymind"
    },
    ref(codeBuildRole),
    AWS_CodeBuild_Project.Environment(
      //TODO constants for these
      "LINUX_CONTAINER",
      "aws/codebuild/standard:1.0-1.8.0",
      "BUILD_GENERAL1_SMALL"
    ).apply {
    }
  ).apply {
    //Description, DependsOn here
    badgeEnabled = true
    cache = AWS_CodeBuild_Project.ProjectCache("LOCAL").apply { modes = listOf("LOCAL_CUSTOM_CACHE") }
    name = join("", listOf("CodeBuildProject", refCurrentStack()))
  }

//val pipeline = AWS_

  val codePipelineServicePolicy = IamPolicy().apply {
    statement {
      action += "s3:GetObject"
      action += "s3:GetObjectVersion"
      action += "s3:GetBucketVersioning"
      effect = IamPolicy.EffectType.Allow
      resource += "*"
    }
    statement {
      action += "s3:PutObject"
      action += "s3:GetObjectVersion"
      action += "s3:GetBucketVersion"
      effect = IamPolicy.EffectType.Allow
      resource += "arn:aws:s3:::codepipeline*"
      resource += "arn:aws:s3:::elasticbeanstalk*"
    }
    statement {
      action += "codecommit:GetBranch"
      action += "codecommit:GetCommit"
      action += "codecommit:UploadArchive"
      action += "codecommit:GetUploadArchiveStatus"
      action += "codecommit:CancelUploadArchive"
      effect = IamPolicy.EffectType.Allow
      resource += join(":", listOf("arn:aws:codecommit", refCurrentRegion(), refCurrentAccountId(), "mymind"))
    }
    statement {
      action += "codebuild:*"
      effect = IamPolicy.EffectType.Allow
      resource += "*"
    }
    statement {
      action += "autoscaling:*"
      action += "cloudwatch:*"
      action += "s3:*"
      action += "sns:*"
      action += "cloudformation:*"
      action += "sqs:*"
      action += "iam:PassRole"
      effect = IamPolicy.EffectType.Allow
      resource += "*"
    }
    statement {
      action += "lambda:InvokeFunction"
      action += "lambda:ListFunctions"
      effect = IamPolicy.EffectType.Allow
      resource += "*"
    }
  }

  val codePipelineServiceRole = AWS_IAM_Role(assumeRolePolicyDocument).apply {
    policies = listOf(AWS_IAM_Role.Policy(codePipelineServicePolicy, "codePipelineServiceRole"))
  }

  val sourceStage = AWS_CodePipeline_Pipeline.StageDeclaration(
    listOf(
      AWS_CodePipeline_Pipeline.ActionDeclaration(
        actionTypeId(
          CodePipelineActionTypeIdCategory.Source, CodePipelineActionTypeIdOwner.AWS,
          CodePipelineActionProvider.CodeCommit
        ),
        "SourceAction"
      ).apply {
        outputArtifacts = listOf(AWS_CodePipeline_Pipeline.OutputArtifact(sourceBundleArtifactName))
        configuration = CodeCommitActionConfiguration(ref(codeCommitRepoName), ref(sourceBranchName))
        runOrder = 1
      }
    ),
    "Source"
  ).apply {
  }

  val buildStage = AWS_CodePipeline_Pipeline.StageDeclaration(
    listOf(
      AWS_CodePipeline_Pipeline.ActionDeclaration(
        actionTypeId(
          CodePipelineActionTypeIdCategory.Build, CodePipelineActionTypeIdOwner.AWS,
          CodePipelineActionProvider.CodeBuild
        ),
        "BuildAction"
      ).apply {
        inputArtifacts = listOf(AWS_CodePipeline_Pipeline.InputArtifact(sourceBundleArtifactName))
        outputArtifacts = listOf(AWS_CodePipeline_Pipeline.OutputArtifact(buildArtifactName))
        configuration = CodeBuildActionConfiguration(join("", listOf("CodeBuildProject", refCurrentStack())))
        runOrder = 1
      }
    ),
    "Build"
  ).apply {
  }

  val artifactStoreImpl = AWS_CodePipeline_Pipeline.ArtifactStore(
    location = ref(artifactsBucket), type = "S3"
  )

  val pipeline = AWS_CodePipeline_Pipeline(
    ref(codePipelineServiceRole.arnAttribute()),
    listOf(sourceStage, buildStage)
  ).apply {
    this.artifactStore = artifactStoreImpl
  }
}

fun main(args: Array<String>) {
  println(toYaml(PipelineCloudFormationTemplate()))
}
