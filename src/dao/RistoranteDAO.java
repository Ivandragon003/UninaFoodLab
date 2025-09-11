package dao;

import model.Ristorante;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RistoranteDAO {

	// Inserimento
	public void save(Ristorante r) throws SQLException {
		String sql = "INSERT INTO ristorante (partitaIva, nome, via, stelleMichelin) VALUES (?, ?, ?, ?)";
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, r.getPartitaIva());
			ps.setString(2, r.getNome());
			ps.setString(3, r.getVia());
			ps.setInt(4, r.getStelleMichelin()); 
			ps.executeUpdate();
		}
	}

	// Lettura
	public List<Ristorante> getAll() throws SQLException {
		List<Ristorante> list = new ArrayList<>();
		String sql = "SELECT * FROM ristorante";
		try (Connection conn = DBConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				Ristorante r = new Ristorante(rs.getString("via"), rs.getString("partitaIva"), rs.getString("nome"));
				int stelle = rs.getInt("stelleMichelin");
				if (rs.wasNull()) {
					stelle = 0; 
				}
				r.setStelleMichelin(stelle);
				list.add(r);
			}
		}
		return list;
	}

	// Aggiornamento
	public void update(int id, Ristorante r) throws SQLException {
		String sql = "UPDATE ristorante SET partitaIva = ?, nome = ?, via = ?, stelleMichelin = ? WHERE idRistorante = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, r.getPartitaIva());
			ps.setString(2, r.getNome());
			ps.setString(3, r.getVia());
			ps.setInt(4, r.getStelleMichelin()); 
			ps.setInt(5, id);
			ps.executeUpdate();
		}
	}

	// Eliminazione
	public void delete(int id) throws SQLException {
		String sql = "DELETE FROM ristorante WHERE idRistorante = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, id);
			ps.executeUpdate();
		}
	}
}
