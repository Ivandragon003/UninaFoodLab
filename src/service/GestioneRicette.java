package service;

import dao.RicettaDAO;
import dao.UsaDAO;
import dao.IngredienteDAO;
import model.Ingrediente;
import model.Ricetta;
import model.Usa;
import model.InPresenza;
import exceptions.ValidationException;
import exceptions.ValidationUtils;
import exceptions.ErrorMessages;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GestioneRicette {
    
    private final RicettaDAO ricettaDAO;
    private final UsaDAO usaDAO;
    private final IngredienteDAO ingredienteDAO;

    public GestioneRicette(RicettaDAO ricettaDAO, UsaDAO usaDAO, IngredienteDAO ingredienteDAO) {
        this.ricettaDAO = ricettaDAO;
        this.usaDAO = usaDAO;
        this.ingredienteDAO = ingredienteDAO;
    }

    // ==================== CRUD ====================

    public void creaRicetta(Ricetta ricetta) throws SQLException, ValidationException {
        validateRicetta(ricetta);
        
        if (ricettaEsiste(ricetta.getNome())) {
            throw new ValidationException("Esiste già una ricetta con il nome: " + ricetta.getNome());
        }
        
        ricettaDAO.save(ricetta);
        salvaIngredientiRicetta(ricetta);
    }

    public void aggiornaRicetta(int id, Ricetta ricetta) throws SQLException, ValidationException {
        validateRicetta(ricetta);
        
        if (!ricettaDAO.findById(id).isPresent()) {
            throw new ValidationException(ErrorMessages.RICETTA_NON_TROVATA);
        }
        
        // Corretto: getByNome ritorna List<Ricetta>
        List<Ricetta> altreRicette = ricettaDAO.getByNome(ricetta.getNome());
        for (Ricetta altraRicetta : altreRicette) {
            if (altraRicetta.getIdRicetta() != id) {
                throw new ValidationException("Esiste già un'altra ricetta con questo nome");
            }
        }
        
        ricettaDAO.update(id, ricetta);
        usaDAO.deleteByRicetta(id);
        salvaIngredientiRicetta(ricetta);
    }

    public void cancellaRicetta(int id) throws SQLException, ValidationException {
        Optional<Ricetta> ricetta = ricettaDAO.findById(id);
        
        if (!ricetta.isPresent()) {
            throw new ValidationException(ErrorMessages.RICETTA_NON_TROVATA);
        }
        
        if (ricetta.get().getSessioni() != null && !ricetta.get().getSessioni().isEmpty()) {
            throw new ValidationException(
                "Impossibile eliminare: la ricetta è utilizzata in " + 
                ricetta.get().getSessioni().size() + " sessione/i"
            );
        }
        
        usaDAO.deleteByRicetta(id);
        ricettaDAO.delete(id);
    }

    // ==================== QUERY ====================

    public List<Ricetta> getAllRicette() throws SQLException {
        return ricettaDAO.getAll();
    }

    // Corretto: ritorna List invece di Optional
    public List<Ricetta> findByNome(String nome) throws SQLException {
        return ricettaDAO.getByNome(nome);
    }

    // ==================== FILTRI ====================

    public List<Ricetta> cercaPerNome(String nome, List<Ricetta> tutteRicette) {
        if (nome == null || nome.trim().isEmpty()) {
            return tutteRicette;
        }
        
        String nomeLC = nome.toLowerCase().trim();
        return tutteRicette.stream()
                .filter(r -> r.getNome().toLowerCase().contains(nomeLC))
                .collect(Collectors.toList());
    }

    public List<Ricetta> filtraCombinato(String nome, Integer tempoMin, Integer tempoMax,
            Integer ingredientiMin, Integer ingredientiMax, List<Ricetta> tutteRicette) 
            throws ValidationException {
        
        ValidationUtils.validateIntRange(tempoMin, tempoMax, "Tempo preparazione");
        ValidationUtils.validateIntRange(ingredientiMin, ingredientiMax, "Numero ingredienti");
        
        return tutteRicette.stream()
                .filter(r -> matchNome(r, nome))
                .filter(r -> matchRange(r.getTempoPreparazione(), tempoMin, tempoMax))
                .filter(r -> matchRange(r.getNumeroIngredienti(), ingredientiMin, ingredientiMax))
                .collect(Collectors.toList());
    }

    // ==================== INGREDIENTI ====================

    public void aggiungiIngrediente(Ricetta ricetta, Ingrediente ingrediente, double quantita) 
            throws SQLException, ValidationException {
        validateIngredienteParams(ricetta, ingrediente, quantita);
        
        if (ricetta.getIngredienti().containsKey(ingrediente)) {
            throw new ValidationException("Ingrediente già presente nella ricetta");
        }
        
        Ingrediente ingredienteConID = recuperaOCreaIngrediente(ingrediente);
        ricetta.getIngredienti().put(ingredienteConID, quantita);
        usaDAO.save(new Usa(ricetta, ingredienteConID, quantita));
    }

    public void aggiornaQuantitaIngrediente(Ricetta ricetta, Ingrediente ingrediente, double nuovaQuantita) 
            throws SQLException, ValidationException {
        validateIngredienteParams(ricetta, ingrediente, nuovaQuantita);
        
        if (!ricetta.getIngredienti().containsKey(ingrediente)) {
            throw new ValidationException("Ingrediente non presente nella ricetta");
        }
        
        ricetta.getIngredienti().put(ingrediente, nuovaQuantita);
        usaDAO.updateQuantita(new Usa(ricetta, ingrediente, nuovaQuantita));
    }

    public void rimuoviIngrediente(Ricetta ricetta, Ingrediente ingrediente) 
            throws SQLException, ValidationException {
        if (ricetta == null) throw new ValidationException("Ricetta non può essere nulla");
        if (ingrediente == null) throw new ValidationException("Ingrediente non può essere nullo");
        
        if (!ricetta.getIngredienti().containsKey(ingrediente)) {
            throw new ValidationException("Ingrediente non presente nella ricetta");
        }
        
        ricetta.getIngredienti().remove(ingrediente);
        usaDAO.delete(new Usa(ricetta, ingrediente, 0));
    }

    // ==================== SESSIONI ====================

    public void associaRicettaASessione(Ricetta ricetta, InPresenza sessione) throws ValidationException {
        if (ricetta == null) throw new ValidationException("Ricetta non può essere nulla");
        if (sessione == null) throw new ValidationException("Sessione non può essere nulla");
        
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

    public List<Ricetta> getRicetteNonAssociate(InPresenza sessione, List<Ricetta> tutteRicette) {
        return tutteRicette.stream()
                .filter(r -> !sessione.getRicette().contains(r))
                .collect(Collectors.toList());
    }

    // ==================== VALIDAZIONI PRIVATE ====================

    private void validateRicetta(Ricetta ricetta) throws ValidationException {
        ValidationUtils.validateNotNull(ricetta, "Ricetta");
        ValidationUtils.validateNomeRicetta(ricetta.getNome());
        ValidationUtils.validateTempoPreparazione(ricetta.getTempoPreparazione());
        ValidationUtils.validateIngredienti(ricetta.getIngredienti());
    }

    private void validateIngredienteParams(Ricetta ricetta, Ingrediente ingrediente, double quantita) 
            throws ValidationException {
        ValidationUtils.validateNotNull(ricetta, "Ricetta");
        ValidationUtils.validateNotNull(ingrediente, "Ingrediente");
        ValidationUtils.validateQuantita(quantita);
    }

    // ==================== HELPER ====================

    private boolean matchNome(Ricetta r, String nome) {
        if (nome == null || nome.trim().isEmpty()) return true;
        return r.getNome().toLowerCase().contains(nome.toLowerCase().trim());
    }

    private boolean matchRange(int value, Integer min, Integer max) {
        if (min != null && value < min) return false;
        return max == null || value <= max;
    }

    // ==================== SUPPORTO ====================

    private void salvaIngredientiRicetta(Ricetta ricetta) throws SQLException {
        for (Map.Entry<Ingrediente, Double> entry : ricetta.getIngredienti().entrySet()) {
            Ingrediente ingredienteConID = recuperaOCreaIngrediente(entry.getKey());
            usaDAO.save(new Usa(ricetta, ingredienteConID, entry.getValue()));
        }
    }

    private Ingrediente recuperaOCreaIngrediente(Ingrediente ingrediente) throws SQLException {
        // Usa findByNome se esiste nel tuo IngredienteDAO, altrimenti crea wrapper
        List<Ingrediente> trovati = ingredienteDAO.getAll().stream()
                .filter(i -> i.getNome().equalsIgnoreCase(ingrediente.getNome()))
                .collect(Collectors.toList());
        
        if (trovati.isEmpty()) {
            ingredienteDAO.save(ingrediente);
            trovati = ingredienteDAO.getAll().stream()
                    .filter(i -> i.getNome().equalsIgnoreCase(ingrediente.getNome()))
                    .collect(Collectors.toList());
        }
        
        if (trovati.isEmpty()) {
            throw new SQLException("Impossibile recuperare ingrediente: " + ingrediente.getNome());
        }
        
        return trovati.get(0);
    }

    private boolean ricettaEsiste(String nome) throws SQLException {
        return !ricettaDAO.getByNome(nome).isEmpty();
    }

    public List<Ingrediente> getAllIngredienti() throws SQLException {
        return ingredienteDAO.getAll();
    }
}
