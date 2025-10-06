
package controller;

import dao.IngredienteDAO;
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

    public int creaIngrediente(String nome, String tipo) throws SQLException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome dell'ingrediente è obbligatorio");
        }
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("Il tipo dell'ingrediente è obbligatorio");
        }

        Ingrediente ingrediente = new Ingrediente(nome.trim(), tipo.trim());
        return gestioneIngrediente.salvaIngrediente(ingrediente);
    }

    public List<Ingrediente> getAllIngredienti() throws SQLException {
        return gestioneIngrediente.getAllIngredienti();
    }

    public List<Ingrediente> cercaIngredientiPerNome(String nome) throws SQLException {
        return gestioneIngrediente.cercaPerNome(nome);
    }

    public List<Ingrediente> cercaIngredientiPerTipo(String tipo) throws SQLException {
        return gestioneIngrediente.cercaPerTipo(tipo);
    }

    public Optional<Ingrediente> trovaIngredientePerId(int id) throws SQLException {
        return gestioneIngrediente.trovaIngredientePerId(id);
    }

    public void aggiornaIngrediente(int id, String nome, String tipo) throws SQLException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome dell'ingrediente è obbligatorio");
        }
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("Il tipo dell'ingrediente è obbligatorio");
        }

        Ingrediente ingrediente = new Ingrediente(nome.trim(), tipo.trim());
        gestioneIngrediente.aggiornaIngrediente(id, ingrediente);
    }
    
    public void eliminaIngrediente(int id) throws SQLException {
        gestioneIngrediente.eliminaIngrediente(id);
    }
    
    public boolean ingredienteEsiste(String nome) throws SQLException {
        return gestioneIngrediente.ingredienteEsiste(nome);
    }
}
