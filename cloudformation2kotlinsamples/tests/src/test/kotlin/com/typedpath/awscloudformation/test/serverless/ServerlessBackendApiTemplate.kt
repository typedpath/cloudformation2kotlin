package com.typedpath.awscloudformation.test.serverless

import com.typedpath.awscloudformation.LambdaRuntime
import com.typedpath.awscloudformation.serverlessschema.AWS_Serverless_Function
import com.typedpath.awscloudformation.serverlessschema.AWS_Serverless_SimpleTable
import com.typedpath.awscloudformation.serverlessschema.ServerlessCloudformationTemplate

class ServerlessBackendApiTemplate(codeUriIn: String) : ServerlessCloudformationTemplate() {

    override var description: String? = "Simple CRUD webservice. State is stored in a SimpleTable (DynamoDB) resource."

    val table = AWS_Serverless_SimpleTable()

    val getFunction = AWS_Serverless_Function("index.get", LambdaRuntime.NodeJs810.id).apply {
        codeUri = codeUriIn
        policy(AWS_Serverless_Function.DynamoDBReadPolicy(table, this@ServerlessBackendApiTemplate))
        event("GetResource", AWS_Serverless_Function.ApiEvent("/resource/{resourceId}", "get"))
        environment("TABLE_NAME", this@ServerlessBackendApiTemplate.ref(table))
    }

    val putFunction = AWS_Serverless_Function("index.put", LambdaRuntime.NodeJs810.id).apply {
        codeUri = codeUriIn
        policy(AWS_Serverless_Function.DynamoDBCrudPolicy(table, this@ServerlessBackendApiTemplate))
        event("PutResource", AWS_Serverless_Function.ApiEvent("/resource/{resourceId}", "put"))
        environment("TABLE_NAME", this@ServerlessBackendApiTemplate.ref(table))
    }

    val deleteFunction = AWS_Serverless_Function("index.delete", LambdaRuntime.NodeJs810.id).apply {
        codeUri = codeUriIn
        policy(AWS_Serverless_Function.DynamoDBCrudPolicy(table, this@ServerlessBackendApiTemplate))
        event("DeleteResource", AWS_Serverless_Function.ApiEvent("/resource/{resourceId}", "delete"))
        environment("TABLE_NAME", this@ServerlessBackendApiTemplate.ref(table))
    }

    val apiUrlRef = sub("https://\${ServerlessRestApi}.execute-api.\${AWS::Region}.amazonaws.com/Prod/resource/")

    val ApiUrl = Output(apiUrlRef).apply {
        description = "API endpoint URL for Prod environment"
    }

}
