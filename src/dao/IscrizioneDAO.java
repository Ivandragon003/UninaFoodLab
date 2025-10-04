package dao;

import model.CorsoCucina;
import model.Iscrizione;
import model.Utente;
import util.DBConnection;
import java.util.Set;
import java.util.HashSet;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IscrizioneDAO {

	public void save(Iscrizione iscrizione) throws SQLException {
		if (iscrizione == null || iscrizione.getUtente() == null || iscrizione.getCorso() == null) {
			throw new IllegalArgumentException("Iscrizione, Utente o Corso null");
		}

		String sql = "INSERT INTO iscritto (codFiscale, idCorsoCucina, votiAvuti, stato) VALUES (?, ?, ?, ?)";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, iscrizione.getUtente().getCodFiscale());
			ps.setInt(2, iscrizione.getCorso().getIdCorso());
			if (iscrizione.getVotiAvuti() != null) {
				ps.setInt(3, iscrizione.getVotiAvuti());
			} else {
				ps.setNull(3, Types.INTEGER);
			}
			ps.setBoolean(4, iscrizione.isStato());
			ps.executeUpdate();
		}
	}

	public void delete(String codFiscale, int idCorso) throws SQLException {
		String sql = "DELETE FROM iscritto WHERE codFiscale = ? AND idCorsoCucina = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, codFiscale);
			ps.setInt(2, idCorso);
			ps.executeUpdate();
		}
	}

	public boolean exists(String codFiscale, int idCorso) throws SQLException {
		String sql = "SELECT 1 FROM iscritto WHERE codFiscale = ? AND idCorsoCucina = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, codFiscale);
			ps.setInt(2, idCorso);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	public List<Iscrizione> getAllFull() throws SQLException {
		List<Iscrizione> list = new ArrayList<>();
		String sql = "SELECT codFiscale, idCorsoCucina, votiAvuti, stato FROM iscritto";

		UtenteDAO utenteDAO = new UtenteDAO();
		CorsoCucinaDAO corsoDAO = new CorsoCucinaDAO();

		try (Connection conn = DBConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				list.add(mapResultSetToIscrizione(rs, utenteDAO, corsoDAO));
			}
		}

		return list;
	}

	public Set<Utente> getIscrittiAttivi(int idCorso) throws SQLException {
		Set<Utente> iscrittiAttivi = new HashSet<>();
		String sql = "SELECT codFiscale, nome, cognome FROM iscrittiConfermatiCorso WHERE idCorsoCucina = ?";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, idCorso);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Utente u = new Utente(rs.getString("codFiscale"), rs.getString("nome"), rs.getString("cognome"));
					iscrittiAttivi.add(u);
				}
			}
		}
		return iscrittiAttivi;
	}

	private Iscrizione mapResultSetToIscrizione(ResultSet rs, UtenteDAO utenteDAO, CorsoCucinaDAO corsoDAO)
			throws SQLException {
		String codFiscale = rs.getString("codFiscale");
		int idCorso = rs.getInt("idCorsoCucina");

		Utente u = utenteDAO.findByCodFiscale(codFiscale)
				.orElseThrow(() -> new SQLException("Utente non trovato: " + codFiscale));
		CorsoCucina c = corsoDAO.findById(idCorso).orElseThrow(() -> new SQLException("Corso non trovato: " + idCorso));

		Iscrizione i = new Iscrizione(u, c, rs.getBoolean("stato"));
		Integer voti = rs.getInt("votiAvuti");
		if (rs.wasNull())
			voti = null;
		i.setVotiAvuti(voti);

		return i;
	}
}
