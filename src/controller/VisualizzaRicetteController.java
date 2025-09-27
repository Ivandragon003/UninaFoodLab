package controller;

import model.Ricetta;
import service.GestioneRicette;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class VisualizzaRicetteController {

    private final GestioneRicette gestioneRicetteService;

    // Cache per ricette già caricate (per filtri pesanti)
    private List<Ricetta> cachedRicette = null;

    public VisualizzaRicetteController(GestioneRicette gestioneRicetteService) {
        this.gestioneRicetteService = gestioneRicetteService;
    }

    public List<Ricetta> getTutteLeRicette() throws SQLException {
        if (cachedRicette == null) {
            cachedRicette = gestioneRicetteService.getAllRicette();
        }
        return cachedRicette;
    }

    public List<Ricetta> cercaPerNome(String filtro) throws SQLException {
        String f = filtro.toLowerCase().trim();
        return getTutteLeRicette().stream()
                .filter(r -> r.getNome().toLowerCase().contains(f))
                .collect(Collectors.toList());
    }

    public List<Ricetta> filtraPerTempo(int maxTempo) throws SQLException {
        return getTutteLeRicette().stream()
                .filter(r -> r.getTempoPreparazione() <= maxTempo)
                .collect(Collectors.toList());
    }

    public void mostraDettagliRicetta(Ricetta r) {
        new Gui.DettagliRicettaGUI(gestioneRicetteService, r).start(new javafx.stage.Stage());
    }

    public GestioneRicette getGestioneRicette() {
        return gestioneRicetteService;
    }

    public void aggiungiRicetta(Ricetta r) throws SQLException {
        gestioneRicetteService.creaRicetta(r);
        cachedRicette = null; 
    }
}
