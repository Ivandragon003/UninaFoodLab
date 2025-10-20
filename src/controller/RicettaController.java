package controller;

import model.Ricetta;
import model.Ingrediente;
import model.InPresenza;
import service.GestioneRicette;
import service.GestioneUsa;
import service.GestioneCucina;
import exceptions.ValidationException;
import exceptions.DataAccessException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RicettaController {

	private final GestioneRicette gestioneRicette;
	private final GestioneUsa gestioneUsa;
	private final GestioneCucina gestioneCucina;
	private final IngredienteController ingredienteController;
	private List<Ricetta> cachedRicette = null;

	public RicettaController(GestioneRicette gestioneRicette, GestioneUsa gestioneUsa, 
	                         GestioneCucina gestioneCucina, IngredienteController ingredienteController) {
		this.gestioneRicette = gestioneRicette;
		this.gestioneUsa = gestioneUsa;
		this.gestioneCucina = gestioneCucina;
		this.ingredienteController = ingredienteController;
	}

	

	public Ricetta creaRicetta(String nome, int tempoPreparazione, Map<Ingrediente, Double> ingredienti)
			throws ValidationException, DataAccessException {

		Ricetta ricetta = new Ricetta(nome, tempoPreparazione);
		gestioneRicette.creaRicetta(ricetta);

		if (ingredienti != null) {
			for (Map.Entry<Ingrediente, Double> entry : ingredienti.entrySet()) {
				Ingrediente ing = entry.getKey();
				double q = entry.getValue();
				gestioneUsa.aggiungiIngredienteARicetta(ricetta, ing, q);
			}
		}

		invalidaCache();
		return ricetta;
	}

	public void aggiornaRicetta(int idRicetta, String nuovoNome, int nuovoTempo,
			Map<Ingrediente, Double> nuoviIngredienti) throws ValidationException, DataAccessException {

		
		Ricetta vecchiaRicetta = gestioneRicette.getAllRicette().stream()
				.filter(r -> r.getIdRicetta() == idRicetta)
				.findFirst()
				.orElseThrow(() -> new ValidationException("Ricetta non trovata"));

		
		if (vecchiaRicetta.getIngredienti() != null) {
			
			List<Ingrediente> ingredientiDaRimuovere = List.copyOf(vecchiaRicetta.getIngredienti().keySet());
			for (Ingrediente ing : ingredientiDaRimuovere) {
				gestioneUsa.rimuoviIngredienteDaRicetta(vecchiaRicetta, ing);
			}
		}

		
		Ricetta ricetta = new Ricetta(nuovoNome, nuovoTempo);
		ricetta.setIdRicetta(idRicetta);
		gestioneRicette.aggiornaRicetta(idRicetta, ricetta);

		
		Map<Ingrediente, Double> mappaIngredentiNormalizzati = new HashMap<>();

		for (Map.Entry<Ingrediente, Double> entry : nuoviIngredienti.entrySet()) {
			Ingrediente ing = entry.getKey();
			Double quantita = entry.getValue();

			try {
				
				Optional<Ingrediente> ingredienteOpt = ingredienteController.trovaIngredientePerId(ing.getIdIngrediente());
				
				if (!ingredienteOpt.isPresent()) {
					throw new ValidationException("Ingrediente non trovato: " + ing.getNome() + " (ID: " + ing.getIdIngrediente() + ")");
				}
				
				Ingrediente ingredienteDB = ingredienteOpt.get();

				
				gestioneUsa.aggiungiIngredienteARicetta(ricetta, ingredienteDB, quantita);
				mappaIngredentiNormalizzati.put(ingredienteDB, quantita);
				
			} catch (ValidationException e) {
				throw e;
			} catch (Exception e) {
				throw new DataAccessException("Errore aggiunta ingrediente '" + ing.getNome() + "': " + e.getMessage(), e);
			}
		}

		ricetta.setIngredienti(mappaIngredentiNormalizzati);
		invalidaCache();
	}

	public void eliminaRicetta(int idRicetta) throws ValidationException, DataAccessException {
		gestioneRicette.cancellaRicetta(idRicetta);
		invalidaCache();
	}



	public List<Ricetta> getAllRicette() throws DataAccessException {
		if (cachedRicette == null) {
			cachedRicette = gestioneRicette.getAllRicette();
		}
		return List.copyOf(cachedRicette);
	}

	public Ricetta getRicettaPerId(int idRicetta) throws DataAccessException {
		return getAllRicette().stream()
				.filter(r -> r.getIdRicetta() == idRicetta)
				.findFirst()
				.orElse(null);
	}

	
	public List<Ricetta> filtraCombinato(String nome, Integer tempoMin, Integer tempoMax, 
			Integer ingredientiMin, Integer ingredientiMax) throws ValidationException, DataAccessException {
		return gestioneRicette.filtraCombinato(nome, tempoMin, tempoMax, ingredientiMin, ingredientiMax, getAllRicette());
	}

	public void invalidaCache() {
		cachedRicette = null;
	}

	public void ricaricaCache() throws DataAccessException {
		invalidaCache();
		getAllRicette();
	}

	public GestioneRicette getGestioneRicette() {
		return gestioneRicette;
	}
}