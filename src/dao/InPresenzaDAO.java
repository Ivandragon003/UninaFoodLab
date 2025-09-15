package dao;

import model.InPresenza;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InPresenzaDAO {

    // Inserimento
    public int save(InPresenza sessione) throws SQLException {
        String sql = "INSERT INTO sessione " +
                     "(datainiziosessione, datafinesessione, tipo, via, citta, cap, numeroposti) " +
                     "VALUES (?, ?, 'inPresenza', ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, Timestamp.valueOf(sessione.getDataInizioSessione()));
            ps.setTimestamp(2, Timestamp.valueOf(sessione.getDataFineSessione()));
            ps.setString(3, sessione.getVia());
            ps.setString(4, sessione.getCitta());
            ps.setString(5, String.valueOf(sessione.getCAP()));
            ps.setInt(6, sessione.getNumeroPosti());

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
        String sql = "UPDATE sessione SET datainiziosessione=?, datafinesessione=?, via=?, citta=?, cap=?, numeroposti=? " +
                     "WHERE idsessione=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(sessione.getDataInizioSessione()));
            ps.setTimestamp(2, Timestamp.valueOf(sessione.getDataFineSessione()));
            ps.setString(3, sessione.getVia());
            ps.setString(4, sessione.getCitta());
            ps.setString(5, String.valueOf(sessione.getCAP()));
            ps.setInt(6, sessione.getNumeroPosti());
            ps.setInt(7, idSessione);

            ps.executeUpdate();
        }
    }

    // Lettura per ID
    public Optional<InPresenza> findById(int idSessione) throws SQLException {
        String sql = "SELECT * FROM sessione WHERE idsessione=? AND tipo='inPresenza'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idSessione);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToInPresenza(rs));
            }
        }
        return Optional.empty();
    }

    // Lettura
    public List<InPresenza> getAll() throws SQLException {
        List<InPresenza> list = new ArrayList<>();
        String sql = "SELECT * FROM sessione WHERE tipo='inPresenza' ORDER BY datainiziosessione";
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
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idSessione);
            ps.executeUpdate();
        }
    }

  
    private InPresenza mapResultSetToInPresenza(ResultSet rs) throws SQLException {
        LocalDateTime inizio = rs.getTimestamp("datainiziosessione").toLocalDateTime();
        LocalDateTime fine = rs.getTimestamp("datafinesessione").toLocalDateTime();
        String via = rs.getString("via");
        String citta = rs.getString("citta");
        int posti = rs.getInt("numeroposti");
        int cap = Integer.parseInt(rs.getString("cap"));

        InPresenza sessione = new InPresenza(inizio, fine, via, citta, posti, cap);
        sessione.setIdSessione(rs.getInt("idsessione")); // ID generato dal DB
        return sessione;
    }
}