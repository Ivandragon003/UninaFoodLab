package Gui;

import controller.ChefController;
import controller.GestioneCorsoController;
import controller.IngredienteController;
import controller.RicettaController;
import controller.VisualizzaCorsiController;
import exceptions.DataAccessException;
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
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import model.CorsoCucina;
import model.Frequenza;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VisualizzaCorsiGUI {
    
    private static final double NOME_FIELD_WIDTH = 220.0;
    private static final double TABLE_PREF_HEIGHT = 500.0;
    private static final double PROGRESS_INDICATOR_SIZE = 40.0;
    private static final int DEBOUNCE_DELAY_MS = 350;
    private static final double COL_NOME_PREF_WIDTH = 200.0;
    private static final double COL_NOME_MIN_WIDTH = 150.0;
    private static final double COL_ARGOMENTO_PREF_WIDTH = 180.0;
    private static final double COL_ARGOMENTO_MIN_WIDTH = 120.0;
    private static final double COL_PREZZO_PREF_WIDTH = 100.0;
    private static final double COL_PREZZO_MIN_WIDTH = 80.0;
    private static final double COL_POSTI_PREF_WIDTH = 90.0;
    private static final double COL_POSTI_MIN_WIDTH = 70.0;
    private static final double COL_FREQUENZA_PREF_WIDTH = 110.0;
    private static final double COL_FREQUENZA_MIN_WIDTH = 90.0;
    private static final double COL_DATA_PREF_WIDTH = 140.0;
    private static final double COL_DATA_MIN_WIDTH = 120.0;
    private static final double COL_STATO_PREF_WIDTH = 100.0;
    private static final double COL_STATO_MIN_WIDTH = 80.0;
    
    private VisualizzaCorsiController visualizzaController;
    private GestioneCorsoController gestioneController;
    private ChefController chefController;
    private RicettaController ricettaController;
    private IngredienteController ingredienteController;
    private StackPane contentRoot;
    
    private ObservableList<CorsoCucina> corsiData;
    private FilteredList<CorsoCucina> filteredData;
    private List<CorsoCucina> cachedCorsiChef;
    
    private TextField nomeFilterField;
    private Spinner<Integer> minRicetteSpinner;
    private ComboBox<String> filtroCombo;
    
    public void setControllers(
        VisualizzaCorsiController visualizzaController,
        GestioneCorsoController gestioneController,
        ChefController chefController,
        StackPane contentRoot
    ) {
        this.visualizzaController = visualizzaController;
        this.gestioneController = gestioneController;
        this.chefController = chefController;
        this.contentRoot = contentRoot;
    }
    
    public void setRicettaController(RicettaController ricettaController) {
        this.ricettaController = ricettaController;
    }
    
    public void setIngredienteController(IngredienteController ingredienteController) {
        this.ingredienteController = ingredienteController;
    }
    
    public StackPane getRoot() {
        if (visualizzaController == null || gestioneController == null || chefController == null) {
            throw new IllegalStateException("Controller non impostati! Usa setControllers() prima di chiamare getRoot().");
        }
        
        StackPane mainContainer = new StackPane();
        mainContainer.setMinSize(400, 400);
        
        Region background = new Region();
        StyleHelper.applyBackgroundGradient(background);
        background.prefWidthProperty().bind(mainContainer.widthProperty());
        background.prefHeightProperty().bind(mainContainer.heightProperty());
        mainContainer.getChildren().add(background);
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setPadding(new Insets(30));
        
        VBox mainCard = createMainCard();
        
        VBox wrapper = new VBox(mainCard);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setPadding(new Insets(30));
        wrapper.setStyle("-fx-background-color: transparent;");
        scrollPane.setContent(wrapper);
        
        mainContainer.getChildren().add(scrollPane);
        return mainContainer;
    }
    
    private VBox createMainCard() {
        VBox card = StyleHelper.createCard();
        card.setMaxWidth(1200);
        card.setSpacing(20);
        
        Label title = StyleHelper.createTitleLabel("📚 I Tuoi Corsi");
        title.setAlignment(Pos.CENTER);
        
        VBox headerSection = createHeaderSection();
        
        TableView<CorsoCucina> table = createTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        
        card.getChildren().addAll(title, headerSection, table);
        
        loadCorsiInBackground(table);
        
        return card;
    }
    
    private VBox createHeaderSection() {
        VBox headerSection = new VBox(12);
        headerSection.setPadding(new Insets(0, 10, 10, 10));
        
        Label filtriLabel = StyleHelper.createSubtitleLabel("🔍 Filtra i corsi:");
        
        nomeFilterField = StyleHelper.createTextField("Cerca per nome...");
        nomeFilterField.setPrefWidth(NOME_FIELD_WIDTH);
        
        Label minRicetteLabel = StyleHelper.createLabel("Min. Ricette:");
        minRicetteSpinner = new Spinner<>(0, 100, 0, 1);
        minRicetteSpinner.setPrefWidth(80);
        minRicetteSpinner.setEditable(true);
        
        filtroCombo = StyleHelper.createComboBox();
        filtroCombo.getItems().addAll("Tutti", "In Corso", "Terminati", "Futuri");
        filtroCombo.setValue("Tutti");
        filtroCombo.setPrefWidth(150);
        
        Button resetBtn = StyleHelper.createInfoButton("Reset");
        resetBtn.setPrefWidth(100);
        resetBtn.setOnAction(e -> resetFilters());
        
        HBox filtersRow1 = new HBox(15, nomeFilterField, new Label("—"), minRicetteLabel, minRicetteSpinner, resetBtn);
        filtersRow1.setAlignment(Pos.CENTER_LEFT);
        
        HBox filtersRow2 = new HBox(15, new Label("Filtri:"), filtroCombo);
        filtersRow2.setAlignment(Pos.CENTER_LEFT);
        
        headerSection.getChildren().addAll(filtriLabel, filtersRow1, filtersRow2);
        
        return headerSection;
    }
    
    private void setupFilters() {
        if (filteredData == null) return;
        
        PauseTransition debounceNome = new PauseTransition(Duration.millis(DEBOUNCE_DELAY_MS));
        nomeFilterField.textProperty().addListener((obs, oldVal, newVal) -> {
            debounceNome.setOnFinished(event -> applyFilters());
            debounceNome.playFromStart();
        });
        
        minRicetteSpinner.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filtroCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }
    
    private void applyFilters() {
        if (filteredData == null) return;
        
        filteredData.setPredicate(corso -> {
            String nomeFilter = nomeFilterField.getText();
            if (nomeFilter != null && !nomeFilter.trim().isEmpty()) {
                if (!corso.getNomeCorso().toLowerCase().contains(nomeFilter.toLowerCase().trim())) {
                    return false;
                }
            }
            
            int minRic = minRicetteSpinner.getValue();
            int numRicette = corso.getRicette() != null ? corso.getRicette().size() : 0;
            if (numRicette < minRic) {
                return false;
            }
            
            String filtroScelto = filtroCombo.getValue();
            if (filtroScelto != null && !filtroScelto.equals("Tutti")) {
                LocalDateTime ora = LocalDateTime.now();
                boolean isFinito = isCorsoFinito(corso);
                boolean isFuturo = corso.getDataInizioCorso() != null && corso.getDataInizioCorso().isAfter(ora);
                boolean isInCorso = !isFinito && !isFuturo;
                
                if (filtroScelto.equals("In Corso") && !isInCorso) return false;
                if (filtroScelto.equals("Terminati") && !isFinito) return false;
                if (filtroScelto.equals("Futuri") && !isFuturo) return false;
            }
            
            return true;
        });
    }
    
    private void resetFilters() {
        nomeFilterField.clear();
        minRicetteSpinner.getValueFactory().setValue(0);
        filtroCombo.setValue("Tutti");
    }
    
    private TableView<CorsoCucina> createTable() {
        TableView<CorsoCucina> table = new TableView<>();
        table.setPrefHeight(TABLE_PREF_HEIGHT);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Nessun corso disponibile"));
        
        table.getColumns().addAll(
            createNomeColumn(),
            createArgomentoColumn(),
            createPrezzoColumn(),
            createPostiColumn(),
            createFrequenzaColumn(),
            createDataInizioColumn(),
            createDataFineColumn(),
            createStatoColumn()
        );
        
        MenuButton menuBtn = createMenuButton(table);
        
        Label istruzioniLabel = new Label("💡 Doppio click su un corso per aprirlo");
        istruzioniLabel.setFont(Font.font("Roboto", FontWeight.NORMAL, 12));
        istruzioniLabel.setStyle("-fx-text-fill: " + StyleHelper.TEXT_GRAY + ";");
        
        table.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                CorsoCucina selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    apriDettagliCorso(selected);
                }
            }
        });
        
        VBox tableContainer = new VBox(10, table, istruzioniLabel, menuBtn);
        tableContainer.setAlignment(Pos.CENTER);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        return table;
    }
    
    private MenuButton createMenuButton(TableView<CorsoCucina> table) {
        MenuButton menuBtn = new MenuButton("⚙️ Azioni Rapide");
        menuBtn.setPrefWidth(180);
        StyleHelper.styleMenuButton(menuBtn);
        
        MenuItem openItem = new MenuItem("📂 Apri Corso");
        openItem.setOnAction(e -> {
            CorsoCucina selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                apriDettagliCorso(selected);
            } else {
                StyleHelper.showValidationDialog("Nessuna Selezione", "Seleziona un corso dalla tabella prima di procedere.");
            }
        });
        
        MenuItem refreshItem = new MenuItem("🔄 Ricarica Lista");
        refreshItem.setOnAction(e -> loadCorsiInBackground(table));
        
        menuBtn.getItems().addAll(openItem, refreshItem);
        
        return menuBtn;
    }
    
    private TableColumn<CorsoCucina, String> createNomeColumn() {
        TableColumn<CorsoCucina, String> col = new TableColumn<>("📚 Nome Corso");
        col.setCellValueFactory(cdf -> new SimpleStringProperty(cdf.getValue().getNomeCorso()));
        col.setPrefWidth(COL_NOME_PREF_WIDTH);
        col.setMinWidth(COL_NOME_MIN_WIDTH);
        return col;
    }
    
    private TableColumn<CorsoCucina, String> createArgomentoColumn() {
        TableColumn<CorsoCucina, String> col = new TableColumn<>("📖 Argomento");
        col.setCellValueFactory(cdf -> new SimpleStringProperty(cdf.getValue().getArgomento()));
        col.setPrefWidth(COL_ARGOMENTO_PREF_WIDTH);
        col.setMinWidth(COL_ARGOMENTO_MIN_WIDTH);
        return col;
    }
    
    private TableColumn<CorsoCucina, Double> createPrezzoColumn() {
        TableColumn<CorsoCucina, Double> col = new TableColumn<>("💰 Prezzo (€)");
        col.setCellValueFactory(cdf -> new SimpleDoubleProperty(cdf.getValue().getPrezzo()).asObject());
        col.setPrefWidth(COL_PREZZO_PREF_WIDTH);
        col.setMinWidth(COL_PREZZO_MIN_WIDTH);
        return col;
    }
    
    private TableColumn<CorsoCucina, Integer> createPostiColumn() {
        TableColumn<CorsoCucina, Integer> col = new TableColumn<>("🪑 Posti");
        col.setCellValueFactory(cdf -> new SimpleIntegerProperty(cdf.getValue().getNumeroPosti()).asObject());
        col.setPrefWidth(COL_POSTI_PREF_WIDTH);
        col.setMinWidth(COL_POSTI_MIN_WIDTH);
        return col;
    }
    
    private TableColumn<CorsoCucina, String> createFrequenzaColumn() {
        TableColumn<CorsoCucina, String> col = new TableColumn<>("📅 Frequenza");
        col.setCellValueFactory(cdf -> {
            Frequenza freq = cdf.getValue().getFrequenzaCorso();
            return new SimpleStringProperty(freq != null ? freq.name() : "N/A");
        });
        col.setPrefWidth(COL_FREQUENZA_PREF_WIDTH);
        col.setMinWidth(COL_FREQUENZA_MIN_WIDTH);
        return col;
    }
    
    private TableColumn<CorsoCucina, String> createDataInizioColumn() {
        TableColumn<CorsoCucina, String> col = new TableColumn<>("🕑 Data Inizio");
        col.setCellValueFactory(cdf -> {
            LocalDateTime dt = cdf.getValue().getDataInizioCorso();
            String formatted = dt != null ? dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";
            return new SimpleStringProperty(formatted);
        });
        col.setPrefWidth(COL_DATA_PREF_WIDTH);
        col.setMinWidth(COL_DATA_MIN_WIDTH);
        return col;
    }
    
    private TableColumn<CorsoCucina, String> createDataFineColumn() {
        TableColumn<CorsoCucina, String> col = new TableColumn<>("🏁 Data Fine");
        col.setCellValueFactory(cdf -> {
            LocalDateTime dt = cdf.getValue().getDataFineCorso();
            String formatted = dt != null ? dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";
            return new SimpleStringProperty(formatted);
        });
        col.setPrefWidth(COL_DATA_PREF_WIDTH);
        col.setMinWidth(COL_DATA_MIN_WIDTH);
        return col;
    }
    
    private TableColumn<CorsoCucina, String> createStatoColumn() {
        TableColumn<CorsoCucina, String> col = new TableColumn<>("📊 Stato");
        col.setCellValueFactory(cdf -> {
            LocalDateTime ora = LocalDateTime.now();
            CorsoCucina corso = cdf.getValue();
            
            String stato;
            if (isCorsoFinito(corso)) {
                stato = "✅ Terminato";
            } else if (corso.getDataInizioCorso() != null && corso.getDataInizioCorso().isAfter(ora)) {
                stato = "🔜 Futuro";
            } else {
                stato = "🟢 In Corso";
            }
            return new SimpleStringProperty(stato);
        });
        col.setPrefWidth(COL_STATO_PREF_WIDTH);
        col.setMinWidth(COL_STATO_MIN_WIDTH);
        return col;
    }
    
    private void loadCorsiInBackground(TableView<CorsoCucina> table) {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(PROGRESS_INDICATOR_SIZE, PROGRESS_INDICATOR_SIZE);
        table.setPlaceholder(progressIndicator);
        
        Task<List<CorsoCucina>> loadTask = new Task<List<CorsoCucina>>() {
            @Override
            protected List<CorsoCucina> call() throws Exception {
                long startTime = System.currentTimeMillis();
                
                if (cachedCorsiChef != null && !cachedCorsiChef.isEmpty()) {
                    return cachedCorsiChef;
                }
                
                List<CorsoCucina> corsi = visualizzaController.getCorsiDelChef();
                cachedCorsiChef = new ArrayList<>(corsi);
                
                long endTime = System.currentTimeMillis();
                System.out.println("[VisualizzaCorsiGUI] Caricati " + corsi.size() + " corsi in " + (endTime - startTime) + " ms");
                
                return corsi;
            }
            
            @Override
            protected void succeeded() {
                try {
                    List<CorsoCucina> result = getValue();
                    corsiData = FXCollections.observableArrayList(result);
                    filteredData = new FilteredList<>(corsiData, p -> true);
                    SortedList<CorsoCucina> sortedData = new SortedList<>(filteredData);
                    sortedData.comparatorProperty().bind(table.comparatorProperty());
                    table.setItems(sortedData);
                    
                    setupFilters();
                    
                    table.setPlaceholder(new Label("Nessun corso disponibile"));
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        table.setPlaceholder(new Label("Errore nel caricamento dei corsi"));
                        StyleHelper.showErrorDialog("Errore", "Impossibile caricare i corsi: " + e.getMessage());
                    });
                }
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    table.setPlaceholder(new Label("Errore nel caricamento"));
                    Throwable ex = getException();
                    if (ex instanceof DataAccessException) {
                        StyleHelper.showErrorDialog("Errore Database", ex.getMessage());
                    } else {
                        StyleHelper.showErrorDialog("Errore", "Si è verificato un errore: " + ex.getMessage());
                    }
                });
            }
        };
        
        Thread loadThread = new Thread(loadTask, "LoadCorsiTask");
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    private void apriDettagliCorso(CorsoCucina corso) {
        if (contentRoot == null) {
            StyleHelper.showErrorDialog("Errore Navigazione", "Navigazione non disponibile. Riavvia l'applicazione.");
            return;
        }
        
        try {
            if (ricettaController == null || ingredienteController == null) {
                StyleHelper.showErrorDialog("Configurazione Errata", "Sistema non configurato correttamente. Contatta l'amministratore.");
                return;
            }
            
            DettagliCorsoGUI dettagliGUI = new DettagliCorsoGUI();
            dettagliGUI.setController(gestioneController);
            dettagliGUI.setChefController(chefController);
            dettagliGUI.setRicettaController(ricettaController);
            dettagliGUI.setIngredienteController(ingredienteController);
            dettagliGUI.setCorso(corso);
            
            dettagliGUI.setOnChiudiCallback(() -> {
                cachedCorsiChef = null;
                contentRoot.getChildren().setAll(this.getRoot());
            });
            
            contentRoot.getChildren().setAll(dettagliGUI.getRoot());
            
        } catch (DataAccessException ex) {
            StyleHelper.showErrorDialog("Errore Database", "Impossibile aprire i dettagli del corso. Riprova tra qualche istante.");
            System.err.println("DataAccessException: " + ex.getMessage());
            
        } catch (Exception ex) {
            StyleHelper.showErrorDialog("Errore Imprevisto", "Si è verificato un errore inaspettato. Contatta l'assistenza.");
            System.err.println("Errore apertura dettagli: " + ex.getMessage());
        }
    }
    
    private boolean isCorsoFinito(CorsoCucina corso) {
        return corso.getDataFineCorso() != null && corso.getDataFineCorso().isBefore(LocalDateTime.now());
    }
}
