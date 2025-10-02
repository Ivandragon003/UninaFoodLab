package Gui;

import controller.VisualizzaRicetteController;
import model.Ricetta;
import model.InPresenza;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class VisualizzaRicetteGUI {

    private final VisualizzaRicetteController controller;
    private InPresenza sessione;

    public VisualizzaRicetteGUI(VisualizzaRicetteController controller) {
        this.controller = controller;
    }

    public void setSessione(InPresenza sessione) {
        this.sessione = sessione;
    }

    public void show(Stage stage) {
        stage.setTitle("Visualizza Ricette");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        ListView<Ricetta> listaRicette = new ListView<>();
        try {
            listaRicette.getItems().addAll(controller.getTutteLeRicette());
        } catch (SQLException e) {
            showError("Errore caricamento ricette: " + e.getMessage());
        }

        Button btnAggiungi = new Button("Aggiungi alla sessione");
        btnAggiungi.setOnAction(e -> {
            Ricetta selected = listaRicette.getSelectionModel().getSelectedItem();
            if (selected != null && sessione != null) {
                try {
                    controller.aggiungiRicettaASessione(sessione, selected);
                    listaRicette.getSelectionModel().clearSelection();
                } catch (SQLException ex) {
                    showError("Errore aggiunta ricetta: " + ex.getMessage());
                }
            }
        });

        root.getChildren().addAll(new Label("Ricette disponibili:"), listaRicette, btnAggiungi);

        Scene scene = new Scene(root, 500, 400);
        stage.setScene(scene);
        stage.show();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
