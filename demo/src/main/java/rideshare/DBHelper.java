package rideshare;

import java.sql.*;
import java.util.Random;

import org.json.JSONObject;

public class DBHelper {
    private static final String URL = "jdbc:mysql://localhost:3306/rideshare";
    private static final String USER = "root"; // replace with your MySQL username
    private static final String PASSWORD = "12345"; // replace with your actual password

    public static int insertBooking(String username, String pickup, String destination) {
    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
        int confirmCode = 1000 + new Random().nextInt(9000); // generates 1000–9999
        PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO booking (rider_id, pickup_loc, dropoff_loc, confirm_code) " +
            "SELECT rider_id, ?, ?, ? FROM rider WHERE username=?",
            Statement.RETURN_GENERATED_KEYS
        );
        stmt.setString(1, pickup);
        stmt.setString(2, destination);
        stmt.setInt(3, confirmCode);
        stmt.setString(4, username);
        stmt.executeUpdate();

        ResultSet generatedKeys = stmt.getGeneratedKeys();
        if (generatedKeys.next()) {
                return generatedKeys.getInt(1); // booking_id
            } else {
                throw new SQLException("Booking creation failed, no ID obtained.");
            }

    } catch (SQLException e) {
        e.printStackTrace();
        return -1;
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

    public static JSONObject getBookingStatus(int bookingId) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            PreparedStatement stmt = conn.prepareStatement("SELECT booking_status, driver_id, confirm_code FROM booking WHERE booking_id = ?");
            stmt.setInt(1, bookingId);
            ResultSet rs = stmt.executeQuery();
            JSONObject result = new JSONObject();
            if (rs.next()) {
                result.put("status", rs.getString("booking_status"));
                result.put("driverId", rs.getInt("driver_id"));
                result.put("confirmCode", rs.getInt("confirm_code"));
            }

            return result;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

public static boolean cancelBooking(int bookingId) {
    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
        PreparedStatement stmt = conn.prepareStatement(
            "UPDATE booking SET booking_status = 'cancelled' WHERE booking_id = ? AND booking_status = 'pending'"
        );
        stmt.setInt(1, bookingId);
        stmt.executeUpdate();
        return true;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

}