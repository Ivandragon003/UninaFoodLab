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
    System.out.println("🔍 === DEBUG REPORT MENSILE ===");
    System.out.println("Periodo: " + inizio + " -> " + fine);
    System.out.println("Chef: " + chefLoggato.getUsername());
    
    int numeroCorsi = 0;
    int sessioniOnline = 0;
    int sessioniPratiche = 0;
    int totaleRicette = 0;
    int minRicette = Integer.MAX_VALUE;
    int maxRicette = 0;

    List<CorsoCucina> corsiDelChef = corsiService.getCorsiByChef(chefLoggato);
    if (corsiDelChef == null || corsiDelChef.isEmpty()) {
        System.out.println("❌ Nessun corso trovato per chef: " + chefLoggato.getUsername());
        return new DatiReportMensile(inizio, fine, 0, 0, 0, 0.0, 0, 0);
    }

    System.out.println("✅ Trovati " + corsiDelChef.size() + " corsi per chef: " + chefLoggato.getUsername());

    List<Integer> sessioniInPresenzaIds = new ArrayList<>();
    Set<Integer> corsiNelPeriodo = new HashSet<>();

    for (CorsoCucina corso : corsiDelChef) {
        if (corso == null) continue;

        List<Sessione> sessioniNelPeriodo = corsiService.getSessioniPerCorsoInPeriodo(
                corso.getIdCorso(), inizio, fine);
        if (sessioniNelPeriodo == null || sessioniNelPeriodo.isEmpty()) continue;

        System.out.println("\n📚 Corso: " + corso.getNomeCorso() + " (ID: " + corso.getIdCorso() + ")");
        System.out.println("   Sessioni trovate: " + sessioniNelPeriodo.size());

        corsiNelPeriodo.add(corso.getIdCorso());

        for (Sessione s : sessioniNelPeriodo) {
            if (s == null) continue;

            // === DEBUG DETTAGLIATO PER OGNI SESSIONE ===
            System.out.println("\n🔎 SESSIONE DEBUG:");
            System.out.println("   ID Sessione: " + s.getIdSessione());
            System.out.println("   Classe Java: " + s.getClass().getName());
            System.out.println("   Classe Semplice: " + s.getClass().getSimpleName());
            System.out.println("   instanceof Online: " + (s instanceof Online));
            System.out.println("   instanceof InPresenza: " + (s instanceof InPresenza));

            // TEST DIRETTO: Chiama il service per questa singola sessione
            try {
                List<Integer> singolaSessione = Arrays.asList(s.getIdSessione());
                Map<Integer, Integer> testRicette = sessioniService.getNumeroRicettePerSessioni(singolaSessione);
                int ricetteDalService = testRicette.getOrDefault(s.getIdSessione(), 0);
                System.out.println("   Ricette dal Service: " + ricetteDalService);
                
                // CONTROLLO CRITICO
                if (s instanceof Online && ricetteDalService > 0) {
                    System.err.println("\n🚨 ERRORE CRITICO TROVATO!");
                    System.err.println("   Sessione ONLINE con " + ricetteDalService + " ricette!");
                    System.err.println("   Questo è il BUG che causa il problema!");
                    System.err.println("   ID sessione problematica: " + s.getIdSessione());
                }
                
            } catch (Exception e) {
                System.err.println("   ❌ Errore chiamando service: " + e.getMessage());
                e.printStackTrace();
            }

            // Logica originale del controller
            if (s instanceof Online) {
                sessioniOnline++;
                System.out.println("   ➡️ Classificata come ONLINE");
            } else if (s instanceof InPresenza ip) {
                sessioniPratiche++;
                sessioniInPresenzaIds.add(ip.getIdSessione());
                System.out.println("   ➡️ Classificata come IN PRESENZA - ID aggiunto: " + ip.getIdSessione());
            } else {
                System.err.println("   ❌ SESSIONE NON CLASSIFICABILE!");
                System.err.println("   Classe: " + s.getClass().getName());
            }
        }
    }

    System.out.println("\n📊 RIEPILOGO CONTEGGI:");
    System.out.println("   Corsi nel periodo: " + corsiNelPeriodo.size());
    System.out.println("   Sessioni Online: " + sessioniOnline);
    System.out.println("   Sessioni In Presenza: " + sessioniPratiche);
    System.out.println("   IDs Sessioni InPresenza: " + sessioniInPresenzaIds);

    numeroCorsi = corsiNelPeriodo.size();

    // CALCOLO RICETTE SOLO PER SESSIONI IN PRESENZA
    if (!sessioniInPresenzaIds.isEmpty()) {
        System.out.println("\n🔄 Chiamando Service per " + sessioniInPresenzaIds.size() + " sessioni InPresenza");
        
        Map<Integer, Integer> ricettePerSessione = sessioniService.getNumeroRicettePerSessioni(sessioniInPresenzaIds);
        System.out.println("   Risultato Service per tutte le sessioni: " + ricettePerSessione);

        // VERIFICA CONSISTENZA
        for (Integer idSessione : ricettePerSessione.keySet()) {
            if (!sessioniInPresenzaIds.contains(idSessione)) {
                System.err.println("🚨 INCONSISTENZA: Service ha restituito ricette per sessione " + 
                                   idSessione + " che non è nella lista InPresenza!");
            }
        }

        for (Integer idSessione : sessioniInPresenzaIds) {
            int numRicette = ricettePerSessione.getOrDefault(idSessione, 0);
            totaleRicette += numRicette;
            minRicette = Math.min(minRicette, numRicette);
            maxRicette = Math.max(maxRicette, numRicette);
            
            System.out.println("   Sessione InPresenza " + idSessione + ": " + numRicette + " ricette");
        }

        if (minRicette == Integer.MAX_VALUE) minRicette = 0;
    } else {
        minRicette = 0;
        System.out.println("\n⚠️ Nessuna sessione InPresenza - nessuna ricetta da contare");
    }

    // Media ricette calcolata SOLO dalle sessioni in presenza
    double mediaRicette = (sessioniPratiche > 0) ? (double) totaleRicette / sessioniPratiche : 0.0;

    System.out.println("\n📈 RISULTATI FINALI:");
    System.out.println("   Totale ricette: " + totaleRicette);
    System.out.println("   Media ricette: " + String.format("%.2f", mediaRicette));
    System.out.println("   Min ricette: " + minRicette);
    System.out.println("   Max ricette: " + maxRicette);
    System.out.println("🔍 === FINE DEBUG ===\n");

    return new DatiReportMensile(inizio, fine, numeroCorsi, sessioniOnline, sessioniPratiche,
            mediaRicette, minRicette, maxRicette);
}


    public Map<LocalDate, Integer> getRicettePerGiorno(LocalDate inizio, LocalDate fine) throws DataAccessException {
        Map<LocalDate, Integer> result = new TreeMap<>();
        List<CorsoCucina> corsiDelChef = corsiService.getCorsiByChef(chefLoggato);

        if (corsiDelChef == null || corsiDelChef.isEmpty()) return result;

        List<Integer> sessioniIds = new ArrayList<>();
        Map<Integer, LocalDate> sessioneToDate = new HashMap<>();

        for (CorsoCucina corso : corsiDelChef) {
            if (corso == null) continue;

            List<Sessione> sessioniNelPeriodo = corsiService.getSessioniPerCorsoInPeriodo(
                    corso.getIdCorso(), inizio, fine);
            if (sessioniNelPeriodo == null || sessioniNelPeriodo.isEmpty()) continue;

            for (Sessione s : sessioniNelPeriodo) {
                if (s instanceof InPresenza ip) {
                    sessioniIds.add(ip.getIdSessione());
                    sessioneToDate.put(ip.getIdSessione(), ip.getDataInizioSessione().toLocalDate());
                }
            }
        }

        if (!sessioniIds.isEmpty()) {
            Map<Integer, Integer> ricettePerSessione = sessioniService.getNumeroRicettePerSessioni(sessioniIds);

            for (Integer idSessione : sessioniIds) {
                int numRicette = ricettePerSessione.getOrDefault(idSessione, 0);
                LocalDate giorno = sessioneToDate.get(idSessione);
                if (giorno != null) {
                    result.put(giorno, result.getOrDefault(giorno, 0) + numRicette);
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

        public LocalDate getInizio() {
            return inizio;
        }

        public LocalDate getFine() {
            return fine;
        }

        public int getNumeroCorsi() {
            return numeroCorsi;
        }

        public int getSessioniOnline() {
            return sessioniOnline;
        }

        public int getSessioniPratiche() {
            return sessioniPratiche;
        }

        public double getMediaRicette() {
            return mediaRicette;
        }

        public int getMinRicette() {
            return minRicette;
        }

        public int getMaxRicette() {
            return maxRicette;
        }
    }
}