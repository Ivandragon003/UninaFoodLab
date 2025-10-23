package controller;

import dao.ChefDAO;
import dao.CorsoCucinaDAO;
import dao.TieneDAO;
import exceptions.DataAccessException;
import exceptions.ValidationException;
import helper.ValidationUtils;
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

	public List<Chef> getTuttiGliChef() throws DataAccessException {
		try {
			return chefDAO.getAll();
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante il recupero degli chef.", e);
		}
	}

	public void creaCorso(CorsoCucina corso) throws ValidationException, DataAccessException {
		validateRequired(corso, "Corso");
		ValidationUtils.validateNotEmpty(corso.getNomeCorso(), "Nome corso");
		ValidationUtils.validatePositiveInt(corso.getNumeroPosti(), "Numero posti"); 
		ensureChefLogged();

		corso.setCodfiscaleFondatore(chefLoggato.getCodFiscale());

		try {
			corsoDAO.save(corso);
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante la creazione del corso.", e);
		}
	}

	public void modificaCorso(CorsoCucina corso) throws ValidationException, DataAccessException {
		validateRequired(corso, "Corso");
		ValidationUtils.validateNotEmpty(corso.getNomeCorso(), "Nome corso");
		try {
			corsoDAO.update(corso);
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante la modifica del corso.", e);
		}
	}

	public void eliminaCorso(int idCorso) throws DataAccessException {
		try {
			corsoDAO.delete(idCorso);
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante l'eliminazione del corso.", e);
		}
	}

	public void aggiungiChefACorso(CorsoCucina corso, Chef chef, String password)
			throws ValidationException, DataAccessException {

		validateRequired(corso, "Corso", chef, "Chef");

		try {
			Optional<Chef> esistente = chefDAO.findByCodFiscale(chef.getCodFiscale());
			if (esistente.isEmpty()) {
				ValidationUtils.validateNotEmpty(password, "Password");
				ValidationUtils.validateTextLength(password, "Password", 6, 128);
				chefDAO.save(chef, password);
			}

			if (isChefAlreadyAssigned(corso, chef.getCodFiscale()))
				throw new ValidationException("Chef già assegnato al corso.");

			tieneDAO.save(chef.getCodFiscale(), corso.getIdCorso());
			corso.getChef().add(chef);

		} catch (SQLException e) {
			throw new DataAccessException("Errore durante l'assegnazione dello chef al corso.", e);
		}
	}

	public void rimuoviChefDaCorso(CorsoCucina corso, Chef chef) throws ValidationException, DataAccessException {

		validateRequired(corso, "Corso", chef, "Chef");

		try {
			if (!isChefAlreadyAssigned(corso, chef.getCodFiscale()))
				throw new ValidationException("Lo chef non è assegnato a questo corso.");

			tieneDAO.delete(chef.getCodFiscale(), corso.getIdCorso());
			corso.getChef().removeIf(c -> c.getCodFiscale().equals(chef.getCodFiscale()));

		} catch (SQLException e) {
			throw new DataAccessException("Errore durante la rimozione dello chef dal corso.", e);
		}
	}

	private void ensureChefLogged() throws ValidationException {
		ValidationUtils.validateNotNull(chefLoggato, "Chef loggato");
	}

	private boolean isChefAlreadyAssigned(CorsoCucina corso, String codFiscale) throws SQLException {
		return tieneDAO.getChefByCorso(corso.getIdCorso()).stream().anyMatch(c -> c.getCodFiscale().equals(codFiscale));
	}

	private void validateRequired(Object... args) throws ValidationException {
		for (int i = 0; i < args.length; i += 2) {
			Object value = args[i];
			String name = (String) args[i + 1];
			if (value instanceof String s)
				ValidationUtils.validateNotEmpty(s, name);
			else
				ValidationUtils.validateNotNull(value, name);
		}
	}
}
