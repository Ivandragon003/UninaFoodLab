import dao.IscrizioneDAO;
import dao.CorsoCucinaDAO;  // DAO per salvare i corsi
import model.CorsoCucina;
import model.Iscrizione;
import model.Utente;

import java.sql.SQLException;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        try {
            // DAO
            IscrizioneDAO iscrizioneDAO = new IscrizioneDAO();
            CorsoCucinaDAO corsoDAO = new CorsoCucinaDAO(); // se vuoi salvare il corso

            // ==============================
            // Utente esistente nel DB
            // ==============================
            Utente utente = new Utente("RSSMRA85M01H501U", "Mario", "Rossi");
            utente.setEmail("mario.rossi@email.com");
            utente.setDataNascita(LocalDate.of(1985, 1, 1));

            // ==============================
            // Corso esistente o nuovo
            // ==============================
            CorsoCucina corso = new CorsoCucina("Pasta Fresca", 100.0, "Cucina Italiana",
                                                null, 20, 5);
            // Se il corso non è ancora nel DB, lo salviamo
            if (corso.getIdCorso() == 0) { // id non assegnato
                int idCorso = corsoDAO.save(corso); // save restituisce l'id generato dal DB
                corso.setIdCorso(idCorso);
                System.out.println("Corso salvato con successo con ID: " + idCorso);
            }

            // ==============================
            // Creazione iscrizione
            // ==============================
            Iscrizione iscrizione = new Iscrizione(utente, corso, true);
            iscrizione.setVotiAvuti(8);

            // ==============================
            // Salvataggio iscrizione
            // ==============================
            iscrizioneDAO.save(iscrizione);
            System.out.println("Iscrizione salvata!");

            // ==============================
            // Controllo esistenza iscrizione
            // ==============================
            boolean exists = iscrizioneDAO.exists(utente.getCodFiscale(), corso.getIdCorso());
            System.out.println("Esiste iscrizione? " + (exists ? "Sì" : "No"));

            // ==============================
            // Stampa tutte le iscrizioni
            // ==============================
            for (Iscrizione i : iscrizioneDAO.getAllFull()) {
                System.out.println(i.getUtente().getNome() + " " + i.getUtente().getCognome() +
                                   " - " + i.getCorso().getNomeCorso() +
                                   " - Voti: " + (i.getVotiAvuti() != null ? i.getVotiAvuti() : "N/A") +
                                   " - Stato: " + (i.isStato() ? "Attivo" : "Non attivo"));
            }

            // ==============================
            // Cancellazione iscrizione (pulizia)
            // ==============================
            iscrizioneDAO.delete(utente.getCodFiscale(), corso.getIdCorso());
            System.out.println("Iscrizione cancellata!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
