package Gui;

import controller.GestioneCorsoController;
import controller.ChefController;
import controller.RicettaController;
import exceptions.ValidationException;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import java.util.*;

public class CreaCorsoGUI {

    private final GestioneCorsoController corsoController;
    private final ChefController chefController;
    private final RicettaController ricettaController;

    private VBox root;

    private TextField nomeField, prezzoField, argomentoField, postiField;
    private ComboBox<Frequenza> frequenzaBox;
    private DatePicker startDatePicker, endDatePicker;
    private ComboBox<Integer> startHour, startMinute, endHour, endMinute;

    private VBox listaChefContainer, listaSessioniContainer;
    private Label numeroSessioniLabel;

    private final ObservableList<Chef> chefSelezionati = FXCollections.observableArrayList();
    private final ObservableList<Sessione> corsoSessioni = FXCollections.observableArrayList();

    private Button aggiungiSessioneBtn;

    public CreaCorsoGUI(GestioneCorsoController corsoController, ChefController chefController,
                         RicettaController ricettaController) {
        this.corsoController = corsoController;
        this.chefController = chefController;
        this.ricettaController = ricettaController;
    }

    public VBox getRoot() {
        if (root == null)
            root = createMainLayout();
        return root;
    }

    private VBox createMainLayout() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        StyleHelper.applyBackgroundGradient(container);

        Label titleLabel = StyleHelper.createTitleLabel("✨ Crea Nuovo Corso di Cucina");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setAlignment(Pos.CENTER);

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

        postiField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                postiField.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        frequenzaBox = createFrequenzaComboBox();

        GridPane grid = createGrid(15, 15);
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

        section.getChildren().addAll(createSectionTitle("📋 Informazioni Corso"), grid);
        return section;
    }

    private VBox createDateTimeSection() {
        VBox section = StyleHelper.createSection();

        startDatePicker = createDatePickerLocal();
        endDatePicker = createDatePickerLocal();
        startDatePicker.setPromptText("Data inizio");
        endDatePicker.setPromptText("Data fine");

        startDatePicker.setOnAction(e -> aggiornaFrequenzeDisponibili());
        endDatePicker.setOnAction(e -> aggiornaFrequenzeDisponibili());

        startHour = createTimeComboBox(24, 9);
        startMinute = createTimeComboBox(60, 0, 15);
        endHour = createTimeComboBox(24, 17);
        endMinute = createTimeComboBox(60, 0, 15);

        GridPane grid = createGrid(15, 15);
        grid.add(StyleHelper.createLabel("Data Inizio:"), 0, 0);
        grid.add(startDatePicker, 1, 0);
        grid.add(StyleHelper.createLabel("Ora Inizio:"), 2, 0);
        grid.add(createTimeBox(startHour, startMinute), 3, 0);
        grid.add(StyleHelper.createLabel("Data Fine:"), 0, 1);
        grid.add(endDatePicker, 1, 1);
        grid.add(StyleHelper.createLabel("Ora Fine:"), 2, 1);
        grid.add(createTimeBox(endHour, endMinute), 3, 1);

        numeroSessioniLabel = createInfoLabel("📊 Sessioni: Seleziona date e frequenza", "#e74c3c");

        frequenzaBox.setOnAction(e -> aggiornaNumeroSessioni());

        section.getChildren().addAll(createSectionTitle("📅 Date e Orari - OBBLIGATORIO"), grid, numeroSessioniLabel);

        return section;
    }

    private VBox createChefSection() {
        VBox section = StyleHelper.createSection();

        Button selezionaChefBtn = StyleHelper.createPrimaryButton("+ Seleziona Chef");
        selezionaChefBtn.setOnAction(e -> apriDialogSelezionaChef());

        listaChefContainer = createListContainer();
        chefSelezionati.addListener((ListChangeListener<Chef>) c -> updateChefDisplay());
        updateChefDisplay();

        section.getChildren().addAll(createSectionTitle("👨‍🍳 Selezione Chef"), selezionaChefBtn,
                StyleHelper.createLabel("Chef Selezionati:"), listaChefContainer);

        return section;
    }

    private VBox createSessionSection() {
        VBox section = StyleHelper.createSection();

        aggiungiSessioneBtn = StyleHelper.createSuccessButton("+ Aggiungi Sessione");
        aggiungiSessioneBtn.setOnAction(e -> aggiungiSessione());
        listaSessioniContainer = createListContainer();
        corsoSessioni.addListener((ListChangeListener<Sessione>) c -> updateSessioniDisplay());
        updateSessioniDisplay();

        section.getChildren().addAll(createSectionTitle("🎯 Sessioni del Corso"), aggiungiSessioneBtn,
                StyleHelper.createLabel("Sessioni aggiunte:"), listaSessioniContainer);

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

    // === HELPERS ===

    private ComboBox<Frequenza> createFrequenzaComboBox() {
        ComboBox<Frequenza> combo = new ComboBox<>();
        combo.setPromptText("Seleziona frequenza");
        combo.setPrefHeight(35);
        combo.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        return combo;
    }

    private DatePicker createDatePickerLocal() {
        DatePicker dp = new DatePicker();
        dp.setPrefHeight(35);
        dp.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        return dp;
    }

    private void aggiornaFrequenzeDisponibili() {
        LocalDate inizio = startDatePicker.getValue();
        LocalDate fine = endDatePicker.getValue();
        List<Frequenza> disponibili = FrequenzaHelper.getFrequenzeDisponibili(inizio, fine);

        Frequenza selezionataAttuale = frequenzaBox.getValue();
        frequenzaBox.getItems().setAll(disponibili);

        if (selezionataAttuale != null && disponibili.contains(selezionataAttuale)) {
            frequenzaBox.setValue(selezionataAttuale);
        } else if (!disponibili.isEmpty()) {
            frequenzaBox.setValue(disponibili.get(0));
        }

        aggiornaNumeroSessioni();
    }

    private void aggiornaNumeroSessioni() {
        if (numeroSessioniLabel == null) return;

        int numSessioniAggiunte = corsoSessioni.size();

        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null && frequenzaBox.getValue() != null) {
            try {
                int sessioniPreviste = FrequenzaHelper.calcolaNumeroSessioni(startDatePicker.getValue(), endDatePicker.getValue(), frequenzaBox.getValue());
                double percentuale = (sessioniPreviste > 0) ? (double) numSessioniAggiunte / sessioniPreviste * 100 : 0;

                if (numSessioniAggiunte == 0) {
                    numeroSessioniLabel.setText(String.format("📊 Sessioni: 0/%d previste (0%%)", sessioniPreviste));
                    numeroSessioniLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px; -fx-font-weight: bold;");
                } else if (numSessioniAggiunte < sessioniPreviste) {
                    numeroSessioniLabel.setText(String.format("📊 Sessioni: %d/%d previste (%.0f%%) ⚠️", numSessioniAggiunte, sessioniPreviste, percentuale));
                    numeroSessioniLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 13px; -fx-font-weight: bold;");
                } else if (numSessioniAggiunte == sessioniPreviste) {
                    numeroSessioniLabel.setText(String.format("📊 Sessioni: %d/%d completate ✅", numSessioniAggiunte, sessioniPreviste));
                    numeroSessioniLabel.setStyle("-fx-text-fill: #28a745; -fx-font-size: 13px; -fx-font-weight: bold;");
                } else {
                    numeroSessioniLabel.setText(String.format("📊 Sessioni: %d aggiunte (previste: %d) ⚡", numSessioniAggiunte, sessioniPreviste));
                    numeroSessioniLabel.setStyle("-fx-text-fill: #17a2b8; -fx-font-size: 13px; -fx-font-weight: bold;");
                }

            } catch (IllegalArgumentException e) {
                numeroSessioniLabel.setText(String.format("📊 Sessioni: %d aggiunte (⚠️ Date non valide)", numSessioniAggiunte));
                numeroSessioniLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 13px;");
            }

        } else {
            if (numSessioniAggiunte == 0) {
                numeroSessioniLabel.setText("📊 Sessioni: Non aggiunte (seleziona date e frequenza)");
                numeroSessioniLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
            } else {
                numeroSessioniLabel.setText(String.format("📊 Sessioni: %d aggiunte (seleziona date e frequenza per calcolo)", numSessioniAggiunte));
                numeroSessioniLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 13px;");
            }
        }
    }

    private void apriDialogSelezionaChef() {
        try {
            SelezionaChefDialog dialog = new SelezionaChefDialog(chefController);
            Chef scelto = dialog.showAndReturn();
            if (scelto != null) {
                if (!Boolean.TRUE.equals(scelto.getDisponibilita())) {
                    StyleHelper.showValidationDialog("Errore Selezione Chef", "Lo chef selezionato non è disponibile ❌");
                    return;
                }
                if (!chefSelezionati.contains(scelto)) {
                    chefSelezionati.add(scelto);
                }
            }
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore durante la selezione chef: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void aggiungiSessione() {
        try {
            LocalDate inizio = startDatePicker.getValue();
            LocalDate fine = endDatePicker.getValue();
            Frequenza freq = frequenzaBox.getValue();

            if (inizio == null || fine == null) {
                throw new ValidationException("Seleziona le date di inizio e fine del corso prima di aggiungere sessioni ❌");
            }

            if (freq == null) {
                throw new ValidationException("Seleziona la frequenza del corso prima di aggiungere sessioni ❌");
            }

            if (!fine.isAfter(inizio)) {
                throw new ValidationException("La data di fine corso deve essere dopo quella di inizio ❌");
            }

            Set<LocalDate> dateOccupate = new HashSet<>();
            for (Sessione s : corsoSessioni) {
                if (s.getDataInizioSessione() != null) {
                    dateOccupate.add(s.getDataInizioSessione().toLocalDate());
                }
            }

            CreaSessioniGUI dialog = new CreaSessioniGUI(inizio, fine, freq, dateOccupate, ricettaController);
            Sessione nuovaSessione = dialog.showDialog();

            if (nuovaSessione != null) {
                corsoSessioni.add(nuovaSessione);
                StyleHelper.showSuccessDialog("Successo", "Sessione aggiunta alla lista del corso ✅");
                aggiornaNumeroSessioni();
            }

        } catch (ValidationException ve) {
            StyleHelper.showValidationDialog("Errore", ve.getMessage());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore durante l'aggiunta della sessione: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void salvaCorso() {
        try {
            if (nomeField.getText().trim().isEmpty()) {
                throw new ValidationException("Inserisci il nome del corso ❌");
            }

            if (chefSelezionati.isEmpty()) {
                throw new ValidationException("Seleziona almeno uno chef per il corso ❌");
            }

            if (corsoSessioni.isEmpty()) {
                throw new ValidationException("Aggiungi almeno una sessione al corso ❌");
            }

            LocalDate inizio = startDatePicker.getValue();
            LocalDate fine = endDatePicker.getValue();
            Frequenza freq = frequenzaBox.getValue();

            if (inizio == null || fine == null) {
                throw new ValidationException("Seleziona le date di inizio e fine del corso ❌");
            }

            if (freq != null) {
                int sessioniPreviste = FrequenzaHelper.calcolaNumeroSessioni(inizio, fine, freq);
                int sessioniAggiunte = corsoSessioni.size();

                if (sessioniAggiunte < sessioniPreviste) {
                    final boolean[] conferma = {false};
                    StyleHelper.showConfirmationDialog("Attenzione",
                            String.format(
                                    "Hai aggiunto solo %d sessioni su %d previste per la frequenza '%s'.\n\nVuoi salvare il corso comunque?",
                                    sessioniAggiunte, sessioniPreviste, freq.getDescrizione()),
                            () -> conferma[0] = true
                    );

                    if (!conferma[0]) return;
                }
            }

            chefController.saveCorsoFromForm(nomeField.getText(), prezzoField.getText(), argomentoField.getText(),
                    postiField.getText(), freq, inizio, startHour.getValue(), startMinute.getValue(), fine,
                    endHour.getValue(), endMinute.getValue(), new ArrayList<>(chefSelezionati),
                    new ArrayList<>(corsoSessioni));

            StyleHelper.showSuccessDialog("Successo", "Corso creato con successo ✅");
            clearForm();

        } catch (ValidationException ve) {
            StyleHelper.showValidationDialog("Errore Validazione", ve.getMessage());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore durante il salvataggio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearForm() {
        nomeField.clear();
        prezzoField.clear();
        argomentoField.clear();
        postiField.clear();
        frequenzaBox.setValue(null);
        frequenzaBox.getItems().clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        startHour.setValue(9);
        startMinute.setValue(0);
        endHour.setValue(17);
        endMinute.setValue(0);
        chefSelezionati.clear();
        corsoSessioni.clear();
        updateChefDisplay();
        updateSessioniDisplay();
        aggiornaNumeroSessioni();
    }

    private void updateChefDisplay() {
        listaChefContainer.getChildren().clear();
        if (chefSelezionati.isEmpty()) {
            listaChefContainer.getChildren().add(createEmptyLabel("Nessuno chef selezionato ❌"));
        } else {
            chefSelezionati.forEach(c ->
                    listaChefContainer.getChildren().add(new Label(c.getNome() + " " + c.getCognome()))
            );
        }
    }

    private void updateSessioniDisplay() {
        listaSessioniContainer.getChildren().clear();
        if (corsoSessioni.isEmpty()) {
            listaSessioniContainer.getChildren().add(createEmptyLabel("Nessuna sessione aggiunta ❌"));
        } else {
            for (Sessione s : corsoSessioni) {
                HBox sessioneBox = new HBox(10);
                sessioneBox.setAlignment(Pos.CENTER_LEFT);
                sessioneBox.setPadding(new Insets(8));
                sessioneBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-border-color: #dee2e6; -fx-border-radius: 5;");

                Label numeroLabel = new Label((corsoSessioni.indexOf(s) + 1) + ".");
                numeroLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF6600;");

                Label infoLabel = new Label(formatSessioneDettagliata(s));
                infoLabel.setStyle("-fx-font-size: 12px;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button rimuoviBtn = new Button("🗑️");
                rimuoviBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand;");
                rimuoviBtn.setOnAction(e -> {
                    corsoSessioni.remove(s);
                    aggiornaNumeroSessioni();
                });

                sessioneBox.getChildren().addAll(numeroLabel, infoLabel, spacer, rimuoviBtn);
                listaSessioniContainer.getChildren().add(sessioneBox);
            }
        }
        aggiornaNumeroSessioni();
    }

    private String formatSessioneDettagliata(Sessione s) {
        String tipo = s instanceof Online ? "🌐 Online" : "🏢 In Presenza";
        String data = s.getDataInizioSessione() != null ? s.getDataInizioSessione().toLocalDate().toString() : "Data non specificata";
        String orario = s.getDataInizioSessione() != null && s.getDataFineSessione() != null
                ? s.getDataInizioSessione().toLocalTime() + " - " + s.getDataFineSessione().toLocalTime() : "";

        if (s instanceof InPresenza ip) {
            int numRicette = ip.getRicette() != null ? ip.getRicette().size() : 0;
            return tipo + " | " + data + " " + orario + " | 🍽️ " + numRicette + " ricette";
        } else if (s instanceof Online on) {
            return tipo + " (" + on.getPiattaformaStreaming() + ") | " + data + " " + orario;
        }

        return tipo + " | " + data + " " + orario;
    }

    private VBox createListContainer() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(5));
        box.setStyle("-fx-background-color: #fff3e0; -fx-background-radius: 8;");
        return box;
    }

    private HBox createTimeBox(ComboBox<Integer> hour, ComboBox<Integer> minute) {
        HBox box = new HBox(5, hour, new Label(":"), minute);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private ComboBox<Integer> createTimeComboBox(int max, int defaultVal) {
        return createTimeComboBox(max, defaultVal, 1);
    }

    private ComboBox<Integer> createTimeComboBox(int max, int defaultVal, int step) {
        ComboBox<Integer> combo = new ComboBox<>();
        for (int i = 0; i < max; i += step)
            combo.getItems().add(i);
        combo.setValue(defaultVal);
        combo.setPrefHeight(35);
        return combo;
    }

    private Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        label.setTextFill(Color.web("#FF6F00"));
        return label;
    }

    private Label createInfoLabel(String text, String color) {
        Label label = new Label(text);
        label.setTextFill(Color.web(color));
        label.setFont(Font.font(13));
        return label;
    }

    private GridPane createGrid(int hgap, int vgap) {
        GridPane grid = new GridPane();
        grid.setHgap(hgap);
        grid.setVgap(vgap);
        grid.setAlignment(Pos.CENTER_LEFT);
        return grid;
    }

    private Label createEmptyLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.RED);
        return label;
    }
}
