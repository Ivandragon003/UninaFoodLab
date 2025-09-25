package controller;

import model.CorsoCucina;
import model.Chef;
import service.GestioneCorsiCucina;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
public class VisualizzaCorsiController {

    private final GestioneCorsiCucina corsiService;
    private final Chef chefLoggato;

    private List<CorsoCucina> cachedCorsi = new ArrayList<>();

    public VisualizzaCorsiController(GestioneCorsiCucina corsiService, Chef chefLoggato) {
        this.corsiService = corsiService;
        this.chefLoggato = chefLoggato;
    }

    public List<CorsoCucina> getTuttiICorsi() throws SQLException {
        if (cachedCorsi.isEmpty()) {
            cachedCorsi = corsiService.getCorsiLeggeri(); 
        }
        return cachedCorsi;
    }

    public List<CorsoCucina> getCorsiChefLoggato() throws SQLException {
        return getTuttiICorsi().stream()
                .filter(c -> c.getChef().contains(chefLoggato))
                .collect(Collectors.toList());
    }


    public List<CorsoCucina> cercaPerNomeChef(String nomeChef) throws SQLException {
        return getTuttiICorsi().stream()
                .filter(c -> c.getChef().stream()
                        .anyMatch(ch -> ch.getNome().toLowerCase().contains(nomeChef.toLowerCase())))
                .collect(Collectors.toList());
    }

    public List<CorsoCucina> cercaPerCategoria(String categoria) throws SQLException {
        return getTuttiICorsi().stream()
                .filter(c -> c.getArgomento().toLowerCase().contains(categoria.toLowerCase()))
                .collect(Collectors.toList());
    }
}

