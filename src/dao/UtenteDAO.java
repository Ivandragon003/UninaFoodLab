
package dao;

import model.Utente;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UtenteDAO {

	public void save(Utente u) throws SQLException {
		String sql = "INSERT INTO utente (codFiscale, nome, cognome, email, dataNascita) " + "VALUES (?, ?, ?, ?, ?)";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, u.getCodFiscale());
			ps.setString(2, u.getNome());
			ps.setString(3, u.getCognome());
			ps.setString(4, u.getEmail());
			ps.setDate(5, u.getDataNascita() != null ? Date.valueOf(u.getDataNascita()) : null);

			ps.executeUpdate();
		}
	}

	public Optional<Utente> findByCodFiscale(String cf) throws SQLException {
		String sql = "SELECT * FROM utente WHERE codFiscale = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, cf);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					Utente u = mapResultSetToUtente(rs);
					return Optional.of(u);
				}
			}
		}
		return Optional.empty();
	}

	public List<Utente> getAll() throws SQLException {
		List<Utente> list = new ArrayList<>();
		String sql = "SELECT * FROM utente ORDER BY nome";
		try (Connection conn = DBConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				list.add(mapResultSetToUtente(rs));
			}
		}
		return list;
	}

	public List<Utente> getByNome(String nome) throws SQLException {
		List<Utente> list = new ArrayList<>();
		String sql = "SELECT * FROM utente WHERE nome = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, nome);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapResultSetToUtente(rs));
				}
			}
		}
		return list;
	}

	public List<Utente> searchByNome(String partialNome) throws SQLException {
		List<Utente> list = new ArrayList<>();
		String sql = "SELECT * FROM utente WHERE nome ILIKE ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, "%" + partialNome + "%");
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapResultSetToUtente(rs));
				}
			}
		}
		return list;
	}

	public void update(Utente u) throws SQLException {
		String sql = "UPDATE utente SET nome = ?, cognome = ?, email = ?, dataNascita = ? WHERE codFiscale = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, u.getNome());
			ps.setString(2, u.getCognome());
			ps.setString(3, u.getEmail());
			ps.setDate(4, u.getDataNascita() != null ? Date.valueOf(u.getDataNascita()) : null);
			ps.setString(5, u.getCodFiscale());

			ps.executeUpdate();
		}
	}

	public void delete(String cf) throws SQLException {
		String sql = "DELETE FROM utente WHERE codFiscale = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, cf);
			ps.executeUpdate();
		}
	}

	private Utente mapResultSetToUtente(ResultSet rs) throws SQLException {
		Utente u = new Utente(rs.getString("codFiscale"), rs.getString("nome"), rs.getString("cognome"));
		Date data = rs.getDate("dataNascita");
		if (data != null)
			u.setDataNascita(data.toLocalDate());
		u.setEmail(rs.getString("email"));
		return u;
	}
}