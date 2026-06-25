package com.myorg;
// package com.therapy.infra;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.lambda.Function;
import java.util.List;
import java.util.Map;

// import software.amazon.awscdk.Duration;
// import software.amazon.awscdk.services.sqs.Queue;

public class TherapyAppStack extends Stack {
    public TherapyAppStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public TherapyAppStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // ──────────────────────────────────────────────
        // DYNAMODB TABLES
        // ──────────────────────────────────────────────

        Table clientsTable = Table.Builder.create(this, "ClientsTable")
                .tableName("ClientsTable")
                .partitionKey(Attribute.builder().name("clientId").type(AttributeType.STRING).build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
        clientsTable.addGlobalSecondaryIndex(software.amazon.awscdk.services.dynamodb.GlobalSecondaryIndexProps.builder()
                .indexName("email-index")
                .partitionKey(Attribute.builder().name("email").type(AttributeType.STRING).build())
                .build());

        Table therapistsTable = Table.Builder.create(this, "TherapistsTable")
                .tableName("TherapistsTable")
                .partitionKey(Attribute.builder().name("therapistId").type(AttributeType.STRING).build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
        therapistsTable.addGlobalSecondaryIndex(software.amazon.awscdk.services.dynamodb.GlobalSecondaryIndexProps.builder()
                .indexName("email-index")
                .partitionKey(Attribute.builder().name("email").type(AttributeType.STRING).build())
                .build());

        Table mappingsTable = Table.Builder.create(this, "MappingsTable")
                .tableName("MappingsTable")
                .partitionKey(Attribute.builder().name("mappingId").type(AttributeType.STRING).build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
        mappingsTable.addGlobalSecondaryIndex(software.amazon.awscdk.services.dynamodb.GlobalSecondaryIndexProps.builder()
                .indexName("clientId-type-index")
                .partitionKey(Attribute.builder().name("clientId").type(AttributeType.STRING).build())
                .sortKey(Attribute.builder().name("type").type(AttributeType.STRING).build())
                .build());
        mappingsTable.addGlobalSecondaryIndex(software.amazon.awscdk.services.dynamodb.GlobalSecondaryIndexProps.builder()
                .indexName("therapistId-type-index")
                .partitionKey(Attribute.builder().name("therapistId").type(AttributeType.STRING).build())
                .sortKey(Attribute.builder().name("type").type(AttributeType.STRING).build())
                .build());

        Table messagesTable = Table.Builder.create(this, "MessagesTable")
                .tableName("MessagesTable")
                .partitionKey(Attribute.builder().name("messageId").type(AttributeType.STRING).build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
        messagesTable.addGlobalSecondaryIndex(software.amazon.awscdk.services.dynamodb.GlobalSecondaryIndexProps.builder()
                .indexName("mappingId-index")
                .partitionKey(Attribute.builder().name("mappingId").type(AttributeType.STRING).build())
                .build());

        Table sessionsTable = Table.Builder.create(this, "SessionsTable")
                .tableName("SessionsTable")
                .partitionKey(Attribute.builder().name("sessionId").type(AttributeType.STRING).build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
        sessionsTable.addGlobalSecondaryIndex(software.amazon.awscdk.services.dynamodb.GlobalSecondaryIndexProps.builder()
                .indexName("clientId-type-index")
                .partitionKey(Attribute.builder().name("clientId").type(AttributeType.STRING).build())
                .sortKey(Attribute.builder().name("type").type(AttributeType.STRING).build())
                .build());
        sessionsTable.addGlobalSecondaryIndex(software.amazon.awscdk.services.dynamodb.GlobalSecondaryIndexProps.builder()
                .indexName("therapistId-type-index")
                .partitionKey(Attribute.builder().name("therapistId").type(AttributeType.STRING).build())
                .sortKey(Attribute.builder().name("type").type(AttributeType.STRING).build())
                .build());
        sessionsTable.addGlobalSecondaryIndex(software.amazon.awscdk.services.dynamodb.GlobalSecondaryIndexProps.builder()
                .indexName("therapistId-datetime-index")
                .partitionKey(Attribute.builder().name("therapistId").type(AttributeType.STRING).build())
                .sortKey(Attribute.builder().name("datetime").type(AttributeType.STRING).build())
                .build());

        Table sessionRequestsTable = Table.Builder.create(this, "SessionRequestsTable")
                .tableName("SessionRequestsTable")
                .partitionKey(Attribute.builder().name("requestId").type(AttributeType.STRING).build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
        sessionRequestsTable.addGlobalSecondaryIndex(software.amazon.awscdk.services.dynamodb.GlobalSecondaryIndexProps.builder()
                .indexName("slotId-index")
                .partitionKey(Attribute.builder().name("slotId").type(AttributeType.STRING).build())
                .build());

        // ──────────────────────────────────────────────
        // LAMBDA FACTORY
        // ──────────────────────────────────────────────

        List<Table> allTables = List.of(
                clientsTable, therapistsTable, mappingsTable,
                messagesTable, sessionsTable, sessionRequestsTable
        );

        // Table names passed as environment variables to every Lambda
        Map<String, String> env = Map.of(
                "CLIENTS_TABLE",          clientsTable.getTableName(),
                "THERAPISTS_TABLE",       therapistsTable.getTableName(),
                "MAPPINGS_TABLE",         mappingsTable.getTableName(),
                "MESSAGES_TABLE",         messagesTable.getTableName(),
                "SESSIONS_TABLE",         sessionsTable.getTableName(),
                "SESSION_REQUESTS_TABLE", sessionRequestsTable.getTableName()
        );

        LambdaFactory factory = new LambdaFactory(this, env, allTables);

        // ──────────────────────────────────────────────
        // LAMBDA FUNCTIONS  (one per API operation)
        // Naming: <Verb><Resource>Function
        // Handler: com.therapy.handlers.<resource>.<ClassName>::handleRequest
        // ──────────────────────────────────────────────

        // Clients
        Function createClientFn        = factory.create("CreateClientFunction",        "com.therapy.handlers.clients.CreateClientHandler::handleRequest");
        Function listClientsFn         = factory.create("ListClientsFunction",          "com.therapy.handlers.clients.ListClientsHandler::handleRequest");
        Function getClientFn           = factory.create("GetClientFunction",            "com.therapy.handlers.clients.GetClientHandler::handleRequest");
        Function deleteClientFn        = factory.create("DeleteClientFunction",         "com.therapy.handlers.clients.DeleteClientHandler::handleRequest");
        Function updateClientFn        = factory.create("UpdateClientFunction",         "com.therapy.handlers.clients.UpdateClientHandler::handleRequest");

        // Client Requests
        Function createClientRequestFn = factory.create("CreateClientRequestFunction",  "com.therapy.handlers.clients.CreateClientRequestHandler::handleRequest");
        Function listClientRequestsFn  = factory.create("ListClientRequestsFunction",   "com.therapy.handlers.clients.ListClientRequestsHandler::handleRequest");
        Function deleteClientRequestFn = factory.create("DeleteClientRequestFunction",  "com.therapy.handlers.clients.DeleteClientRequestHandler::handleRequest");

        // Client Mappings
        Function listClientMappingsFn  = factory.create("ListClientMappingsFunction",   "com.therapy.handlers.clients.ListClientMappingsHandler::handleRequest");
        Function deleteClientMappingFn = factory.create("DeleteClientMappingFunction",  "com.therapy.handlers.clients.DeleteClientMappingHandler::handleRequest");

        // Client Messages
        Function createClientMessageFn = factory.create("CreateClientMessageFunction",  "com.therapy.handlers.clients.CreateClientMessageHandler::handleRequest");
        Function listClientMessagesFn  = factory.create("ListClientMessagesFunction",   "com.therapy.handlers.clients.ListClientMessagesHandler::handleRequest");

        // Client Slots (view + book)
        Function listClientSlotsFn     = factory.create("ListClientMappingSlotsFunction","com.therapy.handlers.clients.ListClientMappingSlotsHandler::handleRequest");
        Function bookSlotFn            = factory.create("BookSlotFunction",              "com.therapy.handlers.clients.BookSlotHandler::handleRequest");

        // Client Journal Access
        Function updateJournalAccessFn = factory.create("UpdateJournalAccessFunction",  "com.therapy.handlers.clients.UpdateJournalAccessHandler::handleRequest");

        // Client Sessions
        Function listClientSessionsFn  = factory.create("ListClientSessionsFunction",   "com.therapy.handlers.clients.ListClientSessionsHandler::handleRequest");

        // Therapists
        Function createTherapistFn        = factory.create("CreateTherapistFunction",        "com.therapy.handlers.therapists.CreateTherapistHandler::handleRequest");
        Function listTherapistsFn         = factory.create("ListTherapistsFunction",          "com.therapy.handlers.therapists.ListTherapistsHandler::handleRequest");
        Function getTherapistFn           = factory.create("GetTherapistFunction",            "com.therapy.handlers.therapists.GetTherapistHandler::handleRequest");
        Function deleteTherapistFn        = factory.create("DeleteTherapistFunction",         "com.therapy.handlers.therapists.DeleteTherapistHandler::handleRequest");
        Function updateTherapistFn        = factory.create("UpdateTherapistFunction",         "com.therapy.handlers.therapists.UpdateTherapistHandler::handleRequest");

        // Therapist Requests
        Function createTherapistRequestFn = factory.create("CreateTherapistRequestFunction",  "com.therapy.handlers.therapists.CreateTherapistRequestHandler::handleRequest");
        Function listTherapistRequestsFn  = factory.create("ListTherapistRequestsFunction",   "com.therapy.handlers.therapists.ListTherapistRequestsHandler::handleRequest");
        Function deleteTherapistRequestFn = factory.create("DeleteTherapistRequestFunction",  "com.therapy.handlers.therapists.DeleteTherapistRequestHandler::handleRequest");

        // Therapist Mappings
        Function listTherapistMappingsFn  = factory.create("ListTherapistMappingsFunction",   "com.therapy.handlers.therapists.ListTherapistMappingsHandler::handleRequest");
        Function deleteTherapistMappingFn = factory.create("DeleteTherapistMappingFunction",  "com.therapy.handlers.therapists.DeleteTherapistMappingHandler::handleRequest");

        // Therapist Messages
        Function createTherapistMessageFn = factory.create("CreateTherapistMessageFunction",  "com.therapy.handlers.therapists.CreateTherapistMessageHandler::handleRequest");
        Function listTherapistMessagesFn  = factory.create("ListTherapistMessagesFunction",   "com.therapy.handlers.therapists.ListTherapistMessagesHandler::handleRequest");

        // Therapist Sessions
        Function listTherapistSessionsFn  = factory.create("ListTherapistSessionsFunction",   "com.therapy.handlers.therapists.ListTherapistSessionsHandler::handleRequest");

        // Therapist Slots
        Function createSlotFn             = factory.create("CreateSlotFunction",               "com.therapy.handlers.therapists.CreateSlotHandler::handleRequest");
        Function listSlotsFn              = factory.create("ListSlotsFunction",                "com.therapy.handlers.therapists.ListSlotsHandler::handleRequest");
        Function deleteSlotFn             = factory.create("DeleteSlotFunction",               "com.therapy.handlers.therapists.DeleteSlotHandler::handleRequest");

        // Therapist Slot Requests
        Function listSlotRequestsFn       = factory.create("ListSlotRequestsFunction",         "com.therapy.handlers.therapists.ListSlotRequestsHandler::handleRequest");
        Function approveSessionRequestFn  = factory.create("ApproveSessionRequestFunction",    "com.therapy.handlers.therapists.ApproveSessionRequestHandler::handleRequest");

        // ──────────────────────────────────────────────
        // API GATEWAY
        // ──────────────────────────────────────────────

        RestApi api = RestApi.Builder.create(this, "TherapyApi")
                .restApiName("TherapyApi")
                .description("Therapy platform REST API")
                .build();

        // ── /clients ──
        Resource clients = api.getRoot().addResource("clients");
        clients.addMethod("POST", new LambdaIntegration(createClientFn));
        clients.addMethod("GET",  new LambdaIntegration(listClientsFn));

        // ── /clients/{clientId} ──
        Resource client = clients.addResource("{clientId}");
        client.addMethod("GET",    new LambdaIntegration(getClientFn));
        client.addMethod("DELETE", new LambdaIntegration(deleteClientFn));
        client.addMethod("PATCH",  new LambdaIntegration(updateClientFn));

        // ── /clients/{clientId}/requests ──
        Resource clientRequests = client.addResource("requests");
        clientRequests.addMethod("POST", new LambdaIntegration(createClientRequestFn));
        clientRequests.addMethod("GET",  new LambdaIntegration(listClientRequestsFn));

        // ── /clients/{clientId}/requests/{requestId} ──
        Resource clientRequest = clientRequests.addResource("{requestId}");
        clientRequest.addMethod("DELETE", new LambdaIntegration(deleteClientRequestFn));

        // ── /clients/{clientId}/mappings ──
        Resource clientMappings = client.addResource("mappings");
        clientMappings.addMethod("GET", new LambdaIntegration(listClientMappingsFn));

        // ── /clients/{clientId}/mappings/{mappingId} ──
        Resource clientMapping = clientMappings.addResource("{mappingId}");
        clientMapping.addMethod("DELETE", new LambdaIntegration(deleteClientMappingFn));

        // ── /clients/{clientId}/mappings/{mappingId}/messages ──
        Resource clientMessages = clientMapping.addResource("messages");
        clientMessages.addMethod("POST", new LambdaIntegration(createClientMessageFn));
        clientMessages.addMethod("GET",  new LambdaIntegration(listClientMessagesFn));

        // ── /clients/{clientId}/mappings/{mappingId}/slots ──
        Resource clientMappingSlots = clientMapping.addResource("slots");
        clientMappingSlots.addMethod("GET", new LambdaIntegration(listClientSlotsFn));

        // ── /clients/{clientId}/mappings/{mappingId}/slots/{slotId} ──
        Resource clientMappingSlot = clientMappingSlots.addResource("{slotId}");
        clientMappingSlot.addMethod("POST", new LambdaIntegration(bookSlotFn));

        // ── /clients/{clientId}/mappings/{mappingId}/journal-access ──
        Resource journalAccess = clientMapping.addResource("journal-access");
        journalAccess.addMethod("PATCH", new LambdaIntegration(updateJournalAccessFn));

        // ── /clients/{clientId}/sessions ──
        Resource clientSessions = client.addResource("sessions");
        clientSessions.addMethod("GET", new LambdaIntegration(listClientSessionsFn));

        // ── /therapists ──
        Resource therapists = api.getRoot().addResource("therapists");
        therapists.addMethod("POST", new LambdaIntegration(createTherapistFn));
        therapists.addMethod("GET",  new LambdaIntegration(listTherapistsFn));

        // ── /therapists/{therapistId} ──
        Resource therapist = therapists.addResource("{therapistId}");
        therapist.addMethod("GET",    new LambdaIntegration(getTherapistFn));
        therapist.addMethod("DELETE", new LambdaIntegration(deleteTherapistFn));
        therapist.addMethod("PATCH",  new LambdaIntegration(updateTherapistFn));

        // ── /therapists/{therapistId}/requests ──
        Resource therapistRequests = therapist.addResource("requests");
        therapistRequests.addMethod("POST", new LambdaIntegration(createTherapistRequestFn));
        therapistRequests.addMethod("GET",  new LambdaIntegration(listTherapistRequestsFn));

        // ── /therapists/{therapistId}/requests/{requestId} ──
        Resource therapistRequest = therapistRequests.addResource("{requestId}");
        therapistRequest.addMethod("DELETE", new LambdaIntegration(deleteTherapistRequestFn));

        // ── /therapists/{therapistId}/mappings ──
        Resource therapistMappings = therapist.addResource("mappings");
        therapistMappings.addMethod("GET", new LambdaIntegration(listTherapistMappingsFn));

        // ── /therapists/{therapistId}/mappings/{mappingId} ──
        Resource therapistMapping = therapistMappings.addResource("{mappingId}");
        therapistMapping.addMethod("DELETE", new LambdaIntegration(deleteTherapistMappingFn));

        // ── /therapists/{therapistId}/mappings/{mappingId}/messages ──
        Resource therapistMessages = therapistMapping.addResource("messages");
        therapistMessages.addMethod("POST", new LambdaIntegration(createTherapistMessageFn));
        therapistMessages.addMethod("GET",  new LambdaIntegration(listTherapistMessagesFn));

        // ── /therapists/{therapistId}/sessions ──
        Resource therapistSessions = therapist.addResource("sessions");
        therapistSessions.addMethod("GET", new LambdaIntegration(listTherapistSessionsFn));

        // ── /therapists/{therapistId}/slots ──
        Resource therapistSlots = therapist.addResource("slots");
        therapistSlots.addMethod("POST", new LambdaIntegration(createSlotFn));
        therapistSlots.addMethod("GET",  new LambdaIntegration(listSlotsFn));

        // ── /therapists/{therapistId}/slots/{slotId} ──
        Resource therapistSlot = therapistSlots.addResource("{slotId}");
        therapistSlot.addMethod("DELETE", new LambdaIntegration(deleteSlotFn));

        // ── /therapists/{therapistId}/slots/{slotId}/requests ──
        Resource slotRequests = therapistSlot.addResource("requests");
        slotRequests.addMethod("GET", new LambdaIntegration(listSlotRequestsFn));

        // ── /therapists/{therapistId}/slots/{slotId}/requests/{requestId} ──
        Resource slotRequest = slotRequests.addResource("{requestId}");
        slotRequest.addMethod("PATCH", new LambdaIntegration(approveSessionRequestFn));

    }
}
