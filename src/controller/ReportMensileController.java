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
import java.util.*;
import java.util.stream.Collectors;

public class ReportMensileController {

    private final GestioneCorsiCucina corsiService;
    private final GestioneSessioni sessioniService;
    private final Chef chefLoggato;

    public ReportMensileController(GestioneCorsiCucina corsiService, GestioneSessioni sessioniService,
                                   Chef chefLoggato) {
        this.corsiService = Objects.requireNonNull(corsiService);
        this.sessioniService = Objects.requireNonNull(sessioniService);
        this.chefLoggato = Objects.requireNonNull(chefLoggato);
    }

    public DatiReportMensile generaReport(LocalDate inizio, LocalDate fine) throws DataAccessException {
        List<CorsoCucina> corsiDelChef = corsiService.getCorsiByChef(chefLoggato);
        if (corsiDelChef == null || corsiDelChef.isEmpty()) {
            return new DatiReportMensile(inizio, fine, 0, 0, 0, 0.0, 0, 0);
        }

        // Raccogli tutti gli ID dei corsi in una sola operazione
        List<Integer> idsCorsi = corsiDelChef.stream()
                .filter(Objects::nonNull)
                .map(CorsoCucina::getIdCorso)
                .collect(Collectors.toList());

        if (idsCorsi.isEmpty()) {
            return new DatiReportMensile(inizio, fine, 0, 0, 0, 0.0, 0, 0);
        }

        // Una sola query per tutte le sessioni (se disponibile nel service)
        // Altrimenti cache i risultati
        Map<Integer, List<Sessione>> sessioniPerCorso = new HashMap<>();
        Set<Integer> corsiNelPeriodo = new HashSet<>();
        List<Integer> sessioniInPresenzaIds = new ArrayList<>();
        int sessioniOnline = 0;

        for (Integer idCorso : idsCorsi) {
            List<Sessione> sessioni = corsiService.getSessioniPerCorsoInPeriodo(idCorso, inizio, fine);
            if (sessioni != null && !sessioni.isEmpty()) {
                sessioniPerCorso.put(idCorso, sessioni);
                corsiNelPeriodo.add(idCorso);
                
                for (Sessione s : sessioni) {
                    if (s instanceof Online) {
                        sessioniOnline++;
                    } else if (s instanceof InPresenza ip) {
                        sessioniInPresenzaIds.add(ip.getIdSessione());
                    }
                }
            }
        }

        int numeroCorsi = corsiNelPeriodo.size();
        int sessioniPratiche = 0;
        int totaleRicette = 0;
        int minRicette = 0;
        int maxRicette = 0;
        double mediaRicette = 0.0;

        // Una sola query per tutte le ricette invece di query multiple
        if (!sessioniInPresenzaIds.isEmpty()) {
            Map<Integer, Integer> ricettePerSessione = sessioniService.getNumeroRicettePerSessioni(sessioniInPresenzaIds);
            
            List<Integer> ricetteValide = new ArrayList<>();
            
            for (Integer idSessione : sessioniInPresenzaIds) {
                int numRicette = ricettePerSessione.getOrDefault(idSessione, 0);
                if (numRicette > 0) {
                    ricetteValide.add(numRicette);
                    totaleRicette += numRicette;
                }
            }
            
            sessioniPratiche = ricetteValide.size();
            
            if (!ricetteValide.isEmpty()) {
                minRicette = ricetteValide.stream().min(Integer::compareTo).orElse(0);
                maxRicette = ricetteValide.stream().max(Integer::compareTo).orElse(0);
                mediaRicette = (double) totaleRicette / sessioniPratiche;
            }
        }

        return new DatiReportMensile(inizio, fine, numeroCorsi, sessioniOnline, sessioniPratiche,
                mediaRicette, minRicette, maxRicette);
    }

    public Map<LocalDate, Integer> getRicettePerGiorno(LocalDate inizio, LocalDate fine) throws DataAccessException {
        Map<LocalDate, Integer> result = new TreeMap<>();
        List<CorsoCucina> corsiDelChef = corsiService.getCorsiByChef(chefLoggato);

        if (corsiDelChef == null || corsiDelChef.isEmpty()) return result;

        List<Integer> sessioniIds = new ArrayList<>();
        Map<Integer, LocalDate> sessioneToDate = new HashMap<>();

        // Raccogli tutte le sessioni in presenza
        for (CorsoCucina corso : corsiDelChef) {
            if (corso == null) continue;

            List<Sessione> sessioni = corsiService.getSessioniPerCorsoInPeriodo(
                    corso.getIdCorso(), inizio, fine);
            
            if (sessioni != null) {
                for (Sessione s : sessioni) {
                    if (s instanceof InPresenza ip) {
                        sessioniIds.add(ip.getIdSessione());
                        sessioneToDate.put(ip.getIdSessione(), ip.getDataInizioSessione().toLocalDate());
                    }
                }
            }
        }

        // Una sola query per tutte le ricette
        if (!sessioniIds.isEmpty()) {
            Map<Integer, Integer> ricettePerSessione = sessioniService.getNumeroRicettePerSessioni(sessioniIds);

            for (Map.Entry<Integer, Integer> entry : ricettePerSessione.entrySet()) {
                LocalDate giorno = sessioneToDate.get(entry.getKey());
                if (giorno != null && entry.getValue() > 0) {
                    result.merge(giorno, entry.getValue(), Integer::sum);
                }
            }
        }

        return result;
    }

    public Chef getChefLoggato() {
        return chefLoggato;
    }

    public static class DatiReportMensile {
        private final LocalDate inizio;
        private final LocalDate fine;
        private final int numeroCorsi;
        private final int sessioniOnline;
        private final int sessioniPratiche;
        private final double mediaRicette;
        private final int minRicette;
        private final int maxRicette;

        public DatiReportMensile(LocalDate inizio, LocalDate fine, int numeroCorsi, int sessioniOnline,
                                 int sessioniPratiche, double mediaRicette, int minRicette, int maxRicette) {
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
}