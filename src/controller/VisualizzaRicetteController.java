package controller;

import model.Ricetta;
import service.GestioneCucina;
import service.GestioneRicette;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import Gui.CreaRicettaGUI;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VisualizzaRicetteController {

    private final GestioneRicette gestioneRicette;
    private List<Ricetta> cachedRicette = null;

    public VisualizzaRicetteController(GestioneRicette gestioneRicetteService) {
        this.gestioneRicette = gestioneRicetteService;
    }
    
    public void show(Stage stage) {
        stage.setTitle("Visualizza Ricette");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        ListView<Ricetta> listaRicette = new ListView<>();
        try {
            listaRicette.getItems().addAll(getTutteLeRicette());
        } catch (Exception e) {
            showError("Errore caricamento ricette: " + e.getMessage());
        }

        Button btnAggiungi = new Button("Aggiungi Ricetta");
        btnAggiungi.setOnAction(e -> {
            CreaRicettaGUI creaGUI = new CreaRicettaGUI(
                    gestioneRicette,
                    null,
                    new GestioneCucina(null)
                    );
            Stage creaStage = new Stage();
            creaGUI.start(creaStage);
        });

        root.getChildren().addAll(new Label("Ricette disponibili:"), listaRicette, btnAggiungi);

        Scene scene = new Scene(root, 500, 400);
        stage.setScene(scene);
        stage.show();
    }

    public List<Ricetta> getTutteLeRicette() throws SQLException {
        if (cachedRicette == null) {
            cachedRicette = gestioneRicette.getAllRicette();
        }
        return cachedRicette;
    }

    public List<Ricetta> cercaPerNome(String filtro) throws SQLException {
        String f = filtro.toLowerCase().trim();
        return getTutteLeRicette().stream()
                .filter(r -> r.getNome().toLowerCase().contains(f))
                .collect(Collectors.toList());
    }

    public List<Ricetta> filtraPerTempo(int maxTempo) throws SQLException {
        return getTutteLeRicette().stream()
                .filter(r -> r.getTempoPreparazione() <= maxTempo)
                .collect(Collectors.toList());
    }

    public void mostraDettagliRicetta(Ricetta r) {
        new Gui.DettagliRicettaGUI(gestioneRicette, r).start(new javafx.stage.Stage());
    }

    public GestioneRicette getGestioneRicetteService() {
        return gestioneRicette;
    }

    public void aggiungiRicetta(Ricetta r) throws SQLException {
        gestioneRicette.creaRicetta(r);
        cachedRicette = null; 
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
