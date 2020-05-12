package com.typedpath.awscloudformation.serverlessschema

import com.typedpath.awscloudformation.CloudFormationTemplate

open class ServerlessCloudformationTemplate() : CloudFormationTemplate(){

    override val transform = "AWS::Serverless-2016-10-31"

    private val serverlessRestApiId = "ServerlessRestApi"

    fun refServerlessRestApi(): String = refStatic(serverlessRestApiId)

}