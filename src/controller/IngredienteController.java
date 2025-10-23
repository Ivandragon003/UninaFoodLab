package controller;

import dao.IngredienteDAO;
import exceptions.DataAccessException;
import exceptions.ValidationException;
import helper.ValidationUtils;
import model.Ingrediente;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class IngredienteController {

    private final IngredienteDAO ingredienteDAO;

    public IngredienteController(IngredienteDAO ingredienteDAO) {
        this.ingredienteDAO = ingredienteDAO;
    }

    public int creaIngrediente(String nome, String tipo) throws ValidationException, DataAccessException {
      
        String n = ValidationUtils.validateNotEmpty(nome, "Nome ingrediente");
        String t = (tipo == null || tipo.trim().isEmpty()) ? null : tipo.trim();

        Ingrediente ingrediente = new Ingrediente(n, t);
        try {
            int id = ingredienteDAO.save(ingrediente);
            ingrediente.setIdIngrediente(id);
            return id;
        } catch (SQLException e) {
            throw new DataAccessException("Errore durante il salvataggio dell'ingrediente", e);
        }
    }

    public List<Ingrediente> getAllIngredienti() throws DataAccessException {
        try {
            return ingredienteDAO.getAll();
        } catch (SQLException e) {
            throw new DataAccessException("Errore nel recupero degli ingredienti", e);
        }
    }

    public List<Ingrediente> cercaIngredientiPerNome(String nome) throws DataAccessException {
        return filtraLocal(i -> {
            if (nome == null || nome.trim().isEmpty())
                return true;
            String q = nome.trim().toLowerCase();
            return i.getNome() != null && i.getNome().toLowerCase().contains(q);
        });
    }

    public List<Ingrediente> cercaIngredientiPerTipo(String tipo) throws DataAccessException {
        return filtraLocal(i -> {
            if (tipo == null || tipo.trim().isEmpty())
                return true;
            String q = tipo.trim().toLowerCase();
            return i.getTipo() != null && i.getTipo().toLowerCase().contains(q);
        });
    }

    private List<Ingrediente> filtraLocal(java.util.function.Predicate<Ingrediente> p) throws DataAccessException {
        try {
            return ingredienteDAO.getAll().stream().filter(p).toList();
        } catch (SQLException e) {
            throw new DataAccessException("Errore durante la ricerca ingredienti", e);
        }
    }

    public Optional<Ingrediente> trovaIngredientePerId(int id) throws DataAccessException {
        try {
            return ingredienteDAO.findById(id);
        } catch (SQLException e) {
            throw new DataAccessException("Errore nel recupero dell'ingrediente con ID " + id, e);
        }
    }
}
