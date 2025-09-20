package controller;

import model.Ricetta;
import model.Sessione;
import model.InPresenza;
import model.Ingrediente;
import service.GestioneRicette;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class RicettaController {

    private final GestioneRicette gestioneRicette;

    public RicettaController(GestioneRicette gestioneRicette) {
        this.gestioneRicette = gestioneRicette;
    }

    // 1. Crea una nuova ricetta
    public void creaRicetta(String nome, int tempoPreparazione, Map<Ingrediente, Double> ingredienti) throws SQLException {
        Ricetta r = new Ricetta(nome, tempoPreparazione);
        r.setIngredienti(ingredienti);
        gestioneRicette.creaRicetta(r);
    }

    // 2. Modifica ricetta esistente
    public void modificaRicetta(int idRicetta, String nuovoNome, int nuovoTempo, Map<Ingrediente, Double> nuoviIngredienti) throws SQLException {
        Ricetta r = new Ricetta(nuovoNome, nuovoTempo);
        r.setIngredienti(nuoviIngredienti);
        gestioneRicette.aggiornaRicetta(idRicetta, r);
    }

    // 3. Elimina ricetta
    public void eliminaRicetta(int idRicetta) throws SQLException {
        gestioneRicette.cancellaRicetta(idRicetta);
    }

    // 4. Recupera tutte le ricette
    public List<Ricetta> visualizzaTutteRicette() throws SQLException {
        return gestioneRicette.getAllRicette();
    }

    // 5. Recupera ricetta per id
    public Ricetta visualizzaRicettaPerId(int idRicetta) throws SQLException {
        return gestioneRicette.getAllRicette().stream()
                .filter(r -> r.getIdRicetta() == idRicetta)
                .findFirst()
                .orElse(null);
    }

    // 6. Associa una ricetta a una sessione (solo sessioni in presenza)
    public void associaRicettaASessione(Ricetta ricetta, Sessione sessione) {
        if (sessione instanceof InPresenza inPresenza) {
            inPresenza.getRicette().add(ricetta);
            ricetta.getSessioni().add(inPresenza);
        } else {
            throw new IllegalArgumentException("Non è possibile associare ricette a sessioni online");
        }
    }

    // 7. Rimuove associazione ricetta-sessione
    public void rimuoviRicettaDaSessione(Ricetta ricetta, Sessione sessione) {
        if (sessione instanceof InPresenza inPresenza) {
            inPresenza.getRicette().remove(ricetta);
            ricetta.getSessioni().remove(inPresenza);
        }
    }

    // 8. Aggiorna ingredienti di una ricetta
    public void aggiornaIngredienti(Ricetta ricetta, Map<Ingrediente, Double> nuoviIngredienti) {
        ricetta.setIngredienti(nuoviIngredienti);
        // eventuale persistenza sul DB tramite DAO
    }
}
