package dao;

import model.Ingrediente;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IngredienteDAO {

    // Inserimento 
	public int save(Ingrediente i) throws SQLException {
	    String sql = "INSERT INTO ingrediente (nome, tipo) VALUES (?, ?)";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

	        ps.setString(1, i.getNome());
	        ps.setString(2, i.getTipo());
	        ps.executeUpdate();

	        try (ResultSet rs = ps.getGeneratedKeys()) {
	            if (rs.next()) {
	                int id = rs.getInt(1);
	                i.setIdIngrediente(id); // solo qui, dopo l'inserimento
	                return id;
	            }
	        }
	    }
	    return -1;
	}


    // Lettura singola per ID
    public Optional<Ingrediente> findById(int id) throws SQLException {
        String sql = "SELECT * FROM ingrediente WHERE idIngrediente = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Ingrediente i = new Ingrediente(
                            rs.getString("nome"),
                            rs.getString("tipo")
                    );
                    i.setIdIngrediente(id);
                    return Optional.of(i);
                }
            }
        }
        return Optional.empty();
    }

    // Lettura tutte
    public List<Ingrediente> getAll() throws SQLException {
        List<Ingrediente> list = new ArrayList<>();
        String sql = "SELECT * FROM ingrediente ORDER BY nome";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Ingrediente i = new Ingrediente(
                        rs.getString("nome"),
                        rs.getString("tipo")
                );
                i.setIdIngrediente(rs.getInt("idIngrediente"));
                list.add(i);
            }
        }
        return list;
    }

    // Ricerca per nome esatto
    public List<Ingrediente> getByNome(String nome) throws SQLException {
        List<Ingrediente> list = new ArrayList<>();
        String sql = "SELECT * FROM ingrediente WHERE nome = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ingrediente i = new Ingrediente(
                            rs.getString("nome"),
                            rs.getString("tipo")
                    );
                    i.setIdIngrediente(rs.getInt("idIngrediente"));
                    list.add(i);
                }
            }
        }
        return list;
    }

    // Ricerca per tipo
    public List<Ingrediente> getByTipo(String tipo) throws SQLException {
        List<Ingrediente> list = new ArrayList<>();
        String sql = "SELECT * FROM ingrediente WHERE tipo = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tipo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ingrediente i = new Ingrediente(
                            rs.getString("nome"),
                            rs.getString("tipo")
                    );
                    i.setIdIngrediente(rs.getInt("idIngrediente"));
                    list.add(i);
                }
            }
        }
        return list;
    }

    // Aggiornamento tramite ID
    public void update(int id, Ingrediente i) throws SQLException {
        String sql = "UPDATE ingrediente SET nome = ?, tipo = ? WHERE idIngrediente = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, i.getNome());
            ps.setString(2, i.getTipo());
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    // Eliminazione tramite ID
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM ingrediente WHERE idIngrediente = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Eliminazione tramite oggetto
    public void delete(Ingrediente i) throws SQLException {
        delete(i.getIdIngrediente());
    }
}
