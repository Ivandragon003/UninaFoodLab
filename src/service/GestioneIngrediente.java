
package service;

import dao.IngredienteDAO;
import model.Ingrediente;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service per la gestione degli ingredienti
 * Collegato al DAO per le operazioni sul database
 */
public class GestioneIngrediente {

    private final IngredienteDAO ingredienteDAO;

    public GestioneIngrediente(IngredienteDAO ingredienteDAO) {
        this.ingredienteDAO = ingredienteDAO;
    }

    /**
     * Salva un nuovo ingrediente nel database
     * @return l'ID generato dall'ingrediente
     */
    public int salvaIngrediente(Ingrediente ingrediente) throws SQLException {
        // Verifica se esiste già un ingrediente con lo stesso nome
        if (ingredienteEsiste(ingrediente.getNome())) {
            throw new IllegalArgumentException("Esiste già un ingrediente con questo nome: " + ingrediente.getNome());
        }
        return ingredienteDAO.save(ingrediente);
    }

    /**
     * Ottiene tutti gli ingredienti ordinati per nome
     */
    public List<Ingrediente> getAllIngredienti() throws SQLException {
        return ingredienteDAO.getAll();
    }

    /**
     * Cerca ingredienti che contengono il nome specificato
     */
    public List<Ingrediente> cercaPerNome(String nome) throws SQLException {
        if (nome == null || nome.trim().isEmpty()) {
            return getAllIngredienti();
        }
        // Implemento una ricerca che cerca per nome parziale
        List<Ingrediente> tuttiIngredienti = getAllIngredienti();
        return tuttiIngredienti.stream()
                .filter(ing -> ing.getNome().toLowerCase().contains(nome.toLowerCase().trim()))
                .toList();
    }

    /**
     * Cerca ingredienti per tipo
     */
    public List<Ingrediente> cercaPerTipo(String tipo) throws SQLException {
        return ingredienteDAO.getByTipo(tipo);
    }

    /**
     * Trova ingrediente per ID
     */
    public Optional<Ingrediente> trovaIngredientePerId(int id) throws SQLException {
        return ingredienteDAO.findById(id);
    }

    /**
     * Trova ingrediente per nome esatto
     */
    public Optional<Ingrediente> trovaIngredientePerNome(String nome) throws SQLException {
        return ingredienteDAO.findByNome(nome);
    }

    /**
     * Aggiorna un ingrediente esistente
     */
    public void aggiornaIngrediente(int id, Ingrediente ingrediente) throws SQLException {
        // Verifica che l'ingrediente esista
        Optional<Ingrediente> esistente = ingredienteDAO.findById(id);
        if (esistente.isEmpty()) {
            throw new IllegalArgumentException("Ingrediente con ID " + id + " non trovato");
        }

        // Verifica che non ci sia già un altro ingrediente con lo stesso nome
        Optional<Ingrediente> altroIngrediente = ingredienteDAO.findByNome(ingrediente.getNome());
        if (altroIngrediente.isPresent() && altroIngrediente.get().getIdIngrediente() != id) {
            throw new IllegalArgumentException("Esiste già un altro ingrediente con questo nome: " + ingrediente.getNome());
        }

        ingredienteDAO.update(id, ingrediente);
    }

    /**
     * Elimina un ingrediente
     */
    public void eliminaIngrediente(int id) throws SQLException {
        Optional<Ingrediente> ingrediente = ingredienteDAO.findById(id);
        if (ingrediente.isEmpty()) {
            throw new IllegalArgumentException("Ingrediente con ID " + id + " non trovato");
        }
        ingredienteDAO.delete(id);
    }

    /**
     * Verifica se esiste un ingrediente con il nome specificato
     */
    public boolean ingredienteEsiste(String nome) throws SQLException {
        return ingredienteDAO.findByNome(nome).isPresent();
    }

    /**
     * Ottiene tutti i tipi di ingredienti distinti
     */
    public List<String> getTipiIngredienti() throws SQLException {
        return getAllIngredienti().stream()
                .map(Ingrediente::getTipo)
                .distinct()
                .sorted()
                .toList();
    }
}
