package dao;

import model.Online;
import model.CorsoCucina;
import model.Frequenza;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OnlineDAO {

	public int save(Online sessione) throws SQLException {
		String sql = "INSERT INTO sessione (datainiziosessione, datafinesessione, tipo, piattaformastreaming, idcorsocucina) "
				+ "VALUES (?, ?, 'online', ?, ?) RETURNING idsessione";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setTimestamp(1, Timestamp.valueOf(sessione.getDataInizioSessione()));
			ps.setTimestamp(2, Timestamp.valueOf(sessione.getDataFineSessione()));
			ps.setString(3, sessione.getPiattaformaStreaming());

			if (sessione.getCorsoCucina() == null)
				ps.setNull(4, Types.INTEGER);
			else
				ps.setInt(4, sessione.getCorsoCucina().getIdCorso());

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

	public void update(int id, Online sessione) throws SQLException {
		String sql = "UPDATE sessione SET datainiziosessione=?, datafinesessione=?, piattaformastreaming=?, idcorsocucina=? "
				+ "WHERE idsessione=?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setTimestamp(1, Timestamp.valueOf(sessione.getDataInizioSessione()));
			ps.setTimestamp(2, Timestamp.valueOf(sessione.getDataFineSessione()));
			ps.setString(3, sessione.getPiattaformaStreaming());

			if (sessione.getCorsoCucina() == null)
				ps.setNull(4, Types.INTEGER);
			else
				ps.setInt(4, sessione.getCorsoCucina().getIdCorso());

			ps.setInt(5, id);

			ps.executeUpdate();
		}
	}

	public void delete(int id) throws SQLException {
		String sql = "DELETE FROM sessione WHERE idsessione = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, id);
			ps.executeUpdate();
		}
	}

	public Optional<Online> findById(int idSessione) throws SQLException {
		String sql = "SELECT s.*, c.nomeCorso, c.argomento, c.prezzo, c.numeroPosti AS numeroposti, c.frequenzaCorso "
				+ "FROM sessione s LEFT JOIN corsocucina c ON s.idcorsocucina = c.idCorsoCucina "
				+ "WHERE s.idsessione = ? AND s.tipo = 'online'";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, idSessione);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return Optional.of(mapResultSetToOnline(rs));
			}
		}
		return Optional.empty();
	}

	public List<Online> getByCorso(int idCorso) throws SQLException {
		String sql = "SELECT s.idsessione, s.datainiziosessione, s.datafinesessione, s.tipo, "
				+ "s.piattaformastreaming, s.idcorsocucina, "
				+ "c.idCorsoCucina, c.nomeCorso, c.argomento, c.prezzo, c.numeroPosti AS numeroposti, c.frequenzaCorso "
				+ "FROM sessione s LEFT JOIN corsocucina c ON s.idcorsocucina = c.idCorsoCucina "
				+ "WHERE s.tipo = 'online' AND s.idcorsocucina = ? ORDER BY s.datainiziosessione";

		List<Online> result = new ArrayList<>();
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, idCorso);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					result.add(mapResultSetToOnline(rs));
			}
		}
		return result;
	}

	public List<Online> getAll() throws SQLException {
		List<Online> list = new ArrayList<>();
		String sql = "SELECT s.idsessione, s.datainiziosessione, s.datafinesessione, s.tipo, "
				+ "s.piattaformastreaming, s.idcorsocucina, "
				+ "c.idCorsoCucina, c.nomeCorso, c.argomento, c.prezzo, c.numeroPosti AS numeroposti, c.frequenzaCorso "
				+ "FROM sessione s LEFT JOIN corsocucina c ON s.idcorsocucina = c.idCorsoCucina "
				+ "WHERE s.tipo = 'online' ORDER BY s.datainiziosessione";
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next())
				list.add(mapResultSetToOnline(rs));
		}
		return list;
	}

	private Online mapResultSetToOnline(ResultSet rs) throws SQLException {
		Timestamp tsInizio = rs.getTimestamp("datainiziosessione");
		Timestamp tsFine = rs.getTimestamp("datafinesessione");
		LocalDateTime inizio = tsInizio != null ? tsInizio.toLocalDateTime() : null;
		LocalDateTime fine = tsFine != null ? tsFine.toLocalDateTime() : null;
		String piattaforma = rs.getString("piattaformastreaming");

		Online sessione = new Online(inizio, fine, piattaforma);
		sessione.setIdSessione(rs.getInt("idsessione"));

		String nomeCorso = rs.getString("nomecorso");
		if (nomeCorso != null) {
			Frequenza freq = null;
			String freqStr = rs.getString("frequenzaCorso");
			if (freqStr != null)
				freq = Frequenza.valueOf(freqStr);

			int numeroPostiCorso = rs.getInt("numeroposti");
			if (numeroPostiCorso <= 0) {
				throw new IllegalArgumentException("Corso '" + nomeCorso + "' ha numeroPosti = " + numeroPostiCorso
						+ " nel database. Controlla i dati!");
			}

			CorsoCucina corso = new CorsoCucina(nomeCorso, rs.getDouble("prezzo"), rs.getString("argomento"), freq,
					numeroPostiCorso);
			corso.setIdCorso(rs.getInt("idCorsoCucina"));
			sessione.setCorsoCucina(corso);
		}

		return sessione;
	}


	public List<Online> getByCorsoInPeriodo(int idCorso, 
                                        LocalDateTime inizio, 
                                        LocalDateTime fine) 
        throws SQLException {
    
    String sql = """
        SELECT * FROM online 
        WHERE idCorsoCucina = ? 
          AND datainiziosessione >= ? 
          AND datainiziosessione <= ?
        ORDER BY datainiziosessione
        """;
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, idCorso);
        stmt.setTimestamp(2, Timestamp.valueOf(inizio));
        stmt.setTimestamp(3, Timestamp.valueOf(fine));
        
        try (ResultSet rs = stmt.executeQuery()) {
            List<Online> sessioni = new ArrayList<>();
            while (rs.next()) {
                sessioni.add(mapResultSetToOnline(rs));
            }
            return sessioni;
        }
    }
}


}
