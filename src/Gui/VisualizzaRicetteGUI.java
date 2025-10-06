package Gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import model.Ricetta;
import model.Ingrediente;
import controller.RicettaController;
import util.StyleHelper;
import exceptions.ValidationException;

import java.util.List;
import java.util.Map;

/**
 * GUI PULITA - Solo logica di interfaccia, nessuna business logic.
 * Delega tutto al controller.
 */
public class VisualizzaRicetteGUI {
    
    // Dipendenze
    private final RicettaController controller;
    
    // Componenti UI
    private ObservableList<Ricetta> ricetteData;
    private ListView<Ricetta> ricetteListView;
    private TextField filtroNomeField;
    private TextField filtroTempoMinField;
    private TextField filtroTempoMaxField;
    private TextField filtroIngredientiMinField;
    private TextField filtroIngredientiMaxField;
    private Label statisticheLabel;
    
    // Modalità selezione
    private boolean modalitaSelezione = false;
    private Ricetta ricettaSelezionata = null;
    
    // Root container
    private VBox root;

    public VisualizzaRicetteGUI(RicettaController controller) {
        this.controller = controller;
        this.ricetteData = FXCollections.observableArrayList();
    }

    public void setSelectionMode(boolean modalitaSelezione) {
        this.modalitaSelezione = modalitaSelezione;
    }

    public VBox getRoot() {
        if (root == null) {
            root = createMainLayout();
            caricaRicette(); // Carica dati iniziali
        }
        return root;
    }

    // ==================== LAYOUT CREATION ====================

    private VBox createMainLayout() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        StyleHelper.applyOrangeBackground(container);

        Label titleLabel = createTitleLabel();
        ScrollPane scrollPane = createScrollPane();
        
        container.getChildren().addAll(titleLabel, scrollPane);
        return container;
    }

    private Label createTitleLabel() {
        Label label = StyleHelper.createTitleLabel("📖 Gestione Ricette");
        label.setAlignment(Pos.CENTER);
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        return label;
    }

    private ScrollPane createScrollPane() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox contentBox = new VBox(15);
        contentBox.getChildren().addAll(
            createStatisticheSection(),
            new Separator(),
            createFiltriSection(),
            new Separator(),
            createListaSection(),
            new Separator(),
            createButtonSection()
        );

        scrollPane.setContent(contentBox);
        return scrollPane;
    }

    // ==================== SEZIONE STATISTICHE ====================

    private VBox createStatisticheSection() {
        VBox section = StyleHelper.createSection();
        
        Label sectionTitle = createSectionLabel("📊 Statistiche", StyleHelper.SUCCESS_GREEN);
        
        statisticheLabel = new Label("Caricamento...");
        statisticheLabel.setFont(javafx.scene.text.Font.font("Roboto", 14));
        statisticheLabel.setTextFill(Color.web("#555555"));
        statisticheLabel.setStyle("-fx-padding: 10; -fx-background-color: #f0f8ff; " +
                                  "-fx-background-radius: 8; -fx-border-radius: 8;");
        
        section.getChildren().addAll(sectionTitle, statisticheLabel);
        return section;
    }

    // ==================== SEZIONE FILTRI ====================

    private VBox createFiltriSection() {
        VBox section = StyleHelper.createSection();
        Label sectionTitle = createSectionLabel("🔍 Filtri Avanzati", StyleHelper.PRIMARY_ORANGE);

        // Ricerca per nome
        HBox nomeBox = new HBox(10);
        nomeBox.setAlignment(Pos.CENTER_LEFT);
        filtroNomeField = StyleHelper.createTextField("Cerca per nome ricetta...");
        filtroNomeField.setPrefWidth(300);
        filtroNomeField.textProperty().addListener((obs, old, val) -> applicaFiltri());
        nomeBox.getChildren().addAll(StyleHelper.createLabel("Nome:"), filtroNomeField);

        // Filtri numerici
        GridPane filtriGrid = createFiltriGrid();

        section.getChildren().addAll(sectionTitle, nomeBox, filtriGrid);
        return section;
    }

    private GridPane createFiltriGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        // TEMPO
        filtroTempoMinField = createNumericField("Min");
        filtroTempoMaxField = createNumericField("Max");
        HBox tempoBox = new HBox(5, filtroTempoMinField, new Label("-"), 
                                 filtroTempoMaxField, new Label("min"));
        tempoBox.setAlignment(Pos.CENTER_LEFT);

        // INGREDIENTI
        filtroIngredientiMinField = createNumericField("Min");
        filtroIngredientiMaxField = createNumericField("Max");
        HBox ingredientiBox = new HBox(5, filtroIngredientiMinField, new Label("-"), 
                                       filtroIngredientiMaxField);
        ingredientiBox.setAlignment(Pos.CENTER_LEFT);

        Button resetBtn = StyleHelper.createInfoButton("🔄 Reset Filtri");
        resetBtn.setOnAction(e -> resetFiltri());

        grid.add(StyleHelper.createLabel("Tempo prep.:"), 0, 0);
        grid.add(tempoBox, 1, 0);
        grid.add(StyleHelper.createLabel("N° ingredienti:"), 2, 0);
        grid.add(ingredientiBox, 3, 0);
        grid.add(resetBtn, 4, 0);

        return grid;
    }

    private TextField createNumericField(String prompt) {
        TextField field = StyleHelper.createTextField(prompt);
        field.setPrefWidth(80);
        field.textProperty().addListener((obs, old, val) -> {
            validateNumericField(field, val);
            applicaFiltri();
        });
        return field;
    }

    // ==================== SEZIONE LISTA ====================

    private VBox createListaSection() {
        VBox section = StyleHelper.createSection();
        HBox headerBox = createListaHeader();

        ricetteListView = new ListView<>();
        ricetteListView.setPrefHeight(350);
        ricetteListView.setItems(ricetteData);
        StyleHelper.applyListViewStyle(ricetteListView);
        ricetteListView.setCellFactory(listView -> new RicettaCell());
        ricetteListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                onRicettaDoppioClick();
            }
        });

        section.getChildren().addAll(headerBox, ricetteListView);
        return section;
    }

    private HBox createListaHeader() {
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = createSectionLabel("📋 Lista Ricette", StyleHelper.PRIMARY_ORANGE);
        Label istruzioniLabel = new Label("💡 Doppio click per vedere ingredienti");
        istruzioniLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
        istruzioniLabel.setTextFill(Color.web(StyleHelper.INFO_BLUE));

        VBox titleBox = new VBox(5, sectionTitle, istruzioniLabel);

        if (!modalitaSelezione) {
            Button creaRicettaBtn = StyleHelper.createSuccessButton("➕ Crea Nuova");
            creaRicettaBtn.setOnAction(e -> apriCreaRicetta());
            headerBox.getChildren().addAll(titleBox, new Region(), creaRicettaBtn);
            HBox.setHgrow(headerBox.getChildren().get(1), Priority.ALWAYS);
        } else {
            headerBox.getChildren().add(titleBox);
        }

        return headerBox;
    }

    // ==================== CELL FACTORY ====================

    private class RicettaCell extends ListCell<Ricetta> {
        @Override
        protected void updateItem(Ricetta item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(createRicettaCellGraphic(item));
                setText(null);
            }
        }
    }

    private HBox createRicettaCellGraphic(Ricetta ricetta) {
        HBox cellBox = new HBox(15);
        cellBox.setAlignment(Pos.CENTER_LEFT);
        cellBox.setPadding(new Insets(12));

        VBox infoBox = new VBox(5);
        
        Label nomeLabel = new Label("📖 " + ricetta.getNome());
        nomeLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 16));
        nomeLabel.setTextFill(Color.BLACK);

        HBox detailsBox = new HBox(20);
        detailsBox.setAlignment(Pos.CENTER_LEFT);

        Label tempoLabel = new Label("⏱️ " + ricetta.getTempoPreparazione() + " min");
        tempoLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
        tempoLabel.setTextFill(Color.web(StyleHelper.INFO_BLUE));

        Label ingredientiLabel = new Label("🥕 " + ricetta.getNumeroIngredienti() + " ingredienti");
        ingredientiLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
        ingredientiLabel.setTextFill(Color.web(StyleHelper.SUCCESS_GREEN));

        detailsBox.getChildren().addAll(tempoLabel, ingredientiLabel);
        infoBox.getChildren().addAll(nomeLabel, detailsBox);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        cellBox.getChildren().addAll(infoBox, spacer);
        return cellBox;
    }

    // ==================== SEZIONE BOTTONI ====================

    private HBox createButtonSection() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        if (modalitaSelezione) {
            Button selezionaBtn = StyleHelper.createPrimaryButton("✅ Seleziona");
            selezionaBtn.setOnAction(e -> selezionaRicetta());

            Button annullaBtn = new Button("❌ Annulla");
            annullaBtn.setPrefWidth(130);
            annullaBtn.setStyle("-fx-background-color: " + StyleHelper.NEUTRAL_GRAY + "; " +
                "-fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-font-weight: bold;");
            annullaBtn.setOnAction(e -> getRoot().getScene().getWindow().hide());

            buttonBox.getChildren().addAll(annullaBtn, selezionaBtn);
        } else {
            Button aggiornaBtn = StyleHelper.createInfoButton("🔄 Aggiorna");
            aggiornaBtn.setOnAction(e -> ricaricaTutto());
            buttonBox.getChildren().add(aggiornaBtn);
        }

        return buttonBox;
    }

    // ==================== EVENTI UI ====================

    private void onRicettaDoppioClick() {
        Ricetta selezionata = ricetteListView.getSelectionModel().getSelectedItem();
        if (selezionata != null) {
            if (modalitaSelezione) {
                selezionaRicetta();
            } else {
                mostraIngredienti(selezionata);
            }
        }
    }

    private void mostraIngredienti(Ricetta ricetta) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Ingredienti - " + ricetta.getNome());
        dialog.setHeaderText("🥕 Ingredienti di " + ricetta.getNome());

        StringBuilder sb = new StringBuilder();
        sb.append("⏱️ Tempo: ").append(ricetta.getTempoPreparazione()).append(" minuti\n");
        sb.append("🥕 Totale ingredienti: ").append(ricetta.getNumeroIngredienti()).append("\n\n");

        Map<Ingrediente, Double> ingredienti = ricetta.getIngredienti();
        if (ingredienti.isEmpty()) {
            sb.append("Nessun ingrediente trovato");
        } else {
            for (Map.Entry<Ingrediente, Double> entry : ingredienti.entrySet()) {
                Ingrediente ing = entry.getKey();
                Double quantita = entry.getValue();
                sb.append("🥕 ").append(ing.getNome())
                    .append(" (").append(ing.getTipo()).append(")")
                    .append(" - ").append(quantita).append("g\n");
            }
        }

        dialog.setContentText(sb.toString());
        
        // Styling migliorato
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-font-family: 'Roboto'; -fx-font-size: 13px;");
        dialogPane.lookupButton(ButtonType.OK).setStyle(
            "-fx-background-color: " + StyleHelper.PRIMARY_ORANGE + "; " +
            "-fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;"
        );
        
        dialog.showAndWait();
    }

    private void apriCreaRicetta() {
        try {
            CreaRicettaGUI creaGUI = new CreaRicettaGUI(controller.getGestioneRicette());
            Ricetta nuovaRicetta = creaGUI.showAndReturn();
            if (nuovaRicetta != null) {
                controller.invalidaCache();
                caricaRicette();
                showSuccessAlert("Ricetta '" + nuovaRicetta.getNome() + "' creata con successo!");
            }
        } catch (Exception e) {
            showErrorAlert("Errore nell'apertura creazione ricetta: " + e.getMessage());
        }
    }

    private void selezionaRicetta() {
        Ricetta selezionata = ricetteListView.getSelectionModel().getSelectedItem();
        if (selezionata == null) {
            showWarningAlert("Seleziona una ricetta dalla lista");
            return;
        }
        ricettaSelezionata = selezionata;
        getRoot().getScene().getWindow().hide();
    }

    // ==================== CARICAMENTO DATI (chiama controller) ====================

    private void caricaRicette() {
        try {
            List<Ricetta> ricette = controller.getAllRicette();
            ricetteData.setAll(ricette);
            aggiornaStatistiche();
        } catch (Exception e) {
            showErrorAlert("Errore nel caricamento ricette: " + e.getMessage());
            ricetteData.clear();
        }
    }

    private void applicaFiltri() {
        try {
            String nome = filtroNomeField.getText();
            Integer tempoMin = parseInteger(filtroTempoMinField.getText());
            Integer tempoMax = parseInteger(filtroTempoMaxField.getText());
            Integer ingredientiMin = parseInteger(filtroIngredientiMinField.getText());
            Integer ingredientiMax = parseInteger(filtroIngredientiMaxField.getText());

            // DELEGA AL CONTROLLER tutta la logica di filtro
            List<Ricetta> filtrate = controller.filtraCombinato(
                nome, tempoMin, tempoMax, ingredientiMin, ingredientiMax
            );
            
            ricetteData.setAll(filtrate);
        } catch (ValidationException e) {
            showWarningAlert(e.getMessage());
        } catch (Exception e) {
            showErrorAlert("Errore nell'applicazione filtri: " + e.getMessage());
        }
    }

    private void resetFiltri() {
        filtroNomeField.clear();
        filtroTempoMinField.clear();
        filtroTempoMaxField.clear();
        filtroIngredientiMinField.clear();
        filtroIngredientiMaxField.clear();
        caricaRicette();
    }

    private void ricaricaTutto() {
        try {
            controller.ricaricaCache();
            caricaRicette();
            showSuccessAlert("Ricette ricaricate dal database");
        } catch (Exception e) {
            showErrorAlert("Errore nel ricaricamento: " + e.getMessage());
        }
    }

    private void aggiornaStatistiche() {
        try {
            RicettaController.StatisticheRicette stats = controller.getStatistiche();
            statisticheLabel.setText(stats.toString());
        } catch (Exception e) {
            statisticheLabel.setText("❌ Errore nel calcolo statistiche");
        }
    }

    //  VALIDAZIONE

    private void validateNumericField(TextField field, String value) {
        if (value.isEmpty()) {
            field.setStyle(""); // Reset
        } else {
            try {
                Integer.parseInt(value);
                field.setStyle("-fx-border-color: " + StyleHelper.SUCCESS_GREEN + "; -fx-border-width: 2px;");
            } catch (NumberFormatException e) {
                field.setStyle("-fx-border-color: #ff6b6b; -fx-border-width: 2px;");
            }
        }
    }

    //  UTILITY

    private Integer parseInteger(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Label createSectionLabel(String text, String color) {
        Label label = new Label(text);
        label.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 18));
        label.setTextFill(Color.web(color));
        return label;
    }

    //  ALERT HELPERS 

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("✅ Successo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert, StyleHelper.SUCCESS_GREEN);
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("❌ Errore");
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert, "#dc3545");
        alert.showAndWait();
    }

    private void showWarningAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("⚠️ Attenzione");
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert, "#ffc107");
        alert.showAndWait();
    }

    private void styleAlert(Alert alert, String color) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-font-family: 'Roboto'; -fx-font-size: 14px;");
        
        dialogPane.lookupButton(ButtonType.OK).setStyle(
            "-fx-background-color: " + color + "; " +
            "-fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-cursor: hand; -fx-background-radius: 8; " +
            "-fx-padding: 10 20 10 20;"
        );
    }


    public Ricetta showAndReturn() {
        return ricettaSelezionata;
    }

    public void refresh() {
        caricaRicette();
    }
}