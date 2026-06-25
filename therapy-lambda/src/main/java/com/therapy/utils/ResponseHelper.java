package com.therapy.util;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.Map;

public class ResponseHelper {

    private static final Map<String, String> HEADERS = Map.of(
            "Content-Type",                "application/json",
            "Access-Control-Allow-Origin", "*"   // allows browser frontends to call the API
    );

    private ResponseHelper() {}

    // ── Success responses ─────────────────────────────────────────────────

    public static APIGatewayProxyResponseEvent ok(String jsonBody) {
        return build(200, jsonBody);
    }

    public static APIGatewayProxyResponseEvent created(String jsonBody) {
        return build(201, jsonBody);
    }

    // ── Error responses ───────────────────────────────────────────────────

    public static APIGatewayProxyResponseEvent badRequest(String message) {
        return build(400, error(message));
    }

    public static APIGatewayProxyResponseEvent unauthorized(String message) {
        return build(401, error(message));
    }

    public static APIGatewayProxyResponseEvent notFound(String message) {
        return build(404, error(message));
    }

    public static APIGatewayProxyResponseEvent conflict(String message) {
        return build(409, error(message));
    }

    public static APIGatewayProxyResponseEvent internalError(String message) {
        return build(500, error(message));
    }

    // ── JSON body builders ────────────────────────────────────────────────

    // Wraps a plain message string into JSON: {"message": "..."}
    public static String message(String text) {
        return "{\"message\": \"" + escape(text) + "\"}";
    }

    // Wraps a created response with id: {"message": "...", "id": "..."}
    public static String created(String text, String id) {
        return "{\"message\": \"" + escape(text) + "\", \"id\": \"" + escape(id) + "\"}";
    }

    // Wraps an error string into JSON: {"error": "..."}
    private static String error(String text) {
        return "{\"error\": \"" + escape(text) + "\"}";
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private static APIGatewayProxyResponseEvent build(int statusCode, String body) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(HEADERS)
                .withBody(body);
    }

    // Escapes double quotes inside strings so JSON stays valid
    private static String escape(String text) {
        if (text == null) return "";
        return text.replace("\"", "\\\"");
    }
}
