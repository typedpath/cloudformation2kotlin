package com.typedpath.awscloudformation.test

import com.typedpath.awscloudformation.LambdaRuntime
import com.typedpath.awscloudformation.serverlessschema.AWS_Serverless_Function
import com.typedpath.awscloudformation.serverlessschema.AWS_Serverless_SimpleTable
import com.typedpath.awscloudformation.serverlessschema.ServerlessCloudformationTemplate
import com.typedpath.awscloudformation.toYaml
import org.junit.Test

class ServerlessTest {
    @Test
    fun basic() {
        val strTemplate = toYaml(BasicTemplate())
        println(strTemplate)
    }

    class BasicTemplate() : ServerlessCloudformationTemplate() {
        val HellowWorldFunction = AWS_Serverless_Function("example.Hello", LambdaRuntime.Java8.id )
            .apply {
                description = "A Hello World Test"
                codeUri= "src/"
        }

    }

    @Test
    fun backendApi() {
         println(toYaml(BackendApiTemplate()))
    }

    class BackendApiTemplate() : ServerlessCloudformationTemplate() {

        override var description : String? = "Simple CRUD webservice. State is stored in a SimpleTable (DynamvarioDB) resource."

        val table = AWS_Serverless_SimpleTable()

        val getFunction = AWS_Serverless_Function("index.get", LambdaRuntime.NodeJs810.id).apply {
             codeUri="src/"
             policy(AWS_Serverless_Function.DynamoDBReadPolicy(table, this@BackendApiTemplate))
             event("GetResource", AWS_Serverless_Function.ApiEvent("/resource/{resourceId}","get"))
            timeout = 21
        }

        val putFunction = AWS_Serverless_Function("index.put", LambdaRuntime.NodeJs810.id).apply {
            codeUri="src/"
            policy(AWS_Serverless_Function.DynamoDBCrudPolicy(table, this@BackendApiTemplate))
            event("PutResource", AWS_Serverless_Function.ApiEvent("/resource/{resourceId}","put"))
            environment("TABLE_NAME", this@BackendApiTemplate.ref(table))
            timeout = 20
        }

        val apiUrlRef = sub("https://\${ServerlessRestApi}.execute-api.\${AWS::Region}.amazonaws.com/Prod/resource/")

        val ApiUrl = Output( apiUrlRef).apply {
            description = "API endpoint URL for Prod environment"
        }


    }


}