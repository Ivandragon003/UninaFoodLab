package service;

import dao.RicettaDAO;
import exceptions.ValidationException;
import exceptions.ValidationUtils;
import model.Ricetta;

import java.sql.SQLException;
import java.util.List;

/**
 * Servizio dedicato esclusivamente alla gestione delle ricette.
 * CRUD, ricerca e filtraggio semplice.
 * Non gestisce ingredienti né associazioni a sessioni.
 */
public class GestioneRicette {

    private final RicettaDAO ricettaDAO;

    public GestioneRicette(RicettaDAO ricettaDAO) {
        this.ricettaDAO = ricettaDAO;
    }

    // ==================== CRUD ====================

    public void creaRicetta(Ricetta ricetta) throws SQLException, ValidationException {
        validateRicetta(ricetta);
        if (ricettaEsiste(ricetta.getNome())) {
            throw new ValidationException("Esiste già una ricetta con il nome: " + ricetta.getNome());
        }
        ricettaDAO.save(ricetta);
    }

    public void aggiornaRicetta(int id, Ricetta ricetta) throws SQLException, ValidationException {
        validateRicetta(ricetta);

      
        ricettaDAO.findById(id)
            .orElseThrow(() -> new ValidationException("Ricetta non trovata"));
     
        List<Ricetta> omonime = ricettaDAO.getByNome(ricetta.getNome());
        for (Ricetta r : omonime) {
            if (r.getIdRicetta() != id) {
                throw new ValidationException("Esiste già un'altra ricetta con questo nome");
            }
        }

        ricettaDAO.update(id, ricetta);
    }

    public void cancellaRicetta(int id) throws SQLException, ValidationException {
        
        ricettaDAO.findById(id)
            .orElseThrow(() -> new ValidationException("Ricetta non trovata"));

        ricettaDAO.delete(id);
    }


    // ==================== QUERY ====================

    public List<Ricetta> getAllRicette() throws SQLException {
        return ricettaDAO.getAll();
    }

    public List<Ricetta> findByNome(String nome) throws SQLException {
        return ricettaDAO.getByNome(nome);
    }

    // ==================== FILTRI ====================

    public List<Ricetta> cercaPerNome(String nome, List<Ricetta> tutteRicette) {
        if (nome == null || nome.trim().isEmpty()) return tutteRicette;
        String nomeLC = nome.toLowerCase().trim();
        return tutteRicette.stream()
                .filter(r -> r.getNome() != null && r.getNome().toLowerCase().contains(nomeLC))
                .toList();
    }

    public List<Ricetta> filtraCombinato(String nome, Integer tempoMin, Integer tempoMax,
                                         Integer ingredientiMin, Integer ingredientiMax,
                                         List<Ricetta> tutteRicette) throws ValidationException {
        ValidationUtils.validateIntRange(tempoMin, tempoMax, "Tempo preparazione");
        ValidationUtils.validateIntRange(ingredientiMin, ingredientiMax, "Numero ingredienti");

        return tutteRicette.stream()
                .filter(r -> matchNome(r, nome))
                .filter(r -> matchRange(r.getTempoPreparazione(), tempoMin, tempoMax))
                .filter(r -> matchRange(r.getNumeroIngredienti(), ingredientiMin, ingredientiMax))
                .toList();
    }

    // ==================== VALIDAZIONI PRIVATE ====================

    private void validateRicetta(Ricetta ricetta) throws ValidationException {
        ValidationUtils.validateNotNull(ricetta, "Ricetta");
        ValidationUtils.validateNomeRicetta(ricetta.getNome());
        ValidationUtils.validateTempoPreparazione(ricetta.getTempoPreparazione());
    }

    private boolean matchNome(Ricetta r, String nome) {
        if (nome == null || nome.trim().isEmpty()) return true;
        return r.getNome() != null && r.getNome().toLowerCase().contains(nome.toLowerCase().trim());
    }

    private boolean matchRange(int value, Integer min, Integer max) {
        if (min != null && value < min) return false;
        return max == null || value <= max;
    }

    private boolean ricettaEsiste(String nome) throws SQLException {
        return !ricettaDAO.getByNome(nome).isEmpty();
    }
}
