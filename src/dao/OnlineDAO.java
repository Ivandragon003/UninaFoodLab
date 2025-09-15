package dao;

import model.Online;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OnlineDAO {

   
	public int save(Online sessione) throws SQLException {
	    String sql = "INSERT INTO sessione " +
	                 "(datainiziosessione, datafinesessione, tipo, piattaformastreaming) " +
	                 "VALUES (?, ?, 'online', ?)";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

	        ps.setTimestamp(1, Timestamp.valueOf(sessione.getDataInizioSessione()));
	        ps.setTimestamp(2, Timestamp.valueOf(sessione.getDataFineSessione()));
	        ps.setString(3, sessione.getPiattaformaStreaming());

	        ps.executeUpdate();

	        try (ResultSet rs = ps.getGeneratedKeys()) {
	            if (rs.next()) {
	                return rs.getInt(1); 
	            }
	        }
	    }
	    return -1; 
	}



	public void update(int id, Online sessione) throws SQLException {
	    String sql = "UPDATE sessione SET datainiziosessione=?, datafinesessione=?, piattaformastreaming=? WHERE idsessione=?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {

	        ps.setTimestamp(1, Timestamp.valueOf(sessione.getDataInizioSessione()));
	        ps.setTimestamp(2, Timestamp.valueOf(sessione.getDataFineSessione()));
	        ps.setString(3, sessione.getPiattaformaStreaming());
	        ps.setInt(4, id);

	        ps.executeUpdate();
	    }
	}


    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM sessione WHERE idsessione=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Optional<Online> findById(int id) throws SQLException {
        String sql = "SELECT * FROM sessione WHERE idsessione=? AND tipo='online'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToSessione(rs));
            }
        }
        return Optional.empty();
    }

    public List<Online> getAll() throws SQLException {
        List<Online> list = new ArrayList<>();
        String sql = "SELECT * FROM sessione WHERE tipo='online' ORDER BY datainiziosessione";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSetToSessione(rs));
        }
        return list;
    }

    private Online mapResultSetToSessione(ResultSet rs) throws SQLException {
        LocalDateTime inizio = rs.getTimestamp("datainiziosessione").toLocalDateTime();
        LocalDateTime fine = rs.getTimestamp("datafinesessione").toLocalDateTime();
        String piattaforma = rs.getString("piattaformastreaming");
        Online sessione = new Online(inizio, fine, piattaforma);
      
        return sessione;
    }
}
