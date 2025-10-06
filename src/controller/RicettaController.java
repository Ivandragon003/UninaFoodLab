package controller;

import model.Ricetta;
import model.Ingrediente;
import model.InPresenza;
import service.GestioneRicette;
import exceptions.ValidationException;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class RicettaController {

    private final GestioneRicette gestioneRicette;
    private List<Ricetta> cachedRicette = null;

    public RicettaController(GestioneRicette gestioneRicette) {
        this.gestioneRicette = gestioneRicette;
    }

    // ==================== CRUD ====================

    public Ricetta creaRicetta(String nome, int tempoPreparazione, Map<Ingrediente, Double> ingredienti) 
            throws SQLException, ValidationException {
        Ricetta ricetta = new Ricetta(nome.trim(), tempoPreparazione);
        ricetta.setIngredienti(ingredienti);
        gestioneRicette.creaRicetta(ricetta);
        invalidaCache();
        return ricetta;
    }

    public void aggiornaRicetta(int idRicetta, String nuovoNome, int nuovoTempo, 
            Map<Ingrediente, Double> nuoviIngredienti) throws SQLException, ValidationException {
        Ricetta ricetta = new Ricetta(nuovoNome.trim(), nuovoTempo);
        ricetta.setIngredienti(nuoviIngredienti);
        gestioneRicette.aggiornaRicetta(idRicetta, ricetta);
        invalidaCache();
    }

    public void eliminaRicetta(int idRicetta) throws SQLException, ValidationException {
        gestioneRicette.cancellaRicetta(idRicetta);
        invalidaCache();
    }

    // ==================== QUERY ====================

    public List<Ricetta> getAllRicette() throws SQLException {
        if (cachedRicette == null) {
            cachedRicette = gestioneRicette.getAllRicette();
        }
        return List.copyOf(cachedRicette);
    }

    public Ricetta getRicettaPerId(int idRicetta) throws SQLException {
        return getAllRicette().stream()
                .filter(r -> r.getIdRicetta() == idRicetta)
                .findFirst()
                .orElse(null);
    }

    public List<Ricetta> cercaPerNome(String nome) throws SQLException, ValidationException {
        return gestioneRicette.cercaPerNome(nome, getAllRicette());
    }

    public List<Ricetta> filtraCombinato(String nome, Integer tempoMin, Integer tempoMax,
            Integer ingredientiMin, Integer ingredientiMax) throws SQLException, ValidationException {
        return gestioneRicette.filtraCombinato(nome, tempoMin, tempoMax, 
                ingredientiMin, ingredientiMax, getAllRicette());
    }

    // ==================== INGREDIENTI ====================

    public void aggiungiIngrediente(Ricetta ricetta, Ingrediente ingrediente, double quantita) 
            throws SQLException, ValidationException {
        gestioneRicette.aggiungiIngrediente(ricetta, ingrediente, quantita);
        invalidaCache();
    }

    public void aggiornaQuantitaIngrediente(Ricetta ricetta, Ingrediente ingrediente, double nuovaQuantita) 
            throws SQLException, ValidationException {
        gestioneRicette.aggiornaQuantitaIngrediente(ricetta, ingrediente, nuovaQuantita);
        invalidaCache();
    }

    public void rimuoviIngrediente(Ricetta ricetta, Ingrediente ingrediente) 
            throws SQLException, ValidationException {
        gestioneRicette.rimuoviIngrediente(ricetta, ingrediente);
        invalidaCache();
    }

    // ==================== SESSIONI ====================

    public void associaRicettaASessione(Ricetta ricetta, InPresenza sessione) throws ValidationException {
        gestioneRicette.associaRicettaASessione(ricetta, sessione);
    }

    public void disassociaRicettaDaSessione(Ricetta ricetta, InPresenza sessione) {
        gestioneRicette.disassociaRicettaDaSessione(ricetta, sessione);
    }

    public List<Ricetta> getRicetteNonAssociate(InPresenza sessione) throws SQLException {
        return gestioneRicette.getRicetteNonAssociate(sessione, getAllRicette());
    }

    // ==================== CACHE ====================

    public void invalidaCache() {
        cachedRicette = null;
    }

    public void ricaricaCache() throws SQLException {
        invalidaCache();
        getAllRicette();
    }

    public GestioneRicette getGestioneRicette() {
        return gestioneRicette;
    }
}
