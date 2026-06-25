package com.therapy.model;

public class Mapping {

    private String mappingId;
    private String clientId;
    private String therapistId;
    private String type;          // 'request' or 'mapping'
    private String requestedBy;   // 'client' or 'therapist'
    private String journalAccess; // 'yes' or 'no'

    // ── Constructors ──────────────────────────────

    public Mapping() {}

    public Mapping(String mappingId, String clientId, String therapistId,
                   String type, String requestedBy, String journalAccess) {
        this.mappingId     = mappingId;
        this.clientId      = clientId;
        this.therapistId   = therapistId;
        this.type          = type;
        this.requestedBy   = requestedBy;
        this.journalAccess = journalAccess;
    }

    // ── Getters & Setters ─────────────────────────

    public String getMappingId()              { return mappingId; }
    public void   setMappingId(String v)      { this.mappingId = v; }

    public String getClientId()               { return clientId; }
    public void   setClientId(String v)       { this.clientId = v; }

    public String getTherapistId()            { return therapistId; }
    public void   setTherapistId(String v)    { this.therapistId = v; }

    public String getType()                   { return type; }
    public void   setType(String v)           { this.type = v; }

    public String getRequestedBy()            { return requestedBy; }
    public void   setRequestedBy(String v)    { this.requestedBy = v; }

    public String getJournalAccess()          { return journalAccess; }
    public void   setJournalAccess(String v)  { this.journalAccess = v; }

    @Override
    public String toString() {
        return "Mapping{mappingId='" + mappingId + "', clientId='" + clientId +
               "', therapistId='" + therapistId + "', type='" + type + "'}";
    }
}
