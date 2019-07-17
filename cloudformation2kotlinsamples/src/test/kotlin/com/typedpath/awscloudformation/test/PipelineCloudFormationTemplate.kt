package com.typedpath.awscloudformation.test

import com.typedpath.awscloudformation.*
import com.typedpath.awscloudformation.schema.*

class PipelineCloudFormationTemplate(defaultReponame: String) : CloudFormationTemplate() {


    val sourceBundleArtifactName = "sourceBundle"
    val buildArtifactName = "buildArtifact"

    // parameters

    val sourceBranchName = CloudFormationTemplate.Parameter(
            ParameterType.STRING,
            "source branch name"
    ).apply {
        default = "master"
    }

    //todo remove this parameter or adjust user of defaultRepoName
    val codeCommitRepoName = CloudFormationTemplate.Parameter(
            ParameterType.STRING,
            "repository name"
    ).apply {
        default = defaultReponame
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


    val codeRepository = AWS_CodeCommit_Repository(defaultReponame).apply {
    }

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
            resource += join(":", listOf("arn", "aws", "codecommit", refCurrentRegion(), refCurrentAccountId(), defaultReponame))
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
                            listOf("codebuild.amazonaws.com", "codepipeline.amazonaws.com", "codedeploy.amazonaws.com")
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
                location = join("", listOf("https://git-codecommit.", refCurrentRegion(), ".amazonaws.com/v1/repos/", defaultReponame))
            },
            AWS_CodeBuild_Project.Artifacts("S3").apply {
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
            resource += join(":", listOf("arn:aws:codecommit", refCurrentRegion(), refCurrentAccountId(), defaultReponame))
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

    val lambdaAppName = "appOf$defaultReponame"
    val lambdaDeploymentGroup = "deploymentGroupOf$defaultReponame"

    var deployApp = AWS_CodeDeploy_Application().apply {
        applicationName = lambdaAppName
        computePlatform = "Lambda"
    }

    //see https://stackoverflow.com/questions/52636182/ec2tagfilters-in-deployment-group-for-computeplatform-lambda
    var deployGroup = AWS_CodeDeploy_DeploymentGroup(lambdaAppName, ref(codePipelineServiceRole.arnAttribute())).apply {
        deploymentConfigName = "CodeDeployDefault.LambdaAllAtOnce"
        deploymentGroupName = lambdaDeploymentGroup
        deploymentStyle = AWS_CodeDeploy_DeploymentGroup.DeploymentStyle().apply {
            deploymentOption = "WITH_TRAFFIC_CONTROL"
            deploymentType = "BLUE_GREEN"
        }
    }

    val deployStage = AWS_CodePipeline_Pipeline.StageDeclaration(
            listOf(
                    AWS_CodePipeline_Pipeline.ActionDeclaration(
                            actionTypeId(
                                    CodePipelineActionTypeIdCategory.Deploy, CodePipelineActionTypeIdOwner.AWS,
                                    CodePipelineActionProvider.CodeDeploy
                            ),
                            "DeployAction"
                    ).apply {
                        inputArtifacts = listOf(AWS_CodePipeline_Pipeline.InputArtifact(buildArtifactName))
                        //outputArtifacts = listOf(AWS_CodePipeline_Pipeline.OutputArtifact(buildArtifactName))
                        configuration = CodeDeployActionConfiguration(lambdaAppName, lambdaDeploymentGroup)
                        runOrder = 1
                    }
            ),
            "Deploy"
    ).apply {
    }

    val artifactStoreImpl = AWS_CodePipeline_Pipeline.ArtifactStore(
            location = ref(artifactsBucket), type = "S3"
    )

    val lambdaDeployFunctionName = "lambdaDeploy$defaultReponame"

    //TODO put in a file
    //TODO code calls bacj pipleine
    val code = AWS_Lambda_Function.Code().apply {
        zipFile = inlineCode("""
var AWS = require('aws-sdk');
exports.handler = function(event, context) {
    var codepipeline = new AWS.CodePipeline();
    // Retrieve the Job ID from the Lambda action
    var jobId = event["CodePipeline.job"].id;
//var responseData = {Message: 'Hello'};

  // Retrieve the value of UserParameters from the Lambda action configuration in AWS CodePipeline, in this case a URL which will be
    // health checked by this function.
    var url = event["CodePipeline.job"].data.actionConfiguration.configuration.UserParameters;

    // Notify AWS CodePipeline of a successful job
    var putJobSuccess = function(message) {
        var params = {
            jobId: jobId
        };
        codepipeline.putJobSuccessResult(params, function(err, data) {
            if(err) {
                context.fail(err);
            } else {
                context.succeed(message);
            }
        });
    };

console.log("here");

putJobSuccess("Everything OK")
console.log("here2");

//context.succeed('hello my name is $lambdaDeployFunctionName was i supposed to deploy something?');
}""")
    }

    val lambdaAssumeRolePolicyDocument = IamPolicy().apply {
        statement {
            //TODO make this a constant
            action("sts:AssumeRole")
            effect = IamPolicy.EffectType.Allow
            //TODO make this a constant
            principal = mapOf(Pair(IamPolicy.PrincipalType.Service, listOf("lambda.amazonaws.com"))
            )
        }
/*  where do these go ?        statement {
            //TODO make this a constant
            action("codepipeline:PutJobSuccessResult")
            action("codepipeline:PutJobFailureResult")
            effect = IamPolicy.EffectType.Allow
            resource = listOf("*")
            //TODO make this a constant
            principal = mapOf(Pair(IamPolicy.PrincipalType.Service, listOf("lambda.amazonaws.com"))
            )
        }
*/
    }

    val lambdaPolicy = IamPolicy().apply {
       statement {
            //TODO make this a constant
            action("codepipeline:PutJobSuccessResult")
            action("codepipeline:PutJobFailureResult")
            effect = IamPolicy.EffectType.Allow
            resource = listOf("*")
        }
    }

    // https://docs.aws.amazon.com/codepipeline/latest/userguide/actions-invoke-lambda-function.html
    //TODO permission to communicate with pipeline
    val lambdaRole = AWS_IAM_Role(lambdaAssumeRolePolicyDocument).apply {
        //TODO make this a constantS
        policies = listOf(
                AWS_IAM_Role.Policy(lambdaPolicy, "lambdaPolicy")
        )
        managedPolicyArns = listOf("arn:aws:iam::aws:policy/AWSLambdaExecute")

    }

    val lambdaFunction = AWS_Lambda_Function(code, "index.handler",
            ref(lambdaRole.arnAttribute()), LambdaRuntime.NodeJs810.id).apply {
        functionName = lambdaDeployFunctionName
    }

    val lambdaDeployStage = AWS_CodePipeline_Pipeline.StageDeclaration(
            listOf(
                    AWS_CodePipeline_Pipeline.ActionDeclaration(
                            actionTypeId(
                                    CodePipelineActionTypeIdCategory.Invoke, CodePipelineActionTypeIdOwner.AWS,
                                    CodePipelineActionProvider.AWSLambda
                            ),
                            "LambdaDeployAction"
                    ).apply {
                        inputArtifacts = listOf(AWS_CodePipeline_Pipeline.InputArtifact(buildArtifactName))
                        //outputArtifacts = listOf(AWS_CodePipeline_Pipeline.OutputArtifact(buildArtifactName))
                        configuration = InvokeLambdaActionConfiguration(lambdaDeployFunctionName)
                        runOrder = 1
                    }

            ), "LambdaDeployStage"
    ).apply {}

    val pipeline = AWS_CodePipeline_Pipeline(
            ref(codePipelineServiceRole.arnAttribute()),
            listOf(sourceStage, buildStage, lambdaDeployStage /*deployStage*/)
    ).apply {
        this.artifactStore = artifactStoreImpl
    }
}
