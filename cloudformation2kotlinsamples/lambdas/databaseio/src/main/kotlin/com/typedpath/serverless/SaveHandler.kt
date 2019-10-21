package com.typedpath.serverless

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler


class SaveHandler : RequestHandler<Any, ApiGatewayResponse> {
    override fun handleRequest(input: Any, context: Context): ApiGatewayResponse {

        val typedInput = input as Map<String, Any?>
        println("body: ${typedInput["body"]}")
        val strJson = typedInput["body"] as String
        println("***received body $strJson")
        //todo record insert count
        createAuroraStorer().saveJson(strJson)

        return ApiGatewayResponse.build {
            statusCode = 200
            objectBody = LambdaResponse("xxxGo Go Serverless v1.x! Your Kotlin function executed successfully!", input)
            headers = mapOf("X-Powered-By" to "AWS Lambda & serverless")
        }
    }

    companion object {
        //private val LOG = LogManager.getLogger(Handler::class.java)
    }
}
