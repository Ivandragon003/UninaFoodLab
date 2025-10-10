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
		int conteggioSessioniConRicette = 0;

		List<CorsoCucina> corsiDelChef = corsiService.getCorsiByChef(chefLoggato);
		if (corsiDelChef == null || corsiDelChef.isEmpty()) {
			return new DatiReportMensile(inizio, fine, 0, 0, 0, 0.0, 0, 0);
		}

		// Conta tutti i corsi dello chef
		numeroCorsi = corsiDelChef.size();

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

					int numRicette;
					try {
						numRicette = sessioniService.getNumeroRicettePerSessione(ip.getIdSessione());
					} catch (Exception ex) {
						numRicette = (ip.getRicette() != null) ? ip.getRicette().size() : 0;
					}

					if (numRicette > 0) {
						totaleRicette += numRicette;
						conteggioSessioniConRicette++;
						minRicette = Math.min(minRicette, numRicette);
						maxRicette = Math.max(maxRicette, numRicette);
					}
				}
			}
		}

		double mediaRicette = (conteggioSessioniConRicette > 0) ? (double) totaleRicette / conteggioSessioniConRicette
				: 0.0;

		if (conteggioSessioniConRicette == 0)
			minRicette = 0;

		return new DatiReportMensile(inizio, fine, numeroCorsi, sessioniOnline, sessioniPratiche, mediaRicette,
				minRicette, maxRicette);
	}

	public Map<LocalDate, Integer> getRicettePerGiorno(LocalDate inizio, LocalDate fine) throws DataAccessException {
		Map<LocalDate, Integer> result = new TreeMap<>();
		List<CorsoCucina> corsiDelChef = corsiService.getCorsiByChef(chefLoggato);
		if (corsiDelChef == null || corsiDelChef.isEmpty())
			return result;

		for (CorsoCucina corso : corsiDelChef) {
			if (corso == null)
				continue;
			int idCorso = corso.getIdCorso();
			List<Sessione> sessioniNelPeriodo = corsiService.getSessioniPerCorsoInPeriodo(idCorso, inizio, fine);
			if (sessioniNelPeriodo == null || sessioniNelPeriodo.isEmpty())
				continue;

			for (Sessione s : sessioniNelPeriodo) {
				if (!(s instanceof InPresenza))
					continue;
				InPresenza ip = (InPresenza) s;
				int numRicette = 0;
				try {
					numRicette = sessioniService.getNumeroRicettePerSessione(ip.getIdSessione());
				} catch (Exception ex) {
					if (ip.getRicette() != null)
						numRicette = ip.getRicette().size();
					else
						numRicette = 0;
				}
				if (numRicette <= 0)
					continue;
				LocalDate giorno = ip.getDataInizioSessione().toLocalDate();
				result.put(giorno, result.getOrDefault(giorno, 0) + numRicette);
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
