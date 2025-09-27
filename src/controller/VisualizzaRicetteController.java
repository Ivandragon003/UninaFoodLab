package controller;

import model.Ricetta;
import model.InPresenza;
import service.GestioneRicette;
import service.GestioneCucina;

import java.sql.SQLException;
import java.util.List;

public class VisualizzaRicetteController {

    private final GestioneRicette gestioneRicetteService;
    private final InPresenza sessione; // null -> caso 1, non null -> caso 2
    private final GestioneCucina gestioneCucinaService;

    // Costruttore per caso 1 (nessuna sessione)
    public VisualizzaRicetteController(GestioneRicette gestioneRicetteService) {
        this(gestioneRicetteService, null);
    }

    // Costruttore generale (caso 1 o caso 2)
    public VisualizzaRicetteController(GestioneRicette gestioneRicetteService, InPresenza sessione) {
        this.gestioneRicetteService = gestioneRicetteService;
        this.sessione = sessione;
        this.gestioneCucinaService = (sessione != null) ? new GestioneCucina(null) : null;
    }

    public List<Ricetta> getAllRicette() throws SQLException {
        return gestioneRicetteService.getAllRicette();
    }

    public List<Ricetta> cercaPerNome(String nome) throws SQLException {
        return getAllRicette().stream()
                .filter(r -> r.getNome().toLowerCase().contains(nome.toLowerCase()))
                .toList();
    }

    public List<Ricetta> filtraPerTempo(int maxTempo) throws SQLException {
        return getAllRicette().stream()
                .filter(r -> r.getTempoPreparazione() <= maxTempo)
                .toList();
    }

    // Caso 1: crea ricetta senza sessione
    public void mostraFormCreazioneRicetta() {
        new Gui.CreaRicettaGUI(gestioneRicetteService, null).start(new javafx.stage.Stage());
    }

    // Caso 2: crea ricetta legata a sessione
    public void mostraFormCreazioneRicetta(InPresenza sessione) {
        new Gui.CreaRicettaGUI(gestioneRicetteService, sessione).start(new javafx.stage.Stage());
    }

    public void mostraDettagliRicetta(Ricetta ricetta) {
        new Gui.DettagliRicettaGUI(gestioneRicetteService, ricetta).start(new javafx.stage.Stage());
    }

    public void aggiungiRicetteSelezionate(List<Ricetta> ricetteSelezionate) {
        if (sessione != null && gestioneCucinaService != null) {
            for (Ricetta r : ricetteSelezionate) {
                try {
                    gestioneCucinaService.aggiungiSessioneARicetta(r, sessione);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
