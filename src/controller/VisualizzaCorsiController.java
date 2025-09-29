package controller;

import model.CorsoCucina;
import model.Chef;
import service.GestioneCorsiCucina;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

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
        return corsiService.getCorsi(); 
    }

    public List<CorsoCucina> getCorsiChefLoggato() throws SQLException {
        return getTuttiICorsi().stream()
                .filter(c -> c.getChef().contains(chefLoggato))
                .collect(Collectors.toList());
    }

    public List<CorsoCucina> cercaPerNomeOCategoria(String filtro) throws SQLException {
        return corsiService.cercaPerNomeOCategoria(filtro);
    }

    // Numero di sessioni per un corso specifico
    public int getNumeroSessioniPerCorso(int idCorso) throws SQLException {
        return corsiService.getNumeroSessioniPerCorso(idCorso);
    }

    // Metodo di utilità per stampare corsi in console
    public void visualizzaCorsi(List<CorsoCucina> corsi) {
        corsi.forEach(c -> System.out.println(c.toStringNomeCorso() + " | ID: " + c.getIdCorso()));
    }
}
