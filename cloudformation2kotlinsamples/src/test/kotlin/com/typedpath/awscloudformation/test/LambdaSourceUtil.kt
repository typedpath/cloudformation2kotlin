package com.typedpath.awscloudformation.test

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

fun addSource(bucketName: String, keyName: String,
              credentialsProvider: AWSCredentialsProvider, sourceDirectory: String, region: Regions) {
    val s3Client = AmazonS3ClientBuilder.standard()
            .withCredentials(credentialsProvider)
            .withRegion(region)
            .build()
    // create a zip of the source and upload


    val baos = ByteArrayOutputStream()
    zipResourceDirectory(sourceDirectory, baos)
    val metadata = ObjectMetadata()
    metadata.setContentType("application/blob")
    metadata.addUserMetadata("x-amz-meta-title", keyName)
    val inputStream = ByteArrayInputStream(baos.toByteArray())
    s3Client.putObject(bucketName, keyName, inputStream, metadata)

}