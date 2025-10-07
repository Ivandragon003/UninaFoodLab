package controller;

import exceptions.ValidationException;
import model.Ingrediente;
import service.GestioneIngrediente;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class IngredienteController {

    private final GestioneIngrediente gestioneIngrediente;

    public IngredienteController(GestioneIngrediente gestioneIngrediente) {
        this.gestioneIngrediente = gestioneIngrediente;
    }

    public int creaIngrediente(String nome, String tipo) throws SQLException, ValidationException {
        Ingrediente ingrediente = new Ingrediente(
                nome == null ? null : nome.trim(),
                tipo == null ? null : tipo.trim()
        );
        return gestioneIngrediente.salvaIngrediente(ingrediente);
    }

    public List<Ingrediente> getAllIngredienti() throws SQLException {
        return gestioneIngrediente.getAllIngredienti();
    }

    public List<Ingrediente> cercaIngredientiPerNome(String nome) throws SQLException, ValidationException {
        return gestioneIngrediente.cercaPerNome(nome);
    }

    public List<Ingrediente> cercaIngredientiPerTipo(String tipo) throws SQLException, ValidationException {
        return gestioneIngrediente.cercaPerTipo(tipo);
    }

    public Optional<Ingrediente> trovaIngredientePerId(int id) throws SQLException {
        return gestioneIngrediente.trovaIngredientePerId(id);
    }

    public Optional<Ingrediente> trovaIngredientePerNome(String nome) throws SQLException, ValidationException {
        return gestioneIngrediente.trovaIngredientePerNome(nome);
    }

    public void aggiornaIngrediente(int id, String nome, String tipo) throws SQLException, ValidationException {
        Ingrediente ingrediente = new Ingrediente(
                nome == null ? null : nome.trim(),
                tipo == null ? null : tipo.trim()
        );
        gestioneIngrediente.aggiornaIngrediente(id, ingrediente);
    }

    public void eliminaIngrediente(int id) throws SQLException, ValidationException {
        gestioneIngrediente.eliminaIngrediente(id);
    }

    public boolean ingredienteEsiste(String nome) throws SQLException, ValidationException {
        return gestioneIngrediente.ingredienteEsiste(nome);
    }
}
