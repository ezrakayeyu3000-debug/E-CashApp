package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
    // This creates a file named "bank.db" in your main project folder
    private static final String URL = "jdbc:sqlite:data/bank.db";

    public static Connection connect() {
        try {
            Class.forName("org.sqlite.JDBC"); // MUST BE HERE
            return DriverManager.getConnection(URL);
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC Driver not found.");
            return null;
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            return null;
        }
    }

    // This method creates the tables if they don't exist yet
    public static void initializeDatabase() {
        // UPGRADE 1: Added "pin TEXT" to the users table
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "username TEXT PRIMARY KEY, " +
                "pin TEXT NOT NULL, " +
                "balance REAL, " +
                "account_type TEXT" +
                ");";

        String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "description TEXT, " +
                "amount REAL, " +
                "timestamp TEXT" +
                ");";

        // UPGRADE 2: Safely check if connection is null before using it
        Connection conn = connect();
        if (conn == null) {
            System.out.println("Failed to initialize database: Connection is null.");
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createTransactionsTable);
        } catch (SQLException e) {
            System.out.println("Error creating tables: " + e.getMessage());
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                // Ignore close errors
            }
        }
    }
}