package service;

import dao.IngredienteDAO;
import exceptions.DataAccessException;
import exceptions.ValidationException;
import exceptions.ValidationUtils;
import model.Ingrediente;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class GestioneIngrediente {

	private final IngredienteDAO ingredienteDAO;

	public GestioneIngrediente(IngredienteDAO ingredienteDAO) {
		this.ingredienteDAO = ingredienteDAO;
	}

	public int salvaIngrediente(Ingrediente ingrediente) throws ValidationException, DataAccessException {
		ValidationUtils.validateNomeIngrediente(ingrediente.getNome());

		try {
			ingredienteDAO.save(ingrediente);

			// tentativo di recuperare l'id: cerchiamo un match per nome (case-insensitive)
			List<Ingrediente> tutti = ingredienteDAO.getAll();
			return tutti.stream()
					.filter(i -> i.getNome() != null && i.getNome().equalsIgnoreCase(ingrediente.getNome()))
					.map(Ingrediente::getIdIngrediente).findFirst().orElseThrow(() -> new DataAccessException(
							"Ingrediente salvato ma non è stato possibile recuperarne l'id"));
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante il salvataggio dell'ingrediente", e);
		}
	}

	public List<Ingrediente> getAllIngredienti() throws DataAccessException {
		try {
			return ingredienteDAO.getAll();
		} catch (SQLException e) {
			throw new DataAccessException("Errore nel recupero degli ingredienti", e);
		}
	}

	public Optional<Ingrediente> trovaIngredientePerId(int id) throws DataAccessException {
		try {
			return ingredienteDAO.findById(id);
		} catch (SQLException e) {
			throw new DataAccessException("Errore nel recupero dell'ingrediente", e);
		}
	}

	public List<Ingrediente> cercaPerNome(String nome) throws DataAccessException {
		try {
			List<Ingrediente> tutti = ingredienteDAO.getAll();
			if (nome == null || nome.trim().isEmpty())
				return tutti;
			String q = nome.trim().toLowerCase();
			return tutti.stream().filter(i -> i.getNome() != null && i.getNome().toLowerCase().contains(q)).toList();
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante la ricerca per nome", e);
		}
	}

	public List<Ingrediente> cercaPerTipo(String tipo) throws DataAccessException {
		try {
			List<Ingrediente> tutti = ingredienteDAO.getAll();
			if (tipo == null || tipo.trim().isEmpty())
				return tutti;
			String q = tipo.trim().toLowerCase();
			return tutti.stream().filter(i -> i.getTipo() != null && i.getTipo().toLowerCase().contains(q)).toList();
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante la ricerca per tipo", e);
		}
	}

}
