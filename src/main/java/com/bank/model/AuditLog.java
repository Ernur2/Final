package com.bank.model;

import java.sql.Timestamp;

public class AuditLog {
    private int id;
    private int userId;
    private String action;
    private String details;
    private Timestamp timestamp;
    
    public AuditLog() {}
    
    public AuditLog(int id, int userId, String action, String details, Timestamp timestamp) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.details = details;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    
    @Override
    public String toString() {
        return timestamp + " - " + action + ": " + details;
    }
}