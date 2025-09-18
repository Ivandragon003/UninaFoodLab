package service;

import dao.IngredienteDAO;
import dao.UsaDAO;
import model.Ingrediente;
import model.Usa;

import java.sql.SQLException;
import java.util.List;

public class GestioneIngrediente {

    private final IngredienteDAO ingredienteDAO;
    private final UsaDAO usaDAO;

    public GestioneIngrediente(IngredienteDAO ingredienteDAO, UsaDAO usaDAO) {
        this.ingredienteDAO = ingredienteDAO;
        this.usaDAO = usaDAO;
    }

    // Crea un nuovo ingrediente e ritorna l'ID generato
    public int creaIngrediente(Ingrediente ingrediente) throws SQLException {
        if (ingrediente.getNome() == null || ingrediente.getNome().isEmpty()) {
            throw new IllegalArgumentException("Nome ingrediente non valido");
        }
        if (ingrediente.getTipo() == null || ingrediente.getTipo().isEmpty()) {
            throw new IllegalArgumentException("Tipo ingrediente non valido");
        }
        return ingredienteDAO.save(ingrediente);
    }

    // Aggiorna un ingrediente esistente tramite ID
    public void aggiornaIngrediente(int id, Ingrediente ingrediente) throws SQLException {
        if (ingrediente.getNome() == null || ingrediente.getNome().isEmpty()) {
            throw new IllegalArgumentException("Nome ingrediente non valido");
        }
        if (ingrediente.getTipo() == null || ingrediente.getTipo().isEmpty()) {
            throw new IllegalArgumentException("Tipo ingrediente non valido");
        }
        ingredienteDAO.update(id, ingrediente);
    }

    // Cancella un ingrediente tramite ID (elimina prima eventuali riferimenti in Usa)
    public void cancellaIngrediente(int id) throws SQLException {
        // usa l'istanza non statica
        usaDAO.deleteByIngrediente(id); // rimuove tutte le righe Usa collegate
        ingredienteDAO.delete(id);
    }

    // Restituisce l'ingrediente tramite ID
    public Ingrediente getIngrediente(int id) throws SQLException {
        return ingredienteDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ingrediente non trovato con id: " + id));
    }

    // Restituisce tutti gli ingredienti
    public List<Ingrediente> getAllIngredienti() throws SQLException {
        return ingredienteDAO.getAll();
    }

    // Ricerca per nome esatto
    public List<Ingrediente> getIngredientiPerNome(String nome) throws SQLException {
        return ingredienteDAO.getByNome(nome);
    }

    // Ricerca per tipo
    public List<Ingrediente> getIngredientiPerTipo(String tipo) throws SQLException {
        return ingredienteDAO.getByTipo(tipo);
    }

    // Aggiunge una relazione Usa tra ingrediente e ricetta
    public void aggiungiIngredienteARicetta(int idIngrediente, int idRicetta, double quantita) throws SQLException {
        Usa usa = new Usa(idIngrediente, idRicetta, quantita);
        usaDAO.save(usa);
    }

    // Rimuove un ingrediente da una ricetta
    public void rimuoviIngredienteDaRicetta(int idIngrediente, int idRicetta) throws SQLException {
        usaDAO.delete(idIngrediente, idRicetta);
    }
}
