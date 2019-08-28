package com.typedpath.awscloudformation.test.withoutextension

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.typedpath.awscloudformation.test.s3.S3PublicReadableCloudFormationTemplate
import com.typedpath.awscloudformation.test.test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.prefs.Preferences


private fun s3ArtifactPropertyName(region: Regions) = "serverless-testutils-artifact-bucket-${region.name.toLowerCase()}".replace('_', '-')

fun getOrCreateTestArtifactS3BucketName(region: Regions, credentialsProvider : AWSCredentialsProvider) : String {
    val propertyName = s3ArtifactPropertyName(region)
    var existingBucketName: String? =  Preferences.userRoot().get(propertyName, null)

    if (existingBucketName!=null) {
        val s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(region)
                .build()
        if (!s3Client.doesBucketExistV2(existingBucketName)) {
            existingBucketName = null
            Preferences.userRoot().remove(propertyName)
        }
    }
    if (existingBucketName==null) {
        val strDateTime = DateTimeFormatter.ofPattern("ddMMyyyy-HHmmss").format(LocalDateTime.now())
        val newBucketName = "$propertyName$strDateTime".toLowerCase()
        val s3Template = S3PublicReadableCloudFormationTemplate(newBucketName)
        test(s3Template, newBucketName, region, false) {
            credentialsProvider, outputs ->
            Preferences.userRoot().put(propertyName, newBucketName)
            existingBucketName = newBucketName
        }
    }
    return existingBucketName!!

}