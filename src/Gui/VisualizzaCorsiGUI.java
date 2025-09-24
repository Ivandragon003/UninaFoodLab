package Gui;

import controller.GestioneCorsoController;
import controller.VisualizzaCorsiController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.CorsoCucina;

import java.sql.SQLException;

public class VisualizzaCorsiGUI {

    private VisualizzaCorsiController visualizzaController;
    private GestioneCorsoController gestioneCorsoController;
    private StackPane menuRoot;

    private ObservableList<CorsoCucina> corsiData = FXCollections.observableArrayList();
    private FilteredList<CorsoCucina> filteredCorsi;

    public void setControllers(VisualizzaCorsiController visualizzaController,
                               GestioneCorsoController gestioneCorsoController,
                               StackPane menuRoot) {
        this.visualizzaController = visualizzaController;
        this.gestioneCorsoController = gestioneCorsoController;
        this.menuRoot = menuRoot;
    }

    public StackPane getRoot() {
        StackPane root = new StackPane();
        root.setPrefSize(500, 700);

        // ===== Sfondo gradiente =====
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FF9966")),
                new Stop(1, Color.web("#FFCC99")));
        Region background = new Region();
        background.setBackground(new Background(new BackgroundFill(gradient, null, null)));
        background.setPrefSize(500, 700);
        root.getChildren().add(background);

        // ===== Card centrale =====
        VBox card = new VBox(20);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(30));
        card.setMaxWidth(500);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-border-radius: 20; -fx-border-color: #FF9966; -fx-border-width: 2;");
        DropShadow shadow = new DropShadow();
        shadow.setRadius(10);
        shadow.setColor(Color.web("#000000", 0.2));
        shadow.setOffsetY(3);
        card.setEffect(shadow);
        root.getChildren().add(card);

        // ===== Titolo =====
        Label title = new Label("Lista dei corsi");
        title.setFont(Font.font("Roboto", FontWeight.BOLD, 26));
        title.setTextFill(Color.web("#FF6600"));
        card.getChildren().add(title);

        // ===== Filtri =====
        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);

        TextField nomeField = new TextField();
        nomeField.setPromptText("Cerca per nome");
        TextField argomentoField = new TextField();
        argomentoField.setPromptText("Cerca per argomento");

        filters.getChildren().addAll(nomeField, argomentoField);
        card.getChildren().add(filters);

        // ===== TableView =====
        TableView<CorsoCucina> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<CorsoCucina, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getIdCorso()).asObject());

        TableColumn<CorsoCucina, String> nomeCol = new TableColumn<>("Nome Corso");
        nomeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNomeCorso()));

        TableColumn<CorsoCucina, String> argomentoCol = new TableColumn<>("Argomento");
        argomentoCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getArgomento()));

        TableColumn<CorsoCucina, Integer> iscrittiCol = new TableColumn<>("Iscritti");
        iscrittiCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(
                c.getValue().getIscrizioni() != null ? c.getValue().getIscrizioni().size() : 0
        ).asObject());

        TableColumn<CorsoCucina, Double> prezzoCol = new TableColumn<>("Prezzo");
        prezzoCol.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getPrezzo()).asObject());

        TableColumn<CorsoCucina, Integer> sessioniCol = new TableColumn<>("Sessioni");
        sessioniCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(
                c.getValue().getSessioni() != null ? c.getValue().getSessioni().size() : 0
        ).asObject());

        table.getColumns().addAll(idCol, nomeCol, argomentoCol, iscrittiCol, prezzoCol, sessioniCol);
        card.getChildren().add(table);

        // ===== Row factory per doppio click =====
        table.setRowFactory(tv -> {
            TableRow<CorsoCucina> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    CorsoCucina corsoSelezionato = row.getItem();
                    try {
                        CorsoCucina corsoDettagli = gestioneCorsoController.getCorsoCompleto(corsoSelezionato.getIdCorso());

                        // crea GUI dettagli e ottieni root
                        DettagliCorsoGUI dettagliGUI = new DettagliCorsoGUI();
                        dettagliGUI.setController(gestioneCorsoController);
                        dettagliGUI.setCorso(corsoDettagli);
                        VBox dettagliRoot = dettagliGUI.getRoot(); // metodo da aggiungere in DettagliCorsoGUI

                        // sostituisci il contenuto del menuRoot con i dettagli
                        menuRoot.getChildren().clear();
                        menuRoot.getChildren().add(dettagliRoot);

                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Errore caricando il corso: " + ex.getMessage(), ButtonType.OK);
                        alert.showAndWait();
                    }
                }
            });
            return row;
        });


        // ===== Bottoni =====
        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);
        Button mostraTuttiBtn = createStylishButton("Mostra tutti i corsi", "#FF6600", "#FF8533");
        Button mieiBtn = createStylishButton("I miei corsi", "#FF6600", "#FF8533");
        Button tornaIndietroBtn = createStylishButton("⬅ Torna indietro", "#FFCC99", "#FFD9B3");
        buttons.getChildren().addAll(mostraTuttiBtn, mieiBtn, tornaIndietroBtn);
        card.getChildren().add(buttons);

        // ===== Caricamento dati =====
        caricaCorsi();
        filteredCorsi = new FilteredList<>(corsiData, p -> true);
        table.setItems(filteredCorsi);

        // ===== Eventi filtri =====
        nomeField.textProperty().addListener((obs, oldVal, newVal) -> applicaFiltri(nomeField.getText(), argomentoField.getText(), false));
        argomentoField.textProperty().addListener((obs, oldVal, newVal) -> applicaFiltri(nomeField.getText(), argomentoField.getText(), false));

        mostraTuttiBtn.setOnAction(e -> {
            nomeField.clear();
            argomentoField.clear();
            applicaFiltri("", "", false);
        });

        mieiBtn.setOnAction(e -> applicaFiltri(nomeField.getText(), argomentoField.getText(), true));

        tornaIndietroBtn.setOnAction(e -> {
            Stage stage = (Stage) menuRoot.getScene().getWindow();
            stage.getScene().setRoot(menuRoot);
        });

        return root;
    }

    private void caricaCorsi() {
        try {
            corsiData.clear();
            corsiData.addAll(visualizzaController.getTuttiICorsi()); // corsi leggeri
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void applicaFiltri(String nomeFiltro, String argomentoFiltro, boolean soloChefLoggato) {
        filteredCorsi.setPredicate(c -> {
            boolean match = true;
            if (soloChefLoggato) {
                try {
                    match = visualizzaController.getCorsiChefLoggato().contains(c);
                } catch (SQLException ignored) {}
            }
            if (nomeFiltro != null && !nomeFiltro.isEmpty()) {
                match &= c.getNomeCorso().toLowerCase().contains(nomeFiltro.toLowerCase());
            }
            if (argomentoFiltro != null && !argomentoFiltro.isEmpty()) {
                match &= c.getArgomento().toLowerCase().contains(argomentoFiltro.toLowerCase());
            }
            return match;
        });
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
}
