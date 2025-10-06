package service;

import dao.RicettaDAO;
import dao.UsaDAO;
import dao.IngredienteDAO;
import model.Ingrediente;
import model.Ricetta;
import model.Usa;
import exceptions.ValidationException;
import exceptions.ValidationUtils;
import exceptions.ErrorMessages;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GestioneRicette {
    
    private final RicettaDAO ricettaDAO;
    private final UsaDAO usaDAO;
    private final IngredienteDAO ingredienteDAO;

    public GestioneRicette(RicettaDAO ricettaDAO, UsaDAO usaDAO, IngredienteDAO ingredienteDAO) {
        this.ricettaDAO = ricettaDAO;
        this.usaDAO = usaDAO;
        this.ingredienteDAO = ingredienteDAO;
    }

    // ==================== CRUD OPERATIONS ====================

    public void creaRicetta(Ricetta ricetta) throws SQLException, ValidationException {
        ValidationUtils.validateNotNull(ricetta, "Ricetta");
        ValidationUtils.validateNomeRicetta(ricetta.getNome());
        ValidationUtils.validateTempoPreparazione(ricetta.getTempoPreparazione());
        ValidationUtils.validateIngredienti(ricetta.getIngredienti());
        
        if (ricettaEsiste(ricetta.getNome())) {
            throw new ValidationException("Esiste già una ricetta con il nome: " + ricetta.getNome());
        }
        
        ricettaDAO.save(ricetta);
        salvaIngredientiRicetta(ricetta);
    }

    public void aggiornaRicetta(int id, Ricetta ricetta) throws SQLException, ValidationException {
        ValidationUtils.validateNotNull(ricetta, "Ricetta");
        ValidationUtils.validateNomeRicetta(ricetta.getNome());
        ValidationUtils.validateTempoPreparazione(ricetta.getTempoPreparazione());
        ValidationUtils.validateIngredienti(ricetta.getIngredienti());
        
        if (!ricettaDAO.findById(id).isPresent()) {
            throw new ValidationException(ErrorMessages.RICETTA_NON_TROVATA);
        }
        
        Optional<Ricetta> altraRicetta = ricettaDAO.getByNome(ricetta.getNome());
        if (altraRicetta.isPresent() && altraRicetta.get().getIdRicetta() != id) {
            throw new ValidationException("Esiste già un'altra ricetta con questo nome");
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

    // ==================== INGREDIENTI ====================

    public void aggiungiIngrediente(Ricetta ricetta, Ingrediente ingrediente, double quantita) 
            throws SQLException, ValidationException {
        
        ValidationUtils.validateNotNull(ricetta, "Ricetta");
        ValidationUtils.validateNotNull(ingrediente, "Ingrediente");
        ValidationUtils.validateQuantita(quantita);
        
        if (ricetta.getIngredienti().containsKey(ingrediente)) {
            throw new ValidationException("Ingrediente già presente nella ricetta");
        }
        
        Ingrediente ingredienteConID = recuperaOCreaIngrediente(ingrediente);
        ricetta.getIngredienti().put(ingredienteConID, quantita);
        
        Usa usa = new Usa(ricetta, ingredienteConID, quantita);
        usaDAO.save(usa);
    }

    public void aggiornaQuantitaIngrediente(Ricetta ricetta, Ingrediente ingrediente, double quantita) 
            throws SQLException, ValidationException {
        
        ValidationUtils.validateNotNull(ricetta, "Ricetta");
        ValidationUtils.validateNotNull(ingrediente, "Ingrediente");
        ValidationUtils.validateQuantita(quantita);
        
        if (!ricetta.getIngredienti().containsKey(ingrediente)) {
            throw new ValidationException("Ingrediente non presente nella ricetta");
        }
        
        ricetta.getIngredienti().put(ingrediente, quantita);
        Usa usa = new Usa(ricetta, ingrediente, quantita);
        usaDAO.updateQuantita(usa);
    }

    public void rimuoviIngrediente(Ricetta ricetta, Ingrediente ingrediente) 
            throws SQLException, ValidationException {
        
        ValidationUtils.validateNotNull(ricetta, "Ricetta");
        ValidationUtils.validateNotNull(ingrediente, "Ingrediente");
        
        if (!ricetta.getIngredienti().containsKey(ingrediente)) {
            throw new ValidationException("Ingrediente non presente nella ricetta");
        }
        
        ricetta.getIngredienti().remove(ingrediente);
        Usa usa = new Usa(ricetta, ingrediente, 0);
        usaDAO.delete(usa);
    }

    // ==================== QUERY ====================

    public List<Ricetta> getAllRicette() throws SQLException {
        return ricettaDAO.getAll();
    }

    public List<Ingrediente> getAllIngredienti() throws SQLException {
        return ingredienteDAO.getAll();
    }

    public Optional<Ricetta> findByNome(String nome) throws SQLException {
        return ricettaDAO.findByNome(nome);
    }

    // ==================== SUPPORTO ====================

    private void salvaIngredientiRicetta(Ricetta ricetta) throws SQLException {
        for (Map.Entry<Ingrediente, Double> entry : ricetta.getIngredienti().entrySet()) {
            Ingrediente ingredienteConID = recuperaOCreaIngrediente(entry.getKey());
            Usa usa = new Usa(ricetta, ingredienteConID, entry.getValue());
            usaDAO.save(usa);
        }
    }

    private Ingrediente recuperaOCreaIngrediente(Ingrediente ingrediente) throws SQLException {
        Optional<Ingrediente> ingrDaDB = ingredienteDAO.findByNome(ingrediente.getNome());
        
        if (!ingrDaDB.isPresent()) {
            ingredienteDAO.save(ingrediente);
            ingrDaDB = ingredienteDAO.findByNome(ingrediente.getNome());
        }
        
        return ingrDaDB.orElseThrow(() -> 
            new SQLException("Impossibile recuperare ingrediente dal database: " + ingrediente.getNome())
        );
    }

    private boolean ricettaEsiste(String nome) throws SQLException {
        return ricettaDAO.findByNome(nome).isPresent();
    }
}
