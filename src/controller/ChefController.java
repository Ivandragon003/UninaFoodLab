package controller;

import model.Chef;
import service.GestioneChef;
import exceptions.ValidationException;
import exceptions.DataAccessException;

import java.time.LocalDate;
import java.util.List;

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

    public Chef login(String username, String password) 
            throws ValidationException, DataAccessException {
        return gestioneChef.login(username, password);
    }

    public Chef registraChef(String codFiscale, String nome, String cognome, 
                            String email, LocalDate dataNascita, boolean disponibilita, 
                            String username, String password) 
            throws ValidationException, DataAccessException {
        return gestioneChef.creaChef(codFiscale, nome, cognome, email, 
                                     dataNascita, disponibilita, username, password);
    }

    public void aggiornaCredenziali(Chef chef, String nuovoUsername, String nuovaPassword) 
            throws ValidationException, DataAccessException {
        gestioneChef.aggiornaCredenziali(chef, nuovoUsername, nuovaPassword);
    }

    public void eliminaAccount(Chef chef) 
            throws ValidationException, DataAccessException {
        if (chef == null) {
            throw new ValidationException("Chef non valido");
        }
        gestioneChef.eliminaChef(chef.getUsername());
    }

    public GestioneChef getGestioneChef() {
        return gestioneChef;
    }

    /**
     * Ritorna la lista di tutti gli chef (delegato al service).
     */
    public List<Chef> getAllChef() throws DataAccessException {
        return gestioneChef.getAll();
    }
}
