package rideshare;

import java.sql.*;

public class DBHelper {
    private static final String URL = "jdbc:mysql://localhost:3306/rideshare";
    private static final String USER = "root"; // replace with your MySQL username
    private static final String PASSWORD = "12345"; // replace with your actual password

    public static boolean insertDestination(String address) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO test (prop1, prop2, prop3) VALUES (?, ?, ?)")) {

            stmt.setInt(1, 34); // hardcoded rider_id for now
            stmt.setFloat(2, (float) 5.49);
            stmt.setString(3, address);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}