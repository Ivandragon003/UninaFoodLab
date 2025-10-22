package controller;

import dao.*;
import exceptions.DataAccessException;


import model.*;

import java.sql.SQLException;
import java.time.LocalDate;

import java.util.*;
import java.util.stream.Collectors;

public class ReportMensileController {

	// DAOs necessari
	
	private final TieneDAO tieneDAO;
	private final OnlineDAO onlineDAO;
	private final InPresenzaDAO inPresenzaDAO;
	private final CucinaDAO cucinaDAO;
	
	private final Chef chefLoggato;

	public ReportMensileController( TieneDAO tieneDAO, 
			OnlineDAO onlineDAO, InPresenzaDAO inPresenzaDAO, CucinaDAO cucinaDAO,
			Chef chefLoggato) {
		
		this.tieneDAO = Objects.requireNonNull(tieneDAO);
		this.onlineDAO = Objects.requireNonNull(onlineDAO);
		this.inPresenzaDAO = Objects.requireNonNull(inPresenzaDAO);
		this.cucinaDAO = Objects.requireNonNull(cucinaDAO);
		this.chefLoggato = Objects.requireNonNull(chefLoggato);
	}

	public DatiReportMensile generaReport(LocalDate inizio, LocalDate fine) throws DataAccessException {
		// Recupera corsi dello chef (logica da GestioneCorsiCucina)
		List<CorsoCucina> corsiDelChef = getCorsiByChef(chefLoggato);
		
		if (corsiDelChef == null || corsiDelChef.isEmpty()) {
			return new DatiReportMensile(inizio, fine, 0, 0, 0, 0.0, 0, 0);
		}

		// Raccogli tutti gli ID dei corsi in una sola operazione
		List<Integer> idsCorsi = corsiDelChef.stream()
				.filter(Objects::nonNull)
				.map(CorsoCucina::getIdCorso)
				.collect(Collectors.toList());

		if (idsCorsi.isEmpty()) {
			return new DatiReportMensile(inizio, fine, 0, 0, 0, 0.0, 0, 0);
		}

		// Recupera sessioni per periodo
		Map<Integer, List<Sessione>> sessioniPerCorso = new HashMap<>();
		Set<Integer> corsiNelPeriodo = new HashSet<>();
		List<Integer> sessioniInPresenzaIds = new ArrayList<>();
		int sessioniOnline = 0;

		for (Integer idCorso : idsCorsi) {
			List<Sessione> sessioni = getSessioniPerCorsoInPeriodo(idCorso, inizio, fine);
			if (sessioni != null && !sessioni.isEmpty()) {
				sessioniPerCorso.put(idCorso, sessioni);
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
		int totaleRicette = 0;
		int minRicette = 0;
		int maxRicette = 0;
		double mediaRicette = 0.0;

		// Recupera numero ricette per sessioni (logica da GestioneSessioni)
		if (!sessioniInPresenzaIds.isEmpty()) {
			Map<Integer, Integer> ricettePerSessione = getNumeroRicettePerSessioni(sessioniInPresenzaIds);

			List<Integer> ricetteValide = new ArrayList<>();

			for (Integer idSessione : sessioniInPresenzaIds) {
				int numRicette = ricettePerSessione.getOrDefault(idSessione, 0);
				if (numRicette > 0) {
					ricetteValide.add(numRicette);
					totaleRicette += numRicette;
				}
			}

			sessioniPratiche = ricetteValide.size();

			if (!ricetteValide.isEmpty()) {
				minRicette = ricetteValide.stream().min(Integer::compareTo).orElse(0);
				maxRicette = ricetteValide.stream().max(Integer::compareTo).orElse(0);
				mediaRicette = (double) totaleRicette / sessioniPratiche;
			}
		}

		return new DatiReportMensile(inizio, fine, numeroCorsi, sessioniOnline, sessioniPratiche, 
				mediaRicette, minRicette, maxRicette);
	}

	public Map<LocalDate, Integer> getRicettePerGiorno(LocalDate inizio, LocalDate fine) throws DataAccessException {
		Map<LocalDate, Integer> result = new TreeMap<>();
		
		// Recupera corsi dello chef
		List<CorsoCucina> corsiDelChef = getCorsiByChef(chefLoggato);

		if (corsiDelChef == null || corsiDelChef.isEmpty())
			return result;

		List<Integer> sessioniIds = new ArrayList<>();
		Map<Integer, LocalDate> sessioneToDate = new HashMap<>();

		for (CorsoCucina corso : corsiDelChef) {
			if (corso == null)
				continue;

			List<Sessione> sessioni = getSessioniPerCorsoInPeriodo(corso.getIdCorso(), inizio, fine);

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
			Map<Integer, Integer> ricettePerSessione = getNumeroRicettePerSessioni(sessioniIds);

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

	// ========== CLASSE DATI REPORT ==========
	
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