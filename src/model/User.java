package model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String pin;
    private Account account;
    private List<Transaction> transactionHistory;

    public User(String username, String pin, double balance, String accountType) {
        this.username = username;
        this.pin = pin;
        this.account = new Account(balance); // Creates the account with the DB balance
        this.transactionHistory = new ArrayList<>();
    }

    public String getUsername() { return username; }
    public String getPin() { return pin; }
    public Account getAccount() { return account; }
    public List<Transaction> getTransactionHistory() { return transactionHistory; }
    
    public void addTransaction(Transaction t) {
        this.transactionHistory.add(t);
    }

    // --- NEW METHOD ---
    public void setPin(String pin) {
        this.pin = pin;
    }
}