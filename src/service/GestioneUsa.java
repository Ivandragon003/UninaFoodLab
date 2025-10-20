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

    try {
        // 1️⃣ Recupera l'ingrediente già presente in DB (con id)
        Ingrediente ingredienteConID = recuperaOCreaIngrediente(ingrediente);

        // 2️⃣ CONTROLLO CORRETTO: confronta solo gli ID, ignorando equals()
        boolean giaPresente = ricetta.getIngredienti().keySet().stream()
            .anyMatch(i -> i.getIdIngrediente() > 0 && 
                          ingredienteConID.getIdIngrediente() > 0 &&
                          i.getIdIngrediente() == ingredienteConID.getIdIngrediente());

        if (giaPresente) {
            throw new ValidationException("Ingrediente già presente nella ricetta");
        }

        // 3️⃣ Associa ingrediente e quantità alla ricetta
        ricetta.getIngredienti().put(ingredienteConID, quantita);

        // 4️⃣ Salva relazione nel DB
        usaDAO.save(new Usa(ricetta, ingredienteConID, quantita));

    } catch (SQLException e) {
        throw new DataAccessException("Errore durante il recupero o la creazione dell'ingrediente", e);
    }
}

// ==================== METODO DI DEBUG ====================
// Aggiungi questo metodo per il debug
	public void debugIngredienti(Ricetta ricetta, Ingrediente nuovoIng) {
		System.out.println("=== DEBUG INGREDIENTI ===");
		System.out.println("Nuovo ingrediente: ID=" + nuovoIng.getIdIngrediente() + ", Nome=" + nuovoIng.getNome());
		System.out.println("Ingredienti nella ricetta:");
		ricetta.getIngredienti().keySet().forEach(i -> System.out
				.println("  - ID=" + i.getIdIngrediente() + ", Nome=" + i.getNome() + ", HashCode=" + i.hashCode()));
		System.out.println("=======================");
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
		return m.keySet().stream().filter(i -> i.equals(ingrediente)) // usa equals corretto
				.findFirst().orElse(null);
	}

	private void validateParams(Ricetta ricetta, Ingrediente ingrediente, double quantita) throws ValidationException {

		ValidationUtils.validateQuantita(quantita);
	}
}
