package com.typedpath.serverless

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.typedpath.aurora.AuroraStorer
import com.typedpath.testdomain.Person
import java.util.*


class RetrieveMultiHandler : RequestHandler<Any, ApiGatewayResponse> {
    override fun handleRequest(input: Any, context: Context): ApiGatewayResponse {

        val typedInput = input as Map<String, Any?>
        val typedPathParameters = typedInput["pathParameters"]  as Map<String, Any?>
        val strRoot = typedPathParameters["type"] as String
        val rootClass = Class.forName(strRoot).kotlin
        val result = createAuroraStorer().loadMultiAsJsonDeep(rootClass)

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
