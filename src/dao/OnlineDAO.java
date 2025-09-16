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

    // Inserimento sessione online
    public int save(Online sessione) throws SQLException {
        String sql = "INSERT INTO sessione (datainiziosessione, datafinesessione, tipo, piattaformastreaming, idcorsocucina) "
                   + "VALUES (?, ?, 'online', ?, ?)";
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

    // Aggiornamento sessione online
    public void update(int id, Online sessione) throws SQLException {
        String sql = "UPDATE sessione SET datainiziosessione=?, datafinesessione=?, piattaformastreaming=?, idcorsocucina=? "
                   + "WHERE idsessione=?";
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

    // Eliminazione sessione
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM sessione WHERE idsessione=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Lettura sessione online per ID
    public Optional<Online> findById(int id) throws SQLException {
        String sql = "SELECT s.*, "
                   + "c.nomecorso, c.argomento, c.prezzo, c.numeroposti, c.numerosessioni, c.frequenzacorso "
                   + "FROM sessione s "
                   + "LEFT JOIN corsocucina c ON s.idcorsocucina = c.idcorsoCucina "
                   + "WHERE s.idsessione = ? AND s.tipo = 'online'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToOnline(rs));
                }
            }
        }
        return Optional.empty();
    }

    // Lettura tutte le sessioni online
    public List<Online> getAll() throws SQLException {
        List<Online> list = new ArrayList<>();
        String sql = "SELECT s.*, "
                   + "c.nomecorso, c.argomento, c.prezzo, c.numeroposti, c.numerosessioni, c.frequenzacorso "
                   + "FROM sessione s "
                   + "LEFT JOIN corsocucina c ON s.idcorsocucina = c.idcorsoCucina "
                   + "WHERE s.tipo = 'online' "
                   + "ORDER BY s.datainiziosessione";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToOnline(rs));
            }
        }
        return list;
    }

    // Mapping ResultSet
    private Online mapResultSetToOnline(ResultSet rs) throws SQLException {
        LocalDateTime inizio = rs.getTimestamp("datainiziosessione").toLocalDateTime();
        LocalDateTime fine = rs.getTimestamp("datafinesessione").toLocalDateTime();
        String piattaforma = rs.getString("piattaformastreaming");

        Online sessione = new Online(inizio, fine, piattaforma);

        String nomeCorso = rs.getString("nomecorso");
        if (nomeCorso != null) {
            Frequenza freq = null;
            String freqStr = rs.getString("frequenzacorso");
            if (freqStr != null) freq = Frequenza.valueOf(freqStr);

            int numeroposti = rs.getInt("numeroposti");
            if (rs.wasNull() || numeroposti <= 0) numeroposti = 1; 

            int numerosessioni = rs.getInt("numerosessioni");
            if (rs.wasNull() || numerosessioni <= 0) numerosessioni = 1; 

            CorsoCucina corso = new CorsoCucina(
                    nomeCorso,
                    rs.getDouble("prezzo"),
                    rs.getString("argomento"),
                    freq,
                    numeroposti,
                    numerosessioni
            );

            sessione.setCorsoCucina(corso);
        }

        return sessione;
    }

}
