package com.typedpath.tools.aws

class S3BucketExample {
    //get attribute
    var Arn: String;
    constructor(Arn: String) {this.Arn=Arn}
}


fun main (args: Array<String>) {
    val s3Bucket = S3BucketExample("an arn")
    println(s3Bucket)
}