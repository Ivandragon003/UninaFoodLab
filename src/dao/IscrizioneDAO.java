package dao;

import model.CorsoCucina;
import model.Iscrizione;
import model.Utente;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IscrizioneDAO {

    public void save(Iscrizione i) throws SQLException {
        if (i == null || i.getUtente() == null || i.getCorso() == null) {
            throw new IllegalArgumentException("Iscrizione, Utente o Corso null");
        }
        String sql = "INSERT INTO iscritto (codFiscale, idCorsoCucina, votiAvuti, stato) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, i.getUtente().getCodFiscale());
            ps.setInt(2, i.getCorso().getIdCorso());
            if (i.getVotiAvuti() != null) {
                ps.setInt(3, i.getVotiAvuti());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setBoolean(4, i.isStato());
            ps.executeUpdate();
        }
    }

    public void delete(String codFiscale, int idCorso) throws SQLException {
        String sql = "DELETE FROM iscritto WHERE codFiscale = ? AND idCorsoCucina = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codFiscale);
            ps.setInt(2, idCorso);
            ps.executeUpdate();
        }
    }

    public boolean exists(String codFiscale, int idCorso) throws SQLException {
        String sql = "SELECT 1 FROM iscritto WHERE codFiscale = ? AND idCorsoCucina = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codFiscale);
            ps.setInt(2, idCorso);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Iscrizione> getAll() throws SQLException {
        List<Iscrizione> list = new ArrayList<>();
        String sql = "SELECT codFiscale, idCorsoCucina, votiAvuti, stato FROM iscritto";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String cf = rs.getString("codFiscale");
                int idCorso = rs.getInt("idCorsoCucina");
                Integer voti = rs.getInt("votiAvuti");
                if (rs.wasNull()) voti = null;
                boolean stato = rs.getBoolean("stato");

                Utente u = new Utente(cf, "placeholderNome", "placeholderCognome");
                CorsoCucina c = new CorsoCucina("placeholderCorso", 0, "placeholderCat", null, 0, 0);

                Iscrizione i = new Iscrizione(u, c, stato);
                i.setVotiAvuti(voti);
                list.add(i);
            }
        }
        return list;
    }

    public List<Iscrizione> getAllFull() throws SQLException {
        List<Iscrizione> list = new ArrayList<>();
        String sql = "SELECT i.codFiscale, u.nome AS u_nome, u.cognome AS u_cognome, u.email, u.dataNascita, " +
                     "i.idCorsoCucina, c.nomeCorso, c.prezzo, c.categoria, c.frequenzaCorso, c.numeroPosti, " +
                     "c.numeroSessioni, i.votiAvuti, i.stato " +
                     "FROM iscritto i " +
                     "JOIN utente u ON i.codFiscale = u.codFiscale " +
                     "JOIN corsoCucina c ON i.idCorsoCucina = c.idCorsoCucina";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Utente u = new Utente(rs.getString("codFiscale"),
                                      rs.getString("u_nome"),
                                      rs.getString("u_cognome"));
                u.setEmail(rs.getString("email"));
                Date data = rs.getDate("dataNascita");
                if (data != null) u.setDataNascita(data.toLocalDate());

                CorsoCucina c = new CorsoCucina(
                        rs.getString("nomeCorso"),
                        rs.getDouble("prezzo"),
                        rs.getString("categoria"),
                        null,
                        rs.getInt("numeroPosti"),
                        rs.getInt("numeroSessioni")
                );

                Iscrizione i = new Iscrizione(u, c, rs.getBoolean("stato"));
                Integer voti = rs.getInt("votiAvuti");
                if (rs.wasNull()) voti = null;
                i.setVotiAvuti(voti);

                list.add(i);
            }
        }
        return list;
    }
}
