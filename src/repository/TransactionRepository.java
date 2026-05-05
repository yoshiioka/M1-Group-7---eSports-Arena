package repository;

import database.DatabaseConnection;
import models.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {
    
    public boolean saveTransaction(Transaction t) {
        String sql =
            "INSERT INTO transactions " +
            "(spectator_id, ticket_type_id, quantity, base_amount, vat_amount, " +
            " discount_amount, total_amount, payment_method, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1,    t.getSpectatorId());
            ps.setInt(2,    t.getTicketTypeId());
            ps.setInt(3,    t.getQuantity());
            ps.setDouble(4, t.getBaseAmount());
            ps.setDouble(5, t.getVatAmount());
            ps.setDouble(6, t.getDiscountAmount());
            ps.setDouble(7, t.getTotalAmount());
            ps.setString(8, t.getPaymentMethod());
            ps.setString(9, t.getStatus());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("[DB ERROR] saveTransaction: " + e.getMessage());
            return false;
        }
    }
    
    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql =
            "SELECT t.*, et.event_name, s.full_name " +
            "FROM transactions t " +
            "JOIN event_tickets et ON t.ticket_type_id = et.ticket_type_id " +
            "JOIN spectators    s  ON t.spectator_id   = s.spectator_id " +
            "ORDER BY t.transacted_at DESC;";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt   = conn.createStatement();
             ResultSet rs     = stmt.executeQuery(sql)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("[DB ERROR] getAllTransactions: " + e.getMessage());
        }
        return list;
    }
    
    public List<Transaction> getTransactionsBySpectator(int spectatorId) {
        List<Transaction> list = new ArrayList<>();
        String sql =
            "SELECT t.*, et.event_name, s.full_name " +
            "FROM transactions t " +
            "JOIN event_tickets et ON t.ticket_type_id = et.ticket_type_id " +
            "JOIN spectators    s  ON t.spectator_id   = s.spectator_id " +
            "WHERE t.spectator_id = ? " +
            "ORDER BY t.transacted_at DESC;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, spectatorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("[DB ERROR] getTransactionsBySpectator: " + e.getMessage());
        }
        return list;
    }
    
    public double getTotalRevenue()      { return sumColumn("total_amount");    }
    public double getTotalVat()          { return sumColumn("vat_amount");      }
    public double getTotalDiscount()     { return sumColumn("discount_amount"); }
    public double getTotalBaseRevenue()  { return sumColumn("base_amount");     }

    public int getTotalTicketsSold() {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM transactions WHERE status = 'SUCCESS';";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt   = conn.createStatement();
             ResultSet rs     = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DB ERROR] getTotalTicketsSold: " + e.getMessage());
        }
        return 0;
    }

    private double sumColumn(String column) {
        String sql = "SELECT COALESCE(SUM(" + column + "), 0) FROM transactions WHERE status = 'SUCCESS';";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt   = conn.createStatement();
             ResultSet rs     = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[DB ERROR] sumColumn(" + column + "): " + e.getMessage());
        }
        return 0.0;
    }
    
    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setSpectatorId(rs.getInt("spectator_id"));
        t.setTicketTypeId(rs.getInt("ticket_type_id"));
        t.setQuantity(rs.getInt("quantity"));
        t.setBaseAmount(rs.getDouble("base_amount"));
        t.setVatAmount(rs.getDouble("vat_amount"));
        t.setDiscountAmount(rs.getDouble("discount_amount"));
        t.setTotalAmount(rs.getDouble("total_amount"));
        t.setPaymentMethod(rs.getString("payment_method"));
        t.setStatus(rs.getString("status"));
        t.setTransactedAt(rs.getString("transacted_at"));
        t.setEventName(rs.getString("event_name"));
        t.setHolderName(rs.getString("full_name"));
        return t;
    }
}
