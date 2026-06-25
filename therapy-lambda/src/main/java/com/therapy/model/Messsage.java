package com.therapy.model;

public class Message {

    private String messageId;
    private String mappingId;
    private String sender;    // 'client' or 'therapist'
    private String body;
    private String timestamp; // ISO-8601 string, e.g. "2024-06-25T10:30:00Z"

    // ── Constructors ──────────────────────────────

    public Message() {}

    public Message(String messageId, String mappingId, String sender,
                   String body, String timestamp) {
        this.messageId  = messageId;
        this.mappingId  = mappingId;
        this.sender     = sender;
        this.body       = body;
        this.timestamp  = timestamp;
    }

    // ── Getters & Setters ─────────────────────────

    public String getMessageId()            { return messageId; }
    public void   setMessageId(String v)    { this.messageId = v; }

    public String getMappingId()            { return mappingId; }
    public void   setMappingId(String v)    { this.mappingId = v; }

    public String getSender()               { return sender; }
    public void   setSender(String v)       { this.sender = v; }

    public String getBody()                 { return body; }
    public void   setBody(String v)         { this.body = v; }

    public String getTimestamp()            { return timestamp; }
    public void   setTimestamp(String v)    { this.timestamp = v; }

    @Override
    public String toString() {
        return "Message{messageId='" + messageId + "', mappingId='" + mappingId +
               "', sender='" + sender + "', timestamp='" + timestamp + "'}";
    }
}
