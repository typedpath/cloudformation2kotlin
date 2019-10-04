package com.typedpath.awscloudformation.test

import com.typedpath.awscloudformation.CloudFormationTemplate
import com.typedpath.awscloudformation.toYaml
import java.io.File
import java.lang.StringBuilder
import kotlin.reflect.KClass
import kotlin.reflect.full.*

/**
 * searches for links containing "?" and seaches for the relevant file and put in the right link
 */

fun main(args: Array<String>) {
    fixReadmeLinks()
}

fun fixReadmeLinks() {
    val file = File("../readme.md")
    val textIn = file.readText()
    val text = process(textIn)
    println(text)
    file.writeText(text)
}

private fun process(textIn: String): String {
    var complete = false
    var index = 0

    class Link(val name: String, val startIndex: Int, val endIndex: Int)

    val links = mutableListOf<Link>()

//    println("processing $textIn")
    while (!complete && (index < textIn.length)) {
        var linkNameStart = textIn.indexOf("[", index)
        if (linkNameStart < 0) break
        var linkNameEnd = textIn.indexOf("]", linkNameStart)
        if (linkNameEnd < 0) break
        val linkStart = linkNameEnd + 1
//        println("processing  linkStart ${linkStart} $textIn")
        if (textIn.length <= (linkStart + 1) || textIn[linkStart] != '(') {
            index = linkStart + 1
            continue
        }
        //println("processing $textIn")

        val linkEnd = textIn.indexOf(")", linkStart + 1)

        if (textIn[linkStart + 1] != '?') {
            index = linkEnd
            if (index < 0) break
            else continue
        }
        val linkName = textIn.substring(linkNameStart + 1, linkNameEnd)

        links.add(Link(linkName, linkStart + 1, linkEnd))

        index = linkEnd + 1

    }

    val sb = StringBuilder()

    var lastEndIndex = 0

    val topFile = File("..")

    links.forEach {

        val file = searchForFile(it.name, topFile)
        if (file == null) {
            println("cant find file ${it.name}")
        }
        val relFilePath = file!!.path.replace("\\", "/").replace("../", "")

        val kotlinSourceDir = "/kotlin/"
        val kotlinIndex = relFilePath.indexOf(kotlinSourceDir)
        var className:String? = null
        if (kotlinIndex>0) {
            className = relFilePath.substring(kotlinIndex + kotlinSourceDir.length)
                    .replace("/", ".")
                    .replace(".kt", "")
        }
        var template: CloudFormationTemplate? = null
        var templateClass: Class<*>? = null
        if (className!=null) {
                try {
                    val theClass = Class.forName(className)
                    val instance = theClass.newInstance()
                    if (instance!=null && instance is TemplateFactory) {
                        template = instance.createTemplate()
                        templateClass = template.javaClass
                    }
                } catch (ex: Exception) {
                    println("cant create ${className} because ${ex} ${ex.message} ${if (ex.cause!=null) 
                        ex.cause else ""}  ")
                }

        }


        println("${it.name} =>  ${it.startIndex}-${it.endIndex}  ${relFilePath} className: $className ")

        if (template!=null) {
            val strTemplate =  toYaml(template)
            val templateFilePath  = topFile.toPath().resolve("docs")
                    .resolve("templates").resolve(templateClass!!.simpleName+".yaml")
            println("templateFile: ${templateFilePath}")
            val templateFile = templateFilePath.toFile()
            templateFile.parentFile.mkdirs()
            templateFile.writeText(strTemplate)
            println("wrote to ${templateFile.absolutePath}  template: ${strTemplate.substring(0, 100)}")
        }

        // write from last endIndex to startIndex
        sb.append(textIn.substring(lastEndIndex, it.startIndex))
        // write relFilePath
        sb.append(relFilePath)
        lastEndIndex = it.endIndex

    }

    //write from last endIndex to end
    sb.append(textIn.substring(lastEndIndex, textIn.length))

    return sb.toString()
}

private fun searchForFile(filename: String, topFile: File): File? {
    val fileTreeWalk = topFile.walk()
    var result: File? = null
    fileTreeWalk.iterator().forEach {
        if (it.name.endsWith(filename)) result = it
    }
    return result
}
