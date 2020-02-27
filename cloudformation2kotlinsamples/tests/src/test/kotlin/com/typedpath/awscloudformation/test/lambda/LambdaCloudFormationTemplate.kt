package com.typedpath.awscloudformation.test.lambda

import com.typedpath.awscloudformation.CloudFormationTemplate
import com.typedpath.awscloudformation.IamPolicy
import com.typedpath.awscloudformation.LambdaRuntime
import com.typedpath.awscloudformation.schema.AWS_IAM_Role
import com.typedpath.awscloudformation.schema.AWS_Lambda_Function

open class LambdaCloudFormationTemplate(val functionNameIn: String, val strCode: String,
                                   val runtime: LambdaRuntime = LambdaRuntime.NodeJs12, val handler: String = "index.handler") : CloudFormationTemplate() {


    val code = AWS_Lambda_Function.Code().apply {
        val comment = if (runtime.id.toLowerCase().contains("python")) "#" else "//"
        zipFile = inlineCode(strCode, comment)
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

    val lambdaFunction = AWS_Lambda_Function(code, handler,
            ref(lambdaRole.arnAttribute()), runtime.id).apply {
        functionName = functionNameIn
    }
}
