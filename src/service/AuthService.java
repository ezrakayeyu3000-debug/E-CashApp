package service;

import model.User;
import model.Transaction;
import util.DatabaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuthService {
    private User currentUser;

    public boolean register(String username, String pin) {
        String sql = "INSERT INTO users(username, pin, balance, account_type) VALUES(?,?,?,?)";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, pin);
            pstmt.setDouble(3, 0.0); 
            pstmt.setString(4, "Savings"); 
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            return false; 
        }
    }

    public boolean login(String username, String pin) {
        String sql = "SELECT * FROM users WHERE username = ? AND pin = ?";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, pin);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double balance = rs.getDouble("balance");
                String accType = rs.getString("account_type");
                currentUser = new User(username, pin, balance, accType); 
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }
        return false;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }

    public void updateBalanceInDB(User user) {
        String sql = "UPDATE users SET balance = ? WHERE username = ?";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, user.getAccount().getBalance());
            pstmt.setString(2, user.getUsername());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.out.println("Error updating balance: " + e.getMessage());
        }
    }

    public void saveTransactionToDB(String username, Transaction t) {
        String sql = "INSERT INTO transactions(username, description, amount, timestamp) VALUES(?,?,?,?)";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, t.getType());
            pstmt.setDouble(3, t.getAmount());
            pstmt.setString(4, t.getTimestamp().toString()); 
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.out.println("Error saving transaction: " + e.getMessage());
        }
    }

    public User findUser(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double balance = rs.getDouble("balance");
                String accType = rs.getString("account_type");
                String pin = rs.getString("pin");
                
                return new User(username, pin, balance, accType);
            }
        } catch (SQLException e) {
            System.out.println("Error finding user: " + e.getMessage());
        }
        return null; 
    }

    public List<Transaction> getTransactionHistoryFromDB(String username) {
        List<Transaction> history = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE username = ? ORDER BY id DESC";
        
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String description = rs.getString("description");
                double amount = rs.getDouble("amount");
                
                String timeString = rs.getString("timestamp");
                java.time.LocalDateTime time = java.time.LocalDateTime.parse(timeString);
                
                history.add(new Transaction(description, amount, time));
            }
        } catch (SQLException e) {
            System.out.println("Error loading history: " + e.getMessage());
        }
        return history;
    }

    // --- NEW METHOD: UPDATE PIN ---
    public boolean updatePinInDB(String username, String newPin) {
        String sql = "UPDATE users SET pin = ? WHERE username = ?";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newPin);
            pstmt.setString(2, username);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
            
        } catch (SQLException e) {
            System.out.println("Error updating PIN: " + e.getMessage());
            return false;
        }
    }
}