package com.typedpath.tools.awscloudformation.schema

import java.io.File
import java.nio.file.*
import java.util.Arrays.asList
import java.util.stream.Collectors

fun main(args: Array<String>) {
    val rootPath = "./buildSrc"
    val destinationRootPath = "./generated/source/kotlin"
//    val includes = asList("**/us-east1_active/RdsDbClusterSpecification.json")
    val includes = asList("**/us-east1/ApiGatewayV2IntegrationSpecification.json")
    val excludes = asList("**/us-east1/CloudFormationResourceSpecification.json")
    transformDirectory(rootPath, includes, excludes, destinationRootPath, "com.typedpath.yy")
}

fun transformDirectory(rootPath: String, strIncludes: List<String>, strExcludes: List<String>, destinationDir: String, strPackage: String) {
    println(
"""******$rootPath $strIncludes
    =>  $destinationDir::$strPackage""".trimMargin())
    val pRootPath = Paths.get(rootPath)
    println("******* rootPath = ${pRootPath.toFile().absolutePath}" )
    val filter = toFileFilter(strIncludes, strExcludes)
    transformDirectory(pRootPath, Paths.get(destinationDir), strPackage, filter)
}

private fun toFileFilter(strIncludes: List<String>, strExcludes: List<String>) : (File)->Boolean {
    val actualIncludes =
    if (strIncludes.isEmpty()) {
        listOf("**/*.json")
    } else {
        strIncludes
    }
    val actualExcludes =
        if (strExcludes.isEmpty()) {
            listOf()
        } else {
            strExcludes
        }
    val sourceIncludes = actualIncludes.stream().map({ f -> pathMatcher(f) }).collect(
        Collectors.toList<PathMatcher>()
    )
    val sourceExcludes = actualExcludes.stream().map({ f -> pathMatcher(f) }).collect(
        Collectors.toList<PathMatcher>()
    )
    return {f :File ->
        println("""${f.name} include=${sourceIncludes.any { p->p.matches(f.toPath()) }} exclude==${sourceExcludes.any { p->p.matches(f.toPath())}}""")

        sourceIncludes.any { p->p.matches(f.toPath()) } && !sourceExcludes.any { p->p.matches(f.toPath()) }
    }
}

private fun pathMatcher(filter: String): PathMatcher {
    return FileSystems.getDefault().getPathMatcher("glob:" + filter.trim { it <= ' ' })
}

fun transformDirectory(rootPath: Path, destinationRootPath: Path, strPackage: String, filter: (File) -> Boolean) {
    val jsonFiles = mutableListOf<File>()

    rootPath.toFile().walkTopDown()
        .forEach { f ->
            if (filter(f)) {
                jsonFiles.add(f)
            }
        }
    jsonFiles.forEach{
        process(it, destinationRootPath, strPackage)
    }
}

private fun copy(from: Path, to: Path) {
    println("copying from (exists: ${from.toFile().exists()}) ${from.toFile().absolutePath} to ${to.toFile().absolutePath}")
    // doesnt work sometiemes Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING)
    var text = from.toFile().readText()
    //TODO work this out
    text= text.replace("com.typedpath.awscloudformation.schema.sample", "com.typedpath.awscloudformation.schema")
    to.toFile().writeText(text)
}

private fun process(jsonFile: File, destinationRootPath: Path, strPackage: String) {
    val text = jsonFile.readText()
    println("**** ${jsonFile.name} / ${jsonFile.absolutePath}")
    val (simpleClassname, src) = jsonSchemaString2Kotlin(text, strPackage)
    val destination = "${strPackage.replace('.', File.separatorChar )}${File.separatorChar}$simpleClassname.kt"
    val fullDestination = destinationRootPath.resolve(destination)
    println("destination:${fullDestination.toFile().absoluteFile.absolutePath}")
    fullDestination.toFile().parentFile.mkdirs()
    val writer = Files.newBufferedWriter(fullDestination, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    writer.use {
        writer.write(src)
    }
}




