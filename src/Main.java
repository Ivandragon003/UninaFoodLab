

import dao.OnlineDAO;
import dao.CorsoCucinaDAO;
import model.Online;
import model.CorsoCucina;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {
        OnlineDAO dao = new OnlineDAO();
        CorsoCucinaDAO corsoDao = new CorsoCucinaDAO(); // DAO per i corsi

        int idSessioneGenerata = -1;

        try {
            // 1️⃣ Prendi un corso esistente dal DB
            List<CorsoCucina> corsi = corsoDao.getAll();
            if (corsi.isEmpty()) {
                System.err.println("Nessun corso trovato nel DB. Inserisci prima un corso!");
                return;
            }
            CorsoCucina corso = corsi.get(0); // prendi il primo corso disponibile

            // 2️⃣ Crea sessione online
            Online sessione = new Online(
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(1).plusHours(2),
                    "Zoom"
            );
            sessione.setCorsoCucina(corso);

            // 3️⃣ Inserimento sessione online
            idSessioneGenerata = dao.save(sessione);
            System.out.println("Sessione online inserita con ID: " + idSessioneGenerata);

            // 4️⃣ Lettura tramite findById
            Optional<Online> letto = dao.findById(idSessioneGenerata);
            letto.ifPresentOrElse(
                    s -> System.out.println("Trovato: " + s.getPiattaformaStreaming() + ", corso: " +
                            (s.getCorsoCucina() != null ? s.getCorsoCucina().getNomeCorso() : "null")),
                    () -> System.out.println("Sessione non trovata")
            );

            // 5️⃣ Lettura di tutte le sessioni online
            List<Online> tutte = dao.getAll();
            System.out.println("Totale sessioni online nel DB: " + tutte.size());

            // 6️⃣ Aggiornamento della sessione
            sessione.setPiattaformaStreaming("Google Meet");
            dao.update(idSessioneGenerata, sessione);
            System.out.println("Sessione aggiornata");

            // 7️⃣ Lettura dopo aggiornamento
            letto = dao.findById(idSessioneGenerata);
            letto.ifPresent(s -> System.out.println("Dopo update: piattaforma = " + s.getPiattaformaStreaming()));

        } catch (SQLException e) {
            System.err.println("Errore DAO: " + e.getMessage());

            // Pulizia in caso di errore
            if (idSessioneGenerata != -1) {
                try {
                    dao.delete(idSessioneGenerata);
                    System.out.println("Sessione inserita rimossa a causa dell'errore.");
                } catch (SQLException ex) {
                    System.err.println("Errore durante la rimozione della sessione inserita: " + ex.getMessage());
                }
            }
        } finally {
            // Pulizia finale
            if (idSessioneGenerata != -1) {
                try {
                    dao.delete(idSessioneGenerata);
                    System.out.println("Sessione rimossa definitivamente.");
                } catch (SQLException e) {
                    System.err.println("Errore durante la rimozione finale della sessione: " + e.getMessage());
                }
            }
        }
    }
}
