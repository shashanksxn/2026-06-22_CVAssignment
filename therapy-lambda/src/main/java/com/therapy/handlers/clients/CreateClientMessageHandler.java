package com.therapy.handlers.clients;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.therapy.model.Mapping;
import com.therapy.model.Message;
import com.therapy.repository.ClientRepository;
import com.therapy.repository.MappingRepository;
import com.therapy.repository.MessageRepository;
import com.therapy.util.IdGenerator;
import com.therapy.util.ResponseHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;

public class CreateClientMessageHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ClientRepository  clientRepository  = new ClientRepository();
    private final MappingRepository mappingRepository = new MappingRepository();
    private final MessageRepository messageRepository = new MessageRepository();
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

            Map<String, String> body = objectMapper.readValue(request.getBody(), Map.class);
            String              text = body.get("body");

            if (text == null || text.isBlank()) {
                return ResponseHelper.badRequest("Fields Missing");
            }

            Message message = new Message(
                    IdGenerator.messageId(),
                    mappingId,
                    "client",
                    text,
                    Instant.now().toString()
            );
            messageRepository.createMessage(message);

            return ResponseHelper.created(
                    ResponseHelper.created("Message sent", message.getMessageId())
            );

        } catch (Exception e) {
            return ResponseHelper.internalError("An unexpected error occurred");
        }
    }
}
