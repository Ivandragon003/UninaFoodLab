package model;

public enum Frequenza {
    Giornaliero("Una lezione ogni giorno"),
    OgniDueGiorni("una lezione ogni due giorni"),
    Settimanale("Una lezione a settimana"),
    Mensile("Una lezione al mese");
    ogniGiorno("Una lezione ogni giorno"),
    ogniDueGiorni("una lezione ogni due giorni"),
    settimanale("Una lezione a settimana"),
    mensile("Una lezione al mese");

    private String descrizione;


    private Frequenza(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }
}