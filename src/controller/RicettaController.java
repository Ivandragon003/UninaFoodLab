package controller;

import model.Ricetta;
import model.Ingrediente;
import model.InPresenza;
import service.GestioneRicette;
import exceptions.ValidationException;
import exceptions.ErrorMessages;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller principale per la gestione delle ricette.
 * Centralizza TUTTA la logica business relativa alle ricette.
 */
public class RicettaController {

    private final GestioneRicette gestioneRicette;
    private List<Ricetta> cachedRicette = null;

    public RicettaController(GestioneRicette gestioneRicette) {
        this.gestioneRicette = gestioneRicette;
    }

    // ==================== CRUD OPERATIONS ====================

    /**
     * Crea una nuova ricetta con validazione completa
     */
    public Ricetta creaRicetta(String nome, int tempoPreparazione, Map<Ingrediente, Double> ingredienti) 
            throws SQLException, ValidationException {
        
        // Validazioni business
        validaNomeRicetta(nome);
        validaTempoPreparazione(tempoPreparazione);
        
        Ricetta ricetta = new Ricetta(nome.trim(), tempoPreparazione);
        ricetta.setIngredienti(ingredienti);
        
        gestioneRicette.creaRicetta(ricetta);
        invalidaCache();
        
        return ricetta;
    }

    /**
     * Aggiorna ricetta esistente
     */
    public void aggiornaRicetta(int idRicetta, String nuovoNome, int nuovoTempo, 
                                Map<Ingrediente, Double> nuoviIngredienti) 
            throws SQLException, ValidationException {
        
        validaNomeRicetta(nuovoNome);
        validaTempoPreparazione(nuovoTempo);
        
        Ricetta ricetta = new Ricetta(nuovoNome.trim(), nuovoTempo);
        ricetta.setIngredienti(nuoviIngredienti);
        
        gestioneRicette.aggiornaRicetta(idRicetta, ricetta);
        invalidaCache();
    }

    /**
     * Elimina ricetta (con controllo sessioni associate)
     */
    public void eliminaRicetta(int idRicetta) throws SQLException, ValidationException {
        Ricetta ricetta = getRicettaPerId(idRicetta);
        
        if (ricetta == null) {
            throw new ValidationException(ErrorMessages.RICETTA_NON_TROVATA);
        }
        
        // Verifica se la ricetta è usata in sessioni
        if (ricetta.getSessioni() != null && !ricetta.getSessioni().isEmpty()) {
            throw new ValidationException(
                "Impossibile eliminare: la ricetta è utilizzata in " + 
                ricetta.getSessioni().size() + " sessione/i"
            );
        }
        
        gestioneRicette.cancellaRicetta(idRicetta);
        invalidaCache();
    }

    // ==================== QUERY OPERATIONS ====================

    /**
     * Recupera tutte le ricette (con cache)
     */
    public List<Ricetta> getAllRicette() throws SQLException {
        if (cachedRicette == null) {
            cachedRicette = gestioneRicette.getAllRicette();
        }
        return List.copyOf(cachedRicette);
    }

    /**
     * Ricerca per ID
     */
    public Ricetta getRicettaPerId(int idRicetta) throws SQLException {
        return getAllRicette().stream()
                .filter(r -> r.getIdRicetta() == idRicetta)
                .findFirst()
                .orElse(null);
    }

    /**
     * Ricerca per nome (case-insensitive, parziale)
     */
    public List<Ricetta> cercaPerNome(String nome) throws SQLException {
        if (nome == null || nome.trim().isEmpty()) {
            return getAllRicette();
        }
        
        String nomeLC = nome.toLowerCase().trim();
        return getAllRicette().stream()
                .filter(r -> r.getNome().toLowerCase().contains(nomeLC))
                .collect(Collectors.toList());
    }

    // ==================== FILTRI AVANZATI ====================

    /**
     * Filtra ricette per tempo di preparazione
     */
    public List<Ricetta> filtraPerTempo(Integer tempoMin, Integer tempoMax) throws SQLException {
        return getAllRicette().stream()
                .filter(r -> tempoMin == null || r.getTempoPreparazione() >= tempoMin)
                .filter(r -> tempoMax == null || r.getTempoPreparazione() <= tempoMax)
                .collect(Collectors.toList());
    }

    /**
     * Filtra ricette per numero ingredienti
     */
    public List<Ricetta> filtraPerNumeroIngredienti(Integer numMin, Integer numMax) throws SQLException {
        return getAllRicette().stream()
                .filter(r -> numMin == null || r.getNumeroIngredienti() >= numMin)
                .filter(r -> numMax == null || r.getNumeroIngredienti() <= numMax)
                .collect(Collectors.toList());
    }

    /**
     * Filtro combinato (nome + tempo + ingredienti)
     */
    public List<Ricetta> filtraCombinato(String nome, Integer tempoMin, Integer tempoMax,
                                         Integer ingredientiMin, Integer ingredientiMax) 
            throws SQLException {
        
        List<Ricetta> risultati = getAllRicette();
        
        // Filtro nome
        if (nome != null && !nome.trim().isEmpty()) {
            String nomeLC = nome.toLowerCase().trim();
            risultati = risultati.stream()
                    .filter(r -> r.getNome().toLowerCase().contains(nomeLC))
                    .collect(Collectors.toList());
        }
        
        // Filtro tempo
        if (tempoMin != null) {
            risultati = risultati.stream()
                    .filter(r -> r.getTempoPreparazione() >= tempoMin)
                    .collect(Collectors.toList());
        }
        if (tempoMax != null) {
            risultati = risultati.stream()
                    .filter(r -> r.getTempoPreparazione() <= tempoMax)
                    .collect(Collectors.toList());
        }
        
        // Filtro ingredienti
        if (ingredientiMin != null) {
            risultati = risultati.stream()
                    .filter(r -> r.getNumeroIngredienti() >= ingredientiMin)
                    .collect(Collectors.toList());
        }
        if (ingredientiMax != null) {
            risultati = risultati.stream()
                    .filter(r -> r.getNumeroIngredienti() <= ingredientiMax)
                    .collect(Collectors.toList());
        }
        
        return risultati;
    }

    // ==================== GESTIONE INGREDIENTI ====================

    /**
     * Aggiungi ingrediente a ricetta
     */
    public void aggiungiIngrediente(Ricetta ricetta, Ingrediente ingrediente, double quantita) 
            throws SQLException, ValidationException {
        
        if (quantita <= 0) {
            throw new ValidationException(ErrorMessages.QUANTITA_NON_VALIDA);
        }
        
        gestioneRicette.aggiungiIngrediente(ricetta, ingrediente, quantita);
        invalidaCache();
    }

    /**
     * Aggiorna quantità ingrediente
     */
    public void aggiornaQuantitaIngrediente(Ricetta ricetta, Ingrediente ingrediente, double nuovaQuantita) 
            throws SQLException, ValidationException {
        
        if (nuovaQuantita <= 0) {
            throw new ValidationException(ErrorMessages.QUANTITA_NON_VALIDA);
        }
        
        gestioneRicette.aggiornaQuantitaIngrediente(ricetta, ingrediente, nuovaQuantita);
        invalidaCache();
    }

    /**
     * Rimuovi ingrediente da ricetta
     */
    public void rimuoviIngrediente(Ricetta ricetta, Ingrediente ingrediente) throws SQLException {
        gestioneRicette.rimuoviIngrediente(ricetta, ingrediente);
        invalidaCache();
    }

    // ==================== SESSIONI IN PRESENZA ====================

    /**
     * Associa ricetta a sessione in presenza
     */
    public void associaRicettaASessione(Ricetta ricetta, InPresenza sessione) 
            throws ValidationException {
        
        if (ricetta == null || sessione == null) {
            throw new ValidationException("Ricetta e sessione non possono essere null");
        }
        
        if (!sessione.getRicette().contains(ricetta)) {
            sessione.getRicette().add(ricetta);
            ricetta.getSessioni().add(sessione);
        }
    }

    /**
     * Disassocia ricetta da sessione
     */
    public void disassociaRicettaDaSessione(Ricetta ricetta, InPresenza sessione) {
        if (ricetta != null && sessione != null) {
            sessione.getRicette().remove(ricetta);
            ricetta.getSessioni().remove(sessione);
        }
    }

    /**
     * Ottieni ricette non associate a una sessione specifica
     */
    public List<Ricetta> getRicetteNonAssociate(InPresenza sessione) throws SQLException {
        return getAllRicette().stream()
                .filter(r -> !sessione.getRicette().contains(r))
                .collect(Collectors.toList());
    }

    /**
     * Ottieni ricette associate a una sessione
     */
    public List<Ricetta> getRicetteAssociate(InPresenza sessione) {
        return List.copyOf(sessione.getRicette());
    }

    // ==================== STATISTICHE ====================

    /**
     * Ottieni statistiche sulle ricette
     */
    public StatisticheRicette getStatistiche() throws SQLException {
        List<Ricetta> ricette = getAllRicette();
        
        if (ricette.isEmpty()) {
            return new StatisticheRicette(0, 0.0, 0, 0);
        }
        
        double tempoMedio = ricette.stream()
                .mapToInt(Ricetta::getTempoPreparazione)
                .average()
                .orElse(0.0);
        
        int conIngredienti = (int) ricette.stream()
                .filter(r -> r.getIngredienti() != null && !r.getIngredienti().isEmpty())
                .count();
        
        int totaleIngredienti = ricette.stream()
                .mapToInt(Ricetta::getNumeroIngredienti)
                .sum();
        
        return new StatisticheRicette(ricette.size(), tempoMedio, conIngredienti, totaleIngredienti);
    }

    // ==================== VALIDAZIONI CENTRALIZZATE ====================

    private void validaNomeRicetta(String nome) throws ValidationException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new ValidationException(ErrorMessages.NOME_RICETTA_MANCANTE);
        }
        if (nome.trim().length() < 3) {
            throw new ValidationException("Il nome deve contenere almeno 3 caratteri");
        }
        if (nome.trim().length() > 100) {
            throw new ValidationException("Il nome non può superare 100 caratteri");
        }
    }

    private void validaTempoPreparazione(int tempo) throws ValidationException {
        if (tempo <= 0) {
            throw new ValidationException("Il tempo di preparazione deve essere maggiore di zero");
        }
        if (tempo > 1440) { // 24 ore
            throw new ValidationException("Il tempo di preparazione non può superare 24 ore");
        }
    }

    // ==================== CACHE MANAGEMENT ====================

    public void invalidaCache() {
        cachedRicette = null;
    }

    public void ricaricaCache() throws SQLException {
        cachedRicette = null;
        getAllRicette();
    }

    // ==================== UTILITY ====================

    public int contaRicette() throws SQLException {
        return getAllRicette().size();
    }

    public GestioneRicette getGestioneRicette() {
        return gestioneRicette;
    }

    // ==================== INNER CLASS PER STATISTICHE ====================

    public static class StatisticheRicette {
        public final int totale;
        public final double tempoMedio;
        public final int conIngredienti;
        public final int totaleIngredienti;

        public StatisticheRicette(int totale, double tempoMedio, int conIngredienti, int totaleIngredienti) {
            this.totale = totale;
            this.tempoMedio = tempoMedio;
            this.conIngredienti = conIngredienti;
            this.totaleIngredienti = totaleIngredienti;
        }

        @Override
        public String toString() {
            return String.format(
                "📊 Totale: %d | ⏱️ Tempo medio: %.1f min | 🥕 Con ingredienti: %d | Ingredienti totali: %d",
                totale, tempoMedio, conIngredienti, totaleIngredienti
            );
        }
    }
}