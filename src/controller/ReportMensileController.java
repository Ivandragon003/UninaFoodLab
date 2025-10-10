package controller;

import exceptions.DataAccessException;
import model.CorsoCucina;
import model.Sessione;
import model.InPresenza;
import model.Online;
import model.Chef;
import service.GestioneCorsiCucina;
import service.GestioneSessioni;

import java.time.LocalDate;
import java.util.*;

public class ReportMensileController {

	private final GestioneCorsiCucina corsiService;
	private final GestioneSessioni sessioniService;
	private final Chef chefLoggato;

	public ReportMensileController(GestioneCorsiCucina corsiService, GestioneSessioni sessioniService,
			Chef chefLoggato) {
		this.corsiService = Objects.requireNonNull(corsiService);
		this.sessioniService = Objects.requireNonNull(sessioniService);
		this.chefLoggato = Objects.requireNonNull(chefLoggato);
	}

	public DatiReportMensile generaReport(LocalDate inizio, LocalDate fine) throws DataAccessException {
		int numeroCorsi = 0;
		int sessioniOnline = 0;
		int sessioniPratiche = 0;
		int totaleRicette = 0;
		int minRicette = Integer.MAX_VALUE;
		int maxRicette = 0;

		List<CorsoCucina> corsiDelChef = corsiService.getCorsiByChef(chefLoggato);
		if (corsiDelChef == null || corsiDelChef.isEmpty()) {
			return new DatiReportMensile(inizio, fine, 0, 0, 0, 0.0, 0, 0);
		}

		numeroCorsi = corsiDelChef.size();
		List<Integer> sessioniInPresenzaIds = new ArrayList<>();

		for (CorsoCucina corso : corsiDelChef) {
			if (corso == null)
				continue;

			List<Sessione> sessioniNelPeriodo = corsiService.getSessioniPerCorsoInPeriodo(corso.getIdCorso(), inizio,
					fine);
			if (sessioniNelPeriodo == null || sessioniNelPeriodo.isEmpty())
				continue;

			for (Sessione s : sessioniNelPeriodo) {
				if (s == null)
					continue;

				if (s instanceof Online) {
					sessioniOnline++;
				} else if (s instanceof InPresenza ip) {
					sessioniPratiche++;
					sessioniInPresenzaIds.add(ip.getIdSessione());
				}
			}
		}

		if (!sessioniInPresenzaIds.isEmpty()) {
			Map<Integer, Integer> ricettePerSessione = sessioniService
					.getNumeroRicettePerSessioni(sessioniInPresenzaIds);

			for (Integer idSessione : sessioniInPresenzaIds) {
				int numRicette = ricettePerSessione.getOrDefault(idSessione, 0);
				totaleRicette += numRicette;
				minRicette = Math.min(minRicette, numRicette);
				maxRicette = Math.max(maxRicette, numRicette);
			}

			if (minRicette == Integer.MAX_VALUE)
				minRicette = 0; // tutte le sessioni hanno 0 ricette
		} else {
			minRicette = 0;
		}

		double mediaRicette = !sessioniInPresenzaIds.isEmpty() ? (double) totaleRicette / sessioniInPresenzaIds.size()
				: 0.0;

		return new DatiReportMensile(inizio, fine, numeroCorsi, sessioniOnline, sessioniPratiche, mediaRicette,
				minRicette, maxRicette);
	}

	public Map<LocalDate, Integer> getRicettePerGiorno(LocalDate inizio, LocalDate fine) throws DataAccessException {
		Map<LocalDate, Integer> result = new TreeMap<>();
		List<CorsoCucina> corsiDelChef = corsiService.getCorsiByChef(chefLoggato);

		if (corsiDelChef == null || corsiDelChef.isEmpty()) {
			return result;
		}

		List<Integer> sessioniIds = new ArrayList<>();
		Map<Integer, LocalDate> sessioneToDate = new HashMap<>();

		for (CorsoCucina corso : corsiDelChef) {
			if (corso == null)
				continue;

			List<Sessione> sessioniNelPeriodo = corsiService.getSessioniPerCorsoInPeriodo(corso.getIdCorso(), inizio,
					fine);

			if (sessioniNelPeriodo == null || sessioniNelPeriodo.isEmpty())
				continue;

			for (Sessione s : sessioniNelPeriodo) {
				if (s instanceof InPresenza ip) {
					sessioniIds.add(ip.getIdSessione());
					sessioneToDate.put(ip.getIdSessione(), ip.getDataInizioSessione().toLocalDate());
				}
			}
		}

		if (!sessioniIds.isEmpty()) {
			Map<Integer, Integer> ricettePerSessione = sessioniService.getNumeroRicettePerSessioni(sessioniIds);

			if (ricettePerSessione != null && !ricettePerSessione.isEmpty()) {
				for (Map.Entry<Integer, Integer> entry : ricettePerSessione.entrySet()) {
					int idSessione = entry.getKey();
					Integer numRicette = entry.getValue();

					if (numRicette != null && numRicette > 0) {
						LocalDate giorno = sessioneToDate.get(idSessione);
						if (giorno != null) {
							result.put(giorno, result.getOrDefault(giorno, 0) + numRicette);
						}
					}
				}
			}
		}

		return result;
	}

	public Chef getChefLoggato() {
		return chefLoggato;
	}

	public static class DatiReportMensile {
		private final LocalDate iwnizio;
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