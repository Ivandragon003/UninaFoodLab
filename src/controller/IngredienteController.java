package controller;

import exceptions.ValidationException;
import exceptions.DataAccessException;
import exceptions.ErrorMessages;
import model.Ingrediente;
import service.GestioneIngrediente;

import java.util.List;
import java.util.Optional;

public class IngredienteController {
    private final GestioneIngrediente gestioneIngrediente;

    public IngredienteController(GestioneIngrediente gestioneIngrediente) {
        this.gestioneIngrediente = gestioneIngrediente;
    }

    // ✅ SEMPLIFICATO: Validazione delegata al service
    public int creaIngrediente(String nome, String tipo) throws ValidationException, DataAccessException {
        Ingrediente ingrediente = new Ingrediente(
            nome == null ? null : nome.trim(),
            tipo == null ? null : tipo.trim()
        );
        return gestioneIngrediente.salvaIngrediente(ingrediente);
    }

    public List<Ingrediente> getAllIngredienti() throws DataAccessException {
        return gestioneIngrediente.getAllIngredienti();
    }

    public List<Ingrediente> cercaIngredientiPerNome(String nome) throws DataAccessException {
        return gestioneIngrediente.cercaPerNome(nome);
    }

    public List<Ingrediente> cercaIngredientiPerTipo(String tipo) throws DataAccessException {
        return gestioneIngrediente.cercaPerTipo(tipo);
    }

    public Optional<Ingrediente> trovaIngredientePerId(int id) throws DataAccessException {
        return gestioneIngrediente.trovaIngredientePerId(id);
    }

    public Optional<Ingrediente> trovaIngredientePerNome(String nome) throws ValidationException, DataAccessException {
        return gestioneIngrediente.trovaIngredientePerNome(nome);
    }

    
}