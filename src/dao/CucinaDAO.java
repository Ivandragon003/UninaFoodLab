package dao;

import java.sql.*;
import util.DBConnection;
import java.util.*;

import model.Ricetta;

public class CucinaDAO {

    public void save(int idRicetta, int idSessione) throws SQLException {
       
        String sql = "INSERT INTO Cucina (idRicetta, idSessione) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRicetta);
            ps.setInt(2, idSessione);
            ps.executeUpdate();
        }
    }

    public void delete(int idRicetta, int idSessione) throws SQLException {
        String sql = "DELETE FROM Cucina WHERE idRicetta = ? AND idSessione = ?";
        try (Connection conn = DBConnection.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRicetta);
            ps.setInt(2, idSessione);
            ps.executeUpdate();
        }
    }

    public Map<Integer, Integer> getNumeroRicettePerSessioni(List<Integer> sessioniIds) throws SQLException {
        Map<Integer, Integer> result = new HashMap<>();

        if (sessioniIds == null || sessioniIds.isEmpty()) {
            return result;
        }

       
        for (Integer sessioneId : sessioniIds) {
            result.put(sessioneId, 0);
        }

        StringBuilder queryBuilder = new StringBuilder(
            "SELECT idSessione, COUNT(idRicetta) AS numRicette " +
            "FROM Cucina " +
            "WHERE idSessione IN (");

        for (int i = 0; i < sessioniIds.size(); i++) {
            queryBuilder.append("?");
            if (i < sessioniIds.size() - 1) {
                queryBuilder.append(",");
            }
        }
        queryBuilder.append(") GROUP BY idSessione");

        String query = queryBuilder.toString();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            for (int i = 0; i < sessioniIds.size(); i++) {
                ps.setInt(i + 1, sessioniIds.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int sessioneId = rs.getInt("idSessione");
                    int numRicette = rs.getInt("numRicette");
                    result.put(sessioneId, numRicette);
                }
            }
        }

        return result;
    }
    
    public Set<Ricetta> getRicettePerSessione(int idSessione) throws SQLException {
        Set<Ricetta> ricette = new HashSet<>();
        String sql = "SELECT r.* FROM ricetta r " +
                     "JOIN cucina c ON r.idricetta = c.idricetta " +
                     "WHERE c.idsessione = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSessione);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ricetta r = new Ricetta(
                        rs.getString("nome"),
                        rs.getInt("tempoPreparazione")
                    );
                    r.setIdRicetta(rs.getInt("idricetta"));
                    ricette.add(r);
                }
            }
        }
        return ricette;
    }

    
}
