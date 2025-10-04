package controller;



import model.Ricetta;
import model.InPresenza;
import service.GestioneRicette;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class VisualizzaRicetteController {

    private final GestioneRicette gestioneRicette;
    private List<Ricetta> cachedRicette = null;

    public VisualizzaRicetteController(GestioneRicette gestioneRicetteService) {
        this.gestioneRicette = gestioneRicetteService;
    }

    public List<Ricetta> getTutteLeRicette() {
        if (cachedRicette == null) {
            try {
                cachedRicette = gestioneRicette.getAllRicette();
            } catch (SQLException e) {
                e.printStackTrace();
                cachedRicette = Collections.emptyList();
            }
        }
        return cachedRicette;
    }

    public void aggiungiRicettaASessione(InPresenza sessione, Ricetta r) {
        try {
            // Solo se il metodo creaRicetta esiste nel tuo GestioneRicette
            // Altrimenti rimuovi questa riga
            if (hasCreateMethod()) {
                gestioneRicette.creaRicetta(r);
            }
            
            if (!sessione.getRicette().contains(r)) {
                sessione.getRicette().add(r);
                r.getSessioni().add(sessione);
            }
        } catch (Exception e) {
            // Se non esiste creaRicetta, aggiungi solo alla sessione
            if (!sessione.getRicette().contains(r)) {
                sessione.getRicette().add(r);
                r.getSessioni().add(sessione);
            }
        }
    }

    // Crea ricetta - usa solo quello che esiste
    public void creaRicetta(Ricetta ricetta) throws SQLException {
        try {
            // Prova a chiamare il metodo se esiste
            gestioneRicette.creaRicetta(ricetta);
            cachedRicette = null; // Invalida cache
        } catch (Exception e) {
            // Se il metodo non esiste, crea un messaggio di errore appropriato
            throw new SQLException("Metodo creaRicetta non implementato nel service GestioneRicette");
        }
    }

    // Cerca ricette per nome - implementazione locale
    public List<Ricetta> cercaRicettePerNome(String nome) throws SQLException {
        if (nome == null || nome.trim().isEmpty()) {
            return getTutteLeRicette();
        }
        
        // Implementa ricerca locale sui dati già caricati
        String nomeLC = nome.toLowerCase();
        return getTutteLeRicette().stream()
                .filter(r -> r.getNome().toLowerCase().contains(nomeLC))
                .collect(Collectors.toList());
    }

    // Ottieni ricetta per ID - implementazione locale  
    public Ricetta getRicettaPerId(int id) throws SQLException {
        return getTutteLeRicette().stream()
                .filter(r -> r.getIdRicetta() == id)
                .findFirst()
                .orElse(null);
    }

    // Verifica eliminazione - implementazione semplice
    public boolean puoEliminareRicetta(int idRicetta) throws SQLException {
        Ricetta ricetta = getRicettaPerId(idRicetta);
        if (ricetta == null) return false;
        
        // Controlla se è associata a sessioni
        return ricetta.getSessioni() == null || ricetta.getSessioni().isEmpty();
    }

    // Elimina ricetta - implementazione placeholder
    public void eliminaRicetta(int idRicetta) throws SQLException {
        throw new SQLException("Eliminazione ricette non implementata nel DAO. " +
                              "Implementa il metodo nel RicettaDAO per abilitare questa funzionalità");
    }

    // Statistiche - implementazione locale
    public String getStatisticheRicette() throws SQLException {
        List<Ricetta> ricette = getTutteLeRicette();
        int totale = ricette.size();
        
        if (totale == 0) return "Nessuna ricetta trovata";
        
        double tempoMedio = ricette.stream()
                .mapToInt(Ricetta::getTempoPreparazione)
                .average()
                .orElse(0.0);
                
        int conIngredienti = (int) ricette.stream()
                .filter(r -> r.getIngredienti() != null && !r.getIngredienti().isEmpty())
                .count();
        
        return String.format("Ricette totali: %d | Tempo medio: %.1f min | Con ingredienti: %d", 
                            totale, tempoMedio, conIngredienti);
    }

    // Conta ricette - implementazione locale
    public int contaRicette() throws SQLException {
        return getTutteLeRicette().size();
    }

    // Ricarica cache
    public void ricaricaCache() {
        cachedRicette = null;
        getTutteLeRicette();
    }

    // Accesso al service
    public GestioneRicette getGestioneRicette() {
        return gestioneRicette;
    }

    // Helper method per controllare se il metodo create esiste
    private boolean hasCreateMethod() {
        try {
            gestioneRicette.getClass().getMethod("creaRicetta", Ricetta.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
   

    // Associa/Disassocia ricette esistenti da sessioni
    public void associaRicettaASessione(InPresenza sessione, Ricetta ricetta) {
        if (!sessione.getRicette().contains(ricetta)) {
            sessione.getRicette().add(ricetta);
            if (ricetta.getSessioni() != null) {
                ricetta.getSessioni().add(sessione);
            }
        }
    }

    public void disassociaRicettaDaSessione(InPresenza sessione, Ricetta ricetta) {
        sessione.getRicette().remove(ricetta);
        if (ricetta.getSessioni() != null) {
            ricetta.getSessioni().remove(sessione);
        }
    }

    // Ottieni ricette non associate a una sessione specifica
    public List<Ricetta> getRicetteNonAssociate(InPresenza sessione) throws SQLException {
        return getTutteLeRicette().stream()
                .filter(r -> !sessione.getRicette().contains(r))
                .collect(Collectors.toList());
    }

    // Ottieni ricette associate a una sessione
    public List<Ricetta> getRicetteAssociate(InPresenza sessione) {
        return new ArrayList<>(sessione.getRicette());
    }
}