package model;

import java.time.LocalDateTime;
import java.util.*;

public class GestoreAdesioni {

    private Set<Adesione> adesioni = new HashSet<>();

    // Invia adesione
    public boolean inviaAdesione(InPresenza sessione, Utente utente, LocalDateTime dataAdesione) {
        if (sessione == null || utente == null || dataAdesione == null) return false;

        Adesione nuova = new Adesione(utente, sessione, dataAdesione);

        // Controllo duplicati (grazie a equals su utente+sessione)
        if (adesioni.contains(nuova)) {
            System.out.println(utente.getNome() + " ha già inviato adesione per questa sessione.");
            return false;
        }

        // Controllo capienza
        long adesioniAttive = adesioni.stream()
                .filter(a -> a.getSessione().equals(sessione) && a.isStato())
                .count();

        if (adesioniAttive >= sessione.getNumeroPosti()) {
            System.out.println("La sessione è già piena!");
            return false;
        }

        // Aggiungi adesione
        adesioni.add(nuova);
        sessione.aggiungiAdesione(nuova);
        System.out.println(utente.getNome() + " ha inviato adesione con successo.");
        return true;
    }

    // Ritira adesione
    public boolean ritiraAdesione(InPresenza sessione, Utente utente) {
        for (Adesione a : adesioni) {
            if (a.getSessione().equals(sessione) && a.getUtente().equals(utente) && a.isStato()) {
                a.setStato(false);
                System.out.println(utente.getNome() + " ha ritirato l'adesione.");
                return true;
            }
        }
        return false;
    }

    // Recupera utenti con adesione attiva
    public Set<Utente> getUtentiAdesione(InPresenza sessione) {
        Set<Utente> attivi = new HashSet<>();
        for (Adesione a : adesioni) {
            if (a.getSessione().equals(sessione) && a.isStato()) {
                attivi.add(a.getUtente());
            }
        }
        return attivi;
    }
}
