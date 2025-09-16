package dao;

import model.Ristorante;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RistoranteDAO {

    // Inserimento 
    public int save(Ristorante r) throws SQLException {
        String sql = "INSERT INTO ristorante (partitaIva, nome, via, stelleMichelin) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, r.getPartitaIva());
            ps.setString(2, r.getNome());
            ps.setString(3, r.getVia());
            ps.setInt(4, r.getStelleMichelin());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    // Lettura singola per ID
    public Optional<Ristorante> findById(int id) throws SQLException {
        String sql = "SELECT * FROM ristorante WHERE idRistorante = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Ristorante r = new Ristorante(
                            rs.getString("via"),
                            rs.getString("partitaIva"),
                            rs.getString("nome")
                    );
                    r.setStelleMichelin(rs.getInt("stelleMichelin"));
                    return Optional.of(r);
                }
            }
        }
        return Optional.empty();
    }

    // Lettura tutti
    public List<Ristorante> getAll() throws SQLException {
        List<Ristorante> list = new ArrayList<>();
        String sql = "SELECT * FROM ristorante ORDER BY nome";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Ristorante r = new Ristorante(
                        rs.getString("via"),
                        rs.getString("partitaIva"),
                        rs.getString("nome")
                );
                r.setStelleMichelin(rs.getInt("stelleMichelin"));
                list.add(r);
            }
        }
        return list;
    }

    // Ricerca 
    public List<Ristorante> searchByNome(String partialNome) throws SQLException {
        List<Ristorante> list = new ArrayList<>();
        String sql = "SELECT * FROM ristorante WHERE nome ILIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + partialNome + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ristorante r = new Ristorante(
                            rs.getString("via"),
                            rs.getString("partitaIva"),
                            rs.getString("nome")
                    );
                    r.setStelleMichelin(rs.getInt("stelleMichelin"));
                    list.add(r);
                }
            }
        }
        return list;
    }

    // Aggiornamento tramite ID
    public void update(int id, Ristorante r) throws SQLException {
        String sql = "UPDATE ristorante SET partitaIva = ?, nome = ?, via = ?, stelleMichelin = ? WHERE idRistorante = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, r.getPartitaIva());
            ps.setString(2, r.getNome());
            ps.setString(3, r.getVia());
            ps.setInt(4, r.getStelleMichelin());
            ps.setInt(5, id);
            ps.executeUpdate();
        }
    }

    // Eliminazione tramite ID
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM ristorante WHERE idRistorante = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
    
    
 
    public Optional<Integer> findIdByPartitaIvaENome(String partitaIva, String nome) throws SQLException {
        String sql = "SELECT idRistorante FROM ristorante WHERE partitaIva = ? AND nome = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, partitaIva);
            ps.setString(2, nome);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getInt("idRistorante"));
                }
            }
        }
        return Optional.empty();
    }

}
