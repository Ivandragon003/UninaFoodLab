package controller;

import dao.CucinaDAO;
import dao.InPresenzaDAO;
import dao.OnlineDAO;
import dao.RicettaDAO;
import exceptions.DataAccessException;
import exceptions.ValidationException;
import exceptions.ValidationUtils;
import model.CorsoCucina;
import model.Frequenza;
import model.InPresenza;
import model.Online;
import model.Ricetta;
import model.Sessione;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestioneSessioniController {

	private final CorsoCucina corso;
	private final InPresenzaDAO inPresenzaDAO;
	private final OnlineDAO onlineDAO;
	private final CucinaDAO cucinaDAO;
	private final RicettaDAO ricettaDAO;

	public GestioneSessioniController(CorsoCucina corso, InPresenzaDAO inPresenzaDAO, OnlineDAO onlineDAO,
			CucinaDAO cucinaDAO, RicettaDAO ricettaDAO) {
		this.corso = corso;
		this.inPresenzaDAO = inPresenzaDAO;
		this.onlineDAO = onlineDAO;
		this.cucinaDAO = cucinaDAO;
		this.ricettaDAO = ricettaDAO;
	}

	public void aggiungiSessione(Sessione sessione, List<Ricetta> ricette)
			throws ValidationException, DataAccessException {

		ValidationUtils.validateNotNull(sessione, "La sessione non può essere nulla");
		ValidationUtils.validateNotNull(corso, "Il corso non può essere nullo");
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
			throw new ValidationException("L'ora di fine deve essere successiva all'ora di inizio.\n\n" + "🕐 Inizio: "
					+ sessione.getDataInizioSessione().toLocalTime() + "\n" + "🕐 Fine: "
					+ sessione.getDataFineSessione().toLocalTime());
		}

		try {
			if (sessione instanceof InPresenza ip) {
				inPresenzaDAO.save(ip);
			} else if (sessione instanceof Online o) {
				onlineDAO.save(o);
			} else {
				throw new ValidationException(
						"Tipo di sessione non valido. È possibile creare solo sessioni in presenza o online.");
			}

			corso.getSessioni().add(sessione);

			if (sessione instanceof InPresenza ip && ricette != null) {
				for (Ricetta r : ricette) {
					if (r.getIdRicetta() == 0) {
						ricettaDAO.save(r);
					}
					cucinaDAO.save(r.getIdRicetta(), ip.getIdSessione());
					r.getSessioni().add(ip);
				}
			}

		} catch (SQLException e) {
			throw new DataAccessException("Errore durante la creazione della sessione: " + e.getMessage(), e);
		}
	}

	public void eliminaSessione(Sessione sessione) throws ValidationException, DataAccessException {

		ValidationUtils.validateNotNull(sessione, "La sessione non può essere nulla");

		if (corso.getSessioni().size() <= 1) {
			throw new ValidationException(
					"Impossibile eliminare l'unica sessione del corso. Aggiungere almeno un'altra sessione prima di procedere.");
		}

		try {
			if (sessione instanceof InPresenza ip) {
				inPresenzaDAO.delete(ip.getIdSessione());
			} else if (sessione instanceof Online o) {
				onlineDAO.delete(o.getIdSessione());
			} else {
				throw new ValidationException("Tipo di sessione non riconosciuto. Eliminazione annullata.");
			}

			corso.getSessioni().remove(sessione);

		} catch (SQLException e) {
			throw new DataAccessException("Errore durante la rimozione della sessione: " + e.getMessage(), e);
		}
	}


	public boolean validaDataSessione(LocalDate dataSelezionata) throws ValidationException {

		if (dataSelezionata == null) {
			throw new ValidationException("La data selezionata non può essere nulla.");
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
			throw new ValidationException("La sessione non può essere prima della data di inizio corso.\n\n"
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

			if (!dataSelezionata.isAfter(dataFineUltima)) {
				throw new ValidationException("❌ Data non valida\n\n"
						+ "La nuova sessione deve iniziare dopo la fine dell'ultima sessione.\n\n"
						+ "🏁 Fine ultima sessione: " + dataFineUltima + "\n" + "📅 Data selezionata: "
						+ dataSelezionata);
			}

			Frequenza freq = corso.getFrequenzaCorso();

			if (freq == Frequenza.unica) {
				throw new ValidationException(
						"❌ Il corso ha frequenza 'Sessione Unica'\n\n" + "Non puoi aggiungere altre sessioni.\n"
								+ "Esiste già 1 sessione terminata il " + dataFineUltima);
			}

			if (freq == Frequenza.settimanale) {
				LocalDate primoLunediSuccessivo = dataFineUltima.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
				LocalDate fineSettimana = primoLunediSuccessivo.plusDays(6);

				if (dataSelezionata.isBefore(primoLunediSuccessivo) || dataSelezionata.isAfter(fineSettimana)) {
					throw new ValidationException(String.format(
							"❌ Frequenza non rispettata (settimanale)\n\n"
									+ "La nuova sessione deve appartenere alla settimana immediatamente successiva.\n\n"
									+ "🏁 Fine ultima sessione: %s\n" + "📅 Data selezionata: %s\n\n"
									+ "💡 La settimana valida è: %s - %s",
							dataFineUltima, dataSelezionata, primoLunediSuccessivo, fineSettimana));
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
				throw new ValidationException(String.format("❌ Frequenza non rispettata\n\n"
						+ "Con frequenza '%s' la nuova sessione deve iniziare almeno %d giorni dopo la fine dell'ultima.\n\n"
						+ "🏁 Fine ultima sessione: %s\n" + "📅 Data selezionata: %s\n" + "📊 Distanza: %d giorni\n"
						+ "✅ Minimo richiesto: %d giorni\n\n" + "💡 La prossima data valida è: %s", freq.name(),
						giorniMinimi, dataFineUltima, dataSelezionata, giorniDistanza, giorniMinimi,
						dataMinimaConsentita));
			}
		}

		return true;
	}

	public void creaSessione(Sessione sessione) throws ValidationException, DataAccessException {
		ValidationUtils.validateNotNull(sessione, "Sessione");

		validaOrari(sessione.getDataInizioSessione(), sessione.getDataFineSessione());

		try {
			if (sessione instanceof InPresenza ip) {
				validaSessioneInPresenza(ip);
				inPresenzaDAO.save(ip);
			} else if (sessione instanceof Online o) {
				onlineDAO.save(o);
			} else {
				throw new ValidationException("Tipo di sessione non riconosciuto");
			}
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante la creazione della sessione", e);
		}
	}

	public void rimuoviSessione(Sessione sessione) throws ValidationException, DataAccessException {
		ValidationUtils.validateNotNull(sessione, "Sessione");

		try {
			if (sessione instanceof InPresenza ip) {
				inPresenzaDAO.delete(ip.getIdSessione());
			} else if (sessione instanceof Online o) {
				onlineDAO.delete(o.getIdSessione());
			} else {
				throw new ValidationException("Tipo di sessione non riconosciuto");
			}
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante la rimozione della sessione", e);
		}
	}

	public Map<Integer, Integer> getNumeroRicettePerSessioni(List<Integer> idSessioni) throws DataAccessException {
		if (idSessioni == null || idSessioni.isEmpty())
			return new HashMap<>();
		try {
			return cucinaDAO.getNumeroRicettePerSessioni(idSessioni);
		} catch (SQLException e) {
			throw new DataAccessException("Errore nel recupero del numero di ricette per le sessioni", e);
		}
	}

	private void validaOrari(LocalDateTime inizio, LocalDateTime fine) throws ValidationException {
		ValidationUtils.validateNotNull(inizio, "Data inizio");
		ValidationUtils.validateNotNull(fine, "Data fine");

		if (!fine.isAfter(inizio)) {
			throw new ValidationException("L'ora di fine deve essere dopo l'inizio");
		}
	}

	private void validaSessioneInPresenza(InPresenza sessione) throws ValidationException {
		ValidationUtils.validateNotNull(sessione, "Sessione");

		if (sessione.getRicette() == null || sessione.getRicette().isEmpty()) {
			throw new ValidationException("Le sessioni in presenza richiedono almeno una ricetta");
		}
	}

	public void aggiungiSessioneARicetta(Ricetta ricetta, Sessione sessione)
			throws ValidationException, DataAccessException {

		if (ricetta == null)
			throw new ValidationException("Ricetta non valida");
		if (sessione == null)
			throw new ValidationException("Sessione non trovata");

		if (!(sessione instanceof InPresenza)) {
			throw new ValidationException("Solo sessioni in presenza possono essere associate a ricette");
		}

		InPresenza ip = (InPresenza) sessione;

		try {
			if (ricetta.getSessioni().contains(ip)) {
				throw new ValidationException("La sessione è già associata a questa ricetta");
			}

			cucinaDAO.save(ricetta.getIdRicetta(), ip.getIdSessione());
			ricetta.getSessioni().add(ip);

		} catch (SQLException e) {
			throw new DataAccessException("Errore durante l'associazione sessione-ricetta", e);
		}
	}

	public void rimuoviSessioneDaRicetta(Ricetta ricetta, Sessione sessione)
			throws ValidationException, DataAccessException {

		if (ricetta == null)
			throw new ValidationException("Ricetta non valida");
		if (sessione == null)
			throw new ValidationException("Sessione non trovata");

		if (!(sessione instanceof InPresenza)) {
			throw new ValidationException("Solo sessioni in presenza possono essere rimosse");
		}

		InPresenza ip = (InPresenza) sessione;

		try {
			if (!ricetta.getSessioni().remove(ip)) {
				throw new ValidationException("La sessione non è associata a questa ricetta");
			}

			cucinaDAO.delete(ricetta.getIdRicetta(), ip.getIdSessione());

		} catch (SQLException e) {
			throw new DataAccessException("Errore durante la rimozione della sessione", e);
		}
	}
}
