package com.myorg;
//package com.therapy.infra;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

/**
 * Factory for creating Lambda functions with consistent configuration.
 * All functions share the same runtime, code asset, memory, timeout,
 * and environment variables. Only the handler class differs per function.
 */
public class LambdaFactory {

    private static final Runtime RUNTIME        = Runtime.JAVA_21;
    private static final int     MEMORY_MB      = 512;
    private static final int     TIMEOUT_SECS   = 30;
    //private static final String  CODE_ASSET_PATH = "target/therapy-lambda.jar";
    private static final String CODE_ASSET_PATH = "../therapy-lambda/target/therapy-lambda.jar";

    private final Construct scope;
    private final Map<String, String> baseEnv;
    private final List<Table> tables;

    /**
     * @param scope   CDK construct scope (the Stack)
     * @param baseEnv shared environment variables for all functions (e.g. table names)
     * @param tables  DynamoDB tables to grant read/write access on every function
     */
    public LambdaFactory(Construct scope, Map<String, String> baseEnv, List<Table> tables) {
        this.scope   = scope;
        this.baseEnv = baseEnv;
        this.tables  = tables;
    }

    /**
     * Creates a Lambda function.
     *
     * @param id      Logical CDK id, e.g. "CreateClientFunction"
     * @param handler Fully-qualified handler, e.g. "com.therapy.handlers.clients.CreateClientHandler::handleRequest"
     */
    public Function create(String id, String handler) {
        Function fn = Function.Builder.create(scope, id)
                .functionName(id)                      // actual name in AWS console
                .runtime(RUNTIME)
                .handler(handler)
                .code(Code.fromAsset(CODE_ASSET_PATH))
                .memorySize(MEMORY_MB)
                .timeout(Duration.seconds(TIMEOUT_SECS))
                .environment(baseEnv)
                .build();

        // Grant every function read/write access to every table
        tables.forEach(table -> table.grantReadWriteData(fn));

        return fn;
    }
}
