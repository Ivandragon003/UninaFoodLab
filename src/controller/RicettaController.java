package controller;

import model.Ricetta;
import model.Ingrediente;
import model.InPresenza;
import service.GestioneRicette;
import exceptions.ValidationException;
import exceptions.ValidationUtils;
import exceptions.ErrorMessages;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RicettaController {

    private final GestioneRicette gestioneRicette;
    private List<Ricetta> cachedRicette = null;

    public RicettaController(GestioneRicette gestioneRicette) {
        this.gestioneRicette = gestioneRicette;
    }

    // ==================== CRUD ====================

    public Ricetta creaRicetta(String nome, int tempoPreparazione, Map<Ingrediente, Double> ingredienti) 
            throws SQLException, ValidationException {
        
        // Validazioni delegate a ValidationUtils
        ValidationUtils.validateNomeRicetta(nome);
        ValidationUtils.validateTempoPreparazione(tempoPreparazione);
        ValidationUtils.validateIngredienti(ingredienti);
        
        Ricetta ricetta = new Ricetta(nome.trim(), tempoPreparazione);
        ricetta.setIngredienti(ingredienti);
        
        gestioneRicette.creaRicetta(ricetta);
        invalidaCache();
        
        return ricetta;
    }

    public void aggiornaRicetta(int idRicetta, String nuovoNome, int nuovoTempo, 
            Map<Ingrediente, Double> nuoviIngredienti) 
            throws SQLException, ValidationException {
        
        ValidationUtils.validateNomeRicetta(nuovoNome);
        ValidationUtils.validateTempoPreparazione(nuovoTempo);
        ValidationUtils.validateIngredienti(nuoviIngredienti);
        
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

    public List<Ricetta> cercaPerNome(String nome) throws SQLException {
        if (nome == null || nome.trim().isEmpty()) {
            return getAllRicette();
        }
        
        String nomeLC = nome.toLowerCase().trim();
        return getAllRicette().stream()
                .filter(r -> r.getNome().toLowerCase().contains(nomeLC))
                .collect(Collectors.toList());
    }

    // ==================== FILTRI ====================

    public List<Ricetta> filtraCombinato(String nome, Integer tempoMin, Integer tempoMax,
            Integer ingredientiMin, Integer ingredientiMax) 
            throws SQLException, ValidationException {
        
        ValidationUtils.validateIntRange(tempoMin, tempoMax, "Tempo preparazione");
        ValidationUtils.validateIntRange(ingredientiMin, ingredientiMax, "Numero ingredienti");
        
        List<Ricetta> risultati = getAllRicette();
        
        if (nome != null && !nome.trim().isEmpty()) {
            String nomeLC = nome.toLowerCase().trim();
            risultati = risultati.stream()
                    .filter(r -> r.getNome().toLowerCase().contains(nomeLC))
                    .collect(Collectors.toList());
        }
        
        if (tempoMin != null) {
            risultati = risultati.stream()
                    .filter(r -> r.getTempoPreparazione() >= tempoMin)
                    .collect(Collectors.toList());
        }
        if (tempoMax != null) {
            risultati = risultati.stream()
                    .filter(r -> r.getTempoPreparazione() <= tempoMax)
                    .collect(Collectors.toList());
        }
        
        if (ingredientiMin != null) {
            risultati = risultati.stream()
                    .filter(r -> r.getNumeroIngredienti() >= ingredientiMin)
                    .collect(Collectors.toList());
        }
        if (ingredientiMax != null) {
            risultati = risultati.stream()
                    .filter(r -> r.getNumeroIngredienti() <= ingredientiMax)
                    .collect(Collectors.toList());
        }
        
        return risultati;
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

    public void associaRicettaASessione(Ricetta ricetta, InPresenza sessione) 
            throws ValidationException {
        
        ValidationUtils.validateNotNull(ricetta, "Ricetta");
        ValidationUtils.validateNotNull(sessione, "Sessione");
        
        if (!sessione.getRicette().contains(ricetta)) {
            sessione.getRicette().add(ricetta);
            ricetta.getSessioni().add(sessione);
        }
    }

    public void disassociaRicettaDaSessione(Ricetta ricetta, InPresenza sessione) {
        if (ricetta != null && sessione != null) {
            sessione.getRicette().remove(ricetta);
            ricetta.getSessioni().remove(sessione);
        }
    }

    public List<Ricetta> getRicetteNonAssociate(InPresenza sessione) throws SQLException {
        return getAllRicette().stream()
                .filter(r -> !sessione.getRicette().contains(r))
                .collect(Collectors.toList());
    }

    // ==================== CACHE ====================

    public void invalidaCache() {
        cachedRicette = null;
    }

    public void ricaricaCache() throws SQLException {
        cachedRicette = null;
        getAllRicette();
    }

    public GestioneRicette getGestioneRicette() {
        return gestioneRicette;
    }
}
