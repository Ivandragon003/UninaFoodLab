package controller;

import exceptions.DataAccessException;
import exceptions.ValidationException;
import exceptions.ErrorMessages;
import exceptions.ValidationUtils;
import model.CorsoCucina;
import model.InPresenza;
import model.Ricetta;
import model.Sessione;
import model.Frequenza;
import service.GestioneCucina;
import service.GestioneRicette;
import service.GestioneSessioni;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;

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

	public void aggiungiSessione(Sessione sessione, List<Ricetta> ricette)
			throws ValidationException, DataAccessException {

		ValidationUtils.validateNotNull(sessione, "Sessione");
		ValidationUtils.validateNotNull(corso, "Corso");

		sessione.setCorsoCucina(corso);

		LocalDate dataSessione = sessione.getDataInizioSessione().toLocalDate();
		validaDataSessione(dataSessione);

		LocalDateTime now = LocalDateTime.now();
		if (sessione.getDataInizioSessione().isBefore(now)) {
			throw new ValidationException("La sessione non può iniziare nel passato.\n\n" + "📅 Data/ora attuale: "
					+ now.toLocalDate() + " " + now.toLocalTime() + "\n" + "📅 Data/ora selezionata: "
					+ sessione.getDataInizioSessione().toLocalDate() + " "
					+ sessione.getDataInizioSessione().toLocalTime());
		}

		if (!sessione.getDataFineSessione().isAfter(sessione.getDataInizioSessione())) {
			throw new ValidationException("L'ora di fine deve essere dopo l'ora di inizio.\n\n" + "🕐 Inizio: "
					+ sessione.getDataInizioSessione().toLocalTime() + "\n" + "🕐 Fine: "
					+ sessione.getDataFineSessione().toLocalTime());
		}

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

	public void eliminaSessione(Sessione sessione) throws ValidationException, DataAccessException {
		ValidationUtils.validateNotNull(sessione, "Sessione");

		if (corso.getSessioni().size() <= 1)
			throw new ValidationException(
					"Impossibile eliminare l'unica sessione del corso. Aggiungere almeno un'altra.");

		gestioneSessioniService.rimuoviSessione(sessione);
		corso.getSessioni().remove(sessione);
	}

	public boolean validaDataSessione(LocalDate dataSelezionata) throws ValidationException {
		if (dataSelezionata == null) {
			throw new ValidationException("Data non può essere null");
		}

		LocalDateTime fineCorso = corso.getDataFineCorso();
		if (fineCorso != null && LocalDateTime.now().isAfter(fineCorso)) {
			throw new ValidationException(
					"❌ Corso Terminato\n\n" + "Impossibile aggiungere sessioni a un corso già concluso.\n\n"
							+ "🏁 Data fine corso: " + fineCorso.toLocalDate());
		}

		LocalDate inizioCorso = corso.getDataInizioCorso().toLocalDate();
		LocalDate fineCorsoDate = fineCorso.toLocalDate();

		if (dataSelezionata.isBefore(inizioCorso)) {
			throw new ValidationException("La sessione non può essere prima dell'inizio del corso.\n\n"
					+ "📅 Data inizio corso: " + inizioCorso + "\n" + "📅 Data selezionata: " + dataSelezionata);
		}

		List<Sessione> sessioni = corso.getSessioni();

		if (sessioni == null || sessioni.isEmpty()) {
			return true;
		}

		boolean conflitto = sessioni.stream()
				.anyMatch(s -> s.getDataInizioSessione().toLocalDate().equals(dataSelezionata));

		if (conflitto) {
			throw new ValidationException("⚠️ Esiste già una sessione in data " + dataSelezionata + "\n\n"
					+ "Ogni data può ospitare una sola sessione.\n" + "Scegli una data diversa.");
		}

		Sessione ultimaSessione = sessioni.stream()
				.max(Comparator.comparing(s -> s.getDataFineSessione().toLocalDate())).orElse(null);

		if (ultimaSessione != null) {
			LocalDate dataFineUltima = ultimaSessione.getDataFineSessione().toLocalDate();

			if (dataSelezionata.isBefore(dataFineUltima) || dataSelezionata.equals(dataFineUltima)) {
				throw new ValidationException("❌ Data Non Valida\n\n"
						+ "La nuova sessione deve iniziare dopo la fine dell'ultima sessione.\n\n"
						+ "🏁 Fine ultima sessione: " + dataFineUltima + "\n" + "📅 Data selezionata: "
						+ dataSelezionata + "\n\n" + "💡 La prossima data valida è: " + dataFineUltima.plusDays(1));
			}

			Frequenza freq = corso.getFrequenzaCorso();

			if (freq == Frequenza.unica) {
				throw new ValidationException(
						"❌ Il corso ha frequenza 'Sessione Unica'\n\n" + "Non puoi aggiungere altre sessioni.\n"
								+ "Esiste già 1 sessione (terminata il " + dataFineUltima + ")");
			}

			if (freq == Frequenza.settimanale) {
				LocalDate primoLunediSuccessivo = dataFineUltima.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
				LocalDate fineSettimana = primoLunediSuccessivo.plusDays(6);

				if (dataSelezionata.isBefore(primoLunediSuccessivo) || dataSelezionata.isAfter(fineSettimana)) {
					throw new ValidationException(String.format("❌ Frequenza Non Rispettata (Settimanale)\n\n"
							+ "La nuova sessione deve appartenere a un giorno della settimana immediatamente successiva.\n\n"
							+ "🏁 Fine ultima sessione: %s\n" + "📅 Data selezionata: %s\n\n"
							+ "💡 La settimana valida è: %s - %s", dataFineUltima, dataSelezionata,
							primoLunediSuccessivo, fineSettimana));
				}

				return true;
			}

			long giorniDistanza = ChronoUnit.DAYS.between(dataFineUltima, dataSelezionata);

			int giorniMinimi = switch (freq) {
			case giornaliero -> 1;
			case ogniDueGiorni -> 2;
			case mensile -> 30;
			default -> Integer.MAX_VALUE;
			};

			if (giorniDistanza < giorniMinimi) {
				LocalDate dataMinimaConsentita = dataFineUltima.plusDays(giorniMinimi);
				throw new ValidationException(String.format("❌ Frequenza Non Rispettata\n\n"
						+ "Con frequenza '%s' la nuova sessione deve iniziare almeno %d giorni dopo la fine dell'ultima.\n\n"
						+ "🏁 Fine ultima sessione: %s\n" + "📅 Data selezionata: %s\n" + "📊 Distanza: %d giorni\n"
						+ "✅ Minimo richiesto: %d giorni\n\n" + "💡 La prossima data valida è: %s", freq.name(),
						giorniMinimi, dataFineUltima, dataSelezionata, giorniDistanza, giorniMinimi,
						dataMinimaConsentita));
			}
		}

		return true;
	}

}