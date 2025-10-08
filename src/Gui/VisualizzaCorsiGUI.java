package Gui;

import controller.GestioneCorsoController;
import controller.VisualizzaCorsiController;
import guihelper.StyleHelper;
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
import javafx.util.Duration;
import model.CorsoCucina;

import java.util.Collections;
import java.util.List;

/**
 * GUI per visualizzare la lista dei corsi.
 * Best practices: separazione tra logica (controller) e presentazione (GUI).
 */
public class VisualizzaCorsiGUI {

    private VisualizzaCorsiController visualizzaController;
    private GestioneCorsoController gestioneCorsoController;
    private StackPane contentRoot;

    private final ObservableList<CorsoCucina> corsiData = FXCollections.observableArrayList();
    private FilteredList<CorsoCucina> filteredCorsi;
    private List<CorsoCucina> cachedCorsiChef = null;

    private ProgressIndicator progressIndicator;
    private final PauseTransition filterPause = new PauseTransition(Duration.millis(350));

    public VisualizzaCorsiGUI() {}

    
    public void setControllers(VisualizzaCorsiController visualizzaController,
                              GestioneCorsoController gestioneCorsoController,
                              StackPane contentRoot) {
        this.visualizzaController = visualizzaController;
        this.gestioneCorsoController = gestioneCorsoController;
        this.contentRoot = contentRoot;
    }

    public StackPane getRoot() {
        StackPane root = new StackPane();
        root.setMinSize(400, 400);

        // Background con gradiente
        createBackground(root);

        // Card principale
        VBox card = createMainCard(root);
        root.getChildren().add(card);

        return root;
    }

    private void createBackground(StackPane root) {
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FF9966")), new Stop(1, Color.web("#FFCC99")));
        Region background = new Region();
        background.setBackground(new Background(new BackgroundFill(gradient, null, null)));
        background.prefWidthProperty().bind(root.widthProperty());
        background.prefHeightProperty().bind(root.heightProperty());
        root.getChildren().add(background);
    }

    private VBox createMainCard(StackPane root) {
        VBox card = new VBox(16);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(20));
        card.setMaxWidth(Double.MAX_VALUE);
        card.prefWidthProperty().bind(root.widthProperty());
        card.prefHeightProperty().bind(root.heightProperty());

        // Effetti ombra
        DropShadow shadow = new DropShadow(12, Color.rgb(0, 0, 0, 0.12));
        shadow.setOffsetY(4);
        card.setEffect(shadow);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-border-radius: 16; " +
                "-fx-border-color: #FF9966; -fx-border-width: 2;");

        // Sezione header con titolo e filtri
        VBox headerSection = createHeaderSection();
        card.getChildren().add(headerSection);

        // Tabella corsi
        TableView<CorsoCucina> table = createOptimizedTable();
        table.prefHeightProperty().bind(card.heightProperty().multiply(0.55));
        table.prefWidthProperty().bind(card.widthProperty().multiply(0.95));

        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(40, 40);
        progressIndicator.setStyle("-fx-accent: #FF6600;");
        progressIndicator.setVisible(false);

        StackPane tableContainer = new StackPane(table, progressIndicator);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);
        card.getChildren().add(tableContainer);

        // Pulsanti azione
        VBox buttonSection = createButtonSection();
        card.getChildren().add(buttonSection);

        // Setup filtri e binding
        TextField nomeField = (TextField) ((HBox) headerSection.getChildren().get(1)).getChildren().get(0);
        TextField argomentoField = (TextField) ((HBox) headerSection.getChildren().get(1)).getChildren().get(1);
        Button mostraTuttiBtn = (Button) ((HBox) buttonSection.getChildren().get(0)).getChildren().get(0);
        Button mieiBtn = (Button) ((HBox) buttonSection.getChildren().get(0)).getChildren().get(1);

        setupTableDataBinding(table);
        setupFilters(nomeField, argomentoField, mostraTuttiBtn, mieiBtn);
        setupTableDoubleClick(table);

        // Caricamento dati asincrono[web:7][web:10]
        loadDataAsync();

        return card;
    }

    private VBox createHeaderSection() {
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
        return headerSection;
    }

    private VBox createButtonSection() {
        VBox buttonSection = new VBox(12);
        buttonSection.setAlignment(Pos.CENTER);

        HBox actionButtons = new HBox(12);
        actionButtons.setAlignment(Pos.CENTER);
        Button mostraTuttiBtn = createStylishButton("📋 Tutti i Corsi", "#FF6600", "#FF8533");
        Button mieiBtn = createStylishButton("👨‍🍳 I Miei Corsi", "#FF6600", "#FF8533");
        actionButtons.getChildren().addAll(mostraTuttiBtn, mieiBtn);

        buttonSection.getChildren().add(actionButtons);
        return buttonSection;
    }

    private TextField createModernTextField(String prompt, String icon) {
        TextField field = new TextField();
        field.setPromptText(icon + " " + prompt);
        field.setPrefHeight(35);
        field.setPrefWidth(190);
        field.setFont(Font.font("Roboto", 13));
        
        String normalStyle = "-fx-background-color: #f8f9fa; -fx-background-radius: 18; " +
                "-fx-border-radius: 18; -fx-border-color: #dee2e6; -fx-border-width: 1; " +
                "-fx-padding: 0 12 0 12;";
        String hoverStyle = "-fx-background-color: #e9ecef; -fx-background-radius: 18; " +
                "-fx-border-radius: 18; -fx-border-color: #FF9966; -fx-border-width: 2; " +
                "-fx-padding: 0 12 0 12;";
        
        field.setStyle(normalStyle);
        field.setOnMouseEntered(e -> field.setStyle(hoverStyle));
        field.setOnMouseExited(e -> field.setStyle(normalStyle));
        
        return field;
    }

    private TableView<CorsoCucina> createOptimizedTable() {
        TableView<CorsoCucina> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(Region.USE_COMPUTED_SIZE);
        table.setMaxHeight(Double.MAX_VALUE);
        table.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-radius: 12; -fx-border-color: #dee2e6; -fx-border-width: 1; " +
                "-fx-table-header-border-color: #FF9966;");

        // Colonne ottimizzate
        TableColumn<CorsoCucina, Integer> idCol = createIdColumn();
        TableColumn<CorsoCucina, String> nomeCol = createNomeColumn();
        TableColumn<CorsoCucina, String> argomentoCol = createArgomentoColumn();
        TableColumn<CorsoCucina, Double> prezzoCol = createPrezzoColumn();
        TableColumn<CorsoCucina, Integer> sessioniCol = createSessioniColumn();
        TableColumn<CorsoCucina, String> freqCol = createFrequenzaColumn();
        TableColumn<CorsoCucina, Integer> postiCol = createPostiColumn();
        TableColumn<CorsoCucina, String> inizioCol = createInizioColumn();
        TableColumn<CorsoCucina, String> fineCol = createFineColumn();

        table.getColumns().addAll(idCol, nomeCol, argomentoCol, prezzoCol, 
                sessioniCol, freqCol, postiCol, inizioCol, fineCol);
        table.getSortOrder().add(nomeCol);

        // Responsive column sizing
        setupResponsiveColumns(table, idCol, nomeCol, argomentoCol, prezzoCol, 
                sessioniCol, freqCol, postiCol, inizioCol, fineCol);

        return table;
    }

    private TableColumn<CorsoCucina, Integer> createIdColumn() {
        TableColumn<CorsoCucina, Integer> col = new TableColumn<>("ID");
        col.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getIdCorso()).asObject());
        col.setPrefWidth(60);
        col.setMinWidth(50);
        col.setMaxWidth(70);
        col.setResizable(false);
        return col;
    }

    private TableColumn<CorsoCucina, String> createNomeColumn() {
        TableColumn<CorsoCucina, String> col = new TableColumn<>("📚 Nome Corso");
        col.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNomeCorso() != null ? c.getValue().getNomeCorso() : ""));
        col.setPrefWidth(200);
        col.setMinWidth(150);
        return col;
    }

    private TableColumn<CorsoCucina, String> createArgomentoColumn() {
        TableColumn<CorsoCucina, String> col = new TableColumn<>("📖 Argomento");
        col.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getArgomento() != null ? c.getValue().getArgomento() : ""));
        col.setPrefWidth(130);
        col.setMinWidth(100);
        return col;
    }

    private TableColumn<CorsoCucina, Double> createPrezzoColumn() {
        TableColumn<CorsoCucina, Double> col = new TableColumn<>("💰 Prezzo");
        col.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrezzo()).asObject());
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("€%.0f", item));
            }
        });
        col.setPrefWidth(90);
        col.setMinWidth(80);
        return col;
    }

    private TableColumn<CorsoCucina, Integer> createSessioniColumn() {
        TableColumn<CorsoCucina, Integer> col = new TableColumn<>("⏰ Sessioni");
        col.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNumeroSessioni()).asObject());
        col.setPrefWidth(90);
        col.setMinWidth(80);
        return col;
    }

    private TableColumn<CorsoCucina, String> createFrequenzaColumn() {
        TableColumn<CorsoCucina, String> col = new TableColumn<>("📅 Frequenza");
        col.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getFrequenzaCorso() != null ? c.getValue().getFrequenzaCorso().toString() : ""));
        col.setPrefWidth(100);
        col.setMinWidth(90);
        return col;
    }

    private TableColumn<CorsoCucina, Integer> createPostiColumn() {
        TableColumn<CorsoCucina, Integer> col = new TableColumn<>("🪑 Posti");
        col.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNumeroPosti()).asObject());
        col.setPrefWidth(80);
        col.setMinWidth(70);
        return col;
    }

    private TableColumn<CorsoCucina, String> createInizioColumn() {
        TableColumn<CorsoCucina, String> col = new TableColumn<>("🕑 Inizio");
        col.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDataInizioCorso() != null ?
                        c.getValue().getDataInizioCorso().toLocalDate().toString() : ""));
        col.setPrefWidth(100);
        col.setMinWidth(90);
        return col;
    }

    private TableColumn<CorsoCucina, String> createFineColumn() {
        TableColumn<CorsoCucina, String> col = new TableColumn<>("🏁 Fine");
        col.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDataFineCorso() != null ?
                        c.getValue().getDataFineCorso().toLocalDate().toString() : ""));
        col.setPrefWidth(100);
        col.setMinWidth(90);
        return col;
    }

    private void setupResponsiveColumns(TableView<CorsoCucina> table, TableColumn<CorsoCucina, Integer> idCol,
                                       TableColumn<CorsoCucina, String> nomeCol, TableColumn<CorsoCucina, String> argomentoCol,
                                       TableColumn<CorsoCucina, Double> prezzoCol, TableColumn<CorsoCucina, Integer> sessioniCol,
                                       TableColumn<CorsoCucina, String> freqCol, TableColumn<CorsoCucina, Integer> postiCol,
                                       TableColumn<CorsoCucina, String> inizioCol, TableColumn<CorsoCucina, String> fineCol) {
        
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
    }

    private Button createStylishButton(String text, String baseColor, String hoverColor) {
        Button button = new Button(text);
        button.setPrefSize(150, 40);
        button.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
        button.setTextFill(Color.web("#4B2E2E"));
        
        String normalStyle = "-fx-background-color: " + baseColor + "; " +
                "-fx-background-radius: 20; -fx-cursor: hand; " +
                "-fx-border-color: transparent; -fx-border-width: 0;";
        String activeStyle = "-fx-background-color: " + hoverColor + "; " +
                "-fx-background-radius: 20; -fx-cursor: hand;";
        
        button.setStyle(normalStyle);

        DropShadow shadow = new DropShadow(6, Color.web("#000000", 0.15));
        shadow.setOffsetY(2);
        button.setEffect(shadow);

        button.setOnMouseEntered(e -> {
            button.setStyle(activeStyle);
            DropShadow hoverShadow = new DropShadow(8, Color.web("#000000", 0.25));
            hoverShadow.setOffsetY(3);
            button.setEffect(hoverShadow);
        });

        button.setOnMouseExited(e -> {
            button.setStyle(normalStyle);
            button.setEffect(shadow);
        });

        return button;
    }

    private void setupTableDataBinding(TableView<CorsoCucina> table) {
        filteredCorsi = new FilteredList<>(corsiData, c -> true);
        SortedList<CorsoCucina> sortedCorsi = new SortedList<>(filteredCorsi);
        sortedCorsi.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedCorsi);
    }

    private void setupTableDoubleClick(TableView<CorsoCucina> table) {
        table.setRowFactory(tv -> {
            TableRow<CorsoCucina> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && 
                    event.getClickCount() == 2 && 
                    !row.isEmpty()) {
                    CorsoCucina selected = row.getItem();
                    loadAndShowDettagli(selected);
                }
            });
            return row;
        });
    }

    private void loadAndShowDettagli(CorsoCucina corso) {
        // Caricamento asincrono dei dettagli completi[web:7]
        Task<CorsoCucina> loadDetailsTask = new Task<>() {
            @Override
            protected CorsoCucina call() throws Exception {
                return gestioneCorsoController != null
                        ? gestioneCorsoController.getCorsoCompleto(corso.getIdCorso())
                        : corso;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> apriDettagliCorso(getValue()));
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> 
                    StyleHelper.showErrorDialog("Errore", 
                        "Impossibile caricare i dettagli: " + getException().getMessage())
                );
            }
        };
        
        new Thread(loadDetailsTask, "LoadDettagliThread").start();
    }

    private void apriDettagliCorso(CorsoCucina corso) {
        if (contentRoot == null) {
            StyleHelper.showErrorDialog("Errore", "Navigazione non disponibile");
            return;
        }

        try {
            DettagliCorsoGUI dettagliGUI = new DettagliCorsoGUI();
            dettagliGUI.setController(gestioneCorsoController);
            dettagliGUI.setCorso(corso);

            // Callback per tornare alla lista[web:26]
            dettagliGUI.setOnChiudiCallback(() -> {
                VisualizzaCorsiGUI nuovaListaCorsi = new VisualizzaCorsiGUI();
                nuovaListaCorsi.setControllers(visualizzaController, gestioneCorsoController, contentRoot);
                contentRoot.getChildren().setAll(nuovaListaCorsi.getRoot());
            });

            // Navigazione al dettaglio[web:24][web:29]
            contentRoot.getChildren().setAll(dettagliGUI.getRoot());
            
        } catch (Exception ex) {
            StyleHelper.showErrorDialog("Errore", "Errore apertura dettagli: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadDataAsync() {
        if (visualizzaController == null) {
            progressIndicator.setVisible(false);
            StyleHelper.showErrorDialog("Errore", "Controller non inizializzato");
            return;
        }

        // Task asincrono per non bloccare la UI[web:7][web:10]
        Task<List<CorsoCucina>> loadTask = new Task<>() {
            @Override
            protected List<CorsoCucina> call() throws Exception {
                long start = System.currentTimeMillis();
                List<CorsoCucina> list = visualizzaController.getTuttiICorsi();
                long end = System.currentTimeMillis();
                System.out.println("[VisualizzaCorsiGUI] Caricati " + 
                        (list == null ? 0 : list.size()) + " corsi in " + (end - start) + " ms");
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
                    StyleHelper.showErrorDialog("Errore", 
                            "Errore caricamento corsi: " + getException().getMessage());
                });
            }
        };

        progressIndicator.setVisible(true);
        new Thread(loadTask, "LoadCorsiThread").start();
    }

    private void setupFilters(TextField nomeField, TextField argomentoField, 
                             Button mostraTuttiBtn, Button mieiBtn) {
        // Debounce per evitare troppi aggiornamenti[web:7]
        nomeField.textProperty().addListener((obs, oldValue, newValue) -> {
            filterPause.setOnFinished(event -> 
                    applicaFiltriLocali(nomeField.getText(), argomentoField.getText(), false));
            filterPause.playFromStart();
        });

        argomentoField.textProperty().addListener((obs, oldValue, newValue) -> {
            filterPause.setOnFinished(event -> 
                    applicaFiltriLocali(nomeField.getText(), argomentoField.getText(), false));
            filterPause.playFromStart();
        });

        mostraTuttiBtn.setOnAction(e -> {
            nomeField.clear();
            argomentoField.clear();
            filteredCorsi.setPredicate(c -> true);
        });

        mieiBtn.setOnAction(e -> 
                applicaFiltriLocali(nomeField.getText(), argomentoField.getText(), true));
    }

    private void applicaFiltriLocali(String nome, String argomento, boolean soloChefLoggato) {
        if (filteredCorsi == null) return;
        
        progressIndicator.setVisible(true);
        
        // Task asincrono per filtri pesanti[web:7]
        Task<Void> filterTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                if (soloChefLoggato && cachedCorsiChef == null && visualizzaController != null) {
                    List<CorsoCucina> corsiChef = visualizzaController.getCorsiChefLoggato();
                    cachedCorsiChef = (corsiChef == null) ? Collections.emptyList() : corsiChef;
                }
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    String nomeLower = nome == null ? "" : nome.toLowerCase().trim();
                    String argomentoLower = argomento == null ? "" : argomento.toLowerCase().trim();

                    filteredCorsi.setPredicate(c -> {
                        if (c == null) return false;
                        
                        boolean matchNome = nomeLower.isEmpty() || 
                                (c.getNomeCorso() != null && c.getNomeCorso().toLowerCase().contains(nomeLower));
                        boolean matchArgomento = argomentoLower.isEmpty() || 
                                (c.getArgomento() != null && c.getArgomento().toLowerCase().contains(argomentoLower));
                        boolean match = matchNome && matchArgomento;

                        if (soloChefLoggato) {
                            return match && (cachedCorsiChef != null && cachedCorsiChef.contains(c));
                        }
                        return match;
                    });

                    progressIndicator.setVisible(false);
                });
            }
        };

        new Thread(filterTask, "FilterThread").start();
    }
}
