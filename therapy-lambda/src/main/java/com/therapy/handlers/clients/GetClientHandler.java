package com.therapy.handlers.clients;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.therapy.model.Client;
import com.therapy.repository.ClientRepository;
import com.therapy.util.ResponseHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

public class GetClientHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ClientRepository clientRepository = new ClientRepository();
    private final ObjectMapper     objectMapper     = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request, Context context) {
        try {
            // 1. Extract path parameter
            String clientId = request.getPathParameters().get("clientId");

            // 2. Look up in DynamoDB
            Optional<Client> result = clientRepository.getClientById(clientId);

            // 3. Return 404 if not found
            if (result.isEmpty()) {
                return ResponseHelper.notFound("Client Not Found");
            }

            // 4. Serialize client to JSON and return
            //    Strip password before returning — never expose it in responses
            Client client = result.get();
            client.setPassword(null);

            return ResponseHelper.ok(objectMapper.writeValueAsString(client));

        } catch (Exception e) {
            return ResponseHelper.internalError("An unexpected error occurred");
        }
    }
}
