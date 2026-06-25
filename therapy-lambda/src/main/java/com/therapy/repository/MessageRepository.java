package com.therapy.repository;

import com.therapy.model.Message;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MessageRepository {

    private final DynamoDbClient dynamoDb  = DynamoDbClient.create();
    private final String         tableName = System.getenv("MESSAGES_TABLE");

    // ── Write ─────────────────────────────────────────────────────────────

    public void createMessage(Message message) {
        dynamoDb.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(toMap(message))
                .build());
    }

    // ── Read ──────────────────────────────────────────────────────────────

    // Get all messages for a mapping, ordered by timestamp ascending
    public List<Message> getMessagesByMappingId(String mappingId) {
        QueryResponse response = dynamoDb.query(QueryRequest.builder()
                .tableName(tableName)
                .indexName("mappingId-index")
                .keyConditionExpression("mappingId = :mappingId")
                .expressionAttributeValues(Map.of(
                        ":mappingId", AttributeValue.fromS(mappingId)
                ))
                .scanIndexForward(true)   // ascending by timestamp
                .build());

        return response.items().stream()
                .map(this::fromMap)
                .collect(Collectors.toList());
    }

    // ── Mapping helpers ───────────────────────────────────────────────────

    private Map<String, AttributeValue> toMap(Message message) {
        return Map.of(
                "messageId",  AttributeValue.fromS(message.getMessageId()),
                "mappingId",  AttributeValue.fromS(message.getMappingId()),
                "sender",     AttributeValue.fromS(message.getSender()),
                "body",       AttributeValue.fromS(message.getBody()),
                "timestamp",  AttributeValue.fromS(message.getTimestamp())
        );
    }

    private Message fromMap(Map<String, AttributeValue> item) {
        return new Message(
                item.get("messageId").s(),
                item.get("mappingId").s(),
                item.get("sender").s(),
                item.get("body").s(),
                item.get("timestamp").s()
        );
    }
}
