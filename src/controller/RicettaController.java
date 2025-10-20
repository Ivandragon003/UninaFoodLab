package controller;

import model.Ricetta;
import model.Ingrediente;
import model.InPresenza;
import service.GestioneRicette;
import service.GestioneUsa;
import service.GestioneCucina;
import exceptions.ValidationException;
import exceptions.DataAccessException;

import java.util.List;
import java.util.Map;

public class RicettaController {

	private final GestioneRicette gestioneRicette;
	private final GestioneUsa gestioneUsa;
	private final GestioneCucina gestioneCucina;
	private List<Ricetta> cachedRicette = null;

	public RicettaController(GestioneRicette gestioneRicette, GestioneUsa gestioneUsa, GestioneCucina gestioneCucina) {
		this.gestioneRicette = gestioneRicette;
		this.gestioneUsa = gestioneUsa;
		this.gestioneCucina = gestioneCucina;
	}

	// ==================== CRUD ====================

	public Ricetta creaRicetta(String nome, int tempoPreparazione, Map<Ingrediente, Double> ingredienti)
			throws ValidationException, DataAccessException {

		// 1️⃣ Crea il model Ricetta
		Ricetta ricetta = new Ricetta(nome, tempoPreparazione);

		// 2️⃣ Salva la ricetta tramite service, ottieni l'ID generato
		gestioneRicette.creaRicetta(ricetta); // qui ricetta avrà idRicetta valorizzato

		// 3️⃣ Aggiungi ingredienti usando il service GestioneUsa
		if (ingredienti != null) {
			for (Map.Entry<Ingrediente, Double> entry : ingredienti.entrySet()) {
				Ingrediente ing = entry.getKey();
				double q = entry.getValue();
				gestioneUsa.aggiungiIngredienteARicetta(ricetta, ing, q);
			}
		}

		// 4️⃣ Aggiorna cache locale
		invalidaCache();

		return ricetta;
	}

	public void aggiornaRicetta(int idRicetta, String nuovoNome, int nuovoTempo,
			Map<Ingrediente, Double> nuoviIngredienti) throws ValidationException, DataAccessException {

		// 1. Prendi la ricetta vecchia
		Ricetta vecchiaRicetta = gestioneRicette.getAllRicette().stream().filter(r -> r.getIdRicetta() == idRicetta)
				.findFirst().orElseThrow(() -> new ValidationException("Ricetta non trovata"));

		// 2. Rimuovi TUTTI i vecchi ingredienti dalla tabella Usa
		if (vecchiaRicetta != null && vecchiaRicetta.getIngredienti() != null) {
			for (Ingrediente ing : vecchiaRicetta.getIngredienti().keySet()) {
				gestioneUsa.rimuoviIngredienteDaRicetta(vecchiaRicetta, ing);
			}
		}

		// 3. Aggiorna info base ricetta
		Ricetta ricetta = new Ricetta(nuovoNome, nuovoTempo);
		ricetta.setIdRicetta(idRicetta);
		ricetta.setIngredienti(nuoviIngredienti);
		gestioneRicette.aggiornaRicetta(idRicetta, ricetta);

		// 4. Inserisci i NUOVI ingredienti nella tabella Usa
		for (Map.Entry<Ingrediente, Double> entry : nuoviIngredienti.entrySet()) {
			gestioneUsa.aggiungiIngredienteARicetta(ricetta, entry.getKey(), entry.getValue());
		}

		invalidaCache();
	}

	public void eliminaRicetta(int idRicetta) throws ValidationException, DataAccessException {
		gestioneRicette.cancellaRicetta(idRicetta);
		invalidaCache();
	}

	// ==================== QUERY ====================

	public List<Ricetta> getAllRicette() throws DataAccessException {
		if (cachedRicette == null) {
			cachedRicette = gestioneRicette.getAllRicette();
		}
		return List.copyOf(cachedRicette);
	}

	public Ricetta getRicettaPerId(int idRicetta) throws DataAccessException {
		return getAllRicette().stream().filter(r -> r.getIdRicetta() == idRicetta).findFirst().orElse(null);
	}

	public List<Ricetta> cercaPerNome(String nome) throws ValidationException, DataAccessException {
		return gestioneRicette.cercaPerNome(nome, getAllRicette());
	}

	public List<Ricetta> filtraCombinato(String nome, Integer tempoMin, Integer tempoMax, Integer ingredientiMin,
			Integer ingredientiMax) throws ValidationException, DataAccessException {

		return gestioneRicette.filtraCombinato(nome, tempoMin, tempoMax, ingredientiMin, ingredientiMax,
				getAllRicette());
	}

	// ==================== OPERAZIONI SUGLI INGREDIENTI (DELEGA A GestioneUsa)
	// ====================

	public void aggiungiIngrediente(Ricetta ricetta, Ingrediente ingrediente, double quantita)
			throws ValidationException, DataAccessException {

		if (ricetta == null || ricetta.getIdRicetta() == 0) {
			throw new ValidationException("La ricetta deve essere salvata prima di aggiungere ingredienti.");
		}

		gestioneUsa.aggiungiIngredienteARicetta(ricetta, ingrediente, quantita);

		invalidaCache();
	}

	public void aggiornaQuantitaIngrediente(Ricetta ricetta, Ingrediente ingrediente, double nuovaQuantita)
			throws ValidationException, DataAccessException {

		gestioneUsa.aggiornaQuantitaIngrediente(ricetta, ingrediente, nuovaQuantita);
		invalidaCache();
	}

	public void rimuoviIngrediente(Ricetta ricetta, Ingrediente ingrediente)
			throws ValidationException, DataAccessException {

		gestioneUsa.rimuoviIngredienteDaRicetta(ricetta, ingrediente);
		invalidaCache();
	}

	// ==================== ASSOCIAZIONI SESSIONI (DELEGA A GestioneCucina)
	// ====================

	public void associaRicettaASessione(Ricetta ricetta, InPresenza sessione)
			throws ValidationException, DataAccessException {

		gestioneCucina.aggiungiSessioneARicetta(ricetta, sessione);
	}

	public void disassociaRicettaDaSessione(Ricetta ricetta, InPresenza sessione)
			throws ValidationException, DataAccessException {

		gestioneCucina.rimuoviSessioneDaRicetta(ricetta, sessione);
	}

	/**
	 * Restituisce le ricette non ancora associate alla sessione (calcolo locale per
	 * evitare dipendenze service mancanti)
	 */
	public List<Ricetta> getRicetteNonAssociate(InPresenza sessione) throws DataAccessException {
		return getAllRicette().stream().filter(r -> r.getSessioni() == null || !r.getSessioni().contains(sessione))
				.toList();
	}

	// CACHE
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
