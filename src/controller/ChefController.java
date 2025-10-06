package controller;

import model.Chef;
import service.GestioneChef;
import exceptions.ValidationException;
import exceptions.DataAccessException;

import java.time.LocalDate;

/**
 * Controller per la gestione delle operazioni Chef.
 * Responsabilità: orchestrazione delle chiamate al service layer.
 * NON contiene logica business.
 */
public class ChefController {
    private final GestioneChef gestioneChef;

    public ChefController(GestioneChef gestioneChef) {
        this.gestioneChef = gestioneChef;
    }

    /**
     * Delega il login al service layer.
     */
    public Chef login(String username, String password) 
            throws ValidationException, DataAccessException {
        return gestioneChef.login(username, password);
    }

    /**
     * Delega la registrazione al service layer.
     */
    public Chef registraChef(String codFiscale, String nome, String cognome, 
                            String email, LocalDate dataNascita, boolean disponibilita, 
                            String username, String password) 
            throws ValidationException, DataAccessException {
        return gestioneChef.creaChef(codFiscale, nome, cognome, email, 
                                     dataNascita, disponibilita, username, password);
    }

    /**
     * Delega l'aggiornamento credenziali al service layer.
     */
    public void aggiornaCredenziali(Chef chef, String nuovoUsername, String nuovaPassword) 
            throws ValidationException, DataAccessException {
        gestioneChef.aggiornaCredenziali(chef, nuovoUsername, nuovaPassword);
    }

    /**
     * Delega l'eliminazione account al service layer.
     * Validazione minima solo per evitare NullPointerException.
     */
    public void eliminaAccount(Chef chef) 
            throws ValidationException, DataAccessException {
        if (chef == null) {
            throw new ValidationException("Chef non valido");
        }
        gestioneChef.eliminaChef(chef.getUsername());
    }

    /**
     * Getter per permettere ad altri controller di accedere al service.
     */
    public GestioneChef getGestioneChef() {
        return gestioneChef;
    }
}