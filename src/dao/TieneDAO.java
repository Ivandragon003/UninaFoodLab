package dao;

import model.Chef;
import model.CorsoCucina;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TieneDAO {

    private ChefDAO chefDAO = new ChefDAO();
    private CorsoCucinaDAO corsoDAO = new CorsoCucinaDAO();

    public void save(String codFiscale, int idCorso) throws SQLException {
        String sql = "INSERT INTO tiene (codfiscale, idcorsocucina) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codFiscale);
            ps.setInt(2, idCorso);
            ps.executeUpdate();
        }
    }

    public void delete(String codFiscale, int idCorso) throws SQLException {
        String sql = "DELETE FROM tiene WHERE codfiscale = ? AND idcorsocucina = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codFiscale);
            ps.setInt(2, idCorso);
            ps.executeUpdate();
        }
    }

    public List<CorsoCucina> getCorsiByChef(String codFiscale) throws SQLException {
        List<CorsoCucina> corsi = new ArrayList<>();
        String sql = "SELECT idcorsocucina FROM tiene WHERE codfiscale = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codFiscale);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idCorso = rs.getInt("idcorsocucina");
                    corsoDAO.findById(idCorso).ifPresent(corsi::add);
                }
            }
        }
        return corsi;
    }

    public List<Chef> getChefByCorso(int idCorso) throws SQLException {
        List<Chef> chefList = new ArrayList<>();
        String sql = "SELECT codfiscale FROM tiene WHERE idcorsocucina = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCorso);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String cf = rs.getString("codfiscale");
                    chefDAO.findByCodFiscale(cf).ifPresent(chefList::add);
                }
            }
        }
        return chefList;
    }

    public void deleteByChef(String codFiscale) throws SQLException {
        String sql = "DELETE FROM tiene WHERE codfiscale = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codFiscale);
            ps.executeUpdate();
        }
    }

    public void deleteByCorso(int idCorso) throws SQLException {
        String sql = "DELETE FROM tiene WHERE idcorsocucina = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCorso);
            ps.executeUpdate();
        }
    }

}
