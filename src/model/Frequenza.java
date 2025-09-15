package model;

public enum Frequenza {
    Giornaliero("Una lezione ogni giorno"),
    OgniDueGiorni("una lezione ogni due giorni"),
    Settimanale("Una lezione a settimana"),
    Mensile("Una lezione al mese");

    private String descrizione;


    private Frequenza(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }
}
