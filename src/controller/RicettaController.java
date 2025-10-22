package controller;

import dao.*;
import exceptions.DataAccessException;
import model.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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

    public List<Ricetta> getRicetteDelChef() throws DataAccessException {
        if (chefLoggato == null) {
            throw new DataAccessException("Chef non autenticato");
        }
        try {
            List<Ricetta> tutteRicette = ricettaDAO.getAll();
            return tutteRicette.stream()
                    .filter(r -> r.getChef() != null && r.getChef().getCodFiscale().equals(chefLoggato.getCodFiscale()))
                    .toList();
        } catch (SQLException e) {
            throw new DataAccessException("Impossibile leggere i dati", e);
        }
    }

    public void creaRicetta(String nome, int tempoPreparazione, Map<Ingrediente, Double> ingredienti)
            throws DataAccessException {
        if (chefLoggato == null) {
            throw new DataAccessException("Chef non autenticato");
        }
        if (nome == null || nome.trim().isEmpty()) {
            throw new DataAccessException("Il nome della ricetta non può essere vuoto");
        }
        if (tempoPreparazione <= 0) {
            throw new DataAccessException("Il tempo di preparazione deve essere maggiore di zero");
        }
        if (ingredienti == null || ingredienti.isEmpty()) {
            throw new DataAccessException("La ricetta deve contenere almeno un ingrediente");
        }

        try {
            Ricetta ricetta = new Ricetta(nome, tempoPreparazione);
            ricetta.setChef(chefLoggato);
            ricettaDAO.save(ricetta);

            for (Map.Entry<Ingrediente, Double> entry : ingredienti.entrySet()) {
                Ingrediente ing = entry.getKey();
                Double quantita = entry.getValue();

                if (quantita <= 0) {
                    throw new DataAccessException("La quantità deve essere maggiore di zero");
                }

                Usa usa = new Usa(ricetta, ing, quantita);
                usaDAO.save(usa);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Impossibile salvare i dati", e);
        }
    }

    public void modificaRicetta(int idRicetta, String nuovoNome, int nuovoTempo,
            Map<Ingrediente, Double> nuoviIngredienti) throws DataAccessException {
        if (nuovoNome == null || nuovoNome.trim().isEmpty()) {
            throw new DataAccessException("Il nome della ricetta non può essere vuoto");
        }
        if (nuovoTempo <= 0) {
            throw new DataAccessException("Il tempo di preparazione deve essere maggiore di zero");
        }
        if (nuoviIngredienti == null || nuoviIngredienti.isEmpty()) {
            throw new DataAccessException("La ricetta deve contenere almeno un ingrediente");
        }

        try {
            Ricetta ricetta = ricettaDAO.findById(idRicetta)
                    .orElseThrow(() -> new DataAccessException("Ricetta non trovata"));

            ricetta.setNome(nuovoNome);
            ricetta.setTempoPreparazione(nuovoTempo);
            ricettaDAO.update(idRicetta, ricetta);

            usaDAO.deleteByRicetta(idRicetta);

            for (Map.Entry<Ingrediente, Double> entry : nuoviIngredienti.entrySet()) {
                if (entry.getValue() <= 0) {
                    throw new DataAccessException("La quantità deve essere maggiore di zero");
                }
                Usa usa = new Usa(ricetta, entry.getKey(), entry.getValue());
                usaDAO.save(usa);
            }
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

    public List<Ricetta> cercaPerNome(String nome) throws DataAccessException {
        try {
            return ricettaDAO.searchByNome(nome);
        } catch (SQLException e) {
            throw new DataAccessException("Impossibile leggere i dati", e);
        }
    }

    public Ricetta getRicettaCompleta(int idRicetta) throws DataAccessException {
        try {
            return ricettaDAO.findById(idRicetta)
                    .orElseThrow(() -> new DataAccessException("Ricetta non trovata"));
        } catch (SQLException e) {
            throw new DataAccessException("Impossibile leggere i dati", e);
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
}
