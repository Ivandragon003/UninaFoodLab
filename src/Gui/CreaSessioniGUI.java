package Gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.*;
import util.StyleHelper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class CreaSessioniGUI extends Stage {
    
    private Sessione sessioneCreata = null;
    private LocalDate dataInizioCorso;
    private LocalDate dataFineCorso;
    private Set<LocalDate> dateOccupate;
    
    private DatePicker dataSessionePicker;
    private ComboBox<Integer> oraInizio;
    private ComboBox<Integer> minutoInizio;
    private ComboBox<Integer> oraFine;
    private ComboBox<Integer> minutoFine;
    private ComboBox<String> tipoCombo;
    
    // Campi Online
    private TextField piattaformaField;
    
    // Campi In Presenza
    private TextField viaField;
    private TextField cittaField;
    private TextField postiField;
    private TextField capField;
    private VBox ricetteContainer;
    private Button aggiungiRicettaBtn;
    
    private VBox onlineBox;
    private VBox presenzaBox;
    private List<RicettaSemplice> ricetteSelezionate = new ArrayList<>();
    
    // Classe semplice per ricetta
    public static class RicettaSemplice {
        private String nome;
        private int tempo;
        private String difficolta;
        
        public RicettaSemplice(String nome, int tempo, String difficolta) {
            this.nome = nome;
            this.tempo = tempo;
            this.difficolta = difficolta;
        }
        
        public String getNome() { return nome; }
        public int getTempo() { return tempo; }
        public String getDifficolta() { return difficolta; }
    }
    
    public CreaSessioniGUI(LocalDate dataInizioCorso, LocalDate dataFineCorso, Set<LocalDate> dateOccupate) {
        this.dataInizioCorso = dataInizioCorso;
        this.dataFineCorso = dataFineCorso;
        this.dateOccupate = dateOccupate != null ? dateOccupate : new HashSet<>();
        initializeDialog();
    }
    
    private void initializeDialog() {
        setTitle("Crea Sessione");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);
        
        createLayout();
    }
    
    private void createLayout() {
        // ROOT con SFONDO ARANCIONE come LOGIN
        StackPane rootPane = new StackPane();
        rootPane.setPrefSize(600, 750);
        
        // Sfondo arancione
        Region background = new Region();
        StyleHelper.applyBackgroundGradient(background);
        
        VBox mainContainer = new VBox(25);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(30));
        
        Label title = new Label("🎯 Crea Nuova Sessione");
        title.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPrefHeight(550);
        
        VBox formCard = StyleHelper.createSection();
        formCard.setSpacing(20);
        
        // Spiegazione calendario
        VBox spiegazioneBox = createSpiegazioneCalendario();
        
        // Sezione data e orari
        VBox dateSection = createDateTimeSection();
        
        // Sezione tipo sessione
        VBox tipoSection = createTipoSessioneSection();
        
        // Sezione campi specifici
        VBox campiSection = createCampiSection();
        
        // Pulsanti
        HBox buttonSection = createButtonSection();
        
        formCard.getChildren().addAll(
            spiegazioneBox,
            new Separator(),
            dateSection,
            new Separator(),
            tipoSection,
            campiSection,
            new Separator(),
            buttonSection
        );
        
        scrollPane.setContent(formCard);
        mainContainer.getChildren().addAll(title, scrollPane);
        
        rootPane.getChildren().addAll(background, mainContainer);
        
        Scene scene = new Scene(rootPane, 600, 750);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);
    }
    
    private VBox createSpiegazioneCalendario() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: #f0f8ff; -fx-border-color: #87ceeb; " +
                    "-fx-border-radius: 8; -fx-background-radius: 8;");
        
        Label titleLabel = new Label("📅 Calendario Sessioni - Legenda");
        titleLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));
        
        Label rossoLabel = new Label("🔴 Date NON disponibili (fuori periodo corso)");
        rossoLabel.setFont(javafx.scene.text.Font.font("Roboto", 13));
        rossoLabel.setTextFill(Color.web(StyleHelper.ERROR_RED));
        
        Label arancioneLabel = new Label("🟠 Date con sessioni esistenti (sovrapponibile)");
        arancioneLabel.setFont(javafx.scene.text.Font.font("Roboto", 13));
        arancioneLabel.setTextFill(Color.web(StyleHelper.WARNING_ORANGE));
        
        Label verdeLabel = new Label("🟢 Date disponibili per nuove sessioni");
        verdeLabel.setFont(javafx.scene.text.Font.font("Roboto", 13));
        verdeLabel.setTextFill(Color.web(StyleHelper.SUCCESS_GREEN));
        
        box.getChildren().addAll(titleLabel, rossoLabel, arancioneLabel, verdeLabel);
        return box;
    }
    
    private VBox createDateTimeSection() {
        VBox section = new VBox(15);
        
        Label sectionTitle = new Label("📅 Data e Orari");
        sectionTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        
        // Data sessione con calendario colorato
        dataSessionePicker = createColoredDatePicker();
        
        // Orari
        oraInizio = createTimeComboBox(24, 10);
        minutoInizio = createTimeComboBox(60, 0, 15);
        oraFine = createTimeComboBox(24, 12);
        minutoFine = createTimeComboBox(60, 0, 15);
        
        HBox oraInizioBox = new HBox(5, oraInizio, new Label(":"), minutoInizio);
        oraInizioBox.setAlignment(Pos.CENTER_LEFT);
        HBox oraFineBox = new HBox(5, oraFine, new Label(":"), minutoFine);
        oraFineBox.setAlignment(Pos.CENTER_LEFT);
        
        grid.add(StyleHelper.createLabel("Data Sessione:"), 0, 0);
        grid.add(dataSessionePicker, 1, 0);
        grid.add(StyleHelper.createLabel("Ora Inizio:"), 0, 1);
        grid.add(oraInizioBox, 1, 1);
        grid.add(StyleHelper.createLabel("Ora Fine:"), 2, 1);
        grid.add(oraFineBox, 3, 1);
        
        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }
    
    private VBox createTipoSessioneSection() {
        VBox section = new VBox(15);
        
        Label sectionTitle = new Label("🎯 Tipo Sessione");
        sectionTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));
        
        tipoCombo = StyleHelper.createComboBox();
        tipoCombo.getItems().addAll("Online", "In Presenza");
        tipoCombo.setValue("Online");
        tipoCombo.setOnAction(e -> updateCampiVisibility());
        
        section.getChildren().addAll(sectionTitle, tipoCombo);
        return section;
    }
    
    private VBox createCampiSection() {
        VBox section = new VBox(15);
        
        // Campi Online
        onlineBox = new VBox(10);
        Label onlineTitle = new Label("🌐 Dettagli Online");
        onlineTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 16));
        onlineTitle.setTextFill(Color.web(StyleHelper.INFO_BLUE));
        
        piattaformaField = StyleHelper.createTextField("Piattaforma (es. Zoom, Teams, Meet)");
        
        onlineBox.getChildren().addAll(onlineTitle, piattaformaField);
        
        // Campi In Presenza
        presenzaBox = new VBox(15);
        Label presenzaTitle = new Label("🏢 Dettagli In Presenza");
        presenzaTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 16));
        presenzaTitle.setTextFill(Color.web(StyleHelper.SUCCESS_GREEN));
        
        GridPane presenzaGrid = new GridPane();
        presenzaGrid.setHgap(15);
        presenzaGrid.setVgap(10);
        
        viaField = StyleHelper.createTextField("Via e numero civico");
        cittaField = StyleHelper.createTextField("Città");
        postiField = StyleHelper.createTextField("Numero posti disponibili");
        capField = StyleHelper.createTextField("CAP");
        
        presenzaGrid.add(StyleHelper.createLabel("Via:"), 0, 0);
        presenzaGrid.add(viaField, 1, 0);
        presenzaGrid.add(StyleHelper.createLabel("Città:"), 0, 1);
        presenzaGrid.add(cittaField, 1, 1);
        presenzaGrid.add(StyleHelper.createLabel("Posti:"), 2, 0);
        presenzaGrid.add(postiField, 3, 0);
        presenzaGrid.add(StyleHelper.createLabel("CAP:"), 2, 1);
        presenzaGrid.add(capField, 3, 1);
        
        // RICETTE OBBLIGATORIE per In Presenza
        VBox ricetteSection = createRicetteSection();
        
        presenzaBox.getChildren().addAll(presenzaTitle, presenzaGrid, ricetteSection);
        presenzaBox.setVisible(false);
        presenzaBox.setManaged(false);
        
        section.getChildren().addAll(onlineBox, presenzaBox);
        return section;
    }
    
    private VBox createRicetteSection() {
        VBox section = new VBox(10);
        
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label ricetteTitle = new Label("📖 Ricette del Corso");
        ricetteTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 15));
        ricetteTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));
        
        Label ricetteObblLabel = new Label("⚠️ OBBLIGATORIE per sessioni in presenza");
        ricetteObblLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 12));
        ricetteObblLabel.setTextFill(Color.web(StyleHelper.ERROR_RED));
        
        headerBox.getChildren().addAll(ricetteTitle, ricetteObblLabel);
        
        aggiungiRicettaBtn = StyleHelper.createSuccessButton("+ Aggiungi Ricetta");
        aggiungiRicettaBtn.setOnAction(e -> aggiungiRicetta());
        
        Label ricetteLabel = StyleHelper.createLabel("Ricette selezionate:");
        
        ricetteContainer = new VBox(5);
        ricetteContainer.setPrefHeight(120);
        ricetteContainer.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; " +
                                 "-fx-border-radius: 8; -fx-padding: 10;");
        
        updateRicetteDisplay();
        
        section.getChildren().addAll(headerBox, aggiungiRicettaBtn, ricetteLabel, ricetteContainer);
        return section;
    }
    
    private void updateRicetteDisplay() {
        ricetteContainer.getChildren().clear();
        
        if (ricetteSelezionate.isEmpty()) {
            Label emptyLabel = new Label("Nessuna ricetta selezionata");
            emptyLabel.setTextFill(Color.GRAY);
            ricetteContainer.getChildren().add(emptyLabel);
        } else {
            for (int i = 0; i < ricetteSelezionate.size(); i++) {
                RicettaSemplice ricetta = ricetteSelezionate.get(i);
                
                HBox ricettaBox = new HBox(10);
                ricettaBox.setAlignment(Pos.CENTER_LEFT);
                ricettaBox.setPadding(new Insets(8));
                ricettaBox.setStyle("-fx-background-color: #fff8dc; -fx-border-color: #deb887; " +
                                   "-fx-border-radius: 5; -fx-background-radius: 5;");
                
                VBox infoBox = new VBox(2);
                
                Label nameLabel = new Label("📖 " + ricetta.getNome());
                nameLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 14));
                nameLabel.setTextFill(Color.BLACK);
                
                Label detailsLabel = new Label("⏱️ " + ricetta.getTempo() + " min • " + ricetta.getDifficolta());
                detailsLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
                detailsLabel.setTextFill(Color.GRAY);
                
                infoBox.getChildren().addAll(nameLabel, detailsLabel);
                
                Button removeBtn = new Button("✕");
                removeBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; " +
                                 "-fx-background-radius: 15; -fx-min-width: 25; -fx-min-height: 25; " +
                                 "-fx-max-width: 25; -fx-max-height: 25; -fx-cursor: hand; -fx-font-weight: bold;");
                final int index = i;
                removeBtn.setOnAction(e -> {
                    ricetteSelezionate.remove(index);
                    updateRicetteDisplay();
                });
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                ricettaBox.getChildren().addAll(infoBox, spacer, removeBtn);
                ricetteContainer.getChildren().add(ricettaBox);
            }
        }
    }
    
    private void aggiungiRicetta() {
        // Dialog semplice per aggiungere ricetta
        AggiungiRicettaDialog dialog = new AggiungiRicettaDialog();
        RicettaSemplice ricetta = dialog.showAndReturn();
        
        if (ricetta != null) {
            ricetteSelezionate.add(ricetta);
            updateRicetteDisplay();
        }
    }
    
    private HBox createButtonSection() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));
        
        Button annullaBtn = new Button("❌ Annulla");
        annullaBtn.setPrefWidth(130);
        annullaBtn.setStyle("-fx-background-color: " + StyleHelper.NEUTRAL_GRAY + "; " +
                           "-fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-font-weight: bold;");
        annullaBtn.setOnAction(e -> {
            sessioneCreata = null;
            close();
        });
        
        Button salvaBtn = StyleHelper.createPrimaryButton("💾 Salva Sessione");
        salvaBtn.setPrefWidth(130);
        salvaBtn.setOnAction(e -> salvaSessione());
        
        buttonBox.getChildren().addAll(annullaBtn, salvaBtn);
        return buttonBox;
    }
    
    private DatePicker createColoredDatePicker() {
        DatePicker picker = StyleHelper.createDatePicker();
        
        picker.setDayCellFactory(new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(DatePicker param) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        if (empty || date == null) return;
                        
                        // Date fuori periodo corso - ROSSE
                        if (date.isBefore(dataInizioCorso) || date.isAfter(dataFineCorso)) {
                            setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-weight: bold;");
                            setDisable(true);
                            setOpacity(0.7);
                            setTooltip(new Tooltip("❌ Data non disponibile - Fuori periodo corso"));
                        }
                        // Date con sessioni esistenti - ARANCIONI
                        else if (dateOccupate.contains(date)) {
                            setStyle("-fx-background-color: #ffd93d; -fx-text-fill: #333; -fx-font-weight: bold;");
                            setTooltip(new Tooltip("⚠️ Data con sessione esistente - Sovrapponibile"));
                        }
                        // Oggi - EVIDENZIATO
                        else if (date.equals(LocalDate.now())) {
                            setStyle("-fx-background-color: #FF9966; -fx-text-fill: white; -fx-font-weight: bold;");
                            setTooltip(new Tooltip("📅 Oggi"));
                        }
                        // Date disponibili - VERDI
                        else {
                            setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-font-weight: bold;");
                            setTooltip(new Tooltip("✅ Data disponibile"));
                        }
                    }
                };
            }
        });
        
        return picker;
    }
    
    private ComboBox<Integer> createTimeComboBox(int max, int defaultValue) {
        return createTimeComboBox(max, defaultValue, 1);
    }
    
    private ComboBox<Integer> createTimeComboBox(int max, int defaultValue, int step) {
        ComboBox<Integer> combo = new ComboBox<>();
        for (int i = 0; i < max; i += step) {
            combo.getItems().add(i);
        }
        combo.setValue(defaultValue);
        combo.setPrefHeight(35);
        combo.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: " + StyleHelper.BORDER_LIGHT + "; " +
                      "-fx-border-radius: 8; -fx-border-width: 1;");
        return combo;
    }
    
    private void updateCampiVisibility() {
        boolean isOnline = "Online".equals(tipoCombo.getValue());
        
        onlineBox.setVisible(isOnline);
        onlineBox.setManaged(isOnline);
        
        presenzaBox.setVisible(!isOnline);
        presenzaBox.setManaged(!isOnline);
    }
    
    private void salvaSessione() {
        try {
            if (!validateForm()) return;
            
            LocalDate dataSessione = dataSessionePicker.getValue();
            LocalTime timeInizio = LocalTime.of(oraInizio.getValue(), minutoInizio.getValue());
            LocalTime timeFine = LocalTime.of(oraFine.getValue(), minutoFine.getValue());
            
            LocalDateTime inizioSessione = LocalDateTime.of(dataSessione, timeInizio);
            LocalDateTime fineSessione = LocalDateTime.of(dataSessione, timeFine);
            
            if ("Online".equals(tipoCombo.getValue())) {
                String piattaforma = piattaformaField.getText().trim();
                sessioneCreata = new Online(inizioSessione, fineSessione, piattaforma);
            } else {
                String via = viaField.getText().trim();
                String citta = cittaField.getText().trim();
                int posti = Integer.parseInt(postiField.getText().trim());
                int cap = Integer.parseInt(capField.getText().trim());
                
                sessioneCreata = new InPresenza(inizioSessione, fineSessione, via, citta, posti, cap);
            }
            
            close();
            
        } catch (Exception e) {
            showAlert("Errore", "Errore nel salvataggio: " + e.getMessage());
        }
    }
    
    private boolean validateForm() {
        if (dataSessionePicker.getValue() == null) {
            showAlert("Validazione", "Selezionare la data della sessione");
            return false;
        }
        
        LocalTime inizio = LocalTime.of(oraInizio.getValue(), minutoInizio.getValue());
        LocalTime fine = LocalTime.of(oraFine.getValue(), minutoFine.getValue());
        
        if (fine.isBefore(inizio) || fine.equals(inizio)) {
            showAlert("Validazione", "L'ora di fine deve essere successiva all'ora di inizio");
            return false;
        }
        
        if ("Online".equals(tipoCombo.getValue())) {
            if (piattaformaField.getText().trim().isEmpty()) {
                showAlert("Validazione", "Inserire la piattaforma per la sessione online");
                return false;
            }
        } else {
            // Validazione campi In Presenza
            if (viaField.getText().trim().isEmpty()) {
                showAlert("Validazione", "Inserire l'indirizzo");
                return false;
            }
            if (cittaField.getText().trim().isEmpty()) {
                showAlert("Validazione", "Inserire la città");
                return false;
            }
            try {
                int posti = Integer.parseInt(postiField.getText().trim());
                if (posti <= 0) {
                    showAlert("Validazione", "Il numero di posti deve essere maggiore di 0");
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("Validazione", "Inserire un numero valido per i posti");
                return false;
            }
            try {
                int cap = Integer.parseInt(capField.getText().trim());
                if (cap < 10000 || cap > 99999) {
                    showAlert("Validazione", "Inserire un CAP valido (5 cifre)");
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("Validazione", "Inserire un CAP valido");
                return false;
            }
            
            // RICETTE OBBLIGATORIE per In Presenza
            if (ricetteSelezionate.isEmpty()) {
                showAlert("Ricette Obbligatorie", 
                         "Le sessioni in presenza devono avere almeno una ricetta associata.\n\n" +
                         "Clicca su '+ Aggiungi Ricetta' per selezionarne una.");
                return false;
            }
        }
        
        return true;
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public Sessione showDialog() {
        showAndWait();
        return sessioneCreata;
    }
}

// Dialog semplice per aggiungere ricetta
class AggiungiRicettaDialog extends Stage {
    private CreaSessioniGUI.RicettaSemplice ricettaCreata = null;
    
    public AggiungiRicettaDialog() {
        setTitle("Aggiungi Ricetta");
        initModality(Modality.APPLICATION_MODAL);
        
        createLayout();
    }
    
    private void createLayout() {
        StackPane rootPane = new StackPane();
        rootPane.setPrefSize(450, 400);
        
        Region background = new Region();
        StyleHelper.applyBackgroundGradient(background);
        
        VBox container = new VBox(20);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(30));
        
        Label title = new Label("📖 Aggiungi Ricetta alla Sessione");
        title.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);
        
        VBox formCard = StyleHelper.createSection();
        formCard.setSpacing(15);
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        
        TextField nomeField = StyleHelper.createTextField("Nome ricetta");
        TextField tempoField = StyleHelper.createTextField("Tempo in minuti");
        tempoField.setText("30");
        
        ComboBox<String> difficoltaBox = StyleHelper.createComboBox();
        difficoltaBox.getItems().addAll("Facile", "Medio", "Difficile");
        difficoltaBox.setValue("Facile");
        
        grid.add(StyleHelper.createLabel("Nome:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(StyleHelper.createLabel("Tempo (min):"), 0, 1);
        grid.add(tempoField, 1, 1);
        grid.add(StyleHelper.createLabel("Difficoltà:"), 0, 2);
        grid.add(difficoltaBox, 1, 2);
        
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));
        
        Button salvaBtn = StyleHelper.createPrimaryButton("📖 Aggiungi");
        Button annullaBtn = new Button("❌ Annulla");
        annullaBtn.setStyle("-fx-background-color: " + StyleHelper.NEUTRAL_GRAY + "; " +
                           "-fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand;");
        
        salvaBtn.setOnAction(e -> {
            String nome = nomeField.getText().trim();
            String tempoText = tempoField.getText().trim();
            String difficolta = difficoltaBox.getValue();
            
            if (nome.isEmpty()) {
                showAlert("Validazione", "Il nome è obbligatorio");
                return;
            }
            
            try {
                int tempo = Integer.parseInt(tempoText);
                if (tempo <= 0) {
                    showAlert("Validazione", "Il tempo deve essere maggiore di 0");
                    return;
                }
                
                ricettaCreata = new CreaSessioniGUI.RicettaSemplice(nome, tempo, difficolta);
                close();
            } catch (NumberFormatException ex) {
                showAlert("Validazione", "Inserire un tempo valido");
            }
        });
        
        annullaBtn.setOnAction(e -> close());
        
        buttonBox.getChildren().addAll(annullaBtn, salvaBtn);
        
        formCard.getChildren().addAll(grid, buttonBox);
        container.getChildren().addAll(title, formCard);
        rootPane.getChildren().addAll(background, container);
        
        setScene(new Scene(rootPane, 450, 400));
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public CreaSessioniGUI.RicettaSemplice showAndReturn() {
        showAndWait();
        return ricettaCreata;
    }
}