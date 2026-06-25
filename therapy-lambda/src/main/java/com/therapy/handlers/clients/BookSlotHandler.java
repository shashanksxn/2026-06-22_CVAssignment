package com.therapy.handlers.clients;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.therapy.model.Mapping;
import com.therapy.model.Session;
import com.therapy.model.SessionRequest;
import com.therapy.repository.ClientRepository;
import com.therapy.repository.MappingRepository;
import com.therapy.repository.SessionRepository;
import com.therapy.repository.SessionRequestRepository;
import com.therapy.util.IdGenerator;
import com.therapy.util.ResponseHelper;

import java.time.Instant;

public class BookSlotHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ClientRepository         clientRepository         = new ClientRepository();
    private final MappingRepository        mappingRepository        = new MappingRepository();
    private final SessionRepository        sessionRepository        = new SessionRepository();
    private final SessionRequestRepository sessionRequestRepository = new SessionRequestRepository();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request, Context context) {
        try {
            String clientId  = request.getPathParameters().get("clientId");
            String mappingId = request.getPathParameters().get("mappingId");
            String slotId    = request.getPathParameters().get("slotId");

            if (clientRepository.getClientById(clientId).isEmpty()) {
                return ResponseHelper.notFound("Client Not Found");
            }

            Mapping mapping = mappingRepository.getMappingById(mappingId).orElse(null);
            if (mapping == null) {
                return ResponseHelper.notFound("Mapping Not Found");
            }

            if (!clientId.equals(mapping.getClientId())) {
                return ResponseHelper.unauthorized("Not Authorized");
            }

            Session slot = sessionRepository.getSessionById(slotId).orElse(null);
            if (slot == null || !"slot".equals(slot.getType())) {
                return ResponseHelper.notFound("Slot Not Found");
            }

            SessionRequest sessionRequest = new SessionRequest(
                    IdGenerator.sessionRequestId(),
                    slotId,
                    clientId,
                    Instant.now().toString()
            );
            sessionRequestRepository.createSessionRequest(sessionRequest);

            return ResponseHelper.created(
                    ResponseHelper.created("Session request created", sessionRequest.getRequestId())
            );

        } catch (Exception e) {
            return ResponseHelper.internalError("An unexpected error occurred");
        }
    }
}
