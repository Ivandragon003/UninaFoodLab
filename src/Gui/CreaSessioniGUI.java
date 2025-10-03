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
    
    private VBox onlineBox;
    private VBox presenzaBox;
    
    public CreaSessioniGUI() {
        this.dataInizioCorso = LocalDate.now();
        this.dataFineCorso = LocalDate.now().plusDays(30);
        this.dateOccupate = new HashSet<>();
        initializeDialog();
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
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(25));
        mainContainer.setStyle("-fx-background-color: #f8f9fa;");
        
        Label title = StyleHelper.createTitleLabel("🎯 Crea Nuova Sessione");
        title.setAlignment(Pos.CENTER);
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPrefHeight(500);
        
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
        
        Scene scene = new Scene(mainContainer, 550, 650);
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
        rossoLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
        rossoLabel.setTextFill(Color.web("#e74c3c"));
        
        Label arancioneLabel = new Label("🟠 Date con sessioni esistenti (sovrapponibile)");
        arancioneLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
        arancioneLabel.setTextFill(Color.web("#f39c12"));
        
        Label verdeLabel = new Label("🟢 Date disponibili per nuove sessioni");
        verdeLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
        verdeLabel.setTextFill(Color.web("#27ae60"));
        
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
        presenzaBox = new VBox(10);
        Label presenzaTitle = new Label("🏢 Dettagli In Presenza");
        presenzaTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 16));
        presenzaTitle.setTextFill(Color.web(StyleHelper.SUCCESS_GREEN));
        
        viaField = StyleHelper.createTextField("Via e numero civico");
        cittaField = StyleHelper.createTextField("Città");
        postiField = StyleHelper.createTextField("Numero posti disponibili");
        capField = StyleHelper.createTextField("CAP");
        
        presenzaBox.getChildren().addAll(presenzaTitle, viaField, cittaField, postiField, capField);
        presenzaBox.setVisible(false);
        presenzaBox.setManaged(false);
        
        section.getChildren().addAll(onlineBox, presenzaBox);
        return section;
    }
    
    private HBox createButtonSection() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));
        
        Button annullaBtn = new Button("❌ Annulla");
        annullaBtn.setPrefWidth(130);
        annullaBtn.setStyle("-fx-background-color: " + StyleHelper.NEUTRAL_GRAY + "; " +
                           "-fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand;");
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
                        
                        // Date fuori periodo corso - ROSSE (non cliccabili)
                        if (date.isBefore(dataInizioCorso) || date.isAfter(dataFineCorso)) {
                            setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-weight: bold;");
                            setDisable(true);
                            setOpacity(0.7);
                            setTooltip(new Tooltip("❌ Data non disponibile - Fuori periodo corso"));
                        }
                        // Date con sessioni esistenti - ARANCIONI (cliccabili)
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
        combo.setStyle("-fx-background-radius: 8; -fx-border-color: " + StyleHelper.BORDER_LIGHT + "; " +
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