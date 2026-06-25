package com.therapy.handlers.therapists;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.therapy.model.Session;
import com.therapy.model.SessionRequest;
import com.therapy.repository.SessionRepository;
import com.therapy.repository.SessionRequestRepository;
import com.therapy.repository.TherapistRepository;
import com.therapy.util.ResponseHelper;

public class ApproveSessionRequestHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final TherapistRepository      therapistRepository      = new TherapistRepository();
    private final SessionRepository        sessionRepository        = new SessionRepository();
    private final SessionRequestRepository sessionRequestRepository = new SessionRequestRepository();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request, Context context) {
        try {
            String therapistId = request.getPathParameters().get("therapistId");
            String slotId      = request.getPathParameters().get("slotId");
            String requestId   = request.getPathParameters().get("requestId");

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

            SessionRequest sessionRequest = sessionRequestRepository
                    .getSessionRequestById(requestId).orElse(null);
            if (sessionRequest == null) {
                return ResponseHelper.notFound("Session Request Not Found");
            }

            // Convert slot → confirmed session by setting clientId and updating type
            sessionRepository.confirmSession(slotId, sessionRequest.getClientId());

            // Clean up all requests for this slot since it's now booked
            sessionRequestRepository.getRequestsBySlotId(slotId)
                    .forEach(sr -> sessionRequestRepository.deleteSessionRequest(sr.getRequestId()));

            return ResponseHelper.ok(ResponseHelper.message("Session confirmed successfully"));

        } catch (Exception e) {
            return ResponseHelper.internalError("An unexpected error occurred");
        }
    }
}
