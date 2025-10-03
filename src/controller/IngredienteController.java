package controller;

import dao.IngredienteDAO;
import model.Ingrediente;
import service.GestioneIngrediente;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Controller per la gestione degli ingredienti
 * Coordina le operazioni tra GUI e service layer
 */
public class IngredienteController {

    private final GestioneIngrediente gestioneIngrediente;

    public IngredienteController(GestioneIngrediente gestioneIngrediente) {
        this.gestioneIngrediente = gestioneIngrediente;
    }

    /**
     * Crea un nuovo ingrediente
     */
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

    /**
     * Ottiene tutti gli ingredienti
     */
    public List<Ingrediente> getAllIngredienti() throws SQLException {
        return gestioneIngrediente.getAllIngredienti();
    }

    /**
     * Cerca ingredienti per nome
     */
    public List<Ingrediente> cercaIngredientiPerNome(String nome) throws SQLException {
        return gestioneIngrediente.cercaPerNome(nome);
    }

    /**
     * Cerca ingredienti per tipo
     */
    public List<Ingrediente> cercaIngredientiPerTipo(String tipo) throws SQLException {
        return gestioneIngrediente.cercaPerTipo(tipo);
    }

    /**
     * Trova ingrediente per ID
     */
    public Optional<Ingrediente> trovaIngredientePerId(int id) throws SQLException {
        return gestioneIngrediente.trovaIngredientePerId(id);
    }

    /**
     * Aggiorna un ingrediente esistente
     */
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

    /**
     * Elimina un ingrediente
     */
    public void eliminaIngrediente(int id) throws SQLException {
        gestioneIngrediente.eliminaIngrediente(id);
    }

    /**
     * Verifica se un ingrediente esiste già
     */
    public boolean ingredienteEsiste(String nome) throws SQLException {
        return gestioneIngrediente.ingredienteEsiste(nome);
    }
}
