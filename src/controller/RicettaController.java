package controller;

import dao.CucinaDAO;
import dao.IngredienteDAO;
import dao.RicettaDAO;
import dao.UsaDAO;
import exceptions.DataAccessException;
import exceptions.ValidationException;
import model.Chef;
import model.Ingrediente;
import model.Ricetta;
import model.Usa;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class RicettaController {

	private final RicettaDAO ricettaDAO;
	private final IngredienteDAO ingredienteDAO;
	private final UsaDAO usaDAO;

	private final Chef chefLoggato;

	public RicettaController(RicettaDAO ricettaDAO, IngredienteDAO ingredienteDAO, UsaDAO usaDAO, CucinaDAO cucinaDAO,
			Chef chefLoggato) {
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

	public Ricetta getRicettaCompleta(int idRicetta) throws DataAccessException {
		try {
			return ricettaDAO.findById(idRicetta).orElseThrow(() -> new DataAccessException("Ricetta non trovata"));
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

	
	public Ricetta creaRicetta(String nome, int tempoPreparazione, Map<Ingrediente, Double> ingredienti)
			throws ValidationException, DataAccessException {
		if (chefLoggato == null)
			throw new DataAccessException("Chef non autenticato");
		if (ingredienti == null || ingredienti.isEmpty())
			throw new ValidationException("La ricetta deve contenere almeno un ingrediente");

		try {
			if (ricettaEsiste(nome)) {
				throw new ValidationException("Esiste già una ricetta con il nome: " + nome);
			}

			Ricetta ricetta = new Ricetta(nome, tempoPreparazione);
			ricettaDAO.save(ricetta);

			for (Map.Entry<Ingrediente, Double> entry : ingredienti.entrySet()) {
				Ingrediente ing = recuperaOCreaIngrediente(entry.getKey());
				Double quantita = entry.getValue();

				if (quantita == null || quantita <= 0 || Double.isNaN(quantita) || Double.isInfinite(quantita)) {
					throw new ValidationException("La quantità deve essere un numero positivo valido");
				}

				usaDAO.save(new Usa(ricetta, ing, quantita));
			}

			return ricetta; // ← restituisci la ricetta creata
		} catch (SQLException e) {
			throw new DataAccessException("Impossibile salvare i dati", e);
		}
	}

	
	public void modificaRicetta(int idRicetta, String nuovoNome, int nuovoTempo,
			Map<Ingrediente, Double> nuoviIngredienti) throws ValidationException, DataAccessException {
		if (nuoviIngredienti == null || nuoviIngredienti.isEmpty()) {
			throw new ValidationException("La ricetta deve contenere almeno un ingrediente");
		}

		try {
			Ricetta ricetta = ricettaDAO.findById(idRicetta)
					.orElseThrow(() -> new ValidationException("Ricetta non trovata"));

		
			List<Ricetta> omonime = ricettaDAO.getByNome(nuovoNome);
			for (Ricetta r : omonime) {
				if (r.getIdRicetta() != idRicetta) {
					throw new ValidationException("Esiste già un'altra ricetta con questo nome");
				}
			}

			ricetta.setNome(nuovoNome);
			ricetta.setTempoPreparazione(nuovoTempo);
			ricettaDAO.update(idRicetta, ricetta);

			
			usaDAO.deleteByRicetta(idRicetta);

			for (Map.Entry<Ingrediente, Double> entry : nuoviIngredienti.entrySet()) {
				Ingrediente ing = recuperaOCreaIngrediente(entry.getKey());
				Double q = entry.getValue();
				if (q == null || q <= 0 || Double.isNaN(q) || Double.isInfinite(q)) {
					throw new ValidationException("La quantità deve essere un numero positivo valido");
				}
				usaDAO.save(new Usa(ricetta, ing, q));
			}
		} catch (SQLException e) {
			throw new DataAccessException("Impossibile salvare i dati", e);
		}
	}

	
	public void eliminaRicetta(int idRicetta) throws ValidationException, DataAccessException {
		try {
			ricettaDAO.findById(idRicetta).orElseThrow(() -> new ValidationException("Ricetta non trovata"));

			usaDAO.deleteByRicetta(idRicetta);
			ricettaDAO.delete(idRicetta);
		} catch (SQLException e) {
			throw new DataAccessException("Impossibile eliminare i dati", e);
		}
	}

	
	public List<Ricetta> filtraCombinato(String nome, Integer tempoMin, Integer tempoMax, Integer ingredientiMin,
			Integer ingredientiMax, List<Ricetta> tutteRicette) throws ValidationException {

		checkRange(tempoMin, tempoMax, "Tempo preparazione");
		checkRange(ingredientiMin, ingredientiMax, "Numero ingredienti");

		return tutteRicette.stream().filter(r -> matchNome(r, nome))
				.filter(r -> matchRange(r.getTempoPreparazione(), tempoMin, tempoMax))
				.filter(r -> matchRange(r.getNumeroIngredienti(), ingredientiMin, ingredientiMax))
				.collect(Collectors.toList());
	}

	public List<Ingrediente> getTuttiIngredienti() throws DataAccessException {
		try {
			return ingredienteDAO.getAll();
		} catch (SQLException e) {
			throw new DataAccessException("Impossibile leggere i dati", e);
		}
	}

	public List<Ingrediente> cercaIngredientiPerNome(String nome) throws DataAccessException {
		try {
			if (nome == null || nome.trim().isEmpty())
				return ingredienteDAO.getAll();
			return ingredienteDAO.getAll().stream()
					.filter(i -> i.getNome() != null && i.getNome().toLowerCase().contains(nome.toLowerCase()))
					.collect(Collectors.toList());
		} catch (SQLException e) {
			throw new DataAccessException("Impossibile leggere i dati", e);
		}
	}

	
	public void aggiungiIngredienteARicetta(Ricetta ricetta, Ingrediente ingrediente, double quantita)
			throws ValidationException, DataAccessException {

		if (ricetta == null || ingrediente == null) {
			throw new ValidationException("Parametri nulli");
		}
		if (quantita <= 0 || Double.isNaN(quantita) || Double.isInfinite(quantita)) {
			throw new ValidationException("La quantità deve essere un numero positivo valido");
		}

		try {
			Ingrediente ingredienteConID = recuperaOCreaIngrediente(ingrediente);

			boolean giaPresente = ricetta.getIngredienti().keySet().stream().anyMatch(
					i -> i.getIdIngrediente() > 0 && i.getIdIngrediente() == ingredienteConID.getIdIngrediente());

			if (giaPresente) {
				throw new ValidationException("Ingrediente già presente nella ricetta");
			}

			ricetta.getIngredienti().put(ingredienteConID, quantita);
			usaDAO.save(new Usa(ricetta, ingredienteConID, quantita));

		} catch (SQLException e) {
			throw new DataAccessException("Errore durante il recupero o la creazione dell'ingrediente", e);
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

	
	private boolean ricettaEsiste(String nome) throws DataAccessException {
		try {
			return !ricettaDAO.getByNome(nome).isEmpty();
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante la verifica di esistenza della ricetta", e);
		}
	}

	private Ingrediente recuperaOCreaIngrediente(Ingrediente ingrediente) throws SQLException, DataAccessException {
		Optional<Ingrediente> trovato = ingredienteDAO.findByNome(ingrediente.getNome());
		if (trovato.isPresent())
			return trovato.get();
		ingredienteDAO.save(ingrediente);
		return ingredienteDAO.findByNome(ingrediente.getNome())
				.orElseThrow(() -> new SQLException("Impossibile recuperare ingrediente: " + ingrediente.getNome()));
	}

	private Ingrediente trovaIngredienteKey(Map<Ingrediente, Double> m, Ingrediente ingrediente) {
		return m.keySet().stream().filter(i -> i.equals(ingrediente)).findFirst().orElse(null);
	}

	private boolean matchNome(Ricetta r, String nome) {
		if (nome == null || nome.trim().isEmpty())
			return true;
		return r.getNome() != null && r.getNome().toLowerCase().contains(nome.toLowerCase().trim());
	}

	private boolean matchRange(int value, Integer min, Integer max) {
		if (min != null && value < min)
			return false;
		return max == null || value <= max;
	}

	private void checkRange(Integer min, Integer max, String label) throws ValidationException {
		if (min != null && min < 0)
			throw new ValidationException(label + " minimo non può essere negativo");
		if (max != null && max < 0)
			throw new ValidationException(label + " massimo non può essere negativo");
		if (min != null && max != null && min > max)
			throw new ValidationException(label + ": minimo non può superare il massimo");
	}
}
