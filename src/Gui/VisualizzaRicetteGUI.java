package Gui;

import controller.VisualizzaRicetteController;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import model.Ricetta;
import service.GestioneRicette;

import java.util.Collections;
import java.util.List;

public class VisualizzaRicetteGUI {

    private VisualizzaRicetteController visualizzaController;
    private StackPane menuRoot;

    private final ObservableList<Ricetta> ricetteData = FXCollections.observableArrayList();
    private FilteredList<Ricetta> filteredRicette;

    private double xOffset = 0;
    private double yOffset = 0;

    private ProgressIndicator progressIndicator;
    private TableView<Ricetta> table;
    private TextField searchField;
    private ComboBox<String> tempoFilter;
    private ComboBox<String> ingredientiFilter;
    private Label countLabel;

    public void setController(VisualizzaRicetteController controller, StackPane menuRoot) {
        this.visualizzaController = controller;
        this.menuRoot = menuRoot;
    }

    public StackPane getRoot() {
        StackPane root = new StackPane();

       
        createOrangeBackground(root);

     
        VBox mainContainer = createMainContainer(root);
        root.getChildren().add(mainContainer);


        VBox headerSection = createHeader();
        mainContainer.getChildren().add(headerSection);

   
        HBox filterSection = createFilterSection();
        mainContainer.getChildren().add(filterSection);

        // ===== Tabella =====
        createTable();
        StackPane tableContainer = new StackPane(table);
        tableContainer.setStyle("-fx-background-color: rgba(255,255,255,0.95);" +
                               "-fx-background-radius: 20;" +
                               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 5);");
        VBox.setVgrow(tableContainer, Priority.ALWAYS);
        mainContainer.getChildren().add(tableContainer);

        
        createProgressIndicator(tableContainer);

     
        HBox actionSection = createActionButtons(root);
        mainContainer.getChildren().add(actionSection);

  
        setupFilters();

        addWindowControls(root);

      
        makeDraggable(root, headerSection);

        // Caricamento inizia
        Platform.runLater(this::refreshData);

        return root;
    }

    private void createOrangeBackground(StackPane root) {
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#FF9966")),
            new Stop(0.5, Color.web("#FFB366")),
            new Stop(1, Color.web("#FFCC99"))
        );
        Region background = new Region();
        background.setBackground(new Background(new BackgroundFill(gradient, null, null)));
        background.prefWidthProperty().bind(root.widthProperty());
        background.prefHeightProperty().bind(root.heightProperty());
        root.getChildren().add(background);

        for (int i = 0; i < 4; i++) {
            Circle decorCircle = new Circle(15 + Math.random() * 30);
            decorCircle.setFill(Color.web("#FFFFFF", 0.08));      
            decorCircle.setTranslateX((i - 2) * 120 + Math.random() * 60);
            decorCircle.setTranslateY((i % 2 == 0 ? -1 : 1) * (80 + Math.random() * 40));
            root.getChildren().add(decorCircle);
        }
    }

    private VBox createMainContainer(StackPane root) {
        VBox container = new VBox(20);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(28));
        container.prefWidthProperty().bind(root.widthProperty().multiply(0.92));
        container.prefHeightProperty().bind(root.heightProperty().multiply(0.9));
        container.setStyle("-fx-background-color: rgba(255,255,255,0.18);" +
                          "-fx-background-radius: 20;" +
                          "-fx-border-radius: 20;" +
                          "-fx-border-color: rgba(255,255,255,0.35);" +
                          "-fx-border-width: 1;");
        DropShadow containerShadow = new DropShadow(18, Color.web("#000000", 0.12));
        containerShadow.setOffsetY(6);
        container.setEffect(containerShadow);
        return container;
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(8, 0, 12, 0));

        Label title = new Label("🍽️ Le Tue Ricette");
        title.setFont(Font.font("Inter", FontWeight.EXTRA_BOLD, 26));
        title.setTextFill(Color.WHITE);
        title.setEffect(new DropShadow(8, Color.web("#FF6600", 0.8)));

        Label subtitle = new Label("Esplora e gestisci le tue creazioni culinarie");
        subtitle.setFont(Font.font("Inter", FontWeight.NORMAL, 12));
        subtitle.setTextFill(Color.web("#ffffff", 0.9));

        header.getChildren().addAll(title, subtitle);
        return header;
    }

    private HBox createFilterSection() {
        HBox filters = new HBox(14);
        filters.setAlignment(Pos.CENTER);
        filters.setPadding(new Insets(0, 12, 6, 12));

        searchField = createOrangeTextField("🔍 Cerca ricette per nome...", 320);

        tempoFilter = createOrangeComboBox();
        tempoFilter.setPromptText("⏰ Tempo");
        tempoFilter.getItems().addAll("Tutti", "< 30 min", "30-60 min", "> 60 min");
        tempoFilter.setValue("Tutti");

        ingredientiFilter = createOrangeComboBox();
        ingredientiFilter.setPromptText("🥕 Ingredienti");
        ingredientiFilter.getItems().addAll("Tutti", "< 5", "5-8", "> 8");
        ingredientiFilter.setValue("Tutti");

        countLabel = new Label("0 ricette");
        countLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 12));
        countLabel.setTextFill(Color.WHITE);
        countLabel.setStyle("-fx-background-color: rgba(255,102,0,0.75); -fx-background-radius: 15; -fx-padding: 8 14;");

        filters.getChildren().addAll(searchField, tempoFilter, ingredientiFilter, countLabel);
        return filters;
    }

    private void createTable() {
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("🍽️ Nessuna ricetta trovata"));


        table.setFixedCellSize(42); 

      
        table.setStyle("-fx-background-color: transparent;" +
                      "-fx-table-cell-border-color: rgba(0,0,0,0.04);" +
                      "-fx-selection-bar: rgba(255,153,102,0.25);" +
                      "-fx-selection-bar-non-focused: rgba(255,153,102,0.18);");

   
        table.widthProperty().addListener((obs, oldVal, newVal) -> {
            table.lookupAll(".column-header-background").forEach(header -> {
                ((Region) header).setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #FFE0CC, #FFCC99);" +
                    "-fx-border-color: #e68a00;" +
                    "-fx-border-width: 0 0 1 0;"
                );
            });
            table.lookupAll(".label").forEach(label -> {
                if (label instanceof Label l) {
                    l.setFont(Font.font("Inter", FontWeight.BOLD, 13));
                    l.setTextFill(Color.web("#333333"));
                }
            });
        });

        TableColumn<Ricetta, String> nomeCol = new TableColumn<>("🍽️ Nome Ricetta");
        nomeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome() != null ? c.getValue().getNome() : ""));
        nomeCol.setMinWidth(240);

        TableColumn<Ricetta, Integer> tempoCol = new TableColumn<>("⏱️ Tempo (min)");
        tempoCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getTempoPreparazione()).asObject());
        tempoCol.setMinWidth(120);
        tempoCol.setCellFactory(col -> new TableCell<Ricetta, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item + " min");
                    if (item <= 30) setTextFill(Color.web("#2E7D32"));     // verde scuro
                    else if (item <= 60) setTextFill(Color.web("#E65100")); // arancione scuro
                    else setTextFill(Color.web("#B71C1C"));                 // rosso scuro
                    setFont(Font.font("Inter", 13));
                }
            }
        });

        TableColumn<Ricetta, Integer> ingredientiCol = new TableColumn<>("🥕 Ingredienti");
        ingredientiCol.setCellValueFactory(c -> new SimpleIntegerProperty(
                safeGetNumeroIngredienti(c.getValue())).asObject());
        ingredientiCol.setMinWidth(110);

        table.getColumns().addAll(nomeCol, tempoCol, ingredientiCol);

       
        table.setRowFactory(tv -> {
            TableRow<Ricetta> row = new TableRow<>() {
                @Override
                protected void updateItem(Ricetta item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setStyle("");
                    } else {
                        if (getIndex() % 2 == 0) {
                            setStyle("-fx-background-color: #ffffff;");
                        } else {
                            setStyle("-fx-background-color: #fafafa;");
                        }
                    }
                }
            };
            row.setOnMouseEntered(e -> {
                if (!row.isEmpty()) row.setStyle("-fx-background-color: #FFF3E0;");
            });
            row.setOnMouseExited(e -> {
                if (!row.isEmpty()) {
                    if (row.getIndex() % 2 == 0) row.setStyle("-fx-background-color: #ffffff;");
                    else row.setStyle("-fx-background-color: #fafafa;");
                }
            });
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && (!row.isEmpty())) {
                    Ricetta selected = row.getItem();
                    if (selected != null && visualizzaController != null) {
                        try {
                            visualizzaController.mostraDettagliRicetta(selected);
                        } catch (Exception e) {
                            showError("Errore aprendo i dettagli: " + e.getMessage());
                        }
                    }
                }
            });
            return row;
        });
    }

   
    private int safeGetNumeroIngredienti(Ricetta r) {
        try {
            return r.getNumeroIngredienti();
        } catch (Throwable t) {
            // fallback conservativo
            return 0;
        }
    }

    private void createProgressIndicator(StackPane parent) {
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(50, 50);
        progressIndicator.setStyle("-fx-accent: #FF6600;");
        progressIndicator.setVisible(false);
        parent.getChildren().add(progressIndicator);
    }

    private HBox createActionButtons(StackPane currentRoot) {
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(18, 0, 8, 0));

        Button addBtn = createOrangeButton("➕ Aggiungi Ricetta", 160);
        Button refreshBtn = createOrangeButton("🔄 Aggiorna", 140);
        Button backBtn = createOrangeButton("⬅️ Torna al Menu", 160);

        addBtn.setOnAction(e -> {
            if (visualizzaController == null) {
                showError("Controller non inizializzato.");
                return;
            }
            try {
                GestioneRicette gestione = visualizzaController.getGestioneRicette();
                Stage creaStage = new Stage();
               
                new CreaRicettaGUI(gestione, null).start(creaStage);
      
                creaStage.setOnHidden(ev -> refreshData());
            } catch (Exception ex) {
                showError("Errore aprendo il form di creazione: " + ex.getMessage());
            }
        });

        refreshBtn.setOnAction(e -> refreshData());

        backBtn.setOnAction(e -> {
            Stage stage = getStage(currentRoot);
            if (stage != null && menuRoot != null) {
                // ripristina il menuRoot come root della scena
                stage.getScene().setRoot(menuRoot);
            } else if (stage != null) {
                // fallback: chiudi lo stage
                stage.close();
            }
        });

        actions.getChildren().addAll(backBtn, addBtn, refreshBtn);
        return actions;
    }

    private TextField createOrangeTextField(String prompt, double width) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(40);
        field.setMaxWidth(width);
        field.setFont(Font.font("Inter", 13));
        field.setStyle("-fx-background-color: rgba(255,255,255,0.95);" +
                     "-fx-background-radius: 20;" +
                     "-fx-border-radius: 20;" +
                     "-fx-border-color: #FF9966;" +
                     "-fx-border-width: 1.2;" +
                     "-fx-text-fill: #333;" +
                     "-fx-prompt-text-fill: #999;" +
                     "-fx-padding: 0 14;");
        field.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) field.setStyle(field.getStyle().replace("-fx-border-color: #FF9966;", "-fx-border-color: #FF6600;"));
            else field.setStyle(field.getStyle().replace("-fx-border-color: #FF6600;", "-fx-border-color: #FF9966;"));
        });
        return field;
    }

    private ComboBox<String> createOrangeComboBox() {
        ComboBox<String> combo = new ComboBox<>();
        combo.setPrefHeight(40);
        combo.setMaxWidth(140);
        combo.setStyle("-fx-background-color: rgba(255,255,255,0.95);" +
                      "-fx-background-radius: 20;" +
                      "-fx-border-radius: 20;" +
                      "-fx-border-color: #FF9966;" +
                      "-fx-border-width: 1.2;" +
                      "-fx-text-fill: #333;");
        return combo;
    }

    private Button createOrangeButton(String text, double width) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Inter", FontWeight.BOLD, 14)); 
        btn.setTextFill(Color.WHITE);
        btn.setPrefWidth(width);
        btn.setPrefHeight(44);


        String baseStyle = "-fx-background-color: #FF6600;" + 
                           "-fx-background-radius: 22;" +
                           "-fx-cursor: hand;" +
                           "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8, 0, 0, 3);";

        String hoverStyle = "-fx-background-color: #FF8533;" +  
                            "-fx-background-radius: 22;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 10, 0, 0, 4);";

        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        btn.setOnMousePressed(e -> { btn.setScaleX(0.97); btn.setScaleY(0.97); });
        btn.setOnMouseReleased(e -> { btn.setScaleX(1.0); btn.setScaleY(1.0); });

        return btn;
    }


    private void setupFilters() {
        filteredRicette = new FilteredList<>(ricetteData, p -> true);
        SortedList<Ricetta> sorted = new SortedList<>(filteredRicette);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);

        searchField.textProperty().addListener((obs, old, text) -> updateFilters());
        tempoFilter.valueProperty().addListener((obs, old, value) -> updateFilters());
        ingredientiFilter.valueProperty().addListener((obs, old, value) -> updateFilters());

        // aggiorna contatore quando la lista cambia
        ricetteData.addListener((javafx.collections.ListChangeListener<Ricetta>) c -> updateCountLabel());
        // filteredRicette non è ObservableList diretta, ma il listener su ricetteData + predicate mantiene count aggiornato
        updateCountLabel();
    }

    private void updateFilters() {
        filteredRicette.setPredicate(ricetta -> {
            if (ricetta == null) return false;

            String searchText = searchField.getText();
            boolean matchesSearch = searchText == null || searchText.isEmpty() ||
                    (ricetta.getNome() != null && ricetta.getNome().toLowerCase().contains(searchText.toLowerCase()));

            String tempoValue = tempoFilter.getValue();
            boolean matchesTempo = true;
            if (tempoValue != null && !tempoValue.equals("Tutti")) {
                int tempo = ricetta.getTempoPreparazione();
                switch (tempoValue) {
                    case "< 30 min": matchesTempo = tempo < 30; break;
                    case "30-60 min": matchesTempo = tempo >= 30 && tempo <= 60; break;
                    case "> 60 min": matchesTempo = tempo > 60; break;
                    default: matchesTempo = true;
                }
            }

            String ingredientiValue = ingredientiFilter.getValue();
            boolean matchesIngredienti = true;
            if (ingredientiValue != null && !ingredientiValue.equals("Tutti")) {
                int numIngredienti = safeGetNumeroIngredienti(ricetta);
                switch (ingredientiValue) {
                    case "< 5": matchesIngredienti = numIngredienti < 5; break;
                    case "5-8": matchesIngredienti = numIngredienti >= 5 && numIngredienti <= 8; break;
                    case "> 8": matchesIngredienti = numIngredienti > 8; break;
                    default: matchesIngredienti = true;
                }
            }

            return matchesSearch && matchesTempo && matchesIngredienti;
        });
        updateCountLabel();
    }

    private void updateCountLabel() {
        Platform.runLater(() -> {
            int count = filteredRicette == null ? ricetteData.size() : filteredRicette.size();
            String text = count + (count == 1 ? " ricetta" : " ricette");
            countLabel.setText(text);
        });
    }

    private void refreshData() {
        if (visualizzaController == null) {
            if (progressIndicator != null) progressIndicator.setVisible(false);
            return;
        }

        Task<List<Ricetta>> task = new Task<>() {
            @Override
            protected List<Ricetta> call() throws Exception {
                List<Ricetta> list = visualizzaController.getTutteLeRicette();
                return list == null ? Collections.emptyList() : list;
            }

            @Override
            protected void succeeded() {
                ricetteData.setAll(getValue());
                progressIndicator.setVisible(false);
                updateFilters(); // applica i filtri alla nuova lista
            }

            @Override
            protected void failed() {
                progressIndicator.setVisible(false);
                showError("Errore caricando ricette: " + getException().getMessage());
            }
        };

        progressIndicator.setVisible(true);
        Thread t = new Thread(task, "RefreshRicetteThread");
        t.setDaemon(true);
        t.start();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    private void addWindowControls(StackPane root) {
        HBox controls = new HBox(6);
        controls.setAlignment(Pos.TOP_RIGHT);
        controls.setPadding(new Insets(8));
        controls.setPickOnBounds(false); // non bloccare click su altri nodi

        Button minimizeBtn = createWindowButton("_", Color.WHITE);
        Button maximizeBtn = createWindowButton("⬜", Color.WHITE);
        Button closeBtn = createWindowButton("✖", Color.web("#FF4444"));

     
        minimizeBtn.setOnAction(e -> {
            Stage stage = getStage(root);
            if (stage != null) stage.setIconified(true);
        });
        maximizeBtn.setOnAction(e -> {
            Stage stage = getStage(root);
            if (stage != null) stage.setMaximized(!stage.isMaximized());
        });
        closeBtn.setOnAction(e -> {
            Stage stage = getStage(root);
            if (stage != null) stage.close();
        });

        controls.getChildren().addAll(minimizeBtn, maximizeBtn, closeBtn);
        StackPane.setAlignment(controls, Pos.TOP_RIGHT);
        root.getChildren().add(controls);
    }

    private Button createWindowButton(String symbol, Color color) {
        Button btn = new Button(symbol);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        btn.setTextFill(color);
        btn.setStyle("-fx-background-color: transparent;");
        btn.setOnMouseEntered(e -> btn.setTextFill(color.brighter()));
        btn.setOnMouseExited(e -> btn.setTextFill(color));
        btn.setPrefSize(28, 28);
        return btn;
    }

    private void makeDraggable(StackPane root, Node dragNode) {
        dragNode.setOnMousePressed(e -> {
            xOffset = e.getSceneX();
            yOffset = e.getSceneY();
        });
        dragNode.setOnMouseDragged(e -> {
            Stage stage = getStage(root);
            if (stage != null && !stage.isMaximized()) {
                stage.setX(e.getScreenX() - xOffset);
                stage.setY(e.getScreenY() - yOffset);
            }
        });
    }

    private Stage getStage(Node node) {
        if (node == null) return null;
        Scene s = node.getScene();
        if (s == null) return null;
        if (s.getWindow() instanceof Stage) return (Stage) s.getWindow();
        return null;
    }
}
