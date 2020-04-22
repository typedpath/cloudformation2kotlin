package com.typedpath.awscloudformation.test.withoutextension

import com.typedpath.awscloudformation.*
import com.typedpath.awscloudformation.schema.*
import com.typedpath.iam2kotlin.IamPolicy
import com.typedpath.iam2kotlin.resources.logs.LogsAction
import com.typedpath.iam2kotlin.resources.sts.StsAction

class PipelineWithoutExtensionTest {

  fun artifactsBucket() : AWS_S3_Bucket {
    return AWS_S3_Bucket {
      deletionPolicy = Resource.DeletionPolicyValue.Delete
    }
  }

  fun codeBuildPolicy(parent: CloudFormationTemplate, role: AWS_IAM_Role) : AWS_IAM_Policy {
    val policyDocument = IamPolicy {
          statement {
            //TODO make these constants
            action  (  LogsAction.CreateLogGroup)
            action  (  LogsAction.CreateLogStream)
            action  (  LogsAction.PutLogEvents)
            effect = IamPolicy.EffectType.Allow
            resource  (  IamPolicy.Resource("*"))
          }
    }

    return AWS_IAM_Policy(policyDocument, "todojoin") {
      roles = listOf( parent.ref(role))
    }
  }

  fun codeBuildRole(parent: CloudFormationTemplate) : AWS_IAM_Role {
    val assumeRolePolicyDocument = IamPolicy {
      statement {
        action  (  StsAction.AssumeRole)
        effect=IamPolicy.EffectType.Allow
        principal = mutableMapOf(Pair(IamPolicy.PrincipalType.Service, listOf("codebuild.amazonaws.com")))
      }
    }
    return AWS_IAM_Role(assumeRolePolicyDocument) {
      path = "/"
      roleName = parent.join("-", listOf(parent.refCurrentStack(), "CodeBuild"))
    }
  }

  fun pipeline() {
    val template = CloudFormationTemplate {
      parameter("SourceBranchName", CloudFormationTemplate.Parameter(ParameterType.STRING, "source branch name") {
        default = "master"
      })
      parameter("CodeCommitRepoName", CloudFormationTemplate.Parameter(ParameterType.STRING, "repository name") {
        default="mymind"
      })
      val codeBuildRole = codeBuildRole(this)
      resource("PipelineBucket", AWS_S3_Bucket())
      resource("ArtifactsBucket", artifactsBucket())
      resource("CodeBuildRole", codeBuildRole)
      resource("CodeBuildPolicy", codeBuildPolicy(this, codeBuildRole))
    }
    println(  toYaml(template))
  }
}

fun main (args: Array<String>) {
  (PipelineWithoutExtensionTest()).pipeline()
}