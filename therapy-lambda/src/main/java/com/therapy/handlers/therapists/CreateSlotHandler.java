package com.therapy.handlers.therapists;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.therapy.model.Session;
import com.therapy.repository.SessionRepository;
import com.therapy.repository.TherapistRepository;
import com.therapy.util.IdGenerator;
import com.therapy.util.ResponseHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;

public class CreateSlotHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final TherapistRepository therapistRepository = new TherapistRepository();
    private final SessionRepository   sessionRepository   = new SessionRepository();
    private final ObjectMapper        objectMapper        = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request, Context context) {
        try {
            String therapistId = request.getPathParameters().get("therapistId");

            if (therapistRepository.getTherapistById(therapistId).isEmpty()) {
                return ResponseHelper.notFound("Therapist Not Found");
            }

            Map<String, String> body     = objectMapper.readValue(request.getBody(), Map.class);
            String              datetime = body.get("datetime");

            if (datetime == null || datetime.isBlank()) {
                return ResponseHelper.badRequest("Fields Missing");
            }

            // Validate that datetime is not in the past
            if (Instant.parse(datetime).isBefore(Instant.now())) {
                return ResponseHelper.badRequest("Invalid Time: time cannot be in the past");
            }

            // Check for duplicate slot at same datetime
            if (sessionRepository.slotExistsForTherapistAtDatetime(therapistId, datetime)) {
                return ResponseHelper.conflict("Entry already exists");
            }

            Session slot = new Session(
                    IdGenerator.sessionId(),
                    therapistId,
                    datetime,
                    "slot"
            );
            sessionRepository.createSlot(slot);

            return ResponseHelper.created(
                    ResponseHelper.created("Created Successfully", slot.getSessionId())
            );

        } catch (Exception e) {
            return ResponseHelper.internalError("An unexpected error occurred");
        }
    }
}
