package controller;

import model.Ricetta;
import model.InPresenza;
import service.GestioneRicette;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class VisualizzaRicetteController {

    private final GestioneRicette gestioneRicette;
    private List<Ricetta> cachedRicette = null;

    public VisualizzaRicetteController(GestioneRicette gestioneRicetteService) {
        this.gestioneRicette = gestioneRicetteService;
    }

    public List<Ricetta> getTutteLeRicette() {
        if (cachedRicette == null) {
            try {
                cachedRicette = gestioneRicette.getAllRicette();
            } catch (SQLException e) {
                e.printStackTrace();
                cachedRicette = Collections.emptyList();
            }
        }
        return cachedRicette;
    }

    public void aggiungiRicettaASessione(InPresenza sessione, Ricetta r) {
        try {
            gestioneRicette.creaRicetta(r);
            if (!sessione.getRicette().contains(r)) {
                sessione.getRicette().add(r);
                r.getSessioni().add(sessione);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
