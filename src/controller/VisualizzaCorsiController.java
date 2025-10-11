package controller;

import model.CorsoCucina;
import model.Chef;
import service.GestioneCorsiCucina;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
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
        return corsiService.getCorsi();
    }

    public List<CorsoCucina> getCorsiChefLoggato() throws SQLException {
    if (chefLoggato == null) {
        return Collections.emptyList();
    }
    
    String cfChefLoggato = chefLoggato.getCodFiscale();
   
    
    List<CorsoCucina> tuttiICorsi = getTuttiICorsi();
    
    List<CorsoCucina> corsiChef = tuttiICorsi.stream()
        .filter(c -> {
            boolean isFondatore = c.getCodfiscaleFondatore() != null && 
                                 c.getCodfiscaleFondatore().equalsIgnoreCase(cfChefLoggato.trim());
       
            
            return isFondatore;
        })
        .collect(Collectors.toList());
    
    
   
    return corsiChef;
}




    public List<CorsoCucina> cercaPerNomeOCategoria(String filtro) throws SQLException {
        return corsiService.cercaPerNomeOCategoria(filtro);
    }

    public int getNumeroSessioniPerCorso(int idCorso) throws SQLException {
        return corsiService.getNumeroSessioniPerCorso(idCorso);
    }

    public void visualizzaCorsi(List<CorsoCucina> corsi) {
        corsi.forEach(c -> System.out.println(c.toStringNomeCorso() + " | ID: " + c.getIdCorso()));
    }
}
