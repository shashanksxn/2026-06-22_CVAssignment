package com.therapy.handlers.clients;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.therapy.model.Mapping;
import com.therapy.repository.ClientRepository;
import com.therapy.repository.MappingRepository;
import com.therapy.repository.TherapistRepository;
import com.therapy.util.IdGenerator;
import com.therapy.util.ResponseHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class CreateClientRequestHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ClientRepository    clientRepository    = new ClientRepository();
    private final TherapistRepository therapistRepository = new TherapistRepository();
    private final MappingRepository   mappingRepository   = new MappingRepository();
    private final ObjectMapper        objectMapper        = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request, Context context) {
        try {
            String clientId = request.getPathParameters().get("clientId");

            if (clientRepository.getClientById(clientId).isEmpty()) {
                return ResponseHelper.notFound("Client Not Found");
            }

            Map<String, String> body        = objectMapper.readValue(request.getBody(), Map.class);
            String              therapistId = body.get("therapistId");

            if (therapistId == null || therapistId.isBlank()) {
                return ResponseHelper.badRequest("Fields Missing");
            }

            if (therapistRepository.getTherapistById(therapistId).isEmpty()) {
                return ResponseHelper.notFound("Therapist Not Found");
            }

            if (mappingRepository.mappingExistsForClient(clientId)) {
                return ResponseHelper.conflict("Mapping already exists");
            }

            Mapping mapping = new Mapping(
                    IdGenerator.mappingId(),
                    clientId,
                    therapistId,
                    "request",
                    "client",
                    "no"
            );
            mappingRepository.createMapping(mapping);

            return ResponseHelper.created(
                    ResponseHelper.created("Request created successfully", mapping.getMappingId())
            );

        } catch (Exception e) {
            return ResponseHelper.internalError("An unexpected error occurred");
        }
    }
}
