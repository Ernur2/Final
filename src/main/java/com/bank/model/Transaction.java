package com.bank.model;

import java.sql.Timestamp;

public class Transaction {
    private int id;
    private int accountId;
    private String transactionType;
    private double amount;
    private String description;
    private Timestamp transactionDate;
    
    public Transaction() {}
    
    public Transaction(int id, int accountId, String transactionType, double amount, 
                      String description, Timestamp transactionDate) {
        this.id = id;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
        this.transactionDate = transactionDate;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Timestamp getTransactionDate() { return transactionDate; }
    public void setTransactionDate(Timestamp transactionDate) { this.transactionDate = transactionDate; }
    
    @Override
    public String toString() {
        String type = switch(transactionType) {
            case "Deposit" -> "➕ Пополнение";
            case "Withdraw" -> "➖ Снятие";
            case "Transfer Out" -> "↗️ Перевод (отправлено)";
            case "Transfer In" -> "↙️ Перевод (получено)";
            default -> transactionType;
        };
        
        return type + ": $" + String.format("%.2f", amount) + 
               " - " + transactionDate + 
               (description != null && !description.isEmpty() ? " (" + description + ")" : "");
    }
}