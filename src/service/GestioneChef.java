package service;

import dao.ChefDAO;
import dao.TieneDAO;
import model.Chef;
import model.CorsoCucina;
import exceptions.ValidationException;
import exceptions.DataAccessException;
import exceptions.ErrorMessages;
import exceptions.ValidationUtils;

import java.util.List;
import java.util.Optional;
import java.sql.SQLException;
import java.time.LocalDate;

public class GestioneChef {

	private final ChefDAO chefDAO;
	private final TieneDAO tieneDAO;

	public GestioneChef(ChefDAO chefDAO, TieneDAO tieneDAO) {
		this.chefDAO = chefDAO;
		this.tieneDAO = tieneDAO;
	}

	public Chef login(String username, String password) throws ValidationException, DataAccessException {
		ValidationUtils.validateNotEmpty(username, "Username");
		ValidationUtils.validateNotEmpty(password, "Password");

		try {
			Chef chef = chefDAO.findByUsername(username)
					.orElseThrow(() -> new ValidationException(ErrorMessages.USERNAME_NON_TROVATO));

			if (!chef.getPassword().equals(password))
				throw new ValidationException(ErrorMessages.PASSWORD_ERRATA);

			return chef;
		} catch (SQLException e) {
			throw new DataAccessException(ErrorMessages.ERRORE_DATABASE, e);
		}
	}

	public Chef creaChef(String codFiscale, String nome, String cognome, String email, LocalDate dataNascita,
			boolean disponibilita, String username, String password) throws ValidationException, DataAccessException {

		validateChefInput(codFiscale, nome, cognome, email, dataNascita, username, password);

		try {
			checkUniqueConstraints(codFiscale, email, username);
			Chef chef = new Chef(codFiscale, nome, cognome, disponibilita, username, password);
			chef.setEmail(email);
			chef.setDataNascita(dataNascita);
			chefDAO.save(chef, password);
			return chef;
		} catch (SQLException e) {
			throw new DataAccessException(ErrorMessages.ERRORE_SALVATAGGIO, e);
		}
	}

	public void eliminaChef(String username) throws ValidationException, DataAccessException {

		ValidationUtils.validateNotEmpty(username, "Username");

		try {
			Optional<Chef> chefOpt = chefDAO.findByUsername(username);
			if (chefOpt.isEmpty())
				throw new ValidationException(ErrorMessages.CHEF_NON_PRESENTE + username);

			chefDAO.delete(chefOpt.get().getCodFiscale());
		} catch (SQLException e) {
			throw new DataAccessException(ErrorMessages.ERRORE_ELIMINAZIONE, e);
		}
	}

	public List<Chef> getAll() throws DataAccessException {
		try {
			return chefDAO.getAll();
		} catch (SQLException e) {
			throw new DataAccessException(ErrorMessages.ERRORE_LETTURA, e);
		}
	}

	private void validateChefInput(String codFiscale, String nome, String cognome, String email, LocalDate dataNascita,
			String username, String password) throws ValidationException {

		if (dataNascita == null)
			throw new ValidationException(ErrorMessages.campoObbligatorio("Data di nascita"));
		if (dataNascita.isAfter(LocalDate.now().minusYears(18)))
			throw new ValidationException("Lo chef deve avere almeno 18 anni");
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
}
