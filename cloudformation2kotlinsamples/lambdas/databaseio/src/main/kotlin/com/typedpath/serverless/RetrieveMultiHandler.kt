package com.typedpath.serverless

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.typedpath.testdomain.ID_FIELD


class RetrieveMultiHandler : RequestHandler<Any, ApiGatewayResponse> {
    override fun handleRequest(input: Any, context: Context): ApiGatewayResponse {

        val typedInput = input as Map<String, Any?>
        val typedPathParameters = typedInput["pathParameters"]  as Map<String, Any?>
        val strRoot = typedPathParameters["type"] as String
        val rootClass = Class.forName(strRoot).kotlin
        val strIds = typedPathParameters["ids"] as String
        val strWhere = if (strIds.equals("*")) "" else  " where $ID_FIELD in (${strIds.split(",").map{""" "$it" """}.joinToString (",")})"
        val result = createAuroraStorer().loadMultiAsJsonDeep(rootClass, strWhere)

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
