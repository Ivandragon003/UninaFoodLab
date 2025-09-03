package model;

import java.util.*;
import java.time.LocalDate;

public class GestoreIscrizioni {

    private Set<Iscrizione> iscrizioni;

    public GestoreIscrizioni() {
        this.iscrizioni = new HashSet<>();
    }

    // Iscrizione di un utente
    public boolean iscriviUtente(CorsoCucina corso, Utente utente, LocalDate data) {
        if (corso == null || utente == null || data == null) return false;

        Iscrizione nuova = new Iscrizione(utente, corso, data);

        // Controllo doppia iscrizione attiva
        if (iscrizioni.contains(nuova)) {
            System.out.println(utente.getNome() + " è già iscritto al corso " + corso.getNomeCorso());
            return false;
        }

        // Controllo capienza
        long iscrittiAttivi = iscrizioni.stream()
                .filter(i -> i.getCorso().equals(corso) && i.isStato())
                .count();

        if (iscrittiAttivi >= corso.getNumeroPosti()) {
            System.out.println("Il corso " + corso.getNomeCorso() + " è già pieno!");
            return false;
        }

        // Aggiungi iscrizione
        iscrizioni.add(nuova);
        corso.aggiungiIscrizione(nuova); // lista bidirezionale
        System.out.println(utente.getNome() + " iscritto con successo al corso " + corso.getNomeCorso());
        return true;
    }

    // Disiscrizione di un utente
    public boolean disiscriviUtente(CorsoCucina corso, Utente utente) {
        for (Iscrizione i : iscrizioni) {
            if (i.getCorso().equals(corso) && i.getUtente().equals(utente) && i.isStato()) {
                i.setStato(false);
                corso.rimuoviIscrizione(i); // lista bidirezionale
                System.out.println(utente.getNome() + " disiscritto dal corso " + corso.getNomeCorso());
                return true;
            }
        }
        return false;
    }

    // Ritorna gli utenti attivi a un corso
    public Set<Utente> getIscrittiAttivi(CorsoCucina corso) {
        Set<Utente> attivi = new HashSet<>();
        for (Iscrizione i : iscrizioni) {
            if (i.getCorso().equals(corso) && i.isStato()) {
                attivi.add(i.getUtente());
            }
        }
        return attivi;
    }

    // Controlla lo stato di iscrizione di un utente
    public boolean getStatoIscrizione(CorsoCucina corso, Utente utente) {
        return iscrizioni.stream()
                .anyMatch(i -> i.getCorso().equals(corso) && i.getUtente().equals(utente) && i.isStato());
    }
}
