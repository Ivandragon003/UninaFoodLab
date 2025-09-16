package dao;

import model.Chef;
import model.Lavora;
import model.Ristorante;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LavoraDAO {

    public void save(Lavora l, int idRistoranteGenerato) throws SQLException {
        if (l == null || l.getChef() == null) {
            throw new IllegalArgumentException("Lavora o Chef null");
        }
        String sql = "INSERT INTO lavora (codFiscale, idRistorante) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, l.getChef().getCodFiscale());
            ps.setInt(2, idRistoranteGenerato);
            ps.executeUpdate();
        }
    }

    public void save(Lavora l) throws SQLException {
        if (l == null || l.getChef() == null || l.getRistorante() == null) {
            throw new IllegalArgumentException("Lavora, Chef o Ristorante null");
        }
        Integer id = l.getRistorante().getIdRistorante();
        if (id == null) {
            throw new IllegalArgumentException("idRistorante mancante nell'oggetto Ristorante");
        }
        String sql = "INSERT INTO lavora (codFiscale, idRistorante) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, l.getChef().getCodFiscale());
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public List<Lavora> getAll() throws SQLException {
        List<Lavora> list = new ArrayList<>();
        String sql = "SELECT codFiscale, idRistorante FROM lavora";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String cf = rs.getString("codFiscale");
                int idRistorante = rs.getInt("idRistorante");
                
                // Crea oggetti placeholder con dati minimi
                Chef chef = new Chef(cf, "", "", true, "placeholderUsername", "placeholder123");
                Ristorante ristorante = new Ristorante("", "00000000000", "");
                
                
                setIdRistorante(ristorante, idRistorante);
                
                list.add(new Lavora(chef, ristorante));
            }
        }
        return list;
    }

    public List<Lavora> getAllFull() throws SQLException {
        List<Lavora> list = new ArrayList<>();
        String sql = "SELECT l.codFiscale, r.idRistorante, r.partitaIva, r.nome AS r_nome, r.via, r.stelleMichelin, " +
                     "c.nome AS c_nome, c.cognome, c.anniEsperienza, c.disponibilita, c.username, c.password, " +
                     "c.email, c.dataNascita " +
                     "FROM lavora l " +
                     "JOIN ristorante r ON l.idRistorante = r.idRistorante " +
                     "JOIN chef c ON l.codFiscale = c.codFiscale";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Crea oggetto Chef completo
                Chef chef = new Chef(
                        rs.getString("codFiscale"),
                        rs.getString("c_nome"),
                        rs.getString("cognome"),
                        rs.getBoolean("disponibilita"),
                        rs.getString("username"),
                        rs.getString("password")
                );
                chef.setAnniEsperienza(rs.getInt("anniEsperienza"));
                chef.setEmail(rs.getString("email"));
                Date dataNascita = rs.getDate("dataNascita");
                if (dataNascita != null) {
                    chef.setDataNascita(dataNascita.toLocalDate());
                }

                // Crea oggetto Ristorante completo
                String via = rs.getString("via");
                String partitaIva = rs.getString("partitaIva");
                String nome = rs.getString("r_nome");
                int idRistorante = rs.getInt("idRistorante");

                Ristorante r = new Ristorante(
                        via != null ? via : "",
                        partitaIva != null ? partitaIva : "00000000000",
                        nome != null ? nome : ""
                );
                r.setStelleMichelin(rs.getInt("stelleMichelin"));
                
                // Usa reflection per settare l'idRistorante privato
                setIdRistorante(r, idRistorante);

                list.add(new Lavora(chef, r));
            }
        }
        return list;
    }

    public void delete(Lavora l, int idRistoranteGenerato) throws SQLException {
        if (l == null || l.getChef() == null) {
            throw new IllegalArgumentException("Lavora o Chef null");
        }
        String sql = "DELETE FROM lavora WHERE codFiscale = ? AND idRistorante = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, l.getChef().getCodFiscale());
            ps.setInt(2, idRistoranteGenerato);
            ps.executeUpdate();
        }
    }

    public void delete(String codFiscale, int idRistorante) throws SQLException {
        String sql = "DELETE FROM lavora WHERE codFiscale = ? AND idRistorante = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codFiscale);
            ps.setInt(2, idRistorante);
            ps.executeUpdate();
        }
    }

    // Metodo aggiuntivo per verificare se esiste una relazione
    public boolean exists(String codFiscale, int idRistorante) throws SQLException {
        String sql = "SELECT 1 FROM lavora WHERE codFiscale = ? AND idRistorante = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codFiscale);
            ps.setInt(2, idRistorante);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Metodo per ottenere tutti i ristoranti di uno chef
    public List<Ristorante> getRistorantiByChef(String codFiscale) throws SQLException {
        List<Ristorante> list = new ArrayList<>();
        String sql = "SELECT r.idRistorante, r.partitaIva, r.nome, r.via, r.stelleMichelin " +
                     "FROM lavora l " +
                     "JOIN ristorante r ON l.idRistorante = r.idRistorante " +
                     "WHERE l.codFiscale = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codFiscale);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ristorante r = new Ristorante(
                            rs.getString("via"),
                            rs.getString("partitaIva"),
                            rs.getString("nome")
                    );
                    r.setStelleMichelin(rs.getInt("stelleMichelin"));
                    setIdRistorante(r, rs.getInt("idRistorante"));
                    list.add(r);
                }
            }
        }
        return list;
    }

    // Metodo per ottenere tutti gli chef di un ristorante
    public List<Chef> getChefByRistorante(int idRistorante) throws SQLException {
        List<Chef> list = new ArrayList<>();
        String sql = "SELECT c.codFiscale, c.nome, c.cognome, c.anniEsperienza, " +
                     "c.disponibilita, c.username, c.password, c.email, c.dataNascita " +
                     "FROM lavora l " +
                     "JOIN chef c ON l.codFiscale = c.codFiscale " +
                     "WHERE l.idRistorante = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRistorante);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Chef chef = new Chef(
                            rs.getString("codFiscale"),
                            rs.getString("nome"),
                            rs.getString("cognome"),
                            rs.getBoolean("disponibilita"),
                            rs.getString("username"),
                            rs.getString("password")
                    );
                    chef.setAnniEsperienza(rs.getInt("anniEsperienza"));
                    chef.setEmail(rs.getString("email"));
                    Date dataNascita = rs.getDate("dataNascita");
                    if (dataNascita != null) {
                        chef.setDataNascita(dataNascita.toLocalDate());
                    }
                    list.add(chef);
                }
            }
        }
        return list;
    }

    // Metodo helper per settare l'idRistorante usando reflection
    private void setIdRistorante(Ristorante r, int id) {
        try {
            java.lang.reflect.Field field = Ristorante.class.getDeclaredField("idRistorante");
            field.setAccessible(true);
            field.set(r, id);
        } catch (Exception e) {
            throw new RuntimeException("Impossibile settare idRistorante", e);
        }
    }
}