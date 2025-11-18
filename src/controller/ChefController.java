package controller;

import dao.ChefDAO;
import dao.TieneDAO;
import exceptions.DataAccessException;
import exceptions.ValidationException;
import helper.ValidationUtils;
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

    public Chef login(String username, String password) 
            throws ValidationException, DataAccessException {
        ValidationUtils.validateNotEmpty(username, "Username");
        ValidationUtils.validateNotEmpty(password, "Password");

        try {
            Chef chef = chefDAO.findByUsername(username)
                    .orElseThrow(() -> new ValidationException("Username non esistente"));

            if (!chef.getPassword().equals(password))
                throw new ValidationException("Password non corretta");

            return chef;
        } catch (IllegalArgumentException e) {
            throw new ValidationException(e.getMessage());
        } catch (SQLException e) {
            throw new DataAccessException("Errore di connessione al database", e);
        }
    }

    public Chef registraChef(String codFiscale, String nome, String cognome, String email, 
                            LocalDate dataNascita, boolean disponibilita, String username, 
                            String password, int anniEsperienza)
            throws ValidationException, DataAccessException {

        ValidationUtils.validateNotEmpty(codFiscale, "Codice fiscale");
        ValidationUtils.validateNotEmpty(nome, "Nome");
        ValidationUtils.validateNotEmpty(cognome, "Cognome");
        ValidationUtils.validateNotEmpty(email, "Email");
        ValidationUtils.validateNotEmpty(username, "Username");
        ValidationUtils.validateNotEmpty(password, "Password");
        ValidationUtils.validateNotNull(dataNascita, "Data di nascita");

        ValidationUtils.validateLettersMin2(nome, "Nome");
        ValidationUtils.validateLettersMin2(cognome, "Cognome");

        int eta = LocalDate.now().getYear() - dataNascita.getYear();
        if (dataNascita.isAfter(LocalDate.now().minusYears(18))) {
            throw new ValidationException("Lo chef deve avere almeno 18 anni");
        }

        if (anniEsperienza < 0) {
            throw new ValidationException("Gli anni di esperienza non possono essere negativi");
        }

        int etaMinimaLavorativa = 18;
        int anniEsperienzaMassimi = eta - etaMinimaLavorativa;
        if (anniEsperienza > anniEsperienzaMassimi) {
            throw new ValidationException(
                String.format("Con %d anni puoi avere al massimo %d anni di esperienza", 
                             eta, anniEsperienzaMassimi));
        }

        if (password == null || password.length() <= 6) {
            throw new ValidationException("La password deve contenere almeno 6 caratteri");
        }

        try {
            checkUniqueConstraints(codFiscale, email, username);

            Chef chef = new Chef(codFiscale, nome, cognome, disponibilita, username, password);
            chef.setEmail(email);
            chef.setDataNascita(dataNascita);
            chef.setAnniEsperienza(anniEsperienza);

            chefDAO.save(chef, password);
            return chef;

        } catch (IllegalArgumentException e) {
            throw new ValidationException(e.getMessage());
        } catch (SQLException e) {
            if (e.getMessage().contains("chef_password_check")) {
                throw new ValidationException("La password deve contenere almeno 7 caratteri");
            }
            throw new DataAccessException("Errore durante il salvataggio", e);
        }
    }

    public void eliminaAccount(Chef chef) 
            throws ValidationException, DataAccessException {
        ValidationUtils.validateNotNull(chef, "Chef");
        ValidationUtils.validateNotEmpty(chef.getUsername(), "Username");
        try {
            Optional<Chef> chefOpt = chefDAO.findByUsername(chef.getUsername());
            if (chefOpt.isEmpty())
                throw new ValidationException("Chef non trovato: " + chef.getUsername());
            chefDAO.delete(chefOpt.get().getCodFiscale());
        } catch (SQLException e) {
            throw new DataAccessException("Errore durante l'eliminazione", e);
        }
    }

    public List<CorsoCucina> getCorsiByChef(Chef chef) 
            throws ValidationException, DataAccessException {
        ValidationUtils.validateNotNull(chef, "Chef");
        try {
            return tieneDAO.getCorsiByChef(chef.getCodFiscale());
        } catch (SQLException e) {
            throw new DataAccessException("Errore durante il recupero dei corsi", e);
        }
    }

    private void checkUniqueConstraints(String codFiscale, String email, String username)
            throws ValidationException, SQLException {
        if (chefDAO.existsByCodFiscale(codFiscale))
            throw new ValidationException("Codice fiscale già presente");
        if (chefDAO.existsByEmail(email))
            throw new ValidationException("Email già utilizzata");
        if (chefDAO.findByUsername(username).isPresent())
            throw new ValidationException("Username già esistente");
    }

    public List<Chef> getAllChef() throws DataAccessException {
        try {
            return chefDAO.getAll();
        } catch (SQLException e) {
            throw new DataAccessException("Errore durante il recupero degli chef", e);
        }
    }
}
