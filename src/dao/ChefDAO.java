package dao;

import model.Chef;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChefDAO {

	public void save(Chef c, String password) throws SQLException {
		String sql = "INSERT INTO chef (codFiscale, nome, cognome, email, dataNascita, anniEsperienza, disponibilita, username, password) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, c.getCodFiscale());
			ps.setString(2, c.getNome());
			ps.setString(3, c.getCognome());
			ps.setString(4, c.getEmail());
			ps.setDate(5, c.getDataNascita() != null ? Date.valueOf(c.getDataNascita()) : null);
			ps.setInt(6, c.getAnniEsperienza());
			ps.setBoolean(7, c.getDisponibilita());
			ps.setString(8, c.getUsername());
			ps.setString(9, password);

			ps.executeUpdate();
		}
	}

	public void update(Chef c, String password) throws SQLException {
		String sql = "UPDATE chef SET nome = ?, cognome = ?, email = ?, dataNascita = ?, anniEsperienza = ?, disponibilita = ?, username = ?, password = ? "
				+ "WHERE codFiscale = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, c.getNome());
			ps.setString(2, c.getCognome());
			ps.setString(3, c.getEmail());
			ps.setDate(4, c.getDataNascita() != null ? Date.valueOf(c.getDataNascita()) : null);
			ps.setInt(5, c.getAnniEsperienza());
			ps.setBoolean(6, c.getDisponibilita());
			ps.setString(7, c.getUsername());
			ps.setString(8, password);
			ps.setString(9, c.getCodFiscale());

			ps.executeUpdate();
		}
	}

	public Optional<Chef> findByCodFiscale(String cf) throws SQLException {
		String sql = "SELECT * FROM chef WHERE codFiscale = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, cf);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return Optional.of(mapResultSetToChef(rs));
			}
		}
		return Optional.empty();
	}

	public Optional<Chef> findByUsername(String username) throws SQLException {
		String sql = "SELECT * FROM chef WHERE username = ?";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, username);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapResultSetToChef(rs));
				}
			}
		}

		return Optional.empty();
	}

	public List<Chef> getAll() throws SQLException {
		List<Chef> list = new ArrayList<>();
		String sql = "SELECT * FROM chef ORDER BY nome";
		try (Connection conn = DBConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next())
				list.add(mapResultSetToChef(rs));
		}
		return list;
	}

	public boolean existsByEmail(String email) throws SQLException {
		String sql = "SELECT 1 FROM chef WHERE email = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, email);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	public void delete(String cf) throws SQLException {
		String sql = "DELETE FROM chef WHERE codFiscale = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, cf);
			ps.executeUpdate();
		}
	}

	private Chef mapResultSetToChef(ResultSet rs) throws SQLException {
		Chef c = new Chef(rs.getString("codFiscale"), rs.getString("nome"), rs.getString("cognome"),
				rs.getBoolean("disponibilita"), rs.getString("username"), rs.getString("password")
																								
		);
		c.setAnniEsperienza(rs.getInt("anniEsperienza"));
		Date data = rs.getDate("dataNascita");
		if (data != null)
			c.setDataNascita(data.toLocalDate());
		c.setEmail(rs.getString("email"));
		return c;
	}

	public boolean existsByUsername(String username) throws SQLException {
		String sql = "SELECT 1 FROM chef WHERE username = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, username);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	public boolean existsByCodFiscale(String codFiscale) throws SQLException {
		String sql = "SELECT 1 FROM chef WHERE codFiscale = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, codFiscale);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

}
