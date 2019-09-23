package com.typedpath.awscloudformation.test.util

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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


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
fun createStack(template: CloudFormationTemplate, stackName: String, region: Regions = Regions.US_EAST_1,
                cleanup: Boolean = true, onSuccess: (credentialsProvider: AWSCredentialsProvider, outputs: List<Output>) -> Unit) {

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
        createRequest.withCapabilities(Capability.CAPABILITY_IAM, Capability.CAPABILITY_NAMED_IAM, Capability.CAPABILITY_AUTO_EXPAND)
        stackbuilder.createStack(createRequest)

        // Wait for stack to be created
        // Note that you could use SNS notifications on the CreateStack call to track the progress of the stack creation
        println("Stack creation completed, the stack " + stackName + " completed with " + waitForCompletion(stackbuilder, stackName))

        // Show all the stacks for this account along with the resources for each stack
        // Lookup a resource by its logical name
        val logicalNameResourceRequest = DescribeStackResourcesRequest()
        logicalNameResourceRequest.setStackName(stackName)
        logicalNameResourceRequest.setLogicalResourceId(logicalResourceName)
        println("Looking up resource name ${logicalNameResourceRequest.getLogicalResourceId()} from stack ${logicalNameResourceRequest.getStackName()}")

        var completeStack: Stack? = null

        val describeStacksRequest = DescribeStacksRequest().withStackName(stackName);

        val timeoutMiliSeconds = 180 * 1000

        val startTime = System.currentTimeMillis()
        while (completeStack == null) {
            val theStacks = stackbuilder.describeStacks(describeStacksRequest).stacks
            if (theStacks != null && theStacks.size > 0 && !theStacks.get(0).stackStatus.toUpperCase().contains("PROGRESS")
            ) {
                completeStack = theStacks.get(0)
            } else {
                Thread.sleep(300)
            }
            if ((System.currentTimeMillis() - startTime) > timeoutMiliSeconds) {
                println("timed out wait for stack")
                throw RuntimeException("times out waiting for stack $stackName to complete")
            }
        }

        println("stack ${completeStack.stackName} status :  ${completeStack.stackStatus}")


        val outputs = completeStack.outputs

        onSuccess(credentialsProvider, outputs)

    } catch (ase: AmazonServiceException) {
        println(("Caught an AmazonServiceException, which means your request made it " + "to AWS CloudFormation, but was rejected with an error response for some reason."))
        println("Error Message:    " + ase.message)
        println("HTTP Status Code: " + ase.statusCode)
        println("AWS Error Code:   " + ase.errorCode)
        println("Error Type:       " + ase.errorType)
        println("Request ID:       " + ase.requestId)
        throw RuntimeException(ase)
    } catch (ace: AmazonClientException) {
        println(("Caught an AmazonClientException, which means the client encountered "
                + "a serious internal problem while trying to communicate with AWS CloudFormation, "
                + "such as not being able to access the network."))
        println("Error Message: " + ace.message)
        throw RuntimeException(ace)
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

fun defaultCurrentDateTimePattern() = "${(DateTimeFormatter.ofPattern("ddMMMyy-HHmmss")).format(LocalDateTime.now()).toLowerCase()}"

fun defaultStackName(cloudFormationTemplate: CloudFormationTemplate): String =
        "${cloudFormationTemplate.javaClass.name.toLowerCase().replace('.', '-')
                .replace('$', '-')}-${defaultCurrentDateTimePattern()}"


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
                        stack.stackStatus == StackStatus.ROLLBACK_COMPLETE.toString() ||
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
        if (!completed) Thread.sleep(5000)
    }

    // Show we are done
    print("done\n")

    return "$stackStatus ($stackReason)"
}
