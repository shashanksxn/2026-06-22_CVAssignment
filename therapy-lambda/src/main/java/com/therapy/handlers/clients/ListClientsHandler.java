package com.therapy.handlers.clients;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.therapy.model.Client;
import com.therapy.repository.ClientRepository;
import com.therapy.util.ResponseHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class ListClientsHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ClientRepository clientRepository = new ClientRepository();
    private final ObjectMapper     objectMapper     = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request, Context context) {
        try {
            List<Client> clients = clientRepository.getAllClients();

            // Strip passwords before returning
            clients.forEach(c -> c.setPassword(null));

            return ResponseHelper.ok(objectMapper.writeValueAsString(clients));

        } catch (Exception e) {
            return ResponseHelper.internalError("An unexpected error occurred");
        }
    }
}
