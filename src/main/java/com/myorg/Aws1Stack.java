package com.myorg;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.Duration;

import java.util.HashMap;
import java.util.Map;

public class Aws1Stack extends Stack {
    public Aws1Stack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // 1. Create the DynamoDB Table
        Table clientsTable = Table.Builder.create(this, "ClientsTable")
                .partitionKey(Attribute.builder().name("clientId").type(AttributeType.STRING).build())
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        // 2. Create the Java Lambda Function
        Map<String, String> lambdaEnv = new HashMap<>();
        lambdaEnv.put("TABLE_NAME", clientsTable.getTableName());

        Function clientHandlerLambda = Function.Builder.create(this, "ClientHandlerLambda")
                        .runtime(Runtime.JAVA_17)
                        .handler("com.myorg.ClientHandler::handleRequest")
                        .code(Code.fromAsset("target/aws-1-0.1.jar"))
                        .environment(lambdaEnv)
                        .timeout(Duration.seconds(30))      // 👈 Bumps timeout limit to 30 seconds
                        .memorySize(1024)                    // 👈 Gives the JVM 1GB of RAM for speedy execution
                        .build();

        // Grant the Lambda permission to write data into our DynamoDB Table
        clientsTable.grantWriteData(clientHandlerLambda);

        // 3. Create the API Gateway natively in Java (No Swagger file parsing needed!)
        RestApi api = RestApi.Builder.create(this, "ClientApi")
                .restApiName("Client Service API")
                .description("API for creating clients")
                .build();

        // 4. Create the "/clients" path and map the POST method to our Lambda function
        Resource clientsResource = api.getRoot().addResource("clients");
        clientsResource.addMethod("POST", new LambdaIntegration(clientHandlerLambda));
    }
}
