package controller;

import dao.RicettaDAO;
import dao.IngredienteDAO;
import dao.UsaDAO;
import exceptions.DataAccessException;
import model.Chef;
import model.Ricetta;
import model.Ingrediente;
import model.Usa;

import java.sql.SQLException;
import java.util.List;

public class RicettaController {

    private final RicettaDAO ricettaDAO;
    private final IngredienteDAO ingredienteDAO;
    private final UsaDAO usaDAO;
    private final Chef chefLoggato;

    public RicettaController(RicettaDAO ricettaDAO, IngredienteDAO ingredienteDAO, UsaDAO usaDAO, Chef chefLoggato) {
        this.ricettaDAO = ricettaDAO;
        this.ingredienteDAO = ingredienteDAO;
        this.usaDAO = usaDAO;
        this.chefLoggato = chefLoggato;
    }

    public Chef getChefLoggato() {
        return chefLoggato;
    }

    public List<Ricetta> visualizzaRicette() throws DataAccessException {
        try {
            return ricettaDAO.getAll();
        } catch (SQLException e) {
            throw new DataAccessException("Impossibile leggere i dati", e);
        }
    }

    public Ricetta getRicettaById(int idRicetta) throws DataAccessException {
        try {
            return ricettaDAO.findById(idRicetta)
                    .orElseThrow(() -> new DataAccessException("Ricetta non trovata"));
        } catch (SQLException e) {
            throw new DataAccessException("Impossibile leggere i dati", e);
        }
    }

    public List<Ricetta> cercaPerNome(String nome) throws DataAccessException {
        try {
            return ricettaDAO.searchByNome(nome);
        } catch (SQLException e) {
            throw new DataAccessException("Impossibile leggere i dati", e);
        }
    }

    public void creaRicetta(Ricetta ricetta) throws DataAccessException {
        if (chefLoggato == null) {
            throw new DataAccessException("Chef non autenticato");
        }
        if (ricetta == null) {
            throw new DataAccessException("La ricetta non può essere nulla");
        }
        if (ricetta.getNome() == null || ricetta.getNome().trim().isEmpty()) {
            throw new DataAccessException("Il nome della ricetta non può essere vuoto");
        }
        if (ricetta.getTempoPreparazione() <= 0) {
            throw new DataAccessException("Il tempo di preparazione deve essere maggiore di zero");
        }

        try {
            ricettaDAO.save(ricetta);
        } catch (SQLException e) {
            throw new DataAccessException("Impossibile salvare i dati", e);
        }
    }

    public void modificaRicetta(int idRicetta, Ricetta ricettaAggiornata) throws DataAccessException {
        if (ricettaAggiornata == null) {
            throw new DataAccessException("La ricetta non può essere nulla");
        }
        if (ricettaAggiornata.getNome() == null || ricettaAggiornata.getNome().trim().isEmpty()) {
            throw new DataAccessException("Il nome della ricetta non può essere vuoto");
        }
        if (ricettaAggiornata.getTempoPreparazione() <= 0) {
            throw new DataAccessException("Il tempo di preparazione deve essere maggiore di zero");
        }

        try {
            ricettaDAO.update(idRicetta, ricettaAggiornata);
        } catch (SQLException e) {
            throw new DataAccessException("Impossibile salvare i dati", e);
        }
    }

    public void eliminaRicetta(int idRicetta) throws DataAccessException {
        try {
            usaDAO.deleteByRicetta(idRicetta);
            ricettaDAO.delete(idRicetta);
        } catch (SQLException e) {
            throw new DataAccessException("Impossibile eliminare i dati", e);
        }
    }

    public List<Ingrediente> getTuttiIngredienti() throws DataAccessException {
        try {
            return ingredienteDAO.getAll();
        } catch (SQLException e) {
            throw new DataAccessException("Impossibile leggere i dati", e);
        }
    }

    public Ingrediente getIngredienteById(int id) throws DataAccessException {
        try {
            return ingredienteDAO.findById(id)
                    .orElseThrow(() -> new DataAccessException("Ingrediente non trovato"));
        } catch (SQLException e) {
            throw new DataAccessException("Impossibile leggere i dati", e);
        }
    }

    public void aggiungiIngredienteARicetta(int idRicetta, int idIngrediente, double quantita) 
            throws DataAccessException {
        if (quantita <= 0) {
            throw new DataAccessException("La quantità deve essere maggiore di zero");
        }

        try {
            Ricetta ricetta = ricettaDAO.findById(idRicetta)
                    .orElseThrow(() -> new DataAccessException("Ricetta non trovata"));
            Ingrediente ingrediente = ingredienteDAO.findById(idIngrediente)
                    .orElseThrow(() -> new DataAccessException("Ingrediente non trovato"));

            Usa usa = new Usa(ricetta, ingrediente, quantita);
            usaDAO.save(usa);
        } catch (SQLException e) {
            throw new DataAccessException("Impossibile salvare i dati", e);
        }
    }

    public void rimuoviIngredienteDaRicetta(int idRicetta, int idIngrediente) throws DataAccessException {
        try {
            usaDAO.deleteByRicettaIdAndIngredienteId(idRicetta, idIngrediente);
        } catch (SQLException e) {
            throw new DataAccessException("Impossibile eliminare i dati", e);
        }
    }

    public List<Usa> getIngredientiDiRicetta(String nomeRicetta) throws DataAccessException {
        try {
            return usaDAO.getByRicetta(nomeRicetta);
        } catch (SQLException e) {
            throw new DataAccessException("Impossibile leggere i dati", e);
        }
    }
}
