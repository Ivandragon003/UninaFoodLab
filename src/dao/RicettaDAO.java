package dao;

import model.Ricetta;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RicettaDAO {

    // Inserimento 
	public int save(Ricetta r) throws SQLException {
	    String sql = "INSERT INTO ricetta (nome, tempoPreparazione) VALUES (?, ?)";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

	        ps.setString(1, r.getNome());
	        ps.setInt(2, r.getTempoPreparazione());
	        ps.executeUpdate();

	        try (ResultSet rs = ps.getGeneratedKeys()) {
	            if (rs.next()) {
	                int id = rs.getInt(1);
	                r.setIdRicetta(id); // solo qui, dopo l'inserimento
	                return id;
	            }
	        }
	    }
	    return -1;
	}


    // Lettura per ID
    public Optional<Ricetta> findById(int id) throws SQLException {
        String sql = "SELECT * FROM ricetta WHERE idRicetta = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Ricetta r = new Ricetta(
                            rs.getString("nome"),
                            rs.getInt("tempoPreparazione")
                    );
                    r.setIdRicetta(id);
                    return Optional.of(r);
                }
            }
        }
        return Optional.empty();
    }

    // Lettura tutte
    public List<Ricetta> getAll() throws SQLException {
        List<Ricetta> list = new ArrayList<>();
        String sql = "SELECT * FROM ricetta ORDER BY nome";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Ricetta r = new Ricetta(
                        rs.getString("nome"),
                        rs.getInt("tempoPreparazione")
                );
                r.setIdRicetta(rs.getInt("idRicetta"));
                list.add(r);
            }
        }
        return list;
    }

    // Ricerca per nome esatto
    public List<Ricetta> getByNome(String nome) throws SQLException {
        List<Ricetta> list = new ArrayList<>();
        String sql = "SELECT * FROM ricetta WHERE nome = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ricetta r = new Ricetta(
                            rs.getString("nome"),
                            rs.getInt("tempoPreparazione")
                    );
                    r.setIdRicetta(rs.getInt("idRicetta"));
                    list.add(r);
                }
            }
        }
        return list;
    }

    // Ricerca per nome parziale (LIKE)
    public List<Ricetta> searchByNome(String partialNome) throws SQLException {
        List<Ricetta> list = new ArrayList<>();
        String sql = "SELECT * FROM ricetta WHERE nome ILIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + partialNome + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ricetta r = new Ricetta(
                            rs.getString("nome"),
                            rs.getInt("tempoPreparazione")
                    );
                    r.setIdRicetta(rs.getInt("idRicetta"));
                    list.add(r);
                }
            }
        }
        return list;
    }

    // Aggiornamento tramite ID
    public void update(int id, Ricetta r) throws SQLException {
        String sql = "UPDATE ricetta SET nome = ?, tempoPreparazione = ? WHERE idRicetta = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, r.getNome());
            ps.setInt(2, r.getTempoPreparazione());
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    // Eliminazione tramite ID
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM ricetta WHERE idRicetta = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
