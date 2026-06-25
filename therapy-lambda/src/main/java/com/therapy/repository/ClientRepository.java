package com.therapy.repository;

import com.therapy.model.Client;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClientRepository {

    private final DynamoDbClient dynamoDb  = DynamoDbClient.create();
    private final String         tableName = System.getenv("CLIENTS_TABLE");

    // ── Write ─────────────────────────────────────────────────────────────

    public void createClient(Client client) {
        dynamoDb.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(toMap(client))
                .build());
    }

    public void updateClient(Client client) {
        dynamoDb.updateItem(UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("clientId", AttributeValue.fromS(client.getClientId())))
                .updateExpression("SET #n = :name, email = :email, password = :password")
                // "name" is a reserved word in DynamoDB so we use an expression alias
                .expressionAttributeNames(Map.of("#n", "name"))
                .expressionAttributeValues(Map.of(
                        ":name",     AttributeValue.fromS(client.getName()),
                        ":email",    AttributeValue.fromS(client.getEmail()),
                        ":password", AttributeValue.fromS(client.getPassword())
                ))
                .build());
    }

    public void deleteClient(String clientId) {
        dynamoDb.deleteItem(DeleteItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("clientId", AttributeValue.fromS(clientId)))
                .build());
    }

    // ── Read ──────────────────────────────────────────────────────────────

    public Optional<Client> getClientById(String clientId) {
        GetItemResponse response = dynamoDb.getItem(GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("clientId", AttributeValue.fromS(clientId)))
                .build());

        if (!response.hasItem() || response.item().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(fromMap(response.item()));
    }

    public Optional<Client> getClientByEmail(String email) {
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

    public List<Client> getAllClients() {
        ScanResponse response = dynamoDb.scan(ScanRequest.builder()
                .tableName(tableName)
                .build());

        return response.items().stream()
                .map(this::fromMap)
                .collect(Collectors.toList());
    }

    // ── Mapping helpers ───────────────────────────────────────────────────

    // Client object → DynamoDB item map
    private Map<String, AttributeValue> toMap(Client client) {
        return Map.of(
                "clientId", AttributeValue.fromS(client.getClientId()),
                "name",     AttributeValue.fromS(client.getName()),
                "email",    AttributeValue.fromS(client.getEmail()),
                "password", AttributeValue.fromS(client.getPassword())
        );
    }

    // DynamoDB item map → Client object
    private Client fromMap(Map<String, AttributeValue> item) {
        return new Client(
                item.get("clientId").s(),
                item.get("name").s(),
                item.get("email").s(),
                item.get("password").s()
        );
    }
}
