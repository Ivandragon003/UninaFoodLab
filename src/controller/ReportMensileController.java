package controller;

import exceptions.DataAccessException;
import exceptions.ValidationException;
import helper.ValidationUtils;
import model.*;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;

public class ReportMensileController {
	private static final Logger logger = Logger.getLogger(ReportMensileController.class.getName());

	private final ChefController chefController;
	private final VisualizzaCorsiController visualizzaCorsiController;
	private final GestioneSessioniController gestioneSessioniController;
	private final Chef chefLoggato;

	public ReportMensileController(ChefController chefController, VisualizzaCorsiController visualizzaCorsiController,
			GestioneSessioniController gestioneSessioniController, Chef chefLoggato) {
		this.chefController = chefController;
		this.visualizzaCorsiController = visualizzaCorsiController;
		this.gestioneSessioniController = gestioneSessioniController;
		this.chefLoggato = chefLoggato;
	}

	public DatiReportMensile generaReport(LocalDate inizio, LocalDate fine)
			throws DataAccessException, ValidationException {
		ValidationUtils.validateNotNull(inizio, "Data inizio");
		ValidationUtils.validateNotNull(fine, "Data fine");

		try {
			List<CorsoCucina> corsiDelChef = chefController.getCorsiByChef(chefLoggato);

			if (corsiDelChef == null || corsiDelChef.isEmpty()) {
				return new DatiReportMensile(inizio, fine, 0, 0, 0, 0.0, 0, 0);
			}

			Set<Integer> corsiNelPeriodo = new HashSet<>();
			List<Integer> sessioniInPresenzaIds = new ArrayList<>();
			int sessioniOnline = 0;

			for (CorsoCucina corso : corsiDelChef) {
				if (corso == null)
					continue;

				try {
					List<Sessione> sessioni = visualizzaCorsiController.getSessioniPerCorsoInPeriodo(corso.getIdCorso(),
							inizio, fine);

					if (sessioni != null && !sessioni.isEmpty()) {
						corsiNelPeriodo.add(corso.getIdCorso());
						for (Sessione s : sessioni) {
							if (s instanceof Online) {
								sessioniOnline++;
							} else if (s instanceof InPresenza ip) {
								sessioniInPresenzaIds.add(ip.getIdSessione());
							}
						}
					}
				} catch (Exception e) {
					logger.warning(
							"Errore nel recupero sessioni per corso " + corso.getIdCorso() + ": " + e.getMessage());
				}
			}

			int numeroCorsi = corsiNelPeriodo.size();
			int sessioniPratiche = 0;
			int minRicette = 0, maxRicette = 0;
			double mediaRicette = 0.0;

			if (!sessioniInPresenzaIds.isEmpty()) {
				try {
					Map<Integer, Integer> ricettePerSessione = gestioneSessioniController
							.getNumeroRicettePerSessioni(sessioniInPresenzaIds);

					if (ricettePerSessione != null && !ricettePerSessione.isEmpty()) {
						IntSummaryStatistics stats = ricettePerSessione.values().stream()
								.filter(n -> n != null && n > 0).mapToInt(Integer::intValue).summaryStatistics();

						sessioniPratiche = (int) stats.getCount();
						if (sessioniPratiche > 0) {
							minRicette = stats.getMin();
							maxRicette = stats.getMax();
							mediaRicette = stats.getAverage();
						}
					}

				} catch (Exception e) {
					logger.warning("Errore nel recupero ricette per sessioni: " + e.getMessage());
				}
			}

			return new DatiReportMensile(inizio, fine, numeroCorsi, sessioniOnline, sessioniPratiche, mediaRicette,
					minRicette, maxRicette);

		} catch (Exception e) {
			logger.severe("Errore critico durante il recupero delle sessioni: " + e.getMessage());
			throw new DataAccessException("Errore durante il recupero delle sessioni: " + e.getMessage(), e);
		}
	}

	public Map<LocalDate, Integer> getRicettePerGiorno(LocalDate inizio, LocalDate fine)
			throws DataAccessException, ValidationException {
		ValidationUtils.validateNotNull(inizio, "Data inizio");
		ValidationUtils.validateNotNull(fine, "Data fine");

		Map<LocalDate, Integer> result = new TreeMap<>();

		try {
			List<CorsoCucina> corsiDelChef = chefController.getCorsiByChef(chefLoggato);

			if (corsiDelChef == null || corsiDelChef.isEmpty()) {
				return result;
			}

			List<Integer> sessioniIds = new ArrayList<>();
			Map<Integer, LocalDate> sessioneToDate = new HashMap<>();

			for (CorsoCucina corso : corsiDelChef) {
				if (corso == null)
					continue;

				try {
					List<Sessione> sessioni = visualizzaCorsiController.getSessioniPerCorsoInPeriodo(corso.getIdCorso(),
							inizio, fine);

					if (sessioni != null) {
						for (Sessione s : sessioni) {
							if (s instanceof InPresenza ip) {
								sessioniIds.add(ip.getIdSessione());
								sessioneToDate.put(ip.getIdSessione(), ip.getDataInizioSessione().toLocalDate());
							}
						}
					}
				} catch (Exception e) {
					logger.warning("Errore nel recupero sessioni per corso " + corso.getIdCorso()
							+ " in getRicettePerGiorno: " + e.getMessage());
				}
			}

			if (!sessioniIds.isEmpty()) {
				try {
					Map<Integer, Integer> ricettePerSessione = gestioneSessioniController
							.getNumeroRicettePerSessioni(sessioniIds);

					if (ricettePerSessione != null) {
						for (Map.Entry<Integer, Integer> entry : ricettePerSessione.entrySet()) {
							LocalDate giorno = sessioneToDate.get(entry.getKey());
							if (giorno != null && entry.getValue() != null && entry.getValue() > 0) {
								result.merge(giorno, entry.getValue(), Integer::sum);
							}
						}
					}
				} catch (Exception e) {
					logger.warning(
							"Errore nel recupero ricette per sessioni in getRicettePerGiorno: " + e.getMessage());
				}
			}

			return result;
		} catch (Exception e) {
			logger.severe("Errore imprevisto nel recupero ricette per giorno: " + e.getMessage());
			throw new DataAccessException("Errore nel recupero ricette per giorno: " + e.getMessage(), e);
		}
	}

	public Chef getChefLoggato() {
		return chefLoggato;
	}
}