import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    `jsonschema2kotlin`
    kotlin("jvm") version "1.3.61"
    `maven-publish`
    `java`
    id("org.jetbrains.dokka") version "0.10.0"
    signing
}

group = "com.typedpath.tools"
version = "1.0.0-SNAPSHOT"

val ossrhUsername: String by project
val ossrhPassword: String by project



tasks.register("notes") {
    doLast {
        println("local maven deploy: gradle publishToMavenLocal")
        println("ossrhUsername=$ossrhUsername")
        println("ossrhUsername=$ossrhPassword")

    }
}

tasks.register("printProps") {
    doLast {
        println(ossrhUsername)
        println(ossrhPassword)
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile (kotlin("reflect"))
    testCompile("junit:junit:4.12")
}

tasks.dokka {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}

// Create dokka Jar task from dokka task output
val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    classifier = "javadoc"
    // dependsOn(tasks.dokka) not needed; dependency automatically inferred by from(tasks.dokka)
    from(tasks.dokka)
}

val compileKotlin by tasks.getting(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class) {
    dependsOn("jsonschema2kotlin")
    kotlinOptions.jvmTarget = "1.8"
}

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

val mavenGroupId = "com.typedpath"
val mavenArtifactId = "cloudformation2kotlin"
val mavenVersion = version as String

sourceSets.main {
    withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
        kotlin.srcDirs("src/main/kotlin", "build/generated/source/kotlin/")
    }
}

publishing {
    repositories {
        maven {
            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                username = ossrhUsername
                password = ossrhPassword
            }
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            groupId = mavenGroupId
            artifactId = artifactId
            version = mavenVersion
            from(components["java"])
            artifact(sourcesJar.get())
            artifact(dokkaJar)

            pom {
                name.set("cloudformation2kotlin")
                description.set("a Kotlin DSL for AWS cloudformation")
                url.set("https://github.com/typedpath/cloudformation2kotlin")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("roberttodd")
                        name.set("Robert Todd")
                        email.set("roberttodd@typedpath.com")
                    }
                }
                scm {
                    connection.set("scm:git:https//github.com/typedpath/cloudformation2kotlin.git")
                    developerConnection.set("scm:git:https//github.com/typedpath/cloudformation2kotlin.git")
                    url.set("https://github.com/typedpath/cloudformation2kotlin")
                }
            }
        }
        // todo
        repositories {
            maven {
                // change URLs to point to your repos, e.g. http://my.org/repo
                val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                credentials {
                    username = ossrhUsername
                    password = ossrhPassword
                }
            }
        }
    }
}


signing {
    sign(publishing.publications["mavenJava"])
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}




