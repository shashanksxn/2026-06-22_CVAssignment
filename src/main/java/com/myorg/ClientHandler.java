package com.myorg;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final DynamoDbClient ddb = DynamoDbClient.create();
    private final String tableName = System.getenv("TABLE_NAME");

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        try {
            String body = request.getBody();

            // Basic regex parsing to extract JSON fields without needing a heavy parsing library
            String name = extractField(body, "name");
            String email = extractField(body, "email");
            String password = extractField(body, "password");
            String clientId = UUID.randomUUID().toString();

            // Prepare DynamoDB item mapping
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("clientId", AttributeValue.builder().s(clientId).build());
            item.put("name", AttributeValue.builder().s(name).build());
            item.put("email", AttributeValue.builder().s(email).build());
            item.put("password", AttributeValue.builder().s(password).build());

            // Write to DynamoDB
            ddb.putItem(PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build());

            // Build success response
            response.setStatusCode(200);
            response.setBody("{\"clientId\": \"" + clientId + "\"}");

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("{\"error\": \"" + e.getMessage() + "\"}");
        }

        return response;
    }

    private String extractField(String json, String field) {
        Pattern pattern = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "unknown";
    }
}
