import java.util.Scanner;
import java.util.List;
import service.AuthService;
import model.User;
import model.Transaction;
import util.DatabaseHelper;
import util.InputValidator;

public class Main {
    public static void main(String[] args) {
        // Initialize database on startup
        DatabaseHelper.initializeDatabase();

        Scanner scanner = new Scanner(System.in);
        AuthService authService = new AuthService();
        boolean isRunning = true;

        while (isRunning) {
            System.out.println("\n=================================");
            System.out.println("Welcome to EMS Cash!");
            System.out.println("=================================");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose: ");

            String choice = scanner.nextLine();

            if (choice.equals("1")) {
                System.out.print("Enter new username: ");
                String user = scanner.nextLine();
                System.out.print("Enter new PIN: ");
                String pin = scanner.nextLine();

                if (authService.register(user, pin)) {
                    System.out.println("Registration successful! You can now login.");
                } else {
                    System.out.println("Registration failed. Username might be taken.");
                }
            } 
            else if (choice.equals("2")) {
                System.out.print("Enter username: ");
                String user = scanner.nextLine();
                System.out.print("Enter PIN: ");
                String pin = scanner.nextLine();

                if (authService.login(user, pin)) {
                    System.out.println("Login successful!");
                    showDashboard(scanner, authService); 
                } else {
                    System.out.println("Invalid username or PIN.");
                }
            } 
            else if (choice.equals("3")) {
                System.out.println("Exiting...");
                isRunning = false;
            } else {
                System.out.println("Invalid choice.");
            }
        }
        scanner.close(); 
    }

    private static void showDashboard(Scanner scanner, AuthService authService) {
        User user = authService.getCurrentUser();
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n--- BANKING DASHBOARD ---");
            System.out.println("1. Check Balance");
            System.out.println("2. Cash-In (Deposit)");
            System.out.println("3. Transfer");
            System.out.println("4. View Transaction History");
            System.out.println("5. Change PIN"); // NEW OPTION
            System.out.println("6. Logout");     // SHIFTED DOWN
            System.out.print("Choose an option: ");
            
            String dashChoice = scanner.nextLine();

            switch (dashChoice) {
                case "1":
                    System.out.println("Current balance: PHP " + user.getAccount().getBalance());
                    break;
                    
                case "2":
                    double depositAmount = InputValidator.getValidDouble(scanner, "Enter amount to cash-in: PHP ");
                    
                    if (depositAmount > 0) {
                        user.getAccount().deposit(depositAmount);
                        authService.updateBalanceInDB(user); 
                        
                        Transaction t = new Transaction("Cash-In", depositAmount);
                        user.addTransaction(t);
                        authService.saveTransactionToDB(user.getUsername(), t);
                        
                        System.out.println("Deposited PHP " + depositAmount);
                        System.out.println("New Balance: PHP " + user.getAccount().getBalance());
                    } else {
                        System.out.println("Invalid amount.");
                    }
                    break;
                    
                case "3":
                    System.out.print("Enter recipient username: ");
                    String recipientName = scanner.nextLine();
                    
                    if (recipientName.equals(user.getUsername())) {
                        System.out.println("You cannot transfer money to yourself.");
                        break;
                    }

                    User recipient = authService.findUser(recipientName);
                    if (recipient == null) {
                        System.out.println("Recipient not found in the database.");
                        break;
                    }

                    double transferAmount = InputValidator.getValidDouble(scanner, "Enter amount to transfer: PHP ");

                    if (transferAmount <= 0) {
                        System.out.println("Invalid amount.");
                    } else if (user.getAccount().getBalance() < transferAmount) {
                        System.out.println("Insufficient funds. Your balance is PHP " + user.getAccount().getBalance());
                    } else {
                        user.getAccount().withdraw(transferAmount);
                        recipient.getAccount().deposit(transferAmount);

                        authService.updateBalanceInDB(user);
                        authService.updateBalanceInDB(recipient);

                        Transaction sent = new Transaction("Sent to " + recipientName, -transferAmount);
                        Transaction received = new Transaction("Received from " + user.getUsername(), transferAmount);

                        user.addTransaction(sent);
                        recipient.addTransaction(received);

                        authService.saveTransactionToDB(user.getUsername(), sent);
                        authService.saveTransactionToDB(recipient.getUsername(), received);

                        System.out.println("Successfully transferred PHP " + transferAmount + " to " + recipientName);
                        System.out.println("Your new balance: PHP " + user.getAccount().getBalance());
                    }
                    break;
                    
                case "4":
                    List<Transaction> history = authService.getTransactionHistoryFromDB(user.getUsername());
                    
                    System.out.println("\n--- TRANSACTION HISTORY ---");
                    if (history.isEmpty()) {
                        System.out.println("No transactions found.");
                    } else {
                        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        
                        for (Transaction t : history) {
                            String formattedTime = t.getTimestamp().format(formatter);
                            System.out.println("[" + formattedTime + "] " + t.getType() + " | PHP " + t.getAmount());
                        }
                    }
                    break;
                    
                case "5":
                    // NEW: CHANGE PIN LOGIC
                    System.out.print("Enter current PIN: ");
                    String oldPin = scanner.nextLine();
                    
                    if (!oldPin.equals(user.getPin())) {
                        System.out.println("Incorrect current PIN.");
                        break;
                    }
                    
                    System.out.print("Enter new PIN: ");
                    String newPin = scanner.nextLine();
                    
                    if (authService.updatePinInDB(user.getUsername(), newPin)) {
                        user.setPin(newPin); // Updates the current session
                        System.out.println("PIN changed successfully!");
                    } else {
                        System.out.println("Failed to change PIN.");
                    }
                    break;
                    
                case "6":
                    authService.logout();
                    loggedIn = false;
                    break;
                    
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
}