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

    public List<Chef> getTuttiGliChef() {
        try {
            return chefService.getAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public void creaCorso(CorsoCucina corso) throws ValidationException {
        Objects.requireNonNull(corso, ErrorMessages.CORSO_NULLO);
        try {
            corsiService.creaCorso(corso);
        } catch (IllegalArgumentException | SQLException e) {
            throw new ValidationException("Errore di validazione del corso: " + e.getMessage(), e);
        }
    }

    public void aggiungiChefACorso(CorsoCucina corso, Chef chef, String password) throws ValidationException {
        Objects.requireNonNull(corso, ErrorMessages.CORSO_NULLO);
        Objects.requireNonNull(chef, ErrorMessages.CHEF_NULLO);
        try {
            corsiService.aggiungiChefACorso(corso, chef, password);
        } catch (IllegalArgumentException | SQLException e) {
            throw new ValidationException("Errore di validazione: " + e.getMessage(), e);
        }
    }

    public void rimuoviChefDaCorso(CorsoCucina corso, Chef chef) {
        try {
            corsiService.rimuoviChefDaCorso(corso, chef);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void eliminaCorso(int idCorso) {
        try {
            corsiService.cancellaCorso(idCorso);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void modificaCorso(CorsoCucina corsoAggiornato) throws ValidationException {
        Objects.requireNonNull(corsoAggiornato, ErrorMessages.CORSO_NULLO);
        try {
            corsiService.aggiornaCorso(corsoAggiornato);
        } catch (IllegalArgumentException | SQLException e) {
            throw new ValidationException("Errore di validazione: " + e.getMessage(), e);
        }
    }

    public CorsoCucina getCorsoCompleto(int idCorso) {
        try {
            return corsiService.getCorsoCompleto(idCorso);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public CorsoCucina getDettagliCorso(CorsoCucina corso) {
        Objects.requireNonNull(corso, ErrorMessages.CORSO_NULLO);
        try {
            return corsiService.getCorsoCompleto(corso.getIdCorso());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
