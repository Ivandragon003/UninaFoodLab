package controller;

import model.CorsoCucina;
import model.Chef;
import service.GestioneCorsiCucina;
import dao.CucinaDAO;


import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class VisualizzaCorsiController {

	 private final GestioneCorsiCucina corsiService;
	    private final Chef chefLoggato;
	    private final CucinaDAO cucinaDAO; // aggiungi l'istanza

	    public VisualizzaCorsiController(GestioneCorsiCucina corsiService, Chef chefLoggato) {
	        this.corsiService = corsiService;
	        this.chefLoggato = chefLoggato;
	        this.cucinaDAO = new CucinaDAO(); // inizializza
	    }

    // Mostra tutti i corsi
    public List<CorsoCucina> getTuttiICorsi() throws SQLException {
        return corsiService.getAllCorsi();
    }

    // Mostra solo i corsi tenuti dallo chef loggato
    public List<CorsoCucina> getCorsiChefLoggato() throws SQLException {
        return corsiService.getCorsiByChef(chefLoggato);
    }

    // Ricerca per nome corso
    public List<CorsoCucina> cercaPerNomeCorso(String nome) throws SQLException {
        return corsiService.getAllCorsi().stream()
                .filter(c -> c.getNomeCorso().toLowerCase().contains(nome.toLowerCase()))
                .collect(Collectors.toList());
    }

    // Ricerca per nome chef
    public List<CorsoCucina> cercaPerNomeChef(String nomeChef) throws SQLException {
        return corsiService.getAllCorsi().stream()
                .filter(c -> c.getChef().stream()
                        .anyMatch(ch -> ch.getNome().toLowerCase().contains(nomeChef.toLowerCase())))
                .collect(Collectors.toList());
    }

    // Ricerca per categoria
    public List<CorsoCucina> cercaPerCategoria(String categoria) throws SQLException {
        return corsiService.getAllCorsi().stream()
                .filter(c -> c.getArgomento().toLowerCase().contains(categoria.toLowerCase()))
                .collect(Collectors.toList());
    }
    public int getNumeroRicettePerCorso(CorsoCucina corso) {
        int count = 0;
        try {
            for (var sessione : corso.getSessioni()) {
                count += cucinaDAO.getRicettePerSessione(sessione.getIdSessione()).size();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
}

