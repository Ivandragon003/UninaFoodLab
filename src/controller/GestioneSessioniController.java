package controller;

import model.*;
import service.GestioneSessioni;
import service.GestioneCucina;
import service.GestioneRicette;
import Gui.VisualizzaRicetteGUI;
import javafx.stage.Stage;

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

    public List<Sessione> getSessioni() {
        return corso.getSessioni();
    }

    public void aggiungiSessione(Sessione sessione, List<Ricetta> ricetteDaAssociare) throws SQLException {
        corso.getSessioni().add(sessione);
        corso.setNumeroSessioni(corso.getSessioni().size());

        gestioneSessioniService.creaSessione(sessione);

        if (sessione instanceof InPresenza inPresenza && ricetteDaAssociare != null) {
            for (Ricetta r : ricetteDaAssociare) {
                if (r.getIdRicetta() == 0) {
                    gestioneRicetteService.creaRicetta(r);
                }
                if (!inPresenza.getRicette().contains(r)) {
                    inPresenza.getRicette().add(r);
                    r.getSessioni().add(inPresenza);
                    gestioneCucinaService.aggiungiSessioneARicetta(r, inPresenza);
                }
            }
        }
    }
    
    public void salvaNuovaSessione(Sessione sessione, CorsoCucina corso) {
        try {
            // Aggiunge la sessione senza ricette inizialmente
            aggiungiSessione(sessione, null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void eliminaSessione(Sessione sessione) throws SQLException {
        corso.getSessioni().remove(sessione);
        corso.setNumeroSessioni(corso.getSessioni().size());
        gestioneSessioniService.rimuoviSessione(sessione);
    }

    public void aggiornaSessione(Sessione oldS, Sessione newS) throws SQLException {
        int idx = corso.getSessioni().indexOf(oldS);
        if (idx >= 0) {
            corso.getSessioni().set(idx, newS);
            gestioneSessioniService.rimuoviSessione(oldS);
            gestioneSessioniService.creaSessione(newS);
        }
    }

    public boolean verificaNumeroSessioni() {
        return corso.getSessioni().size() == corso.getNumeroSessioni();
    }

    public void rimuoviRicettaDaSessione(InPresenza sessione, Ricetta ricetta) throws SQLException {
        if (sessione.getRicette().remove(ricetta)) {
            ricetta.getSessioni().remove(sessione);
            gestioneCucinaService.rimuoviSessioneDaRicetta(ricetta, sessione);
        }
    }

    public void apriSelezionaRicettaGUI(InPresenza sessione) {
        VisualizzaRicetteGUI gui = new VisualizzaRicetteGUI();

        VisualizzaRicetteController vController = new VisualizzaRicetteController(gestioneRicetteService) {
            @Override
            public void aggiungiRicetteSelezionate(List<Ricetta> ricetteSelezionate) {
                try {
                    for (Ricetta r : ricetteSelezionate) {
                        if (r.getIdRicetta() == 0) {
                            gestioneRicetteService.creaRicetta(r);
                        }
                        if (!sessione.getRicette().contains(r)) {
                            sessione.getRicette().add(r);
                            r.getSessioni().add(sessione);
                            gestioneCucinaService.aggiungiSessioneARicetta(r, sessione);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };

        gui.setController(vController);
        Stage stage = new Stage();
        gui.show(stage);
    }
}
