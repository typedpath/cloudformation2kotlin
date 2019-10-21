package com.typedpath.serverless

import com.typedpath.aurora.AuroraStorer

fun createAuroraStorer() : AuroraStorer{
    val secretsArn = System.getenv("secretsArn")
    val dbClusterArn = System.getenv("dbClusterArn")
    val schemaName =  System.getenv("schemaName")
    //todo record insert count
    return AuroraStorer(dbClusterArn, secretsArn, schemaName)
}
