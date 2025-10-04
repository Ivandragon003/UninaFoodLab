package service;

import dao.UsaDAO;
import model.Ricetta;
import model.Ingrediente;
import model.Usa;

import java.sql.SQLException;


public class GestioneUsa {

    private final UsaDAO usaDAO;

    public GestioneUsa(UsaDAO usaDAO) {
        this.usaDAO = usaDAO;
    }

    public void aggiungiIngredienteARicetta(Ricetta r, Ingrediente i, double quantita) throws SQLException {
        if (!r.getIngredienti().containsKey(i)) {
            r.getIngredienti().put(i, quantita);
            usaDAO.save(new Usa(r, i, quantita));
        } else {
            throw new IllegalArgumentException("Ingrediente già presente nella ricetta");
        }
    }
    
    
    public void rimuoviIngredienteDaRicetta(Ricetta r, Ingrediente i) throws SQLException {
        if (r.getIngredienti().containsKey(i)) {
            r.getIngredienti().remove(i);
            usaDAO.delete(new Usa(r, i, 0));
        } else {
            throw new IllegalArgumentException("Ingrediente non presente nella ricetta");
        }
    }
}
