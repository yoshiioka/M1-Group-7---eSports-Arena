package repository;

import database.DatabaseConnection;
import models.Match;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatchRepository {

    public List<Match> getAllMatches() {
        List<Match> matches = new ArrayList<>();
        String sql = "SELECT * FROM matches ORDER BY match_id;";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {

            while (rs.next()) {
                matches.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[DB ERROR] getAllMatches: " + e.getMessage());
        }
        return matches;
    }

    public Match getMatchById(int matchId) {
        String sql = "SELECT * FROM matches WHERE match_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, matchId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            System.err.println("[DB ERROR] getMatchById: " + e.getMessage());
        }
        return null;
    }

    private Match mapRow(ResultSet rs) throws SQLException {
        return new Match(
            rs.getInt("match_id"),
            rs.getString("game"),
            rs.getString("team_a"),
            rs.getString("team_b"),
            rs.getString("match_datetime")
        );
    }
}
