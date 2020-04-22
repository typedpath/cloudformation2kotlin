package com.typedpath.awscloudformation.test.pipeline

import com.typedpath.awscloudformation.*
import com.typedpath.awscloudformation.schema.*
import com.typedpath.iam2kotlin.IamPolicy
import com.typedpath.iam2kotlin.resources.autoscaling.AutoscalingAction
import com.typedpath.iam2kotlin.resources.cloudformation.CloudformationAction
import com.typedpath.iam2kotlin.resources.cloudwatch.CloudwatchAction
import com.typedpath.iam2kotlin.resources.codebuild.CodebuildAction
import com.typedpath.iam2kotlin.resources.codecommit.CodecommitAction
import com.typedpath.iam2kotlin.resources.codepipeline.CodepipelineAction
import com.typedpath.iam2kotlin.resources.iam.IamAction
import com.typedpath.iam2kotlin.resources.lambda.LambdaAction
import com.typedpath.iam2kotlin.resources.logs.LogsAction
import com.typedpath.iam2kotlin.resources.s3.S3Action
import com.typedpath.iam2kotlin.resources.sns.SnsAction
import com.typedpath.iam2kotlin.resources.sqs.SqsAction
import com.typedpath.iam2kotlin.resources.sts.StsAction

//TODO
// add latest deployment function
//change template file from appspec.yml

class PipelineCloudFormationTemplate(defaultReponame: String, targetCloudFormationTemplateFilename: String,
                                     codePackageFilename: String
) : CloudFormationTemplate() {


    val sourceBundleArtifactName = "sourceBundle"
    val buildArtifactName = "buildArtifact"
    val lambdaDeployArtifactName = "lambdaDeployArtifact"
    // parameters

    val sourceBranchName = Parameter(
            ParameterType.STRING,
            "source branch name"
    ) {
        default = "master"
    }

    //todo remove this parameter or adjust user of defaultRepoName
    val codeCommitRepoName = Parameter(
            ParameterType.STRING,
            "repository name"
    ) {
        default = defaultReponame
    }

    //outputs
    val ArtifactsBucket: Output by lazy {
        Output(
                ref(artifactsBucket)
//    join("-", listOf(refCurrentStack(), refCurrentRegion()))
        ) {
            export = Output.Export(join("-", listOf(refCurrentStack(), refCurrentRegion(), "ArtifactsBucket")))
        }
    }

    val codeRepository = AWS_CodeCommit_Repository(defaultReponame) {
    }

    val pipeLineBucket = AWS_S3_Bucket()

    val artifactsBucket =
            AWS_S3_Bucket() {
                deletionPolicy = Resource.DeletionPolicyValue.Delete
                tags = listOf(
                        AWS_S3_Bucket.Tag("Name", join("-", listOf(refCurrentStack(), "ArtifactsBucket")))
                )
                versioningConfiguration = AWS_S3_Bucket.VersioningConfiguration("Enabled")

            }

    val codeBuildPolicyDocument = IamPolicy {
        statement {
            //TODO make these constants
            action ( LogsAction.CreateLogGroup)
            action ( LogsAction.CreateLogStream)
            action ( LogsAction.PutLogEvents)
            effect = IamPolicy.EffectType.Allow
            resource ( IamPolicy.Resource("*"))
        }
        statement {
            action ( CodecommitAction.GitPull)
            effect = IamPolicy.EffectType.Allow
            resource ( IamPolicy.Resource(join(":", listOf("arn", "aws", "codecommit", refCurrentRegion(), refCurrentAccountId(), defaultReponame))))
        }
        statement {
            action ( S3Action.PutObject)
            action ( S3Action.GetObject)
            action ( S3Action.GetObjectVersion)
            effect = IamPolicy.EffectType.Allow
            resource ( IamPolicy.Resource(join("", listOf("arn:aws:s3:::", ref(artifactsBucket), "/*"))))
        }
    }

    val codeBuildPolicy by lazy {
        AWS_IAM_Policy(codeBuildPolicyDocument, join("-", listOf(refCurrentStack(), "CodeBuildPolicy"))) {
            roles = listOf(ref(codeBuildRole))
        }
    }

    val assumeRolePolicyDocument = IamPolicy {
        statement {
            action ( StsAction.AssumeRole)
            effect = IamPolicy.EffectType.Allow
            principal = mutableMapOf(
                    Pair(
                            IamPolicy.PrincipalType.Service,
                            listOf("codebuild.amazonaws.com", "codepipeline.amazonaws.com", "codedeploy.amazonaws.com")
                    )
            )
        }
    }

    val codeBuildRole = AWS_IAM_Role(assumeRolePolicyDocument) {
        path = "/"
        roleName = join("-", listOf(refCurrentStack(), "CodeBuild"))
    }

    //TODO Description, DependsOn - what is the relationship with by lazy ?
    val codeBuildProject = AWS_CodeBuild_Project(
            AWS_CodeBuild_Project.Source("CODECOMMIT") {
                //why is location an optional ?
                location = join("", listOf("https://git-codecommit.", refCurrentRegion(), ".amazonaws.com/v1/repos/", defaultReponame))
            },
            AWS_CodeBuild_Project.Artifacts("S3") {
                encryptionDisabled = true
                location = ref(artifactsBucket)
                name = defaultReponame
                packaging = "NONE"

            },
            ref(codeBuildRole),
            AWS_CodeBuild_Project.Environment(
                    //TODO constants for these
                    "LINUX_CONTAINER",
                    "aws/codebuild/standard:1.0-1.8.0",
                    "BUILD_GENERAL1_SMALL"
            )
    ) {
        //Description, DependsOn here
        badgeEnabled = true
        cache = AWS_CodeBuild_Project.ProjectCache("LOCAL") { modes = listOf("LOCAL_CUSTOM_CACHE") }
        name = join("", listOf("CodeBuildProject", refCurrentStack()))
    }

    val codePipelineServicePolicy = IamPolicy {
        statement {
            action ( S3Action.GetObject)
            action ( S3Action.GetObjectVersion)
            action ( S3Action.GetBucketVersioning)
            effect = IamPolicy.EffectType.Allow
            resource ( IamPolicy.Resource("*"))
        }
        statement {
            action ( S3Action.PutObject)
            action ( S3Action.GetObjectVersion)
            action ( S3Action.GetBucketVersioning) // was "s3:GetBucketVersion"
            effect = IamPolicy.EffectType.Allow
            resource (  S3Action.GetBucketVersioning.byBucketname("codepipeline*"))
            resource ( S3Action.GetBucketVersioning.byBucketname("elasticbeanstalk*") )
        }
        statement {
            action (  CodecommitAction.GetBranch)
            action (  CodecommitAction.GetCommit)
            action (  CodecommitAction.UploadArchive)
            action (  CodecommitAction.GetUploadArchiveStatus)
            action (  CodecommitAction.CancelUploadArchive)
            effect = IamPolicy.EffectType.Allow
            resource ( IamPolicy.Resource(join(":", listOf("arn:aws:codecommit", refCurrentRegion(), refCurrentAccountId(), defaultReponame))))
        }
        statement {
            action ( CodebuildAction.All)
            effect = IamPolicy.EffectType.Allow
            resource ( IamPolicy.Resource.All)
        }
        statement {
            action ( AutoscalingAction.All )
            action ( CloudwatchAction.All)
            action ( S3Action.All)
            action ( SnsAction.All)
            action ( CloudformationAction.All)
            action ( SqsAction.All)
            action ( IamAction.PassRole)
            effect = IamPolicy.EffectType.Allow
            resource ( IamPolicy.Resource.All)
        }
        statement {
            action ( LambdaAction.InvokeFunction)
            action ( LambdaAction.ListFunctions)
            effect = IamPolicy.EffectType.Allow
            resource ( IamPolicy.Resource.All)
        }
    }

    val codePipelineServiceRole = AWS_IAM_Role(assumeRolePolicyDocument) {
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
                    ) {
                        outputArtifacts = listOf(AWS_CodePipeline_Pipeline.OutputArtifact(sourceBundleArtifactName))
                        configuration = CodeCommitActionConfiguration(ref(codeCommitRepoName), ref(sourceBranchName))
                        runOrder = 1
                    }
            ),
            "Source"
    )

    val buildStage = AWS_CodePipeline_Pipeline.StageDeclaration(
            listOf(
                    AWS_CodePipeline_Pipeline.ActionDeclaration(
                            actionTypeId(
                                    CodePipelineActionTypeIdCategory.Build, CodePipelineActionTypeIdOwner.AWS,
                                    CodePipelineActionProvider.CodeBuild
                            ),
                            "BuildAction"
                    ) {
                        inputArtifacts = listOf(AWS_CodePipeline_Pipeline.InputArtifact(sourceBundleArtifactName))
                        outputArtifacts = listOf(AWS_CodePipeline_Pipeline.OutputArtifact(buildArtifactName))
                        configuration = CodeBuildActionConfiguration(join("", listOf("CodeBuildProject", refCurrentStack())))
                        runOrder = 1
                    }
            ),
            "Build"
    )

    val lambdaAppName = "appOf$defaultReponame"
    val lambdaDeploymentGroup = "deploymentGroupOf$defaultReponame"

    var deployApp = AWS_CodeDeploy_Application() {
        applicationName = lambdaAppName
        computePlatform = "Lambda"
    }

    //see https://stackoverflow.com/questions/52636182/ec2tagfilters-in-deployment-group-for-computeplatform-lambda
    var deployGroup = AWS_CodeDeploy_DeploymentGroup(lambdaAppName, ref(codePipelineServiceRole.arnAttribute())) {
        deploymentConfigName = "CodeDeployDefault.LambdaAllAtOnce"
        deploymentGroupName = lambdaDeploymentGroup
        deploymentStyle = AWS_CodeDeploy_DeploymentGroup.DeploymentStyle() {
            deploymentOption = "WITH_TRAFFIC_CONTROL"
            deploymentType = "BLUE_GREEN"
        }
    }

    val deployStage by lazy {
        AWS_CodePipeline_Pipeline.StageDeclaration(
                listOf(
                        AWS_CodePipeline_Pipeline.ActionDeclaration(
                                actionTypeId(
                                        CodePipelineActionTypeIdCategory.Deploy, CodePipelineActionTypeIdOwner.AWS,
                                        CodePipelineActionProvider.CodeDeploy
                                ),
                                "DeployAction"
                        ) {
                            inputArtifacts = listOf(AWS_CodePipeline_Pipeline.InputArtifact(lambdaDeployArtifactName))
                            //outputArtifacts = listOf(AWS_CodePipeline_Pipeline.OutputArtifact(buildArtifactName))
                            configuration = CodeDeployActionConfiguration(lambdaAppName, lambdaDeploymentGroup)
                            runOrder = 1
                            //roleArn = ref(lambdaRole.arnAttribute())
                        }
                ),
                "Deploy"
        )
    }

    val artifactStoreImpl = AWS_CodePipeline_Pipeline.ArtifactStore(
            location = ref(artifactsBucket), type = "S3"
    )

    val lambdaUnzipFunctionName = "lambdaUnzip$defaultReponame"
    val lambdaDeployFunctionName = "lambdaDeploy$defaultReponame"

    val unzipcode = AWS_Lambda_Function.Code() {
        zipFile = inlinePythonCode(pythonUnzipArtifactFunction)
    }

    val deploycode = AWS_Lambda_Function.Code() {
        zipFile = inlinePythonCode(pythonDeployLambdaCodeFunction(targetCloudFormationTemplateFilename, codePackageFilename))
    }

    val lambdaAssumeRolePolicyDocument = IamPolicy() {
        statement {
            //TODO make this a constant
            action ( StsAction.AssumeRole)
            effect = IamPolicy.EffectType.Allow
            //TODO make this a constant
            principal = mutableMapOf(Pair(IamPolicy.PrincipalType.Service, listOf("lambda.amazonaws.com"))
            )
        }

    }

    val lambdaPolicy = IamPolicy() {
        statement {
            action ( CodepipelineAction.PutJobSuccessResult)
            action ( CodepipelineAction.PutJobFailureResult)
            action ( CloudformationAction.All)
            action ( IamAction.All)
            action ( LambdaAction.All )
            effect = IamPolicy.EffectType.Allow
            resource  ( IamPolicy.Resource.All)
        }
    }

    // https://docs.aws.amazon.com/codepipeline/latest/userguide/actions-invoke-lambda-function.html
    val lambdaRole = AWS_IAM_Role(lambdaAssumeRolePolicyDocument) {
        //TODO make this a constantS
        policies = listOf(
                AWS_IAM_Role.Policy(lambdaPolicy, "lambdaPolicy")
        )
        managedPolicyArns = listOf("arn:aws:iam::aws:policy/AWSLambdaExecute")

    }

    val lambdaUnzipFunction = AWS_Lambda_Function(unzipcode, "index.handler",
            ref(lambdaRole.arnAttribute()), LambdaRuntime.Python3_7.id) {
        functionName = lambdaUnzipFunctionName
    }

    val lambdaDeployFunction = AWS_Lambda_Function(deploycode, "index.handler",
            ref(lambdaRole.arnAttribute()), LambdaRuntime.Python3_7.id) {
        functionName = lambdaDeployFunctionName
    }


    val lambdaUnzipStage = AWS_CodePipeline_Pipeline.StageDeclaration(
            listOf(
                    AWS_CodePipeline_Pipeline.ActionDeclaration(
                            actionTypeId(
                                    CodePipelineActionTypeIdCategory.Invoke, CodePipelineActionTypeIdOwner.AWS,
                                    CodePipelineActionProvider.AWSLambda
                            ),
                            "LambdaUnzipAction"
                    ) {
                        inputArtifacts = listOf(AWS_CodePipeline_Pipeline.InputArtifact(buildArtifactName))
                        outputArtifacts = listOf(AWS_CodePipeline_Pipeline.OutputArtifact(lambdaDeployArtifactName))
                        configuration = InvokeLambdaActionConfiguration(lambdaUnzipFunctionName)
                        runOrder = 1
                    }

            ), "LambdaUnzipStage"
    )

    val lambdaDeployStage = AWS_CodePipeline_Pipeline.StageDeclaration(
            listOf(
                    AWS_CodePipeline_Pipeline.ActionDeclaration(
                            actionTypeId(
                                    CodePipelineActionTypeIdCategory.Invoke, CodePipelineActionTypeIdOwner.AWS,
                                    CodePipelineActionProvider.AWSLambda
                            ),
                            "LambdaDeployAction"
                    ) {
                        inputArtifacts = listOf(AWS_CodePipeline_Pipeline.InputArtifact(lambdaDeployArtifactName))
                        configuration = InvokeLambdaActionConfiguration(lambdaDeployFunctionName)
                        runOrder = 1
                    }

            ), "LambdaDeployStage"
    )


    val pipeline = AWS_CodePipeline_Pipeline(
            ref(codePipelineServiceRole.arnAttribute()),
            listOf(sourceStage, buildStage, lambdaUnzipStage, lambdaDeployStage)
    ) {
        this.artifactStore = artifactStoreImpl
    }
}
