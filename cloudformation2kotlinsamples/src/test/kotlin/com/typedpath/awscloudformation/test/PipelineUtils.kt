package com.typedpath.awscloudformation.test

val pythonUnzipArtifactFunction="""
#based on code here: https://www.quora.com/How-do-I-extract-a-zip-file-in-an-Amazon-S3-by-using-Lambda
from __future__ import print_function

import urllib
import boto3
import os
import zipfile

lambda_client = boto3.client('lambda')

#-------- S3 variables -----------
s3_resource= boto3.resource('s3')
s3_client= boto3.client('s3')
code_pipeline = boto3.client('codepipeline')

#-------- global variables -------
tmpFolder = '/tmp/'
unzipTmpFile='file.zip'
deletedZipFolder='deletedZipFolder/'
extension = ".zip"

#-------- functions begin---------
def handler(event, context):
    print(event)

    theJobId = event['CodePipeline.job']['id']
    try:
        print('here x')
        job = event['CodePipeline.job']
        print('job {}'.format(job))
        data = job['data']
        inputArtifacts = data['inputArtifacts']
        print('input artifacts {}'.format(inputArtifacts))
        s3InputLocation = inputArtifacts[0]['location']['s3Location']
        print('s3InputLocation {}'.format(s3InputLocation))
        outputArtifacts = data['outputArtifacts']
        print('outputArtifacts {}'.format(outputArtifacts))
        s3OutputLocation = outputArtifacts[0]['location']['s3Location']
        print('s3OutputLocation {}'.format(s3OutputLocation))
        bucketName = s3InputLocation['bucketName']
        key = s3InputLocation['objectKey']
        destinationFolder = s3OutputLocation['objectKey']
        bucket = s3_resource.Bucket(bucketName)
        print('bucketName:{} key:{} '.format(bucketName, key))
        unzipAttachment(bucketName, key, destinationFolder)
        code_pipeline.put_job_success_result(jobId=theJobId)
        return('success')
    except Exception as e:
        code_pipeline.put_job_success_result(jobId=theJobId)
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
