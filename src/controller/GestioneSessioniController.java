package controller;

import model.CorsoCucina;
import model.Sessione;

import java.util.List;

public class GestioneSessioniController {

    private final CorsoCucina corso;

    public GestioneSessioniController(CorsoCucina corso) {
        this.corso = corso;
    }

    public List<Sessione> getSessioni() {
        return corso.getSessioni();
    }

    public void aggiungiSessione(Sessione s) {
        corso.getSessioni().add(s);
        corso.setNumeroSessioni(corso.getSessioni().size());
    }

    public void eliminaSessione(Sessione s) {
        corso.getSessioni().remove(s);
        corso.setNumeroSessioni(corso.getSessioni().size());
    }

    public void aggiornaSessione(Sessione oldS, Sessione newS) {
        int idx = corso.getSessioni().indexOf(oldS);
        if (idx >= 0) {
            corso.getSessioni().set(idx, newS);
        }
    }

    public boolean verificaNumeroSessioni() {
        return corso.getSessioni().size() == corso.getNumeroSessioni();
    }
}
