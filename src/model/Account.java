package model;

public class Account {
    private double balance;

    public Account(double balance) {
        this.balance = balance;
    }

    public double getBalance() { return balance; }
    public void deposit(double amount) { balance += amount; }
    public boolean withdraw(double amount) { 
        if (amount > 0 && balance >= amount) {
            balance -= amount; 
            return true;
        }
        return false;
    }
}