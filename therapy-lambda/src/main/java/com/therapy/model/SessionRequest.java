package com.therapy.model;

public class SessionRequest {

    private String requestId;
    private String slotId;
    private String clientId;
    private String timestamp; // when the request was made, ISO-8601 string

    // ── Constructors ──────────────────────────────

    public SessionRequest() {}

    public SessionRequest(String requestId, String slotId, String clientId, String timestamp) {
        this.requestId = requestId;
        this.slotId    = slotId;
        this.clientId  = clientId;
        this.timestamp = timestamp;
    }

    // ── Getters & Setters ─────────────────────────

    public String getRequestId()            { return requestId; }
    public void   setRequestId(String v)    { this.requestId = v; }

    public String getSlotId()               { return slotId; }
    public void   setSlotId(String v)       { this.slotId = v; }

    public String getClientId()             { return clientId; }
    public void   setClientId(String v)     { this.clientId = v; }

    public String getTimestamp()            { return timestamp; }
    public void   setTimestamp(String v)    { this.timestamp = v; }

    @Override
    public String toString() {
        return "SessionRequest{requestId='" + requestId + "', slotId='" + slotId +
               "', clientId='" + clientId + "', timestamp='" + timestamp + "'}";
    }
}
