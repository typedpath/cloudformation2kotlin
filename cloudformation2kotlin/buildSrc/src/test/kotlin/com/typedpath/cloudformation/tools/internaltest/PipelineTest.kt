package com.typedpath.cloudformation.tools.internaltest

import com.typedpath.awscloudformation.*
import com.typedpath.awscloudformation.schema.sample.*

class PipelineTest {

  fun artifactsBucket() : AWS_S3_Bucket {
    return aWS_S3_Bucket {
      deletionPolicy = Resource.DeletionPolicyValue.Delete
    }
  }

  fun codeBuildPolicy(parent: CloudFormationTemplate, role: AWS_IAM_Role) : AWS_IAM_Policy {
    val policyDocument = iamPolicy() {
          statement {
            //TODO make these constants
            action += "logs:CreateLogGroup"
            action += "logs:CreateLogGroup"
            action += "logs:CreateLogStream"
            action += "logs:PutLogEvents"
            effect = IamPolicy.EffectType.Allow
            resource += "*"
          }
    }

    return aWS_IAM_Policy(policyDocument, "todojoin") {
      roles = listOf( parent.ref(role))
    }
  }

  fun codeBuildRole(parent: CloudFormationTemplate) : AWS_IAM_Role {
    val assumeRolePolicyDocument = iamPolicy {
      statement {
        action+="sts:AssumeRole"
        effect=IamPolicy.EffectType.Allow
        principal = mapOf(Pair(IamPolicy.PrincipalType.Service, listOf("codebuild.amazonaws.com")))
      }
    }
    return aWS_IAM_Role(assumeRolePolicyDocument) {
      path = "/"
      roleName = parent.join("-", listOf(parent.refCurrentStack(), "CodeBuild"))
    }
  }

  fun pipeline() {
    val template = cloudFormationTemplate {
      parameter("SourceBranchName", Parameter(ParameterType.STRING, "source branch name") {
          default="master"
      })
      parameter("CodeCommitRepoName", Parameter(ParameterType.STRING, "repository name") {
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
  (PipelineTest()).pipeline()
}