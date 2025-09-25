package controller;

import model.Ricetta;
import service.GestioneRicette;

import java.sql.SQLException;
import java.util.List;

public class VisualizzaRicetteController {

    private final GestioneRicette gestioneRicetteService;

    public VisualizzaRicetteController(GestioneRicette gestioneRicetteService) {
        this.gestioneRicetteService = gestioneRicetteService;
    }

    /** --- METODI PER RICERCA E FILTRO --- */

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

    /** --- NUOVO METODO PER GESTIONE SELEZIONE RICETTE --- */

    /**
     * Questo metodo viene chiamato quando l'utente seleziona le ricette nella GUI
     * e clicca "Seleziona e Associa".
     *
     * Viene sovrascritto da GestioneSessioniController al momento dell'apertura
     * della GUI per associare le ricette alla sessione.
     *
     * @param ricetteSelezionate lista di ricette selezionate
     */
    public void aggiungiRicetteSelezionate(List<Ricetta> ricetteSelezionate) {
        // Di default non fa nulla; viene sovrascritto dalla GUI di gestione sessioni
    }
}