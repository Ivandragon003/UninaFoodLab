package dao;

import model.Adesione;
import model.Utente;
import model.InPresenza;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class AdesioneDAO {

	public void save(Adesione adesione) throws SQLException {
	    String sql = "INSERT INTO adesione (idsessione, codfiscale, stato, dataadesione) VALUES (?, ?, ?, ?)";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {

	        ps.setInt(1, adesione.getSessione().getIdSessione()); 
	        ps.setString(2, adesione.getUtente().getCodFiscale());
	        ps.setBoolean(3, adesione.isStato());
	        ps.setTimestamp(4, Timestamp.valueOf(adesione.getDataAdesione()));

	        ps.executeUpdate();
	    }
	}

	public void delete(Adesione adesione) throws SQLException {
	    String sql = "DELETE FROM adesione WHERE idsessione = ? AND codfiscale = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {

	        ps.setInt(1, adesione.getSessione().getIdSessione());
	        ps.setString(2, adesione.getUtente().getCodFiscale());
	        ps.executeUpdate();
	    }
	}

    public void deleteBySessione(int idSessione) throws SQLException {
        String sql = "DELETE FROM adesione WHERE idsessione = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idSessione);
            ps.executeUpdate();
        }
    }

    public boolean exists(Adesione adesione) throws SQLException {
        String sql = "SELECT 1 FROM adesione WHERE idsessione = ? AND codfiscale = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, adesione.getSessione().getIdSessione());
            ps.setString(2, adesione.getUtente().getCodFiscale());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Adesione> getAll() throws SQLException {
        List<Adesione> list = new ArrayList<>();
        String sql = "SELECT a.codfiscale, a.idsessione, a.stato, a.dataadesione, " +
                     "u.nome, u.cognome, u.email, u.datanascita " +
                     "FROM adesione a JOIN utente u ON a.codfiscale = u.codfiscale";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Utente u = new Utente(
                        rs.getString("codfiscale"),
                        rs.getString("nome"),
                        rs.getString("cognome")
                );
                u.setEmail(rs.getString("email"));
                if (rs.getDate("datanascita") != null)
                    u.setDataNascita(rs.getDate("datanascita").toLocalDate());

                InPresenza s = null;

                Adesione a = new Adesione(u, s, rs.getTimestamp("dataadesione").toLocalDateTime());

              
                if (!rs.getBoolean("stato")) {
                    a.setStato(false);
                }

                list.add(a);
            }
        }

        return list;
    }


    public Set<Utente> getPartecipantiInPresenza(int idSessione) throws SQLException {
        Set<Utente> partecipanti = new HashSet<>();
        String sql = "SELECT codFiscale, nome, cognome " +
                     "FROM VistaPartecipantiInPresenza " +
                     "WHERE idSessione = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSessione);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Utente u = new Utente(
                            rs.getString("codFiscale"),
                            rs.getString("nome"),
                            rs.getString("cognome")
                    );
                    partecipanti.add(u);
                }
            }
        }
        return partecipanti;
    }
}
