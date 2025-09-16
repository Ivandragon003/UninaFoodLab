package dao;

import model.Ricetta;
import model.Ingrediente;
import util.DBConnection;

import java.sql.*;
import java.util.*;

public class RicettaDAO {

    // Inserimento di una nuova ricetta
    public void save(Ricetta r) throws SQLException {
        String sql = "INSERT INTO ricetta (nome, tempoPreparazione) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, r.getNome());
            ps.setInt(2, r.getTempoPreparazione());
            ps.executeUpdate();
        }
    }

    // Lettura di una ricetta per ID
    public Optional<Ricetta> findById(int id) throws SQLException {
        String sql = "SELECT * FROM ricetta WHERE idRicetta = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Ricetta r = mapResultSetToRicetta(rs);
                    return Optional.of(r);
                }
            }
        }
        return Optional.empty();
    }

    // Lettura di tutte le ricette
    public List<Ricetta> getAll() throws SQLException {
        List<Ricetta> list = new ArrayList<>();
        String sql = "SELECT * FROM ricetta ORDER BY nome";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Ricetta r = mapResultSetToRicetta(rs);
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
                    Ricetta r = mapResultSetToRicetta(rs);
                    list.add(r);
                }
            }
        }
        return list;
    }

    // Ricerca per nome parziale 
    public List<Ricetta> searchByNome(String partialNome) throws SQLException {
        List<Ricetta> list = new ArrayList<>();
        String sql = "SELECT * FROM ricetta WHERE nome ILIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + partialNome + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ricetta r = mapResultSetToRicetta(rs);
                    list.add(r);
                }
            }
        }
        return list;
    }

    // Aggiornamento 
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

    // Eliminazione 
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM ricetta WHERE idRicetta = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Mappa un ResultSet in un oggetto Ricetta
    private Ricetta mapResultSetToRicetta(ResultSet rs) throws SQLException {
        String nome = rs.getString("nome");
        int tempo = rs.getInt("tempoPreparazione");
        int idRicetta = rs.getInt("idRicetta"); 

        Ricetta r = new Ricetta(nome, tempo);

        Map<Ingrediente, Double> ingredienti = getIngredientiPerRicetta(idRicetta);
        r.setIngredienti(ingredienti);

        return r;
    }


    // Carica ingredienti di una ricetta dalla tabella Usa
    private Map<Ingrediente, Double> getIngredientiPerRicetta(int idRicetta) throws SQLException {
        Map<Ingrediente, Double> map = new HashMap<>();
        String sql = "SELECT i.nome, i.tipo, i.tempoPreparazione, u.quantita " +
                     "FROM Usa u " +
                     "JOIN Ingrediente i ON u.idIngrediente = i.idIngrediente " +
                     "WHERE u.idRicetta = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idRicetta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nomeIngrediente = rs.getString("nome");
                    String tipoIngrediente = rs.getString("tipo");
                    Ingrediente ing = new Ingrediente(nomeIngrediente, tipoIngrediente);
                    double quantita = rs.getDouble("quantita");
                    map.put(ing, quantita);
                }
            }
        }
        return map;
    }

}