package com.therapy.util;

import java.util.UUID;

public class IdGenerator {

    private IdGenerator() {}

    // ── ID generators per entity ──────────────────────────────────────────

    public static String clientId() {
        return "CLT-" + shortUuid();
    }

    public static String therapistId() {
        return "THR-" + shortUuid();
    }

    public static String mappingId() {
        return "MAP-" + shortUuid();
    }

    public static String messageId() {
        return "MSG-" + shortUuid();
    }

    public static String sessionId() {
        return "SES-" + shortUuid();
    }

    public static String sessionRequestId() {
        return "REQ-" + shortUuid();
    }

    // ── Private helper ────────────────────────────────────────────────────

    // Takes the first 8 characters of a UUID, e.g. "a3f8c2d1"
    private static String shortUuid() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
