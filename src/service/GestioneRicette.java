package service;

import dao.RicettaDAO;
import exceptions.DataAccessException;
import exceptions.ValidationException;
import exceptions.ValidationUtils;
import model.Ricetta;

import java.sql.SQLException;
import java.util.List;

/**
 * Servizio dedicato esclusivamente alla gestione delle ricette.
 * CRUD, ricerca e filtraggio semplice.
 */
public class GestioneRicette {

    private final RicettaDAO ricettaDAO;

    public GestioneRicette(RicettaDAO ricettaDAO) {
        this.ricettaDAO = ricettaDAO;
    }

    // ==================== CRUD ====================

    public void creaRicetta(Ricetta ricetta) throws ValidationException, DataAccessException {
        // assumiamo che il model assicuri campi non-null; qui restano le validazioni di business
        validateRicetta(ricetta);

        try {
            if (ricettaEsiste(ricetta.getNome())) {
                throw new ValidationException("Esiste già una ricetta con il nome: " + ricetta.getNome());
            }
            ricettaDAO.save(ricetta);
        } catch (SQLException e) {
            throw new DataAccessException("Errore durante la creazione della ricetta", e);
        }
    }

    public void aggiornaRicetta(int id, Ricetta ricetta) throws ValidationException, DataAccessException {
        validateRicetta(ricetta);

        try {
            ricettaDAO.findById(id)
                .orElseThrow(() -> new ValidationException("Ricetta non trovata"));

            List<Ricetta> omonime = ricettaDAO.getByNome(ricetta.getNome());
            for (Ricetta r : omonime) {
                if (r.getIdRicetta() != id) {
                    throw new ValidationException("Esiste già un'altra ricetta con questo nome");
                }
            }

            ricettaDAO.update(id, ricetta);
        } catch (SQLException e) {
            throw new DataAccessException("Errore durante l'aggiornamento della ricetta", e);
        }
    }

    public void cancellaRicetta(int id) throws ValidationException, DataAccessException {
        try {
            ricettaDAO.findById(id)
                .orElseThrow(() -> new ValidationException("Ricetta non trovata"));

            ricettaDAO.delete(id);
        } catch (SQLException e) {
            throw new DataAccessException("Errore durante la cancellazione della ricetta", e);
        }
    }

    // ==================== QUERY ====================

    public List<Ricetta> getAllRicette() throws DataAccessException {
        try {
            return ricettaDAO.getAll();
        } catch (SQLException e) {
            throw new DataAccessException("Errore nel recupero delle ricette", e);
        }
    }

    public List<Ricetta> findByNome(String nome) throws DataAccessException {
        try {
            return ricettaDAO.getByNome(nome);
        } catch (SQLException e) {
            throw new DataAccessException("Errore durante la ricerca per nome", e);
        }
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
        // tolta la validateNotNull(ricetta, "Ricetta") per evitare ridondanza col model
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

    private boolean ricettaEsiste(String nome) throws DataAccessException {
        try {
            return !ricettaDAO.getByNome(nome).isEmpty();
        } catch (SQLException e) {
            throw new DataAccessException("Errore durante la verifica di esistenza della ricetta", e);
        }
    }
}
