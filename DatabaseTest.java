import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.Locale; 

public class DatabaseTest {

    private static final String DB_HOST = "mysql-tictactoee-kakagastya2-ecdb.c.aivencloud.com";
    private static final String DB_PORT = "28606";
    private static final String DB_NAME = "defaultdb";
    private static final String DB_USER = "avnadmin";
    private static final String DB_PASS = "AVNS_bgOBYQNuctKG0ErN3KQ";

    public static void main(String[] args) {
        System.out.println("--- Starting Database Connection Test ---");

        System.out.println("\nTesting database connection...");
        if (testConnection()) {
            System.out.println("Connection successful!");
        } else {
            System.out.println("Connection failed. Exiting test.");
            return; 
        }

        System.out.println("\n--- Reading Data from 'gameuser' table ---");
        readUsersFromDatabase();

        System.out.println("\n--- Inserting New Data into 'game_user' table ---");
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter new username: ");
        String newUsername = scanner.nextLine();
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();
        System.out.print("Enter first name (optional): ");
        String newFirstName = scanner.nextLine();
        System.out.print("Enter last name (optional): ");
        String newLastName = scanner.nextLine();
        System.out.print("Enter sex (M/F - optional): ");
        String newSex = scanner.nextLine();

        insertNewUser(newUsername, newPassword, newFirstName, newLastName, newSex);

        System.out.println("\n--- Reading Data After Insertion ---");
        readUsersFromDatabase(); 

        System.out.println("\n--- Database Test Complete ---");
        scanner.close();
    }

    public static boolean testConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?sslmode=require",
                    DB_USER, DB_PASS)) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Error: MySQL JDBC Driver not found.");
            e.printStackTrace();
            return false;
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    public static void readUsersFromDatabase() {
        String sqlQuery = "SELECT username, firstname, lastname, sex, password FROM gameuser";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?sslmode=require",
                    DB_USER, DB_PASS);
                 PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                System.out.println("------------------------------------------------------------------------");
                System.out.printf("%-15s %-15s %-15s %-5s %-15s\n", "USERNAME", "FIRSTNAME", "LASTNAME", "SEX", "PASSWORD");
                System.out.println("------------------------------------------------------------------------");

                boolean foundUsers = false;
                while (resultSet.next()) {
                    foundUsers = true;
                    String username = resultSet.getString("username");
                    String firstname = resultSet.getString("firstname");
                    String lastname = resultSet.getString("lastname");
                    String sex = resultSet.getString("sex");
                    String password = resultSet.getString("password"); 
                    System.out.printf("%-15s %-15s %-15s %-5s %-15s\n", username, firstname, lastname, sex, password);
                }
                if (!foundUsers) {
                    System.out.println("No users found in the 'gameuser' table.");
                }
                System.out.println("------------------------------------------------------------------------");

            }
        } catch (ClassNotFoundException e) {
            System.err.println("Error: MySQL JDBC Driver not found. " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error reading from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void insertNewUser(String username, String password, String firstName, String lastName, String sex) {
       
        String sqlInsert = "INSERT INTO gameuser (username, password, firstname, lastname, sex) VALUES (?, ?, ?, ?, ?)";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?sslmode=require",
                    DB_USER, DB_PASS);
                 PreparedStatement preparedStatement = connection.prepareStatement(sqlInsert)) {

                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                preparedStatement.setString(3, firstName.isEmpty() ? null : firstName); 
                preparedStatement.setString(4, lastName.isEmpty() ? null : lastName); 
                preparedStatement.setString(5, sex.isEmpty() ? null : sex); 
                int rowsAffected = preparedStatement.executeUpdate(); 
                if (rowsAffected > 0) {
                    System.out.println("Successfully inserted new user: " + username);
                } else {
                    System.out.println("Failed to insert new user: " + username);
                }

            }
        } catch (ClassNotFoundException e) {
            System.err.println("Error: MySQL JDBC Driver not found. " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error inserting data into database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
