package com.typedpath.awscloudformation.serverlessschema

import com.typedpath.awscloudformation.CloudFormationTemplate
import com.typedpath.iam2kotlin.IamPolicy
import com.typedpath.awscloudformation.schema.AWS_S3_Bucket

//TODO ref sam_resources.py
// https://github.com/softprops/typed-lambda/tree/master/events
//https://github.com/awslabs/serverless-application-model/blob/master/versions/2016-10-31.md#resource-types
class AWS_Serverless_Function(  val handler: String,
                                val runtime: String, initIn: AWS_Serverless_Function.() -> Unit = {}): ServerlessResource () {
    override fun getResourceType_() = "AWS::Serverless::Function"
    var functionName : String? = null
    // Properties:
    var codeUri: String? = null
    var inlineCode: String? = null
    var description: String? = null
    var memorySize: Int? = null
    var timeout: Int? = null
    var role: String? = null
    var policies : Any? = null
    var environment: Environment? = null

    fun environment(key: String, value: String) {
        if (environment==null) {
            environment = Environment()
        }
        environment!!.variables.put(key, value)
    }

    fun policy(value: IamPolicy) {
         policies = value
    }

    fun policy(value: List<IamPolicy>) {
        policies = value
    }

    fun policy(serverlessPolicy: ServerlessPolicy) {
        if (policies == null) {
            policies = HashMap<String, ServerlessPolicy>()
        }
        (policies as HashMap<String, ServerlessPolicy>).put(serverlessPolicy.javaClass.simpleName, serverlessPolicy)
    }

    //full list here https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-policy-templates.html
    abstract class ServerlessPolicy
    class DynamoDBReadPolicy(tableIn: AWS_Serverless_SimpleTable, parent: CloudFormationTemplate) : ServerlessPolicy() {
         val tableName = parent.ref(tableIn)
    }
    class DynamoDBCrudPolicy(tableIn: AWS_Serverless_SimpleTable, parent: CloudFormationTemplate) : ServerlessPolicy() {
        val tableName = parent.ref(tableIn)
    }
    class S3CrudPolicy(bucketNameIn: String,  parent: CloudFormationTemplate) : ServerlessPolicy() {
        val bucketName = parent.sub(bucketNameIn)
    }
    class S3FullAccessPolicy(bucketNameIn: String,  parent: CloudFormationTemplate) : ServerlessPolicy() {
        val bucketName = parent.sub(bucketNameIn)
    }

    abstract class ServerlessEvent : ServerlessResource()

    //https://github.com/awslabs/serverless-application-model/blob/master/versions/2016-10-31.md#api
    class ApiEvent(val path: String, val method: String) : ServerlessEvent() {
        override fun getResourceType_() = "Api"
    }

    class S3Event(bucketIn: AWS_S3_Bucket, parent: CloudFormationTemplate) : ServerlessEvent() {
        override fun getResourceType_() = "S3"
        val bucket = parent.ref(bucketIn)
        val events : MutableList<String> = mutableListOf()
        fun event(strEvent: String) = events.add(strEvent)
    }

    val events = HashMap<String, ServerlessEvent>()

    fun event(key: String, event: ServerlessEvent) = events.put(key, event)

    fun arnAttribute() = Attribute(this, "Arn");

    class Environment() {
        fun getResourceType_() = "AWS::Lambda::Function.Environment"
        // Properties:
        var variables: MutableMap<String, String> = mutableMapOf()
    }

    init { initIn ()}

}