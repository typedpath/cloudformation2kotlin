
plugins {
    java
    maven
    //"kotlin"
    "io.spring.dependency-management"
    //"com.github.johnrengelman.shadow"
    //"de.sebastianboegl.shadow.transformer.log4j"
}

group = "com.typedpath"
version = "testonly"

description = """hello"""

val sourceCompatibility = 1.5
val targetCompatibility = 1.5
 tasks.withType<JavaCompile>().configureEach {
  options.encoding = "UTF-8"
}

buildscript {
  repositories {
    mavenCentral()
    maven { url = uri("https://plugins.gradle.org/m2/") }
  }
  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.50")
    classpath ("io.spring.gradle:dependency-management-plugin:1.0.3.RELEASE")
    //classpath ("com.github.jengelman.gradle.plugins:shadow:2.0.1")
    //classpath ("de.sebastianboegl.gradle.plugins:shadow-log4j-transformer:2.1.1")
  }
}


repositories {
  maven { url =uri("http://repo.maven.apache.org/maven2") }
}

// If requiring AWS JDK, uncomment the dependencyManagement to use the bill of materials
//   https://aws.amazon.com/blogs/developer/managing-dependencies-with-aws-sdk-for-java-bill-of-materials-module-bom/
//dependencyManagement {
//    imports {
//        mavenBom 'com.amazonaws:aws-java-sdk-bom:1.11.206'
//    }
//}

dependencies {
  compile("org.jetbrains.kotlin:kotlin-stdlib:1.3.50")

  compile("com.amazonaws:aws-lambda-java-core:1.1.0")
  compile ("com.amazonaws:aws-java-sdk-s3:1.11.574")
  //compile group: 'com.amazonaws', name: 'aws-lambda-java-log4j2', version:'1.0.0'
  compile("com.amazonaws:aws-lambda-java-events:2.0.1")

    compile( "org.jetbrains.kotlin:kotlin-reflect")
    testCompile( "org.jetbrains.kotlin:kotlin-reflect")

    //extraLibs("net.java.dev.jna:jna-platform:4.2.2")

  // compile group: 'com.fasterxml.jackson.core', name: 'jackson-core', version:'2.8.5'
  //compile group: 'com.fasterxml.jackson.core', name: 'jackson-databind', version:'2.8.5'
  //compile group: 'com.fasterxml.jackson.core', name: 'jackson-annotations', version:'2.8.5'
}

//task deploy(type: Exec, dependsOn: 'shadowJar') {
//  commandLine 'serverless', 'deploy'
//}


apply {
     //plugin("com.github.johnrengelman.shadow")
    plugin("java")
    plugin("maven")
    plugin("idea")
    plugin("kotlin")
}

//shadowJar { Tasks.shouldRunAfter(build) }

val fatJar = task("fatJar", type = Jar::class) {
    baseName = "${project.name}-fat"
    //exclude("**/amazonaws/**/*.class")
    from(configurations.runtimeClasspath.get()
            .map({
                println(it)
       if (it.isDirectory) it else zipTree(it) }))
    with(tasks["jar"] as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}
 



