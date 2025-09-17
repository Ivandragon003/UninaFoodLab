package dao;

import model.Ricetta;
import model.Sessione;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CucinaDAO {

    // Inserisce una relazione tra ricetta e sessione
    public void save(int idRicetta, int idSessione) throws SQLException {
        String sql = "INSERT INTO SessioneRicetta (idRicetta, idSessione) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idRicetta);
            ps.setInt(2, idSessione);
            ps.executeUpdate();
        }
    }

    // Elimina la relazione tra ricetta e sessione
    public void delete(int idRicetta, int idSessione) throws SQLException {
        String sql = "DELETE FROM SessioneRicetta WHERE idRicetta = ? AND idSessione = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idRicetta);
            ps.setInt(2, idSessione);
            ps.executeUpdate();
        }
    }

    // Restituisce tutte le ricette associate a una sessione
    public List<Ricetta> getRicettePerSessione(int idSessione) throws SQLException {
        List<Ricetta> ricette = new ArrayList<>();
        String sql = "SELECT r.idRicetta, r.nome, r.tempoPreparazione " +
                     "FROM Ricetta r " +
                     "JOIN SessioneRicetta sr ON r.idRicetta = sr.idRicetta " +
                     "WHERE sr.idSessione = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idSessione);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("idRicetta");
                    String nome = rs.getString("nome");
                    int tempo = rs.getInt("tempoPreparazione");
                    Ricetta r = new Ricetta(nome, tempo);
                    // setta l'id (se serve, altrimenti gestisci tramite DAO Ricetta)
                    ricette.add(r);
                }
            }
        }
        return ricette;
    }

}
