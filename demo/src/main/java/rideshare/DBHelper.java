package rideshare;

import java.sql.*;

public class DBHelper {
    private static final String URL = "jdbc:mysql://localhost:3306/rideshare";
    private static final String USER = "root"; // replace with your MySQL username
    private static final String PASSWORD = "12345"; // replace with your actual password

    public static boolean insertDestination(String username, String destination) {
    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
        PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO test (username, destination) VALUES (?, ?)"
        );
        stmt.setString(1, username);
        stmt.setString(2, destination);
        stmt.executeUpdate();
        return true;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

    public static boolean riderExists(String username) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM rider WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // true if a row exists
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean driverExists(String username) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM driver WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // true if a row exists
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

public static boolean insertRider(String username, String name, String email, String phone, String payment) {
    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
        PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO rider (username, name, email, phone, payment_info) VALUES (?, ?, ?, ?, ?)"
        );
        stmt.setString(1, username);
        stmt.setString(2, name);
        stmt.setString(3, email);
        stmt.setString(4, phone);
        stmt.setString(5, payment);
        stmt.executeUpdate();
        return true;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

public static boolean insertDriver(String username, String name, String email, String phone, String bank, String vehicle) {
    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
        PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO driver (username, name, email, phone, bank_id, vehicle_type) VALUES (?, ?, ?, ?, ?, ?)"
        );
        stmt.setString(1, username);
        stmt.setString(2, name);
        stmt.setString(3, email);
        stmt.setString(4, phone);
        stmt.setString(5, bank);
        stmt.setString(6, vehicle);
        stmt.executeUpdate();
        return true;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

}