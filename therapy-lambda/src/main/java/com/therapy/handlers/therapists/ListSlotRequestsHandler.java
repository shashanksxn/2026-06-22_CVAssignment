package com.therapy.handlers.therapists;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.therapy.model.Client;
import com.therapy.model.Session;
import com.therapy.model.SessionRequest;
import com.therapy.repository.ClientRepository;
import com.therapy.repository.SessionRepository;
import com.therapy.repository.SessionRequestRepository;
import com.therapy.repository.TherapistRepository;
import com.therapy.util.ResponseHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListSlotRequestsHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final TherapistRepository      therapistRepository      = new TherapistRepository();
    private final SessionRepository        sessionRepository        = new SessionRepository();
    private final SessionRequestRepository sessionRequestRepository = new SessionRequestRepository();
    private final ClientRepository         clientRepository         = new ClientRepository();
    private final ObjectMapper             objectMapper             = new ObjectMapper();

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

            // Get all session requests for this slot, then enrich with client name
            List<SessionRequest> sessionRequests = sessionRequestRepository.getRequestsBySlotId(slotId);
            List<Map<String, String>> result = new ArrayList<>();

            for (SessionRequest sr : sessionRequests) {
                Map<String, String> entry = new HashMap<>();
                entry.put("requestId", sr.getRequestId());
                entry.put("clientId",  sr.getClientId());

                // Look up client name
                clientRepository.getClientById(sr.getClientId())
                        .ifPresent(c -> entry.put("name", c.getName()));

                result.add(entry);
            }

            return ResponseHelper.ok(objectMapper.writeValueAsString(result));

        } catch (Exception e) {
            return ResponseHelper.internalError("An unexpected error occurred");
        }
    }
}
