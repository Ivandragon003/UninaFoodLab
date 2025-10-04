package Gui;

import controller.GestioneCorsoController;
import controller.VisualizzaCorsiController;
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
import javafx.util.Duration;
import model.CorsoCucina;

import java.util.Collections;
import java.util.List;

public class VisualizzaCorsiGUI {

    private VisualizzaCorsiController visualizzaController;
    private GestioneCorsoController gestioneCorsoController;
    private StackPane contentRoot;

    private final ObservableList<CorsoCucina> corsiData = FXCollections.observableArrayList();
    private FilteredList<CorsoCucina> filteredCorsi;

    private List<CorsoCucina> cachedCorsiChef = null;

    private double xOffset = 0;
    private double yOffset = 0;
    private ProgressIndicator progressIndicator;
    private PauseTransition filterPause = new PauseTransition(Duration.millis(350));
    private double stageXOffset = 0;
    private double stageYOffset = 0;

    public void setControllers(VisualizzaCorsiController visualizzaController,
                               GestioneCorsoController gestioneCorsoController, StackPane contentRoot) {
        this.visualizzaController = visualizzaController;
        this.gestioneCorsoController = gestioneCorsoController;
        this.contentRoot = contentRoot;
    }

    public StackPane getRoot() {
        StackPane root = new StackPane();
        root.setMinSize(400, 400);
        
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FF9966")), new Stop(1, Color.web("#FFCC99")));
        Region background = new Region();
        background.setBackground(new Background(new BackgroundFill(gradient, null, null)));
        background.prefWidthProperty().bind(root.widthProperty());
        background.prefHeightProperty().bind(root.heightProperty());
        root.getChildren().add(background);

        VBox card = new VBox(16);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(20));
        card.setMaxWidth(Double.MAX_VALUE);
        card.prefWidthProperty().bind(root.widthProperty());
        card.prefHeightProperty().bind(root.heightProperty());

        DropShadow shadow = new DropShadow();
        shadow.setRadius(12);
        shadow.setColor(Color.rgb(0, 0, 0, 0.12));
        shadow.setOffsetY(4);
        card.setEffect(shadow);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-border-radius: 16; " +
                "-fx-border-color: #FF9966; -fx-border-width: 2;");
        root.getChildren().add(card);

        VBox headerSection = new VBox(10);
        headerSection.setAlignment(Pos.CENTER);
        Label title = new Label("📚 Lista dei Corsi");
        title.setFont(Font.font("Roboto", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#FF6600"));

        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER);
        TextField nomeField = createModernTextField("Cerca per nome...", "👨‍🍳");
        TextField argomentoField = createModernTextField("Cerca per argomento...", "📖");
        filters.getChildren().addAll(nomeField, argomentoField);

        headerSection.getChildren().addAll(title, filters);
        card.getChildren().add(headerSection);

        TableView<CorsoCucina> table = createOptimizedTable();
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

        VBox buttonSection = new VBox(12);
        buttonSection.setAlignment(Pos.CENTER);
        HBox actionButtons = new HBox(12);
        actionButtons.setAlignment(Pos.CENTER);
        Button mostraTuttiBtn = createStylishButton("📋 Tutti i Corsi", "#FF6600", "#FF8533");
        Button mieiBtn = createStylishButton("👨‍🍳 I Miei Corsi", "#FF6600", "#FF8533");
        actionButtons.getChildren().addAll(mostraTuttiBtn, mieiBtn);

        Button backBtn = createStylishButton("⬅ Torna al Menu", "#FFCC99", "#FFD9B3");
        backBtn.setPrefWidth(180);

        buttonSection.getChildren().addAll(actionButtons, backBtn);
        card.getChildren().add(buttonSection);

        if (contentRoot != null) {
            backBtn.setVisible(false);
            backBtn.setManaged(false);
        } else {
            HBox windowButtons = createWindowButtons(root);
            root.getChildren().add(windowButtons);
            StackPane.setAlignment(windowButtons, Pos.TOP_RIGHT);
            StackPane.setMargin(windowButtons, new Insets(10));
        }

        filteredCorsi = new FilteredList<>(corsiData, c -> true);
        SortedList<CorsoCucina> sortedCorsi = new SortedList<>(filteredCorsi);
        sortedCorsi.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedCorsi);

        loadDataAsync();
        setupFilters(nomeField, argomentoField, mostraTuttiBtn, mieiBtn);
        setupTableDoubleClick(table, root);
        setupBackButton(backBtn, root);

        makeDraggable(root, card);

        return root;
    }

    private void setupTableDoubleClick(TableView<CorsoCucina> table, StackPane localRoot) {
        table.setRowFactory(tv -> {
            TableRow<CorsoCucina> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
                    CorsoCucina selected = row.getItem();
                    Task<CorsoCucina> loadDetailsTask = new Task<>() {
                        @Override
                        protected CorsoCucina call() throws Exception {
                            return gestioneCorsoController != null
                                    ? gestioneCorsoController.getCorsoCompleto(selected.getIdCorso())
                                    : selected;
                        }
                        @Override
                        protected void succeeded() {
                            Platform.runLater(() -> {
                                CorsoCucina dettagli = getValue();
                                apriDettagliCorso(dettagli);
                            });
                        }
                    };
                    new Thread(loadDetailsTask).start();
                }
            });
            return row;
        });
    }

    private void apriDettagliCorso(CorsoCucina corso) {
        try {
            System.out.println("🔍 DEBUG: Apertura dettagli corso: " + corso.getNomeCorso());
            
            DettagliCorsoGUI dettagliGUI = new DettagliCorsoGUI();
            dettagliGUI.setController(gestioneCorsoController);
            dettagliGUI.setCorso(corso);
            
            dettagliGUI.setOnChiudiCallback(() -> {
                System.out.println("🔙 DEBUG: Callback chiudi dettagli - torno alla lista");
                VisualizzaCorsiGUI nuovaListaCorsi = new VisualizzaCorsiGUI();
                nuovaListaCorsi.setControllers(visualizzaController, gestioneCorsoController, contentRoot);
                if (contentRoot != null) {
                    contentRoot.getChildren().setAll(nuovaListaCorsi.getRoot());
                }
            });
            
            StackPane dettagliNode = dettagliGUI.getRoot();
            
            if (contentRoot != null) {
                System.out.println("🔍 DEBUG: contentRoot children prima: " + contentRoot.getChildren().size());
                contentRoot.getChildren().clear();
                contentRoot.getChildren().add(dettagliNode);
                System.out.println("🔍 DEBUG: contentRoot children dopo: " + contentRoot.getChildren().size());
                System.out.println("🔍 DEBUG: dettagliNode style: " + dettagliNode.getStyle());
            } else {
                System.err.println("❌ ERRORE: contentRoot è NULL!");
            }
        } catch (Exception ex) {
            System.err.println("🔥 ERRORE in apriDettagliCorso: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    private TextField createModernTextField(String prompt, String icon) {
        TextField field = new TextField();
        field.setPromptText(icon + " " + prompt);
        field.setPrefHeight(35);
        field.setPrefWidth(190);
        field.setFont(Font.font("Roboto", 13));
        field.setStyle("-fx-background-color: #f8f9fa;" +
                "-fx-background-radius: 18;" +
                "-fx-border-radius: 18;" +
                "-fx-border-color: #dee2e6;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 0 12 0 12;");
        field.setOnMouseEntered(e -> field.setStyle("-fx-background-color: #e9ecef;" +
                "-fx-background-radius: 18;" +
                "-fx-border-radius: 18;" +
                "-fx-border-color: #FF9966;" +
                "-fx-border-width: 2;" +
                "-fx-padding: 0 12 0 12;"));
        field.setOnMouseExited(e -> field.setStyle("-fx-background-color: #f8f9fa;" +
                "-fx-background-radius: 18;" +
                "-fx-border-radius: 18;" +
                "-fx-border-color: #dee2e6;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 0 12 0 12;"));
        return field;
    }

    private TableView<CorsoCucina> createOptimizedTable() {
        TableView<CorsoCucina> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(Region.USE_COMPUTED_SIZE);
        table.setMaxHeight(Double.MAX_VALUE);
        table.setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 12;" +
                "-fx-border-radius: 12;" +
                "-fx-border-color: #dee2e6;" +
                "-fx-border-width: 1;" +
                "-fx-table-header-border-color: #FF9966;");

        TableColumn<CorsoCucina, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getIdCorso()).asObject());
        idCol.setPrefWidth(60);
        idCol.setMinWidth(50);
        idCol.setMaxWidth(70);
        idCol.setResizable(false);

        TableColumn<CorsoCucina, String> nomeCol = new TableColumn<>("📚 Nome Corso");
        nomeCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNomeCorso() != null ? c.getValue().getNomeCorso() : ""));
        nomeCol.setPrefWidth(200);
        nomeCol.setMinWidth(150);

        TableColumn<CorsoCucina, String> argomentoCol = new TableColumn<>("📖 Argomento");
        argomentoCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getArgomento() != null ? c.getValue().getArgomento() : ""));
        argomentoCol.setPrefWidth(130);
        argomentoCol.setMinWidth(100);

        TableColumn<CorsoCucina, Double> prezzoCol = new TableColumn<>("💰 Prezzo");
        prezzoCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrezzo()).asObject());
        prezzoCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("€%.0f", item));
            }
        });
        prezzoCol.setPrefWidth(90);
        prezzoCol.setMinWidth(80);

        TableColumn<CorsoCucina, Integer> sessioniCol = new TableColumn<>("⏰ Sessioni");
        sessioniCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNumeroSessioni()).asObject());
        sessioniCol.setPrefWidth(90);
        sessioniCol.setMinWidth(80);

        TableColumn<CorsoCucina, String> freqCol = new TableColumn<>("📅 Frequenza");
        freqCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getFrequenzaCorso() != null ? c.getValue().getFrequenzaCorso().toString() : ""));
        freqCol.setPrefWidth(100);
        freqCol.setMinWidth(90);

        TableColumn<CorsoCucina, Integer> postiCol = new TableColumn<>("🪑 Posti");
        postiCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNumeroPosti()).asObject());
        postiCol.setPrefWidth(80);
        postiCol.setMinWidth(70);

        TableColumn<CorsoCucina, String> inizioCol = new TableColumn<>("🕑 Inizio");
        inizioCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDataInizioCorso() != null ?
                        c.getValue().getDataInizioCorso().toLocalDate().toString() : ""));
        inizioCol.setPrefWidth(100);
        inizioCol.setMinWidth(90);

        TableColumn<CorsoCucina, String> fineCol = new TableColumn<>("🏁 Fine");
        fineCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDataFineCorso() != null ?
                        c.getValue().getDataFineCorso().toLocalDate().toString() : ""));
        fineCol.setPrefWidth(100);
        fineCol.setMinWidth(90);

        table.getColumns().addAll(idCol, nomeCol, argomentoCol, prezzoCol, sessioniCol, freqCol, postiCol, inizioCol, fineCol);
        table.getSortOrder().add(nomeCol);

        table.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double totalWidth = newWidth.doubleValue();
            if (totalWidth > 0) {
                double flexibleWidth = totalWidth - idCol.getWidth() - 50;
                if (flexibleWidth > 500) {
                    nomeCol.setPrefWidth(flexibleWidth * 0.25);
                    argomentoCol.setPrefWidth(flexibleWidth * 0.15);
                    prezzoCol.setPrefWidth(flexibleWidth * 0.12);
                    sessioniCol.setPrefWidth(flexibleWidth * 0.12);
                    freqCol.setPrefWidth(flexibleWidth * 0.12);
                    postiCol.setPrefWidth(flexibleWidth * 0.08);
                    inizioCol.setPrefWidth(flexibleWidth * 0.08);
                    fineCol.setPrefWidth(flexibleWidth * 0.08);
                }
            }
        });

        return table;
    }

    private Button createStylishButton(String text, String baseColor, String hoverColor) {
        Button button = new Button(text);
        button.setPrefSize(150, 40);
        button.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
        button.setTextFill(Color.web("#4B2E2E"));
        button.setStyle("-fx-background-color: " + baseColor + ";" +
                "-fx-background-radius: 20;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: transparent;" +
                "-fx-border-width: 0;");

        DropShadow shadow = new DropShadow(6, Color.web("#000000", 0.15));
        shadow.setOffsetY(2);
        button.setEffect(shadow);

        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-background-color: " + hoverColor + ";" +
                    "-fx-background-radius: 20;" +
                    "-fx-cursor: hand;");
            DropShadow hoverShadow = new DropShadow(8, Color.web("#000000", 0.25));
            hoverShadow.setOffsetY(3);
            button.setEffect(hoverShadow);
        });

        button.setOnMouseExited(e -> {
            button.setStyle("-fx-background-color: " + baseColor + ";" +
                    "-fx-background-radius: 20;" +
                    "-fx-cursor: hand;");
            button.setEffect(shadow);
        });

        return button;
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
            btn.setStyle("-fx-background-color: rgba(255,140,0,0.7);" +
                    "-fx-background-radius: 16;" +
                    "-fx-cursor: hand;");
            btn.setFocusTraversable(false);
        }

        closeButton.setOnAction(e -> {
            Stage stage = getStage(root);
            if (stage != null) stage.close();
        });
        minimizeButton.setOnAction(e -> {
            Stage stage = getStage(root);
            if (stage != null) stage.setIconified(true);
        });
        maximizeButton.setOnAction(e -> {
            Stage stage = getStage(root);
            if (stage != null) stage.setMaximized(!stage.isMaximized());
        });

        HBox box = new HBox(4, minimizeButton, maximizeButton, closeButton);
        box.setAlignment(Pos.TOP_RIGHT);
        box.setPickOnBounds(false);
        return box;
    }

    private void loadDataAsync() {
        if (visualizzaController == null) {
            progressIndicator.setVisible(false);
            return;
        }
        Task<List<CorsoCucina>> loadTask = new Task<>() {
            @Override
            protected List<CorsoCucina> call() throws Exception {
                long start = System.currentTimeMillis();
                List<CorsoCucina> list = visualizzaController.getTuttiICorsi();
                long end = System.currentTimeMillis();
                System.out.println("[VisualizzaCorsiGUI] Caricati " + (list == null ? 0 : list.size()) + " corsi in " + (end - start) + " ms");
                return list == null ? Collections.emptyList() : list;
            }
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    corsiData.setAll(getValue());
                    progressIndicator.setVisible(false);
                });
            }
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    showAlert(Alert.AlertType.ERROR, "Errore", "Errore caricando corsi: " + getException().getMessage());
                });
            }
        };
        progressIndicator.setVisible(true);
        new Thread(loadTask, "LoadCorsiThread").start();
    }

    private void setupFilters(TextField nomeField, TextField argomentoField, Button mostraTuttiBtn, Button mieiBtn) {
        nomeField.textProperty().addListener((obs, oldValue, newValue) -> {
            filterPause.setOnFinished(event -> applicaFiltriLocali(nomeField.getText(), argomentoField.getText(), false));
            filterPause.playFromStart();
        });
        argomentoField.textProperty().addListener((obs, oldValue, newValue) -> {
            filterPause.setOnFinished(event -> applicaFiltriLocali(nomeField.getText(), argomentoField.getText(), false));
            filterPause.playFromStart();
        });
        mostraTuttiBtn.setOnAction(e -> {
            nomeField.clear();
            argomentoField.clear();
            filteredCorsi.setPredicate(c -> true);
        });
        mieiBtn.setOnAction(e -> applicaFiltriLocali(nomeField.getText(), argomentoField.getText(), true));
    }

    private void setupBackButton(Button backBtn, StackPane root) {
        if (contentRoot == null) {
            backBtn.setOnAction(e -> {
                Stage stage = getStage(root);
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }

    private void makeDraggable(StackPane pane, Node dragHandle) {
        dragHandle.setOnMousePressed(event -> {
            Stage stage = getStage(pane);
            if (stage != null) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });

        dragHandle.setOnMouseDragged(event -> {
            Stage stage = getStage(pane);
            if (stage != null) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });
    }

    private void applicaFiltriLocali(String nome, String argomento, boolean soloChefLoggato) {
        if (filteredCorsi == null) return;
        progressIndicator.setVisible(true);
        Platform.runLater(() -> {
            String nomeLower = nome == null ? "" : nome.toLowerCase().trim();
            String argomentoLower = argomento == null ? "" : argomento.toLowerCase().trim();

            if (soloChefLoggato && cachedCorsiChef == null && visualizzaController != null) {
                try {
                    List<CorsoCucina> corsiChef = visualizzaController.getCorsiChefLoggato();
                    cachedCorsiChef = (corsiChef == null) ? Collections.emptyList() : corsiChef;
                } catch (Exception ex) {
                    cachedCorsiChef = Collections.emptyList();
                    ex.printStackTrace();
                }
            }

            filteredCorsi.setPredicate(c -> {
                if (c == null) return false;
                boolean matchNome = nomeLower.isEmpty() || (c.getNomeCorso() != null && c.getNomeCorso().toLowerCase().contains(nomeLower));
                boolean matchArgomento = argomentoLower.isEmpty() || (c.getArgomento() != null && c.getArgomento().toLowerCase().contains(argomentoLower));
                boolean match = matchNome && matchArgomento;

                if (soloChefLoggato) {
                    return match && (cachedCorsiChef != null && cachedCorsiChef.contains(c));
                } else {
                    return match;
                }
            });

            progressIndicator.setVisible(false);
        });
    }

    private Stage getStage(Node node) {
        if (node == null) return null;
        Scene s = node.getScene();
        if (s == null) return null;
        return (s.getWindow() instanceof Stage) ? (Stage) s.getWindow() : null;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
