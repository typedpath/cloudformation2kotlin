package com.typedpath.awscloudformation.test.serverless

import com.typedpath.awscloudformation.LambdaRuntime
import com.typedpath.awscloudformation.serverlessschema.AWS_Serverless_Function
import com.typedpath.awscloudformation.serverlessschema.ServerlessCloudformationTemplate

class LambdaServerlessTemplate(codeUriIn: String, functionNameIn: String) : ServerlessCloudformationTemplate() {
    val HellowWorldFunction = AWS_Serverless_Function("index.handler", LambdaRuntime.NodeJs12.id)
        .apply {
            description = "A Hello World Test"
            codeUri= codeUriIn
            functionName = functionNameIn
    }

}