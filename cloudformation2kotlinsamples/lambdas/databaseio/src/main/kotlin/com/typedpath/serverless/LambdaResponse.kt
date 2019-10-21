package com.typedpath.serverless

data class LambdaResponse(val message: String, val input: Any) : Response()
