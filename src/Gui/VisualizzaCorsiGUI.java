package Gui;

import controller.GestioneCorsoController;
import controller.VisualizzaCorsiController;
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
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.CorsoCucina;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VisualizzaCorsiGUI {

    private VisualizzaCorsiController visualizzaController;
    private GestioneCorsoController gestioneCorsoController;
    private StackPane menuRoot;

    private final ObservableList<CorsoCucina> corsiData = FXCollections.observableArrayList();
    private FilteredList<CorsoCucina> filteredCorsi;

    // Cache risultati chef loggato per non ripetere query pesanti
    private List<CorsoCucina> cachedCorsiChef = null;

    // Variabili per trascinamento
    private double xOffset = 0;
    private double yOffset = 0;

    // Componenti UI principali
    private ProgressIndicator progressIndicator;
    private TableView<CorsoCucina> table;
    private TextField nomeField;
    private TextField argomentoField;
    private Label countLabel;
    private Button mostraTuttiBtn;
    private Button mieiBtn;

    public void setControllers(VisualizzaCorsiController visualizzaController,
                               GestioneCorsoController gestioneCorsoController, StackPane menuRoot) {
        this.visualizzaController = visualizzaController;
        this.gestioneCorsoController = gestioneCorsoController;
        this.menuRoot = menuRoot;
    }

    public StackPane getRoot() {
        StackPane root = new StackPane();

        // Sfondo moderno con gradiente
        createOrangeBackground(root);

        // Container principale
        VBox mainContainer = createMainContainer(root);
        root.getChildren().add(mainContainer);

        // Header con titolo
        VBox headerSection = createHeader();
        mainContainer.getChildren().add(headerSection);

        // Sezione filtri
        HBox filterSection = createFilterSection();
        mainContainer.getChildren().add(filterSection);

        // Tabella
        createTable();
        StackPane tableContainer = new StackPane(table);
        tableContainer.setStyle("-fx-background-color: rgba(255,255,255,0.95);" +
                               "-fx-background-radius: 20;" +
                               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 5);");
        VBox.setVgrow(tableContainer, Priority.ALWAYS);
        mainContainer.getChildren().add(tableContainer);

        // Progress indicator
        createProgressIndicator(tableContainer);

        // Bottoni azione
        HBox actionSection = createActionButtons(root);
        mainContainer.getChildren().add(actionSection);

        // Setup filtri
        setupFilters();

        // Controlli finestra
        addWindowControls(root);

        // Trascinamento
        makeDraggable(root, headerSection);

        // Caricamento iniziale
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

        // Cerchi decorativi
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

        Label title = new Label("📚 Lista dei Corsi");
        title.setFont(Font.font("Inter", FontWeight.EXTRA_BOLD, 26));
        title.setTextFill(Color.WHITE);
        title.setEffect(new DropShadow(8, Color.web("#FF6600", 0.8)));

        Label subtitle = new Label("Esplora e gestisci i corsi di cucina disponibili");
        subtitle.setFont(Font.font("Inter", FontWeight.NORMAL, 12));
        subtitle.setTextFill(Color.web("#ffffff", 0.9));

        header.getChildren().addAll(title, subtitle);
        return header;
    }

    private HBox createFilterSection() {
        HBox filters = new HBox(14);
        filters.setAlignment(Pos.CENTER);
        filters.setPadding(new Insets(0, 12, 6, 12));

        nomeField = createOrangeTextField("🔍 Cerca per nome corso...", 250);
        argomentoField = createOrangeTextField("📖 Cerca per argomento...", 200);

        countLabel = new Label("0 corsi");
        countLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 12));
        countLabel.setTextFill(Color.WHITE);
        countLabel.setStyle("-fx-background-color: rgba(255,102,0,0.75); -fx-background-radius: 15; -fx-padding: 8 14;");

        filters.getChildren().addAll(nomeField, argomentoField, countLabel);
        return filters;
    }

    private void createTable() {
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("📚 Nessun corso trovato"));

        table.setFixedCellSize(42);

        table.setStyle("-fx-background-color: transparent;" +
                      "-fx-table-cell-border-color: rgba(0,0,0,0.04);" +
                      "-fx-selection-bar: rgba(255,153,102,0.25);" +
                      "-fx-selection-bar-non-focused: rgba(255,153,102,0.18);");

        // Styling header quando la tabella è pronta
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

        // Colonne
        TableColumn<CorsoCucina, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getIdCorso()).asObject());
        idCol.setPrefWidth(60);
        idCol.setMinWidth(50);
        idCol.setMaxWidth(70);
        idCol.setResizable(false);

        TableColumn<CorsoCucina, String> nomeCol = new TableColumn<>("📚 Nome Corso");
        nomeCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getNomeCorso() != null ? c.getValue().getNomeCorso() : ""));
        nomeCol.setMinWidth(200);

        TableColumn<CorsoCucina, String> argomentoCol = new TableColumn<>("📖 Argomento");
        argomentoCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getArgomento() != null ? c.getValue().getArgomento() : ""));
        argomentoCol.setMinWidth(130);

        TableColumn<CorsoCucina, Double> prezzoCol = new TableColumn<>("💰 Prezzo");
        prezzoCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrezzo()).asObject());
        prezzoCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("€%.0f", item));
                    setFont(Font.font("Inter", 13));
                    setTextFill(Color.web("#2E7D32"));
                }
            }
        });
        prezzoCol.setMinWidth(90);

        TableColumn<CorsoCucina, Integer> sessioniCol = new TableColumn<>("⏰ Sessioni");
        sessioniCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNumeroSessioni()).asObject());
        sessioniCol.setMinWidth(90);

        TableColumn<CorsoCucina, String> freqCol = new TableColumn<>("📅 Frequenza");
        freqCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getFrequenzaCorso() != null ? c.getValue().getFrequenzaCorso().toString() : ""));
        freqCol.setMinWidth(100);

        TableColumn<CorsoCucina, Integer> postiCol = new TableColumn<>("🪑 Posti");
        postiCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNumeroPosti()).asObject());
        postiCol.setMinWidth(80);

        TableColumn<CorsoCucina, String> inizioCol = new TableColumn<>("🕑 Inizio");
        inizioCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getDataInizioCorso() != null ?
                c.getValue().getDataInizioCorso().toString().substring(0, 10) : ""));
        inizioCol.setMinWidth(100);

        TableColumn<CorsoCucina, String> fineCol = new TableColumn<>("🏁 Fine");
        fineCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getDataFineCorso() != null ?
                c.getValue().getDataFineCorso().toString().substring(0, 10) : ""));
        fineCol.setMinWidth(100);

        table.getColumns().addAll(idCol, nomeCol, argomentoCol, prezzoCol, sessioniCol, freqCol, postiCol, inizioCol, fineCol);
        table.getSortOrder().add(nomeCol);

        // Row factory per styling, hover E doppio click (tutto insieme)
        table.setRowFactory(tv -> {
            TableRow<CorsoCucina> row = new TableRow<>() {
                @Override
                protected void updateItem(CorsoCucina item, boolean empty) {
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
            
            // Hover effects
            row.setOnMouseEntered(e -> {
                if (!row.isEmpty()) row.setStyle("-fx-background-color: #FFF3E0;");
            });
            row.setOnMouseExited(e -> {
                if (!row.isEmpty()) {
                    if (row.getIndex() % 2 == 0) row.setStyle("-fx-background-color: #ffffff;");
                    else row.setStyle("-fx-background-color: #fafafa;");
                }
            });
            
            // Doppio click per aprire dettagli
            row.setOnMouseClicked(event -> {
                // Consuma l'evento per evitare propagazione
                event.consume();
                
                if (event.getButton() == MouseButton.PRIMARY && 
                    event.getClickCount() == 2 && 
                    !row.isEmpty() && 
                    row.getItem() != null) {
                    
                    CorsoCucina selected = row.getItem();
                    System.out.println("Doppio click su corso: " + selected.getNomeCorso()); // Debug
                    apriDettagliCorso(selected);
                }
            });
            
            return row;
        });
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

        mostraTuttiBtn = createOrangeButton("📋 Tutti i Corsi", 160);
        mieiBtn = createOrangeButton("👨‍🍳 I Miei Corsi", 160);
        Button backBtn = createOrangeButton("⬅️ Torna al Menu", 160);

        mostraTuttiBtn.setOnAction(e -> {
            nomeField.clear();
            argomentoField.clear();
            cachedCorsiChef = null; // reset cache
            filteredCorsi.setPredicate(p -> true);
        });

        mieiBtn.setOnAction(e -> applicaFiltriLocali(nomeField.getText(), argomentoField.getText(), true));

        backBtn.setOnAction(e -> {
            if (menuRoot != null) {
                // Rimuove questa GUI dal menuRoot invece di cambiare scene
                menuRoot.getChildren().remove(currentRoot);
            } else {
                Stage stage = getStage(currentRoot);
                if (stage != null) {
                    stage.close();
                }
            }
        });

        actions.getChildren().addAll(backBtn, mostraTuttiBtn, mieiBtn);
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
        filteredCorsi = new FilteredList<>(corsiData, p -> true);
        SortedList<CorsoCucina> sorted = new SortedList<>(filteredCorsi);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);

        // Listener per i filtri con debounce-like behavior
        nomeField.textProperty().addListener((obs, old, text) -> 
            Platform.runLater(() -> applicaFiltriLocali(text, argomentoField.getText(), false)));
        argomentoField.textProperty().addListener((obs, old, text) -> 
            Platform.runLater(() -> applicaFiltriLocali(nomeField.getText(), text, false)));

        // Aggiorna contatore
        corsiData.addListener((javafx.collections.ListChangeListener<CorsoCucina>) c -> updateCountLabel());
        updateCountLabel();
    }

    private void apriDettagliCorso(CorsoCucina selected) {
        if (selected == null) {
            showError("Nessun corso selezionato");
            return;
        }

        // Mostra progress indicator durante il caricamento
        progressIndicator.setVisible(true);
        
        Task<CorsoCucina> loadDetailsTask = new Task<>() {
            @Override
            protected CorsoCucina call() throws Exception {
                return gestioneCorsoController != null ? 
                    gestioneCorsoController.getCorsoCompleto(selected.getIdCorso()) : selected;
            }

            @Override
            protected void succeeded() {
                CorsoCucina dettagli = getValue();
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    
                    // Crea la GUI dei dettagli
                    DettagliCorsoGUI detGui = new DettagliCorsoGUI();
                    detGui.setController(gestioneCorsoController);
                    detGui.setCorso(dettagli != null ? dettagli : selected);

                    if (menuRoot != null) {
                        // Sostituisce il contenuto corrente con i dettagli
                        StackPane currentRoot = (StackPane) table.getScene().getRoot();
                        StackPane dettagliRoot = detGui.getRoot();
                        
                        // Trova lo stage e sostituisce la root
                        Stage stage = getStage(currentRoot);
                        if (stage != null) {
                            stage.getScene().setRoot(dettagliRoot);
                        }
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    showError("Errore caricando dettagli: " + getException().getMessage());
                    getException().printStackTrace(); // Debug
                });
            }
        };

        Thread t = new Thread(loadDetailsTask, "LoadDettagliThread");
        t.setDaemon(true);
        t.start();
    }

    private void updateCountLabel() {
        Platform.runLater(() -> {
            int count = filteredCorsi == null ? corsiData.size() : filteredCorsi.size();
            String text = count + (count == 1 ? " corso" : " corsi");
            countLabel.setText(text);
        });
    }

    private void refreshData() {
        if (visualizzaController == null) {
            if (progressIndicator != null) progressIndicator.setVisible(false);
            return;
        }

        Task<List<CorsoCucina>> loadTask = new Task<>() {
            @Override
            protected List<CorsoCucina> call() throws Exception {
                long t0 = System.currentTimeMillis();
                List<CorsoCucina> list = visualizzaController.getTuttiICorsi();
                long t1 = System.currentTimeMillis();
                System.out.println("[VisualizzaCorsi] Caricati corsi in ms: " + (t1 - t0) + " count=" + (list == null ? 0 : list.size()));
                return list == null ? Collections.emptyList() : list;
            }

            @Override
            protected void succeeded() {
                List<CorsoCucina> loaded = getValue();
                Platform.runLater(() -> {
                    corsiData.setAll(loaded);
                    progressIndicator.setVisible(false);
                    updateCountLabel();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    showError("Errore caricando corsi: " + getException().getMessage());
                });
            }
        };

        progressIndicator.setVisible(true);
        Thread t = new Thread(loadTask, "LoadCorsiThread");
        t.setDaemon(true);
        t.start();
    }

    private void applicaFiltriLocali(String nome, String argomento, boolean soloChefLoggato) {
        if (filteredCorsi == null) return;

        String n = nome == null ? "" : nome.toLowerCase().trim();
        String a = argomento == null ? "" : argomento.toLowerCase().trim();

        // Se il filtro richiede solo i corsi del chef loggato, carica la lista una volta (cache)
        if (soloChefLoggato && cachedCorsiChef == null && visualizzaController != null) {
            Task<List<CorsoCucina>> chefCorsiTask = new Task<>() {
                @Override
                protected List<CorsoCucina> call() throws Exception {
                    List<CorsoCucina> tmp = visualizzaController.getCorsiChefLoggato();
                    return tmp == null ? new ArrayList<>() : new ArrayList<>(tmp);
                }

                @Override
                protected void succeeded() {
                    cachedCorsiChef = getValue();
                    // Applica il filtro dopo aver caricato la cache
                    Platform.runLater(() -> applicaFiltroConCache(n, a, soloChefLoggato));
                }

                @Override
                protected void failed() {
                    cachedCorsiChef = new ArrayList<>();
                    Platform.runLater(() -> applicaFiltroConCache(n, a, soloChefLoggato));
                }
            };

            Thread t = new Thread(chefCorsiTask, "LoadChefCorsiThread");
            t.setDaemon(true);
            t.start();
            return;
        }

        applicaFiltroConCache(n, a, soloChefLoggato);
    }

    private void applicaFiltroConCache(String n, String a, boolean soloChefLoggato) {
        filteredCorsi.setPredicate(c -> {
            boolean matchNome = n.isEmpty() || (c.getNomeCorso() != null && c.getNomeCorso().toLowerCase().contains(n));
            boolean matchArg = a.isEmpty() || (c.getArgomento() != null && c.getArgomento().toLowerCase().contains(a));
            boolean match = matchNome && matchArg;
            
            if (soloChefLoggato) {
                return match && (cachedCorsiChef != null && cachedCorsiChef.contains(c));
            }
            return match;
        });
        updateCountLabel();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    private void addWindowControls(StackPane root) {
        HBox controls = new HBox(6);
        controls.setAlignment(Pos.TOP_RIGHT);
        controls.setPadding(new Insets(8));
        controls.setPickOnBounds(false);

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