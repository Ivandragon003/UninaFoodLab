package model;

import util.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class TestDB {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Connessione riuscita!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Errore di connessione: " + e.getMessage());
            e.printStackTrace();
        }
    }
}