package com.typedpath.awscloudformation.test

import com.typedpath.awscloudformation.CloudFormationTemplate

interface TemplateFactory {
    fun createTemplate() :  CloudFormationTemplate
}