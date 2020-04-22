package com.typedpath.awscloudformation.test.serverless.typesafebackendapi

import com.typedpath.awscloudformation.LambdaRuntime
import com.typedpath.awscloudformation.schema.AWS_RDS_DBCluster
import com.typedpath.awscloudformation.schema.AWS_SecretsManager_Secret
import com.typedpath.awscloudformation.serverlessschema.AWS_Serverless_Function
import com.typedpath.awscloudformation.serverlessschema.ServerlessCloudformationTemplate
import com.typedpath.iam2kotlin.IamPolicy
import com.typedpath.iam2kotlin.resources.secretsmanager.SecretsmanagerAction

class AuroraBackendApiTemplate(databaseNameIn: String, dbMasterUserName: String,
                               val codeUriIn: String, val functionNamePrefix: String, val schemaName: String)
 : ServerlessCloudformationTemplate() {

    val dbSecret = AWS_SecretsManager_Secret {
        name = "$databaseNameIn-AuroraUserSecret"
        description = "RDS database auto-generated user password"
        generateSecretString = AWS_SecretsManager_Secret.GenerateSecretString {
            secretStringTemplate = """{"username": "${dbMasterUserName}"}"""
            generateStringKey = "password"
            passwordLength = 30
            excludeCharacters = """"@/\"""
            tags = listOf(AWS_SecretsManager_Secret.Tag("test $databaseNameIn", "test"))
        }
    }

    // using default db subnet group
    val db = AWS_RDS_DBCluster("aurora") {
// use this for convenience / insecurity
//                masterUsername = dbMasterUserName
//                masterUserPassword = databaseNameIn
                masterUsername =  join ("", listOf("{{resolve:secretsmanager:", ref( dbSecret), ":SecretString:username}}"))
                masterUserPassword =  join ("", listOf("{{resolve:secretsmanager:", ref( dbSecret), ":SecretString:password}}"))
                databaseName = databaseNameIn
                engineMode = "serverless"
                engineVersion = "5.6.10a"
                scalingConfiguration = AWS_RDS_DBCluster.ScalingConfiguration {
                    autoPause = true
                    maxCapacity = 4
                    minCapacity = 1
                    secondsUntilAutoPause = 900 // 15 min
                }
                //dBSubnetGroupName:   Ref: DBSubnetGroup
            }

    val secretArn = Output(ref(dbSecret)) {
        description = "the secrets arn"
    }

    val databaseArnCalc = join(":", listOf("arn:aws:rds", refCurrentRegion(),
    refCurrentAccountId(), "cluster",  ref(db))  )

    val databaseArn = Output( databaseArnCalc ) {
        description = "the db arn"
    }

    val dbClusterIdentifier = Output( ref(db)) {
        description = "the dbClusterIdentifier"
    }

    /**
     * this is necessary because the SAM built in S3FullAccessPolicy doesnt allow tag access
     */
    val dbAccessPolicy = IamPolicy() {
        statement {
            effect = IamPolicy.EffectType.Allow
            action ( SecretsmanagerAction.GetSecretValue)
            resource  (  SecretsmanagerAction.GetSecretValue.secret_byRegionAccountSecret_name("*", "*", "$databaseNameIn*"))
        }
        statement {
            effect = IamPolicy.EffectType.Allow
            //TODO add rds-data to iam2kotlin
            action  (   IamPolicy.Action("rds-data:BatchExecuteStatement"))
            action  (  IamPolicy.Action("rds-data:BeginTransaction"))
            action  (  IamPolicy.Action("rds-data:CommitTransaction"))
            action  (  IamPolicy.Action("rds-data:ExecuteStatement"))
            action  (  IamPolicy.Action("rds-data:RollbackTransaction"))
            resource  (  IamPolicy.Resource.All)
        }
    }

    val saveFunction = function("save", "put", "/resource")
    val retrieveFunction = function("retrieve", "get", "/resource/{type}/{id}")
    val deleteFunction = function("delete", "delete", "/resource/{type}/{id}")
    val retrieveAllFunction = function("retrieveMulti", "get", "/resource/multi/{type}/{ids}")

    fun function(fType: String, httpMethod: String, path: String) : AWS_Serverless_Function {
        return AWS_Serverless_Function("com.typedpath.serverless.${fType.capitalize()}Handler", LambdaRuntime.Java8.id) {
                    description = "test $fType aurora"
                    codeUri= codeUriIn
                    functionName = getFunctionName(fType)
                    memorySize=1024
                    timeout =30
                    policy(dbAccessPolicy)
                    event("${fType.capitalize()}Resource", AWS_Serverless_Function.ApiEvent(path, httpMethod))
                    environment("secretsArn", ref(dbSecret))
                    environment("dbClusterArn", databaseArnCalc)
                    environment("schemaName", schemaName)
                }
    }

    private fun getFunctionName(fType: String) = "${functionNamePrefix}-$fType"

    val apiUrlRef = sub("https://\${ServerlessRestApi}.execute-api.\${AWS::Region}.amazonaws.com/Prod/resource/")

    val apiUrl = Output(apiUrlRef) {
        description = "API endpoint URL for Prod environment"
    }

}
