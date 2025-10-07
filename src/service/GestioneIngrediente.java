package service;

import dao.IngredienteDAO;
import exceptions.ValidationException;
import exceptions.ValidationUtils;
import model.Ingrediente;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class GestioneIngredienti {

    private final IngredienteDAO ingredienteDAO;

    public GestioneIngredienti(IngredienteDAO ingredienteDAO) {
        this.ingredienteDAO = ingredienteDAO;
    }

    public void creaIngrediente(Ingrediente ingrediente) throws SQLException, ValidationException {
        validateIngrediente(ingrediente);
        ingredienteDAO.save(ingrediente);
    }

    public void aggiornaIngrediente(int id, Ingrediente ingrediente) throws SQLException, ValidationException {
        validateIngrediente(ingrediente);
        ingredienteDAO.update(id, ingrediente);
    }

    public void cancellaIngrediente(int id) throws SQLException {
        ingredienteDAO.delete(id);
    }

    public List<Ingrediente> getAllIngredienti() throws SQLException {
        return ingredienteDAO.getAll();
    }

    public Optional<Ingrediente> findById(int id) throws SQLException {
        return ingredienteDAO.findById(id);
    }

    private void validateIngrediente(Ingrediente ingrediente) throws ValidationException {
        ValidationUtils.validateNotNull(ingrediente, "Ingrediente");
        ValidationUtils.validateNomeIngrediente(ingrediente.getNome());
    }
}
