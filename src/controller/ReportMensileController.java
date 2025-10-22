package controller;

import exceptions.DataAccessException;
import exceptions.ValidationException;
import exceptions.ValidationUtils;
import model.*;

import java.time.LocalDate;
import java.util.*;

public class ReportMensileController {

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

		List<CorsoCucina> corsiDelChef = chefController.getCorsiByChef(chefLoggato);
		if (corsiDelChef == null || corsiDelChef.isEmpty()) {
			return new DatiReportMensile(inizio, fine, 0, 0, 0, 0.0, 0, 0);
		}

		List<Integer> idsCorsi = corsiDelChef.stream().filter(Objects::nonNull).map(CorsoCucina::getIdCorso).toList();
		if (idsCorsi.isEmpty()) {
			return new DatiReportMensile(inizio, fine, 0, 0, 0, 0.0, 0, 0);
		}

		Set<Integer> corsiNelPeriodo = new HashSet<>();
		List<Integer> sessioniInPresenzaIds = new ArrayList<>();
		int sessioniOnline = 0;

		for (Integer idCorso : idsCorsi) {
			List<Sessione> sessioni = visualizzaCorsiController.getSessioniPerCorsoInPeriodo(idCorso, inizio, fine);
			if (sessioni != null && !sessioni.isEmpty()) {
				corsiNelPeriodo.add(idCorso);
				for (Sessione s : sessioni) {
					if (s instanceof Online) {
						sessioniOnline++;
					} else if (s instanceof InPresenza ip) {
						sessioniInPresenzaIds.add(ip.getIdSessione());
					}
				}
			}
		}

		int numeroCorsi = corsiNelPeriodo.size();
		int sessioniPratiche = 0;
		int minRicette = 0, maxRicette = 0;
		double mediaRicette = 0.0;

		if (!sessioniInPresenzaIds.isEmpty()) {
			Map<Integer, Integer> ricettePerSessione = gestioneSessioniController
					.getNumeroRicettePerSessioni(sessioniInPresenzaIds);

			java.util.IntSummaryStatistics stats = sessioniInPresenzaIds.stream()
					.map(id -> ricettePerSessione.getOrDefault(id, 0)).filter(n -> n > 0).mapToInt(Integer::intValue)
					.summaryStatistics();

			sessioniPratiche = (int) stats.getCount();
			if (sessioniPratiche > 0) {
				minRicette = stats.getMin();
				maxRicette = stats.getMax();
				mediaRicette = stats.getAverage();
			}
		}

		return new DatiReportMensile(inizio, fine, numeroCorsi, sessioniOnline, sessioniPratiche, mediaRicette,
				minRicette, maxRicette);
	}

	public Map<LocalDate, Integer> getRicettePerGiorno(LocalDate inizio, LocalDate fine)
			throws DataAccessException, ValidationException {

		ValidationUtils.validateNotNull(inizio, "Data inizio");
		ValidationUtils.validateNotNull(fine, "Data fine");

		Map<LocalDate, Integer> result = new TreeMap<>();

		List<CorsoCucina> corsiDelChef = chefController.getCorsiByChef(chefLoggato);

		if (corsiDelChef == null || corsiDelChef.isEmpty())
			return result;

		List<Integer> sessioniIds = new ArrayList<>();
		Map<Integer, LocalDate> sessioneToDate = new HashMap<>();

		for (CorsoCucina corso : corsiDelChef) {
			if (corso == null)
				continue;

			List<Sessione> sessioni = visualizzaCorsiController.getSessioniPerCorsoInPeriodo(corso.getIdCorso(), inizio,
					fine);

			if (sessioni != null) {
				for (Sessione s : sessioni) {
					if (s instanceof InPresenza ip) {
						sessioniIds.add(ip.getIdSessione());
						sessioneToDate.put(ip.getIdSessione(), ip.getDataInizioSessione().toLocalDate());
					}
				}
			}
		}

		if (!sessioniIds.isEmpty()) {
			Map<Integer, Integer> ricettePerSessione = gestioneSessioniController
					.getNumeroRicettePerSessioni(sessioniIds);

			for (Map.Entry<Integer, Integer> entry : ricettePerSessione.entrySet()) {
				LocalDate giorno = sessioneToDate.get(entry.getKey());
				if (giorno != null && entry.getValue() > 0) {
					result.merge(giorno, entry.getValue(), Integer::sum);
				}
			}
		}

		return result;
	}

	public Chef getChefLoggato() {
		return chefLoggato;
	}

	public static class DatiReportMensile {
		private final LocalDate inizio;
		private final LocalDate fine;
		private final int numeroCorsi;
		private final int sessioniOnline;
		private final int sessioniPratiche;
		private final double mediaRicette;
		private final int minRicette;
		private final int maxRicette;

		public DatiReportMensile(LocalDate inizio, LocalDate fine, int numeroCorsi, int sessioniOnline,
				int sessioniPratiche, double mediaRicette, int minRicette, int maxRicette) {
			this.inizio = inizio;
			this.fine = fine;
			this.numeroCorsi = numeroCorsi;
			this.sessioniOnline = sessioniOnline;
			this.sessioniPratiche = sessioniPratiche;
			this.mediaRicette = mediaRicette;
			this.minRicette = minRicette;
			this.maxRicette = maxRicette;
		}

		public LocalDate getInizio() {
			return inizio;
		}

		public LocalDate getFine() {
			return fine;
		}

		public int getNumeroCorsi() {
			return numeroCorsi;
		}

		public int getSessioniOnline() {
			return sessioniOnline;
		}

		public int getSessioniPratiche() {
			return sessioniPratiche;
		}

		public double getMediaRicette() {
			return mediaRicette;
		}

		public int getMinRicette() {
			return minRicette;
		}

		public int getMaxRicette() {
			return maxRicette;
		}
	}
}