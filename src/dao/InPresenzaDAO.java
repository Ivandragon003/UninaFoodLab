package dao;

import model.InPresenza;
import model.Ricetta;
import model.CorsoCucina;
import model.Frequenza;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class InPresenzaDAO {

	private final CucinaDAO cucinaDAO;

	public InPresenzaDAO(CucinaDAO cucinaDAO) {
		this.cucinaDAO = cucinaDAO;
	}

	public int save(InPresenza sessione) throws SQLException {
		String sql = "INSERT INTO sessione "
				+ "(datainiziosessione, datafinesessione, tipo, via, citta, cap, numeroposti, idcorsocucina) "
				+ "VALUES (?, ?, 'inPresenza', ?, ?, ?, ?, ?) RETURNING idsessione";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setTimestamp(1, Timestamp.valueOf(sessione.getDataInizioSessione()));
			ps.setTimestamp(2, Timestamp.valueOf(sessione.getDataFineSessione()));
			ps.setString(3, sessione.getVia());
			ps.setString(4, sessione.getCitta());
			ps.setInt(5, sessione.getCAP());
			ps.setInt(6, sessione.getNumeroPosti());

			if (sessione.getCorsoCucina() == null)
				throw new SQLException("La sessione in presenza deve avere un corso associato!");
			ps.setInt(7, sessione.getCorsoCucina().getIdCorso());

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					int id = rs.getInt(1);
					sessione.setIdSessione(id);
					return id;
				}
			}
		}
		return -1;
	}

	public void update(int idSessione, InPresenza sessione) throws SQLException {
		String sql = "UPDATE sessione SET datainiziosessione=?, datafinesessione=?, via=?, citta=?, cap=?, numeroposti=? "
				+ "WHERE idsessione=?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setTimestamp(1, Timestamp.valueOf(sessione.getDataInizioSessione()));
			ps.setTimestamp(2, Timestamp.valueOf(sessione.getDataFineSessione()));
			ps.setString(3, sessione.getVia());
			ps.setString(4, sessione.getCitta());
			ps.setInt(5, sessione.getCAP());
			ps.setInt(6, sessione.getNumeroPosti());
			ps.setInt(7, idSessione);

			ps.executeUpdate();
		}
	}

	public Optional<InPresenza> findById(int idSessione) throws SQLException {
		String sql = "SELECT s.*, c.nomeCorso, c.argomento, c.prezzo, c.numeroPosti AS postiCorso, "
				+ "c.frequenzaCorso FROM sessione s LEFT JOIN corsocucina c ON s.idcorsocucina = c.idCorsoCucina "
				+ "WHERE s.idsessione = ? AND s.tipo = 'inPresenza'";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, idSessione);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return Optional.of(mapResultSetToInPresenza(rs));
			}
		}
		return Optional.empty();
	}

	public List<InPresenza> getAll() throws SQLException {
		List<InPresenza> list = new ArrayList<>();
		String sql = "SELECT s.idsessione, s.datainiziosessione, s.datafinesessione, s.tipo, "
				+ "s.via, s.citta, s.cap, s.numeroposti as postiSessione, s.idcorsocucina, "
				+ "c.idCorsoCucina, c.nomeCorso, c.argomento, c.prezzo, c.numeroPosti as postiCorso, c.frequenzaCorso "
				+ "FROM sessione s LEFT JOIN corsocucina c ON s.idcorsocucina = c.idCorsoCucina "
				+ "WHERE s.tipo = 'inPresenza' ORDER BY s.datainiziosessione";
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next())
				list.add(mapResultSetToInPresenza(rs));
		}
		return list;
	}

	public void delete(int idSessione) throws SQLException {
		String sql = "DELETE FROM sessione WHERE idsessione=?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, idSessione);
			ps.executeUpdate();
		}
	}

	public List<InPresenza> getByCorso(int idCorso) throws SQLException {
		String sql = "SELECT s.idsessione, s.datainiziosessione, s.datafinesessione, s.tipo, "
				+ "s.via, s.citta, s.cap, s.numeroposti as postiSessione, s.idcorsocucina, "
				+ "c.idCorsoCucina, c.nomeCorso, c.argomento, c.prezzo, c.numeroPosti as postiCorso, c.frequenzaCorso "
				+ "FROM sessione s LEFT JOIN corsocucina c ON s.idcorsocucina = c.idCorsoCucina "
				+ "WHERE s.tipo = 'inPresenza' AND s.idcorsocucina = ? ORDER BY s.datainiziosessione";

		List<InPresenza> result = new ArrayList<>();
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, idCorso);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					result.add(mapResultSetToInPresenza(rs));
			}
		}
		return result;
	}

	private InPresenza mapResultSetToInPresenza(ResultSet rs) throws SQLException {
		Timestamp tsInizio = rs.getTimestamp("datainiziosessione");
		Timestamp tsFine = rs.getTimestamp("datafinesessione");
		LocalDateTime inizio = tsInizio != null ? tsInizio.toLocalDateTime() : null;
		LocalDateTime fine = tsFine != null ? tsFine.toLocalDateTime() : null;

		String via = rs.getString("via");
		String citta = rs.getString("citta");
		int cap = rs.getInt("cap");
		int postiSessione = rs.getInt("postiSessione");

		InPresenza sessione = new InPresenza(inizio, fine, via, citta, postiSessione, cap);
		sessione.setIdSessione(rs.getInt("idsessione"));

		String nomeCorso = rs.getString("nomecorso");
		if (nomeCorso != null) {
			Frequenza freq = null;
			String freqStr = rs.getString("frequenzaCorso");
			if (freqStr != null)
				freq = Frequenza.valueOf(freqStr);

			int numeroPostiCorso = rs.getInt("postiCorso");
			if (numeroPostiCorso <= 0) {
				throw new IllegalArgumentException("Corso '" + nomeCorso + "' ha numeroPosti = " + numeroPostiCorso
						+ " nel database. Controlla i dati!");
			}

			CorsoCucina corso = new CorsoCucina(nomeCorso, rs.getDouble("prezzo"), rs.getString("argomento"), freq,
					numeroPostiCorso);
			corso.setIdCorso(rs.getInt("idCorsoCucina"));
			sessione.setCorsoCucina(corso);

			int idSessione = sessione.getIdSessione();
			Set<Ricetta> ricette = cucinaDAO.getRicettePerSessione(idSessione);
			sessione.setRicette(ricette);
		}

		return sessione;
	}

	public List<InPresenza> getByCorsoInPeriodo(int idCorso, LocalDateTime inizio, LocalDateTime fine)
			throws SQLException {
		String sql = """
				SELECT s.idsessione, s.datainiziosessione, s.datafinesessione, s.tipo,
				       s.via, s.citta, s.cap, s.numeroposti as postiSessione, s.idcorsocucina,
				       c.idCorsoCucina, c.nomeCorso, c.argomento, c.prezzo,
				       c.numeroPosti as postiCorso, c.frequenzaCorso
				FROM sessione s
				LEFT JOIN corsocucina c ON s.idcorsocucina = c.idCorsoCucina
				WHERE s.tipo = 'inPresenza'
				  AND s.idcorsocucina = ?
				  AND s.datainiziosessione >= ?
				  AND s.datainiziosessione <= ?
				ORDER BY s.datainiziosessione
				""";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, idCorso);
			stmt.setTimestamp(2, Timestamp.valueOf(inizio));
			stmt.setTimestamp(3, Timestamp.valueOf(fine));

			try (ResultSet rs = stmt.executeQuery()) {
				List<InPresenza> sessioni = new ArrayList<>();
				while (rs.next()) {
					sessioni.add(mapResultSetToInPresenza(rs));
				}
				return sessioni;
			}
		}
	}

}
