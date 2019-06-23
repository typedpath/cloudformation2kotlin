package com.typedpath.tools.deployment

import com.amazonaws.AmazonClientException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import java.io.File
import java.nio.file.Paths

fun deploy(
    localRepo: String,
    mavenGroupId: String, mavenArtifactId: String, mavenVersion: String,
    s3Location: String, region: String = "us-east-1"
) {
    println("***deploying ${localRepo}:${mavenGroupId}:${mavenArtifactId}:${mavenVersion} => ${s3Location} ${region} ***")

    // load the local contents and upload to s3
    // current use must be in a role with write access

    var credentials: AWSCredentials? = null
    try {
        credentials = ProfileCredentialsProvider().credentials
    } catch (e: Exception) {
        throw AmazonClientException(
            "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "default location (~/.aws/credentials), and is in valid format.",
            e
        )
    }

    val localRepoDirectory = File(localRepo)
    if (!localRepoDirectory.exists()) {
        throw RuntimeException("localRepoDirectory doesnt exist: ${localRepoDirectory.absolutePath} ")
    }
    if (!localRepoDirectory.isDirectory()) {
        throw RuntimeException("localRepoDirectory isnt a directory : ${localRepoDirectory.absolutePath} ")
    }

    //TODO - deal with windows drives correctly
    val localRepoPath = Paths.get(localRepo.replace("/C:", ""))
    val localGroupPathItems = mavenGroupId.split(".")
    var localRelativePath = Paths.get(localGroupPathItems.get(0))
    for (done in 1..localGroupPathItems.size - 1) {
        localRelativePath = localRelativePath.resolve(localGroupPathItems.get(done))
    }
    localRelativePath = localRelativePath.resolve(mavenArtifactId)
    localRelativePath = localRelativePath.resolve(mavenVersion)

    val localFullPath = localRepoPath.resolve(localRelativePath)

    val s3Client = AmazonS3ClientBuilder.standard()
        .withCredentials(AWSStaticCredentialsProvider(credentials))
        .withRegion(region)
        .build()

    //TODO Change to list
    localFullPath.toFile().walkTopDown().filter {
        !it.isDirectory
    }.forEach {
        //println("found file $it")
        var contentType: String? = if (it.name.endsWith(".jar")) "application/java-archive"
        else if (it.name.endsWith("pom")) "application/pom"
        else if (it.name.endsWith("sha1")) "application/sha1"
        else null
        if (contentType == null) {
            println("not deploying file of unknown type: ${it.absolutePath}")
        } else {
            //TODO check existance of non snapshot version
            val fileObjKeyName = """repository/${localRelativePath.toString().replace("\\", "/")}/${it.name}"""
            println("using file key: $fileObjKeyName contentType: $contentType")
            val request = PutObjectRequest(s3Location, fileObjKeyName, it)
            val metadata = ObjectMetadata()
            metadata.setContentType(contentType)
            metadata.addUserMetadata("x-amz-meta-title", it.name)
            request.setMetadata(metadata)
            s3Client.putObject(request)
        }
    }


    //s3Client.doesObjectExist(s3Location, )

/*  var listResponse = client.ListObjectsV2(new ListObjectsV2Request
          {
            BucketName = bucketName,
            Prefix = "folder1/folder2"
          });
  */
    // check for existance of repo


}
