package service;

import dao.UsaDAO;
import dao.IngredienteDAO;
import model.Ingrediente;
import model.Ricetta;
import model.Usa;
import exceptions.ValidationException;
import exceptions.ValidationUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GestioneUsa {

    private final UsaDAO usaDAO;
    private final IngredienteDAO ingredienteDAO;

    public GestioneUsa(UsaDAO usaDAO, IngredienteDAO ingredienteDAO) {
        this.usaDAO = usaDAO;
        this.ingredienteDAO = ingredienteDAO;
    }

    // ==================== OPERAZIONI SULL'ASSOCIAZIONE ====================

    public void aggiungiIngredienteARicetta(Ricetta ricetta, Ingrediente ingrediente, double quantita)
            throws SQLException, ValidationException {
        validateParams(ricetta, ingrediente, quantita);

        Ingrediente keyEsistente = trovaIngredienteKey(ricetta.getIngredienti(), ingrediente);
        if (keyEsistente != null) {
            throw new ValidationException("Ingrediente già presente nella ricetta");
        }

        Ingrediente ingredienteConID = recuperaOCreaIngrediente(ingrediente);
        ricetta.getIngredienti().put(ingredienteConID, quantita);
        usaDAO.save(new Usa(ricetta, ingredienteConID, quantita));
    }

    public void aggiornaQuantitaIngrediente(Ricetta ricetta, Ingrediente ingrediente, double nuovaQuantita)
            throws SQLException, ValidationException {
        validateParams(ricetta, ingrediente, nuovaQuantita);

        Ingrediente key = trovaIngredienteKey(ricetta.getIngredienti(), ingrediente);
        if (key == null) {
            throw new ValidationException("Ingrediente non presente nella ricetta");
        }

        ricetta.getIngredienti().put(key, nuovaQuantita);
        usaDAO.updateQuantita(new Usa(ricetta, key, nuovaQuantita));
    }

    public void rimuoviIngredienteDaRicetta(Ricetta ricetta, Ingrediente ingrediente)
            throws SQLException, ValidationException {
        ValidationUtils.validateNotNull(ricetta, "Ricetta");
        ValidationUtils.validateNotNull(ingrediente, "Ingrediente");

        Ingrediente key = trovaIngredienteKey(ricetta.getIngredienti(), ingrediente);
        if (key == null) {
            throw new ValidationException("Ingrediente non presente nella ricetta");
        }

        ricetta.getIngredienti().remove(key);
        usaDAO.delete(new Usa(ricetta, key, 0));
    }

    // ==================== SUPPORTO DATABASE ====================

    private Ingrediente recuperaOCreaIngrediente(Ingrediente ingrediente) throws SQLException {
        List<Ingrediente> trovati = ingredienteDAO.getAll().stream()
                .filter(i -> i.getNome().equalsIgnoreCase(ingrediente.getNome()))
                .collect(Collectors.toList());

        if (trovati.isEmpty()) {
            ingredienteDAO.save(ingrediente);
            trovati = ingredienteDAO.getAll().stream()
                    .filter(i -> i.getNome().equalsIgnoreCase(ingrediente.getNome()))
                    .collect(Collectors.toList());
        }

        if (trovati.isEmpty()) {
            throw new SQLException("Impossibile recuperare ingrediente: " + ingrediente.getNome());
        }

        return trovati.get(0);
    }

    private Ingrediente trovaIngredienteKey(Map<Ingrediente, Double> m, Ingrediente ingrediente) {
        if (m.containsKey(ingrediente)) return ingrediente;
        return m.keySet().stream()
                .filter(i -> i.getNome() != null && ingrediente.getNome() != null
                        && i.getNome().equalsIgnoreCase(ingrediente.getNome()))
                .findFirst()
                .orElse(null);
    }

    private void validateParams(Ricetta ricetta, Ingrediente ingrediente, double quantita)
            throws ValidationException {
        ValidationUtils.validateNotNull(ricetta, "Ricetta");
        ValidationUtils.validateNotNull(ingrediente, "Ingrediente");
        ValidationUtils.validateQuantita(quantita);
    }
}
