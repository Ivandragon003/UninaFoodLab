package Gui;

import controller.VisualizzaRicetteController;
import model.Ricetta;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class VisualizzaRicetteGUI {

    private VisualizzaRicetteController controller;

    public void setController(VisualizzaRicetteController controller) {
        this.controller = controller;
    }

    public void show(Stage stage) {
        if (controller == null) {
            throw new IllegalStateException("Controller non impostato!");
        }

        stage.setTitle("Visualizza Ricette");

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        Label nomeLabel = new Label("Cerca ricetta per nome:");
        TextField nomeField = new TextField();
        nomeField.setPromptText("Digita il nome della ricetta");

        Label tempoLabel = new Label("Tempo massimo di preparazione (min):");
        TextField tempoField = new TextField();
        tempoField.setPromptText("Es: 30");

        ListView<Ricetta> ricetteList = new ListView<>();

        Button mostraTutteBtn = new Button("Mostra tutte le ricette");
        Button creaRicettaBtn = new Button("Crea Ricetta");
        Button tornaIndietroBtn = new Button("Torna indietro");

        HBox bottomButtons = new HBox(10, creaRicettaBtn, tornaIndietroBtn);

        root.getChildren().addAll(nomeLabel, nomeField, tempoLabel, tempoField,
                mostraTutteBtn, ricetteList, bottomButtons);

        // Ricerca live
        nomeField.setOnKeyReleased(e -> {
            try {
                List<Ricetta> ricette = controller.cercaPerNome(nomeField.getText().trim());
                aggiornaLista(ricetteList, ricette);
            } catch (SQLException ex) {
                showAlert("Errore", "Impossibile cercare le ricette: " + ex.getMessage());
            }
        });

        tempoField.setOnKeyReleased(e -> {
            try {
                int maxTempo = Integer.parseInt(tempoField.getText().trim());
                List<Ricetta> ricette = controller.filtraPerTempo(maxTempo);
                aggiornaLista(ricetteList, ricette);
            } catch (NumberFormatException ignored) { }
            catch (SQLException ex) {
                showAlert("Errore", "Impossibile filtrare le ricette: " + ex.getMessage());
            }
        });

        // Mostra tutte
        mostraTutteBtn.setOnAction(e -> {
            try {
                List<Ricetta> ricette = controller.getAllRicette();
                aggiornaLista(ricetteList, ricette);
            } catch (SQLException ex) {
                showAlert("Errore", "Impossibile caricare le ricette: " + ex.getMessage());
            }
        });

        // Crea ricetta (chiama la versione corretta in base al caso)
        creaRicettaBtn.setOnAction(e -> controller.mostraFormCreazioneRicetta());

        // Doppio click per dettagli
        ricetteList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Ricetta selezionata = ricetteList.getSelectionModel().getSelectedItem();
                if (selezionata != null) {
                    controller.mostraDettagliRicetta(selezionata);
                }
            }
        });

        tornaIndietroBtn.setOnAction(e -> stage.close());

        stage.setScene(new Scene(root, 650, 450));
        stage.show();
    }

    private void aggiornaLista(ListView<Ricetta> listView, List<Ricetta> ricette) {
        listView.getItems().setAll(ricette);
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Ricetta r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) {
                    setText(null);
                } else {
                    setText(r.getNome() + " | Tempo: " + r.getTempoPreparazione() +
                            " min | Ingredienti: " + r.getNumeroIngredienti());
                }
            }
        });
    }

    private void showAlert(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
