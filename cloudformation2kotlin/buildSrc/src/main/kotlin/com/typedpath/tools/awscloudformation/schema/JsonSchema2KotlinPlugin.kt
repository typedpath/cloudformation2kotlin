package com.typedpath.tools.awscloudformation.schema

import org.gradle.api.Plugin
import org.gradle.api.Project

import org.gradle.kotlin.dsl.*

class JsonSchema2KotlinPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = project.run {

        tasks {
            register("jsonschema2kotlin") {
                group = "cloudformation2kotlin"
                description = "Converts cloudformation json schema to json for  ${project.name}."

                doFirst {
                    transformDirectory("${projectDir}",
                        listOf("**/us-east-1/*.json"),
                        listOf("**/us-east-1/CloudFormationResourceSpecification.json",
//TODO fix mapping for these: MediaConnectFlowOutputSpecification
                            "**/us-east-1/MediaConnectFlowSpecification.json",
                            "**/us-east-1/MediaConnectFlowOutputSpecification.json",
                            "**/us-east-1/AmplifyUIBuilderComponentSpecification.json",
                            "**/us-east-1/MediaConnectBridgeSpecification.json",
                            "**/us-east-1/AppFlowFlowSpecification.json"
                            ),
                        "${buildDir.absolutePath}/generated/source/kotlin/",
                        "com.typedpath.awscloudformation.schema")
                    println("*******   dooing first ${this.javaClass.simpleName}")
                }
            }
        }
    }
}