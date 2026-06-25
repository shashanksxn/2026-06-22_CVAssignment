package com.therapy.repository;

import com.therapy.model.Therapist;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TherapistRepository {

    private final DynamoDbClient dynamoDb  = DynamoDbClient.create();
    private final String         tableName = System.getenv("THERAPISTS_TABLE");

    // ── Write ─────────────────────────────────────────────────────────────

    public void createTherapist(Therapist therapist) {
        dynamoDb.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(toMap(therapist))
                .build());
    }

    public void updateTherapist(Therapist therapist) {
        dynamoDb.updateItem(UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("therapistId", AttributeValue.fromS(therapist.getTherapistId())))
                .updateExpression("SET #n = :name, email = :email, password = :password")
                .expressionAttributeNames(Map.of("#n", "name"))
                .expressionAttributeValues(Map.of(
                        ":name",     AttributeValue.fromS(therapist.getName()),
                        ":email",    AttributeValue.fromS(therapist.getEmail()),
                        ":password", AttributeValue.fromS(therapist.getPassword())
                ))
                .build());
    }

    public void deleteTherapist(String therapistId) {
        dynamoDb.deleteItem(DeleteItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("therapistId", AttributeValue.fromS(therapistId)))
                .build());
    }

    // ── Read ──────────────────────────────────────────────────────────────

    public Optional<Therapist> getTherapistById(String therapistId) {
        GetItemResponse response = dynamoDb.getItem(GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("therapistId", AttributeValue.fromS(therapistId)))
                .build());

        if (!response.hasItem() || response.item().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(fromMap(response.item()));
    }

    public Optional<Therapist> getTherapistByEmail(String email) {
        QueryResponse response = dynamoDb.query(QueryRequest.builder()
                .tableName(tableName)
                .indexName("email-index")
                .keyConditionExpression("email = :email")
                .expressionAttributeValues(Map.of(
                        ":email", AttributeValue.fromS(email)
                ))
                .build());

        return response.items().stream()
                .findFirst()
                .map(this::fromMap);
    }

    public List<Therapist> getAllTherapists() {
        ScanResponse response = dynamoDb.scan(ScanRequest.builder()
                .tableName(tableName)
                .build());

        return response.items().stream()
                .map(this::fromMap)
                .collect(Collectors.toList());
    }

    // ── Mapping helpers ───────────────────────────────────────────────────

    private Map<String, AttributeValue> toMap(Therapist therapist) {
        return Map.of(
                "therapistId", AttributeValue.fromS(therapist.getTherapistId()),
                "name",        AttributeValue.fromS(therapist.getName()),
                "email",       AttributeValue.fromS(therapist.getEmail()),
                "password",    AttributeValue.fromS(therapist.getPassword())
        );
    }

    private Therapist fromMap(Map<String, AttributeValue> item) {
        return new Therapist(
                item.get("therapistId").s(),
                item.get("name").s(),
                item.get("email").s(),
                item.get("password").s()
        );
    }
}
