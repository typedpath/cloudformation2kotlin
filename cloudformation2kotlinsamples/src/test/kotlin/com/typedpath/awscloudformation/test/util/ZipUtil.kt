package com.typedpath.awscloudformation.test.util

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


fun zipDirectory(directory: File, os: OutputStream): Unit {

    val files  = mutableListOf<Path>()

    val fileVisitor =object : SimpleFileVisitor<Path>() {
       override fun visitFile(path: Path, attrs: BasicFileAttributes) : FileVisitResult
       {
           println("visited ${path}" )
           files.add(path)
           return FileVisitResult.CONTINUE
       }
    }

    Files.walkFileTree(directory.toPath(), fileVisitor)
    val out =  ZipOutputStream(os);
    files.forEach {
        var ze =  ZipEntry(it.fileName.toString());
        out.putNextEntry(ze);
        out.write(it.toFile().readBytes())
    }
    out.close();
}


private fun getResourceFolder(folder: String): File {
    val loader = Thread.currentThread().contextClassLoader
    val url = loader.getResource(folder)
    return File(url!!.path)
}


fun zipResourceDirectory(resourceDirectory: String, os: OutputStream) {
    zipDirectory(getResourceFolder(resourceDirectory), os)
}

fun main(args: Array<String>) {

    val file =  File("./createStack.zip")
    val resourceDir  = "serverless/basic/src"
    val fos = FileOutputStream(file);

    println("zipping resource $resourceDir into ${file.absoluteFile}" )
    val dir = zipResourceDirectory(resourceDir, fos)
    fos.close()

}