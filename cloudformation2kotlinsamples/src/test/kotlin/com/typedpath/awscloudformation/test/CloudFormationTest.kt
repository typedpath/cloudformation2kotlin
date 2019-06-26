package com.typedpath.awscloudformation.test

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudformation.AmazonCloudFormation
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder
import com.amazonaws.services.cloudformation.model.*
import com.typedpath.awscloudformation.CloudFormationTemplate
import com.typedpath.awscloudformation.toYaml


fun defaultCredentialsProvider(): AWSCredentialsProvider {
    try {
        return ProfileCredentialsProvider()
    } catch (e: Exception) {
        throw AmazonClientException(
                "Cannot load the credentials from the credential profiles file. " +
                        "Please make sure that your credentials file is at the correct " +
                        "location (~/.aws/credentials), and is in valid format.",
                e)
    }
}

/**
 * this is based on https://github.com/aws/aws-sdk-java/tree/master/src/samples/AwsCloudFormation
 */
fun test(template: CloudFormationTemplate, stackName: String, region: Regions = Regions.US_EAST_1, cleanup: Boolean = true, onSuccess: (credentialsProvider: AWSCredentialsProvider) -> Unit = { }) {

    val credentialsProvider = defaultCredentialsProvider()

    val stackbuilder = AmazonCloudFormationClientBuilder.standard()
            .withCredentials(credentialsProvider)
            .withRegion(region)
            .build()

    val logicalResourceName = "SampleNotificationTopic"

    try {
        // Create a stack
        val createRequest = CreateStackRequest()
        createRequest.setStackName(stackName)
        val strTemplate = toYaml(template)
        println(strTemplate)
        createRequest.setTemplateBody(strTemplate)
        println("Creating a stack called " + createRequest.getStackName() + ".")
        createRequest.withCapabilities(Capability.CAPABILITY_IAM, Capability.CAPABILITY_NAMED_IAM)
        stackbuilder.createStack(createRequest)

        // Wait for stack to be created
        // Note that you could use SNS notifications on the CreateStack call to track the progress of the stack creation
        println("Stack creation completed, the stack " + stackName + " completed with " + waitForCompletion(stackbuilder, stackName))

        // Show all the stacks for this account along with the resources for each stack
        for (stack in stackbuilder.describeStacks(DescribeStacksRequest()).getStacks()) {
            println("Stack : " + stack.getStackName() + " [" + stack.getStackStatus().toString() + "]")

            val stackResourceRequest = DescribeStackResourcesRequest()
            stackResourceRequest.setStackName(stack.getStackName())
            for (resource in stackbuilder.describeStackResources(stackResourceRequest).getStackResources()) {
                System.out.format("    %1$-40s %2$-25s %3\$s\n", resource.getResourceType(), resource.getLogicalResourceId(), resource.getPhysicalResourceId())
            }
        }

        // Lookup a resource by its logical name
        val logicalNameResourceRequest = DescribeStackResourcesRequest()
        logicalNameResourceRequest.setStackName(stackName)
        logicalNameResourceRequest.setLogicalResourceId(logicalResourceName)
        System.out.format("Looking up resource name %1\$s from stack %2\$s\n", logicalNameResourceRequest.getLogicalResourceId(), logicalNameResourceRequest.getStackName())
        for (resource in stackbuilder.describeStackResources(logicalNameResourceRequest).getStackResources()) {
            System.out.format("    %1$-40s %2$-25s %3\$s\n", resource.getResourceType(), resource.getLogicalResourceId(), resource.getPhysicalResourceId())
        }

        onSuccess(credentialsProvider)

    } catch (ase: AmazonServiceException) {
        println(("Caught an AmazonServiceException, which means your request made it " + "to AWS CloudFormation, but was rejected with an error response for some reason."))
        println("Error Message:    " + ase.message)
        println("HTTP Status Code: " + ase.statusCode)
        println("AWS Error Code:   " + ase.errorCode)
        println("Error Type:       " + ase.errorType)
        println("Request ID:       " + ase.requestId)
    } catch (ace: AmazonClientException) {
        println(("Caught an AmazonClientException, which means the client encountered "
                + "a serious internal problem while trying to communicate with AWS CloudFormation, "
                + "such as not being able to access the network."))
        println("Error Message: " + ace.message)
    } finally {
        if (cleanup) {
            // Delete the stack
            val deleteRequest = DeleteStackRequest()
            deleteRequest.setStackName(stackName)
            println("Deleting the stack called " + deleteRequest.getStackName() + ".")
            stackbuilder.deleteStack(deleteRequest)

            // Wait for stack to be deleted
            // Note that you could used SNS notifications on the original CreateStack call to track the progress of the stack deletion
            println("Stack creation completed, the stack " + stackName + " completed with " + waitForCompletion(stackbuilder, stackName))
        }
    }

}

// Wait for a stack to complete transitioning
// End stack states are:
//    CREATE_COMPLETE
//    CREATE_FAILED
//    DELETE_FAILED
//    ROLLBACK_FAILED
// OR the stack no longer exists
@Throws(Exception::class)
fun waitForCompletion(stackbuilder: AmazonCloudFormation, stackName: String): String {

    val wait = DescribeStacksRequest()
    wait.stackName = stackName
    var completed: Boolean = false
    var stackStatus = "Unknown"
    var stackReason = ""

    print("Waiting")

    while (!completed) {
        var stacks: List<Stack>
        try {
            stacks = stackbuilder.describeStacks(wait).stacks
        } catch (ace: AmazonCloudFormationException) {
            //describe stacks bombs out if the stack doesnt exist
            if (ace.message!!.contains("does not exist")) {
                stacks = emptyList()
            } else throw ace
        }
        if (stacks.isEmpty()) {
            completed = true
            stackStatus = "NO_SUCH_STACK"
            stackReason = "Stack has been deleted"
        } else {
            for (stack in stacks) {
                if (stack.stackStatus == StackStatus.CREATE_COMPLETE.toString() ||
                        stack.stackStatus == StackStatus.CREATE_FAILED.toString() ||
                        stack.stackStatus == StackStatus.ROLLBACK_FAILED.toString() ||
                        stack.stackStatus == StackStatus.DELETE_FAILED.toString()) {
                    completed = true
                    stackStatus = stack.stackStatus
                    try {
                        stackReason = stack.stackStatusReason
                    } catch (tw: Throwable) {
                        stackReason = tw.toString()
                    }
                }
            }
        }

        // Show we are waiting
        print(".")

        // Not done yet so sleep for 10 seconds.
        if (completed) Thread.sleep(5000)
    }

    // Show we are done
    print("done\n")

    return "$stackStatus ($stackReason)"
}
