package controller;

import exceptions.DataAccessException;
import exceptions.ValidationException;
import exceptions.ErrorMessages;
import model.CorsoCucina;
import model.Chef;
import service.GestioneCorsiCucina;
import service.GestioneCucina;
import service.GestioneRicette;
import service.GestioneSessioni;
import service.GestioneChef;

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
        if (corso == null) throw new ValidationException(ErrorMessages.CORSO_NULLO);
        
        if (chefLoggato == null) {
            throw new ValidationException("Nessun chef loggato. Impossibile creare il corso.");
        }
        
        corso.setCodfiscaleFondatore(chefLoggato.getCodFiscale());
        
        corsiService.creaCorso(corso);
        
        System.out.println("[GestioneCorsoController] ✅ Corso creato");
        System.out.println("[GestioneCorsoController] 👑 Fondatore: " + 
                         chefLoggato.getNome() + " " + chefLoggato.getCognome() +
                         " (CF: " + chefLoggato.getCodFiscale() + ")");
    }

    public void aggiungiChefACorso(CorsoCucina corso, Chef chef, String password) throws ValidationException {
        corsiService.aggiungiChefACorso(corso, chef, password);
    }

    public void rimuoviChefDaCorso(CorsoCucina corso, Chef chef) throws ValidationException, DataAccessException {
        if (chef == null) throw new ValidationException(ErrorMessages.CHEF_NULLO);
        if (corso == null) throw new ValidationException(ErrorMessages.CORSO_NULLO);

        corsiService.rimuoviChefDaCorso(chef, corso);
    }

    public void eliminaCorso(int idCorso) {
        corsiService.cancellaCorso(idCorso);
    }

    public void modificaCorso(CorsoCucina corsoAggiornato) throws ValidationException {
        if (corsoAggiornato == null) throw new ValidationException(ErrorMessages.CORSO_NULLO);
        corsiService.aggiornaCorso(corsoAggiornato);
    }

    public CorsoCucina getCorsoCompleto(int idCorso) {
        return corsiService.getCorsoCompleto(idCorso);
    }

    public CorsoCucina getDettagliCorso(CorsoCucina corso) throws ValidationException {
        if (corso == null) throw new ValidationException(ErrorMessages.CORSO_NULLO);
        return corsiService.getCorsoCompleto(corso.getIdCorso());
    }
    
}
