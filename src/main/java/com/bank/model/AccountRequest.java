package com.bank.model;

import java.sql.Timestamp;

public class AccountRequest {
    private int id;
    private int customerId;
    private String accountType;
    private String status; // PENDING, APPROVED, REJECTED
    private Timestamp requestDate;
    private Integer approvedBy; // ID сотрудника
    private Timestamp approvedDate;
    
    public AccountRequest() {}
    
    public AccountRequest(int id, int customerId, String accountType, String status, 
                         Timestamp requestDate, Integer approvedBy, Timestamp approvedDate) {
        this.id = id;
        this.customerId = customerId;
        this.accountType = accountType;
        this.status = status;
        this.requestDate = requestDate;
        this.approvedBy = approvedBy;
        this.approvedDate = approvedDate;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Timestamp getRequestDate() { return requestDate; }
    public void setRequestDate(Timestamp requestDate) { this.requestDate = requestDate; }
    
    public Integer getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Integer approvedBy) { this.approvedBy = approvedBy; }
    
    public Timestamp getApprovedDate() { return approvedDate; }
    public void setApprovedDate(Timestamp approvedDate) { this.approvedDate = approvedDate; }
    
    @Override
    public String toString() {
        String statusIcon = switch(status) {
            case "PENDING" -> "⏳";
            case "APPROVED" -> "✅";
            case "REJECTED" -> "❌";
            default -> "❓";
        };
        return statusIcon + " " + accountType + " - " + status + " (" + requestDate + ")";
    }
}