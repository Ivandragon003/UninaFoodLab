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

    // ✅ CORRETTO: Associa l'intero oggetto corso alla sessione
    sessione.setCorsoCucina(corso);

    // Valida solo la DATA (ignora orario)
    LocalDate dataSessione = sessione.getDataInizioSessione().toLocalDate();
    validaDataSessione(dataSessione);

    // Validazioni orario
    LocalDateTime now = LocalDateTime.now();
    if (sessione.getDataInizioSessione().isBefore(now)) {
        throw new ValidationException(
            "La sessione non può iniziare nel passato.\n\n" +
            "📅 Data/ora attuale: " + now.toLocalDate() + " " + now.toLocalTime() + "\n" +
            "📅 Data/ora selezionata: " + sessione.getDataInizioSessione().toLocalDate() + 
            " " + sessione.getDataInizioSessione().toLocalTime()
        );
    }

    if (!sessione.getDataFineSessione().isAfter(sessione.getDataInizioSessione())) {
        throw new ValidationException(
            "L'ora di fine deve essere dopo l'ora di inizio.\n\n" +
            "🕐 Inizio: " + sessione.getDataInizioSessione().toLocalTime() + "\n" +
            "🕐 Fine: " + sessione.getDataFineSessione().toLocalTime()
        );
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

	// ========== GESTIONE AUTOMATICA DATE CON FREQUENZA ==========

	/**
	 * ✅ Calcola la prossima data valida per una nuova sessione Usa la DATA FINE
	 * dell'ultima sessione come riferimento
	 */
	public LocalDate calcolaProssimaDataSessione() {
		List<Sessione> sessioni = corso.getSessioni();

		if (sessioni == null || sessioni.isEmpty()) {
			return corso.getDataInizioCorso().toLocalDate();
		}

		// ✅ Trova l'ultima sessione per DATA FINE (più recente)
		Sessione ultimaSessione = sessioni.stream().max(Comparator.comparing(Sessione::getDataFineSessione))
				.orElse(null);

		if (ultimaSessione == null) {
			return corso.getDataInizioCorso().toLocalDate();
		}

		// ✅ USA LA DATA FINE (non data inizio)
		LocalDate dataFineUltimaSessione = ultimaSessione.getDataFineSessione().toLocalDate();
		Frequenza freq = corso.getFrequenzaCorso();

		LocalDate prossimaData = switch (freq) {
		case unica -> null;
		case giornaliero -> dataFineUltimaSessione.plusDays(1);
		case ogniDueGiorni -> dataFineUltimaSessione.plusDays(2);
		case settimanale -> dataFineUltimaSessione.plusWeeks(1);
		case mensile -> dataFineUltimaSessione.plusMonths(1);
		};

		if (prossimaData != null && prossimaData.isAfter(corso.getDataFineCorso().toLocalDate())) {
			return null;
		}

		return prossimaData;
	}

	/**
	 * ✅ Valida che una data rispetti la frequenza del corso
	 */
	public boolean validaDataSessione(LocalDate dataSelezionata) throws ValidationException {
		if (dataSelezionata == null) {
			throw new ValidationException("Data non può essere null");
		}

		// Verifica se corso è finito
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

		if (dataSelezionata.isAfter(fineCorsoDate)) {
			throw new ValidationException("La sessione non può essere dopo la fine del corso.\n\n"
					+ "📅 Data fine corso: " + fineCorsoDate + "\n" + "📅 Data selezionata: " + dataSelezionata);
		}

		List<Sessione> sessioni = corso.getSessioni();

		if (sessioni == null || sessioni.isEmpty()) {
			return true;
		}

		// ✅ Controlla conflitti sulla STESSA DATA (solo giorno, ignora orario)
		boolean conflitto = sessioni.stream()
				.anyMatch(s -> s.getDataInizioSessione().toLocalDate().equals(dataSelezionata));

		if (conflitto) {
			throw new ValidationException("⚠️ Esiste già una sessione in data " + dataSelezionata + "\n\n"
					+ "Ogni data può ospitare una sola sessione.\n" + "Scegli una data diversa.");
		}

		// ✅ Trova l'ultima sessione (per data FINE, solo giorno)
		Sessione ultimaSessione = sessioni.stream()
				.max(Comparator.comparing(s -> s.getDataFineSessione().toLocalDate())).orElse(null);

		if (ultimaSessione != null) {
			LocalDate dataFineUltima = ultimaSessione.getDataFineSessione().toLocalDate();

			// ✅ Verifica che la nuova sessione inizi DOPO la fine dell'ultima (solo date)
			if (dataSelezionata.isBefore(dataFineUltima) || dataSelezionata.equals(dataFineUltima)) {
				throw new ValidationException("❌ Data Non Valida\n\n"
						+ "La nuova sessione deve iniziare dopo la fine dell'ultima sessione.\n\n"
						+ "🏁 Fine ultima sessione: " + dataFineUltima + "\n" + "📅 Data selezionata: "
						+ dataSelezionata + "\n\n" + "💡 La prossima data valida è: " + dataFineUltima.plusDays(1));
			}

			// ✅ Calcola distanza dalla DATA FINE (solo giorni)
			long giorniDistanza = ChronoUnit.DAYS.between(dataFineUltima, dataSelezionata);
			Frequenza freq = corso.getFrequenzaCorso();

			int giorniMinimi = switch (freq) {
			case unica -> Integer.MAX_VALUE;
			case giornaliero -> 1;
			case ogniDueGiorni -> 2;
			case settimanale -> 7;
			case mensile -> 30;
			};

			if (freq == Frequenza.unica) {
				throw new ValidationException(
						"❌ Il corso ha frequenza 'Sessione Unica'\n\n" + "Non puoi aggiungere altre sessioni.\n"
								+ "Esiste già 1 sessione (terminata il " + dataFineUltima + ")");
			}

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

	/**
	 * ✅ Verifica se è possibile aggiungere sessioni (controlla anche se corso
	 * finito)
	 */
	public boolean puoAggiungereSessions() {
		// ✅ Blocco se corso finito
		LocalDateTime fineCorso = corso.getDataFineCorso();
		if (fineCorso != null && LocalDateTime.now().isAfter(fineCorso)) {
			return false;
		}

		LocalDate prossimaData = calcolaProssimaDataSessione();
		return prossimaData != null;
	}

	public String getMotivoBloccoSessioni() {
		// ✅ Controlla se corso finito
		LocalDateTime fineCorso = corso.getDataFineCorso();
		if (fineCorso != null && LocalDateTime.now().isAfter(fineCorso)) {
			return "Il corso è terminato il " + fineCorso.toLocalDate()
					+ " - non è possibile aggiungere nuove sessioni";
		}

		if (corso.getFrequenzaCorso() == Frequenza.unica) {
			List<Sessione> sessioni = corso.getSessioni();
			if (sessioni != null && !sessioni.isEmpty()) {
				return "Il corso ha frequenza 'Sessione Unica' - è già presente 1 sessione";
			}
		}

		LocalDate prossimaData = calcolaProssimaDataSessione();
		if (prossimaData == null) {
			return "La prossima sessione supererebbe la data fine corso (" + corso.getDataFineCorso().toLocalDate()
					+ ")";
		}

		return null;
	}

	public String getInfoProssimaSessione() {
		LocalDate prossima = calcolaProssimaDataSessione();

		if (prossima == null) {
			return "❌ Non è possibile aggiungere altre sessioni";
		}

		List<Sessione> sessioni = corso.getSessioni();
		if (sessioni == null || sessioni.isEmpty()) {
			return "📅 Prima sessione - Data inizio corso: " + prossima;
		}

		Sessione ultima = sessioni.stream().max(Comparator.comparing(Sessione::getDataFineSessione)).orElse(null);

		if (ultima == null) {
			return "📅 Data suggerita: " + prossima;
		}

		LocalDate dataFineUltima = ultima.getDataFineSessione().toLocalDate();
		long giorni = ChronoUnit.DAYS.between(dataFineUltima, prossima);

		return String.format(
				"📅 Data suggerita: %s\n" + "📊 %d giorni dopo la fine dell'ultima sessione (%s)\n"
						+ "✅ Rispetta frequenza: %s",
				prossima, giorni, dataFineUltima, corso.getFrequenzaCorso().name());
	}

	public int getNumeroSessioniAttuali() {
		List<Sessione> sessioni = corso.getSessioni();
		return sessioni != null ? sessioni.size() : 0;
	}
}
