package model;

public enum Frequenza {
    UNICA(0, "Sessione Unica"),
    GIORNALIERO(1, "Giornaliero"),
    OGNI_DUE_GIORNI(2, "Ogni Due Giorni"),
    SETTIMANALE(7, "Settimanale"),
    MENSILE(30, "Mensile");

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
            case UNICA -> 0;              
            case GIORNALIERO -> 1;        
            case OGNI_DUE_GIORNI -> 2;      
            case SETTIMANALE -> 7;        
            case MENSILE -> 30;           
        };
    }

    @Override
    public String toString() { return descrizione; }
}
