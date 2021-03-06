package com.typedpath.awscloudformation.test.pipeline

import com.amazonaws.regions.Regions
import com.amazonaws.services.codecommit.AWSCodeCommitClientBuilder
import com.amazonaws.services.codecommit.model.*
import com.typedpath.awscloudformation.CloudFormationTemplate
import com.typedpath.awscloudformation.test.TemplateFactory
import com.typedpath.awscloudformation.test.util.createStack
import com.typedpath.awscloudformation.test.util.defaultCredentialsProvider
import org.junit.Test
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


/**
 *  This test creates a 4 stage code pipeline with template PipelineCloudFormationTemplate.
 *  In order to test it adds code for a testUnzip function to the created repository and waits for the pipeline
 *  to deploy it. TODO Then calls the testUnzip to check the deployment worked.
 */
class PipelineTest : TemplateFactory {

    val targetCloudFormationTemplateFilename = "cloudFormationTemplate.yml"
    val codePackageFilename = "lambda-java-example-1.0-SNAPSHOT.jar"
    val strDateTime = (DateTimeFormatter.ofPattern("ddMMMyy-HHmmss")).format(LocalDateTime.now())
    val defaultReponame = "testrepo$strDateTime"

    override fun createTemplate(): CloudFormationTemplate {
        return PipelineCloudFormationTemplate(defaultReponame, targetCloudFormationTemplateFilename, codePackageFilename)
    }


    fun addSource(repoName: String) {


        //val repoName = "testrepo23Jun19-230637"
        val credentialsProvider = defaultCredentialsProvider()
        val client = AWSCodeCommitClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(Regions.US_EAST_1)
                .build()

        val createCommitRequest = CreateCommitRequest()
        createCommitRequest.branchName = "master"
        createCommitRequest.repositoryName = repoName
        createCommitRequest.setPutFiles(mutableListOf())

        fun addfile(code: String, path: String) = createCommitRequest.putFiles.add(PutFileEntry()
                .withFileContent(ByteBuffer.wrap(code.toByteArray()))
                .withFilePath(path))
        addfile(buildspecyml, "buildspec.yml")
        addfile(pomxml, "pom.xml")
        addfile(Hellojava, "src/main/java/example/Hello.java")
        addfile(cloudFormationTemplate, targetCloudFormationTemplateFilename)

        var branch: GetBranchResult
        //if there is a branch set the parentCommitId
        try {
            val getBranchRequest = GetBranchRequest()
            getBranchRequest.branchName = "master"
            getBranchRequest.repositoryName = repoName
            branch = client.getBranch(getBranchRequest)
            createCommitRequest.parentCommitId = branch.branch.commitId
        } catch (be: BranchDoesNotExistException) {
        }
        client.createCommit(createCommitRequest)
    }

    @Test
    fun pipeline() {

        val strStackName = """pipelineTestStack$strDateTime"""

        val region = Regions.US_EAST_1

        createStack(createTemplate(), strStackName, region, false) { credentialsProvider, outputs ->
            println("""*********testing testing credentials $credentialsProvider*************""")
            try {
                // add files to the unzipcode source
                // wait for build artifict
                // wait for deployment
                // createStack deployment
                //
                addSource(defaultReponame)

            } catch (e: Exception) {
                error("" + e.message)
                throw RuntimeException("failed s3 createStack", e)
            }
        }
    }

}

fun main(args: Array<String>) {
    val repo = "testrepo26Jun19-120603"
    //PipelineTest().zipResourceDirectoryToS3(repo)
    PipelineTest().pipeline()
    //println(toYaml(JavaLambdaTemplate("%functionName%", "%s3bucket%", "%s3key%", "example.Hello")))

}



