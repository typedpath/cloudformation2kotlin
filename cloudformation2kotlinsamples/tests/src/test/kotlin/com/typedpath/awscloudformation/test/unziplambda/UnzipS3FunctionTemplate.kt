package com.typedpath.awscloudformation.test.unziplambda

import com.typedpath.awscloudformation.LambdaRuntime
import com.typedpath.awscloudformation.schema.AWS_S3_Bucket
import com.typedpath.awscloudformation.schema.AWS_S3_BucketPolicy
import com.typedpath.awscloudformation.test.lambda.LambdaCloudFormationTemplate
import com.typedpath.iam2kotlin.IamPolicy
import com.typedpath.iam2kotlin.resources.s3.S3Action


class UnzipS3FunctionTemplate(functionName: String, testBucketName: String) : LambdaCloudFormationTemplate(functionName, pythonZipFunction(testBucketName),
        LambdaRuntime.Python3_7, "index.lambda_handler") {

    val s3Bucket = AWS_S3_Bucket {
        //TODO inject enumeration
        accessControl = "PublicRead"
        websiteConfiguration = AWS_S3_Bucket.WebsiteConfiguration {
            indexDocument = "index.html"
            errorDocument = "error.html"
        }
        bucketName = testBucketName
    }

    val policyDocument = IamPolicy() {
        statement {
            effect = IamPolicy.EffectType.Allow
            principal = mutableMapOf(
                    Pair(IamPolicy.PrincipalType.AWS, listOf("*"))
            )
            action (S3Action.GetObject)
            action( S3Action.PutObject)
            action (S3Action.GetObjectVersion)
            action(S3Action.ListBucket)
            resource(S3Action.GetObject.byBucketnameKeyname(testBucketName, "*"))
            resource(S3Action.ListBucket.byBucketname(testBucketName))
        }
    }

    val s3BucketPolicy = AWS_S3_BucketPolicy(ref(s3Bucket), policyDocument)

}
//based on https://www.quora.com/How-do-I-extract-a-zip-file-in-an-Amazon-S3-by-using-Lambda
private fun pythonZipFunction(bucketName: String)="""
#based on unzipcode here: https://www.quora.com/How-do-I-extract-a-zip-file-in-an-Amazon-S3-by-using-Lambda
from __future__ import print_function

import urllib
import boto3
import os
import zipfile

lambda_client = boto3.client('lambda')

#-------- S3 variables -----------
s3_resource= boto3.resource('s3')
s3_client= boto3.client('s3')


#-------- global variables -------
tmpFolder = '/tmp/'
unzipTmpFile='file.zip'
deletedZipFolder='deletedZipFolder/'
extension = ".zip"

#-------- functions begin---------
def lambda_handler(event, context):
    print(event)
    bucketName = event['bucketName']
    key = event['key']
    destinationFolder = event['destinationFolder']
    bucket = s3_resource.Bucket(bucketName)
    try:
        print('bucketName:{} key:{} '.format(bucketName, key))
        unzipAttachment(bucketName, key, destinationFolder)
        return('success')
    except Exception as e:
        print(e)
        print('Error getting object {} from bucket {}. Make sure they exist and your bucket is in the same region as this function.'.format(key, bucket))
        raise e
        return(e)

#--------- unzip files ----------
def unzipAttachment(bucketName, key, destinationFolder):
    print('unzipAttachment {} {} {}'.format(bucketName, key, destinationFolder))
    s3_client.download_file(bucketName, key, tmpFolder+unzipTmpFile)
    dir_name = tmpFolder
    os.chdir(dir_name)
    for item in os.listdir(tmpFolder):
        if item.endswith(extension):
            file_name = os.path.abspath(item)
            zip_ref = zipfile.ZipFile(file_name)
            zip_ref.extractall(dir_name)
            zip_ref.close()
            os.remove(file_name)
    removeFolderStucture(bucketName, key, destinationFolder)

#--------- removes nested folder structure----------
def removeFolderStucture( bucketName, key, destinationFolder):
    for p, d, f in os.walk(tmpFolder, topdown=False):
        for n in f:
            os.rename(os.path.join(p, n), os.path.join(tmpFolder, n))
        for n in d:
            os.rmdir(os.path.join(p, n))
    for iitem in os.listdir(tmpFolder):
        file_name = os.path.abspath(iitem)
        s3_client.upload_file(tmpFolder+iitem, bucketName, '{}/{}'.format(destinationFolder, iitem))
        os.remove(file_name)
""".trimIndent()