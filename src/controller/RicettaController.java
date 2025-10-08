package controller;

import model.Ricetta;
import model.Ingrediente;
import model.InPresenza;
import service.GestioneRicette;
import service.GestioneUsa;
import service.GestioneCucina;
import exceptions.ValidationException;
import exceptions.DataAccessException;

import java.util.List;
import java.util.Map;

/**
 * Controller per la gestione delle ricette
 * Coordina le operazioni tra GUI e Service, gestisce la cache
 */
public class RicettaController {

    private final GestioneRicette gestioneRicette;
    private final GestioneUsa gestioneUsa;
    private final GestioneCucina gestioneCucina;
    private List<Ricetta> cachedRicette = null;

    public RicettaController(GestioneRicette gestioneRicette,
                             GestioneUsa gestioneUsa,
                             GestioneCucina gestioneCucina) {
        this.gestioneRicette = gestioneRicette;
        this.gestioneUsa = gestioneUsa;
        this.gestioneCucina = gestioneCucina;
    }

    // ==================== CRUD ====================

    public Ricetta creaRicetta(String nome, int tempoPreparazione, Map<Ingrediente, Double> ingredienti)
            throws ValidationException, DataAccessException {

        Ricetta ricetta = new Ricetta(nome, tempoPreparazione);
        ricetta.setIngredienti(ingredienti);

        gestioneRicette.creaRicetta(ricetta);
        invalidaCache();

        return ricetta;
    }

    public void aggiornaRicetta(int idRicetta, String nuovoNome, int nuovoTempo,
                                Map<Ingrediente, Double> nuoviIngredienti) throws ValidationException, DataAccessException {

        Ricetta ricetta = new Ricetta(nuovoNome, nuovoTempo);
        ricetta.setIngredienti(nuoviIngredienti);

        gestioneRicette.aggiornaRicetta(idRicetta, ricetta);
        invalidaCache();
    }

    public void eliminaRicetta(int idRicetta) throws ValidationException, DataAccessException {
        gestioneRicette.cancellaRicetta(idRicetta);
        invalidaCache();
    }

    // ==================== QUERY ====================

    public List<Ricetta> getAllRicette() throws DataAccessException {
        if (cachedRicette == null) {
            cachedRicette = gestioneRicette.getAllRicette();
        }
        return List.copyOf(cachedRicette);
    }

    public Ricetta getRicettaPerId(int idRicetta) throws DataAccessException {
        return getAllRicette().stream()
                .filter(r -> r.getIdRicetta() == idRicetta)
                .findFirst()
                .orElse(null);
    }

    public List<Ricetta> cercaPerNome(String nome) throws ValidationException, DataAccessException {
        return gestioneRicette.cercaPerNome(nome, getAllRicette());
    }

    public List<Ricetta> filtraCombinato(String nome, Integer tempoMin, Integer tempoMax,
                                         Integer ingredientiMin, Integer ingredientiMax) throws ValidationException, DataAccessException {

        return gestioneRicette.filtraCombinato(nome, tempoMin, tempoMax,
                ingredientiMin, ingredientiMax, getAllRicette());
    }

    // ==================== OPERAZIONI SUGLI INGREDIENTI (DELEGA A GestioneUsa) ====================

    public void aggiungiIngrediente(Ricetta ricetta, Ingrediente ingrediente, double quantita)
            throws ValidationException, DataAccessException {

        gestioneUsa.aggiungiIngredienteARicetta(ricetta, ingrediente, quantita);
        invalidaCache();
    }

    public void aggiornaQuantitaIngrediente(Ricetta ricetta, Ingrediente ingrediente, double nuovaQuantita)
            throws ValidationException, DataAccessException {

        gestioneUsa.aggiornaQuantitaIngrediente(ricetta, ingrediente, nuovaQuantita);
        invalidaCache();
    }

    public void rimuoviIngrediente(Ricetta ricetta, Ingrediente ingrediente)
            throws ValidationException, DataAccessException {

        gestioneUsa.rimuoviIngredienteDaRicetta(ricetta, ingrediente);
        invalidaCache();
    }

    // ==================== ASSOCIAZIONI SESSIONI (DELEGA A GestioneCucina) ====================

    public void associaRicettaASessione(Ricetta ricetta, InPresenza sessione)
            throws ValidationException, DataAccessException {

        gestioneCucina.aggiungiSessioneARicetta(ricetta, sessione);
    }

    public void disassociaRicettaDaSessione(Ricetta ricetta, InPresenza sessione)
            throws ValidationException, DataAccessException {

        gestioneCucina.rimuoviSessioneDaRicetta(ricetta, sessione);
    }

    /**
     * Restituisce le ricette non ancora associate alla sessione (calcolo locale per evitare dipendenze service mancanti)
     */
    public List<Ricetta> getRicetteNonAssociate(InPresenza sessione) throws DataAccessException {
        return getAllRicette().stream()
                .filter(r -> r.getSessioni() == null || !r.getSessioni().contains(sessione))
                .toList();
    }

    // CACHE
    public void invalidaCache() {
        cachedRicette = null;
    }

    public void ricaricaCache() throws DataAccessException {
        invalidaCache();
        getAllRicette();
    }

    public GestioneRicette getGestioneRicette() {
        return gestioneRicette;
    }
}
