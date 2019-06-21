package com.typedpath.tools.awscloudformation.schema.sample

import com.typedpath.awscloudformation.IamPolicy
import com.typedpath.awscloudformation.cloudFormationTemplate
import com.typedpath.awscloudformation.iamPolicy
import com.typedpath.awscloudformation.schema.sample.aWS_IAM_ManagedPolicy
import com.typedpath.awscloudformation.toYaml
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.full.memberProperties


fun main(args: Array<String>) {

    IamPolicy.StatementEntry::class.java.kotlin.memberProperties
    println("${IamPolicy.StatementEntry::class.java.kotlin}")

    val iamPolicy: IamPolicy = iamPolicy {
        statement {
            effect = IamPolicy.EffectType.Allow
            action("*")
            resource += "*"

        }
    }

    val policyName = """aaaTestPolicy${SimpleDateFormat("yyMMdd_HHmmss").format(Date())}"""

    val policy = aWS_IAM_ManagedPolicy(iamPolicy) {
        //arn:aws:iam::950651224730:user/myuser
        users = listOf("myuser")
        description = "a test policy"
        managedPolicyName = policyName
    }

    val template = cloudFormationTemplate {
        resource("policy1", policy)
//        Users
    }

    println(
        """
         type: AWS::IAM::Policy
            Properties:
              PolicyName: "root"
              PolicyDocument:
                Version: "2012-10-17"
                Statement:
                  -
                    effect: "Allow"
                    action: "*"
                    resource: "*"
              Roles:
                -
                 Ref: "RootRole"
    """.trimIndent()
    )

    println(toYaml(template))
}