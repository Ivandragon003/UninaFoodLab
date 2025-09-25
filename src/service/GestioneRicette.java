package service;

import dao.RicettaDAO;
import dao.UsaDAO;
import model.Ingrediente;
import model.Ricetta;
import model.Usa;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class GestioneRicette {

    private final RicettaDAO ricettaDAO;
    private final UsaDAO usaDAO;

    public GestioneRicette(RicettaDAO ricettaDAO, UsaDAO usaDAO) {
        this.ricettaDAO = ricettaDAO;
        this.usaDAO = usaDAO;
    }

    // Creazione ricetta con i suoi ingredienti
    public void creaRicetta(Ricetta r) throws SQLException {
        ricettaDAO.save(r);
        // dopo aver salvato la ricetta, persistiamo anche gli ingredienti
        for (Map.Entry<Ingrediente, Double> entry : r.getIngredienti().entrySet()) {
            Usa usa = new Usa(r, entry.getKey(), entry.getValue());
            usaDAO.save(usa);
        }
    }

    // Aggiornamento ricetta (solo dati base, NON ingredienti)
    public void aggiornaRicetta(int id, Ricetta r) throws SQLException {
        ricettaDAO.update(id, r);
    }

    // Aggiungi ingrediente a ricetta
    public void aggiungiIngrediente(Ricetta r, Ingrediente i, double quantita) throws SQLException {
        if (r.getIngredienti().containsKey(i)) {
            throw new IllegalArgumentException("Ingrediente già presente nella ricetta");
        }
        r.getIngredienti().put(i, quantita);
        Usa usa = new Usa(r, i, quantita);
        usaDAO.save(usa);
    }

    // Aggiorna la quantità di un ingrediente già presente
    public void aggiornaQuantitaIngrediente(Ricetta r, Ingrediente i, double quantita) throws SQLException {
        if (!r.getIngredienti().containsKey(i)) {
            throw new IllegalArgumentException("Ingrediente non presente nella ricetta");
        }
        r.getIngredienti().put(i, quantita);
        Usa usa = new Usa(r, i, quantita);
        usaDAO.updateQuantita(usa);
    }

    // Rimuovi ingrediente da ricetta
    public void rimuoviIngrediente(Ricetta r, Ingrediente i) throws SQLException {
        if (r.getIngredienti().remove(i) != null) {
            Usa usa = new Usa(r, i, 0);
            usaDAO.delete(usa);
        } else {
            throw new IllegalArgumentException("Ingrediente non presente nella ricetta");
        }
    }

    // Lettura di tutte le ricette
    public List<Ricetta> getAllRicette() throws SQLException {
        return ricettaDAO.getAll();
    }

    // Cancellazione ricetta (con tutti i suoi ingredienti)
    public void cancellaRicetta(int id) throws SQLException {
        usaDAO.deleteByRicetta(id);
        ricettaDAO.delete(id);
    }
}
