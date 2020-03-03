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
                        listOf("**/us-east1/*.json"),
                        listOf("**/us-east1/CloudFormationResourceSpecification.json"),
                        "${buildDir.absolutePath}/generated/source/kotlin/",
                        "com.typedpath.awscloudformation.schema")
                    println("*******   dooing first ${this.javaClass.simpleName}")
                }
            }
        }
    }
}