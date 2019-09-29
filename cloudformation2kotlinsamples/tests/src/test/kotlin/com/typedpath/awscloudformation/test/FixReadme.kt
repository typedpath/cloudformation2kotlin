package com.typedpath.awscloudformation.test

import java.io.File
import java.lang.StringBuilder

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

    links.forEach {

        val file = searchForFile(it.name)
        if (file == null) {
            println("cant find file ${it.name}")
        }
        val relFilePath = file!!.path.replace("\\", "/").replace("../", "")

        println("${it.name} =>  ${it.startIndex}-${it.endIndex}  ${relFilePath}  ")


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


private fun searchForFile(filename: String): File? {
    val topFile = File("..")
    val fileTreeWalk = topFile.walk()
    var result: File? = null
    fileTreeWalk.iterator().forEach {
        if (it.name.endsWith(filename)) result = it
    }
    return result
}
