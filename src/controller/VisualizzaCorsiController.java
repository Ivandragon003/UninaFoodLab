package controller;

import model.CorsoCucina;
import model.Chef;
import service.GestioneCorsiCucina;
import exceptions.DataAccessException;
import java.sql.SQLException;
import java.util.List;
import java.util.Collections;

public class VisualizzaCorsiController {

    private final GestioneCorsiCucina corsiService;
    private final Chef chefLoggato;

    public VisualizzaCorsiController(GestioneCorsiCucina corsiService, Chef chefLoggato) {
        this.corsiService = corsiService;
        this.chefLoggato = chefLoggato;
    }

    public Chef getChefLoggato() {
        return chefLoggato;
    }

    public GestioneCorsiCucina getGestioneCorsi() {
        return corsiService;
    }

    public List<CorsoCucina> getTuttiICorsi() throws SQLException {
        try {
            return corsiService.getCorsi();
        } catch (DataAccessException e) {
            throw new SQLException("Errore caricamento corsi", e);
        }
    }

    public List<CorsoCucina> getCorsiDelChef() throws DataAccessException {
        try {
            return getCorsiChefLoggato();
        } catch (SQLException e) {
            throw new DataAccessException("Errore nel caricamento dei corsi: " + e.getMessage(), e);
        }
    }

    public List<CorsoCucina> visualizzaCorsiChef() throws DataAccessException {
        return getCorsiDelChef();
    }

    public List<CorsoCucina> getCorsiChefLoggato() throws SQLException {
        if (chefLoggato == null) {
            return Collections.emptyList();
        }

        try {
            return corsiService.getCorsiByChef(chefLoggato);
        } catch (DataAccessException e) {
            throw new SQLException("Errore caricamento corsi chef", e);
        }
    }

    public List<CorsoCucina> cercaPerNomeOCategoria(String filtro) throws SQLException {
        try {
            return corsiService.cercaPerNomeOCategoria(filtro);
        } catch (DataAccessException e) {
            throw new SQLException("Errore ricerca corsi", e);
        }
    }

    public int getNumeroSessioniPerCorso(int idCorso) throws SQLException {
        try {
            return corsiService.getNumeroSessioniPerCorso(idCorso);
        } catch (DataAccessException e) {
            throw new SQLException("Errore conteggio sessioni", e);
        }
    }

    public void visualizzaCorsi(List<CorsoCucina> corsi) {
        corsi.forEach(c -> System.out.println(c.toStringNomeCorso() + " | ID: " + c.getIdCorso()));
    }
}
