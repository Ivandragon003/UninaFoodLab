package Gui;

import controller.VisualizzaRicetteController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Ricetta;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VisualizzaRicetteGUI {

    private VisualizzaRicetteController controller;
    private Pane menuRoot; // Pane del menu principale
    private ObservableList<Ricetta> ricetteData = FXCollections.observableArrayList();
    private VBox root;

    public void setController(VisualizzaRicetteController controller, Pane menuRoot) {
        this.controller = controller;
        this.menuRoot = menuRoot;
    }

    public VBox getRoot() {
        return root;
    }

    public void start() {
        if (controller == null) {
            throw new IllegalStateException("Controller non impostato!");
        }

        root = new VBox();
        root.setPrefSize(600, 450);

        // Sfondo gradiente
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FF9966")),
                new Stop(1, Color.web("#FFCC99")));
        root.setBackground(new Background(new BackgroundFill(gradient, null, null)));

        // Card centrale
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setMaxWidth(500);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-border-radius: 20; -fx-border-color: #FF9966; -fx-border-width: 2;");

        DropShadow shadow = new DropShadow();
        shadow.setRadius(10);
        shadow.setColor(Color.web("#000000", 0.2));
        shadow.setOffsetY(3);
        card.setEffect(shadow);

        Label title = new Label("Visualizza Ricette");
        title.setFont(Font.font("Roboto", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#FF6600"));

        TextField nomeField = new TextField();
        nomeField.setPromptText("Cerca ricetta per nome");
        TextField tempoField = new TextField();
        tempoField.setPromptText("Tempo massimo (min)");

        ListView<String> ricetteList = new ListView<>();

        Button mostraTutteBtn = createStylishButton("Mostra tutte le ricette", "#FF6600", "#FF8533");
        Button tornaIndietroBtn = createStylishButton("Torna indietro", "#FFCC99", "#FFD9B3");

        card.getChildren().addAll(title, nomeField, tempoField, mostraTutteBtn, ricetteList, tornaIndietroBtn);
        root.getChildren().add(card);

        caricaRicette(ricetteList);

        // Filtri live
        nomeField.setOnKeyReleased(e -> filtraRicette(nomeField.getText(), tempoField.getText(), ricetteList));
        tempoField.setOnKeyReleased(e -> filtraRicette(nomeField.getText(), tempoField.getText(), ricetteList));

        mostraTutteBtn.setOnAction(e -> {
            nomeField.clear();
            tempoField.clear();
            caricaRicette(ricetteList);
        });

        tornaIndietroBtn.setOnAction(e -> root.getScene().setRoot(menuRoot));
    }

    private void caricaRicette(ListView<String> listView) {
        try {
            ricetteData.clear();
            ricetteData.addAll(controller.getAllRicette());
            aggiornaLista(listView);
        } catch (SQLException ex) {
            showAlert("Errore", "Impossibile caricare le ricette: " + ex.getMessage());
        }
    }

    private void filtraRicette(String nomeFiltro, String tempoFiltro, ListView<String> listView) {
        List<Ricetta> filtered = new ArrayList<>(ricetteData);
        if (nomeFiltro != null && !nomeFiltro.isEmpty()) {
            filtered.removeIf(r -> !r.getNome().toLowerCase().contains(nomeFiltro.toLowerCase()));
        }
        if (tempoFiltro != null && !tempoFiltro.isEmpty()) {
            try {
                int maxTempo = Integer.parseInt(tempoFiltro);
                filtered.removeIf(r -> r.getTempoPreparazione() > maxTempo);
            } catch (NumberFormatException ignored) {}
        }
        aggiornaLista(listView, filtered);
    }

    private void aggiornaLista(ListView<String> listView) {
        aggiornaLista(listView, new ArrayList<>(ricetteData));
    }

    private void aggiornaLista(ListView<String> listView, List<Ricetta> ricette) {
        List<String> items = new ArrayList<>();
        for (Ricetta r : ricette) {
            String text = String.format("%s | Tempo: %d min | Ingredienti: %d",
                    r.getNome(), r.getTempoPreparazione(), r.getNumeroIngredienti());
            items.add(text);
        }
        listView.getItems().setAll(items);
    }

    private Button createStylishButton(String text, String baseColor, String hoverColor) {
        Button button = new Button(text);
        button.setPrefSize(150, 45);
        button.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        button.setTextFill(Color.web("#4B2E2E"));
        button.setStyle("-fx-background-color: " + baseColor + "; -fx-background-radius: 20; -fx-cursor: hand;");
        DropShadow shadow = new DropShadow();
        shadow.setRadius(5);
        shadow.setColor(Color.web("#000000", 0.2));
        button.setEffect(shadow);
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: " + hoverColor + "; -fx-background-radius: 20; -fx-cursor: hand;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + baseColor + "; -fx-background-radius: 20; -fx-cursor: hand;"));
        return button;
    }

    private void showAlert(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
