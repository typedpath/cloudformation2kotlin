package com.typedpath.serverless

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.typedpath.aurora.AuroraStorer
import com.typedpath.testdomain.Person
import java.util.*


private fun store(secretArn: String, dbClusterArn: String,
                  schemaName: String, strJson: String) {
    AuroraStorer(secretArn, dbClusterArn, schemaName).saveJson(strJson)
}


class DeleteHandler : RequestHandler<Any, ApiGatewayResponse> {
    override fun handleRequest(input: Any, context: Context): ApiGatewayResponse {

        val typedInput = input as Map<String, Any?>
//        println("typedInput: ${typedInput}")
        val pathParameters = typedInput["pathParameters"]
        println("***received pathParameters $pathParameters")
        val typedPathParameters = pathParameters as Map<String, Any?>
        val strRoot = typedPathParameters["type"] as String
        val strId = typedPathParameters["id"] as String
        val rootClass = Class.forName(strRoot).kotlin
        val id = UUID.fromString(strId)

        val result = createAuroraStorer().delete(rootClass, id)

        return ApiGatewayResponse.build {
            statusCode = 200
            objectBody = result
            headers = mapOf("X-Powered-By" to "AWS Lambda & serverless")
        }
    }

    companion object {
        //private val LOG = LogManager.getLogger(Handler::class.java)
    }
}
