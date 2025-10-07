package controller;

import model.Ricetta;
import model.Ingrediente;
import model.InPresenza;
import service.GestioneRicette;
import exceptions.ValidationException;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Controller per la gestione delle ricette
 * Coordina le operazioni tra GUI e Service, gestisce la cache
 */
public class RicettaController {

    private final GestioneRicette gestioneRicette;
    private List<Ricetta> cachedRicette = null;

    public RicettaController(GestioneRicette gestioneRicette) {
        this.gestioneRicette = gestioneRicette;
    }

    // ==================== CRUD ====================

    /**
     * Crea una nuova ricetta con validazione completa
     * @return La ricetta creata con ID assegnato
     * @throws SQLException se errore database
     * @throws ValidationException se i dati non sono validi
     */
    public Ricetta creaRicetta(String nome, int tempoPreparazione, Map<Ingrediente, Double> ingredienti) 
            throws SQLException, ValidationException {
        
        // Delega tutta la validazione al Service
        Ricetta ricetta = new Ricetta(nome, tempoPreparazione);
        ricetta.setIngredienti(ingredienti);
        
        gestioneRicette.creaRicetta(ricetta);
        invalidaCache();
        
        return ricetta;
    }

    /**
     * Aggiorna una ricetta esistente
     * @throws SQLException se errore database
     * @throws ValidationException se i dati non sono validi o la ricetta non esiste
     */
    public void aggiornaRicetta(int idRicetta, String nuovoNome, int nuovoTempo, 
            Map<Ingrediente, Double> nuoviIngredienti) throws SQLException, ValidationException {
        
        Ricetta ricetta = new Ricetta(nuovoNome, nuovoTempo);
        ricetta.setIngredienti(nuoviIngredienti);
        
        gestioneRicette.aggiornaRicetta(idRicetta, ricetta);
        invalidaCache();
    }

    /**
     * Elimina una ricetta
     * @throws SQLException se errore database
     * @throws ValidationException se la ricetta non esiste o è in uso
     */
    public void eliminaRicetta(int idRicetta) throws SQLException, ValidationException {
        gestioneRicette.cancellaRicetta(idRicetta);
        invalidaCache();
    }

    // ==================== QUERY ====================

    /**
     * Ottiene tutte le ricette (con cache)
     * @return Lista immutabile delle ricette
     */
    public List<Ricetta> getAllRicette() throws SQLException {
        if (cachedRicette == null) {
            cachedRicette = gestioneRicette.getAllRicette();
        }
        return List.copyOf(cachedRicette);
    }

    /**
     * Cerca una ricetta per ID
     * @return La ricetta trovata, o null se non esiste
     */
    public Ricetta getRicettaPerId(int idRicetta) throws SQLException {
        return getAllRicette().stream()
                .filter(r -> r.getIdRicetta() == idRicetta)
                .findFirst()
                .orElse(null);
    }

    /**
     * Cerca ricette per nome (ricerca parziale)
     */
    public List<Ricetta> cercaPerNome(String nome) throws SQLException, ValidationException {
        return gestioneRicette.cercaPerNome(nome, getAllRicette());
    }

    /**
     * Filtra le ricette con criteri combinati
     * @param nome Nome da cercare (parziale, opzionale)
     * @param tempoMin Tempo minimo preparazione (opzionale)
     * @param tempoMax Tempo massimo preparazione (opzionale)
     * @param ingredientiMin Numero minimo ingredienti (opzionale)
     * @param ingredientiMax Numero massimo ingredienti (opzionale)
     * @throws ValidationException se i range non sono validi (min > max)
     */
    public List<Ricetta> filtraCombinato(String nome, Integer tempoMin, Integer tempoMax,
            Integer ingredientiMin, Integer ingredientiMax) throws SQLException, ValidationException {
        
        return gestioneRicette.filtraCombinato(nome, tempoMin, tempoMax, 
                ingredientiMin, ingredientiMax, getAllRicette());
    }

    // ==================== GESTIONE INGREDIENTI ====================

    /**
     * Aggiunge un ingrediente a una ricetta esistente
     * @throws ValidationException se l'ingrediente è già presente o i dati non sono validi
     */
    public void aggiungiIngrediente(Ricetta ricetta, Ingrediente ingrediente, double quantita) 
            throws SQLException, ValidationException {
        
        gestioneRicette.aggiungiIngrediente(ricetta, ingrediente, quantita);
        invalidaCache();
    }

    /**
     * Aggiorna la quantità di un ingrediente in una ricetta
     * @throws ValidationException se l'ingrediente non è presente o la quantità non è valida
     */
    public void aggiornaQuantitaIngrediente(Ricetta ricetta, Ingrediente ingrediente, double nuovaQuantita) 
            throws SQLException, ValidationException {
        
        gestioneRicette.aggiornaQuantitaIngrediente(ricetta, ingrediente, nuovaQuantita);
        invalidaCache();
    }

    /**
     * Rimuove un ingrediente da una ricetta
     * @throws ValidationException se l'ingrediente non è presente nella ricetta
     */
    public void rimuoviIngrediente(Ricetta ricetta, Ingrediente ingrediente) 
            throws SQLException, ValidationException {
        
        gestioneRicette.rimuoviIngrediente(ricetta, ingrediente);
        invalidaCache();
    }

    // ==================== ASSOCIAZIONI SESSIONI ====================

    /**
     * Associa una ricetta a una sessione in presenza
     * @throws ValidationException se i parametri sono null
     */
    public void associaRicettaASessione(Ricetta ricetta, InPresenza sessione) 
            throws ValidationException {
        
        gestioneRicette.associaRicettaASessione(ricetta, sessione);
    }

    /**
     * Rimuove l'associazione tra una ricetta e una sessione
     */
    public void disassociaRicettaDaSessione(Ricetta ricetta, InPresenza sessione) {
        gestioneRicette.disassociaRicettaDaSessione(ricetta, sessione);
    }

    /**
     * Ottiene le ricette non ancora associate a una sessione
     */
    public List<Ricetta> getRicetteNonAssociate(InPresenza sessione) throws SQLException {
        return gestioneRicette.getRicetteNonAssociate(sessione, getAllRicette());
    }

    // ==================== GESTIONE CACHE ====================

    /**
     * Invalida la cache forzando il ricaricamento al prossimo accesso
     */
    public void invalidaCache() {
        cachedRicette = null;
    }

    /**
     * Ricarica immediatamente la cache
     */
    public void ricaricaCache() throws SQLException {
        invalidaCache();
        getAllRicette();
    }

    // ==================== ACCESSO SERVICE ====================

    /**
     * Fornisce accesso diretto al service per operazioni avanzate
     */
    public GestioneRicette getGestioneRicette() {
        return gestioneRicette;
    }
}