package com.typedpath.awscloudformation.test.pipeline

import com.typedpath.awscloudformation.CloudFormationTemplate
import com.typedpath.awscloudformation.LambdaRuntime
import com.typedpath.awscloudformation.schema.AWS_IAM_Role
import com.typedpath.awscloudformation.schema.AWS_Lambda_Function
import com.typedpath.iam2kotlin.IamPolicy
import com.typedpath.iam2kotlin.resources.sts.StsAction

class JavaLambdaTemplate(val functionNameIn: String, val codeBucket: String, val codeBucketKey: String, val handler: String ) : CloudFormationTemplate() {


    val code = AWS_Lambda_Function.Code {
        this.s3Bucket=codeBucket
        this.s3Key = codeBucketKey
    }

    val assumeRolePolicyDocument = IamPolicy {
        statement {
            //TODO make this a constant
            action(StsAction.AssumeRole)
            effect = IamPolicy.EffectType.Allow
            //TODO make this a constant
            principal = mutableMapOf( Pair( IamPolicy.PrincipalType.Service, listOf("lambda.amazonaws.com"))
            )
        }
    }

    val lambdaRole = AWS_IAM_Role(assumeRolePolicyDocument) {
        //TODO make this a constantS
        managedPolicyArns = listOf("arn:aws:iam::aws:policy/AWSLambdaExecute")
    }

    val lambdaFunction = AWS_Lambda_Function(code, handler,
            ref(lambdaRole.arnAttribute()), LambdaRuntime.Java8.id) {
        functionName = functionNameIn
    }
}
