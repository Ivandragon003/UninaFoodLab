package model;

import java.time.LocalDateTime;
import java.util.*;

public class Adesione {

    private LocalDateTime dataAdesione;
    private boolean stato; // true = attiva, false = ritirata
    private Map<InPresenza, Map<Utente, Boolean>> adesioni;

    // Costruttore vuoto
    

    // Costruttore completo: registra subito l'adesione
    
    // Metodo per inviare un'adesione
    public boolean inviaAdesione(InPresenza sessione, Utente utente, LocalDateTime dataAdesione) {
        if (sessione == null || utente == null || dataAdesione == null) return false;

        adesioni.putIfAbsent(sessione, new HashMap<>());
        Map<Utente, Boolean> utentiAdesione = adesioni.get(sessione);

        // Controllo adesione già inviata
        if (utentiAdesione.containsKey(utente) && utentiAdesione.get(utente)) {
            System.out.println(utente.getNome() + " ha già inviato adesione per questa sessione.");
            return false;
        }

        // Controllo capienza
        long adesioniAttive = utentiAdesione.values().stream().filter(Boolean::booleanValue).count();
        if (adesioniAttive >= sessione.getNumeroPosti()) {
            System.out.println("La sessione è già piena!");
            return false;
        }

        // Registra adesione
        utentiAdesione.put(utente, true);
        this.dataAdesione = dataAdesione;
        this.stato = true;

        // Aggiorna liste bidirezionali
        utente.aggiungiAdesione(this);
        sessione.aggiungiAdesione(this);

        System.out.println(utente.getNome() + " ha inviato adesione con successo.");
        return true;
    }

    // Ritira adesione
    public boolean ritiraAdesione(InPresenza sessione, Utente utente) {
        if (sessione == null || utente == null) return false;

        Map<Utente, Boolean> utentiAdesione = adesioni.get(sessione);
        if (utentiAdesione != null && utentiAdesione.getOrDefault(utente, false)) {
            utentiAdesione.put(utente, false);
            this.stato = false;
            System.out.println(utente.getNome() + " ha ritirato l'adesione.");
            return true;
        }
        return false;
    }

    // Getter e setter
    public LocalDateTime getDataAdesione() { return dataAdesione; }
    public boolean isStato() { return stato; }
    public void setStato(boolean stato) { this.stato = stato; }

    public Set<Utente> getUtentiAdesione(InPresenza sessione) {
        Map<Utente, Boolean> utentiAdesione = adesioni.get(sessione);
        if (utentiAdesione == null) return Collections.emptySet();
        Set<Utente> attivi = new HashSet<>();
        utentiAdesione.forEach((u, s) -> { if (s) attivi.add(u); });
        return attivi;
    }

    public boolean getStatoAdesione(InPresenza sessione, Utente utente) {
        Map<Utente, Boolean> utentiAdesione = adesioni.get(sessione);
        return utentiAdesione != null && utentiAdesione.getOrDefault(utente, false);
    }

    @Override
    public String toString() {
        return "Adesione{" +
                "dataAdesione=" + dataAdesione +
                ", stato=" + stato +
                '}';
    }
}
