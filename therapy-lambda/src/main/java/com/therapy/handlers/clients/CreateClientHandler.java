package com.therapy.handlers.clients;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.therapy.model.Client;
import com.therapy.repository.ClientRepository;
import com.therapy.util.IdGenerator;
import com.therapy.util.ResponseHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class CreateClientHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ClientRepository clientRepository = new ClientRepository();
    private final ObjectMapper     objectMapper     = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request, Context context) {
        try {
            // 1. Parse request body into a map
            Map<String, String> body = objectMapper.readValue(request.getBody(), Map.class);

            // 2. Validate required fields
            String name     = body.get("name");
            String email    = body.get("email");
            String password = body.get("password");

            if (name == null || email == null || password == null) {
                return ResponseHelper.badRequest("Fields Missing");
            }
            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                return ResponseHelper.badRequest("Invalid Fields");
            }

            // 3. Check email uniqueness
            if (clientRepository.getClientByEmail(email).isPresent()) {
                return ResponseHelper.conflict("Email already exists");
            }

            // 4. Create and save the new client
            String clientId = IdGenerator.clientId();
            Client client   = new Client(clientId, name, email, password);
            clientRepository.createClient(client);

            // 5. Return success
            return ResponseHelper.created(
                    ResponseHelper.created("New account created", clientId)
            );

        } catch (Exception e) {
            return ResponseHelper.internalError("An unexpected error occurred");
        }
    }
}
