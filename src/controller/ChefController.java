package controller;

import model.Chef;
import model.CorsoCucina;
import model.Frequenza;
import model.Sessione;
import service.GestioneChef;
import exceptions.ValidationException;
import exceptions.DataAccessException;
import exceptions.ErrorMessages;
import exceptions.ValidationUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ChefController {

	private final GestioneChef gestioneChef;
	private GestioneCorsoController gestioneCorsoController;

	public ChefController(GestioneChef gestioneChef) {
		this.gestioneChef = gestioneChef;
	}

	public Chef login(String username, String password) throws ValidationException, DataAccessException {
		return gestioneChef.login(username, password);
	}

	public Chef registraChef(String codFiscale, String nome, String cognome, String email, LocalDate dataNascita,
			boolean disponibilita, String username, String password) throws ValidationException, DataAccessException {
		return gestioneChef.creaChef(codFiscale, nome, cognome, email, dataNascita, disponibilita, username, password);
	}

	public void eliminaAccount(Chef chef) throws ValidationException, DataAccessException {
		if (chef == null) {
			throw new ValidationException("Chef non valido");
		}
		gestioneChef.eliminaChef(chef.getUsername());
	}

	public void setGestioneCorsoController(GestioneCorsoController gestioneCorsoController) {
		this.gestioneCorsoController = gestioneCorsoController;
	}

	public List<Chef> getAllChef() throws DataAccessException {
		return gestioneChef.getAll();
	}

	public void saveCorsoFromForm(String nome, String prezzoText, String argomento, String postiText,
			Frequenza frequenza, LocalDate dataInizio, Integer startHour, Integer startMinute, LocalDate dataFine,
			Integer endHour, Integer endMinute, List<Chef> chefSelezionati, List<Sessione> sessioni)
			throws ValidationException, DataAccessException {

		if (gestioneCorsoController == null)
			throw new IllegalStateException("GestioneCorsoController non impostato: impossibile salvare corso");

		double prezzo = ValidationUtils.parseDoubleSafe(prezzoText);
		int posti = ValidationUtils.parseIntegerSafe(postiText);

		if (prezzo <= 0)
			throw new ValidationException(ErrorMessages.PREZZO_NON_VALIDO);
		if (posti <= 0)
			throw new ValidationException(ErrorMessages.NUMERO_POSTI_NON_VALIDO);
		if (dataInizio == null || dataFine == null)
			throw new ValidationException(ErrorMessages.DATE_CORSO_MANCANTI);
		if (frequenza == null)
			throw new ValidationException("Selezionare una frequenza");

		LocalDateTime inizio = LocalDateTime.of(dataInizio,
				LocalTime.of(startHour != null ? startHour : 9, startMinute != null ? startMinute : 0));
		LocalDateTime fine = LocalDateTime.of(dataFine,
				LocalTime.of(endHour != null ? endHour : 17, endMinute != null ? endMinute : 0));

		if (!inizio.isBefore(fine))
			throw new ValidationException(ErrorMessages.DATA_FINE_PRECEDENTE);

		if (chefSelezionati == null || chefSelezionati.isEmpty())
			throw new ValidationException("Selezionare almeno uno chef per il corso");

		if (sessioni == null || sessioni.isEmpty())
			throw new ValidationException("Aggiungere almeno una sessione al corso");

		CorsoCucina corso = new CorsoCucina(nome.trim(), prezzo, argomento == null ? "" : argomento.trim(), frequenza,
				posti);
		corso.setDataInizioCorso(inizio);
		corso.setDataFineCorso(fine);
		corso.setNumeroSessioni(sessioni.size());
		corso.setChef(List.copyOf(chefSelezionati));
		corso.setSessioni(List.copyOf(sessioni));

		gestioneCorsoController.creaCorso(corso);
	}

}
