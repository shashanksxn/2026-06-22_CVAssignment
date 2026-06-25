package com.therapy.handlers.therapists;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.therapy.model.Therapist;
import com.therapy.repository.TherapistRepository;
import com.therapy.util.ResponseHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class UpdateTherapistHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final TherapistRepository therapistRepository = new TherapistRepository();
    private final ObjectMapper        objectMapper        = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request, Context context) {
        try {
            String    therapistId = request.getPathParameters().get("therapistId");
            Therapist existing    = therapistRepository.getTherapistById(therapistId).orElse(null);

            if (existing == null) {
                return ResponseHelper.notFound("Therapist Not Found");
            }

            Map<String, String> body     = objectMapper.readValue(request.getBody(), Map.class);
            String              name     = body.getOrDefault("name",     existing.getName());
            String              email    = body.getOrDefault("email",    existing.getEmail());
            String              password = body.getOrDefault("password", existing.getPassword());

            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                return ResponseHelper.badRequest("Invalid Fields");
            }

            existing.setName(name);
            existing.setEmail(email);
            existing.setPassword(password);

            therapistRepository.updateTherapist(existing);
            return ResponseHelper.ok(ResponseHelper.message("Changed Successfully"));

        } catch (Exception e) {
            return ResponseHelper.internalError("An unexpected error occurred");
        }
    }
}
