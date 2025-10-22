package controller;

import dao.ChefDAO;
import dao.TieneDAO;
import exceptions.DataAccessException;
import exceptions.ValidationException;
import exceptions.ValidationUtils;
import model.Chef;
import model.CorsoCucina;

import java.sql.SQLException;
import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

public class ChefController {

	private final ChefDAO chefDAO;
	private final TieneDAO tieneDAO;

	public ChefController(ChefDAO chefDAO, TieneDAO tieneDAO) {
		this.chefDAO = chefDAO;
		this.tieneDAO = tieneDAO;
	}

	public Chef login(String username, String password) throws ValidationException, DataAccessException {
		ValidationUtils.validateNotEmpty(username, "Username");
		ValidationUtils.validateNotEmpty(password, "Password");

		try {
			Chef chef = chefDAO.findByUsername(username)
					.orElseThrow(() -> new ValidationException("Username non esistente"));

			if (!chef.getPassword().equals(password))
				throw new ValidationException("Password non corretta");

			return chef;
		} catch (SQLException e) {
			throw new DataAccessException("Errore di connessione al database durante il login", e);
		}
	}

	public Chef registraChef(String codFiscale, String nome, String cognome, String email, LocalDate dataNascita,
			boolean disponibilita, String username, String password) throws ValidationException, DataAccessException {

		validateRequired(codFiscale, "Codice fiscale", nome, "Nome", cognome, "Cognome", email, "Email", username,
				"Username", password, "Password");
		ValidationUtils.validateNotNull(dataNascita, "Data di nascita");

		if (dataNascita.isAfter(LocalDate.now().minusYears(18)))
			throw new ValidationException("Lo chef deve avere almeno 18 anni");

		try {
			checkUniqueConstraints(codFiscale, email, username);

			Chef chef = new Chef(codFiscale, nome, cognome, disponibilita, username, password);
			chef.setEmail(email);
			chef.setDataNascita(dataNascita);

			chefDAO.save(chef, password);
			return chef;

		} catch (SQLException e) {
			throw new DataAccessException("Errore durante il salvataggio del nuovo chef nel database", e);
		}
	}

	public void eliminaAccount(Chef chef) throws ValidationException, DataAccessException {
		ValidationUtils.validateNotNull(chef, "Chef");
		ValidationUtils.validateNotEmpty(chef.getUsername(), "Username");
		try {
			Optional<Chef> chefOpt = chefDAO.findByUsername(chef.getUsername());
			if (chefOpt.isEmpty())
				throw new ValidationException("Chef non trovato nel sistema: " + chef.getUsername());
			chefDAO.delete(chefOpt.get().getCodFiscale());
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante l'eliminazione dell'account chef dal database", e);
		}
	}

	public List<CorsoCucina> getCorsiByChef(Chef chef) throws ValidationException, DataAccessException {
		ValidationUtils.validateNotNull(chef, "Chef");
		try {
			return tieneDAO.getCorsiByChef(chef.getCodFiscale());
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante il recupero dei corsi per lo chef", e);
		}
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

	private void checkUniqueConstraints(String codFiscale, String email, String username)
			throws ValidationException, SQLException {
		if (chefDAO.existsByCodFiscale(codFiscale))
			throw new ValidationException("Codice fiscale già presente nel sistema");
		if (chefDAO.existsByEmail(email))
			throw new ValidationException("Email già utilizzata");
		if (chefDAO.findByUsername(username).isPresent())
			throw new ValidationException("Username già esistente");
	}

	public List<Chef> getAllChef() throws DataAccessException {
		try {
			return chefDAO.getAll();
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante il recupero della lista degli chef", e);
		}
	}

}