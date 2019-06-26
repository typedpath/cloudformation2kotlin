package com.typedpath.awscloudformation.test

import com.amazonaws.regions.Regions
import com.amazonaws.services.codecommit.AWSCodeCommitClientBuilder
import com.amazonaws.services.codecommit.model.*
import org.junit.Test
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class PipelineTest {


    private fun readLine(url: URL): String = (BufferedReader(InputStreamReader(url.openStream()))).readLine()

    @Test
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
        createCommitRequest.putFiles.add(PutFileEntry()
                .withFileContent(ByteBuffer.wrap(buildspecyml.toByteArray()))
                .withFilePath("buildspec.yml"))
        createCommitRequest.putFiles.add(PutFileEntry()
                .withFileContent(ByteBuffer.wrap(pomxml.toByteArray()))
                .withFilePath("pom.xml"))
        createCommitRequest.putFiles.add(PutFileEntry()
                .withFileContent(ByteBuffer.wrap(Hellojava.toByteArray()))
                .withFilePath("src/main/java/example/Hello.java"))
        createCommitRequest.putFiles.add(PutFileEntry()
                .withFileContent(ByteBuffer.wrap(appspecyml.toByteArray()))
                .withFilePath("appspec.yml"))
        var branch:GetBranchResult
        try {
            val getBranchRequest = GetBranchRequest()
            getBranchRequest.branchName = "master"
            getBranchRequest.repositoryName = repoName
            branch = client.getBranch(getBranchRequest)
            createCommitRequest.parentCommitId=branch.branch.commitId
        } catch (be: BranchDoesNotExistException) {
        }
        client.createCommit(createCommitRequest)
        /*   var branch:GetBranchResult
           try {
               val getBranchRequest = GetBranchRequest()
               getBranchRequest.branchName = "master"
               getBranchRequest.repositoryName = repoName
               branch = client.getBranch(getBranchRequest)
           } catch (be: BranchDoesNotExistException) {
               val branchRequest = CreateBranchRequest()
               branchRequest.repositoryName=repoName
               branchRequest.branchName = "master"
   //            branchRequest.commitId =
               client.createBranch(branchRequest)
           }

           fun putFile(filename: String, path: String, content: String) {
               val putFileRequest = PutFileRequest()
               val getBranchRequest = GetBranchRequest()
               getBranchRequest.branchName = "master"
               getBranchRequest.repositoryName = repoName
               val branch = client.getBranch(getBranchRequest)
               putFileRequest.name = filename
               putFileRequest.fileContent = ByteBuffer.wrap(content.toByteArray())
               putFileRequest.repositoryName = repoName
               putFileRequest.branchName = "master"
               putFileRequest.filePath = path
   //            putFileRequest.fileMode = FileModeTypeEnum.NORMAL.toString()
                   putFileRequest.parentCommitId = branch.branch.commitId
               try {
                   client.putFile(putFileRequest)
               } catch (e: SameFileContentException) {

               }
           }
           putFile("readme.txt", "readme.txt", "just a test commit")
           putFile("buildspec.yml", "buildspec.yml", buildspecyml)
           putFile("pom.xml", "pom.xml", pomxml)
           putFile("Hello.java", "src/main/java/example/Hello.java", Hellojava)
           putFile("appspec.yml", "apppec.yml", appspecyml)
   */
    }

    @Test
    fun pipleline() {

        val strDateTime = (DateTimeFormatter.ofPattern("ddMMMyy-HHMMss")).format(LocalDateTime.now())

        val defaultReponame = "testrepo$strDateTime"

        val testTemplate = PipelineCloudFormationTemplate(defaultReponame)
        val strStackName = """testStack$strDateTime"""

        val region = Regions.US_EAST_1

        test(testTemplate, strStackName, region, false) { credentialsProvider ->
            println("""*********testing testing credentials $credentialsProvider*************""")
            try {
                // add files to the code source
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
    PipelineTest().addSource(repo)
    //PipelineTest().pipleline()
}



