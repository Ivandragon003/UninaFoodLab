package controller;

import dao.*;
import exceptions.DataAccessException;
import exceptions.ValidationException;
import helper.ValidationUtils;
import model.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class VisualizzaCorsiController {

	private final CorsoCucinaDAO corsoDAO;
	private final TieneDAO tieneDAO;
	private final OnlineDAO onlineDAO;
	private final InPresenzaDAO inPresenzaDAO;
	private final Chef chefLoggato;

	public VisualizzaCorsiController(CorsoCucinaDAO corsoDAO, TieneDAO tieneDAO, OnlineDAO onlineDAO,
			InPresenzaDAO inPresenzaDAO, Chef chefLoggato) {
		this.corsoDAO = corsoDAO;
		this.tieneDAO = tieneDAO;
		this.onlineDAO = onlineDAO;
		this.inPresenzaDAO = inPresenzaDAO;
		this.chefLoggato = chefLoggato;
	}

	public Chef getChefLoggato() {
		return chefLoggato;
	}

	public List<CorsoCucina> getTuttiICorsi() throws DataAccessException {
		try {
			return corsoDAO.getAll();
		} catch (SQLException e) {
			throw new DataAccessException("Impossibile leggere i dati dei corsi", e);
		}
	}

	public List<CorsoCucina> getCorsiDelChef() throws DataAccessException {
		if (chefLoggato == null) {
			return Collections.emptyList();
		}

		try {
			String cf = chefLoggato.getCodFiscale();

			List<CorsoCucina> assegnati = tieneDAO.getCorsiByChef(cf);
			List<CorsoCucina> fondati = corsoDAO.getCorsiByFondatore(cf);

			LinkedHashMap<Integer, CorsoCucina> byId = new LinkedHashMap<>();

			for (CorsoCucina c : assegnati) {
				byId.put(c.getIdCorso(), c);
			}

			for (CorsoCucina c : fondati) {
				byId.putIfAbsent(c.getIdCorso(), c);
			}

			return new ArrayList<>(byId.values());

		} catch (SQLException e) {
			throw new DataAccessException("Impossibile recuperare i corsi dello chef", e);
		}
	}

	public List<CorsoCucina> visualizzaCorsiChef() throws DataAccessException {
		return getCorsiDelChef();
	}

	public List<CorsoCucina> cercaPerNomeOCategoria(String filtro) throws DataAccessException {
		try {
			return corsoDAO.findByNomeOrArgomento(filtro);
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante la ricerca dei corsi", e);
		}
	}

	public int getNumeroSessioniPerCorso(int idCorso) throws DataAccessException {
		try {
			ValidationUtils.validatePositiveInt(idCorso, "ID corso");
		} catch (ValidationException e) {
			throw new DataAccessException(e.getMessage(), e);
		}

		try {
			return corsoDAO.getNumeroSessioniPerCorso(idCorso);
		} catch (SQLException e) {
			throw new DataAccessException("Errore nel conteggio delle sessioni", e);
		}
	}

	public CorsoCucina getCorsoCompleto(int idCorso) throws DataAccessException {
		try {
			ValidationUtils.validatePositiveInt(idCorso, "ID corso");
		} catch (ValidationException e) {
			throw new DataAccessException(e.getMessage(), e);
		}

		try {
			CorsoCucina corso = corsoDAO.findById(idCorso)
					.orElseThrow(() -> new DataAccessException("Corso non trovato nel sistema (ID: " + idCorso + ")"));

			List<Sessione> sessioni = new ArrayList<>();
			sessioni.addAll(onlineDAO.getByCorso(idCorso));
			sessioni.addAll(inPresenzaDAO.getByCorso(idCorso));
			corso.setSessioni(sessioni);

			corso.setChef(tieneDAO.getChefByCorso(idCorso));

			return corso;

		} catch (SQLException e) {
			throw new DataAccessException("Errore durante il recupero del corso completo", e);
		}
	}

	public List<Sessione> getSessioniPerCorsoInPeriodo(int idCorso, LocalDate inizio, LocalDate fine)
			throws DataAccessException {
		try {
			ValidationUtils.validatePositiveInt(idCorso, "ID corso");
			ValidationUtils.validateNotNull(inizio, "Data inizio");
			ValidationUtils.validateNotNull(fine, "Data fine");

			if (inizio.isAfter(fine)) {
				throw new ValidationException("La data di inizio non può essere successiva alla data di fine");
			}
		} catch (ValidationException e) {
			throw new DataAccessException("Errore durante il recupero delle sessioni per il corso " + idCorso, e);
		}

		try {
			LocalDateTime inizioDateTime = inizio.atStartOfDay();
			LocalDateTime fineDateTime = fine.atTime(23, 59, 59);

			List<Sessione> sessioni = new ArrayList<>();
			sessioni.addAll(onlineDAO.getByCorsoInPeriodo(idCorso, inizioDateTime, fineDateTime));
			sessioni.addAll(inPresenzaDAO.getByCorsoInPeriodo(idCorso, inizioDateTime, fineDateTime));

			sessioni.sort(Comparator.comparing(Sessione::getDataInizioSessione));

			return sessioni;

		} catch (SQLException e) {
			throw new DataAccessException("Errore durante il recupero delle sessioni", e);
		}
	}

}
