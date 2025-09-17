package service;

import dao.RicettaDAO;
import dao.UsaDAO;
import dao.CucinaDAO;

import model.*;

import java.sql.SQLException;
import java.util.Map;

public class GestioneRicette {

    private final RicettaDAO ricettaDAO;
    private final UsaDAO usaDAO;
    private final CucinaDAO cucinaDAO;

    public GestioneRicette(RicettaDAO ricettaDAO, UsaDAO usaDAO, CucinaDAO cucinaDAO) {
        this.ricettaDAO = ricettaDAO;
        this.usaDAO = usaDAO;
        this.cucinaDAO = cucinaDAO;
    }

    // Gestione ricette
    public void creaRicetta(Ricetta r) throws SQLException {
        ricettaDAO.save(r);
    }

    public void aggiornaRicetta(int id, Ricetta r) throws SQLException {
        ricettaDAO.update(id, r);
    }

    public void cancellaRicetta(int id) throws SQLException {
        usaDAO.deleteByRicetta(id);  // rimuove tutte le associazioni ingredienti
        cucinaDAO.deleteByRicetta(id); // rimuove tutte le associazioni sessioni
        ricettaDAO.delete(id);
    }

    // Gestione ingredienti
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

    public Map<Ingrediente, Double> getIngredienti(Ricetta r) {
        return r.getIngredienti();
    }

    // Gestione sessioni
    public void aggiungiSessioneARicetta(Ricetta r, Sessione s) throws SQLException {
        if (!r.getSessioni().contains(s)) {
            r.getSessioni().add(s);
            cucinaDAO.save(r.getIdRicetta(), s.getIdSessione());
        } else {
            throw new IllegalArgumentException("Sessione già associata a questa ricetta");
        }
    }

    public void rimuoviSessioneDaRicetta(Ricetta r, Sessione s) throws SQLException {
        if (r.getSessioni().remove(s)) {
            cucinaDAO.delete(r.getIdRicetta(), s.getIdSessione());
        } else {
            throw new IllegalArgumentException("Sessione non associata a questa ricetta");
        }
    }


}
