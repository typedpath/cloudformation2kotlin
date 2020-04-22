package com.typedpath.awscloudformation.test.serverless

import com.typedpath.awscloudformation.LambdaRuntime
import com.typedpath.awscloudformation.serverlessschema.AWS_Serverless_Function
import com.typedpath.awscloudformation.serverlessschema.AWS_Serverless_SimpleTable
import com.typedpath.awscloudformation.serverlessschema.ServerlessCloudformationTemplate
import com.typedpath.awscloudformation.toYaml

class ServerlessBackendApiRefactoredTemplate(val codeUriIn: String) : ServerlessCloudformationTemplate() {

    override var description: String? = "Simple CRUD webservice. State is stored in a SimpleTable (DynamoDB) resource."

    val table = AWS_Serverless_SimpleTable()

    val getFunction = function("get")

    val putFunction = function("put")

    val deleteFunction = function("delete")

    private fun function(method: String) : AWS_Serverless_Function =
        AWS_Serverless_Function("index.${method.toLowerCase()}", LambdaRuntime.NodeJs12.id) {
            codeUri = codeUriIn
            policy(AWS_Serverless_Function.DynamoDBCrudPolicy(table, this@ServerlessBackendApiRefactoredTemplate))
            event("${method.toLowerCase().capitalize()}Resource", AWS_Serverless_Function.ApiEvent("/resource/{resourceId}", method.toLowerCase()))
            environment("TABLE_NAME", this@ServerlessBackendApiRefactoredTemplate.ref(table))
    }

    val apiUrlRef = sub("https://\${ServerlessRestApi}.execute-api.\${AWS::Region}.amazonaws.com/Prod/resource/")

    val ApiUrl = Output(apiUrlRef) {
        description = "API endpoint URL for Prod environment"
    }
}

fun main(args: Array<String>) {
    println(toYaml(ServerlessBackendApiRefactoredTemplate("s3://bucketName/codePackageName")))
}
