package Gui;

import controller.GestioneCorsoController;
import controller.GestioneSessioniController;
import exceptions.ValidationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.*;
import service.GestioneRicette;
import service.GestioneCucina;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class CreaCorsoGUI {
    
    private GestioneCorsoController gestioneController;
    private GestioneRicette gestioneRicette;
    private GestioneSessioniController sessioniController;
    private GestioneCucina gestioneCucina;
    
    // Componenti della form
    private TextField nomeField;
    private TextField prezzoField;
    private TextField argomentoField;
    private TextField postiField;
    private ComboBox<Frequenza> frequenzaBox;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private ComboBox<Integer> startHour;
    private ComboBox<Integer> startMinute;
    private ComboBox<Integer> endHour;
    private ComboBox<Integer> endMinute;
    
    // Liste per chef e sessioni
    private ListView<Chef> listaChefDisponibili;
    private ListView<Chef> listaChefSelezionati;
    private ListView<Sessione> listaSessioni;
    
    // Dati
    private ObservableList<Chef> chefSelezionati = FXCollections.observableArrayList();
    private ObservableList<Sessione> corsoSessioni = FXCollections.observableArrayList();
    
    private VBox root;

    public CreaCorsoGUI() {
        // Costruttore vuoto per compatibilità
    }

    public void setController(GestioneCorsoController gestioneController) {
        this.gestioneController = gestioneController;
        initializeServices();
    }
    
    private void initializeServices() {
        // Inizializza i servizi necessari per le sessioni
        try {
            // Questi dovrebbero essere passati dal ChefMenuGUI o inizializzati qui
            // Per ora li lascio null, ma dovrai collegarli ai tuoi DAO reali
            this.gestioneRicette = null;
            this.gestioneCucina = null;
            this.sessioniController = null;
        } catch (Exception e) {
            System.err.println("Errore inizializzazione servizi: " + e.getMessage());
        }
    }

    public VBox getRoot() {
        if (root == null) {
            root = createMainLayout();
        }
        return root;
    }

    private VBox createMainLayout() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(25));
        container.setStyle("-fx-background-color: #f8f9fa;");

        // Titolo principale
        Label titleLabel = new Label("✨ Crea Nuovo Corso di Cucina");
        titleLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#FF6600"));
        titleLabel.setAlignment(Pos.CENTER);

        // Sezioni del form
        VBox infoSection = createInfoSection();
        VBox dateSection = createDateTimeSection();
        VBox chefSection = createChefSection();
        VBox sessionSection = createSessionSection();
        HBox buttonSection = createButtonSection();

        // Contenuto con scroll
        VBox contentBox = new VBox(15);
        contentBox.getChildren().addAll(
            infoSection,
            new Separator(),
            dateSection,
            new Separator(),
            chefSection,
            new Separator(),
            sessionSection,
            new Separator(),
            buttonSection
        );

        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setPrefHeight(600);

        container.getChildren().addAll(titleLabel, scrollPane);
        return container;
    }

    private VBox createInfoSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; " +
                        "-fx-border-color: #e0e0e0; -fx-border-radius: 10;");

        Label sectionTitle = new Label("📋 Informazioni Corso");
        sectionTitle.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web("#FF6600"));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        // Campi del form
        nomeField = createStyledTextField("Es. Corso Base di Pasta Italiana");
        prezzoField = createStyledTextField("Es. 150.00");
        argomentoField = createStyledTextField("Es. Pasta fresca e condimenti");
        postiField = createStyledTextField("Es. 12");
        
        frequenzaBox = new ComboBox<>();
        frequenzaBox.getItems().addAll(Frequenza.values());
        frequenzaBox.setPromptText("Seleziona frequenza");
        frequenzaBox.setPrefHeight(35);
        styleComboBox(frequenzaBox);

        // Layout griglia
        grid.add(createLabel("Nome Corso:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(createLabel("Prezzo (€):"), 2, 0);
        grid.add(prezzoField, 3, 0);

        grid.add(createLabel("Argomento:"), 0, 1);
        grid.add(argomentoField, 1, 1);
        grid.add(createLabel("Numero Posti:"), 2, 1);
        grid.add(postiField, 3, 1);

        grid.add(createLabel("Frequenza:"), 0, 2);
        grid.add(frequenzaBox, 1, 2);

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    private VBox createDateTimeSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; " +
                        "-fx-border-color: #e0e0e0; -fx-border-radius: 10;");

        Label sectionTitle = new Label("📅 Date e Orari");
        sectionTitle.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web("#FF6600"));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        // Date picker
        startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Data inizio");
        startDatePicker.setPrefHeight(35);
        styleDatePicker(startDatePicker);
        
        endDatePicker = new DatePicker();
        endDatePicker.setPromptText("Data fine");
        endDatePicker.setPrefHeight(35);
        styleDatePicker(endDatePicker);

        // Combo box orari
        startHour = createHourComboBox();
        startMinute = createMinuteComboBox();
        endHour = createHourComboBox();
        endMinute = createMinuteComboBox();

        HBox startTimeBox = new HBox(5, startHour, new Label(":"), startMinute);
        startTimeBox.setAlignment(Pos.CENTER_LEFT);
        HBox endTimeBox = new HBox(5, endHour, new Label(":"), endMinute);
        endTimeBox.setAlignment(Pos.CENTER_LEFT);

        // Layout griglia
        grid.add(createLabel("Data Inizio:"), 0, 0);
        grid.add(startDatePicker, 1, 0);
        grid.add(createLabel("Ora Inizio:"), 2, 0);
        grid.add(startTimeBox, 3, 0);

        grid.add(createLabel("Data Fine:"), 0, 1);
        grid.add(endDatePicker, 1, 1);
        grid.add(createLabel("Ora Fine:"), 2, 1);
        grid.add(endTimeBox, 3, 1);

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    private VBox createChefSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; " +
                        "-fx-border-color: #e0e0e0; -fx-border-radius: 10;");

        Label sectionTitle = new Label("👨‍🍳 Selezione Chef");
        sectionTitle.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web("#FF6600"));

        HBox chefContainer = new HBox(20);

        // Lista chef disponibili
        VBox disponibiliBox = new VBox(10);
        Label disponibiliLabel = createLabel("Chef Disponibili:");
        listaChefDisponibili = new ListView<>();
        listaChefDisponibili.setPrefHeight(120);
        styleListView(listaChefDisponibili);
        loadChefDisponibili();
        disponibiliBox.getChildren().addAll(disponibiliLabel, listaChefDisponibili);

        // Pulsanti gestione
        VBox buttonsBox = new VBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        Button aggiungiChefBtn = createButton("➡️ Aggiungi", "#FF6600");
        Button rimuoviChefBtn = createButton("⬅️ Rimuovi", "#e74c3c");
        
        aggiungiChefBtn.setOnAction(e -> aggiungiChef());
        rimuoviChefBtn.setOnAction(e -> rimuoviChef());
        
        buttonsBox.getChildren().addAll(aggiungiChefBtn, rimuoviChefBtn);

        // Lista chef selezionati
        VBox selezionatiBox = new VBox(10);
        Label selezionatiLabel = createLabel("Chef Selezionati:");
        listaChefSelezionati = new ListView<>(chefSelezionati);
        listaChefSelezionati.setPrefHeight(120);
        styleListView(listaChefSelezionati);
        selezionatiBox.getChildren().addAll(selezionatiLabel, listaChefSelezionati);

        chefContainer.getChildren().addAll(disponibiliBox, buttonsBox, selezionatiBox);
        section.getChildren().addAll(sectionTitle, chefContainer);
        return section;
    }

    private VBox createSessionSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; " +
                        "-fx-border-color: #e0e0e0; -fx-border-radius: 10;");

        Label sectionTitle = new Label("🎯 Sessioni del Corso");
        sectionTitle.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web("#FF6600"));

        HBox sessionContainer = new HBox(20);

        // Lista sessioni
        VBox sessioniBox = new VBox(10);
        Label sessioniLabel = createLabel("Sessioni create:");
        listaSessioni = new ListView<>(corsoSessioni);
        listaSessioni.setPrefHeight(120);
        styleListView(listaSessioni);
        
        // Formato visualizzazione sessioni
        listaSessioni.setCellFactory(listView -> new ListCell<Sessione>() {
            @Override
            protected void updateItem(Sessione sessione, boolean empty) {
                super.updateItem(sessione, empty);
                if (empty || sessione == null) {
                    setText(null);
                } else {
                    String tipo = sessione instanceof Online ? "Online" : "In Presenza";
                    String data = sessione.getDataInizioSessione() != null ? 
                        sessione.getDataInizioSessione().toLocalDate().toString() : "Data non specificata";
                    setText(String.format("%s - %s", tipo, data));
                }
            }
        });
        
        sessioniBox.getChildren().addAll(sessioniLabel, listaSessioni);

        // Pulsanti sessioni con collegamento a CreaSessioniGUI
        VBox sessionButtonsBox = new VBox(10);
        sessionButtonsBox.setAlignment(Pos.CENTER);
        
        Button creaSessioneBtn = createButton("➕ Crea", "#27ae60");
        Button eliminaSessioneBtn = createButton("🗑️ Elimina", "#e74c3c");

        // COLLEGAMENTO PRINCIPALE - Quando clicchi "Crea" apre CreaSessioniGUI
        creaSessioneBtn.setOnAction(e -> creaSessione());
        eliminaSessioneBtn.setOnAction(e -> eliminaSessione());

        sessionButtonsBox.getChildren().addAll(creaSessioneBtn, eliminaSessioneBtn);

        sessionContainer.getChildren().addAll(sessioniBox, sessionButtonsBox);
        section.getChildren().addAll(sectionTitle, sessionContainer);
        return section;
    }

    // METODO PRINCIPALE PER COLLEGARE CreaSessioniGUI
    private void creaSessione() {
        try {
            // Crea CreaSessioniGUI con i servizi necessari
            CreaSessioniGUI creaSessioniGUI = new CreaSessioniGUI(gestioneRicette, gestioneCucina);
            
            // Apre il dialog e aspetta il risultato
            Sessione nuovaSessione = creaSessioniGUI.creaSessioneEmbedded();
            
            // Se è stata creata una sessione, aggiungila alla lista
            if (nuovaSessione != null) {
                corsoSessioni.add(nuovaSessione);
                showInfo("Successo", "Sessione creata e aggiunta al corso!");
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Errore", "Errore durante la creazione della sessione: " + ex.getMessage());
        }
    }

    private HBox createButtonSection() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button salvaBtn = createButton("💾 Salva Corso", "#FF6600");
        Button resetBtn = createButton("🔄 Reset Form", "#95a5a6");

        salvaBtn.setPrefWidth(150);
        resetBtn.setPrefWidth(150);

        salvaBtn.setOnAction(e -> salvaCorso());
        resetBtn.setOnAction(e -> clearForm());

        buttonBox.getChildren().addAll(resetBtn, salvaBtn);
        return buttonBox;
    }

    // Creazione componenti helper
    
    private TextField createStyledTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(35);
        styleTextField(field);
        return field;
    }
    
    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        return label;
    }
    
    private ComboBox<Integer> createHourComboBox() {
        ComboBox<Integer> combo = new ComboBox<>();
        for (int i = 0; i < 24; i++) {
            combo.getItems().add(i);
        }
        combo.setValue(9);
        combo.setPrefHeight(35);
        styleComboBox(combo);
        return combo;
    }

    private ComboBox<Integer> createMinuteComboBox() {
        ComboBox<Integer> combo = new ComboBox<>();
        for (int i = 0; i < 60; i += 15) {
            combo.getItems().add(i);
        }
        combo.setValue(0);
        combo.setPrefHeight(35);
        styleComboBox(combo);
        return combo;
    }

    private Button createButton(String text, String color) {
        Button button = new Button(text);
        button.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
        button.setTextFill(Color.WHITE);
        button.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 8 16; " +
            "-fx-cursor: hand;", color
        ));

        // Effetto hover
        button.setOnMouseEntered(e -> button.setStyle(button.getStyle() + "-fx-opacity: 0.8;"));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle().replace("-fx-opacity: 0.8;", "")));

        return button;
    }

    // Stili componenti
    
    private void styleTextField(TextField field) {
        field.setStyle("-fx-background-radius: 8; -fx-border-color: #d0d0d0; " +
                      "-fx-border-radius: 8; -fx-border-width: 1;");
    }

    private void styleComboBox(ComboBox<?> combo) {
        combo.setStyle("-fx-background-radius: 8; -fx-border-color: #d0d0d0; " +
                      "-fx-border-radius: 8; -fx-border-width: 1;");
    }

    private void styleDatePicker(DatePicker picker) {
        picker.setStyle("-fx-background-radius: 8; -fx-border-color: #d0d0d0; " +
                       "-fx-border-radius: 8; -fx-border-width: 1;");
    }

    private void styleListView(ListView<?> listView) {
        listView.setStyle("-fx-background-radius: 8; -fx-border-color: #d0d0d0; " +
                         "-fx-border-radius: 8; -fx-border-width: 1;");
    }

    // Gestione dati

    private void loadChefDisponibili() {
        if (gestioneController != null) {
            try {
                List<Chef> tuttiChef = gestioneController.getTuttiGliChef();
                listaChefDisponibili.getItems().setAll(tuttiChef);
            } catch (Exception e) {
                showError("Errore", "Impossibile caricare i chef: " + e.getMessage());
            }
        }
    }

    private void aggiungiChef() {
        Chef selected = listaChefDisponibili.getSelectionModel().getSelectedItem();
        if (selected != null && !chefSelezionati.contains(selected)) {
            chefSelezionati.add(selected);
            listaChefDisponibili.getSelectionModel().clearSelection();
        }
    }

    private void rimuoviChef() {
        Chef selected = listaChefSelezionati.getSelectionModel().getSelectedItem();
        if (selected != null) {
            chefSelezionati.remove(selected);
        }
    }

    private void eliminaSessione() {
        Sessione selected = listaSessioni.getSelectionModel().getSelectedItem();
        if (selected != null) {
            corsoSessioni.remove(selected);
        }
    }

    private void salvaCorso() {
        try {
            if (!validateForm()) return;

            CorsoCucina corso = createCorsoFromForm();

            // Aggiungi chef e sessioni al corso
            corso.getChef().clear();
            corso.getChef().addAll(chefSelezionati);
            corso.getSessioni().clear();
            corso.getSessioni().addAll(corsoSessioni);

            if (gestioneController != null) {
                try {
                    gestioneController.creaCorso(corso);
                    showInfo("Successo", "Corso creato con successo!");
                    clearForm();
                } catch (ValidationException ve) {
                    showError("Errore di validazione", ve.getMessage());
                } catch (Exception ex) {
                    // Controlla se è un errore del database
                    Throwable cause = ex.getCause();
                    if (cause instanceof SQLException) {
                        showError("Errore Database", "Problema con il database: " + cause.getMessage());
                    } else {
                        showError("Errore", "Si è verificato un errore: " + ex.getMessage());
                    }
                    ex.printStackTrace();
                }
            } else {
                showError("Errore", "Controller non inizializzato");
            }

        } catch (Exception ex) {
            showError("Errore", "Errore nella creazione del corso: " + ex.getMessage());
        }
    }

    private boolean validateForm() {
        if (nomeField.getText().trim().isEmpty()) {
            showError("Validazione", "Il nome del corso è obbligatorio");
            return false;
        }
        
        try {
            Double.parseDouble(prezzoField.getText());
        } catch (NumberFormatException e) {
            showError("Validazione", "Inserire un prezzo valido");
            return false;
        }
        
        try {
            Integer.parseInt(postiField.getText());
        } catch (NumberFormatException e) {
            showError("Validazione", "Inserire un numero di posti valido");
            return false;
        }

        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            showError("Validazione", "Selezionare le date di inizio e fine");
            return false;
        }

        return true;
    }

    private CorsoCucina createCorsoFromForm() {
        String nome = nomeField.getText().trim();
        double prezzo = Double.parseDouble(prezzoField.getText());
        String argomento = argomentoField.getText().trim();
        Frequenza frequenza = frequenzaBox.getValue();
        int numeroPosti = Integer.parseInt(postiField.getText());

        // Costruzione date complete
        LocalDate startDate = startDatePicker.getValue();
        LocalTime startTime = LocalTime.of(startHour.getValue(), startMinute.getValue());
        LocalDateTime dataInizio = LocalDateTime.of(startDate, startTime);

        LocalDate endDate = endDatePicker.getValue();
        LocalTime endTime = LocalTime.of(endHour.getValue(), endMinute.getValue());
        LocalDateTime dataFine = LocalDateTime.of(endDate, endTime);

        // Creazione corso
        CorsoCucina corso = new CorsoCucina(nome, prezzo, argomento, frequenza, numeroPosti);
        corso.setDataInizioCorso(dataInizio);
        corso.setDataFineCorso(dataFine);

        return corso;
    }

    private void clearForm() {
        nomeField.clear();
        prezzoField.clear();
        argomentoField.clear();
        postiField.clear();
        frequenzaBox.setValue(null);
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        chefSelezionati.clear();
        corsoSessioni.clear();
    }

    // Dialog di sistema
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}