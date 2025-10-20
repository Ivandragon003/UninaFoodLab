package service;

import dao.UsaDAO;
import dao.IngredienteDAO;
import exceptions.DataAccessException;
import exceptions.ValidationException;
import exceptions.ValidationUtils;
import model.Ingrediente;
import model.Ricetta;
import model.Usa;

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
			throws ValidationException, DataAccessException {
		validateParams(ricetta, ingrediente, quantita);

		Ingrediente keyEsistente = trovaIngredienteKey(ricetta.getIngredienti(), ingrediente);
		if (keyEsistente != null) {
			throw new ValidationException("Ingrediente già presente nella ricetta");
		}

		try {
			Ingrediente ingredienteConID = recuperaOCreaIngrediente(ingrediente);
			ricetta.getIngredienti().put(ingredienteConID, quantita);
			usaDAO.save(new Usa(ricetta, ingredienteConID, quantita));
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante l'aggiunta ingrediente-ricetta", e);
		}
	}

	public void aggiornaQuantitaIngrediente(Ricetta ricetta, Ingrediente ingrediente, double nuovaQuantita)
			throws ValidationException, DataAccessException {
		validateParams(ricetta, ingrediente, nuovaQuantita);

		Ingrediente key = trovaIngredienteKey(ricetta.getIngredienti(), ingrediente);
		if (key == null) {
			throw new ValidationException("Ingrediente non presente nella ricetta");
		}

		try {
			ricetta.getIngredienti().put(key, nuovaQuantita);
			usaDAO.updateQuantita(new Usa(ricetta, key, nuovaQuantita));
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante l'aggiornamento della quantità", e);
		}
	}

	public void rimuoviIngredienteDaRicetta(Ricetta ricetta, Ingrediente ingrediente)
			throws ValidationException, DataAccessException {

		if (ricetta == null || ingrediente == null) {
			throw new ValidationException("Parametri nulli");
		}

		Ingrediente key = trovaIngredienteKey(ricetta.getIngredienti(), ingrediente);
		if (key == null) {
			throw new ValidationException("Ingrediente non presente nella ricetta");
		}

		try {
			
			ricetta.getIngredienti().remove(key);

			
			usaDAO.deleteByRicettaIdAndIngredienteId(ricetta.getIdRicetta(), key.getIdIngrediente());

		} catch (SQLException e) {
			throw new DataAccessException("Errore durante la rimozione ingrediente-ricetta", e);
		}
	}
	// ==================== SUPPORTO DATABASE ====================

	private Ingrediente recuperaOCreaIngrediente(Ingrediente ingrediente) throws SQLException, DataAccessException {
		// cerca per nome (case-insensitive) nella tabella ingredienti
		List<Ingrediente> trovati = ingredienteDAO.getAll().stream()
				.filter(i -> i.getNome() != null && i.getNome().equalsIgnoreCase(ingrediente.getNome()))
				.collect(Collectors.toList());

		if (trovati.isEmpty()) {
			ingredienteDAO.save(ingrediente);
			// ricarichiamo la lista
			trovati = ingredienteDAO.getAll().stream()
					.filter(i -> i.getNome() != null && i.getNome().equalsIgnoreCase(ingrediente.getNome()))
					.collect(Collectors.toList());
		}

		if (trovati.isEmpty()) {
			throw new SQLException("Impossibile recuperare ingrediente: " + ingrediente.getNome());
		}

		return trovati.get(0);
	}

	private Ingrediente trovaIngredienteKey(Map<Ingrediente, Double> m, Ingrediente ingrediente) {
		if (m.containsKey(ingrediente))
			return ingrediente;
		return m.keySet().stream().filter(i -> i.getNome() != null && ingrediente.getNome() != null
				&& i.getNome().equalsIgnoreCase(ingrediente.getNome())).findFirst().orElse(null);
	}

	private void validateParams(Ricetta ricetta, Ingrediente ingrediente, double quantita) throws ValidationException {
		// non validiamo oggetti null in quanto il model li gestisce; validiamo solo la
		// quantità
		ValidationUtils.validateQuantita(quantita);
	}
}
