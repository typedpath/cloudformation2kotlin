package com.typedpath.awscloudformation

/* https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies.html */
class IamPolicy() {

    var Version = "2012-10-17"
    //TODO resolve why these stop cloudformation working !
    var Id: String? = null
    var Sid: String? = null

    //effect
    enum class EffectType {Allow, Deny}


    var Statement:MutableList<StatementEntry> = mutableListOf()

    fun statement(init: StatementEntry.() -> Unit) {
        val statement = StatementEntry()
        statement.apply(init)
        Statement.add(statement)
    }

    class StatementEntry {
        var effect: EffectType?=null
        //TODO constant star action
        //"action": [ "sqs:SendMessage", "sqs:ReceiveMessage", "ec2:StartInstances", "iam:ChangePassword", "s3:GetObject" ]
        var action : List<String> = mutableListOf()
        var noAction : List<String> = mutableListOf()
        //resource
        var Condition: List<ConditionMapEntry>? = mutableListOf()

        var resource: List<String> = mutableListOf()
        var notResource: String?=null

        //TODO constant star principal block
        //var principal: List<PrincipalMapEntry>? = null
        var principal: Map<PrincipalType, List<String>>?=null
        var notPrincipal: List<PrincipalMapEntry>? = null

        fun action(action: String) {
            this.action = this.action + action
        }


    }

    enum class PrincipalType() {
        AWS, Federated, Service ;}

    class PrincipalMapEntry(val Type: PrincipalType, val PrincipalIds: List<String>) {
    }

    class ConditionMapEntry(val type: String, val values: List<ConditionValuePair>) {}
    class ConditionValuePair(val key: String, val values:Any) {}


//    class Conditio

  //  <condition_type_string> : { <condition_key_string> : <condition_value_list> },

   /* "Condition": {
        "DateGreaterThan": {"aws:CurrentTime": "2017-07-01T00:00:00Z"},
        "DateLessThan": {"aws:CurrentTime": "2017-12-31T23:59:59Z"}
    }
     "Condition": {
                "StringEquals": {"account:TargetRegion": "ap-east-1"}
            }
    "Condition" :  {
      "DateGreaterThan" : {
         "aws:CurrentTime" : "2013-08-16T12:00:00Z"
       },
      "DateLessThan": {
         "aws:CurrentTime" : "2013-08-16T15:00:00Z"
       },
       "IpAddress" : {
          "aws:SourceIp" : ["192.0.2.0/24", "203.0.113.0/24"]
      }
}

    */

}

fun iamPolicy(init: IamPolicy.() -> Unit): IamPolicy = IamPolicy().apply(init)

/**
<resource_block>,
<condition_block?>
}

<effect_block> = "effect" : ("Allow" | "Deny")

<principal_block> = ("principal" | "notPrincipal") : ("*" | <principal_map>)

<principal_map> = { <principal_map_entry>, <principal_map_entry>, ... }

<principal_map_entry> = ("AWS" | "Federated" | "Service") :
[<principal_id_string>, <principal_id_string>, ...]

"principal": {
"AWS": [
"arn:aws:iam::123456789012:root",
"999999999999"
]
}

<action_block> = ("action" | "NotAction") :
("*" | [<action_string>, <action_string>, ...])

<resource_block> = ("resource" | "notResource") :
("*" | [<resource_string>, <resource_string>, ...])

<condition_block> = "Condition" : { <condition_map> }
<condition_map> = {
<condition_type_string> : { <condition_key_string> : <condition_value_list> },
<condition_type_string> : { <condition_key_string> : <condition_value_list> }, ...
}
<condition_value_list> = [<condition_value>, <condition_value>, ...]
<condition_value> = ("string" | "number" | "Boolean")
 */