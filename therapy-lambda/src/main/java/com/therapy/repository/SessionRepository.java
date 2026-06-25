package com.therapy.repository;

import com.therapy.model.Session;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SessionRepository {

    private final DynamoDbClient dynamoDb  = DynamoDbClient.create();
    private final String         tableName = System.getenv("SESSIONS_TABLE");

    // ── Write ─────────────────────────────────────────────────────────────

    // Creates a new slot (type = 'slot', no clientId yet)
    public void createSlot(Session session) {
        dynamoDb.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(toMap(session))
                .build());
    }

    // Converts a slot into a confirmed session by setting clientId and type = 'session'
    public void confirmSession(String sessionId, String clientId) {
        dynamoDb.updateItem(UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("sessionId", AttributeValue.fromS(sessionId)))
                .updateExpression("SET clientId = :clientId, #t = :type")
                .expressionAttributeNames(Map.of("#t", "type"))
                .expressionAttributeValues(Map.of(
                        ":clientId", AttributeValue.fromS(clientId),
                        ":type",     AttributeValue.fromS("session")
                ))
                .build());
    }

    public void deleteSession(String sessionId) {
        dynamoDb.deleteItem(DeleteItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("sessionId", AttributeValue.fromS(sessionId)))
                .build());
    }

    // ── Read ──────────────────────────────────────────────────────────────

    public Optional<Session> getSessionById(String sessionId) {
        GetItemResponse response = dynamoDb.getItem(GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("sessionId", AttributeValue.fromS(sessionId)))
                .build());

        if (!response.hasItem() || response.item().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(fromMap(response.item()));
    }

    // Get all slots for a therapist (type = 'slot')
    public List<Session> getSlotsByTherapistId(String therapistId) {
        return getByTherapistIdAndType(therapistId, "slot");
    }

    // Get all confirmed sessions for a therapist (type = 'session')
    public List<Session> getSessionsByTherapistId(String therapistId) {
        return getByTherapistIdAndType(therapistId, "session");
    }

    // Get all confirmed sessions for a client (type = 'session')
    public List<Session> getSessionsByClientId(String clientId) {
        QueryResponse response = dynamoDb.query(QueryRequest.builder()
                .tableName(tableName)
                .indexName("clientId-type-index")
                .keyConditionExpression("clientId = :clientId AND #t = :type")
                .expressionAttributeNames(Map.of("#t", "type"))
                .expressionAttributeValues(Map.of(
                        ":clientId", AttributeValue.fromS(clientId),
                        ":type",     AttributeValue.fromS("session")
                ))
                .build());

        return response.items().stream()
                .map(this::fromMap)
                .collect(Collectors.toList());
    }

    // Check if a slot already exists for a therapist at a given datetime
    public boolean slotExistsForTherapistAtDatetime(String therapistId, String datetime) {
        QueryResponse response = dynamoDb.query(QueryRequest.builder()
                .tableName(tableName)
                .indexName("therapistId-datetime-index")
                .keyConditionExpression("therapistId = :therapistId AND #dt = :datetime")
                .expressionAttributeNames(Map.of("#dt", "datetime"))
                .expressionAttributeValues(Map.of(
                        ":therapistId", AttributeValue.fromS(therapistId),
                        ":datetime",    AttributeValue.fromS(datetime)
                ))
                .build());

        return !response.items().isEmpty();
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private List<Session> getByTherapistIdAndType(String therapistId, String type) {
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

    // ── Mapping helpers ───────────────────────────────────────────────────

    private Map<String, AttributeValue> toMap(Session session) {
        Map<String, AttributeValue> item = new java.util.HashMap<>();
        item.put("sessionId",   AttributeValue.fromS(session.getSessionId()));
        item.put("therapistId", AttributeValue.fromS(session.getTherapistId()));
        item.put("datetime",    AttributeValue.fromS(session.getDatetime()));
        item.put("type",        AttributeValue.fromS(session.getType()));
        // clientId and mark are only present on confirmed sessions, not slots
        if (session.getClientId() != null) {
            item.put("clientId", AttributeValue.fromS(session.getClientId()));
        }
        if (session.getMark() != null) {
            item.put("mark", AttributeValue.fromS(session.getMark()));
        }
        return item;
    }

    private Session fromMap(Map<String, AttributeValue> item) {
        Session session = new Session();
        session.setSessionId(item.get("sessionId").s());
        session.setTherapistId(item.get("therapistId").s());
        session.setDatetime(item.get("datetime").s());
        session.setType(item.get("type").s());
        // clientId and mark may not be present (slots don't have them)
        if (item.containsKey("clientId")) session.setClientId(item.get("clientId").s());
        if (item.containsKey("mark"))     session.setMark(item.get("mark").s());
        return session;
    }
}
