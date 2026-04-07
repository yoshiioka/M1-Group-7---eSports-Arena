package repository;

import database.DatabaseConnection;
import models.PurchasedTicket;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchasedTicketRepository {

    public boolean purchaseTicket(PurchasedTicket ticket) {
        String sql = "INSERT INTO purchased_tickets " +
                     "(ticket_no, spectator_id, ticket_type_id, seat_no, card_no, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?);";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ticket.getTicketNo());
            ps.setInt(2,    ticket.getSpectatorId());
            ps.setInt(3,    ticket.getTicketTypeId());
            ps.setInt(4,    ticket.getSeatNo());
            ps.setString(5, ticket.getCardNo());
            ps.setString(6, ticket.getStatus());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("[DB ERROR] purchaseTicket: " + e.getMessage());
        }
        return false;
    }

    public List<PurchasedTicket> getTicketsBySpectator(int spectatorId) {
        List<PurchasedTicket> list = new ArrayList<>();
        String sql =
            "SELECT pt.*, et.event_name, et.section, m.match_datetime, s.full_name " +
            "FROM purchased_tickets pt " +
            "JOIN event_tickets et ON pt.ticket_type_id = et.ticket_type_id " +
            "JOIN matches m        ON et.match_id = m.match_id " +
            "JOIN spectators s     ON pt.spectator_id = s.spectator_id " +
            "WHERE pt.spectator_id = ? " +
            "ORDER BY pt.purchase_id DESC;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, spectatorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[DB ERROR] getTicketsBySpectator: " + e.getMessage());
        }
        return list;
    }

    public PurchasedTicket getTicketByNo(String ticketNo) {
        String sql =
            "SELECT pt.*, et.event_name, et.section, m.match_datetime, s.full_name " +
            "FROM purchased_tickets pt " +
            "JOIN event_tickets et ON pt.ticket_type_id = et.ticket_type_id " +
            "JOIN matches m        ON et.match_id = m.match_id " +
            "JOIN spectators s     ON pt.spectator_id = s.spectator_id " +
            "WHERE pt.ticket_no = ?;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ticketNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            System.err.println("[DB ERROR] getTicketByNo: " + e.getMessage());
        }
        return null;
    }

    private PurchasedTicket mapRow(ResultSet rs) throws SQLException {
        PurchasedTicket pt = new PurchasedTicket();
        pt.setPurchaseId(rs.getInt("purchase_id"));
        pt.setTicketNo(rs.getString("ticket_no"));
        pt.setSpectatorId(rs.getInt("spectator_id"));
        pt.setTicketTypeId(rs.getInt("ticket_type_id"));
        pt.setSeatNo(rs.getInt("seat_no"));
        pt.setCardNo(rs.getString("card_no"));
        pt.setStatus(rs.getString("status"));
        pt.setPurchasedAt(rs.getString("purchased_at"));
        pt.setEventName(rs.getString("event_name"));
        pt.setSection(rs.getString("section"));
        pt.setMatchDatetime(rs.getString("match_datetime"));
        pt.setHolderName(rs.getString("full_name"));
        return pt;
    }
}
