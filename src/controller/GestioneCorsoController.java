package controller;

import exceptions.DataAccessException;
import exceptions.ErrorMessages;
import exceptions.ValidationException;
import model.CorsoCucina;
import model.Chef;
import service.GestioneCorsiCucina;
import service.GestioneChef;

import java.sql.SQLException;
import java.util.List;

public class GestioneCorsoController {

    private final GestioneCorsiCucina corsiService;
    private final GestioneChef chefService;
    private Chef chefLoggato;

    public GestioneCorsoController(GestioneCorsiCucina corsiService, GestioneChef chefService) {
        this.corsiService = corsiService;
        this.chefService = chefService;
    }

    public void setChefLoggato(Chef chef) {
        this.chefLoggato = chef;
    }

    public Chef getChefLoggato() {
        return chefLoggato;
    }

    public List<Chef> getTuttiGliChef() {
        return chefService.getAll();
    }

    public void creaCorso(CorsoCucina corso) throws ValidationException {
        if (corso == null) {
            throw new ValidationException(ErrorMessages.CORSO_NULLO);
        }
        
        try {
            corsiService.creaCorso(corso);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Errore di validazione del corso: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new DataAccessException(ErrorMessages.ERRORE_SALVATAGGIO, e);
        }
    }

    public void aggiungiChefACorso(CorsoCucina corso, Chef chef, String password) 
            throws ValidationException {
        if (corso == null) {
            throw new ValidationException(ErrorMessages.CORSO_NULLO);
        }
        if (chef == null) {
            throw new ValidationException(ErrorMessages.CHEF_NULLO);
        }
        
        try {
            corsiService.aggiungiChefACorso(corso, chef, password);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Errore di validazione: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new DataAccessException(ErrorMessages.ERRORE_SALVATAGGIO, e);
        }
    }

    public void rimuoviChefDaCorso(CorsoCucina corso, Chef chef) {
        try {
            corsiService.rimuoviChefDaCorso(corso, chef);
        } catch (SQLException e) {
            throw new DataAccessException(ErrorMessages.ERRORE_ELIMINAZIONE, e);
        }
    }

    public void eliminaCorso(int idCorso) {
        try {
            corsiService.cancellaCorso(idCorso);
        } catch (SQLException e) {
            throw new DataAccessException(ErrorMessages.ERRORE_ELIMINAZIONE, e);
        }
    }

    public void modificaCorso(CorsoCucina corsoAggiornato) throws ValidationException {
        if (corsoAggiornato == null) {
            throw new ValidationException(ErrorMessages.CORSO_NULLO);
        }
        
        try {
            corsiService.aggiornaCorso(corsoAggiornato);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Errore di validazione: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new DataAccessException(ErrorMessages.ERRORE_AGGIORNAMENTO, e);
        }
    }

    public CorsoCucina getCorsoCompleto(int idCorso) {
        try {
            return corsiService.getCorsoCompleto(idCorso);
        } catch (SQLException e) {
            throw new DataAccessException(ErrorMessages.ERRORE_LETTURA, e);
        }
    }


    public CorsoCucina getDettagliCorso(CorsoCucina corso) throws ValidationException {
        if (corso == null) {
            throw new ValidationException(ErrorMessages.CORSO_NULLO);
        }
        
        try {
            return corsiService.getCorsoCompleto(corso.getIdCorso());
        } catch (SQLException e) {
            throw new DataAccessException(ErrorMessages.ERRORE_LETTURA, e);
        }
    }
}