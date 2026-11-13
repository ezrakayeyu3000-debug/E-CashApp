package model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String pin;
    private Account account;
    private List<Transaction> transactionHistory;
    private boolean isAdmin; // NEW: To track if the user is an admin

    // UPDATED: Added boolean isAdmin to the constructor
    public User(String username, String pin, double balance, String accountType, boolean isAdmin) {
        this.username = username;
        this.pin = pin;
        this.account = new Account(balance); 
        this.transactionHistory = new ArrayList<>();
        this.isAdmin = isAdmin; 
    }

    public String getUsername() { return username; }
    public String getPin() { return pin; }
    public Account getAccount() { return account; }
    public List<Transaction> getTransactionHistory() { return transactionHistory; }
    
    // NEW: Getter to check if the user is an admin
    public boolean isAdmin() { 
        return isAdmin; 
    }
    
    public void addTransaction(Transaction t) {
        this.transactionHistory.add(t);
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}