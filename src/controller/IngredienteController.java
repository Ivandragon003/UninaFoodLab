package controller;

import dao.IngredienteDAO;
import exceptions.DataAccessException;
import exceptions.ValidationException;
import model.Ingrediente;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class IngredienteController {

	private final IngredienteDAO ingredienteDAO;

	public IngredienteController(IngredienteDAO ingredienteDAO) {
		this.ingredienteDAO = ingredienteDAO;
	}

	public int creaIngrediente(String nome, String tipo) throws ValidationException, DataAccessException {
		

		Ingrediente ingrediente = new Ingrediente(nome.trim(), tipo != null ? tipo.trim() : null);

		try {
			ingredienteDAO.save(ingrediente);

			List<Ingrediente> tutti = ingredienteDAO.getAll();
			return tutti.stream()
					.filter(i -> i.getNome() != null && i.getNome().equalsIgnoreCase(ingrediente.getNome()))
					.map(Ingrediente::getIdIngrediente)
					.findFirst()
					.orElseThrow(() -> new DataAccessException(
							"Ingrediente salvato ma non è stato possibile recuperarne l'ID"));
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante il salvataggio dell'ingrediente: " + e.getMessage(), e);
		}
	}

	public List<Ingrediente> getAllIngredienti() throws DataAccessException {
		try {
			return ingredienteDAO.getAll();
		} catch (SQLException e) {
			throw new DataAccessException("Errore nel recupero degli ingredienti: " + e.getMessage(), e);
		}
	}

	public List<Ingrediente> cercaIngredientiPerNome(String nome) throws DataAccessException {
		try {
			List<Ingrediente> tutti = ingredienteDAO.getAll();
			if (nome == null || nome.trim().isEmpty()) {
				return tutti;
			}
			String q = nome.trim().toLowerCase();
			return tutti.stream()
					.filter(i -> i.getNome() != null && i.getNome().toLowerCase().contains(q))
					.toList();
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante la ricerca per nome: " + e.getMessage(), e);
		}
	}

	public List<Ingrediente> cercaIngredientiPerTipo(String tipo) throws DataAccessException {
		try {
			List<Ingrediente> tutti = ingredienteDAO.getAll();
			if (tipo == null || tipo.trim().isEmpty()) {
				return tutti;
			}
			String q = tipo.trim().toLowerCase();
			return tutti.stream()
					.filter(i -> i.getTipo() != null && i.getTipo().toLowerCase().contains(q))
					.toList();
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante la ricerca per tipo: " + e.getMessage(), e);
		}
	}

	public Optional<Ingrediente> trovaIngredientePerId(int id) throws DataAccessException {
		try {
			return ingredienteDAO.findById(id);
		} catch (SQLException e) {
			throw new DataAccessException("Errore nel recupero dell'ingrediente con ID " + id + ": " + e.getMessage(),
					e);
		}
	}
}