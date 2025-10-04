package service;

import dao.RicettaDAO;
import dao.UsaDAO;
import dao.IngredienteDAO;
import model.Ingrediente;
import model.Ricetta;
import model.Usa;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GestioneRicette {

    private final RicettaDAO ricettaDAO;
    private final UsaDAO usaDAO;
    private final IngredienteDAO ingredienteDAO;

    public GestioneRicette(RicettaDAO ricettaDAO, UsaDAO usaDAO, IngredienteDAO ingredienteDAO) {
        this.ricettaDAO = ricettaDAO;
        this.usaDAO = usaDAO;
        this.ingredienteDAO = ingredienteDAO;
    }

    public void creaRicetta(Ricetta r) throws SQLException {
        ricettaDAO.save(r);
        
        for (Map.Entry<Ingrediente, Double> entry : r.getIngredienti().entrySet()) {
            Ingrediente ingr = entry.getKey();
            
            Optional<Ingrediente> ingrDaDB = ingredienteDAO.findByNome(ingr.getNome());
            
            if (!ingrDaDB.isPresent()) {
                System.out.println("🔍 DEBUG: Ingrediente '" + ingr.getNome() + "' non trovato, lo salvo...");
                ingredienteDAO.save(ingr);
                ingrDaDB = ingredienteDAO.findByNome(ingr.getNome());
            }
            
            if (ingrDaDB.isPresent()) {
                Ingrediente ingredienteConID = ingrDaDB.get();
                System.out.println("✅ DEBUG: Uso ingrediente ID=" + ingredienteConID.getIdIngrediente() + ", Nome=" + ingredienteConID.getNome());
                Usa usa = new Usa(r, ingredienteConID, entry.getValue());
                usaDAO.save(usa);
            } else {
                throw new SQLException("Impossibile recuperare ingrediente '" + ingr.getNome() + "' dal database");
            }
        }
        
        System.out.println("✅ Ricetta '" + r.getNome() + "' salvata con successo!");
    }

    public void aggiornaRicetta(int id, Ricetta r) throws SQLException {
        ricettaDAO.update(id, r);
    }

    public void aggiungiIngrediente(Ricetta r, Ingrediente i, double quantita) throws SQLException {
        if (r.getIngredienti().containsKey(i)) {
            throw new IllegalArgumentException("Ingrediente già presente nella ricetta");
        }
        
        Optional<Ingrediente> ingrDaDB = ingredienteDAO.findByNome(i.getNome());
        
        if (!ingrDaDB.isPresent()) {
            ingredienteDAO.save(i);
            ingrDaDB = ingredienteDAO.findByNome(i.getNome());
        }
        
        if (ingrDaDB.isPresent()) {
            Ingrediente ingredienteConID = ingrDaDB.get();
            r.getIngredienti().put(ingredienteConID, quantita);
            Usa usa = new Usa(r, ingredienteConID, quantita);
            usaDAO.save(usa);
        }
    }

    public void aggiornaQuantitaIngrediente(Ricetta r, Ingrediente i, double quantita) throws SQLException {
        if (!r.getIngredienti().containsKey(i)) {
            throw new IllegalArgumentException("Ingrediente non presente nella ricetta");
        }
        r.getIngredienti().put(i, quantita);
        Usa usa = new Usa(r, i, quantita);
        usaDAO.updateQuantita(usa);
    }

    public void rimuoviIngrediente(Ricetta r, Ingrediente i) throws SQLException {
        if (r.getIngredienti().remove(i) != null) {
            Usa usa = new Usa(r, i, 0);
            usaDAO.delete(usa);
        } else {
            throw new IllegalArgumentException("Ingrediente non presente nella ricetta");
        }
    }

    public List<Ricetta> getAllRicette() throws SQLException {
        return ricettaDAO.getAll();
    }

    public List<Ingrediente> getAllIngredienti() throws SQLException {
        return ingredienteDAO.getAll();
    }

    public Ingrediente creaIngrediente(Ingrediente i) throws SQLException {
        ingredienteDAO.save(i);
        return i;
    }

    public void cancellaRicetta(int id) throws SQLException {
        usaDAO.deleteByRicetta(id);
        ricettaDAO.delete(id);
    }
}
