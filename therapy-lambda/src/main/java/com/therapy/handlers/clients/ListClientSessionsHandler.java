package com.therapy.handlers.clients;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.therapy.model.Session;
import com.therapy.repository.ClientRepository;
import com.therapy.repository.SessionRepository;
import com.therapy.util.ResponseHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class ListClientSessionsHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ClientRepository  clientRepository  = new ClientRepository();
    private final SessionRepository sessionRepository = new SessionRepository();
    private final ObjectMapper      objectMapper      = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request, Context context) {
        try {
            String clientId = request.getPathParameters().get("clientId");

            if (clientRepository.getClientById(clientId).isEmpty()) {
                return ResponseHelper.notFound("Client Not Found");
            }

            List<Session> sessions = sessionRepository.getSessionsByClientId(clientId);
            return ResponseHelper.ok(objectMapper.writeValueAsString(sessions));

        } catch (Exception e) {
            return ResponseHelper.internalError("An unexpected error occurred");
        }
    }
}
