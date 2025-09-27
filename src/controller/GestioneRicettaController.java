package controller;

import model.Ricetta;
import service.GestioneRicette;

import java.sql.SQLException;

public class GestioneRicettaController {

    private final GestioneRicette gestioneRicette;

    public GestioneRicettaController(GestioneRicette gestioneRicette) {
        this.gestioneRicette = gestioneRicette;
    }


    public void aggiornaRicetta(int id, Ricetta r) throws SQLException {
        gestioneRicette.aggiornaRicetta(id, r);
    }

    public void cancellaRicetta(int id) throws SQLException {
        gestioneRicette.cancellaRicetta(id);
    }

    public GestioneRicette getGestioneRicette() {
        return gestioneRicette;
    }
}
