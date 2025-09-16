import dao.CorsoCucinaDAO;
import dao.OnlineDAO;
import dao.IscrizioneDAO;
import dao.InPresenzaDAO;
import model.CorsoCucina;
import model.Frequenza;
import model.Online;
import model.InPresenza;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        CorsoCucinaDAO corsoDAO = new CorsoCucinaDAO();
        OnlineDAO onlineDAO = new OnlineDAO();
        InPresenzaDAO inPresenzaDAO = new InPresenzaDAO();

        CorsoCucina corso = null;
        Online sessioneOnline = null;
        InPresenza sessionePresenza = null;

        int idCorso = -1;
        int idOnline = -1;
        int idPresenza = -1;

        try {
            // --- CREA CORSO ---
            LocalDateTime inizioCorso = LocalDateTime.now().plusDays(1);
            LocalDateTime fineCorso = inizioCorso.plusHours(4);

            corso = new CorsoCucina(
                    "Corso Test DAO",
                    100.0,
                    "Cucina Italiana",
                    Frequenza.settimanale,
                    20,
                    2
            );
            corso.setDataInizioCorso(inizioCorso);
            corso.setDataFineCorso(fineCorso);

            corsoDAO.save(corso);
            System.out.println("Corso salvato con successo!");

            // Recupera ID generato
            List<CorsoCucina> corsi = corsoDAO.findByNomeEsatto("Corso Test DAO");
            if (!corsi.isEmpty()) idCorso = corsi.get(0).getIdCorso();

            // --- CREA SESSIONE ONLINE ---
            sessioneOnline = new Online(
                    inizioCorso.plusMinutes(30),
                    inizioCorso.plusHours(1),
                    "Zoom"
            );
            sessioneOnline.setCorsoCucina(corso);

            idOnline = onlineDAO.save(sessioneOnline);
            System.out.println("Sessione online salvata con successo con ID: " + idOnline);

            // --- CREA SESSIONE IN PRESENZA ---
            sessionePresenza = new InPresenza(
                    inizioCorso.plusHours(2),
                    inizioCorso.plusHours(3),
                    "Via Roma 410",
                    "Napoli",
                    15,
                    80100
            );
            sessionePresenza.setCorsoCucina(corso); // se la sessione ha campo corso
            idPresenza = inPresenzaDAO.save(sessionePresenza);
            System.out.println("Sessione in presenza salvata con successo con ID: " + idPresenza);

            // --- TEST FUNZIONI DAO ---

            // Aggiorna corso
            corso.setPrezzo(120.0);
            corsoDAO.update(corso);
            System.out.println("Corso aggiornato con successo!");

            // Aggiorna sessione online
            sessioneOnline.setPiattaformaStreaming("Google Meet");
            onlineDAO.update(idOnline, sessioneOnline);
            System.out.println("Sessione online aggiornata con successo!");

            // Aggiorna sessione in presenza
            sessionePresenza.setNumeroPosti(18);
            inPresenzaDAO.update(idPresenza, sessionePresenza);
            System.out.println("Sessione in presenza aggiornata con successo!");

            // Lettura da DB
            Optional<CorsoCucina> corsoLetto = corsoDAO.findById(idCorso);
            corsoLetto.ifPresent(c -> System.out.println("Corso letto: " + c.toStringNomeCorso()));

            Optional<Online> onlineLetto = onlineDAO.findById(idOnline);
            onlineLetto.ifPresent(o -> System.out.println("Sessione online letta: " + o.getPiattaformaStreaming()));

            Optional<InPresenza> inPresenzaLetto = inPresenzaDAO.findById(idPresenza);
            inPresenzaLetto.ifPresent(p -> System.out.println("Sessione in presenza letta: " + p.getVia()));

            // Lista completa
            List<CorsoCucina> tuttiCorsi = corsoDAO.getAll();
            System.out.println("Numero corsi nel DB: " + tuttiCorsi.size());

            List<Online> tutteOnline = onlineDAO.getAll();
            System.out.println("Numero sessioni online nel DB: " + tutteOnline.size());

            List<InPresenza> tutteInPresenza = inPresenzaDAO.getAll();
            System.out.println("Numero sessioni in presenza nel DB: " + tutteInPresenza.size());

            System.out.println("Test completato con successo!");

        } catch (SQLException e) {
            System.err.println("Errore durante il test: " + e.getMessage());
            System.err.println("Eliminazione dei dati creati...");

            try {
                if (idOnline != -1) onlineDAO.delete(idOnline);
                if (idPresenza != -1) inPresenzaDAO.delete(idPresenza);
                if (idCorso != -1) corsoDAO.delete(idCorso);
                System.out.println("Dati eliminati.");
            } catch (SQLException ex) {
                System.err.println("Errore durante l'eliminazione: " + ex.getMessage());
            }
        }
    }
}
