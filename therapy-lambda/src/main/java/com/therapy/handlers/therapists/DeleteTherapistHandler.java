package com.therapy.handlers.therapists;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.therapy.repository.TherapistRepository;
import com.therapy.util.ResponseHelper;

public class DeleteTherapistHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final TherapistRepository therapistRepository = new TherapistRepository();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request, Context context) {
        try {
            String therapistId = request.getPathParameters().get("therapistId");

            if (therapistRepository.getTherapistById(therapistId).isEmpty()) {
                return ResponseHelper.notFound("Therapist Not Found");
            }

            therapistRepository.deleteTherapist(therapistId);
            return ResponseHelper.ok(ResponseHelper.message("Deleted Successfully"));

        } catch (Exception e) {
            return ResponseHelper.internalError("An unexpected error occurred");
        }
    }
}
