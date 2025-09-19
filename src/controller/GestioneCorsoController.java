package controller;

import model.CorsoCucina;
import service.GestioneCorsiCucina;
import java.sql.SQLException;

public class GestioneCorsoController {

    private final GestioneCorsiCucina corsiService;

    public GestioneCorsoController(GestioneCorsiCucina corsiService) {
        this.corsiService = corsiService;
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
}
