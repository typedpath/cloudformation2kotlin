package com.typedpath.awscloudformation.test.pipeline

import com.typedpath.awscloudformation.toYaml

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
    - cloudFormationTemplate.yml
  discard-paths: yes
""".trimIndent()

val cloudFormationTemplate = toYaml(JavaLambdaTemplate("%functionName%", "%s3bucket%", "%s3key%", "example.Hello"))
//TODO implement this https://stackoverflow.com/questions/46721330/how-to-deploy-sam-template-using-boto3
