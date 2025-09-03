package model;

public enum Frequenza {
    GIORNALIERO("Una lezione ogni giorno"),
    SETTIMANALE("Una lezione a settimana"),
    MENSILE("Una lezione al mese");

    private String descrizione;

    // COSTRUTTORE ENUM
    private Frequenza(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }
}
