package com.therapy.handlers.clients;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.therapy.model.Mapping;
import com.therapy.model.Session;
import com.therapy.repository.ClientRepository;
import com.therapy.repository.MappingRepository;
import com.therapy.repository.SessionRepository;
import com.therapy.util.ResponseHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class ListClientMappingSlotsHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ClientRepository  clientRepository  = new ClientRepository();
    private final MappingRepository mappingRepository = new MappingRepository();
    private final SessionRepository sessionRepository = new SessionRepository();
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

            // Fetch slots for the therapist linked to this mapping
            List<Session> slots = sessionRepository.getSlotsByTherapistId(mapping.getTherapistId());
            return ResponseHelper.ok(objectMapper.writeValueAsString(slots));

        } catch (Exception e) {
            return ResponseHelper.internalError("An unexpected error occurred");
        }
    }
}
