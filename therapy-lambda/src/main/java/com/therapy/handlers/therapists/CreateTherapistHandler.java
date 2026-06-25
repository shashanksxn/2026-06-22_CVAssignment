package com.therapy.handlers.therapists;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.therapy.model.Therapist;
import com.therapy.repository.TherapistRepository;
import com.therapy.util.IdGenerator;
import com.therapy.util.ResponseHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class CreateTherapistHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final TherapistRepository therapistRepository = new TherapistRepository();
    private final ObjectMapper        objectMapper        = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request, Context context) {
        try {
            Map<String, String> body     = objectMapper.readValue(request.getBody(), Map.class);
            String              name     = body.get("name");
            String              email    = body.get("email");
            String              password = body.get("password");

            if (name == null || email == null || password == null) {
                return ResponseHelper.badRequest("Fields Missing");
            }
            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                return ResponseHelper.badRequest("Invalid Fields");
            }

            if (therapistRepository.getTherapistByEmail(email).isPresent()) {
                return ResponseHelper.conflict("Email already exists");
            }

            String    therapistId = IdGenerator.therapistId();
            Therapist therapist   = new Therapist(therapistId, name, email, password);
            therapistRepository.createTherapist(therapist);

            return ResponseHelper.created(
                    ResponseHelper.created("New account created", therapistId)
            );

        } catch (Exception e) {
            return ResponseHelper.internalError("An unexpected error occurred");
        }
    }
}
