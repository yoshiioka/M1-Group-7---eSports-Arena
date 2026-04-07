package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

           
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS spectators (" +
                "    spectator_id   INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    full_name      TEXT    NOT NULL," +
                "    email          TEXT    NOT NULL UNIQUE," +
                "    password       TEXT    NOT NULL," +
                "    created_at     TEXT    DEFAULT (datetime('now','localtime'))" +
                ");"
            );

           
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS matches (" +
                "    match_id       INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    game           TEXT    NOT NULL," +
                "    team_a         TEXT    NOT NULL," +
                "    team_b         TEXT    NOT NULL," +
                "    match_datetime TEXT    NOT NULL" +
                ");"
            );

            
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS event_tickets (" +
                "    ticket_type_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    match_id       INTEGER NOT NULL," +
                "    event_name     TEXT    NOT NULL," +
                "    section        TEXT    NOT NULL," +
                "    price          REAL    NOT NULL," +
                "    total_seats    INTEGER NOT NULL DEFAULT 50," +
                "    sold_seats     INTEGER NOT NULL DEFAULT 0," +
                "    FOREIGN KEY (match_id) REFERENCES matches(match_id)" +
                ");"
            );

           
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS purchased_tickets (" +
                "    purchase_id    INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    ticket_no      TEXT    NOT NULL UNIQUE," +
                "    spectator_id   INTEGER NOT NULL," +
                "    ticket_type_id INTEGER NOT NULL," +
                "    seat_no        INTEGER NOT NULL," +
                "    card_no        TEXT    NOT NULL," +
                "    status         TEXT    NOT NULL DEFAULT 'CONFIRMED'," +
                "    purchased_at   TEXT    DEFAULT (datetime('now','localtime'))," +
                "    FOREIGN KEY (spectator_id)   REFERENCES spectators(spectator_id)," +
                "    FOREIGN KEY (ticket_type_id) REFERENCES event_tickets(ticket_type_id)" +
                ");"
            );

           
            seedMatches(stmt);
            seedEventTickets(stmt);

            System.out.println("[DB] Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("[DB ERROR] Initialization failed: " + e.getMessage());
        }
    }

    
    private static void seedMatches(Statement stmt) throws SQLException {
        int count = stmt.executeQuery("SELECT COUNT(*) FROM matches;").getInt(1);
        if (count > 0) return;

        stmt.executeUpdate(
            "INSERT INTO matches (game, team_a, team_b, match_datetime) VALUES " +
            "('Valorant', 'Team Alpha',  'Team Omega', 'Apr 12, 2026 6:00 PM')," +
            "('DOTA',     'NightOwls',   'SkyForce',   'Apr 15, 2026 3:00 PM')," +
            "('MLBB',     'Iron Wolves', 'Phantom 5',  'Apr 18, 2026 7:30 PM');"
        );
    }

    private static void seedEventTickets(Statement stmt) throws SQLException {
        int count = stmt.executeQuery("SELECT COUNT(*) FROM event_tickets;").getInt(1);
        if (count > 0) return;

        stmt.executeUpdate(
            "INSERT INTO event_tickets (match_id, event_name, section, price, total_seats, sold_seats) VALUES " +
            "(1, 'Valorant Grand Finals',  'A',       350.0, 50,  0)," +
            "(2, 'DOTA Quarterfinals',     'B',       250.0, 50,  0)," +
            "(3, 'MLBB Open Division',     'General', 150.0, 50, 50);"
        );
    }
}
