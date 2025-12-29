package com.bank.model;

public class Account {
    private int id;
    private String accountNumber;
    private int customerId;
    private double balance;
    private String accountType;
    
    public Account() {}
    
    public Account(int id, String accountNumber, int customerId, double balance, String accountType) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.balance = balance;
        this.accountType = accountType;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    
    @Override
    public String toString() {
        return accountNumber + " - Баланс: $" + String.format("%.2f", balance);
    }
}