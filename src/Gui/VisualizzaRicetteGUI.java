package Gui;

import controller.VisualizzaRicetteController;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Ingrediente;
import model.Ricetta;

import java.util.Collections;
import java.util.List;

public class VisualizzaRicetteGUI {

    private VisualizzaRicetteController visualizzaController;
    private StackPane menuRoot;

    private final ObservableList<Ricetta> ricetteData = FXCollections.observableArrayList();
    private FilteredList<Ricetta> filteredRicette;
    private List<Ricetta> cachedRicetteChef = null;

    private double xOffset = 0;
    private double yOffset = 0;
    private ProgressIndicator progressIndicator;
    private final PauseTransition filterPause = new PauseTransition(javafx.util.Duration.millis(350));

    public void setController(VisualizzaRicetteController visualizzaController, StackPane menuRoot) {
        this.visualizzaController = visualizzaController;
        this.menuRoot = menuRoot;
    }

    public StackPane getRoot() {
        StackPane root = new StackPane();

        // background gradient
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FF9966")), new Stop(1, Color.web("#FFCC99")));
        Region background = new Region();
        background.setBackground(new Background(new BackgroundFill(gradient, null, null)));
        background.prefWidthProperty().bind(root.widthProperty());
        background.prefHeightProperty().bind(root.heightProperty());
        root.getChildren().add(background);

        // card principale
        VBox card = new VBox(16);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(20));
        card.prefWidthProperty().bind(root.widthProperty().multiply(0.95));
        card.prefHeightProperty().bind(root.heightProperty().multiply(0.9));

        DropShadow shadow = new DropShadow();
        shadow.setRadius(12);
        shadow.setColor(Color.web("#000000", 0.12));
        shadow.setOffsetY(4);
        card.setEffect(shadow);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-border-radius: 16; -fx-border-color: #FF9966; -fx-border-width: 2;");
        root.getChildren().add(card);

        // header con filtri
        VBox headerSection = new VBox(10);
        headerSection.setAlignment(Pos.CENTER);
        Label title = new Label("🍽 Lista delle Ricette");
        title.setFont(Font.font("Roboto", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#FF6600"));

        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER);
        TextField nomeField = createModernTextField("Cerca per nome...", "🍲");
        TextField ingredienteField = createModernTextField("Cerca per ingrediente...", "🧂");
        filters.getChildren().addAll(nomeField, ingredienteField);

        headerSection.getChildren().addAll(title, filters);
        card.getChildren().add(headerSection);

        // tabella
        TableView<Ricetta> table = createOptimizedTable();
        table.prefHeightProperty().bind(card.heightProperty().multiply(0.55));
        table.prefWidthProperty().bind(card.widthProperty().multiply(0.95));
        VBox.setVgrow(table, Priority.ALWAYS);

        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(40, 40);
        progressIndicator.setStyle("-fx-accent: #FF6600;");
        progressIndicator.setVisible(false);

        StackPane tableContainer = new StackPane(table, progressIndicator);
        card.getChildren().add(tableContainer);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        // buttons
        VBox buttonSection = new VBox(12);
        buttonSection.setAlignment(Pos.CENTER);
        HBox actionButtons = new HBox(12);
        actionButtons.setAlignment(Pos.CENTER);
        Button mostraTuttiBtn = createStylishButton("📋 Tutte le Ricette", "#FF6600", "#FF8533");
        Button mieBtn = createStylishButton("👨‍🍳 Le mie Ricette", "#FF6600", "#FF8533");
        actionButtons.getChildren().addAll(mostraTuttiBtn, mieBtn);
        Button backBtn = createStylishButton("⬅ Torna al Menu", "#FFCC99", "#FFD9B3");
        backBtn.setPrefWidth(180);
        buttonSection.getChildren().addAll(actionButtons, backBtn);
        card.getChildren().add(buttonSection);

        boolean embedded = (this.menuRoot != null);
        if (embedded) {
            backBtn.setVisible(false);
            backBtn.setManaged(false);
        }

        filteredRicette = new FilteredList<>(ricetteData, p -> true);
        SortedList<Ricetta> sorted = new SortedList<>(filteredRicette);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);

        loadDataAsync();
        setupFilters(nomeField, ingredienteField, mostraTuttiBtn, mieBtn);
        setupTableDoubleClick(table, root);
        setupBackButton(backBtn, root);

        if (!embedded) {
            HBox windowButtons = createWindowButtons(root);
            root.getChildren().add(windowButtons);
            StackPane.setAlignment(windowButtons, Pos.TOP_RIGHT);
            StackPane.setMargin(windowButtons, new Insets(8));
        }

        makeDraggable(root, headerSection);

        return root;
    }

    private void loadDataAsync() {
        if (visualizzaController == null) {
            progressIndicator.setVisible(false);
            return;
        }

        Task<List<Ricetta>> loadTask = new Task<>() {
            @Override
            protected List<Ricetta> call() throws Exception {
                List<Ricetta> list = visualizzaController.getTutteLeRicette();
                return list == null ? Collections.emptyList() : list;
            }

            @Override
            protected void succeeded() {
                List<Ricetta> loaded = getValue();
                Platform.runLater(() -> {
                    ricetteData.setAll(loaded);
                    progressIndicator.setVisible(false);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    new Alert(Alert.AlertType.ERROR,
                            "Errore caricando ricette: " + getException().getMessage(),
                            ButtonType.OK).showAndWait();
                });
            }
        };

        progressIndicator.setVisible(true);
        Thread t = new Thread(loadTask, "LoadRicetteThread");
        t.setDaemon(true);
        t.start();
    }

    private TextField createModernTextField(String prompt, String icon) {
        TextField field = new TextField();
        field.setPromptText(icon + " " + prompt);
        field.setPrefHeight(35);
        field.setPrefWidth(190);
        field.setFont(Font.font("Roboto", 13));
        field.setStyle("-fx-background-color: #f8f9fa;"
                + "-fx-background-radius: 18;"
                + "-fx-border-radius: 18;"
                + "-fx-border-color: #dee2e6;"
                + "-fx-border-width: 1;"
                + "-fx-padding: 0 12 0 12;");

        field.setOnMouseEntered(e -> field.setStyle("-fx-background-color: #e9ecef;"
                + "-fx-background-radius: 18;"
                + "-fx-border-radius: 18;"
                + "-fx-border-color: #FF9966;"
                + "-fx-border-width: 2;"
                + "-fx-padding: 0 12 0 12;"));

        field.setOnMouseExited(e -> field.setStyle("-fx-background-color: #f8f9fa;"
                + "-fx-background-radius: 18;"
                + "-fx-border-radius: 18;"
                + "-fx-border-color: #dee2e6;"
                + "-fx-border-width: 1;"
                + "-fx-padding: 0 12 0 12;"));

        return field;
    }

    private TableView<Ricetta> createOptimizedTable() {
        TableView<Ricetta> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(Region.USE_COMPUTED_SIZE);
        table.setMaxHeight(Double.MAX_VALUE);

        table.setRowFactory(tv -> {
            TableRow<Ricetta> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null && row.getIndex() % 2 == 0) {
                    row.setStyle("-fx-background-color: #f8f9fa;");
                } else {
                    row.setStyle("-fx-background-color: white;");
                }
            });
            return row;
        });

        table.setStyle("-fx-background-color: white;"
                + "-fx-background-radius: 12;"
                + "-fx-border-radius: 12;"
                + "-fx-border-color: #dee2e6;"
                + "-fx-border-width: 1;"
                + "-fx-table-header-border-color: #FF9966;");

        // colonne
        TableColumn<Ricetta, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getIdRicetta()).asObject());

        TableColumn<Ricetta, String> nomeCol = new TableColumn<>("🍲 Nome Ricetta");
        nomeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome() != null ? c.getValue().getNome() : ""));

        TableColumn<Ricetta, Integer> numIngredientiCol = new TableColumn<>("Ingredienti");
        numIngredientiCol.setCellValueFactory(c -> new SimpleIntegerProperty(
                c.getValue().getIngredienti() != null ? c.getValue().getIngredienti().size() : 0
        ).asObject());

    
        table.getColumns().addAll(idCol, nomeCol, numIngredientiCol);
        table.getSortOrder().add(nomeCol);

        return table;
    }

    private Button createStylishButton(String text, String baseColor, String hoverColor) {
        Button button = new Button(text);
        button.setPrefSize(150, 40);
        button.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
        button.setTextFill(Color.web("#4B2E2E"));
        button.setStyle("-fx-background-color: " + baseColor + ";" + "-fx-background-radius: 20;" + "-fx-cursor: hand;"
                + "-fx-border-color: transparent;" + "-fx-border-width: 0;");

        DropShadow shadow = new DropShadow();
        shadow.setRadius(6);
        shadow.setColor(Color.web("#000000", 0.15));
        shadow.setOffsetY(2);
        button.setEffect(shadow);

        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-background-color: " + hoverColor + ";" + "-fx-background-radius: 20;" + "-fx-cursor: hand;");
            DropShadow hoverShadow = new DropShadow();
            hoverShadow.setRadius(8);
            hoverShadow.setColor(Color.web("#000000", 0.25));
            hoverShadow.setOffsetY(3);
            button.setEffect(hoverShadow);
        });

        button.setOnMouseExited(e -> {
            button.setStyle("-fx-background-color: " + baseColor + ";" + "-fx-background-radius: 20;" + "-fx-cursor: hand;");
            button.setEffect(shadow);
        });

        return button;
    }

    private void setupFilters(TextField nomeField, TextField ingredienteField, Button mostraTuttiBtn, Button mieBtn) {
        nomeField.textProperty().addListener((obs, o, n) -> {
            filterPause.setOnFinished(e -> applicaFiltriLocali(nomeField.getText(), ingredienteField.getText(), false));
            filterPause.playFromStart();
        });
        ingredienteField.textProperty().addListener((obs, o, n) -> {
            filterPause.setOnFinished(e -> applicaFiltriLocali(nomeField.getText(), ingredienteField.getText(), false));
            filterPause.playFromStart();
        });

        mostraTuttiBtn.setOnAction(e -> {
            nomeField.clear();
            ingredienteField.clear();
            filteredRicette.setPredicate(p -> true);
        });

        mieBtn.setOnAction(e -> applicaFiltriLocali(nomeField.getText(), ingredienteField.getText(), true));
    }

    private void setupTableDoubleClick(TableView<Ricetta> table, StackPane root) {
        table.setRowFactory(tv -> {
            TableRow<Ricetta> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
                    Ricetta selected = row.getItem();
                    // Implementare popup dettagli ricetta
                }
            });
            return row;
        });
    }

    private void setupBackButton(Button backBtn, StackPane root) {
        if (this.menuRoot == null) {
            backBtn.setOnAction(e -> {
                Stage stage = getStage(root);
                if (stage != null && stage.getScene() != null) {
                    stage.getScene().setRoot(menuRoot);
                } else {
                    root.getChildren().setAll(menuRoot);
                }
            });
        }
    }

    private HBox createWindowButtons(StackPane root) {
        Button closeButton = new Button("✕");
        Button minimizeButton = new Button("_");
        Button maximizeButton = new Button("□");

        Button[] buttons = {minimizeButton, maximizeButton, closeButton};
        for (Button btn : buttons) {
            btn.setPrefSize(32, 32);
            btn.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
            btn.setTextFill(Color.WHITE);
            btn.setStyle("-fx-background-color: rgba(255,140,0,0.7); -fx-background-radius: 16; -fx-cursor: hand;");
        }

        closeButton.setOnAction(e -> { Stage stage = getStage(root); if (stage != null) stage.close(); });
        minimizeButton.setOnAction(e -> { Stage stage = getStage(root); if (stage != null) stage.setIconified(true); });
        maximizeButton.setOnAction(e -> { Stage stage = getStage(root); if (stage != null) stage.setMaximized(!stage.isMaximized()); });

        HBox box = new HBox(4, minimizeButton, maximizeButton, closeButton);
        box.setAlignment(Pos.TOP_RIGHT);
        return box;
    }

    private Stage getStage(Node node) {
        if (node == null) return null;
        Scene s = node.getScene();
        if (s == null) return null;
        if (s.getWindow() instanceof Stage) return (Stage) s.getWindow();
        return null;
    }

    private void makeDraggable(StackPane pane, Node dragHandle) {
        dragHandle.setOnMousePressed(event -> {
            Stage stage = getStage(pane);
            if (stage != null) { xOffset = event.getSceneX(); yOffset = event.getSceneY(); }
        });

        dragHandle.setOnMouseDragged(event -> {
            Stage stage = getStage(pane);
            if (stage != null) { stage.setX(event.getScreenX() - xOffset); stage.setY(event.getScreenY() - yOffset); }
        });
    }

    private void applicaFiltriLocali(String nome, String ingrediente, boolean soloChefLoggato) {
        if (filteredRicette == null) return;

        progressIndicator.setVisible(true);

        Platform.runLater(() -> {
            String n = nome == null ? "" : nome.toLowerCase().trim();
            String i = ingrediente == null ? "" : ingrediente.toLowerCase().trim();

            filteredRicette.setPredicate(r -> {
                if (r == null) return false;
                boolean matchNome = n.isEmpty() || (r.getNome() != null && r.getNome().toLowerCase().contains(n));

                boolean matchIngrediente = i.isEmpty() || (r.getIngredienti() != null &&
                        r.getIngredienti().keySet().stream()
                                .map(Ingrediente::getNome)
                                .anyMatch(nomeIng -> nomeIng.toLowerCase().contains(i))
                );

                boolean match = matchNome && matchIngrediente;

                if (soloChefLoggato) {
                    return match && (cachedRicetteChef != null && cachedRicetteChef.contains(r));
                }
                return match;
            });

            progressIndicator.setVisible(false);
        });
    }
}
