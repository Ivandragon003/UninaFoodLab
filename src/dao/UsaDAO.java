package dao;

import model.Usa;
import model.Ricetta;
import model.Ingrediente;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsaDAO {

    public void save(Usa usa) throws SQLException {
        String sql = "INSERT INTO Usa (idRicetta, idIngrediente, quantita) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, usa.getRicetta().getIdRicetta());
            ps.setInt(2, usa.getIngrediente().getIdIngrediente());
            ps.setDouble(3, usa.getQuantita());
            ps.executeUpdate();
        }
    }

    public void updateQuantita(Usa usa) throws SQLException {
        String sql = "UPDATE Usa SET quantita = ? WHERE idRicetta = ? AND idIngrediente = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, usa.getQuantita());
            ps.setInt(2, usa.getRicetta().getIdRicetta());
            ps.setInt(3, usa.getIngrediente().getIdIngrediente());
            ps.executeUpdate();
        }
    }

    public void delete(Usa usa) throws SQLException {
        String sql = "DELETE FROM Usa WHERE idRicetta = ? AND idIngrediente = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, usa.getRicetta().getIdRicetta());
            ps.setInt(2, usa.getIngrediente().getIdIngrediente());
            ps.executeUpdate();
        }
    }

    // Nuovo metodo: cancella usando direttamente gli id (evita di creare Usa con quantita=0)
    public void deleteByRicettaIdAndIngredienteId(int idRicetta, int idIngrediente) throws SQLException {
        String sql = "DELETE FROM Usa WHERE idRicetta = ? AND idIngrediente = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRicetta);
            ps.setInt(2, idIngrediente);
            ps.executeUpdate();
        }
    }

    public void deleteByRicetta(int idRicetta) throws SQLException {
        String sql = "DELETE FROM Usa WHERE idRicetta = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRicetta);
            ps.executeUpdate();
        }
    }

    public void deleteByIngrediente(Ingrediente ingrediente) throws SQLException {
        if (ingrediente == null) {
            throw new IllegalArgumentException("Ingrediente non può essere null");
        }

        String sql = "DELETE FROM Usa WHERE idIngrediente = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ingrediente.getIdIngrediente());
            ps.executeUpdate();
        }
    }

    public List<Usa> getAll() throws SQLException {
        List<Usa> list = new ArrayList<>();
        String sql = "SELECT r.idRicetta, r.nome AS nomeRicetta, r.tempoPreparazione, " +
                     "i.idIngrediente, i.nome AS nomeIngrediente, i.tipo AS tipoIngrediente, u.quantita " +
                     "FROM Usa u " +
                     "JOIN Ricetta r ON u.idRicetta = r.idRicetta " +
                     "JOIN Ingrediente i ON u.idIngrediente = i.idIngrediente";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Ricetta r = new Ricetta(rs.getString("nomeRicetta"), rs.getInt("tempoPreparazione"));
                r.setIdRicetta(rs.getInt("idRicetta"));

                Ingrediente i = new Ingrediente(rs.getString("nomeIngrediente"), rs.getString("tipoIngrediente"));
                i.setIdIngrediente(rs.getInt("idIngrediente"));

                Usa usa = new Usa(r, i, rs.getDouble("quantita"));
                list.add(usa);
            }
        }
        return list;
    }

    public List<Usa> getByRicetta(String nomeRicetta) throws SQLException {
        List<Usa> list = new ArrayList<>();
        String sql = "SELECT r.idRicetta, r.nome AS nomeRicetta, r.tempoPreparazione, " +
                     "i.idIngrediente, i.nome AS nomeIngrediente, i.tipo AS tipoIngrediente, u.quantita " +
                     "FROM Usa u " +
                     "JOIN Ricetta r ON u.idRicetta = r.idRicetta " +
                     "JOIN Ingrediente i ON u.idIngrediente = i.idIngrediente " +
                     "WHERE r.nome = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nomeRicetta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ricetta r = new Ricetta(rs.getString("nomeRicetta"), rs.getInt("tempoPreparazione"));
                    r.setIdRicetta(rs.getInt("idRicetta"));

                    Ingrediente i = new Ingrediente(rs.getString("nomeIngrediente"), rs.getString("tipoIngrediente"));
                    i.setIdIngrediente(rs.getInt("idIngrediente"));

                    Usa usa = new Usa(r, i, rs.getDouble("quantita"));
                    list.add(usa);
                }
            }
        }
        return list;
    }
}