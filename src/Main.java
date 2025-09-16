import dao.RicettaDAO;
import dao.UsaDAO;
import model.Ingrediente;
import model.Ricetta;
import model.Usa;
import util.DBConnection;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        RicettaDAO ricettaDAO = new RicettaDAO();
        UsaDAO usaDAO = new UsaDAO();

        // nomi brevi e fissi per rispettare il limite VARCHAR(50)
        String uniqueRicettaName = "TestRicetta";
        String uniqueIngredienteName = "TestIngrediente";

        Ricetta ricetta = new Ricetta(uniqueRicettaName, 25);
        Ingrediente ingrediente = new Ingrediente(uniqueIngredienteName, "tipoTest");

        Integer idRicetta = null;
        Integer idIngrediente = null;
        boolean usaInserted = false;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(true);

            // 1) Inserisco la ricetta
            System.out.println("Salvo ricetta: " + ricetta.getNome());
            ricettaDAO.save(ricetta);

            // Recupero l'id della ricetta appena inserita
            idRicetta = fetchIdRicettaByName(conn, uniqueRicettaName);
            if (idRicetta == null) throw new SQLException("Id ricetta non trovato");
            System.out.println("Id ricetta inserita: " + idRicetta);
            setPrivateIntField(ricetta, "idRicetta", idRicetta);

            // 2) Inserisco l'ingrediente
            System.out.println("Salvo ingrediente: " + ingrediente.getNome());
            idIngrediente = insertIngredienteAndReturnId(conn, ingrediente.getNome(), ingrediente.getTipo());
            if (idIngrediente == null) throw new SQLException("Id ingrediente non trovato");
            System.out.println("Id ingrediente inserito: " + idIngrediente);
            setPrivateIntField(ingrediente, "idIngrediente", idIngrediente);

            // 3) Inserisco in Usa
            Usa usa = new Usa(ricetta, ingrediente, 123.45);
            System.out.println("Salvo usa (ricetta-id=" + ricetta.getIdRicetta() +
                    ", ingrediente-id=" + ingrediente.getIdIngrediente() + ")");
            usaDAO.save(usa);
            usaInserted = true;
            System.out.println("Inserimento in 'usa' effettuato.");

            // 4) Update quantità
            usa.setQuantita(200.0);
            System.out.println("Update quantità in usa a 200.0");
            usaDAO.updateQuantita(usa);
            System.out.println("Update effettuato.");

            // 5) Get all
            System.out.println("\n--- Tutti gli Usa ---");
            List<Usa> all = usaDAO.getAll();
            for (Usa u : all) {
                System.out.println(u.toStringRicetta() + " | " + u.toStringIngrediente() +
                        " | " + u.toStringQuantita());
            }

            // 6) Get by ricetta
            System.out.println("\n--- Usa per la ricetta appena creata ---");
            List<Usa> byRic = usaDAO.getByRicetta(uniqueRicettaName);
            for (Usa u : byRic) {
                System.out.println(u.toStringRicetta() + " | " + u.toStringIngrediente() +
                        " | " + u.toStringQuantita());
            }

            // 7) Cleanup normale
            System.out.println("\nPulizia: elimino la riga in 'usa'.");
            usaDAO.delete(usa);
            usaInserted = false;

            System.out.println("Elimino la ricetta (id=" + idRicetta + ").");
            ricettaDAO.delete(idRicetta);
            idRicetta = null;

            System.out.println("Elimino l'ingrediente (id=" + idIngrediente + ").");
            deleteIngredienteById(conn, idIngrediente);
            idIngrediente = null;

            System.out.println("\nTutti i test sono terminati con successo.");

        } catch (Exception e) {
            System.err.println("Errore durante i test: " + e.getMessage());
            e.printStackTrace();

            // Cleanup in caso di errore
            try (Connection conn2 = DBConnection.getConnection()) {
                if (usaInserted && idRicetta != null && idIngrediente != null) {
                    try (PreparedStatement ps = conn2.prepareStatement(
                            "DELETE FROM Usa WHERE idRicetta = ? AND idIngrediente = ?")) {
                        ps.setInt(1, idRicetta);
                        ps.setInt(2, idIngrediente);
                        ps.executeUpdate();
                    }
                }

                if (idRicetta != null) new RicettaDAO().delete(idRicetta);
                if (idIngrediente != null) deleteIngredienteById(conn2, idIngrediente);

            } catch (SQLException ex) {
                System.err.println("Errore durante cleanup: " + ex.getMessage());
            }
            System.out.println("Cleanup completato dopo errore.");
        }
    }

    // ---------------- helper methods ----------------
    private static Integer fetchIdRicettaByName(Connection conn, String nome) throws SQLException {
        String sql = "SELECT idRicetta FROM ricetta WHERE nome = ? ORDER BY idRicetta DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("idRicetta");
            }
        }
        return null;
    }

    private static Integer insertIngredienteAndReturnId(Connection conn, String nome, String tipo) throws SQLException {
        String sql = "INSERT INTO ingrediente (nome, tipo) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nome);
            ps.setString(2, tipo);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        String fallback = "SELECT idIngrediente FROM ingrediente WHERE nome = ? ORDER BY idIngrediente DESC LIMIT 1";
        try (PreparedStatement ps2 = conn.prepareStatement(fallback)) {
            ps2.setString(1, nome);
            try (ResultSet rs = ps2.executeQuery()) {
                if (rs.next()) return rs.getInt("idIngrediente");
            }
        }
        return null;
    }

    private static void deleteIngredienteById(Connection conn, int idIngrediente) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM ingrediente WHERE idIngrediente = ?")) {
            ps.setInt(1, idIngrediente);
            ps.executeUpdate();
        }
    }

    private static void setPrivateIntField(Object target, String fieldName, int value) throws Exception {
        Field f = null;
        Class<?> cls = target.getClass();
        while (cls != null) {
            try {
                f = cls.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException nsfe) {
                cls = cls.getSuperclass();
            }
        }
        if (f == null) throw new NoSuchFieldException("Campo '" + fieldName + "' non trovato.");
        f.setAccessible(true);
        f.setInt(target, value);
    }
}
