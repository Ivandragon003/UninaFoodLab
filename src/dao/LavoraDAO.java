package dao;

import model.Chef;
import model.Ristorante;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LavoraDAO {

	// Aggiunge un legame Chef-Ristorante
	public void addChefToRistorante(String codFiscale, int idRistorante) throws SQLException {
		String sql = "INSERT INTO lavora (codFiscale, idRistorante) VALUES (?, ?)";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, codFiscale);
			ps.setInt(2, idRistorante);
			ps.executeUpdate();
		}
	}

	// Rimuove un legame
	public void removeChefFromRistorante(String codFiscale, int idRistorante) throws SQLException {
		String sql = "DELETE FROM lavora WHERE codFiscale = ? AND idRistorante = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, codFiscale);
			ps.setInt(2, idRistorante);
			ps.executeUpdate();
		}
	}

	// Verifica esistenza legame
	public boolean exists(String codFiscale, int idRistorante) throws SQLException {
		String sql = "SELECT 1 FROM lavora WHERE codFiscale = ? AND idRistorante = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, codFiscale);
			ps.setInt(2, idRistorante);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	// Ottieni tutti i ristoranti di uno chef
	public List<Ristorante> getRistorantiByChef(String codFiscale) throws SQLException {
		List<Ristorante> list = new ArrayList<>();
		String sql = "SELECT r.* FROM lavora l JOIN ristorante r ON l.idRistorante = r.idRistorante WHERE l.codFiscale = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, codFiscale);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Ristorante r = new Ristorante(rs.getString("via"), rs.getString("partitaIva"),
							rs.getString("nome"));
					r.setStelleMichelin(rs.getInt("stelleMichelin"));
					list.add(r);
				}
			}
		}
		return list;
	}

	// Ottieni tutti gli chef di un ristorante
	public List<Chef> getChefByRistorante(int idRistorante) throws SQLException {
		List<Chef> list = new ArrayList<>();
		String sql = "SELECT c.* FROM lavora l JOIN chef c ON l.codFiscale = c.codFiscale WHERE l.idRistorante = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, idRistorante);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Chef chef = new Chef(rs.getString("codFiscale"), rs.getString("nome"), rs.getString("cognome"),
							rs.getBoolean("disponibilita"), rs.getString("username"), rs.getString("password"));
					chef.setAnniEsperienza(rs.getInt("anniEsperienza"));
					chef.setEmail(rs.getString("email"));
					Date dataNascita = rs.getDate("dataNascita");
					if (dataNascita != null) {
						chef.setDataNascita(dataNascita.toLocalDate());
					}
					list.add(chef);
				}
			}
		}
		return list;
	}

	public void deleteByRistorante(int idRistorante) throws SQLException {
		String sql = "DELETE FROM lavora WHERE idRistorante = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idRistorante);
			ps.executeUpdate();
		}
	}

}
