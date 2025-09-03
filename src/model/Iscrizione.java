package model;

import java.time.LocalDate;
import java.util.Objects;

public class Iscrizione {

    private Utente utente;
    private CorsoCucina corso;
    private LocalDate dataIscrizione;
    private boolean stato; // true = attiva, false = ritirata

    public Iscrizione(Utente utente, CorsoCucina corso, LocalDate dataIscrizione) {
        if (utente == null || corso == null || dataIscrizione == null)
            throw new IllegalArgumentException("Utente, corso e data non possono essere null");
        this.utente = utente;
        this.corso = corso;
        this.dataIscrizione = dataIscrizione;
        this.stato = true; 
    }

    // Getter e Setter
    public Utente getUtente() { return utente; }
    public CorsoCucina getCorso() { return corso; }
    public LocalDate getDataIscrizione() { return dataIscrizione; }
    public boolean isStato() { return stato; }
    public void setStato(boolean stato) { this.stato = stato; }

    public String toStringUtente() { return "Utente: " + utente.getNome() + " " + utente.getCognome(); }
    public String toStringCorso() { return "Corso: " + corso.getNomeCorso(); }
    public String toStringDataIscrizione() { return "Data Iscrizione: " + dataIscrizione; }
    public String toStringStato() { return "Stato: " + (stato ? "Attiva" : "Ritirata"); }

    // equals e hashCode basati su utente + corso (chiave univoca)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Iscrizione)) return false;
        Iscrizione that = (Iscrizione) o;
        return Objects.equals(utente, that.utente) &&
               Objects.equals(corso, that.corso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(utente, corso);
    }
}
