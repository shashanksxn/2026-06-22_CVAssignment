package com.therapy.repository;

import com.therapy.model.SessionRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SessionRequestRepository {

    private final DynamoDbClient dynamoDb  = DynamoDbClient.create();
    private final String         tableName = System.getenv("SESSION_REQUESTS_TABLE");

    // ── Write ─────────────────────────────────────────────────────────────

    public void createSessionRequest(SessionRequest request) {
        dynamoDb.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(toMap(request))
                .build());
    }

    public void deleteSessionRequest(String requestId) {
        dynamoDb.deleteItem(DeleteItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("requestId", AttributeValue.fromS(requestId)))
                .build());
    }

    // ── Read ──────────────────────────────────────────────────────────────

    public Optional<SessionRequest> getSessionRequestById(String requestId) {
        GetItemResponse response = dynamoDb.getItem(GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("requestId", AttributeValue.fromS(requestId)))
                .build());

        if (!response.hasItem() || response.item().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(fromMap(response.item()));
    }

    // Get all session requests for a given slot
    public List<SessionRequest> getRequestsBySlotId(String slotId) {
        QueryResponse response = dynamoDb.query(QueryRequest.builder()
                .tableName(tableName)
                .indexName("slotId-index")
                .keyConditionExpression("slotId = :slotId")
                .expressionAttributeValues(Map.of(
                        ":slotId", AttributeValue.fromS(slotId)
                ))
                .build());

        return response.items().stream()
                .map(this::fromMap)
                .collect(Collectors.toList());
    }

    // ── Mapping helpers ───────────────────────────────────────────────────

    private Map<String, AttributeValue> toMap(SessionRequest request) {
        return Map.of(
                "requestId", AttributeValue.fromS(request.getRequestId()),
                "slotId",    AttributeValue.fromS(request.getSlotId()),
                "clientId",  AttributeValue.fromS(request.getClientId()),
                "timestamp", AttributeValue.fromS(request.getTimestamp())
        );
    }

    private SessionRequest fromMap(Map<String, AttributeValue> item) {
        return new SessionRequest(
                item.get("requestId").s(),
                item.get("slotId").s(),
                item.get("clientId").s(),
                item.get("timestamp").s()
        );
    }
}
