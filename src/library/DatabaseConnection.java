package library;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * DatabaseConnection - Public class for MySQL connectivity.
 * Activity 3 & 5 - Library Management System
 * Connection settings match the library_system database from Activity 3.
 */
public class DatabaseConnection {

    // ── Configuration ──────────────────────────────────────────────
    public static final String HOST     = "localhost";
    public static final String PORT     = "3306";
    public static final String DATABASE = "library_system";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "";          // change as needed

    private static final String URL =
        "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static Connection connection = null;

    // ── Singleton Connection ────────────────────────────────────────
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("[DB] Connected to MySQL: " + DATABASE);
            }
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                "MySQL JDBC Driver not found.\nAdd mysql-connector-j.jar to classpath.",
                "Driver Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Cannot connect to database.\n" + e.getMessage()
                + "\n\nCheck that MySQL is running and credentials are correct.",
                "Connection Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
