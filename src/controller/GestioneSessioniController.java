package controller;

import model.*;
import service.GestioneSessioni;
import service.GestioneCucina;
import service.GestioneRicette;

import java.sql.SQLException;
import java.util.List;
public class GestioneSessioniController {

    private final CorsoCucina corso;
    private final GestioneSessioni gestioneSessioniService;
    private final GestioneCucina gestioneCucinaService;
    private final GestioneRicette gestioneRicetteService;

    public GestioneSessioniController(CorsoCucina corso,
                                      GestioneSessioni gestioneSessioniService,
                                      GestioneCucina gestioneCucinaService,
                                      GestioneRicette gestioneRicetteService) {
        this.corso = corso;
        this.gestioneSessioniService = gestioneSessioniService;
        this.gestioneCucinaService = gestioneCucinaService;
        this.gestioneRicetteService = gestioneRicetteService;
    }

    // --- Sessioni ---
    public void aggiungiSessione(Sessione s) throws SQLException {
        aggiungiSessione(s, null);
    }

    public void aggiungiSessione(Sessione s, List<Ricetta> ricette) throws SQLException {
        if (s == null) throw new IllegalArgumentException("Sessione nulla");
        corso.getSessioni().add(s);
        gestioneSessioniService.creaSessione(s);

        if (s instanceof InPresenza ip && ricette != null) {
            for (Ricetta r : ricette) {
                if (!ip.getRicette().contains(r)) {
                    ip.getRicette().add(r);
                    r.getSessioni().add(ip);
                    gestioneCucinaService.aggiungiSessioneARicetta(r, ip);
                }
            }
        }
    }

    public void aggiornaSessione(Sessione oldS, Sessione newS) throws SQLException {
        int idx = corso.getSessioni().indexOf(oldS);
        if (idx >= 0) {
            corso.getSessioni().set(idx, newS);
            gestioneSessioniService.rimuoviSessione(oldS);
            gestioneSessioniService.creaSessione(newS);
        }
    }

    public void eliminaSessione(Sessione s) throws SQLException {
        corso.getSessioni().remove(s);
        gestioneSessioniService.rimuoviSessione(s);
    }

    // --- Ricette ---
    public void aggiungiRicettaAInPresenza(InPresenza ip, Ricetta r) throws SQLException {
        gestioneRicetteService.creaRicetta(r);
        if (!ip.getRicette().contains(r)) {
            ip.getRicette().add(r);
            r.getSessioni().add(ip);
            gestioneCucinaService.aggiungiSessioneARicetta(r, ip);
        }
    }

    public void rimuoviRicettaDaSessione(InPresenza ip, Ricetta r) throws SQLException {
        if (ip.getRicette().remove(r)) {
            r.getSessioni().remove(ip);
            gestioneCucinaService.rimuoviSessioneDaRicetta(r, ip);
        }
    }

    // --- GUI Ricette ---
    public void apriSelezionaRicettaGUI(InPresenza ip) {
        if (ip == null) return;
        try {
            // logica per aprire GUI di selezione/creazione ricetta
        } catch (Exception e) {
            System.err.println("Errore apertura GUI ricette: " + e.getMessage());
        }
    }
}
