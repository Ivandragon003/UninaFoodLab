package controller;

import service.GestioneCorsiCucina;
import service.GestioneChef;
import model.CorsoCucina;
import model.Chef;

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

    public void setChefLoggato(Chef chef) { this.chefLoggato = chef; }
    public Chef getChefLoggato() { return chefLoggato; }

    public List<Chef> getTuttiGliChef() throws SQLException {
        return chefService.getAll();
    }

    public void creaCorso(CorsoCucina corso) throws SQLException {
        corsiService.creaCorso(corso);
    }

    public void aggiungiChefACorso(CorsoCucina corso, Chef chef, String password) throws SQLException {
        corsiService.aggiungiChefACorso(corso, chef, password);
    }

    public void eliminaCorso(int idCorso) throws SQLException {
        corsiService.cancellaCorso(idCorso);
    }

    public void modificaCorso(CorsoCucina corsoAggiornato) throws SQLException {
        corsiService.aggiornaCorso(corsoAggiornato);
    }

    public CorsoCucina getCorsoCompleto(int idCorso) throws SQLException {
        return corsiService.getCorsoCompleto(idCorso);
    }
}
