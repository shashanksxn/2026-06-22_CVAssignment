package com.therapy.model;

public class Session {

    private String sessionId;
    private String therapistId;
    private String datetime;  // ISO-8601 string, e.g. "2024-06-25T10:00:00Z"
    private String type;      // 'slot' or 'session'
    private String clientId;  // null when type = 'slot', set when type = 'session'
    private String mark;      // optional rating/note, only on confirmed sessions

    // ── Constructors ──────────────────────────────

    public Session() {}

    // Constructor for creating a new slot (no clientId or mark yet)
    public Session(String sessionId, String therapistId, String datetime, String type) {
        this.sessionId   = sessionId;
        this.therapistId = therapistId;
        this.datetime    = datetime;
        this.type        = type;
    }

    // Constructor for a fully confirmed session
    public Session(String sessionId, String therapistId, String datetime,
                   String type, String clientId, String mark) {
        this.sessionId   = sessionId;
        this.therapistId = therapistId;
        this.datetime    = datetime;
        this.type        = type;
        this.clientId    = clientId;
        this.mark        = mark;
    }

    // ── Getters & Setters ─────────────────────────

    public String getSessionId()            { return sessionId; }
    public void   setSessionId(String v)    { this.sessionId = v; }

    public String getTherapistId()          { return therapistId; }
    public void   setTherapistId(String v)  { this.therapistId = v; }

    public String getDatetime()             { return datetime; }
    public void   setDatetime(String v)     { this.datetime = v; }

    public String getType()                 { return type; }
    public void   setType(String v)         { this.type = v; }

    public String getClientId()             { return clientId; }
    public void   setClientId(String v)     { this.clientId = v; }

    public String getMark()                 { return mark; }
    public void   setMark(String v)         { this.mark = v; }

    @Override
    public String toString() {
        return "Session{sessionId='" + sessionId + "', therapistId='" + therapistId +
               "', datetime='" + datetime + "', type='" + type + "', clientId='" + clientId + "'}";
    }
}
