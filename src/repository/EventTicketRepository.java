package repository;

import database.DatabaseConnection;
import models.EventTicket;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventTicketRepository {

    public List<EventTicket> getAllEventTickets() {
        List<EventTicket> tickets = new ArrayList<>();
        String sql = "SELECT * FROM event_tickets ORDER BY ticket_type_id;";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tickets.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[DB ERROR] getAllEventTickets: " + e.getMessage());
        }
        return tickets;
    }

    public EventTicket getEventTicketById(int ticketTypeId) {
        String sql = "SELECT * FROM event_tickets WHERE ticket_type_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ticketTypeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            System.err.println("[DB ERROR] getEventTicketById: " + e.getMessage());
        }
        return null;
    }

    public boolean incrementSoldSeats(int ticketTypeId) {
        String sql = "UPDATE event_tickets SET sold_seats = sold_seats + 1 " +
                     "WHERE ticket_type_id = ? AND sold_seats < total_seats;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ticketTypeId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[DB ERROR] incrementSoldSeats: " + e.getMessage());
        }
        return false;
    }

    public int getNextSeatNumber(int ticketTypeId) {
        String sql = "SELECT sold_seats FROM event_tickets WHERE ticket_type_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ticketTypeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("sold_seats") + 1;

        } catch (SQLException e) {
            System.err.println("[DB ERROR] getNextSeatNumber: " + e.getMessage());
        }
        return 1;
    }

    private EventTicket mapRow(ResultSet rs) throws SQLException {
        return new EventTicket(
            rs.getInt("ticket_type_id"),
            rs.getInt("match_id"),
            rs.getString("event_name"),
            rs.getString("section"),
            rs.getDouble("price"),
            rs.getInt("total_seats"),
            rs.getInt("sold_seats")
        );
    }
}
