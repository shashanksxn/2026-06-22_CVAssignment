package com.therapy.handlers.therapists;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.therapy.model.Mapping;
import com.therapy.repository.MappingRepository;
import com.therapy.repository.TherapistRepository;
import com.therapy.util.ResponseHelper;

public class DeleteTherapistRequestHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final TherapistRepository therapistRepository = new TherapistRepository();
    private final MappingRepository   mappingRepository   = new MappingRepository();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request, Context context) {
        try {
            String therapistId = request.getPathParameters().get("therapistId");
            String requestId   = request.getPathParameters().get("requestId");

            if (therapistRepository.getTherapistById(therapistId).isEmpty()) {
                return ResponseHelper.notFound("Therapist Not Found");
            }

            Mapping mapping = mappingRepository.getMappingById(requestId).orElse(null);
            if (mapping == null) {
                return ResponseHelper.notFound("Request Not Found");
            }

            if ("mapping".equals(mapping.getType())) {
                return ResponseHelper.badRequest("Invalid request");
            }

            if (!therapistId.equals(mapping.getTherapistId())) {
                return ResponseHelper.unauthorized("Not Authorized");
            }

            mappingRepository.deleteMapping(requestId);
            return ResponseHelper.ok(ResponseHelper.message("Deleted Successfully"));

        } catch (Exception e) {
            return ResponseHelper.internalError("An unexpected error occurred");
        }
    }
}
