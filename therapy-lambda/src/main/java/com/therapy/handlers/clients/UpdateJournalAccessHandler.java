package com.therapy.handlers.clients;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.therapy.model.Mapping;
import com.therapy.repository.ClientRepository;
import com.therapy.repository.MappingRepository;
import com.therapy.util.ResponseHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class UpdateJournalAccessHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ClientRepository  clientRepository  = new ClientRepository();
    private final MappingRepository mappingRepository = new MappingRepository();
    private final ObjectMapper      objectMapper      = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request, Context context) {
        try {
            String clientId  = request.getPathParameters().get("clientId");
            String mappingId = request.getPathParameters().get("mappingId");

            if (clientRepository.getClientById(clientId).isEmpty()) {
                return ResponseHelper.notFound("Client Not Found");
            }

            Mapping mapping = mappingRepository.getMappingById(mappingId).orElse(null);
            if (mapping == null) {
                return ResponseHelper.notFound("Mapping Not Found");
            }

            if (!clientId.equals(mapping.getClientId())) {
                return ResponseHelper.unauthorized("Not Authorized");
            }

            Map<String, String> body          = objectMapper.readValue(request.getBody(), Map.class);
            String              journalAccess = body.get("journalAccess");

            if (journalAccess == null || (!journalAccess.equals("yes") && !journalAccess.equals("no"))) {
                return ResponseHelper.badRequest("Invalid Fields: journalAccess must be 'yes' or 'no'");
            }

            mappingRepository.updateJournalAccess(mappingId, journalAccess);
            return ResponseHelper.ok(ResponseHelper.message("Journal access updated successfully"));

        } catch (Exception e) {
            return ResponseHelper.internalError("An unexpected error occurred");
        }
    }
}
