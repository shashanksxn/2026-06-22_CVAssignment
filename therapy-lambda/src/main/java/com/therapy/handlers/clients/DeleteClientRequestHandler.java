package com.therapy.handlers.clients;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.therapy.model.Mapping;
import com.therapy.repository.ClientRepository;
import com.therapy.repository.MappingRepository;
import com.therapy.util.ResponseHelper;

public class DeleteClientRequestHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ClientRepository  clientRepository  = new ClientRepository();
    private final MappingRepository mappingRepository = new MappingRepository();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request, Context context) {
        try {
            String clientId  = request.getPathParameters().get("clientId");
            String requestId = request.getPathParameters().get("requestId");

            if (clientRepository.getClientById(clientId).isEmpty()) {
                return ResponseHelper.notFound("Client Not Found");
            }

            Mapping mapping = mappingRepository.getMappingById(requestId).orElse(null);
            if (mapping == null) {
                return ResponseHelper.notFound("Request Not Found");
            }

            if ("mapping".equals(mapping.getType())) {
                return ResponseHelper.badRequest("Invalid request");
            }

            if (!clientId.equals(mapping.getClientId())) {
                return ResponseHelper.unauthorized("Not Authorized");
            }

            mappingRepository.deleteMapping(requestId);
            return ResponseHelper.ok(ResponseHelper.message("Deleted Successfully"));

        } catch (Exception e) {
            return ResponseHelper.internalError("An unexpected error occurred");
        }
    }
}
