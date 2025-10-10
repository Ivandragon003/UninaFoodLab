package controller;

import exceptions.DataAccessException;
import model.CorsoCucina;
import model.Sessione;
import model.InPresenza;
import model.Online;
import model.Chef;
import service.GestioneCorsiCucina;
import service.GestioneSessioni;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * Controller ottimizzato / robusto per la generazione del report mensile.
 * Ora tratta in modo tollerante gli errori nel conteggio delle ricette.
 */
public class ReportMensileController {

    private final GestioneCorsiCucina corsiService;
    private final GestioneSessioni sessioniService;
    private final Chef chefLoggato;

    public ReportMensileController(GestioneCorsiCucina corsiService,
                                   GestioneSessioni sessioniService,
                                   Chef chefLoggato) {
        this.corsiService = Objects.requireNonNull(corsiService);
        this.sessioniService = Objects.requireNonNull(sessioniService);
        this.chefLoggato = Objects.requireNonNull(chefLoggato);
    }

    public DatiReportMensile generaReport(int mese, int anno) throws DataAccessException {
        YearMonth periodo = YearMonth.of(anno, mese);
        LocalDate inizio = periodo.atDay(1);
        LocalDate fine = periodo.atEndOfMonth();

        int numeroCorsi = 0;
        int sessioniOnline = 0;
        int sessioniPratiche = 0;
        int totaleRicette = 0;
        int minRicette = Integer.MAX_VALUE;
        int maxRicette = 0;
        int conteggioSessioniConRicette = 0;

        List<CorsoCucina> corsiDelChef = corsiService.getCorsiByChef(chefLoggato);
        if (corsiDelChef == null || corsiDelChef.isEmpty()) {
            return new DatiReportMensile(mese, anno, 0, 0, 0, 0.0, 0, 0);
        }

        for (CorsoCucina corso : corsiDelChef) {
            if (corso == null) continue;
            int idCorso = corso.getIdCorso();

            List<Sessione> sessioniNelPeriodo = corsiService.getSessioniPerCorsoInPeriodo(idCorso, inizio, fine);
            if (sessioniNelPeriodo == null || sessioniNelPeriodo.isEmpty()) continue;

            numeroCorsi++;

            for (Sessione s : sessioniNelPeriodo) {
                if (s == null) continue;
                if (s instanceof Online) {
                    sessioniOnline++;
                } else if (s instanceof InPresenza) {
                    sessioniPratiche++;
                    InPresenza ip = (InPresenza) s;

                    int numRicette = 0;

                    // Prima prova il conteggio tramite service/DAO (economico)
                    try {
                        numRicette = sessioniService.getNumeroRicettePerSessione(ip.getIdSessione());
                    } catch (Exception ex) {
                        // LOG: mostra messaggio e stacktrace sulla console per debug
                        System.err.println("[ReportMensileController] Errore recupero numero ricette per sessione id="
                                + ip.getIdSessione() + " : " + ex.getMessage());
                        ex.printStackTrace();

                        // fallback: se l'oggetto InPresenza ha già la lista di ricette, usala
                        try {
                            if (ip.getRicette() != null) {
                                numRicette = ip.getRicette().size();
                            } else {
                                // default 0 (non interrompiamo il calcolo globale)
                                numRicette = 0;
                            }
                        } catch (Exception nested) {
                            // se anche il fallback fallisce, teniamo numRicette=0
                            System.err.println("[ReportMensileController] Fallback conteggio ricette fallito: " + nested.getMessage());
                        }
                    }

                    if (numRicette > 0) {
                        totaleRicette += numRicette;
                        conteggioSessioniConRicette++;
                        minRicette = Math.min(minRicette, numRicette);
                        maxRicette = Math.max(maxRicette, numRicette);
                    }
                }
            }
        }

        double mediaRicette = conteggioSessioniConRicette > 0 ? (double) totaleRicette / conteggioSessioniConRicette : 0.0;
        if (conteggioSessioniConRicette == 0) minRicette = 0;

        return new DatiReportMensile(mese, anno, numeroCorsi, sessioniOnline, sessioniPratiche,
                mediaRicette, minRicette, maxRicette);
    }

    public Map<Integer, Integer> getRicettePerGiorno(int mese, int anno) throws DataAccessException {
        YearMonth periodo = YearMonth.of(anno, mese);
        LocalDate inizio = periodo.atDay(1);
        LocalDate fine = periodo.atEndOfMonth();

        Map<Integer, Integer> result = new TreeMap<>();
        List<CorsoCucina> corsiDelChef = corsiService.getCorsiByChef(chefLoggato);
        if (corsiDelChef == null || corsiDelChef.isEmpty()) return result;

        for (CorsoCucina corso : corsiDelChef) {
            if (corso == null) continue;
            int idCorso = corso.getIdCorso();

            List<Sessione> sessioniNelPeriodo = corsiService.getSessioniPerCorsoInPeriodo(idCorso, inizio, fine);
            if (sessioniNelPeriodo == null || sessioniNelPeriodo.isEmpty()) continue;

            for (Sessione s : sessioniNelPeriodo) {
                if (!(s instanceof InPresenza)) continue;
                InPresenza ip = (InPresenza) s;
                int idSessione = ip.getIdSessione();
                int numRicette = 0;
                try {
                    numRicette = sessioniService.getNumeroRicettePerSessione(idSessione);
                } catch (Exception ex) {
                    System.err.println("[ReportMensileController] Errore recupero numero ricette (giorno) id=" + idSessione + " : " + ex.getMessage());
                    ex.printStackTrace();
                    if (ip.getRicette() != null) numRicette = ip.getRicette().size();
                    else numRicette = 0;
                }

                if (numRicette <= 0) continue;
                int giorno = ip.getDataInizioSessione().toLocalDate().getDayOfMonth();
                result.put(giorno, result.getOrDefault(giorno, 0) + numRicette);
            }
        }
        return result;
    }

    public Chef getChefLoggato() {
        return chefLoggato;
    }

    public static class DatiReportMensile {
        private final int mese;
        private final int anno;
        private final int numeroCorsi;
        private final int sessioniOnline;
        private final int sessioniPratiche;
        private final double mediaRicette;
        private final int minRicette;
        private final int maxRicette;

        public DatiReportMensile(int mese, int anno, int numeroCorsi,
                                 int sessioniOnline, int sessioniPratiche,
                                 double mediaRicette, int minRicette, int maxRicette) {
            this.mese = mese; this.anno = anno; this.numeroCorsi = numeroCorsi;
            this.sessioniOnline = sessioniOnline; this.sessioniPratiche = sessioniPratiche;
            this.mediaRicette = mediaRicette; this.minRicette = minRicette; this.maxRicette = maxRicette;
        }

        public int getMese() { return mese; }
        public int getAnno() { return anno; }
        public int getNumeroCorsi() { return numeroCorsi; }
        public int getSessioniOnline() { return sessioniOnline; }
        public int getSessioniPratiche() { return sessioniPratiche; }
        public double getMediaRicette() { return mediaRicette; }
        public int getMinRicette() { return minRicette; }
        public int getMaxRicette() { return maxRicette; }

        public String getNomeMese() {
            String[] nomi = {"Gennaio","Febbraio","Marzo","Aprile","Maggio","Giugno",
                             "Luglio","Agosto","Settembre","Ottobre","Novembre","Dicembre"};
            if (mese>=1 && mese<=12) return nomi[mese-1];
            return String.valueOf(mese);
        }
    }
}
