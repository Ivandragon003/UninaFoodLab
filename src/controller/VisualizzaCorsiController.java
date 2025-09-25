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

    // Corsi leggeri (senza iscritti e sessioni)
    public List<CorsoCucina> getTuttiICorsi() throws SQLException {
        return corsiService.getCorsi();
    }

    // Corsi completi (con iscritti e sessioni)
    public List<CorsoCucina> getTuttiICorsiCompleti() throws SQLException {
        return corsiService.getTuttiICorsiCompleti();
    }

    // Corsi del chef loggato (da tutti i corsi completi)
    public List<CorsoCucina> getCorsiChefLoggato() throws SQLException {
        return getTuttiICorsiCompleti().stream()
                .filter(c -> c.getChef().contains(chefLoggato))
                .collect(Collectors.toList());
    }


    // Ricerca per categoria / argomento del corso
    public List<CorsoCucina> cercaPerNomeOCategoria(String filtro) throws SQLException {
        return corsiService.cercaPerNomeOCategoria(filtro);
    }

}
