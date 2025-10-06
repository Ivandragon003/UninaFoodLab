package Gui;

import controller.GestioneCorsoController;
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
import util.StyleHelper;
import util.FrequenzaHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

public class CreaCorsoGUI {
    private final GestioneCorsoController gestioneController;
    
    // Campi input
    private TextField nomeField, prezzoField, argomentoField, postiField;
    private ComboBox<Frequenza> frequenzaBox;
    private DatePicker startDatePicker, endDatePicker;
    private ComboBox<Integer> startHour, startMinute, endHour, endMinute;
    
    // Container
    private VBox listaChefContainer, listaSessioniContainer;
    private Label numeroSessioniLabel;
    private Button aggiungiSessioneBtn;
    
    // Dati
    private final ObservableList<Chef> chefSelezionati = FXCollections.observableArrayList();
    private final ObservableList<Sessione> corsoSessioni = FXCollections.observableArrayList();
    
    private VBox root;

    public CreaCorsoGUI(GestioneCorsoController gestioneController) {
        this.gestioneController = gestioneController;
    }

    public VBox getRoot() {
        if (root == null) {
            root = createMainLayout();
        }
        return root;
    }

    // ==================== LAYOUT ====================

    private VBox createMainLayout() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        StyleHelper.applyOrangeBackground(container);

        Label titleLabel = StyleHelper.createTitleLabel("✨ Crea Nuovo Corso di Cucina");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setTextFill(Color.WHITE);

        ScrollPane scrollPane = new ScrollPane(new VBox(15,
            createInfoSection(),
            new Separator(),
            createDateTimeSection(),
            new Separator(),
            createChefSection(),
            new Separator(),
            createSessionSection(),
            new Separator(),
            createButtonSection()
        ));
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        container.getChildren().addAll(titleLabel, scrollPane);
        return container;
    }

    private VBox createInfoSection() {
        VBox section = StyleHelper.createSection();

        nomeField = StyleHelper.createTextField("Es. Corso Base di Pasta Italiana");
        prezzoField = StyleHelper.createTextField("Es. 150.00");
        argomentoField = StyleHelper.createTextField("Es. Pasta fresca e condimenti");
        postiField = StyleHelper.createTextField("Es. 12");
        frequenzaBox = createFrequenzaComboBox();

        GridPane grid = createInfoGrid();
        section.getChildren().addAll(createSectionTitle("📋 Informazioni Corso"), grid);
        return section;
    }

    private GridPane createInfoGrid() {
        GridPane grid = createGrid(15, 15);
        addGridRow(grid, 0, "Nome Corso:", nomeField, "Prezzo (€):", prezzoField);
        addGridRow(grid, 1, "Argomento:", argomentoField, "Numero Posti:", postiField);
        grid.add(StyleHelper.createLabel("Frequenza:"), 0, 2);
        grid.add(frequenzaBox, 1, 2);
        return grid;
    }

    private VBox createDateTimeSection() {
        VBox section = StyleHelper.createSection();

        initializeDatePickers();
        initializeTimeComboBoxes();

        GridPane grid = createDateTimeGrid();
        
        numeroSessioniLabel = createInfoLabel("📊 Sessioni: Non calcolate", "#666666");
        Label avisoLabel = createInfoLabel("⚠️ Seleziona le date per vedere le frequenze disponibili", 
                                          StyleHelper.ERROR_RED);

        section.getChildren().addAll(
            createSectionTitle("📅 Date e Orari - OBBLIGATORIO"),
            grid,
            numeroSessioniLabel,
            avisoLabel
        );
        return section;
    }

    private void initializeDatePickers() {
        startDatePicker = StyleHelper.createDatePicker();
        endDatePicker = StyleHelper.createDatePicker();
        startDatePicker.setPromptText("Data inizio");
        endDatePicker.setPromptText("Data fine");

        startDatePicker.setOnAction(e -> {
            onDataInizioChange();
            validateDatesForSessions();
        });
        
        endDatePicker.setOnAction(e -> {
            aggiornaFrequenzeDisponibili();
            validateDatesForSessions();
        });
    }

    private void initializeTimeComboBoxes() {
        startHour = createTimeComboBox(24, 9);
        startMinute = createTimeComboBox(60, 0, 15);
        endHour = createTimeComboBox(24, 17);
        endMinute = createTimeComboBox(60, 0, 15);
    }

    private GridPane createDateTimeGrid() {
        GridPane grid = createGrid(15, 15);
        addGridRow(grid, 0, "Data Inizio:", startDatePicker, "Ora Inizio:", createTimeBox(startHour, startMinute));
        addGridRow(grid, 1, "Data Fine:", endDatePicker, "Ora Fine:", createTimeBox(endHour, endMinute));
        return grid;
    }

    private VBox createChefSection() {
        VBox section = StyleHelper.createSection();

        Button selezionaBtn = StyleHelper.createPrimaryButton("+ Seleziona Chef");
        selezionaBtn.setOnAction(e -> apriDialogSelezionaChef());

        listaChefContainer = createListContainer();
        updateChefDisplay();

        section.getChildren().addAll(
            createSectionTitle("👨‍🍳 Selezione Chef"),
            selezionaBtn,
            StyleHelper.createLabel("Chef Selezionati:"),
            listaChefContainer
        );
        return section;
    }

    private VBox createSessionSection() {
        VBox section = StyleHelper.createSection();

        aggiungiSessioneBtn = StyleHelper.createSuccessButton("+ Aggiungi Sessione");
        aggiungiSessioneBtn.setDisable(true);
        aggiungiSessioneBtn.setOnAction(e -> aggiungiSessione());

        Button eliminaBtn = StyleHelper.createDangerButton("🗑️ Rimuovi");
        eliminaBtn.setOnAction(e -> eliminaUltimaSessione());

        listaSessioniContainer = createListContainer();
        updateSessioniDisplay();

        section.getChildren().addAll(
            createSectionTitle("🎯 Sessioni del Corso"),
            new HBox(10, aggiungiSessioneBtn, eliminaBtn),
            StyleHelper.createLabel("Sessioni aggiunte:"),
            listaSessioniContainer
        );
        return section;
    }

    private HBox createButtonSection() {
        Button resetBtn = StyleHelper.createSecondaryButton("🔄 Reset Form");
        resetBtn.setPrefWidth(150);
        resetBtn.setOnAction(e -> clearForm());

        Button salvaBtn = StyleHelper.createPrimaryButton("💾 Salva Corso");
        salvaBtn.setPrefWidth(150);
        salvaBtn.setOnAction(e -> salvaCorso());

        HBox box = new HBox(15, resetBtn, salvaBtn);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 0, 0, 0));
        return box;
    }

    // ==================== FREQUENZA E DATE ====================

    private ComboBox<Frequenza> createFrequenzaComboBox() {
        ComboBox<Frequenza> combo = new ComboBox<>();
        combo.setPromptText("Seleziona frequenza");
        combo.setPrefHeight(35);
        combo.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        combo.setOnAction(e -> onFrequenzaChange());
        return combo;
    }

    private void onDataInizioChange() {
        if (frequenzaBox.getValue() == Frequenza.UNICA && startDatePicker.getValue() != null) {
            endDatePicker.setValue(startDatePicker.getValue());
        }
        aggiornaFrequenzeDisponibili();
    }

    private void onFrequenzaChange() {
        boolean isUnica = frequenzaBox.getValue() == Frequenza.UNICA;
        
        if (isUnica && startDatePicker.getValue() != null) {
            endDatePicker.setValue(startDatePicker.getValue());
        }
        
        endDatePicker.setDisable(isUnica);
        endDatePicker.setStyle(isUnica ? "-fx-opacity: 0.6;" : "");
        aggiornaNumeroSessioniCalcolato();
    }

    private void aggiornaFrequenzeDisponibili() {
        LocalDate inizio = startDatePicker.getValue();
        LocalDate fine = endDatePicker.getValue();
        
        if (inizio != null && fine != null) {
            java.util.List<Frequenza> disponibili = FrequenzaHelper.getFrequenzeDisponibili(inizio, fine);
            Frequenza attuale = frequenzaBox.getValue();
            
            frequenzaBox.getItems().setAll(disponibili);
            frequenzaBox.setValue(disponibili.contains(attuale) ? attuale : 
                                  (!disponibili.isEmpty() ? disponibili.get(0) : null));
        } else {
            frequenzaBox.getItems().setAll(Frequenza.values());
        }
        
        aggiornaNumeroSessioniCalcolato();
    }

    private void aggiornaNumeroSessioniCalcolato() {
        LocalDate inizio = startDatePicker.getValue();
        LocalDate fine = endDatePicker.getValue();
        Frequenza freq = frequenzaBox.getValue();
        
        if (inizio != null && fine != null && freq != null) {
            try {
                int numSessioni = FrequenzaHelper.calcolaNumeroSessioni(inizio, fine, freq);
                updateSessioniLabel(String.format("📊 Sessioni calcolate: %d", numSessioni), "#4CAF50");
            } catch (Exception e) {
                updateSessioniLabel("❌ Errore calcolo sessioni", "#F44336");
            }
        } else {
            updateSessioniLabel("📊 Sessioni: Non calcolate", "#666666");
        }
    }

    private void updateSessioniLabel(String text, String color) {
        numeroSessioniLabel.setText(text);
        numeroSessioniLabel.setTextFill(Color.web(color));
    }

    private void validateDatesForSessions() {
        boolean dateValid = startDatePicker.getValue() != null && endDatePicker.getValue() != null;
        if (aggiungiSessioneBtn != null) {
            aggiungiSessioneBtn.setDisable(!dateValid);
            aggiungiSessioneBtn.setText(dateValid ? "+ Aggiungi Sessione" : "❌ Date Corso Richieste");
        }
    }

    // ==================== GESTIONE CHEF ====================

    private void updateChefDisplay() {
        updateListDisplay(listaChefContainer, chefSelezionati, 
                         "Nessun chef selezionato", this::createChefCard);
    }

    private HBox createChefCard(Chef chef) {
        Label nameLabel = createBoldLabel("👨‍🍳 " + chef.getNome() + " " + chef.getCognome(), 16, Color.BLACK);
        Label expLabel = createStandardLabel("📅 " + chef.getAnniEsperienza() + " anni di esperienza", 12, Color.GRAY);

        VBox infoBox = new VBox(3, nameLabel, expLabel);
        Button removeBtn = createRemoveButton(() -> {
            chefSelezionati.remove(chef);
            updateChefDisplay();
        });

        return createItemCard(infoBox, removeBtn, "#f0f8ff", "#87ceeb");
    }

    private void apriDialogSelezionaChef() {
        try {
            service.GestioneChef gestioneChef = new service.GestioneChef(
                new dao.ChefDAO(), 
                new dao.TieneDAO()
            );

            Chef scelto = new SelezionaChefDialog(gestioneChef).showAndReturn();

            if (scelto != null) {
                if (isChefGiaSelezionato(scelto)) {
                    StyleHelper.showInfoDialog("Chef già selezionato", "Questo chef è già stato selezionato.");
                } else {
                    chefSelezionati.add(scelto);
                    updateChefDisplay();
                    StyleHelper.showSuccessDialog("Chef aggiunto", "Chef aggiunto con successo!");
                }
            }
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore nella selezione chef: " + e.getMessage());
        }
    }

    private boolean isChefGiaSelezionato(Chef chef) {
        return chefSelezionati.stream()
                .anyMatch(c -> c.getUsername().equals(chef.getUsername()));
    }

    // ==================== GESTIONE SESSIONI ====================

    private void updateSessioniDisplay() {
        updateListDisplay(listaSessioniContainer, corsoSessioni, 
                         "Nessuna sessione aggiunta", 
                         (sessione, index) -> createSessioneCard(sessione, index));
    }

    private HBox createSessioneCard(Sessione sessione, int index) {
        String tipo = sessione instanceof Online ? "🌐 Online" : "🏢 In Presenza";
        String data = sessione.getDataInizioSessione() != null
                ? "📅 " + sessione.getDataInizioSessione().toLocalDate()
                : "Data non specificata";

        Label tipoLabel = createBoldLabel(tipo, 16, Color.BLACK);
        Label dataLabel = createStandardLabel(data, 13, Color.web("#333333"));

        VBox infoBox = new VBox(3, tipoLabel, dataLabel);

        String dettagli = getSessioneDettagli(sessione);
        if (!dettagli.isEmpty()) {
            infoBox.getChildren().add(createStandardLabel(dettagli, 12, Color.GRAY));
        }

        Button removeBtn = createRemoveButton(() -> {
            corsoSessioni.remove(index);
            updateSessioniDisplay();
        });

        return createItemCard(infoBox, removeBtn, "#f0fff0", "#90ee90");
    }

    private String getSessioneDettagli(Sessione sessione) {
        if (sessione instanceof Online) {
            return "💻 " + ((Online) sessione).getPiattaformaStreaming();
        } else if (sessione instanceof InPresenza) {
            InPresenza p = (InPresenza) sessione;
            return "📍 " + p.getVia() + ", " + p.getCitta();
        }
        return "";
    }

    private void aggiungiSessione() {
        if (!areDateValide()) {
            StyleHelper.showErrorDialog("Errore", "Inserire prima le date del corso");
            return;
        }

        try {
            Set<LocalDate> dateOccupate = getDateOccupate();
            Sessione nuova = new CreaSessioniGUI(
                startDatePicker.getValue(), 
                endDatePicker.getValue(), 
                dateOccupate
            ).showDialog();

            if (nuova != null) {
                corsoSessioni.add(nuova);
                updateSessioniDisplay();
                StyleHelper.showSuccessDialog("Successo", "Sessione creata con successo!");
            }
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore creazione sessione: " + e.getMessage());
        }
    }

    private boolean areDateValide() {
        return startDatePicker.getValue() != null && endDatePicker.getValue() != null;
    }

    private Set<LocalDate> getDateOccupate() {
        Set<LocalDate> date = new HashSet<>();
        corsoSessioni.forEach(s -> {
            if (s.getDataInizioSessione() != null) {
                date.add(s.getDataInizioSessione().toLocalDate());
            }
        });
        return date;
    }

    private void eliminaUltimaSessione() {
        if (!corsoSessioni.isEmpty()) {
            corsoSessioni.remove(corsoSessioni.size() - 1);
            updateSessioniDisplay();
        }
    }

    // ==================== SALVATAGGIO ====================

    private void salvaCorso() {
        if (!validateForm()) return;

        try {
            // Parsing dati
            String nome = nomeField.getText().trim();
            double prezzo = Double.parseDouble(prezzoField.getText());
            String argomento = argomentoField.getText().trim();
            Frequenza frequenza = frequenzaBox.getValue();
            int numeroPosti = Integer.parseInt(postiField.getText());

            LocalDateTime dataInizio = buildDateTime(startDatePicker, startHour, startMinute);
            LocalDateTime dataFine = buildDateTime(endDatePicker, endHour, endMinute);

            // TODO: Chiamata al controller per salvare il corso
            // gestioneController.creaCorso(nome, prezzo, argomento, frequenza, numeroPosti, 
            //                              dataInizio, dataFine, chefSelezionati, corsoSessioni);

            StyleHelper.showSuccessDialog("Successo", 
                "Corso salvato con successo!\n\n" +
                buildChefInfo() +
                "Sessioni create: " + corsoSessioni.size()
            );

            clearForm();

        } catch (NumberFormatException e) {
            StyleHelper.showErrorDialog("Errore", "Errore nel formato numerico: " + e.getMessage());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore nella creazione del corso: " + e.getMessage());
        }
    }

    private LocalDateTime buildDateTime(DatePicker datePicker, ComboBox<Integer> hour, ComboBox<Integer> minute) {
        return LocalDateTime.of(datePicker.getValue(), LocalTime.of(hour.getValue(), minute.getValue()));
    }

    private boolean validateForm() {
        return validateNonEmpty(nomeField, "nome del corso")
            && validateNumeric(prezzoField, "prezzo")
            && validateNumeric(postiField, "numero di posti")
            && validateDates()
            && validateFrequenza()
            && validateChefAndSessioni();
    }

    private boolean validateNonEmpty(TextField field, String fieldName) {
        if (field.getText().trim().isEmpty()) {
            StyleHelper.showValidationDialog("Validazione", "Il " + fieldName + " è obbligatorio");
            return false;
        }
        return true;
    }

    private boolean validateNumeric(TextField field, String fieldName) {
        if (field == prezzoField && !isValidDouble(field.getText())) {
            StyleHelper.showValidationDialog("Validazione", "Inserire un " + fieldName + " valido");
            return false;
        }
        if (field == postiField && !isValidInteger(field.getText())) {
            StyleHelper.showValidationDialog("Validazione", "Inserire un " + fieldName + " valido");
            return false;
        }
        return true;
    }

    private boolean validateDates() {
        if (!areDateValide()) {
            StyleHelper.showValidationDialog("Validazione", "Selezionare le date di inizio e fine");
            return false;
        }
        return true;
    }

    private boolean validateFrequenza() {
        if (frequenzaBox.getValue() == null) {
            StyleHelper.showValidationDialog("Validazione", "Selezionare una frequenza");
            return false;
        }

        if (!FrequenzaHelper.isFrequenzaValida(
                startDatePicker.getValue(), 
                endDatePicker.getValue(), 
                frequenzaBox.getValue())) {
            StyleHelper.showValidationDialog("Frequenza non valida", 
                FrequenzaHelper.getMessaggioErroreFrequenza(
                    startDatePicker.getValue(), 
                    endDatePicker.getValue(), 
                    frequenzaBox.getValue()
                ));
            return false;
        }

        return true;
    }

    private boolean validateChefAndSessioni() {
        if (chefSelezionati.isEmpty()) {
            StyleHelper.showValidationDialog("Validazione", "Selezionare almeno uno chef per il corso");
            return false;
        }

        if (corsoSessioni.isEmpty()) {
            StyleHelper.showValidationDialog("Validazione", "Aggiungere almeno una sessione al corso");
            return false;
        }

        return true;
    }

    private void clearForm() {
        nomeField.clear();
        prezzoField.clear();
        argomentoField.clear();
        postiField.clear();
        frequenzaBox.setValue(null);
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        endDatePicker.setDisable(false);
        chefSelezionati.clear();
        corsoSessioni.clear();
        updateChefDisplay();
        updateSessioniDisplay();
        validateDatesForSessions();
        updateSessioniLabel("📊 Sessioni: Non calcolate", "#666666");
    }

    // ==================== HELPER UI ====================

    private Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
        label.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));
        return label;
    }

    private Label createInfoLabel(String text, String color) {
        Label label = new Label(text);
        label.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
        label.setTextFill(Color.web(color));
        return label;
    }

    private Label createBoldLabel(String text, int fontSize, Color color) {
        Label label = new Label(text);
        label.setFont(Font.font("Roboto", FontWeight.BOLD, fontSize));
        label.setTextFill(color);
        return label;
    }

    private Label createStandardLabel(String text, int fontSize, Color color) {
        Label label = new Label(text);
        label.setFont(Font.font("Roboto", fontSize));
        label.setTextFill(color);
        return label;
    }

    private Label createEmptyLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.GRAY);
        return label;
    }

    private VBox createListContainer() {
        VBox container = new VBox(8);
        container.setPrefHeight(150);
        container.setStyle(
            "-fx-background-color: white; -fx-border-color: #e0e0e0; " +
            "-fx-border-radius: 8; -fx-padding: 10;"
        );
        return container;
    }

    private <T> void updateListDisplay(VBox container, ObservableList<T> items, 
                                       String emptyMessage, CardCreator<T> cardCreator) {
        container.getChildren().clear();
        
        if (items.isEmpty()) {
            container.getChildren().add(createEmptyLabel(emptyMessage));
        } else {
            for (int i = 0; i < items.size(); i++) {
                container.getChildren().add(cardCreator.create(items.get(i), i));
            }
        }
    }

    private HBox createItemCard(VBox infoBox, Button removeBtn, String bgColor, String borderColor) {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox card = new HBox(10, infoBox, spacer, removeBtn);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10));
        card.setStyle(
            "-fx-background-color: " + bgColor + "; " +
            "-fx-border-color: " + borderColor + "; " +
            "-fx-border-radius: 8; -fx-background-radius: 8;"
        );
        return card;
    }

    private Button createRemoveButton(Runnable action) {
        Button btn = new Button("✕");
        btn.setStyle(
            "-fx-background-color: #ff6b6b; -fx-text-fill: white; " +
            "-fx-background-radius: 15; -fx-min-width: 30; -fx-min-height: 30; " +
            "-fx-max-width: 30; -fx-max-height: 30; -fx-cursor: hand; -fx-font-weight: bold;"
        );
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private GridPane createGrid(double hgap, double vgap) {
        GridPane grid = new GridPane();
        grid.setHgap(hgap);
        grid.setVgap(vgap);
        return grid;
    }

    private void addGridRow(GridPane grid, int row, String label1, Control field1, 
                           String label2, Control field2) {
        grid.add(StyleHelper.createLabel(label1), 0, row);
        grid.add(field1, 1, row);
        grid.add(StyleHelper.createLabel(label2), 2, row);
        grid.add(field2, 3, row);
    }

    private HBox createTimeBox(ComboBox<Integer> hour, ComboBox<Integer> minute) {
        HBox box = new HBox(5, hour, new Label(":"), minute);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
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
        combo.setStyle(
            "-fx-background-color: white; -fx-background-radius: 8; " +
            "-fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-border-width: 1;"
        );
        return combo;
    }

    private boolean isValidDouble(String text) {
        try {
            Double.parseDouble(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidInteger(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String buildChefInfo() {
        StringBuilder sb = new StringBuilder("Chef selezionati:\n");
        chefSelezionati.forEach(chef -> 
            sb.append("• ").append(chef.getNome()).append(" ").append(chef.getCognome()).append("\n")
        );
        return sb.toString();
    }

    // ==================== FUNCTIONAL INTERFACE ====================

    @FunctionalInterface
    private interface CardCreator<T> {
        HBox create(T item, int index);
    }
}
