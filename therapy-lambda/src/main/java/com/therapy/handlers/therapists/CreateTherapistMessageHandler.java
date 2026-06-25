package com.therapy.handlers.therapists;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.therapy.model.Mapping;
import com.therapy.model.Message;
import com.therapy.repository.MappingRepository;
import com.therapy.repository.MessageRepository;
import com.therapy.repository.TherapistRepository;
import com.therapy.util.IdGenerator;
import com.therapy.util.ResponseHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;

public class CreateTherapistMessageHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final TherapistRepository therapistRepository = new TherapistRepository();
    private final MappingRepository   mappingRepository   = new MappingRepository();
    private final MessageRepository   messageRepository   = new MessageRepository();
    private final ObjectMapper        objectMapper        = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request, Context context) {
        try {
            String therapistId = request.getPathParameters().get("therapistId");
            String mappingId   = request.getPathParameters().get("mappingId");

            if (therapistRepository.getTherapistById(therapistId).isEmpty()) {
                return ResponseHelper.notFound("Therapist Not Found");
            }

            Mapping mapping = mappingRepository.getMappingById(mappingId).orElse(null);
            if (mapping == null) {
                return ResponseHelper.notFound("Mapping Not Found");
            }

            if (!therapistId.equals(mapping.getTherapistId())) {
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
                    "therapist",
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
