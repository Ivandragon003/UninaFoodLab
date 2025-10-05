package controller;

import model.Chef;
import service.GestioneChef;
import exceptions.ValidationException;
import exceptions.ErrorMessages;

import java.time.LocalDate;
import java.sql.SQLException;

public class ChefController {

    private final GestioneChef gestioneChef;

    public ChefController(GestioneChef gestioneChef) {
        this.gestioneChef = gestioneChef;
    }

    // LOGIN
    public Chef login(String username, String password) throws ValidationException {
        if (username == null || username.isEmpty())
            throw new ValidationException(ErrorMessages.campoObbligatorio("Username"));
        if (password == null || password.isEmpty())
            throw new ValidationException(ErrorMessages.campoObbligatorio("Password"));

        try {
            Chef chef = gestioneChef.getChefByUsername(username);
            if (chef == null)
                throw new ValidationException(ErrorMessages.CREDENZIALI_ERRATE);
            if (!chef.getPassword().equals(password))
                throw new ValidationException(ErrorMessages.CREDENZIALI_ERRATE);

            return chef;

        } catch (SQLException e) {
            throw new ValidationException(ErrorMessages.ERRORE_DATABASE, e);
        }
    }

    // REGISTRAZIONE
    public Chef registraChef(String codFiscale, String nome, String cognome, String email,
                             LocalDate dataNascita, boolean disponibilita, String username, String password)
                             throws ValidationException {

        if (codFiscale == null || codFiscale.isEmpty())
            throw new ValidationException(ErrorMessages.campoObbligatorio("Codice fiscale"));
        if (nome == null || nome.isEmpty())
            throw new ValidationException(ErrorMessages.campoObbligatorio("Nome"));
        if (cognome == null || cognome.isEmpty())
            throw new ValidationException(ErrorMessages.campoObbligatorio("Cognome"));
        if (email == null || email.isEmpty())
            throw new ValidationException(ErrorMessages.campoObbligatorio("Email"));
        if (dataNascita == null)
            throw new ValidationException(ErrorMessages.campoObbligatorio("Data di nascita"));
        if (username == null || username.isEmpty())
            throw new ValidationException(ErrorMessages.campoObbligatorio("Username"));
        if (password == null || password.length() < 6)
            throw new ValidationException(ErrorMessages.PASSWORD_NON_VALIDA);

        try {
            if (gestioneChef.getChefByUsername(username) != null)
                throw new ValidationException("⚠️ Username già esistente");
            if (gestioneChef.existsByCodFiscale(codFiscale))
                throw new ValidationException("⚠️ Codice fiscale già presente");
            if (gestioneChef.existsByEmail(email))
                throw new ValidationException("⚠️ Email già presente");

            Chef chef = new Chef(codFiscale, nome, cognome, disponibilita, username, password);
            chef.setEmail(email);
            chef.setDataNascita(dataNascita);

            gestioneChef.creaChef(chef);
            return chef;

        } catch (SQLException e) {
            throw new ValidationException(ErrorMessages.ERRORE_DATABASE, e);
        }
    }

    public void aggiornaCredenziali(Chef chef, String nuovoUsername, String nuovaPassword) throws ValidationException {
        if (nuovoUsername == null || nuovoUsername.isEmpty())
            throw new ValidationException(ErrorMessages.campoObbligatorio("Username"));
        if (nuovaPassword == null || nuovaPassword.length() < 6)
            throw new ValidationException(ErrorMessages.PASSWORD_NON_VALIDA);

        try {
            chef.setUsername(nuovoUsername);
            chef.setPassword(nuovaPassword);
            gestioneChef.aggiornaChef(chef);
        } catch (SQLException e) {
            throw new ValidationException(ErrorMessages.ERRORE_AGGIORNAMENTO, e);
        }
    }

    public void eliminaAccount(Chef chef) throws ValidationException {
        try {
            gestioneChef.eliminaChef(chef.getUsername());
        } catch (SQLException e) {
            throw new ValidationException(ErrorMessages.ERRORE_ELIMINAZIONE, e);
        }
    }

    public GestioneChef getGestioneChef() {
        return gestioneChef;
    }
}
