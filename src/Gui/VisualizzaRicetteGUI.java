package Gui;

import controller.RicettaController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import model.Ricetta;
import model.Ingrediente;
import util.StyleHelper;
import exceptions.ValidationException;

import java.util.List;
import java.util.Map;

public class VisualizzaRicetteGUI {
    private RicettaController ricettaController;
    private ObservableList<Ricetta> ricetteData;
    private ListView<Ricetta> ricetteListView;
    private TextField filtroNomeField;
    private TextField filtroTempoMinField;
    private TextField filtroTempoMaxField;
    private TextField filtroIngredientiMinField;
    private TextField filtroIngredientiMaxField;
    private Button resetFiltriBtn;
    
    private StackPane parentPane;

    private boolean modalitaSelezione = false;
    private Ricetta ricettaSelezionata = null;
    private VBox root;

    public VisualizzaRicetteGUI(RicettaController ricettaController) {
        this.ricettaController = ricettaController;
        this.ricetteData = FXCollections.observableArrayList();
        caricaRicette();
    }
    
    public void setParentPane(StackPane parentPane) {
        this.parentPane = parentPane;
    }

    public void setSelectionMode(boolean modalitaSelezione) {
        this.modalitaSelezione = modalitaSelezione;
    }

    public VBox getRoot() {
        if (root == null) {
            root = createMainLayout();
            setupListeners();
        }
        return root;
    }

    private VBox createMainLayout() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        StyleHelper.applyOrangeBackground(container);

        Label titleLabel = StyleHelper.createTitleLabel("📖 Gestione Ricette");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setTextFill(Color.WHITE);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox contentBox = new VBox(15);
        contentBox.getChildren().addAll(
            createFiltriSection(),
            new Separator(),
            createListaSection(),
            new Separator(),
            createButtonSection()
        );

        scrollPane.setContent(contentBox);
        container.getChildren().addAll(titleLabel, scrollPane);
        return container;
    }

    private VBox createFiltriSection() {
        VBox section = StyleHelper.createSection();
        Label sectionTitle = new Label("🔍 Filtri Avanzati");
        sectionTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        HBox nomeBox = new HBox(10);
        nomeBox.setAlignment(Pos.CENTER_LEFT);
        filtroNomeField = StyleHelper.createTextField("Cerca per nome ricetta...");
        filtroNomeField.setPrefWidth(300);
        nomeBox.getChildren().addAll(StyleHelper.createLabel("Nome:"), filtroNomeField);

        GridPane filtriGrid = new GridPane();
        filtriGrid.setHgap(15);
        filtriGrid.setVgap(10);

        filtroTempoMinField = StyleHelper.createTextField("Min");
        filtroTempoMinField.setPrefWidth(80);
        filtroTempoMaxField = StyleHelper.createTextField("Max");
        filtroTempoMaxField.setPrefWidth(80);
        HBox tempoBox = new HBox(5, filtroTempoMinField, new Label("-"), filtroTempoMaxField, new Label("min"));
        tempoBox.setAlignment(Pos.CENTER_LEFT);

        filtroIngredientiMinField = StyleHelper.createTextField("Min");
        filtroIngredientiMinField.setPrefWidth(80);
        filtroIngredientiMaxField = StyleHelper.createTextField("Max");
        filtroIngredientiMaxField.setPrefWidth(80);
        HBox ingredientiBox = new HBox(5, filtroIngredientiMinField, new Label("-"), filtroIngredientiMaxField);
        ingredientiBox.setAlignment(Pos.CENTER_LEFT);

        resetFiltriBtn = StyleHelper.createInfoButton("🔄 Reset Filtri");
        resetFiltriBtn.setOnAction(e -> resetFiltri());

        filtriGrid.add(StyleHelper.createLabel("Tempo prep.:"), 0, 0);
        filtriGrid.add(tempoBox, 1, 0);
        filtriGrid.add(StyleHelper.createLabel("N° ingredienti:"), 2, 0);
        filtriGrid.add(ingredientiBox, 3, 0);
        filtriGrid.add(resetFiltriBtn, 4, 0);

        section.getChildren().addAll(sectionTitle, nomeBox, filtriGrid);
        return section;
    }

    private VBox createListaSection() {
        VBox section = StyleHelper.createSection();
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = new Label("📋 Lista Ricette");
        sectionTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        Label istruzioniLabel = new Label("💡 Doppio click per vedere ingredienti");
        istruzioniLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
        istruzioniLabel.setTextFill(Color.web(StyleHelper.INFO_BLUE));

        VBox titleBox = new VBox(5);
        titleBox.getChildren().addAll(sectionTitle, istruzioniLabel);

        if (!modalitaSelezione) {
            Button creaRicettaBtn = StyleHelper.createSuccessButton("➕ Crea Nuova");
            creaRicettaBtn.setOnAction(e -> apriCreaRicetta());
            headerBox.getChildren().addAll(titleBox, new Region(), creaRicettaBtn);
            HBox.setHgrow(headerBox.getChildren().get(1), Priority.ALWAYS);
        } else {
            headerBox.getChildren().add(titleBox);
        }

        ricetteListView = new ListView<>();
        ricetteListView.setPrefHeight(350);
        ricetteListView.setItems(ricetteData);
        StyleHelper.applyListViewStyle(ricetteListView);

        ricetteListView.setCellFactory(listView -> new ListCell<Ricetta>() {
            @Override
            protected void updateItem(Ricetta item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox cellBox = new HBox(15);
                    cellBox.setAlignment(Pos.CENTER_LEFT);
                    cellBox.setPadding(new Insets(12));

                    VBox infoBox = new VBox(5);
                    Label nomeLabel = new Label("📖 " + item.getNome());
                    nomeLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 16));
                    nomeLabel.setTextFill(Color.BLACK);

                    HBox detailsBox = new HBox(20);
                    detailsBox.setAlignment(Pos.CENTER_LEFT);

                    Label tempoLabel = new Label("⏱️ " + item.getTempoPreparazione() + " min");
                    tempoLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
                    tempoLabel.setTextFill(Color.web(StyleHelper.INFO_BLUE));

                    Label ingredientiLabel = new Label("🥕 " + item.getNumeroIngredienti() + " ingredienti");
                    ingredientiLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
                    ingredientiLabel.setTextFill(Color.web(StyleHelper.SUCCESS_GREEN));

                    detailsBox.getChildren().addAll(tempoLabel, ingredientiLabel);
                    infoBox.getChildren().addAll(nomeLabel, detailsBox);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    cellBox.getChildren().addAll(infoBox, spacer);
                    setGraphic(cellBox);
                    setText(null);
                }
            }
        });

        ricetteListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Ricetta selezionata = ricetteListView.getSelectionModel().getSelectedItem();
                if (selezionata != null) {
                    if (modalitaSelezione) {
                        selezionaRicetta();
                    } else {
                        mostraIngredienti(selezionata);
                    }
                }
            }
        });

        section.getChildren().addAll(headerBox, ricetteListView);
        return section;
    }

    private void mostraIngredienti(Ricetta ricetta) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Ingredienti - " + ricetta.getNome());
        dialog.setHeaderText("🥕 Ingredienti di " + ricetta.getNome());

        StringBuilder ingredientiText = new StringBuilder();
        ingredientiText.append("⏱️ Tempo: ").append(ricetta.getTempoPreparazione()).append(" minuti\n");
        ingredientiText.append("🥕 Totale ingredienti: ").append(ricetta.getNumeroIngredienti()).append("\n\n");

        Map<Ingrediente, Double> ingredienti = ricetta.getIngredienti();
        if (ingredienti.isEmpty()) {
            ingredientiText.append("Nessun ingrediente trovato");
        } else {
            for (Map.Entry<Ingrediente, Double> entry : ingredienti.entrySet()) {
                Ingrediente ingrediente = entry.getKey();
                Double quantita = entry.getValue();
                ingredientiText.append("🥕 ").append(ingrediente.getNome())
                    .append(" (").append(ingrediente.getTipo()).append(")")
                    .append(" - ").append(quantita).append("g\n");
            }
        }

        dialog.setContentText(ingredientiText.toString());
        dialog.showAndWait();
    }

    private HBox createButtonSection() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        if (modalitaSelezione) {
            Button selezionaBtn = StyleHelper.createPrimaryButton("✅ Seleziona");
            selezionaBtn.setOnAction(e -> selezionaRicetta());

            Button annullaBtn = StyleHelper.createDangerButton("❌ Annulla");

            buttonBox.getChildren().addAll(annullaBtn, selezionaBtn);
        } else {
            Button aggiornaCacheBtn = StyleHelper.createInfoButton("🔄 Aggiorna");
            aggiornaCacheBtn.setOnAction(e -> {
                try {
                    ricettaController.ricaricaCache();
                    caricaRicette();
                    StyleHelper.showSuccessDialog("Successo", "Ricette ricaricate dal database");
                } catch (Exception ex) {
                    StyleHelper.showErrorDialog("Errore", "Errore nel ricaricamento: " + ex.getMessage());
                }
            });
            buttonBox.getChildren().add(aggiornaCacheBtn);
        }

        return buttonBox;
    }

    private void setupListeners() {
        filtroNomeField.textProperty().addListener((obs, old, val) -> applicaFiltri());
        filtroTempoMinField.textProperty().addListener((obs, old, val) -> applicaFiltri());
        filtroTempoMaxField.textProperty().addListener((obs, old, val) -> applicaFiltri());
        filtroIngredientiMinField.textProperty().addListener((obs, old, val) -> applicaFiltri());
        filtroIngredientiMaxField.textProperty().addListener((obs, old, val) -> applicaFiltri());
    }

    private void applicaFiltri() {
        try {
            String nome = filtroNomeField.getText();
            Integer tempoMin = parseIntSafe(filtroTempoMinField.getText());
            Integer tempoMax = parseIntSafe(filtroTempoMaxField.getText());
            Integer ingredientiMin = parseIntSafe(filtroIngredientiMinField.getText());
            Integer ingredientiMax = parseIntSafe(filtroIngredientiMaxField.getText());

            // Controller fa TUTTE le validazioni
            List<Ricetta> filtrate = ricettaController.filtraCombinato(
                nome, tempoMin, tempoMax, ingredientiMin, ingredientiMax
            );
            ricetteData.setAll(filtrate);

        } catch (ValidationException e) {
            StyleHelper.showValidationDialog("Validazione", e.getMessage());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore nell'applicazione filtri: " + e.getMessage());
        }
    }

    private Integer parseIntSafe(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void caricaRicette() {
        try {
            ricetteData.setAll(ricettaController.getAllRicette());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore nel caricamento ricette: " + e.getMessage());
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

    private void apriCreaRicetta() {
        try {
            dao.IngredienteDAO ingredienteDAO = new dao.IngredienteDAO();
            service.GestioneIngrediente gestioneIngrediente = new service.GestioneIngrediente(ingredienteDAO);
            controller.IngredienteController ingredienteController = new controller.IngredienteController(gestioneIngrediente);
            
            CreaRicettaGUI creaGUI = new CreaRicettaGUI(ricettaController, ingredienteController);
            Ricetta nuovaRicetta = creaGUI.showAndReturn();
            if (nuovaRicetta != null) {
                caricaRicette();
                StyleHelper.showSuccessDialog("Successo", "Ricetta '" + nuovaRicetta.getNome() + "' creata con successo!");
            }
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore nell'apertura creazione ricetta: " + e.getMessage());
        }
    }

    private void selezionaRicetta() {
        Ricetta selezionata = ricetteListView.getSelectionModel().getSelectedItem();
        if (selezionata == null) {
            StyleHelper.showValidationDialog("Validazione", "Seleziona una ricetta dalla lista");
            return;
        }
        ricettaSelezionata = selezionata;
    }

    public Ricetta showAndReturn() {
        return ricettaSelezionata;
    }
}
