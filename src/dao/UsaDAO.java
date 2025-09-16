package dao;

import model.Usa;
import model.Ricetta;
import model.Ingrediente;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsaDAO {

    // Inserimento
    public void save(Usa usa) throws SQLException {
        String sql = "INSERT INTO Usa (idRicetta, idIngrediente, quantita) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, usa.getRicetta().getIdRicetta());  // prendi l'id direttamente dal DB
            ps.setInt(2, usa.getIngrediente().getIdIngrediente());
            ps.setDouble(3, usa.getQuantita());
            ps.executeUpdate();
        }
    }

    // Aggiornamento quantità
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

    // Eliminazione per ricetta + ingrediente
    public void delete(Usa usa) throws SQLException {
        String sql = "DELETE FROM Usa WHERE idRicetta = ? AND idIngrediente = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, usa.getRicetta().getIdRicetta());
            ps.setInt(2, usa.getIngrediente().getIdIngrediente());
            ps.executeUpdate();
        }
    }

    // Lettura tutti
    public List<Usa> getAll() throws SQLException {
        List<Usa> list = new ArrayList<>();
        String sql = "SELECT r.nome AS nomeRicetta, i.nome AS nomeIngrediente, i.tipo AS tipoIngrediente, u.quantita " +
                     "FROM Usa u " +
                     "JOIN Ricetta r ON u.idRicetta = r.idRicetta " +
                     "JOIN Ingrediente i ON u.idIngrediente = i.idIngrediente";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Ricetta r = new Ricetta(rs.getString("nomeRicetta"), 0);  // tempoPreparazione ignoto
                Ingrediente i = new Ingrediente(rs.getString("nomeIngrediente"), rs.getString("tipoIngrediente"));
                Usa usa = new Usa(r, i, rs.getDouble("quantita"));
                list.add(usa);
            }
        }
        return list;
    }

    // Lettura per ricetta
    public List<Usa> getByRicetta(String nomeRicetta) throws SQLException {
        List<Usa> list = new ArrayList<>();
        String sql = "SELECT r.nome AS nomeRicetta, i.nome AS nomeIngrediente, i.tipo AS tipoIngrediente, u.quantita " +
                     "FROM Usa u " +
                     "JOIN Ricetta r ON u.idRicetta = r.idRicetta " +
                     "JOIN Ingrediente i ON u.idIngrediente = i.idIngrediente " +
                     "WHERE r.nome = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nomeRicetta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ricetta r = new Ricetta(rs.getString("nomeRicetta"), 0);
                    Ingrediente i = new Ingrediente(rs.getString("nomeIngrediente"), rs.getString("tipoIngrediente"));
                    Usa usa = new Usa(r, i, rs.getDouble("quantita"));
                    list.add(usa);
                }
            }
        }
        return list;
    }
}
