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
		String sql = "INSERT INTO corsocucina "
				+ "(nomeCorso, argomento, frequenzaCorso, prezzo, numeroPosti, dataInizioCorso, dataFineCorso) "
				+ "VALUES (?, ?, ?::frequenza, ?, ?, ?, ?) RETURNING idCorsoCucina";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, corso.getNomeCorso());
			ps.setString(2, corso.getArgomento());
			ps.setString(3, corso.getFrequenzaCorso() != null ? corso.getFrequenzaCorso().name() : null);
			ps.setBigDecimal(4, BigDecimal.valueOf(corso.getPrezzo()));
			ps.setInt(5, corso.getNumeroPosti());

			if (corso.getDataInizioCorso() != null)
				ps.setTimestamp(6, Timestamp.valueOf(corso.getDataInizioCorso()));
			else
				ps.setTimestamp(6, null);

			if (corso.getDataFineCorso() != null)
				ps.setTimestamp(7, Timestamp.valueOf(corso.getDataFineCorso()));
			else
				ps.setTimestamp(7, null);

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
		String sql = "UPDATE corsocucina SET nomeCorso = ?, argomento = ?, frequenzaCorso = ?::frequenza, "
				+ "prezzo = ?, numeroPosti = ?, dataInizioCorso = ?, dataFineCorso = ? " + "WHERE idCorsoCucina = ?";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, corso.getNomeCorso());
			ps.setString(2, corso.getArgomento());
			ps.setString(3, corso.getFrequenzaCorso() != null ? corso.getFrequenzaCorso().name() : null);
			ps.setBigDecimal(4, BigDecimal.valueOf(corso.getPrezzo()));
			ps.setInt(5, corso.getNumeroPosti());

			if (corso.getDataInizioCorso() != null)
				ps.setTimestamp(6, Timestamp.valueOf(corso.getDataInizioCorso()));
			else
				ps.setTimestamp(6, null);

			if (corso.getDataFineCorso() != null)
				ps.setTimestamp(7, Timestamp.valueOf(corso.getDataFineCorso()));
			else
				ps.setTimestamp(7, null);

			ps.setInt(8, corso.getIdCorso());

			ps.executeUpdate();
		}
	}

	public Optional<CorsoCucina> findById(int id) throws SQLException {
		String sql = "SELECT * FROM corsocucina WHERE idCorsoCucina = ?";
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
		String sql = "SELECT * FROM corsocucina ORDER BY nomeCorso";
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next())
				list.add(mapResultSetToCorso(rs));
		}
		return list;
	}

	public void delete(int id) throws SQLException {
		String sql = "DELETE FROM corsocucina WHERE idCorsoCucina = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, id);
			ps.executeUpdate();
		}
	}

	public List<CorsoCucina> findByNomeEsatto(String nome) throws SQLException {
		List<CorsoCucina> list = new ArrayList<>();
		String sql = "SELECT * FROM corsocucina WHERE nomeCorso = ? ORDER BY dataInizioCorso";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, nome);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapResultSetToCorso(rs));
				}
			}
		}
		return list;
	}

	private CorsoCucina mapResultSetToCorso(ResultSet rs) throws SQLException {
		String nome = rs.getString("nomeCorso");
		double prezzo = rs.getDouble("prezzo");
		String argomento = rs.getString("argomento");

		String freqStr = rs.getString("frequenzaCorso");
		Frequenza freq = freqStr != null ? Frequenza.valueOf(freqStr) : null;

		int numeroPosti = rs.getInt("numeroPosti");

		CorsoCucina corso = new CorsoCucina(nome, prezzo, argomento, freq, numeroPosti);
		corso.setIdCorso(rs.getInt("idCorsoCucina"));

		Timestamp tsInizio = rs.getTimestamp("dataInizioCorso");
		Timestamp tsFine = rs.getTimestamp("dataFineCorso");
		if (tsInizio != null)
			corso.setDataInizioCorsoFromDB(tsInizio.toLocalDateTime());
		if (tsFine != null)
			corso.setDataFineCorsoFromDB(tsFine.toLocalDateTime());

		return corso;
	}
}
