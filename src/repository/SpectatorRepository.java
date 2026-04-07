package repository;

import database.DatabaseConnection;
import models.Spectator;

import java.sql.*;

public class SpectatorRepository {

    public boolean createAccount(Spectator spectator) {
        String sql = "INSERT INTO spectators (full_name, email, password) VALUES (?, ?, ?);";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, spectator.getFullName());
            ps.setString(2, spectator.getEmail());
            ps.setString(3, spectator.getPassword());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                System.out.println("[!] Email is already registered.");
            } else {
                System.err.println("[DB ERROR] createAccount: " + e.getMessage());
            }
            return false;
        }
    }
    public Spectator login(String email, String password) {
        String sql = "SELECT * FROM spectators WHERE email = ? AND password = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[DB ERROR] login: " + e.getMessage());
        }
        return null;
    }

    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM spectators WHERE email = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.err.println("[DB ERROR] emailExists: " + e.getMessage());
        }
        return false;
    }

    private Spectator mapRow(ResultSet rs) throws SQLException {
        return new Spectator(
            rs.getInt("spectator_id"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("created_at")
        );
    }
}
