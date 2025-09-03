package model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Adesione {

    private LocalDateTime dataAdesione;
    private boolean stato;
    private Utente utente;
    private InPresenza sessione;

    public Adesione(Utente utente, InPresenza sessione, LocalDateTime dataAdesione) {
        this.utente = utente;
        this.sessione = sessione;
        this.dataAdesione = dataAdesione;
        this.stato = true;
    }

    public LocalDateTime getDataAdesione() { return dataAdesione; }
    public boolean isStato() { return stato; }
    public void setStato(boolean stato) { this.stato = stato; }
    public Utente getUtente() { return utente; }
    public InPresenza getSessione() { return sessione; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Adesione)) return false;
        Adesione adesione = (Adesione) o;
        return Objects.equals(utente, adesione.utente) &&
               Objects.equals(sessione, adesione.sessione);
    }

    @Override
    public int hashCode() {
        return Objects.hash(utente, sessione);
    }

    @Override
    public String toString() {
        return "Adesione{" +
                "utente=" + utente.getNome() +
                ", sessione=" + sessione.toString() +
                ", dataAdesione=" + dataAdesione +
                ", stato=" + stato +
                '}';
    }
}
