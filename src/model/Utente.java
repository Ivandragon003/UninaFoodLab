package model;

import java.util.ArrayList;
import java.util.List;

public class Utente extends Persona {
    private List<Adesione> adesioniUtente; // lista delle adesioni dell’utente

    public Utente(String codFiscale, String nome, String cognome) {
        super(codFiscale, nome, cognome);
        this.adesioniUtente = new ArrayList<>();
    }

    public List<Adesione> getAdesioniUtente() {
        return adesioniUtente;
    }

    public void aggiungiAdesione(Adesione adesione) {
        if (!adesioniUtente.contains(adesione)) {
            adesioniUtente.add(adesione);
        }
    }
}
