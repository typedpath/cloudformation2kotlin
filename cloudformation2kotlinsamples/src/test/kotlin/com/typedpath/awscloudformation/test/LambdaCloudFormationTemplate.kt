package com.typedpath.awscloudformation.test

import com.typedpath.awscloudformation.CloudFormationTemplate
import com.typedpath.awscloudformation.IamPolicy
import com.typedpath.awscloudformation.LambdaRuntime
import com.typedpath.awscloudformation.schema.AWS_IAM_Role
import com.typedpath.awscloudformation.schema.AWS_Lambda_Function
import com.typedpath.awscloudformation.toYaml

class LambdaCloudFormationTemplate(functionName: String, jsCode: String) : CloudFormationTemplate() {


    val code = AWS_Lambda_Function.Code().apply {
        zipFile = inlineJsCode(jsCode)
    }

    val assumeRolePolicyDocument = IamPolicy().apply {
        statement {
            //TODO make this a constant
            action("sts:AssumeRole")
            effect = IamPolicy.EffectType.Allow
            //TODO make this a constant
            principal = mapOf( Pair( IamPolicy.PrincipalType.Service, listOf("lambda.amazonaws.com"))
            )
        }
    }

    val lambdaRole = AWS_IAM_Role(assumeRolePolicyDocument).apply {
        //TODO make this a constantS
        managedPolicyArns = listOf("arn:aws:iam::aws:policy/AWSLambdaExecute")
    }

    val lambdaFunction = AWS_Lambda_Function(code, "index.handler",
            ref(lambdaRole.arnAttribute()), LambdaRuntime.NodeJs810.id).apply {
        this.functionName = functionName
    }
}
