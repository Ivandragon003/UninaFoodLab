package controller;

import exceptions.DataAccessException;
import exceptions.ValidationException;
import exceptions.ErrorMessages;
import exceptions.ValidationUtils;
import model.CorsoCucina;
import model.InPresenza;
import model.Ricetta;
import model.Sessione;
import service.GestioneCucina;
import service.GestioneRicette;
import service.GestioneSessioni;

import java.time.LocalDateTime;
import java.util.List;

public class GestioneSessioniController {

	private final CorsoCucina corso;
	private final GestioneSessioni gestioneSessioniService;
	private final GestioneCucina gestioneCucinaService;
	private final GestioneRicette gestioneRicetteService;

	public GestioneSessioniController(CorsoCucina corso, GestioneSessioni gestioneSessioniService,
			GestioneCucina gestioneCucinaService, GestioneRicette gestioneRicetteService) {
		this.corso = corso;
		this.gestioneSessioniService = gestioneSessioniService;
		this.gestioneCucinaService = gestioneCucinaService;
		this.gestioneRicetteService = gestioneRicetteService;
	}

	// --- SESSIONI ---
	public void aggiungiSessione(Sessione sessione, List<Ricetta> ricette)
			throws ValidationException, DataAccessException {

		ValidationUtils.validateNotNull(sessione, "Sessione");
		ValidationUtils.validateNotNull(corso, "Corso");

		LocalDateTime now = LocalDateTime.now();
		if (sessione.getDataInizioSessione().isBefore(now))
			throw new ValidationException(ErrorMessages.DATA_INIZIO_SESSIONE_PASSATA);

		if (!sessione.getDataFineSessione().isAfter(sessione.getDataInizioSessione()))
			throw new ValidationException(ErrorMessages.DATA_FINE_SESSIONE_PRECEDENTE);

		gestioneSessioniService.creaSessione(sessione);
		corso.getSessioni().add(sessione);

		if (sessione instanceof InPresenza ip && ricette != null) {
			for (Ricetta r : ricette) {

				if (r.getIdRicetta() == 0) {
					gestioneRicetteService.creaRicetta(r);
				}

				if (ip.getIdSessione() == 0) {
					gestioneSessioniService.creaSessione(ip);
				}

				gestioneCucinaService.aggiungiSessioneARicetta(r, ip);
			}
		}

	}

	public void aggiornaSessione(Sessione oldS, Sessione newS) throws ValidationException, DataAccessException {

		ValidationUtils.validateNotNull(oldS, "Sessione vecchia");
		ValidationUtils.validateNotNull(newS, "Sessione nuova");

		int idx = corso.getSessioni().indexOf(oldS);
		if (idx < 0)
			throw new ValidationException(ErrorMessages.SESSIONE_NON_TROVATA);

		LocalDateTime now = LocalDateTime.now();
		if (newS.getDataInizioSessione().isBefore(now))
			throw new ValidationException(ErrorMessages.DATA_INIZIO_SESSIONE_PASSATA);
		if (!newS.getDataFineSessione().isAfter(newS.getDataInizioSessione()))
			throw new ValidationException(ErrorMessages.DATA_FINE_SESSIONE_PRECEDENTE);

		gestioneSessioniService.creaSessione(newS);
		gestioneSessioniService.rimuoviSessione(oldS);
		corso.getSessioni().set(idx, newS);
	}

	public void eliminaSessione(Sessione sessione) throws ValidationException, DataAccessException {

		ValidationUtils.validateNotNull(sessione, "Sessione");

		if (corso.getSessioni().size() <= 1)
			throw new ValidationException(
					"Impossibile eliminare l'unica sessione del corso. Aggiungere almeno un'altra.");

		gestioneSessioniService.rimuoviSessione(sessione);
		corso.getSessioni().remove(sessione);
	}

}
