package com.therapy.handlers.clients;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.therapy.model.Client;
import com.therapy.repository.ClientRepository;
import com.therapy.util.ResponseHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class UpdateClientHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ClientRepository clientRepository = new ClientRepository();
    private final ObjectMapper     objectMapper     = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request, Context context) {
        try {
            String clientId = request.getPathParameters().get("clientId");

            Client existing = clientRepository.getClientById(clientId).orElse(null);
            if (existing == null) {
                return ResponseHelper.notFound("Client Not Found");
            }

            Map<String, String> body = objectMapper.readValue(request.getBody(), Map.class);

            // Only update fields that are provided; keep existing values otherwise
            String name     = body.getOrDefault("name",     existing.getName());
            String email    = body.getOrDefault("email",    existing.getEmail());
            String password = body.getOrDefault("password", existing.getPassword());

            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                return ResponseHelper.badRequest("Invalid Fields");
            }

            existing.setName(name);
            existing.setEmail(email);
            existing.setPassword(password);

            clientRepository.updateClient(existing);
            return ResponseHelper.ok(ResponseHelper.message("Changed Successfully"));

        } catch (Exception e) {
            return ResponseHelper.internalError("An unexpected error occurred");
        }
    }
}
