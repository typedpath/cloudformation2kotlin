package com.typedpath.awscloudformation.test

import com.typedpath.awscloudformation.IamPolicy
import com.typedpath.awscloudformation.cloudFormationTemplate
import com.typedpath.awscloudformation.iamPolicy
import com.typedpath.awscloudformation.schema.AWS_IAM_Policy
import com.typedpath.awscloudformation.toYaml
import org.junit.Test

class IamPolicyTest {
    //https://github.com/widdix/aws-cf-templates/blob/master/state/s3.yaml

    @Test
    fun testS3() {
        val iamPolicy :IamPolicy = iamPolicy {
           statement{
               effect = IamPolicy.EffectType.Allow
               action +="*"
               resource += "*"
           }
        }

        val policy= AWS_IAM_Policy(iamPolicy, "root")

        val template = cloudFormationTemplate {
            resource("policy1", policy)
        }

        println(toYaml(template))
    }
/*
https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-iam-policy.html#aws-resource-iam-policy--examples
  {
              "Type": "AWS::IAM::Policy",
              "Properties": {
              "PolicyName": "root",
              "PolicyDocument": {
                "Version" : "2012-10-17",
                "Statement": [
                   { "Effect": "Allow", "Action": "*", "Resource": "*" }
                 ]
               },
               "Roles": [ { "Ref": "RootRole" } ]
             }
            }
 */



}