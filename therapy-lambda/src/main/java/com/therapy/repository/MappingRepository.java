package com.therapy.repository;

import com.therapy.model.Mapping;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MappingRepository {

    private final DynamoDbClient dynamoDb  = DynamoDbClient.create();
    private final String         tableName = System.getenv("MAPPINGS_TABLE");

    // ── Write ─────────────────────────────────────────────────────────────

    public void createMapping(Mapping mapping) {
        dynamoDb.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(toMap(mapping))
                .build());
    }

    public void updateJournalAccess(String mappingId, String journalAccess) {
        dynamoDb.updateItem(UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("mappingId", AttributeValue.fromS(mappingId)))
                .updateExpression("SET journalAccess = :journalAccess")
                .expressionAttributeValues(Map.of(
                        ":journalAccess", AttributeValue.fromS(journalAccess)
                ))
                .build());
    }

    public void deleteMapping(String mappingId) {
        dynamoDb.deleteItem(DeleteItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("mappingId", AttributeValue.fromS(mappingId)))
                .build());
    }

    // ── Read ──────────────────────────────────────────────────────────────

    public Optional<Mapping> getMappingById(String mappingId) {
        GetItemResponse response = dynamoDb.getItem(GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("mappingId", AttributeValue.fromS(mappingId)))
                .build());

        if (!response.hasItem() || response.item().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(fromMap(response.item()));
    }

    // Get all mappings for a client filtered by type ('request' or 'mapping')
    public List<Mapping> getMappingsByClientId(String clientId, String type) {
        QueryResponse response = dynamoDb.query(QueryRequest.builder()
                .tableName(tableName)
                .indexName("clientId-type-index")
                .keyConditionExpression("clientId = :clientId AND #t = :type")
                .expressionAttributeNames(Map.of("#t", "type"))
                .expressionAttributeValues(Map.of(
                        ":clientId", AttributeValue.fromS(clientId),
                        ":type",     AttributeValue.fromS(type)
                ))
                .build());

        return response.items().stream()
                .map(this::fromMap)
                .collect(Collectors.toList());
    }

    // Get all mappings for a therapist filtered by type ('request' or 'mapping')
    public List<Mapping> getMappingsByTherapistId(String therapistId, String type) {
        QueryResponse response = dynamoDb.query(QueryRequest.builder()
                .tableName(tableName)
                .indexName("therapistId-type-index")
                .keyConditionExpression("therapistId = :therapistId AND #t = :type")
                .expressionAttributeNames(Map.of("#t", "type"))
                .expressionAttributeValues(Map.of(
                        ":therapistId", AttributeValue.fromS(therapistId),
                        ":type",        AttributeValue.fromS(type)
                ))
                .build());

        return response.items().stream()
                .map(this::fromMap)
                .collect(Collectors.toList());
    }

    // Check if any mapping (request or confirmed) already exists between a client and therapist
    public boolean mappingExistsForClient(String clientId) {
        QueryResponse response = dynamoDb.query(QueryRequest.builder()
                .tableName(tableName)
                .indexName("clientId-type-index")
                .keyConditionExpression("clientId = :clientId")
                .expressionAttributeValues(Map.of(
                        ":clientId", AttributeValue.fromS(clientId)
                ))
                .build());

        return !response.items().isEmpty();
    }

    public boolean mappingExistsForTherapist(String therapistId) {
        QueryResponse response = dynamoDb.query(QueryRequest.builder()
                .tableName(tableName)
                .indexName("therapistId-type-index")
                .keyConditionExpression("therapistId = :therapistId")
                .expressionAttributeValues(Map.of(
                        ":therapistId", AttributeValue.fromS(therapistId)
                ))
                .build());

        return !response.items().isEmpty();
    }

    // ── Mapping helpers ───────────────────────────────────────────────────

    private Map<String, AttributeValue> toMap(Mapping mapping) {
        return Map.of(
                "mappingId",     AttributeValue.fromS(mapping.getMappingId()),
                "clientId",      AttributeValue.fromS(mapping.getClientId()),
                "therapistId",   AttributeValue.fromS(mapping.getTherapistId()),
                "type",          AttributeValue.fromS(mapping.getType()),
                "requestedBy",   AttributeValue.fromS(mapping.getRequestedBy()),
                "journalAccess", AttributeValue.fromS(mapping.getJournalAccess())
        );
    }

    private Mapping fromMap(Map<String, AttributeValue> item) {
        return new Mapping(
                item.get("mappingId").s(),
                item.get("clientId").s(),
                item.get("therapistId").s(),
                item.get("type").s(),
                item.get("requestedBy").s(),
                item.get("journalAccess").s()
        );
    }
}
