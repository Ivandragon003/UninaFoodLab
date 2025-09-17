import dao.AdesioneDAO;
import dao.UtenteDAO;
import dao.InPresenzaDAO;
import model.*;
import util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        UtenteDAO utenteDAO = new UtenteDAO();
        InPresenzaDAO inPresenzaDAO = new InPresenzaDAO();
        AdesioneDAO adesioneDAO = new AdesioneDAO();

        String testCF = "RSSMRA90A10H502l"; // codice fiscale di test
        int idSessioneGenerata = -1;
        boolean utenteCreato = false;
        boolean sessioneCreato = false;
        boolean adesioneCreato = false;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // rollback se serve

            try {
                // 1. Creo un utente
                Utente u = new Utente(testCF, "Mario", "Rossi");
                u.setEmail("mario.rossi." + System.currentTimeMillis() + "@example.com");
                u.setDataNascita(LocalDate.of(1990, 5, 10));
                utenteDAO.save(u);
                utenteCreato = true;
                System.out.println("Utente inserito: " + u.getCodFiscale());

                // 2. Creo un corso fittizio
                CorsoCucina corso = new CorsoCucina("Corso Pizza", 50.0, "Pizza", Frequenza.settimanale, 30, 1);

                // 3. Creo una sessione in presenza
                InPresenza s = new InPresenza(
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(1).plusHours(2),
                        "Via Roma", "Napojli", 30, 80100
                );
                s.setCorsoCucina(corso);

                idSessioneGenerata = inPresenzaDAO.save(s);
                sessioneCreato = true;
                System.out.println("Sessione in presenza inserita con id = " + idSessioneGenerata);

                // 4. Creo un’adesione
                Adesione a = new Adesione(u, null, LocalDateTime.now());
                adesioneDAO.insert(a, idSessioneGenerata); // usa idSessione generata
                adesioneCreato = true;
                System.out.println("Adesione inserita!");

                // 5. Verifico con exists
                boolean exists = adesioneDAO.exists(idSessioneGenerata, testCF);
                System.out.println("L'adesione esiste? " + exists);

                // 6. Stampo tutte le adesioni
                List<Adesione> adesioni = adesioneDAO.getAll();
                System.out.println("Adesioni presenti in DB:");
                for (Adesione ad : adesioni) {
                    System.out.println(" - CF: " + ad.getUtente().getCodFiscale() +
                            ", stato: " + ad.isStato() +
                            ", data: " + ad.getDataAdesione());
                }

                // 7. Pulizia normale
                adesioneDAO.deleteBySessione(idSessioneGenerata);
                inPresenzaDAO.delete(idSessioneGenerata);
                utenteDAO.delete(testCF);

                conn.commit();
                System.out.println("Pulizia completata, DB ripristinato.");

            } catch (Exception ex) {
                // rollback e pulizia degli oggetti creati
                conn.rollback();
                System.err.println("Errore, rollback eseguito. Pulizia oggetti creati...");

                if (adesioneCreato) {
                    try { adesioneDAO.deleteBySessione(idSessioneGenerata); } catch (SQLException e) { /* ignoriamo */ }
                }
                if (sessioneCreato) {
                    try { inPresenzaDAO.delete(idSessioneGenerata); } catch (SQLException e) { /* ignoriamo */ }
                }
                if (utenteCreato) {
                    try { utenteDAO.delete(testCF); } catch (SQLException e) { /* ignoriamo */ }
                }

                ex.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
