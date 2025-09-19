package controller;

import model.CorsoCucina;
import service.GestioneCorsiCucina;

import java.sql.SQLException;

public class GestioneCorsoController {

    private final GestioneCorsiCucina corsiService;

    public GestioneCorsoController(GestioneCorsiCucina corsiService) {
        this.corsiService = corsiService;
    }

    // Aggiorna un corso
    public void modificaCorso(CorsoCucina corsoAggiornato) throws SQLException {
        corsiService.aggiornaCorso(corsoAggiornato);
    }

    // Elimina un corso
    public void eliminaCorso(int idCorso) throws SQLException {
        corsiService.cancellaCorso(idCorso);
    }

    // Recupera dettagli di un corso per id
    public CorsoCucina getCorsoById(int idCorso) throws SQLException {
        return corsiService.getCorsoById(idCorso);
    }

}
