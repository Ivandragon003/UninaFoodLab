package model;

public enum Frequenza {
    unica(0, "Sessione Unica"),
    giornaliero(1, "Giornaliero"),
    ogniDueGiorni(2, "Ogni Due Giorni"),
    settimanale(7, "Settimanale"),
    mensile(30, "Mensile");

    private final int intervallo;
    private final String descrizione;

    Frequenza(int intervallo, String descrizione) {
        this.intervallo = intervallo;
        this.descrizione = descrizione;
    }

    public int getIntervallo() { return intervallo; }
    public String getDescrizione() { return descrizione; }

    public int getDurataMinima() {
        return switch (this) {
            case unica -> 0;              
            case giornaliero -> 1;        
            case ogniDueGiorni -> 2;      
            case settimanale -> 7;        
            case mensile -> 30;           
        };
    }

    @Override
    public String toString() { return descrizione; }
}
