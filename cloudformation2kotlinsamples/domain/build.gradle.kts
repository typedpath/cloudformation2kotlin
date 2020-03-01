import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.11"
    maven
}

group  = "com.typedpath"
version  = "1.0.0"

val sourceCompatibility = 1.8

apply {
     plugin("idea")
     plugin("kotlin")
}

repositories {
    maven {
          url = uri("https://repositorybuckettest65-s3hostingbucket-qpezmj5xooak.s3.amazonaws.com/repository")
    }
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testCompile("junit:junit:4.12")
    compile( "org.jetbrains.kotlin:kotlin-reflect")
    compile("com.amazonaws:aws-java-sdk-rdsdata:1.11.574")
    compile("com.amazonaws:aws-java-sdk-rds:1.11.574")
}

val compileKotlin by tasks.getting(KotlinCompile::class) {
    kotlinOptions.jvmTarget = "1.8"
}

val compileTestKotlin by tasks.getting(KotlinCompile::class) {
    kotlinOptions.jvmTarget = "1.8"
}

