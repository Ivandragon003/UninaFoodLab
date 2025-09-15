import dao.IscrizioneDAO;
import model.CorsoCucina;
import model.Iscrizione;
import model.Utente;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public class Main {

    public static void main(String[] args) {
        IscrizioneDAO dao = new IscrizioneDAO();

        // Creiamo oggetti Utente e Corso (ID del corso deve già esistere nel DB!)
        Utente u = new Utente("RSSMRA85M01H501U", "Mario", "Rossi");
        CorsoCucina c = new CorsoCucina("Pasta Fresca", 100.0, "Cucina Italiana", null, 20, 5);
        // idCorso deve essere un ID esistente nel DB per funzionare correttamente
        // es: c.getIdCorso() = 1 se nel DB c'è un corso con idCorso = 1

        // Creiamo iscrizione
        Iscrizione i = new Iscrizione(u, c, true);
        i.setVotiAvuti(8);

        try {
            // Salvataggio
            dao.save(i);
            System.out.println("Iscrizione salvata!");

            // Controllo esistenza
            boolean exists = dao.exists(u.getCodFiscale(), c.getIdCorso());
            System.out.println("Esiste iscrizione? " + exists);

            // Lettura tutte le iscrizioni
            List<Iscrizione> iscrizioni = dao.getAllFull();
            System.out.println("\nElenco iscrizioni completo:");
            for (Iscrizione iscr : iscrizioni) {
                System.out.println(iscr.toStringUtente() + " - " + iscr.toStringCorso() +
                                   " - " + iscr.toStringVotiAvuti() + " - " + iscr.toStringStato());
            }

            // Cancellazione
            dao.delete(u.getCodFiscale(), c.getIdCorso());
            System.out.println("\nIscrizione cancellata!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
