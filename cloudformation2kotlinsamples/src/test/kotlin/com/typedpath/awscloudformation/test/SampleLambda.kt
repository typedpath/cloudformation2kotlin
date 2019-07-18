package com.typedpath.awscloudformation.test

//https://docs.aws.amazon.com/codedeploy/latest/userguide/application-revisions-appspec-file.html#add-appspec-file-lambda

//based on     https://docs.aws.amazon.com/lambda/latest/dg/java-create-jar-pkg-maven-no-ide.html
//project-dir/pom.xml
val pomxml = """
    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>doc-examples</groupId>
  <artifactId>lambda-java-example</artifactId>
  <packaging>jar</packaging>
  <version>1.0-SNAPSHOT</version>
  <name>lambda-java-example</name>

  <dependencies>
    <dependency>
      <groupId>com.amazonaws</groupId>
      <artifactId>aws-lambda-java-core</artifactId>
      <version>1.1.0</version>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>2.3</version>
        <configuration>
          <createDependencyReducedPom>false</createDependencyReducedPom>
        </configuration>
        <executions>
          <execution>
            <phase>package</phase>
            <goals>
              <goal>shade</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
""".trimIndent()

//project-dir/src/main/java/example/Hello.java
val Hellojava="""
package example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Hello implements RequestHandler<Object, Object>{
  public Object handleRequest(Object in, Context context) {
    return "hello";
  }
}""".trimIndent()

val buildspecyml = """
version: 0.2
phases:
  build:
    commands:
        mvn package
artifacts:
  files:
    - target/lambda-java-example-1.0-SNAPSHOT.jar
    - appspec.yml
  discard-paths: yes
""".trimIndent()

val appspecyml = """
# This is an appspec.yml template file for use with an AWS Lambda deployment in CodeDeploy.
# The lines in this template starting with the hashtag symbol are
#   instructional comments and can be safely left in the file or
#   ignored.
# For help completing this file, see the "AppSpec File Reference" in the
#   "CodeDeploy User Guide" at
#   https://docs.aws.amazon.com/codedeploy/latest/userguide/app-spec-ref.html
version: 0.0
# In the Resources section specify the name, alias,
# target version, and (optional) the current version of your AWS Lambda function.
Resources:
  - TestFunction: # Replace "MyFunction" with the name of your Lambda function
      Type: AWS::Lambda::Function
      Properties:
        Name: "TestFunction" # Specify the name of your Lambda function
        Alias: "TestFunction" # Specify the alias for your Lambda function
        CurrentVersion: "0.0" # Specify the current version of your Lambda function
        TargetVersion: "1.0" # Specify the version of your Lambda function to deploy
# (Optional) In the Hooks section, specify a validation Lambda function to run during
# a lifecycle event. Replace "LifeCycleEvent" with BeforeAllowTraffic
# or AfterAllowTraffic. """


val __samplePipelineInput ="""
    {
    "CodePipeline.job": {
        "id": "7e62de82-4fcf-4ee6-a787-e007319c8b47",
        "accountId": "950651224730",
        "data": {
            "actionConfiguration": {
                "configuration": {
                    "FunctionName": "lambdaDeploytestrepo17Jul19-211546"
                }
            },
            "inputArtifacts": [
                {
                    "name": "buildArtifact",
                    "revision": null,
                    "location": {
                        "type": "S3",
                        "s3Location": {
                            "bucketName": "teststack17jul19-211546-artifactsbucket-rjjkgn23g01s",
                            "objectKey": "testStack17Jul19-211/buildArtif/5b3d9Bw"
                        }
                    }
                }
            ],
            "outputArtifacts": [
                {
                    "name": "lambdaDeployArtifact",
                    "revision": null,
                    "location": {
                        "type": "S3",
                        "s3Location": {
                            "bucketName": "teststack17jul19-211546-artifactsbucket-rjjkgn23g01s",
                            "objectKey": "testStack17Jul19-211/lambdaDepl/JQ2di3f"
                        }
                    }
                }
            ],
            "artifactCredentials": {
                "accessKeyId": "ASIA52VZ2QKNEGG7OVEF",
                "secretAccessKey": "CiCyIYAFGZO85D4kmv2MC/86Xd6kURIEGPDjrGYH",
                "sessionToken": "AgoJb3JpZ2luX2VjEOb//////////wEaCXVzLWVhc3QtMSJIMEYCIQD36lvOhixH/60PwvBVrt/i/xRLF20UI68Sc+APf/XyWgIhANHqxIdEfFjLKNsBxwsA5SyouqzfMyqK8SAEFQTS8qrzKrsFCE4QABoMOTUwNjUxMjI0NzMwIgzJbDBLMg1lw6EgPEIqmAXv9o1+X83pn+LnXkiGr6utAOrWGkANOks8wrPRYg4tWYho8UKO9BHvisZCw7LOZ1WbjvRr4gZ6R1NQZJieR9b5Gw7owtkRKJzkDLVjB0nnhtn5QJ0rbTWEecPDKdMn6+5ovZ3PsrrbskhCXdLwKPwEJhKR5M8vVwM2GBoBbjlMSDyqpGXlmRoK/UX7DMM0CY4g1p85OrnhP9l/bfzsirEoO//rzpzziyt19XCRYOABt2itkCAYbxmgXP4TqgmQdWm+IyyvYSk7Dq2dIc/l05AEc6naI3EUXLospiHmcaJ6ZuV3jL4jidT5a2NwaaDzrCPbbojNc3sFZMayC5u61Ss2+NydVLX8RxboUxP9aPfEyTh2QIiSzlfhr5bhB6jfd/0uNo8FQcYo+xurbn/yL0aO9ktRPAw4bTyIHc6zUENvgV1qNA4KG8for8na4LOpXBrko3nfbr25sTdT8byaKggjDJQoeJh3MqsA8DmeidqMaOJHwqAQyGVNMrdAlNGvJE3u+4DXnAl6F5FwAJxDgnqZCPnMA/9hMaW24xVvbsF2Y+6Sh5L3nVrGfPfBcFbANC3Lg4JYgAyTkizLTMutarnUlI9cmAYVPU17uRPYklAh8j0GN9zWaJ4/8RblD8XmAFQ0xCEWJQxkdN1V0K75pHVQF0/lTB04K1Z6MS6yNNE8eTiuWaHjvkNwNhsmZayKZZ17tRhWUlFddF62eW7ZC8CnQguhwdhwyXqAACTLug46Z+8cbKUVWYG1sZ/04VudjCDOsWposL6ZhPdEjRB8bKWaQ8YRumOxvDF9H3wq6FF+F+1dE28KM3V0iHr/Wjjl674rpkayuZnSVocoGtlOvIeqXX0Z/aKtuwrAoB3SxoUZZjmSiz/cZHhNMNKdvukFOrMBIiwacLcMnfvO+dnktqVyudWLjI3QP9QJMbteczNRF0lSumfmrteKPnJsn1yP4INgj+xfnxVQ8pdlBFQUR8HuaWtmv063HvCGqBU0xO8BHiLArZy9BKBnwGlmvF40jqJDj5dXuS+Hl1XvZKCzVsXNEcJLMJaWOJaZJQaE2gnKTpK3qmWPW7imQFbGuDMdkFdhvWgwdG+O+deQdHJ7BqHlGM/AqUB+Yu8yYa636Jqb6fNteGo="
            }
        }
    }
}
""".trimIndent()