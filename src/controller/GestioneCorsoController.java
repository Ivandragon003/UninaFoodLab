package controller;

import exceptions.ErrorMessages;
import exceptions.ValidationException;
import model.CorsoCucina;
import model.Chef;
import service.GestioneCorsiCucina;
import service.GestioneChef;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;



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
    

    public List<Chef> getTuttiGliChef() throws SQLException {
        return chefService.getAll();
    }

      // Se il model lancia IllegalArgumentException (invarianti non rispettate),
      //lo trasformiamo in ValidationException per mantenere la semantica.
     
    public void creaCorso(CorsoCucina corso) throws SQLException, ValidationException {
        Objects.requireNonNull(corso, ErrorMessages.CORSO_NULLO);
        try {
            corsiService.creaCorso(corso);
        } catch (IllegalArgumentException iae) {
            throw new ValidationException("Errore di validazione del corso: " + iae.getMessage(), iae);
        }
    }

    public void aggiungiChefACorso(CorsoCucina corso, Chef chef, String password) throws SQLException, ValidationException {
        Objects.requireNonNull(corso, ErrorMessages.CORSO_NULLO);
        Objects.requireNonNull(chef, ErrorMessages.CHEF_NULLO);
        try {
            corsiService.aggiungiChefACorso(corso, chef, password);
        } catch (IllegalArgumentException iae) {
            throw new ValidationException("Errore di validazione: " + iae.getMessage(), iae);
        }
    }
    
    public void rimuoviChefDaCorso(CorsoCucina corso, Chef chef) throws SQLException {
        corsiService.rimuoviChefDaCorso(corso, chef);
    }

    public void eliminaCorso(int idCorso) throws SQLException {
        corsiService.cancellaCorso(idCorso);
    }

    public void modificaCorso(CorsoCucina corsoAggiornato) throws SQLException, ValidationException {
        Objects.requireNonNull(corsoAggiornato, ErrorMessages.CORSO_NULLO);
        try {
            corsiService.aggiornaCorso(corsoAggiornato);
        } catch (IllegalArgumentException iae) {
            throw new ValidationException("Errore di validazione: " + iae.getMessage(), iae);
        }
    }

    public CorsoCucina getCorsoCompleto(int idCorso) throws SQLException {
        return corsiService.getCorsoCompleto(idCorso);
    }
    
    public CorsoCucina getDettagliCorso(CorsoCucina corso) throws SQLException {
        Objects.requireNonNull(corso, ErrorMessages.CORSO_NULLO);
        return corsiService.getCorsoCompleto(corso.getIdCorso());
    }
}
