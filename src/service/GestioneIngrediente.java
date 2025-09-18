package service;

import dao.IngredienteDAO;
import dao.UsaDAO;
import model.Ingrediente;
import model.Usa;
import model.Ricetta;

import java.sql.SQLException;
import java.util.List;

public class GestioneIngrediente {

    private final IngredienteDAO ingredienteDAO;
    private final UsaDAO usaDAO;

    public GestioneIngrediente(IngredienteDAO ingredienteDAO, UsaDAO usaDAO) {
        this.ingredienteDAO = ingredienteDAO;
        this.usaDAO = usaDAO;
    }

    public int creaIngrediente(Ingrediente ingrediente) throws SQLException {
        if (ingrediente.getNome() == null || ingrediente.getNome().isEmpty()) {
            throw new IllegalArgumentException("Nome ingrediente non valido");
        }
        if (ingrediente.getTipo() == null || ingrediente.getTipo().isEmpty()) {
            throw new IllegalArgumentException("Tipo ingrediente non valido");
        }
        return ingredienteDAO.save(ingrediente);
    }

    public void aggiornaIngrediente(int id, Ingrediente ingrediente) throws SQLException {
        if (ingrediente.getNome() == null || ingrediente.getNome().isEmpty()) {
            throw new IllegalArgumentException("Nome ingrediente non valido");
        }
        if (ingrediente.getTipo() == null || ingrediente.getTipo().isEmpty()) {
            throw new IllegalArgumentException("Tipo ingrediente non valido");
        }
        ingredienteDAO.update(id, ingrediente);
    }

    public void cancellaIngrediente(Ingrediente ingrediente) throws SQLException {
        usaDAO.deleteByIngrediente(ingrediente);

        ingredienteDAO.delete(ingrediente.getIdIngrediente());
    }

    public void aggiungiIngredienteARicetta( Ricetta ricetta, Ingrediente ingrediente, double quantita) throws SQLException {
        Usa usa = new Usa(ricetta, ingrediente, quantita);
        usaDAO.save(usa);
    }

    public void rimuoviIngredienteDaRicetta( Ricetta ricetta, Ingrediente ingrediente, double quantita) throws SQLException {
        Usa usa = new Usa(ricetta, ingrediente, quantita);
        usaDAO.delete(usa);
    }
}
