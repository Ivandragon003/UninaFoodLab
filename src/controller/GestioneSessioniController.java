package controller;

import model.CorsoCucina;
import model.Sessione;

import java.util.List;

public class GestioneSessioniController {

    private CorsoCucina corso;

    public GestioneSessioniController(CorsoCucina corso) {
        this.corso = corso;
    }

    public List<Sessione> getSessioni() {
        return corso.getSessioni();
    }

    public void aggiungiSessione(Sessione s) {
        corso.getSessioni().add(s);
    }

    public void eliminaSessione(Sessione s) {
        corso.getSessioni().remove(s);
    }

    public boolean verificaNumeroSessioni() {
        return corso.getSessioni().size() == corso.getNumeroSessioni();
    }
}
