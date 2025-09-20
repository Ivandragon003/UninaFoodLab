package controller;

import model.Chef;
import model.CorsoCucina;
import service.GestioneCorsiCucina;
import service.GestioneChef;
import java.sql.SQLException;
import java.util.List;

public class GestioneCorsoController {

    private final GestioneCorsiCucina corsiService;
    private final GestioneChef gestioneChef; 

    public GestioneCorsoController(GestioneCorsiCucina corsiService, GestioneChef gestioneChef) {
        this.corsiService = corsiService;
        this.gestioneChef = gestioneChef; 
    }

    public void modificaCorso(CorsoCucina corsoAggiornato) throws SQLException {
        corsiService.aggiornaCorso(corsoAggiornato);
    }

    public void eliminaCorso(int idCorso) throws SQLException {
        corsiService.cancellaCorso(idCorso);
    }

    public CorsoCucina getCorsoById(int idCorso) throws SQLException {
        return corsiService.getCorsoById(idCorso);
    }

  
    public List<Chef> getTuttiGliChef() throws SQLException {
        return gestioneChef.getAll();
    }
}
