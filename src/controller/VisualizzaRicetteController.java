package controller;

import model.Ricetta;
import service.GestioneRicette;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class VisualizzaRicetteController {
    private final GestioneRicette gestioneRicette;

    public VisualizzaRicetteController(GestioneRicette gestioneRicette) {
        this.gestioneRicette = gestioneRicette;
    }

    // Restituisce tutte le ricette
    public List<Ricetta> getAllRicette() throws SQLException {
        return gestioneRicette.getAllRicette();
    }

    // Ricerca ricette per nome
    public List<Ricetta> cercaPerNome(String nome) throws SQLException {
        return gestioneRicette.getAllRicette().stream()
                .filter(r -> r.getNome().toLowerCase().contains(nome.toLowerCase()))
                .collect(Collectors.toList());
    }

    // Filtra ricette per tempo massimo
    public List<Ricetta> filtraPerTempo(int tempoMax) throws SQLException {
        return gestioneRicette.getAllRicette().stream()
                .filter(r -> r.getTempoPreparazione() <= tempoMax)
                .collect(Collectors.toList());
    }
}
