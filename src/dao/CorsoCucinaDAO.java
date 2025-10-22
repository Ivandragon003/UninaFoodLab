package dao;

import model.CorsoCucina;
import model.Frequenza;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

public class CorsoCucinaDAO {

	public int save(CorsoCucina corso) throws SQLException {
		String sql = "INSERT INTO corsocucina " + "(nomecorso, argomento, frequenzacorso, prezzo, numeroposti, "
				+ "datainiziocorso, datafinecorso, codfiscalefondatore) "
				+ "VALUES (?, ?, ?::frequenza, ?, ?, ?, ?, ?) RETURNING idcorsocucina";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, corso.getNomeCorso());
			ps.setString(2, corso.getArgomento());
			ps.setString(3, corso.getFrequenzaCorso() != null ? corso.getFrequenzaCorso().name() : null);
			ps.setBigDecimal(4, BigDecimal.valueOf(corso.getPrezzo()));
			ps.setInt(5, corso.getNumeroPosti());

			if (corso.getDataInizioCorso() != null)
				ps.setTimestamp(6, Timestamp.valueOf(corso.getDataInizioCorso()));
			else
				ps.setNull(6, Types.TIMESTAMP);

			if (corso.getDataFineCorso() != null)
				ps.setTimestamp(7, Timestamp.valueOf(corso.getDataFineCorso()));
			else
				ps.setNull(7, Types.TIMESTAMP);

			ps.setString(8, corso.getCodfiscaleFondatore());

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					int id = rs.getInt(1);
					corso.setIdCorso(id);
					return id;
				}
			}
		}
		return -1;
	}

	public void update(CorsoCucina corso) throws SQLException {
		String sql = "UPDATE corsocucina SET nomecorso = ?, argomento = ?, frequenzacorso = ?::frequenza, "
				+ "prezzo = ?, numeroposti = ?, datainiziocorso = ?, datafinecorso = ?, " + "codfiscalefondatore = ? "
				+ "WHERE idcorsocucina = ?";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, corso.getNomeCorso());
			ps.setString(2, corso.getArgomento());
			ps.setString(3, corso.getFrequenzaCorso() != null ? corso.getFrequenzaCorso().name() : null);
			ps.setBigDecimal(4, BigDecimal.valueOf(corso.getPrezzo()));
			ps.setInt(5, corso.getNumeroPosti());

			if (corso.getDataInizioCorso() != null)
				ps.setTimestamp(6, Timestamp.valueOf(corso.getDataInizioCorso()));
			else
				ps.setNull(6, Types.TIMESTAMP);

			if (corso.getDataFineCorso() != null)
				ps.setTimestamp(7, Timestamp.valueOf(corso.getDataFineCorso()));
			else
				ps.setNull(7, Types.TIMESTAMP);

			ps.setString(8, corso.getCodfiscaleFondatore());
			ps.setInt(9, corso.getIdCorso());

			ps.executeUpdate();
		}
	}

	public Optional<CorsoCucina> findById(int id) throws SQLException {
		String sql = "SELECT * FROM corsocucina WHERE idcorsocucina = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return Optional.of(mapResultSetToCorso(rs));
			}
		}
		return Optional.empty();
	}

	public List<CorsoCucina> getAll() throws SQLException {
		List<CorsoCucina> list = new ArrayList<>();
		String sql = "SELECT * FROM corsocucina ORDER BY nomecorso";
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				CorsoCucina corso = mapResultSetToCorso(rs);
				corso.setNumeroSessioni(getNumeroSessioniPerCorso(corso.getIdCorso()));
				list.add(corso);
			}
		}
		return list;
	}

	public void delete(int id) throws SQLException {
		String sql = "DELETE FROM corsocucina WHERE idcorsocucina = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, id);
			ps.executeUpdate();
		}
	}

	public List<CorsoCucina> findByNomeOrArgomento(String filtro) throws SQLException {
		List<CorsoCucina> list = new ArrayList<>();
		String sql = "SELECT * FROM corsocucina WHERE nomecorso ILIKE ? OR argomento ILIKE ? ORDER BY datainiziocorso";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, "%" + filtro + "%");
			ps.setString(2, "%" + filtro + "%");

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					CorsoCucina corso = mapResultSetToCorso(rs);
					corso.setNumeroSessioni(getNumeroSessioniPerCorso(corso.getIdCorso()));
					list.add(corso);
				}
			}
		}
		return list;
	}

	private CorsoCucina mapResultSetToCorso(ResultSet rs) throws SQLException {
		String nome = rs.getString("nomecorso");
		double prezzo = rs.getDouble("prezzo");
		String argomento = rs.getString("argomento");
		String freqStr = rs.getString("frequenzacorso");
		Frequenza freq = freqStr != null ? Frequenza.valueOf(freqStr) : null;
		int numeroPosti = rs.getInt("numeroposti");

		CorsoCucina corso = new CorsoCucina(nome, prezzo, argomento, freq, numeroPosti);
		corso.setIdCorso(rs.getInt("idcorsocucina"));

		corso.setCodfiscaleFondatore(rs.getString("codfiscalefondatore"));

		Timestamp tsInizio = rs.getTimestamp("datainiziocorso");
		Timestamp tsFine = rs.getTimestamp("datafinecorso");
		if (tsInizio != null)
			corso.setDataInizioCorsoFromDB(tsInizio.toLocalDateTime());
		if (tsFine != null)
			corso.setDataFineCorsoFromDB(tsFine.toLocalDateTime());

		return corso;
	}

	public int getNumeroSessioniPerCorso(int idCorso) throws SQLException {
		String sql = "SELECT COUNT(*) AS num_sessioni FROM sessione WHERE idcorsocucina = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, idCorso);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("num_sessioni");
				}
			}
		}
		return 0;
	}

	public CorsoCucina getCorsoCompleto(int idCorso) throws SQLException {
		String sql = "SELECT * FROM corsocucina WHERE idcorsocucina = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idCorso);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					CorsoCucina corso = mapResultSetToCorso(rs);
					corso.setNumeroSessioni(getNumeroSessioniPerCorso(idCorso));
					return corso;
				} else {
					throw new SQLException("Nessun corso trovato con ID " + idCorso);
				}
			}
		}
	}

}
