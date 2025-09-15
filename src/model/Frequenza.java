package model;

public enum Frequenza {
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
