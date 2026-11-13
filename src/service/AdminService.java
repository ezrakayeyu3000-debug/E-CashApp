package service;

import java.util.Scanner;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import util.DatabaseHelper;

public class AdminService {
    
    public void adminDashboard(Scanner scanner, String currentAdminUsername) {
        while (true) {
            System.out.println("\n--- ADMIN DASHBOARD ---");
            System.out.println("1. View All Users");
            System.out.println("2. Delete a User");
            System.out.println("3. Logout");
            System.out.print("Choose an option: ");
            
            // Use a try-catch to prevent crashing if the user types letters instead of numbers
            int choice = -1;
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // clear the bad input
                continue;
            }

            switch (choice) {
                case 1:
                    viewAllUsers();
                    break;
                case 2:
                    deleteUser(scanner, currentAdminUsername);
                    break;
                case 3:
                    System.out.println("Logging out of Admin Dashboard...");
                    return; 
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    // ACTUAL DATABASE QUERY FOR VIEWING USERS
    private void viewAllUsers() {
        String sql = "SELECT username, balance, account_type, is_admin FROM users";
        
        System.out.println("\n--- ALL REGISTERED USERS ---");
        System.out.printf("%-15s | %-10s | %-10s | %-5s%n", "Username", "Balance", "Type", "Admin");
        System.out.println("------------------------------------------------------");

        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                double balance = rs.getDouble("balance");
                String accType = rs.getString("account_type");
                boolean isAdmin = rs.getInt("is_admin") == 1;

                System.out.printf("%-15s | PHP %-6.2f | %-10s | %-5s%n", 
                        username, balance, accType, (isAdmin ? "YES" : "NO"));
            }
            System.out.println("------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println("Error fetching users: " + e.getMessage());
        }
    }

    // NEW: DELETE USER METHOD
    private void deleteUser(Scanner scanner, String currentAdminUsername) {
        System.out.print("\nEnter the username you want to delete: ");
        String targetUser = scanner.nextLine();

        // Prevent the admin from deleting their own account
        if (targetUser.equalsIgnoreCase(currentAdminUsername)) {
            System.out.println("Error: You cannot delete your own admin account!");
            return;
        }

        String sql = "DELETE FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, targetUser);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Successfully deleted user: " + targetUser);
            } else {
                System.out.println("User '" + targetUser + "' not found.");
            }

        } catch (SQLException e) {
            System.out.println("Error deleting user: " + e.getMessage());
        }
    }
}