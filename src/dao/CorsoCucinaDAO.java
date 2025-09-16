package dao;

import model.CorsoCucina;
import model.Frequenza;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CorsoCucinaDAO {

	public void save(CorsoCucina corso) throws SQLException {
		String sql = "INSERT INTO corsocucina "
				+ "(nomeCorso, argomento, frequenzaCorso, prezzo, numeroPosti, numeroSessioni, dataInizioCorso, dataFineCorso) "
				+ "VALUES (?, ?, ?::frequenza, ?, ?, ?, ?, ?)";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, corso.getNomeCorso());
			ps.setString(2, corso.getArgomento());
			ps.setString(3, corso.getFrequenzaCorso().name());
			ps.setBigDecimal(4, java.math.BigDecimal.valueOf(corso.getPrezzo()));
			ps.setInt(5, corso.getNumeroPosti());
			ps.setInt(6, corso.getNumeroSessioni());
			ps.setTimestamp(7, Timestamp.valueOf(corso.getDataInizioCorso()));
			ps.setTimestamp(8, Timestamp.valueOf(corso.getDataFineCorso()));

			ps.executeUpdate();
		}
	}

	public void update(CorsoCucina corso) throws SQLException {
		String sql = "UPDATE corsocucina SET nomeCorso = ?, argomento = ?, frequenzaCorso = ?::frequenza, "
				+ "prezzo = ?, numeroPosti = ?, numeroSessioni = ?, dataInizioCorso = ?, dataFineCorso = ? "
				+ "WHERE idCorsoCucina = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, corso.getNomeCorso());
			ps.setString(2, corso.getArgomento());
			ps.setString(3, corso.getFrequenzaCorso().name());
			ps.setBigDecimal(4, java.math.BigDecimal.valueOf(corso.getPrezzo()));
			ps.setInt(5, corso.getNumeroPosti());
			ps.setInt(6, corso.getNumeroSessioni());
			ps.setTimestamp(7, Timestamp.valueOf(corso.getDataInizioCorso()));
			ps.setTimestamp(8, Timestamp.valueOf(corso.getDataFineCorso()));
			ps.setInt(9, corso.getIdCorso());

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
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

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
	    CorsoCucina corso = new CorsoCucina(
	        rs.getString("nomeCorso"),
	        rs.getDouble("prezzo"),
	        rs.getString("argomento"),
	        Frequenza.valueOf(rs.getString("frequenzaCorso")),
	        rs.getInt("numeroPosti"),    // senza alias
	        rs.getInt("numeroSessioni")  // senza alias
	    );

	    Timestamp tsInizio = rs.getTimestamp("dataInizioCorso");
	    Timestamp tsFine = rs.getTimestamp("dataFineCorso");
	    if (tsInizio != null) corso.setDataInizioCorsoFromDB(tsInizio.toLocalDateTime());
	    if (tsFine != null) corso.setDataFineCorsoFromDB(tsFine.toLocalDateTime());

	    return corso;
	}


}