package controller;

import model.Chef;
import service.GestioneChef;
import exceptions.ValidationException;
import exceptions.DataAccessException;

import java.time.LocalDate;

/**
 * Controller semplificato - delega TUTTA la logica al Service
 * Fa solo da tramite tra GUI e Service
 */
public class ChefController {

    private final GestioneChef gestioneChef;

    public ChefController(GestioneChef gestioneChef) {
        this.gestioneChef = gestioneChef;
    }

    /**
     * Login - delega completamente al service
     * @throws ValidationException se credenziali non valide
     * @throws DataAccessException se errore database
     */
    public Chef login(String username, String password) 
            throws ValidationException, DataAccessException {
        return gestioneChef.login(username, password);
    }

    /**
     * Registrazione - delega completamente al service
     * @throws ValidationException se validazione fallisce
     * @throws DataAccessException se errore database
     */
    public Chef registraChef(String codFiscale, String nome, String cognome, String email,
                            LocalDate dataNascita, boolean disponibilita, 
                            String username, String password) 
            throws ValidationException, DataAccessException {
        
        return gestioneChef.creaChef(codFiscale, nome, cognome, email, 
                                     dataNascita, disponibilita, username, password);
    }

    /**
     * Aggiorna credenziali - delega al service
     */
    public void aggiornaCredenziali(Chef chef, String nuovoUsername, String nuovaPassword) 
            throws ValidationException, DataAccessException {
        gestioneChef.aggiornaCredenziali(chef, nuovoUsername, nuovaPassword);
    }

    /**
     * Elimina account - delega al service
     */
    public void eliminaAccount(Chef chef) 
            throws ValidationException, DataAccessException {
        if (chef == null || chef.getUsername() == null) {
            throw new ValidationException("Chef non valido");
        }
        gestioneChef.eliminaChef(chef.getUsername());
    }

    /**
     * Getter per accesso al service (se necessario per altri controller)
     */
    public GestioneChef getGestioneChef() {
        return gestioneChef;
    }
}