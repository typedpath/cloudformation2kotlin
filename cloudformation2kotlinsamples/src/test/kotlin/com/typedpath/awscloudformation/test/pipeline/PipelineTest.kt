package com.typedpath.awscloudformation.test.pipeline

import com.amazonaws.regions.Regions
import com.amazonaws.services.codecommit.AWSCodeCommitClientBuilder
import com.amazonaws.services.codecommit.model.*
import com.typedpath.awscloudformation.test.*
import com.typedpath.awscloudformation.toYaml
import org.junit.Test
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class PipelineTest {

    val targetCloudFormationTemplateFilename = "cloudFormationTemplate.yml"
    val codePackageFilename = "lambda-java-example-1.0-SNAPSHOT.jar"

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

        var branch:GetBranchResult
        //if there is a branch set the parentCommitId
        try {
            val getBranchRequest = GetBranchRequest()
            getBranchRequest.branchName = "master"
            getBranchRequest.repositoryName = repoName
            branch = client.getBranch(getBranchRequest)
            createCommitRequest.parentCommitId=branch.branch.commitId
        } catch (be: BranchDoesNotExistException) {
        }
        client.createCommit(createCommitRequest)
        }

    @Test
    fun pipleline() {

        val strDateTime = (DateTimeFormatter.ofPattern("ddMMMyy-HHmmss")).format(LocalDateTime.now())

        val defaultReponame = "testrepo$strDateTime"

        val testTemplate = PipelineCloudFormationTemplate(defaultReponame, targetCloudFormationTemplateFilename, codePackageFilename)
        val strStackName = """testStack$strDateTime"""

        val region = Regions.US_EAST_1

        test(testTemplate, strStackName, region, false) { credentialsProvider ->
            println("""*********testing testing credentials $credentialsProvider*************""")
            try {
                // add files to the unzipcode source
                // wait for build artifict
                // wait for deployment
                // test deployment
                //
                addSource(defaultReponame)

            } catch (e: Exception) {
                error("" + e.message)
                throw RuntimeException("failed s3 test", e)
            }
        }
    }

}

fun main(args: Array<String>) {
    val repo = "testrepo26Jun19-120603"
    //PipelineTest().addSource(repo)
    PipelineTest().pipleline()
    //println(toYaml(JavaLambdaTemplate("%functionName%", "%s3bucket%", "%s3key%", "example.Hello")))

}



