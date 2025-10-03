package model;

public enum Frequenza {
    ogniGiorno("Giornaliero"),
    ogniDueGiorni("Ogni due giorni"),
    settimanale("settimanale"),
    mensile("mensile");

    private String descrizione;


    private Frequenza(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }
}
