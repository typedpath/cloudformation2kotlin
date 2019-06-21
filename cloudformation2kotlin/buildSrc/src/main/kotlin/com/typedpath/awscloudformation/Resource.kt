package com.typedpath.awscloudformation

abstract class Resource {
    abstract fun getResourceType() : String

    enum class DeletionPolicyValue {Delete}

    var  deletionPolicy:DeletionPolicyValue?=null

    //TODO this field should be available in AWS::IAM::Policy !
    //var description: String? = null

    class Attribute (val parent: Resource, val name: String)

    public fun testAttribute() = Attribute(this, "test")

}