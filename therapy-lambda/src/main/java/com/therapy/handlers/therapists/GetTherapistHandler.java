package com.therapy.handlers.therapists;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.therapy.model.Therapist;
import com.therapy.repository.TherapistRepository;
import com.therapy.util.ResponseHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GetTherapistHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final TherapistRepository therapistRepository = new TherapistRepository();
    private final ObjectMapper        objectMapper        = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request, Context context) {
        try {
            String    therapistId = request.getPathParameters().get("therapistId");
            Therapist therapist   = therapistRepository.getTherapistById(therapistId).orElse(null);

            if (therapist == null) {
                return ResponseHelper.notFound("Therapist Not Found");
            }

            therapist.setPassword(null);
            return ResponseHelper.ok(objectMapper.writeValueAsString(therapist));

        } catch (Exception e) {
            return ResponseHelper.internalError("An unexpected error occurred");
        }
    }
}
