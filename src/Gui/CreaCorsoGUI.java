package Gui;

import controller.GestioneCorsoController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import model.*;
import util.StyleHelper;
import util.FrequenzaHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CreaCorsoGUI {
    private GestioneCorsoController gestioneController;
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
    private VBox listaChefContainer;
    private VBox listaSessioniContainer;
    private Label numeroSessioniLabel;
    private ObservableList<Chef> chefSelezionati = FXCollections.observableArrayList();
    private ObservableList<Sessione> corsoSessioni = FXCollections.observableArrayList();
    private VBox root;
    private Button aggiungiSessioneBtn;

    public void setController(GestioneCorsoController gestioneController) {
        this.gestioneController = gestioneController;
    }

    public VBox getRoot() {
        if (root == null) {
            root = createMainLayout();
        }
        return root;
    }

    private VBox createMainLayout() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        StyleHelper.applyOrangeBackground(container);

        Label titleLabel = StyleHelper.createTitleLabel("✨ Crea Nuovo Corso di Cucina");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox contentBox = new VBox(15);
        contentBox.getChildren().addAll(
                createInfoSection(),
                new Separator(),
                createDateTimeSection(),
                new Separator(),
                createChefSection(),
                new Separator(),
                createSessionSection(),
                new Separator(),
                createButtonSection()
        );

        scrollPane.setContent(contentBox);
        container.getChildren().addAll(titleLabel, scrollPane);
        return container;
    }

    private VBox createInfoSection() {
        VBox section = StyleHelper.createSection();

        Label sectionTitle = new Label("📋 Informazioni Corso");
        sectionTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        nomeField = StyleHelper.createTextField("Es. Corso Base di Pasta Italiana");
        prezzoField = StyleHelper.createTextField("Es. 150.00");
        argomentoField = StyleHelper.createTextField("Es. Pasta fresca e condimenti");
        postiField = StyleHelper.createTextField("Es. 12");

        frequenzaBox = new ComboBox<>();
        frequenzaBox.setPromptText("Seleziona frequenza");
        frequenzaBox.setPrefHeight(35);
        frequenzaBox.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        
        frequenzaBox.setOnAction(e -> onFrequenzaChange());

        grid.add(StyleHelper.createLabel("Nome Corso:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(StyleHelper.createLabel("Prezzo (€):"), 2, 0);
        grid.add(prezzoField, 3, 0);

        grid.add(StyleHelper.createLabel("Argomento:"), 0, 1);
        grid.add(argomentoField, 1, 1);
        grid.add(StyleHelper.createLabel("Numero Posti:"), 2, 1);
        grid.add(postiField, 3, 1);

        grid.add(StyleHelper.createLabel("Frequenza:"), 0, 2);
        grid.add(frequenzaBox, 1, 2);

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    private VBox createDateTimeSection() {
        VBox section = StyleHelper.createSection();

        Label sectionTitle = new Label("📅 Date e Orari - OBBLIGATORIO");
        sectionTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        startDatePicker = StyleHelper.createDatePicker();
        startDatePicker.setPromptText("Data inizio");
        endDatePicker = StyleHelper.createDatePicker();
        endDatePicker.setPromptText("Data fine");

        startDatePicker.setOnAction(e -> {
            onDataInizioChange();
            validateDatesForSessions();
        });
        
        endDatePicker.setOnAction(e -> {
            aggiornaFrequenzeDisponibili();
            validateDatesForSessions();
        });

        startHour = createTimeComboBox(24, 9);
        startMinute = createTimeComboBox(60, 0, 15);
        endHour = createTimeComboBox(24, 17);
        endMinute = createTimeComboBox(60, 0, 15);

        HBox startTimeBox = new HBox(5, startHour, new Label(":"), startMinute);
        startTimeBox.setAlignment(Pos.CENTER_LEFT);
        HBox endTimeBox = new HBox(5, endHour, new Label(":"), endMinute);
        endTimeBox.setAlignment(Pos.CENTER_LEFT);

        grid.add(StyleHelper.createLabel("Data Inizio:"), 0, 0);
        grid.add(startDatePicker, 1, 0);
        grid.add(StyleHelper.createLabel("Ora Inizio:"), 2, 0);
        grid.add(startTimeBox, 3, 0);

        grid.add(StyleHelper.createLabel("Data Fine:"), 0, 1);
        grid.add(endDatePicker, 1, 1);
        grid.add(StyleHelper.createLabel("Ora Fine:"), 2, 1);
        grid.add(endTimeBox, 3, 1);

        numeroSessioniLabel = new Label("📊 Sessioni: Non calcolate");
        numeroSessioniLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 14));
        numeroSessioniLabel.setTextFill(Color.web("#666666"));
        numeroSessioniLabel.setPadding(new Insets(10, 0, 0, 0));

        Label avisoLabel = new Label("⚠️ Seleziona le date per vedere le frequenze disponibili");
        avisoLabel.setTextFill(Color.web(StyleHelper.ERROR_RED));
        avisoLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 12));

        section.getChildren().addAll(sectionTitle, grid, numeroSessioniLabel, avisoLabel);
        return section;
    }

    private void onDataInizioChange() {
        LocalDate inizio = startDatePicker.getValue();
        
        if (frequenzaBox.getValue() == Frequenza.unica && inizio != null) {
            endDatePicker.setValue(inizio);
        }
        
        aggiornaFrequenzeDisponibili();
    }

    private void onFrequenzaChange() {
        Frequenza selezionata = frequenzaBox.getValue();
        
        if (selezionata == Frequenza.unica) {
            if (startDatePicker.getValue() != null) {
                endDatePicker.setValue(startDatePicker.getValue());
            }
            endDatePicker.setDisable(true);
            endDatePicker.setStyle("-fx-opacity: 0.6;");
            
        } else {
            endDatePicker.setDisable(false);
            endDatePicker.setStyle("");
        }
        
        aggiornaNumeroSessioniCalcolato();
    }

    private void aggiornaFrequenzeDisponibili() {
        LocalDate inizio = startDatePicker.getValue();
        LocalDate fine = endDatePicker.getValue();
        
        if (inizio != null && fine != null) {
            java.util.List<Frequenza> disponibili = FrequenzaHelper.getFrequenzeDisponibili(inizio, fine);
            
            Frequenza attuale = frequenzaBox.getValue();
            frequenzaBox.getItems().setAll(disponibili);
            
            if (disponibili.contains(attuale)) {
                frequenzaBox.setValue(attuale);
            } else if (!disponibili.isEmpty()) {
                frequenzaBox.setValue(disponibili.get(0));
            }
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
                numeroSessioniLabel.setText(String.format("📊 Sessioni calcolate: %d", numSessioni));
                numeroSessioniLabel.setTextFill(Color.web("#4CAF50"));
            } catch (Exception e) {
                numeroSessioniLabel.setText("❌ Errore calcolo sessioni");
                numeroSessioniLabel.setTextFill(Color.web("#F44336"));
            }
        } else {
            numeroSessioniLabel.setText("📊 Sessioni: Non calcolate");
            numeroSessioniLabel.setTextFill(Color.web("#666666"));
        }
    }

    private VBox createChefSection() {
        VBox section = StyleHelper.createSection();

        Label sectionTitle = new Label("👨‍🍳 Selezione Chef");
        sectionTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        Button selezionaChefBtn = StyleHelper.createPrimaryButton("+ Seleziona Chef");
        selezionaChefBtn.setOnAction(e -> apriDialogSelezionaChef());

        Label selezionatiLabel = StyleHelper.createLabel("Chef Selezionati:");

        listaChefContainer = new VBox(8);
        listaChefContainer.setPrefHeight(150);
        listaChefContainer.setStyle(
                "-fx-background-color: white; -fx-border-color: #e0e0e0; " +
                        "-fx-border-radius: 8; -fx-padding: 10;");

        updateChefDisplay();

        section.getChildren().addAll(sectionTitle, selezionaChefBtn, selezionatiLabel, listaChefContainer);
        return section;
    }

    private void updateChefDisplay() {
        listaChefContainer.getChildren().clear();

        if (chefSelezionati.isEmpty()) {
            Label emptyLabel = new Label("Nessun chef selezionato");
            emptyLabel.setTextFill(Color.GRAY);
            listaChefContainer.getChildren().add(emptyLabel);
        } else {
            for (Chef chef : chefSelezionati) {
                HBox chefBox = new HBox(10);
                chefBox.setAlignment(Pos.CENTER_LEFT);
                chefBox.setPadding(new Insets(10));
                chefBox.setStyle("-fx-background-color: #f0f8ff; -fx-border-color: #87ceeb; " +
                        "-fx-border-radius: 8; -fx-background-radius: 8;");

                VBox infoBox = new VBox(3);

                Label nameLabel = new Label("👨‍🍳 " + chef.getNome() + " " + chef.getCognome());
                nameLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 16));
                nameLabel.setTextFill(Color.BLACK);

                Label expLabel = new Label("📅 " + chef.getAnniEsperienza() + " anni di esperienza");
                expLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
                expLabel.setTextFill(Color.GRAY);

                infoBox.getChildren().addAll(nameLabel, expLabel);

                Button removeBtn = new Button("✕");
                removeBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; " +
                        "-fx-background-radius: 15; -fx-min-width: 30; -fx-min-height: 30; " +
                        "-fx-max-width: 30; -fx-max-height: 30; -fx-cursor: hand; -fx-font-weight: bold;");
                removeBtn.setOnAction(e -> {
                    chefSelezionati.remove(chef);
                    updateChefDisplay();
                });

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                chefBox.getChildren().addAll(infoBox, spacer, removeBtn);
                listaChefContainer.getChildren().add(chefBox);
            }
        }
    }

    private VBox createSessionSection() {
        VBox section = StyleHelper.createSection();

        Label sectionTitle = new Label("🎯 Sessioni del Corso");
        sectionTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        HBox buttonBox = new HBox(10);
        aggiungiSessioneBtn = StyleHelper.createSuccessButton("+ Aggiungi Sessione");
        aggiungiSessioneBtn.setDisable(true);
        aggiungiSessioneBtn.setOnAction(e -> aggiungiSessione());

        Button eliminaSessioneBtn = StyleHelper.createDangerButton("🗑️ Rimuovi");
        eliminaSessioneBtn.setOnAction(e -> eliminaSessioneSelezionata());

        buttonBox.getChildren().addAll(aggiungiSessioneBtn, eliminaSessioneBtn);

        Label sessioniLabel = StyleHelper.createLabel("Sessioni aggiunte:");

        listaSessioniContainer = new VBox(8);
        listaSessioniContainer.setPrefHeight(150);
        listaSessioniContainer.setStyle(
                "-fx-background-color: white; -fx-border-color: #e0e0e0; " +
                        "-fx-border-radius: 8; -fx-padding: 10;");

        updateSessioniDisplay();

        section.getChildren().addAll(sectionTitle, buttonBox, sessioniLabel, listaSessioniContainer);
        return section;
    }

    private void updateSessioniDisplay() {
        listaSessioniContainer.getChildren().clear();

        if (corsoSessioni.isEmpty()) {
            Label emptyLabel = new Label("Nessuna sessione aggiunta");
            emptyLabel.setTextFill(Color.GRAY);
            listaSessioniContainer.getChildren().add(emptyLabel);
        } else {
            for (int i = 0; i < corsoSessioni.size(); i++) {
                Sessione sessione = corsoSessioni.get(i);
                HBox sessioneBox = new HBox(10);
                sessioneBox.setAlignment(Pos.CENTER_LEFT);
                sessioneBox.setPadding(new Insets(10));
                sessioneBox.setStyle("-fx-background-color: #f0fff0; -fx-border-color: #90ee90; " +
                        "-fx-border-radius: 8; -fx-background-radius: 8;");

                VBox infoBox = new VBox(3);

                String tipo = sessione instanceof Online ? "🌐 Online" : "🏢 In Presenza";
                Label tipoLabel = new Label(tipo);
                tipoLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 16));
                tipoLabel.setTextFill(Color.BLACK);

                String data = sessione.getDataInizioSessione() != null
                        ? "📅 " + sessione.getDataInizioSessione().toLocalDate().toString()
                        : "Data non specificata";
                Label dataLabel = new Label(data);
                dataLabel.setFont(javafx.scene.text.Font.font("Roboto", 13));
                dataLabel.setTextFill(Color.web("#333333"));

                String dettagli = "";
                if (sessione instanceof Online) {
                    Online online = (Online) sessione;
                    dettagli = "💻 " + online.getPiattaformaStreaming();
                } else if (sessione instanceof InPresenza) {
                    InPresenza presenza = (InPresenza) sessione;
                    dettagli = "📍 " + presenza.getVia() + ", " + presenza.getCitta();
                }

                if (!dettagli.isEmpty()) {
                    Label dettagliLabel = new Label(dettagli);
                    dettagliLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
                    dettagliLabel.setTextFill(Color.GRAY);
                    infoBox.getChildren().add(dettagliLabel);
                }

                infoBox.getChildren().addAll(tipoLabel, dataLabel);

                Button removeBtn = new Button("✕");
                removeBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; " +
                        "-fx-background-radius: 15; -fx-min-width: 30; -fx-min-height: 30; " +
                        "-fx-max-width: 30; -fx-max-height: 30; -fx-cursor: hand; -fx-font-weight: bold;");

                final int index = i;
                removeBtn.setOnAction(e -> {
                    corsoSessioni.remove(index);
                    updateSessioniDisplay();
                });

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                sessioneBox.getChildren().addAll(infoBox, spacer, removeBtn);
                listaSessioniContainer.getChildren().add(sessioneBox);
            }
        }
    }

    private HBox createButtonSection() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button resetBtn = new Button("🔄 Reset Form");
        resetBtn.setPrefWidth(150);
        resetBtn.setStyle("-fx-background-color: " + StyleHelper.NEUTRAL_GRAY + "; " +
                "-fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-font-weight: bold;");
        resetBtn.setOnAction(e -> clearForm());

        Button salvaBtn = StyleHelper.createPrimaryButton("💾 Salva Corso");
        salvaBtn.setPrefWidth(150);
        salvaBtn.setOnAction(e -> salvaCorso());

        buttonBox.getChildren().addAll(resetBtn, salvaBtn);
        return buttonBox;
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
        combo.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e0e0e0; " +
                "-fx-border-radius: 8; -fx-border-width: 1;");
        return combo;
    }

    private void validateDatesForSessions() {
        boolean dateValid = startDatePicker.getValue() != null && endDatePicker.getValue() != null;
        if (aggiungiSessioneBtn != null) {
            aggiungiSessioneBtn.setDisable(!dateValid);
            if (dateValid) {
                aggiungiSessioneBtn.setText("+ Aggiungi Sessione");
            } else {
                aggiungiSessioneBtn.setText("❌ Date Corso Richieste");
            }
        }
    }

    private void apriDialogSelezionaChef() {
        if (gestioneController == null) {
            StyleHelper.showErrorDialog("Errore", "Controller non inizializzato");
            return;
        }

        try {
            dao.ChefDAO chefDAO = new dao.ChefDAO();
            dao.TieneDAO tieneDAO = new dao.TieneDAO();
            service.GestioneChef gestioneChef = new service.GestioneChef(chefDAO, tieneDAO);

            SelezionaChefDialog dialog = new SelezionaChefDialog(gestioneChef);
            Chef scelto = dialog.showAndReturn();

            if (scelto != null) {
                boolean giaSelezionato = chefSelezionati.stream()
                        .anyMatch(chef -> chef.getUsername().equals(scelto.getUsername()));

                if (!giaSelezionato) {
                    chefSelezionati.add(scelto);
                    updateChefDisplay();
                    StyleHelper.showSuccessDialog("Chef aggiunto", "Chef aggiunto con successo!");
                } else {
                    StyleHelper.showInfoDialog("Chef già selezionato", "Questo chef è già stato selezionato.");
                }
            }
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore nella selezione chef: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void aggiungiSessione() {
        LocalDate dataInizio = startDatePicker.getValue();
        LocalDate dataFine = endDatePicker.getValue();

        if (dataInizio == null || dataFine == null) {
            StyleHelper.showErrorDialog("Errore", "Inserire prima le date del corso");
            return;
        }

        try {
            java.util.Set<LocalDate> dateOccupate = new java.util.HashSet<>();
            for (Sessione s : corsoSessioni) {
                if (s.getDataInizioSessione() != null) {
                    dateOccupate.add(s.getDataInizioSessione().toLocalDate());
                }
            }

            CreaSessioniGUI sessioneDialog = new CreaSessioniGUI(dataInizio, dataFine, dateOccupate);
            Sessione nuovaSessione = sessioneDialog.showDialog();

            if (nuovaSessione != null) {
                corsoSessioni.add(nuovaSessione);
                updateSessioniDisplay();
                StyleHelper.showSuccessDialog("Successo", "Sessione creata con successo!");
            }
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore durante la creazione della sessione: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void eliminaSessioneSelezionata() {
        if (!corsoSessioni.isEmpty()) {
            corsoSessioni.remove(corsoSessioni.size() - 1);
            updateSessioniDisplay();
        }
    }

    private void salvaCorso() {
        try {
            if (!validateForm()) return;

            String nome = nomeField.getText().trim();
            double prezzo = Double.parseDouble(prezzoField.getText());
            String argomento = argomentoField.getText().trim();
            Frequenza frequenza = frequenzaBox.getValue();  
            int numeroPosti = Integer.parseInt(postiField.getText());

            LocalDate startDate = startDatePicker.getValue();
            LocalTime startTime = LocalTime.of(startHour.getValue(), startMinute.getValue());
            LocalDateTime dataInizio = LocalDateTime.of(startDate, startTime);

            LocalDate endDate = endDatePicker.getValue();
            LocalTime endTime = LocalTime.of(endHour.getValue(), endMinute.getValue());
            LocalDateTime dataFine = LocalDateTime.of(endDate, endTime);

            StringBuilder chefInfo = new StringBuilder("Chef selezionati:\n");
            for (Chef chef : chefSelezionati) {
                chefInfo.append("• ").append(chef.getNome()).append(" ").append(chef.getCognome()).append("\n");
            }

            StyleHelper.showSuccessDialog("Successo", 
                    "Corso salvato con successo!\n\n" + chefInfo.toString() + 
                    "Sessioni create: " + corsoSessioni.size());

            clearForm();

        } catch (Exception ex) {
            StyleHelper.showErrorDialog("Errore", "Errore nella creazione del corso: " + ex.getMessage());
        }
    }

    private boolean validateForm() {
        if (nomeField.getText().trim().isEmpty()) {
            StyleHelper.showValidationDialog("Validazione", "Il nome del corso è obbligatorio");
            return false;
        }

        try {
            Double.parseDouble(prezzoField.getText());
        } catch (NumberFormatException e) {
            StyleHelper.showValidationDialog("Validazione", "Inserire un prezzo valido");
            return false;
        }

        try {
            Integer.parseInt(postiField.getText());
        } catch (NumberFormatException e) {
            StyleHelper.showValidationDialog("Validazione", "Inserire un numero di posti valido");
            return false;
        }

        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            StyleHelper.showValidationDialog("Validazione", "Selezionare le date di inizio e fine");
            return false;
        }

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
        numeroSessioniLabel.setText("📊 Sessioni: Non calcolate");
        numeroSessioniLabel.setTextFill(Color.web("#666666"));
    }
}
