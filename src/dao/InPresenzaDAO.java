package dao;

import model.InPresenza;
import util.DBConnection;
import model.Frequenza;
import model.CorsoCucina;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InPresenzaDAO {

	// Inserimento
	public int save(InPresenza sessione) throws SQLException {
		String sql = "INSERT INTO sessione "
				+ "(datainiziosessione, datafinesessione, tipo, via, citta, cap, numeroposti, idcorsocucina) "
				+ "VALUES (?, ?, 'inPresenza', ?, ?, ?, ?, ?)";
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			ps.setTimestamp(1, Timestamp.valueOf(sessione.getDataInizioSessione()));
			ps.setTimestamp(2, Timestamp.valueOf(sessione.getDataFineSessione()));
			ps.setString(3, sessione.getVia());
			ps.setString(4, sessione.getCitta());
			ps.setInt(5, sessione.getCAP());
			ps.setInt(6, sessione.getNumeroPosti());

			if (sessione.getCorsoCucina() == null) {
				throw new SQLException("La sessione in presenza deve avere un corso associato!");
			}
			ps.setInt(7, sessione.getCorsoCucina().getIdCorso());

			ps.executeUpdate();

			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		}
		return -1;
	}

	// Aggiornamento tramite ID
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

	// Lettura per ID
	public Optional<InPresenza> findById(int idSessione) throws SQLException {
		String sql = "SELECT s.*, c.nomeCorso, c.argomento, c.prezzo, c.numeroPosti AS postiCorso, "
				+ "c.numeroSessioni AS sessioniCorso, c.frequenzaCorso " + "FROM sessione s "
				+ "LEFT JOIN corsocucina c ON s.idcorsocucina = c.idCorsoCucina "
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

	// Lettura tutte le sessioni in presenza
	public List<InPresenza> getAll() throws SQLException {
		List<InPresenza> list = new ArrayList<>();
		String sql = "SELECT s.*, c.nomeCorso, c.argomento, c.prezzo, c.numeroPosti AS postiCorso, "
				+ "c.numeroSessioni AS sessioniCorso, c.frequenzaCorso " + "FROM sessione s "
				+ "LEFT JOIN corsocucina c ON s.idcorsocucina = c.idCorsoCucina "
				+ "WHERE s.tipo = 'inPresenza' ORDER BY s.datainiziosessione";
		try (Connection conn = DBConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				list.add(mapResultSetToInPresenza(rs));
			}
		}
		return list;
	}

	// Eliminazione
	public void delete(int idSessione) throws SQLException {
		String sql = "DELETE FROM sessione WHERE idsessione=?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idSessione);
			ps.executeUpdate();
		}
	}

	// Mapping 
	private InPresenza mapResultSetToInPresenza(ResultSet rs) throws SQLException {
	    LocalDateTime inizio = rs.getTimestamp("datainiziosessione").toLocalDateTime();
	    LocalDateTime fine = rs.getTimestamp("datafinesessione").toLocalDateTime();
	    String via = rs.getString("via");
	    String citta = rs.getString("citta");
	    int posti = rs.getInt("numeroposti");
	    int cap = rs.getInt("cap");

	    InPresenza sessione = new InPresenza(inizio, fine, via, citta, posti, cap);

	    String nomeCorso = rs.getString("nomeCorso");
	    if (nomeCorso != null) {
	        Frequenza freq = null;
	        try {
	            String freqStr = rs.getString("frequenzaCorso");
	            if (freqStr != null)
	                freq = Frequenza.valueOf(freqStr);
	        } catch (IllegalArgumentException ignored) {
	        }

	        CorsoCucina corso = new CorsoCucina(nomeCorso, rs.getDouble("prezzo"), rs.getString("argomento"), freq,
	                rs.getInt("postiCorso"), rs.getInt("sessioniCorso"));
	        sessione.setCorsoCucina(corso);
	    }

	    return sessione;
	}

}
