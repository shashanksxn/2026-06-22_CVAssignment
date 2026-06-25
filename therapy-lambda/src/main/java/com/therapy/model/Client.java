package com.therapy.model;

public class Client {

    private String clientId;
    private String name;
    private String email;
    private String password;

    // ── Constructors ──────────────────────────────

    public Client() {}

    public Client(String clientId, String name, String email, String password) {
        this.clientId = clientId;
        this.name     = name;
        this.email    = email;
        this.password = password;
    }

    // ── Getters & Setters ─────────────────────────

    public String getClientId()             { return clientId; }
    public void   setClientId(String v)     { this.clientId = v; }

    public String getName()                 { return name; }
    public void   setName(String v)         { this.name = v; }

    public String getEmail()                { return email; }
    public void   setEmail(String v)        { this.email = v; }

    public String getPassword()             { return password; }
    public void   setPassword(String v)     { this.password = v; }

    @Override
    public String toString() {
        return "Client{clientId='" + clientId + "', name='" + name + "', email='" + email + "'}";
    }
}
