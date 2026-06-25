package com.therapy.model;

public class Therapist {

    private String therapistId;
    private String name;
    private String email;
    private String password;

    // ── Constructors ──────────────────────────────

    public Therapist() {}

    public Therapist(String therapistId, String name, String email, String password) {
        this.therapistId = therapistId;
        this.name        = name;
        this.email       = email;
        this.password    = password;
    }

    // ── Getters & Setters ─────────────────────────

    public String getTherapistId()          { return therapistId; }
    public void   setTherapistId(String v)  { this.therapistId = v; }

    public String getName()                 { return name; }
    public void   setName(String v)         { this.name = v; }

    public String getEmail()                { return email; }
    public void   setEmail(String v)        { this.email = v; }

    public String getPassword()             { return password; }
    public void   setPassword(String v)     { this.password = v; }

    @Override
    public String toString() {
        return "Therapist{therapistId='" + therapistId + "', name='" + name + "', email='" + email + "'}";
    }
}
