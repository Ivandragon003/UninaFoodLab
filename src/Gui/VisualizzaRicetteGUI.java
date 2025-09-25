package Gui;

import controller.VisualizzaRicetteController;
import model.Ricetta;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VisualizzaRicetteGUI {

    private VisualizzaRicetteController controller;
    private Pane menuRoot;

    public void setController(VisualizzaRicetteController controller, Pane menuRoot) {
        this.controller = controller;
        this.menuRoot = menuRoot;
    }

    public void show(Stage stage) {
        if (controller == null) {
            throw new IllegalStateException("Controller non impostato!");
        }

        stage.setTitle("Visualizza Ricette");

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        // Campi ricerca
        Label nomeLabel = new Label("Cerca ricetta per nome:");
        TextField nomeField = new TextField();
        nomeField.setPromptText("Digita il nome della ricetta");

        Label tempoLabel = new Label("Tempo massimo di preparazione (min):");
        TextField tempoField = new TextField();
        tempoField.setPromptText("Es: 30");

        ListView<String> ricetteList = new ListView<>();

        // Pulsanti
        Button mostraTutteBtn = new Button("Mostra tutte le ricette");
        Button tornaIndietroBtn = new Button("Torna indietro");

        root.getChildren().addAll(nomeLabel, nomeField, tempoLabel, tempoField,
                mostraTutteBtn, ricetteList, tornaIndietroBtn);

        // Ricerca live per nome
        nomeField.setOnKeyReleased(e -> {
            try {
                List<Ricetta> ricette = controller.cercaPerNome(nomeField.getText().trim());
                aggiornaLista(ricetteList, ricette);
            } catch (SQLException ex) {
                showAlert("Errore", "Impossibile cercare le ricette: " + ex.getMessage());
            }
        });

        // Filtro live per tempo
        tempoField.setOnKeyReleased(e -> {
            try {
                int maxTempo = Integer.parseInt(tempoField.getText().trim());
                List<Ricetta> ricette = controller.filtraPerTempo(maxTempo);
                aggiornaLista(ricetteList, ricette);
            } catch (NumberFormatException ignored) {
                // Ignora input non numerici
            } catch (SQLException ex) {
                showAlert("Errore", "Impossibile filtrare le ricette: " + ex.getMessage());
            }
        });

        // Mostra tutte le ricette
        mostraTutteBtn.setOnAction(e -> {
            try {
                List<Ricetta> ricette = controller.getAllRicette();
                aggiornaLista(ricetteList, ricette);
            } catch (SQLException ex) {
                showAlert("Errore", "Impossibile caricare le ricette: " + ex.getMessage());
            }
        });

        // Torna al menu principale
        tornaIndietroBtn.setOnAction(e -> {
            Stage currentStage = (Stage) root.getScene().getWindow();
            currentStage.getScene().setRoot(menuRoot);
        });

        // ✅ Mostra la scena
        stage.setScene(new Scene(root, 600, 450));
        stage.show();
    }

    private void aggiornaLista(ListView<String> listView, List<Ricetta> ricette) {
        List<String> items = new ArrayList<>();
        for (Ricetta r : ricette) {
            String text = String.format("%s | Tempo: %d min | Ingredienti: %d",
                    r.getNome(),
                    r.getTempoPreparazione(),
                    r.getNumeroIngredienti());
            items.add(text);
        }
        listView.getItems().setAll(items);
    }

    private void showAlert(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
