package model;

import java.time.LocalDate;

public class DatiReportMensile {
    
    private final LocalDate inizio;
    private final LocalDate fine;
    private final int numeroCorsi;
    private final int sessioniOnline;
    private final int sessioniPratiche;
    private final double mediaRicette;
    private final int minRicette;
    private final int maxRicette;

    public DatiReportMensile(LocalDate inizio, LocalDate fine, 
                             int numeroCorsi, int sessioniOnline,
                             int sessioniPratiche, double mediaRicette, 
                             int minRicette, int maxRicette) {
        this.inizio = inizio;
        this.fine = fine;
        this.numeroCorsi = numeroCorsi;
        this.sessioniOnline = sessioniOnline;
        this.sessioniPratiche = sessioniPratiche;
        this.mediaRicette = mediaRicette;
        this.minRicette = minRicette;
        this.maxRicette = maxRicette;
    }

    public LocalDate getInizio() { return inizio; }
    public LocalDate getFine() { return fine; }
    public int getNumeroCorsi() { return numeroCorsi; }
    public int getSessioniOnline() { return sessioniOnline; }
    public int getSessioniPratiche() { return sessioniPratiche; }
    public double getMediaRicette() { return mediaRicette; }
    public int getMinRicette() { return minRicette; }
    public int getMaxRicette() { return maxRicette; }
}
