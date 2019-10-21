package com.typedpath.awscloudformation.test

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
import com.typedpath.awscloudformation.test.util.zipResourceDirectory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Paths

fun uploadBlobToS3(region: Regions, bucketName: String, keyName: String, inputStream: InputStream, credentialsProvider: AWSCredentialsProvider) : String {
    val metadata = ObjectMetadata()
    metadata.setContentType("application/blob")
    metadata.addUserMetadata("x-amz-meta-title", keyName)
    val s3Client = AmazonS3ClientBuilder.standard()
            .withCredentials(credentialsProvider)
            .withRegion(region)
            .build()
    s3Client.putObject(bucketName, keyName, inputStream, metadata)
    return "s3://$bucketName/$keyName"
}

fun uploadCodeToS3(strFile: String, region: Regions, bucketName: String, bucketKey: String, credentialsProvider: AWSCredentialsProvider): String {
    val path = Paths.get(strFile)
    val theFile = path.toFile()
    println("loading code from ${theFile.absolutePath}")
    val inputStream = FileInputStream(path.toFile())
    println("found ${inputStream.available()} bytes in ${path.toAbsolutePath()}")
    return uploadBlobToS3(region, bucketName, bucketKey, inputStream, credentialsProvider)
}

fun zipResourceDirectoryToS3(region: Regions, bucketName: String, keyName: String,
                             credentialsProvider: AWSCredentialsProvider, resourceDirectory: String) : String {
    // create a zip of the source and upload
    val baos = ByteArrayOutputStream()
    zipResourceDirectory(resourceDirectory, baos)
    val inputStream = ByteArrayInputStream(baos.toByteArray())
    return uploadBlobToS3(region, bucketName, keyName, inputStream, credentialsProvider)
}