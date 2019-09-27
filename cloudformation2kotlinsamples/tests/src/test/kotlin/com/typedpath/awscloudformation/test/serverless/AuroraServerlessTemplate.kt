package com.typedpath.awscloudformation.test.serverless

import com.typedpath.awscloudformation.schema.AWS_RDS_DBCluster
import com.typedpath.awscloudformation.schema.AWS_SecretsManager_Secret
import com.typedpath.awscloudformation.serverlessschema.ServerlessCloudformationTemplate

class AuroraServerlessTemplate(databaseNameIn: String, dbMasterUserName: String) : ServerlessCloudformationTemplate() {
    val dbSecret = AWS_SecretsManager_Secret().apply {
        name = "$databaseNameIn-AuroraUserSecret"
        description = "RDS database auto-generated user password"
        generateSecretString = AWS_SecretsManager_Secret.GenerateSecretString().apply {
            secretStringTemplate = """{"username": "${dbMasterUserName}"}"""
            generateStringKey = "password"
            passwordLength = 30
            excludeCharacters = """"@/\"""
            tags = listOf(AWS_SecretsManager_Secret.Tag("test $databaseNameIn", "test"))
        }
    }


    // using default db subnet group
    val db = AWS_RDS_DBCluster("aurora")
            .apply {
// use this for conveience / insecurity
//                masterUsername = dbMasterUserName
//                masterUserPassword = databaseNameIn
                masterUsername =  join ("", listOf("{{resolve:secretsmanager:", ref( dbSecret), ":SecretString:username}}"))
                masterUserPassword =  join ("", listOf("{{resolve:secretsmanager:", ref( dbSecret), ":SecretString:password}}"))
                databaseName = databaseNameIn
                engineMode = "serverless"
                engineVersion = "5.6.10a"
                scalingConfiguration = AWS_RDS_DBCluster.ScalingConfiguration().apply {
                    autoPause = true
                    maxCapacity = 4
                    minCapacity = 1
                    secondsUntilAutoPause = 900 // 15 min
                }
                //dBSubnetGroupName:   Ref: DBSubnetGroup
            }

    val secretArn = Output(ref(dbSecret)).apply {
        description = "the secrets arn"
    }

    val databaseArn = Output( join(":", listOf("arn:aws:rds", refCurrentRegion(),
            refCurrentAccountId(), "cluster",  ref(db))  )).apply {
        description = "the db arn"
    }

    val dbClusterIdentifier = Output( ref(db)).apply {
        description = "the dbClusterIdentifier"
    }


}
