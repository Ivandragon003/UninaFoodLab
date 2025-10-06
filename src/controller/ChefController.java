package controller;

import model.Chef;
import service.GestioneChef;
import exceptions.ValidationException;
import exceptions.DataAccessException;
import exceptions.ErrorMessages;

import java.time.LocalDate;
import java.sql.SQLException;
import java.util.Optional;

public class ChefController {

    private final GestioneChef gestioneChef;

    public ChefController(GestioneChef gestioneChef) {
        this.gestioneChef = gestioneChef;
    }

    public Chef login(String username, String password) throws ValidationException, DataAccessException {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username obbligatorio");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Password obbligatoria");
        }

        try {
            Optional<Chef> chefOpt = gestioneChef.trovaChefPerUsername(username.trim());
            
            if (chefOpt.isEmpty()) {
                throw new ValidationException(ErrorMessages.USERNAME_NON_TROVATO);
            }
            
            Chef chef = chefOpt.get();
            
            if (!chef.getPassword().equals(password)) {
                throw new ValidationException(ErrorMessages.PASSWORD_ERRATA);
            }
            
            return chef;
            
        } catch (SQLException e) {
            throw new DataAccessException(ErrorMessages.ERRORE_DATABASE);
        }
    }

    public Chef registraChef(String codFiscale, String nome, String cognome, String email,
                            LocalDate dataNascita, boolean disponibilita, 
                            String username, String password) 
            throws ValidationException, DataAccessException {
        
        return gestioneChef.creaChef(codFiscale, nome, cognome, email, 
                                     dataNascita, disponibilita, username, password);
    }

    public void aggiornaCredenziali(Chef chef, String nuovoUsername, String nuovaPassword) 
            throws ValidationException, DataAccessException {
        gestioneChef.aggiornaCredenziali(chef, nuovoUsername, nuovaPassword);
    }

    public void eliminaAccount(Chef chef) throws ValidationException, DataAccessException {
        if (chef == null || chef.getUsername() == null) {
            throw new ValidationException("Chef non valido");
        }
        gestioneChef.eliminaChef(chef.getUsername());
    }

    public GestioneChef getGestioneChef() {
        return gestioneChef;
    }
}
