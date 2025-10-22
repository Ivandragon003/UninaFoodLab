package controller;

import dao.ChefDAO;
import dao.CorsoCucinaDAO;
import dao.TieneDAO;
import exceptions.DataAccessException;
import exceptions.ValidationException;
import exceptions.ValidationUtils;
import model.Chef;
import model.CorsoCucina;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class GestioneCorsoController {

	private final CorsoCucinaDAO corsoDAO;
	private final ChefDAO chefDAO;
	private final TieneDAO tieneDAO;
	private Chef chefLoggato;

	public GestioneCorsoController(ChefDAO chefDAO, TieneDAO tieneDAO, CorsoCucinaDAO corsoDAO) {
		this.chefDAO = chefDAO;
		this.tieneDAO = tieneDAO;
		this.corsoDAO = corsoDAO;
	}

	public void setChefLoggato(Chef chef) {
		this.chefLoggato = chef;
	}

	public Chef getChefLoggato() {
		return chefLoggato;
	}

	// Recupero tutti i corsi
	public List<CorsoCucina> getTuttiICorsi() throws DataAccessException {
		try {
			return corsoDAO.getAll();
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante il recupero dei corsi.", e);
		}
	}

	// Recupero tutti gli chef
	public List<Chef> getTuttiGliChef() throws DataAccessException {
		try {
			return chefDAO.getAll();
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante il recupero degli chef.", e);
		}
	}

	// Creazione corso
	public void creaCorso(CorsoCucina corso) throws ValidationException, DataAccessException {
		ValidationUtils.validateNotNull(corso, "Corso");
		ValidationUtils.validateNotEmpty(corso.getNomeCorso(), "Nome corso");
		ValidationUtils.validatePositiveInt(corso.getNumeroPosti(), "Numero posti");

		ValidationUtils.validateNotNull(chefLoggato, "Chef loggato");

		corso.setCodfiscaleFondatore(chefLoggato.getCodFiscale());

		try {
			corsoDAO.save(corso);
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante la creazione del corso.", e);
		}
	}

	// Modifica corso
	public void modificaCorso(CorsoCucina corso) throws ValidationException, DataAccessException {
		ValidationUtils.validateNotNull(corso, "Corso");
		ValidationUtils.validateNotEmpty(corso.getNomeCorso(), "Nome corso");

		try {
			corsoDAO.update(corso);
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante la modifica del corso.", e);
		}
	}

	// Eliminazione corso
	public void eliminaCorso(int idCorso) throws DataAccessException {
		try {
			corsoDAO.delete(idCorso);
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante l'eliminazione del corso.", e);
		}
	}

	// Assegna chef a corso
	public void aggiungiChefACorso(CorsoCucina corso, Chef chef, String password)
			throws ValidationException, DataAccessException {

		ValidationUtils.validateNotNull(corso, "Corso");
		ValidationUtils.validateNotNull(chef, "Chef");

		try {
			Optional<Chef> esistente = chefDAO.findByCodFiscale(chef.getCodFiscale());
			if (esistente.isEmpty()) {
				ValidationUtils.validateNotEmpty(password, "Password");
				ValidationUtils.validateTextLength(password, "Password", 6, 128);
				chefDAO.save(chef, password);
			}

			List<Chef> giaAssegnati = tieneDAO.getChefByCorso(corso.getIdCorso());
			if (giaAssegnati.stream().anyMatch(c -> c.getCodFiscale().equals(chef.getCodFiscale())))
				throw new ValidationException("Chef già assegnato al corso.");

			tieneDAO.save(chef.getCodFiscale(), corso.getIdCorso());
			corso.getChef().add(chef);

		} catch (SQLException e) {
			throw new DataAccessException("Errore durante l'assegnazione dello chef al corso.", e);
		}
	}

	// Rimuovi chef dal corso
	public void rimuoviChefDaCorso(CorsoCucina corso, Chef chef) throws ValidationException, DataAccessException {

		ValidationUtils.validateNotNull(corso, "Corso");
		ValidationUtils.validateNotNull(chef, "Chef");

		try {
			List<Chef> assegnati = tieneDAO.getChefByCorso(corso.getIdCorso());
			boolean presente = assegnati.stream().anyMatch(c -> c.getCodFiscale().equals(chef.getCodFiscale()));

			if (!presente)
				throw new ValidationException("Lo chef non è assegnato a questo corso.");

			tieneDAO.delete(chef.getCodFiscale(), corso.getIdCorso());
			corso.getChef().removeIf(c -> c.getCodFiscale().equals(chef.getCodFiscale()));

		} catch (SQLException e) {
			throw new DataAccessException("Errore durante la rimozione dello chef dal corso.", e);
		}
	}

	// Recupera corso completo
	public CorsoCucina getCorsoCompleto(int idCorso) throws DataAccessException {
		try {
			return corsoDAO.getCorsoCompleto(idCorso);
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante il recupero del corso.", e);
		}
	}
}
