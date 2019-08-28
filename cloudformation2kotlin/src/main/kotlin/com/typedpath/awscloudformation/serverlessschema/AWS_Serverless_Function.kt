package com.typedpath.awscloudformation.serverlessschema

import com.typedpath.awscloudformation.CloudFormationTemplate

//TODO ref sam_resources.py
// https://github.com/softprops/typed-lambda/tree/master/events
//https://github.com/awslabs/serverless-application-model/blob/master/versions/2016-10-31.md#resource-types
class AWS_Serverless_Function(  val handler: String,
                                val runtime: String): ServerlessResource () {
    override fun getResourceType() = "AWS::Serverless::Function"
    var functionName : String? = null
    // Properties:
    var codeUri: String? = null
    var inlineCode: String? = null
    var description: String? = null
    var memorySize: Int? = null
    var timeout: Int? = null
    var role: String? = null
    var policies =  HashMap<String, Any>()
    var environment: Environment? = null

    fun environment(key: String, value: String) {
        if (environment==null) {
            environment = Environment()
        }
        environment!!.variables.put(key, value)
    }

    fun policy(builtInPolicyName: String) {

    }

    fun policy(serverlessPolicy: ServerlessPolicy) {
        policies.put(serverlessPolicy.javaClass.simpleName, serverlessPolicy)
    }

    abstract class ServerlessPolicy
    class DynamoDBReadPolicy(tableIn: AWS_Serverless_SimpleTable, parent: CloudFormationTemplate) : ServerlessPolicy() {
         val tableName = parent.ref(tableIn)
    }
    class DynamoDBCrudPolicy(tableIn: AWS_Serverless_SimpleTable, parent: CloudFormationTemplate) : ServerlessPolicy() {
        val tableName = parent.ref(tableIn)
    }


    abstract class ServerlessEvent : ServerlessResource()

    //https://github.com/awslabs/serverless-application-model/blob/master/versions/2016-10-31.md#api
    class ApiEvent(val path: String, val method: String) : ServerlessEvent() {
        override fun getResourceType() = "Api"
    }

    val events = HashMap<String, ServerlessEvent>()

    fun event(key: String, event: ServerlessEvent) = events.put(key, event)

    fun arnAttribute() = Attribute(this, "Arn");

    class Environment() {
        fun getResourceType() = "AWS::Lambda::Function.Environment"
        // Properties:
        var variables: MutableMap<String, String> = mutableMapOf()
    }


}