package dao;

import java.sql.*;

import util.DBConnection;
import java.util.*;


public class CucinaDAO {

    public void save(int idRicetta, int idSessione) throws SQLException {
        String sql = "INSERT INTO SessioneRicetta (idRicetta, idSessione) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRicetta);
            ps.setInt(2, idSessione);
            ps.executeUpdate();
        }
    }

    public void delete(int idRicetta, int idSessione) throws SQLException {
        String sql = "DELETE FROM SessioneRicetta WHERE idRicetta = ? AND idSessione = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRicetta);
            ps.setInt(2, idSessione);
            ps.executeUpdate();
        }
    }

    public int getNumeroRicettePerSessione(int idSessione) throws SQLException {
        String query = "SELECT COUNT(*) AS numRicette FROM usa WHERE idsessione = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idSessione);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("numRicette");
            }
        }
        return 0;
    }

   
    public Map<Integer, Integer> getNumeroRicettePerSessioni(List<Integer> sessioniIds) throws SQLException {
        Map<Integer, Integer> result = new HashMap<>();
        if (sessioniIds == null || sessioniIds.isEmpty()) return result;

        StringBuilder query = new StringBuilder(
                "SELECT idsessione, COUNT(*) AS numRicette FROM usa WHERE idsessione IN ("
        );
        for (int i = 0; i < sessioniIds.size(); i++) {
            query.append("?");
            if (i < sessioniIds.size() - 1) query.append(",");
        }
        query.append(") GROUP BY idsessione");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query.toString())) {
            for (int i = 0; i < sessioniIds.size(); i++) ps.setInt(i + 1, sessioniIds.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getInt("idsessione"), rs.getInt("numRicette"));
                }
            }
        }
        return result;
    }
}

