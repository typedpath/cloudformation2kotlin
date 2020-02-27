package com.typedpath.awscloudformation

enum class LambdaRuntime(val id: String) {
    NodeJs12("nodejs12.x"),
    NodeJs10("nodejs10.x"),
   NodeJs810("nodejs8.10"),
    Python2_7("python2.7"),
    Python3_6("python3.6"),
    Python3_7("python3.7"),
    Java8("java8")

}