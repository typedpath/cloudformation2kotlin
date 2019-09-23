package com.typedpath.awscloudformation.test.pipeline

fun pythonDeployLambdaCodeFunction(templateFileName: String, codePackageName: String)="""
from __future__ import print_function
import boto3
s3_client= boto3.client('s3')
cf_client = boto3.client('cloudformation')
code_pipeline = boto3.client('codepipeline')

def handler(event, context):
    print(event)
    theJobId = event['CodePipeline.job']['id']
    try:
        job = event['CodePipeline.job']
        inputData = job['data']
        inputArtifacts = inputData['inputArtifacts']
        s3InputLocation = inputArtifacts[0]['location']['s3Location']
        bucketName = s3InputLocation['bucketName']
        key = s3InputLocation['objectKey']

        tmplatesrckey = '{}/$templateFileName'.format(key)

        s3client = boto3.client('s3')
        data = s3client.get_object(Bucket=bucketName, Key=tmplatesrckey)
        contents = data['Body'].read()
        templateSrcText = contents.decode('utf-8' )
        functionName = "{}-target".format(inputData['actionConfiguration']['configuration']['FunctionName'])
        templateText = templateSrcText.replace("%functionName%", functionName).replace("%s3bucket%", bucketName).replace("%s3key%", "{}/$codePackageName".format(key))
        templateFilledKey =  '{}/deploy.yml'.format(key)
        s3client.put_object(Bucket=bucketName, Key=templateFilledKey, Body=templateText.encode())
        stackName = "{}".format(functionName)
        stackStatus = evalStackStatus(stackName)

        print("stack {} status: {}".format(stackName, stackStatus))
        # create changeset

        if stackStatus == 'ROLLBACK_COMPLETE':
            print('Deleting {}'.format(stackName))
            response = cf_client.delete_stack(
               StackName = stackName
               )
            waiter = cf_client.get_waiter('stack_delete_complete')

            stackStatus = evalStackStatus(stackName)
            print("POST DELETE stack {} status: {}".format(stackName, stackStatus))

            if stackStatus == 'ROLLBACK_COMPLETE':
                print('Deleting {}'.format(stackName))
                response = cf_client.delete_stack(
                   StackName = stackName
                   )
                waiter = cf_client.get_waiter('stack_delete_complete')
                stackStatus = evalStackStatus(stackName)
                print("POST POST DELETE stack {} status: {}".format(stackName, stackStatus))
            stackExists = False
        else:
           stackExists = stackStatus != None

        if stackExists:
            print('Updating {}'.format(stackName))
            response = cf_client.update_stack(
               StackName = stackName,
               TemplateBody = templateText,
               Capabilities = ['CAPABILITY_IAM']
               )
            waiter = cf_client.get_waiter('stack_update_complete')
        else:
            print('Creating {}'.format(stackName))
            response = cf_client.create_stack(
               StackName = stackName,
               TemplateBody = templateText,
               Capabilities = ['CAPABILITY_IAM']
               )
            waiter = cf_client.get_waiter('stack_create_complete')

        print("...waiting for stack to be ready...")
        waiter.wait(StackName=stackName)
        code_pipeline.put_job_success_result(jobId=theJobId)
        return('success')
    except Exception as e:
        print(e)
        errorMessage = "{}".format(e)
        if "No updates are to be performed" in errorMessage:
            code_pipeline.put_job_success_result(jobId=theJobId)
            print ("update already complete")
            return "success"
        else:
            code_pipeline.put_job_failure_result(jobId=theJobId,
              failureDetails={'type': 'JobFailed', 'message': errorMessage})
            raise e
            return(e)

def evalStackStatus(stack_name):
    stacks = cf_client.list_stacks()['StackSummaries']
    for stack in stacks:
        theStatus = stack['StackStatus']
        if theStatus == 'DELETE_COMPLETE':
            continue
        if stack_name == stack['StackName']:
            return theStatus
    return None
""".trimIndent()

//TODO tidy this
val pythonUnzipArtifactFunction="""
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
        code_pipeline.put_job_failure_result(jobId=theJobId)
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
