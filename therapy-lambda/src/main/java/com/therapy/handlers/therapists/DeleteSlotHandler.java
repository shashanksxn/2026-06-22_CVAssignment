package com.therapy.handlers.therapists;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.therapy.model.Session;
import com.therapy.repository.SessionRepository;
import com.therapy.repository.TherapistRepository;
import com.therapy.util.ResponseHelper;

public class DeleteSlotHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final TherapistRepository therapistRepository = new TherapistRepository();
    private final SessionRepository   sessionRepository   = new SessionRepository();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request, Context context) {
        try {
            String therapistId = request.getPathParameters().get("therapistId");
            String slotId      = request.getPathParameters().get("slotId");

            if (therapistRepository.getTherapistById(therapistId).isEmpty()) {
                return ResponseHelper.notFound("Therapist Not Found");
            }

            Session slot = sessionRepository.getSessionById(slotId).orElse(null);
            if (slot == null || !"slot".equals(slot.getType())) {
                return ResponseHelper.notFound("Slot Not Found");
            }

            if (!therapistId.equals(slot.getTherapistId())) {
                return ResponseHelper.unauthorized("Not Authorized");
            }

            sessionRepository.deleteSession(slotId);
            return ResponseHelper.ok(ResponseHelper.message("Deleted Successfully"));

        } catch (Exception e) {
            return ResponseHelper.internalError("An unexpected error occurred");
        }
    }
}
