package controller;

import model.Ricetta;
import service.GestioneRicette;

import java.sql.SQLException;

public class GestioneRicettaController {

    private final GestioneRicette gestioneRicette;

    public GestioneRicettaController(GestioneRicette gestioneRicette) {
        this.gestioneRicette = gestioneRicette;
    }

    public void aggiornaRicetta(int id, Ricetta r) {
        try {
            gestioneRicette.aggiornaRicetta(id, r);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cancellaRicetta(int id) {
        try {
            gestioneRicette.cancellaRicetta(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public GestioneRicette getGestioneRicette() {
        return gestioneRicette;
    }
    
    
}
