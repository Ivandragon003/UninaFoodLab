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

    // Inserimento
    public int save(Online sessione) throws SQLException {
        String sql = "INSERT INTO sessione " +
                     "(datainiziosessione, datafinesessione, tipo, piattaformastreaming, idcorsocucina) " +
                     "VALUES (?, ?, 'online', ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, Timestamp.valueOf(sessione.getDataInizioSessione()));
            ps.setTimestamp(2, Timestamp.valueOf(sessione.getDataFineSessione()));
            ps.setString(3, sessione.getPiattaformaStreaming());
            ps.setInt(4, sessione.getCorsoCucina().getIdCorso());

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
    public void update(int id, Online sessione) throws SQLException {
        String sql = "UPDATE sessione SET datainiziosessione=?, datafinesessione=?, piattaformastreaming=?, idcorsocucina=? WHERE idsessione=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(sessione.getDataInizioSessione()));
            ps.setTimestamp(2, Timestamp.valueOf(sessione.getDataFineSessione()));
            ps.setString(3, sessione.getPiattaformaStreaming());
            ps.setInt(4, sessione.getCorsoCucina().getIdCorso());
            ps.setInt(5, id);

            ps.executeUpdate();
        }
    }

    // Eliminazione
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM sessione WHERE idsessione=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Lettura per ID
    public Optional<Online> findById(int id) throws SQLException {
        String sql = "SELECT * FROM sessione WHERE idsessione=? AND tipo='online'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToOnline(rs));
            }
        }
        return Optional.empty();
    }

    // Lettura tutte le sessioni online
    public List<Online> getAll() throws SQLException {
        List<Online> list = new ArrayList<>();
        String sql = "SELECT * FROM sessione WHERE tipo='online' ORDER BY datainiziosessione";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) list.add(mapResultSetToOnline(rs));
        }
        return list;
    }

    // Mapping ResultSet -> Online
    private Online mapResultSetToOnline(ResultSet rs) throws SQLException {
        // Recupera le date e la piattaforma
        LocalDateTime inizio = rs.getTimestamp("datainiziosessione").toLocalDateTime();
        LocalDateTime fine = rs.getTimestamp("datafinesessione").toLocalDateTime();
        String piattaforma = rs.getString("piattaformastreaming");

        // Crea l'oggetto Online
        Online sessione = new Online(inizio, fine, piattaforma);

        // Imposta l'ID generato dal DB
        sessione.setIdSessione(rs.getInt("idsessione"));

        // Recupera il corso dal DB
        int idCorso = rs.getInt("idcorsocucina");
        CorsoCucina corso = null;
        String sqlCorso = "SELECT * FROM corsoCucina WHERE idCorsoCucina = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sqlCorso)) {
            ps.setInt(1, idCorso);
            try (ResultSet rsCorso = ps.executeQuery()) {
                if (rsCorso.next()) {
                    Frequenza freq = null;
                    try {
                        freq = Frequenza.valueOf(rsCorso.getString("frequenzaCorso"));
                    } catch (IllegalArgumentException e) {
                        // Se la stringa non corrisponde all'enum, lascia null o gestisci default
                    }
                    corso = new CorsoCucina(
                        rsCorso.getString("nomeCorso"),
                        rsCorso.getDouble("prezzo"),
                        rsCorso.getString("categoria"),
                        freq,
                        rsCorso.getInt("numeroPosti"),
                        rsCorso.getInt("numeroSessioni")
                    );
                }
            }
        }

        // Associa il corso all'oggetto Online
        sessione.setCorsoCucina(corso);

        return sessione;
    }
}
