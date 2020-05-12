package com.typedpath.awscloudformation.serverlessschema

import com.typedpath.awscloudformation.CloudFormationTemplate
import com.typedpath.iam2kotlin.IamPolicy
import com.typedpath.awscloudformation.schema.AWS_S3_Bucket

//TODO ref sam_resources.py
// https://github.com/softprops/typed-lambda/tree/master/events
//https://github.com/awslabs/serverless-application-model/blob/master/versions/2016-10-31.md#resource-types
//this is open because its not complete - etend as required
open class AWS_Serverless_Api(initIn: AWS_Serverless_Api.() -> Unit = {}): ServerlessResource () {
    override fun getResourceType_() = "AWS::Serverless::Api"
    var stageName : String? = null
    var cors: Any? = null
    // Properties:
    open class Cors (initIn: Cors.() -> Unit = {}) {
        var allowMethods: String? = null // e.g. "POST, GET"
        var allowHeaders: String? = null// e.g.  "X-Forwarded-For"
        var allowOrigin: String? = null //e.g. "www.example.com"
        var maxAge: String? = null // e.g.  "600"
        var allowCredentials: Boolean? = null // e.g.  True
        init { initIn ()}
    }
    open class Auth(initIn: Auth.() -> Unit = {}) {
        var defaultAuthorizer: String? = null
        open class Authorizer(val userPoolArn: String, initIn: Authorizer.() -> Unit = {}) {
            var authType: String? = null // e.g. "COGNITO_USER_POOLS"
            var header: String? = null
            var validationExpression: String? = null
            var invokeRole: String? = null //  CALLER_CREDENTIALS, NONE, IAM Role Arn
            init {initIn()}
        }
        var addDefaultAuthorizerToCorsPreflight: Boolean? = null

        var authorizers : Map<String, Authorizer>? = null
        init {initIn()}
    }
    var auth: Auth? =null


    init { initIn ()}

}