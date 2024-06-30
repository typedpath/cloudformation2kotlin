package com.typedpath.awscloudformation.test.appsync

import com.typedpath.awscloudformation.CloudFormationTemplate
import com.typedpath.awscloudformation.schema.AWS_AppSync_DataSource
import com.typedpath.awscloudformation.schema.AWS_AppSync_GraphQLApi

//based on https://gist.github.com/adrianhall/50e9fdf08e7a7e52d3ab0f01467b72f7
class AppsyncCloudFormationTemplate() : CloudFormationTemplate() {

    override var description: String? = "AWSAppSync DynamoDB Example"

    val appSyncDynamoDbExample = AWS_AppSync_GraphQLApi(
            name = "AWSAppSync DynamoDB Example",
            authenticationType = "AWS_IAM"
    )

    val arn = Output(ref(appSyncDynamoDbExample.arnAttribute())) {
        description = "the secrets arn"
    }

    val postDynamoDBTableDataSource = AWS_AppSync_DataSource(
            type = "AMAZON_DYNAMODB",
            apiId = ref (appSyncDynamoDbExample.apiIdAttribute()),
            name = "PostDynamoDBTable"
    ) {
        serviceRoleArn =  join("",  listOf("arn:aws:iam::", refCurrentAccountId(), ":role/AppSyncTutorialAmazonDynamoDBRole"))
        dynamoDBConfig = AWS_AppSync_DataSource.DynamoDBConfig(awsRegion= refCurrentRegion(), tableName = "AppSyncTutorial-Post")
    }

}