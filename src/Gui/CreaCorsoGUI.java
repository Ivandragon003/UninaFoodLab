package Gui;

import controller.GestioneCorsoController;
import controller.ChefController;
import controller.RicettaController;
import exceptions.ValidationException;
import guihelper.StyleHelper;
import util.FrequenzaHelper;
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
import javafx.util.StringConverter;
import model.*;

import java.time.LocalDate;
import java.util.*;

public class CreaCorsoGUI {

    // ========== COSTANTI ==========
    private static final int DEFAULT_START_HOUR = 9;
    private static final int DEFAULT_START_MINUTE = 0;
    private static final int DEFAULT_END_HOUR = 17;
    private static final int DEFAULT_END_MINUTE = 0;

    // ========== CONTROLLER ==========
    private final GestioneCorsoController corsoController;
    private final ChefController chefController;
    private final RicettaController ricettaController;

    // ========== ROOT ==========
    private VBox root;

    // ========== CAMPI FORM ==========
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

    // ========== LISTE ==========
    private VBox listaChefContainer;
    private VBox listaSessioniContainer;
    private Label numeroSessioniLabel;

    // ========== OBSERVABLE LISTS ==========
    private final ObservableList<Chef> chefSelezionati = FXCollections.observableArrayList();
    private final ObservableList<Sessione> corsoSessioni = FXCollections.observableArrayList();

    // ========== BOTTONI ==========
    private Button aggiungiSessioneBtn;

    // ========== COSTRUTTORE ==========
    public CreaCorsoGUI(GestioneCorsoController corsoController, 
                        ChefController chefController,
                        RicettaController ricettaController) {
        this.corsoController = corsoController;
        this.chefController = chefController;
        this.ricettaController = ricettaController;
    }

    // ========== GET ROOT ==========
    public VBox getRoot() {
        if (root == null) {
            root = createMainLayout();
        }
        return root;
    }

    // ========== MAIN LAYOUT ==========
    private VBox createMainLayout() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        StyleHelper.applyBackgroundGradient(container);

        Label titleLabel = StyleHelper.createTitleLabel("✨ Crea Nuovo Corso di Cucina");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setAlignment(Pos.CENTER);

        ScrollPane scrollPane = createScrollPane();
        
        container.getChildren().addAll(titleLabel, scrollPane);
        return container;
    }

    // ========== SCROLL PANE ==========
    private ScrollPane createScrollPane() {
        VBox content = new VBox(15);
        content.getChildren().addAll(
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

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scrollPane;
    }

    // ========== SEZIONE INFO ==========
    private VBox createInfoSection() {
        VBox section = StyleHelper.createSection();

        // Creazione campi
        nomeField = StyleHelper.createTextField("Es. Corso Base di Pasta Italiana");
        prezzoField = StyleHelper.createTextField("Es. 150.00");
        argomentoField = StyleHelper.createTextField("Es. Pasta fresca e condimenti");
        postiField = StyleHelper.createTextField("Es. 12");

        // Validazione solo numeri per posti
        postiField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                postiField.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        // CORREZIONE: Frequenza ora precaricata con tutte le opzioni
        frequenzaBox = createFrequenzaComboBox();
        frequenzaBox.getItems().setAll(Frequenza.values());
        frequenzaBox.setOnAction(e -> aggiornaNumeroSessioni());

        // Grid layout
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

        section.getChildren().addAll(
            createSectionTitle("📋 Informazioni Corso"), 
            grid
        );
        return section;
    }

    // ========== SEZIONE DATE E ORARI ==========
    private VBox createDateTimeSection() {
        VBox section = StyleHelper.createSection();

        // Date pickers
        startDatePicker = StyleHelper.createDatePicker();
        endDatePicker = StyleHelper.createDatePicker();
        startDatePicker.setPromptText("Data inizio");
        endDatePicker.setPromptText("Data fine");

        // Listener per aggiornamento frequenze
        startDatePicker.setOnAction(e -> aggiornaFrequenzeDisponibili());
        endDatePicker.setOnAction(e -> aggiornaFrequenzeDisponibili());

        // Time pickers
        startHour = createTimeComboBox(24, DEFAULT_START_HOUR);
        startMinute = createTimeComboBox(60, DEFAULT_START_MINUTE, 15);
        endHour = createTimeComboBox(24, DEFAULT_END_HOUR);
        endMinute = createTimeComboBox(60, DEFAULT_END_MINUTE, 15);

        // Grid layout
        GridPane grid = createGrid(15, 15);
        grid.add(StyleHelper.createLabel("Data Inizio:"), 0, 0);
        grid.add(startDatePicker, 1, 0);
        grid.add(StyleHelper.createLabel("Ora Inizio:"), 2, 0);
        grid.add(createTimeBox(startHour, startMinute), 3, 0);
        grid.add(StyleHelper.createLabel("Data Fine:"), 0, 1);
        grid.add(endDatePicker, 1, 1);
        grid.add(StyleHelper.createLabel("Ora Fine:"), 2, 1);
        grid.add(createTimeBox(endHour, endMinute), 3, 1);

        // Label contatore sessioni
        numeroSessioniLabel = createInfoLabel(
            "📊 Sessioni: Seleziona date e frequenza", 
            "#e74c3c"
        );

        // Listener per aggiornamento contatore
        frequenzaBox.setOnAction(e -> aggiornaNumeroSessioni());

        section.getChildren().addAll(
            createSectionTitle("📅 Date e Orari - OBBLIGATORIO"), 
            grid, 
            numeroSessioniLabel
        );

        return section;
    }

    // ========== SEZIONE CHEF ==========
    private VBox createChefSection() {
        VBox section = StyleHelper.createSection();

        Button selezionaChefBtn = StyleHelper.createPrimaryButton("+ Seleziona Chef");
        selezionaChefBtn.setOnAction(e -> apriDialogSelezionaChef());

        listaChefContainer = createListContainer();
        
        // Listener per aggiornamento display
        chefSelezionati.addListener((ListChangeListener<Chef>) c -> updateChefDisplay());
        updateChefDisplay();

        section.getChildren().addAll(
            createSectionTitle("👨‍🍳 Selezione Chef"), 
            selezionaChefBtn,
            StyleHelper.createLabel("Chef Selezionati:"), 
            listaChefContainer
        );

        return section;
    }

    // ========== SEZIONE SESSIONI ==========
    private VBox createSessionSection() {
        VBox section = StyleHelper.createSection();

        aggiungiSessioneBtn = StyleHelper.createSuccessButton("+ Aggiungi Sessione");
        aggiungiSessioneBtn.setOnAction(e -> aggiungiSessione());
        
        listaSessioniContainer = createListContainer();
        
        // Listener per aggiornamento display
        corsoSessioni.addListener((ListChangeListener<Sessione>) c -> updateSessioniDisplay());
        updateSessioniDisplay();

        section.getChildren().addAll(
            createSectionTitle("🎯 Sessioni del Corso"), 
            aggiungiSessioneBtn,
            StyleHelper.createLabel("Sessioni aggiunte:"), 
            listaSessioniContainer
        );

        return section;
    }

    // ========== SEZIONE BOTTONI ==========
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

    // ========== HELPERS UI ==========
    private ComboBox<Frequenza> createFrequenzaComboBox() {
        ComboBox<Frequenza> combo = StyleHelper.createComboBox();
        combo.setPromptText("Seleziona frequenza");
        
        // CORREZIONE: StringConverter per visualizzare correttamente l'enum
        combo.setConverter(new StringConverter<Frequenza>() {
            @Override
            public String toString(Frequenza frequenza) {
                return frequenza != null ? frequenza.getDescrizione() : "";
            }

            @Override
            public Frequenza fromString(String string) {
                return combo.getItems().stream()
                    .filter(f -> f.getDescrizione().equals(string))
                    .findFirst()
                    .orElse(null);
            }
        });
        
        return combo;
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
        ComboBox<Integer> combo = StyleHelper.createComboBox();
        for (int i = 0; i < max; i += step) {
            combo.getItems().add(i);
        }
        combo.setValue(defaultVal);
        return combo;
    }

    private VBox createListContainer() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        // CORREZIONE: Sfondo più scuro per miglior contrasto
        box.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 8;");
        return box;
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

    private Label createEmptyLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web("#d32f2f"));
        label.setFont(Font.font(13));
        label.setStyle("-fx-font-weight: bold;");
        return label;
    }

    private GridPane createGrid(int hgap, int vgap) {
        GridPane grid = new GridPane();
        grid.setHgap(hgap);
        grid.setVgap(vgap);
        grid.setAlignment(Pos.CENTER_LEFT);
        return grid;
    }

    // ========== LOGICA FREQUENZE ==========
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

        if (startDatePicker.getValue() != null && 
            endDatePicker.getValue() != null && 
            frequenzaBox.getValue() != null) {
            
            try {
                int sessioniPreviste = FrequenzaHelper.calcolaNumeroSessioni(
                    startDatePicker.getValue(), 
                    endDatePicker.getValue(), 
                    frequenzaBox.getValue()
                );
                
                double percentuale = (sessioniPreviste > 0) 
                    ? (double) numSessioniAggiunte / sessioniPreviste * 100 
                    : 0;

                aggiornaLabelSessioni(numSessioniAggiunte, sessioniPreviste, percentuale);

            } catch (IllegalArgumentException e) {
                numeroSessioniLabel.setText(String.format(
                    "📊 Sessioni: %d aggiunte (⚠️ Date non valide)", 
                    numSessioniAggiunte
                ));
                numeroSessioniLabel.setStyle(
                    "-fx-text-fill: #6c757d; -fx-font-size: 13px;"
                );
            }

        } else {
            aggiornaLabelSessioniSenzaDate(numSessioniAggiunte);
        }
    }

    private void aggiornaLabelSessioni(int aggiunte, int previste, double percentuale) {
        if (aggiunte == 0) {
            numeroSessioniLabel.setText(String.format(
                "📊 Sessioni: 0/%d previste (0%%)", 
                previste
            ));
            numeroSessioniLabel.setStyle(
                "-fx-text-fill: #e74c3c; -fx-font-size: 13px; -fx-font-weight: bold;"
            );
        } else if (aggiunte < previste) {
            numeroSessioniLabel.setText(String.format(
                "📊 Sessioni: %d/%d previste (%.0f%%) ⚠️", 
                aggiunte, previste, percentuale
            ));
            numeroSessioniLabel.setStyle(
                "-fx-text-fill: #f39c12; -fx-font-size: 13px; -fx-font-weight: bold;"
            );
        } else if (aggiunte == previste) {
            numeroSessioniLabel.setText(String.format(
                "📊 Sessioni: %d/%d completate ✅", 
                aggiunte, previste
            ));
            numeroSessioniLabel.setStyle(
                "-fx-text-fill: #28a745; -fx-font-size: 13px; -fx-font-weight: bold;"
            );
        } else {
            numeroSessioniLabel.setText(String.format(
                "📊 Sessioni: %d aggiunte (previste: %d) ⚡", 
                aggiunte, previste
            ));
            numeroSessioniLabel.setStyle(
                "-fx-text-fill: #17a2b8; -fx-font-size: 13px; -fx-font-weight: bold;"
            );
        }
    }

    private void aggiornaLabelSessioniSenzaDate(int aggiunte) {
        if (aggiunte == 0) {
            numeroSessioniLabel.setText(
                "📊 Sessioni: Non aggiunte (seleziona date e frequenza)"
            );
            numeroSessioniLabel.setStyle(
                "-fx-text-fill: #e74c3c; -fx-font-size: 13px;"
            );
        } else {
            numeroSessioniLabel.setText(String.format(
                "📊 Sessioni: %d aggiunte (seleziona date e frequenza per calcolo)", 
                aggiunte
            ));
            numeroSessioniLabel.setStyle(
                "-fx-text-fill: #f39c12; -fx-font-size: 13px;"
            );
        }
    }

    // ========== AZIONI ==========
    private void apriDialogSelezionaChef() {
        try {
            SelezionaChefDialog dialog = new SelezionaChefDialog(chefController);
            Chef scelto = dialog.showAndReturn();
            
            if (scelto != null) {
                // CORREZIONE: Verifica disponibilità già gestita nel dialog
                if (!chefSelezionati.contains(scelto)) {
                    chefSelezionati.add(scelto);
                } else {
                    StyleHelper.showValidationDialog(
                        "Chef già selezionato", 
                        "Questo chef è già stato aggiunto al corso"
                    );
                }
            }
        } catch (Exception e) {
            StyleHelper.showErrorDialog(
                "Errore", 
                "Errore durante la selezione chef: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    private void aggiungiSessione() {
        try {
            LocalDate inizio = startDatePicker.getValue();
            LocalDate fine = endDatePicker.getValue();
            Frequenza freq = frequenzaBox.getValue();

            // Validazioni
            if (inizio == null || fine == null) {
                throw new ValidationException(
                    "Seleziona le date di inizio e fine del corso prima di aggiungere sessioni ❌"
                );
            }

            if (freq == null) {
                throw new ValidationException(
                    "Seleziona la frequenza del corso prima di aggiungere sessioni ❌"
                );
            }

            if (!fine.isAfter(inizio)) {
                throw new ValidationException(
                    "La data di fine corso deve essere dopo quella di inizio ❌"
                );
            }

            // Raccogli date occupate
            Set<LocalDate> dateOccupate = new HashSet<>();
            for (Sessione s : corsoSessioni) {
                if (s.getDataInizioSessione() != null) {
                    dateOccupate.add(s.getDataInizioSessione().toLocalDate());
                }
            }

            // Apri dialog creazione sessione
            CreaSessioniGUI dialog = new CreaSessioniGUI(
                inizio, fine, freq, dateOccupate, ricettaController
            );
            Sessione nuovaSessione = dialog.showDialog();

            if (nuovaSessione != null) {
                corsoSessioni.add(nuovaSessione);
                StyleHelper.showSuccessDialog(
                    "Successo", 
                    "Sessione aggiunta alla lista del corso ✅"
                );
                aggiornaNumeroSessioni();
            }

        } catch (ValidationException ve) {
            StyleHelper.showValidationDialog("Errore", ve.getMessage());
        } catch (Exception e) {
            StyleHelper.showErrorDialog(
                "Errore", 
                "Errore durante l'aggiunta della sessione: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    private void salvaCorso() {
        try {
            // Validazioni base
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

            // Verifica completezza sessioni
            if (freq != null) {
                int sessioniPreviste = FrequenzaHelper.calcolaNumeroSessioni(inizio, fine, freq);
                int sessioniAggiunte = corsoSessioni.size();

                if (sessioniAggiunte < sessioniPreviste) {
                    final boolean[] conferma = {false};
                    StyleHelper.showConfirmationDialog(
                        "Attenzione",
                        String.format(
                            "Hai aggiunto solo %d sessioni su %d previste per la frequenza '%s'.\n\n" +
                            "Vuoi salvare il corso comunque?",
                            sessioniAggiunte, sessioniPreviste, freq.getDescrizione()
                        ),
                        () -> conferma[0] = true
                    );

                    if (!conferma[0]) return;
                }
            }

            // Salvataggio tramite ChefController
            chefController.saveCorsoFromForm(
                nomeField.getText(), 
                prezzoField.getText(), 
                argomentoField.getText(),
                postiField.getText(), 
                freq, 
                inizio, 
                startHour.getValue(), 
                startMinute.getValue(), 
                fine,
                endHour.getValue(), 
                endMinute.getValue(), 
                new ArrayList<>(chefSelezionati),
                new ArrayList<>(corsoSessioni)
            );

            StyleHelper.showSuccessDialog("Successo", "Corso creato con successo ✅");
            clearForm();

        } catch (ValidationException ve) {
            StyleHelper.showValidationDialog("Errore Validazione", ve.getMessage());
        } catch (Exception e) {
            StyleHelper.showErrorDialog(
                "Errore", 
                "Errore durante il salvataggio: " + e.getMessage()
            );
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
        startHour.setValue(DEFAULT_START_HOUR);
        startMinute.setValue(DEFAULT_START_MINUTE);
        endHour.setValue(DEFAULT_END_HOUR);
        endMinute.setValue(DEFAULT_END_MINUTE);
        chefSelezionati.clear();
        corsoSessioni.clear();
        updateChefDisplay();
        updateSessioniDisplay();
        aggiornaNumeroSessioni();
    }

    // ========== DISPLAY UPDATES ==========
    private void updateChefDisplay() {
        listaChefContainer.getChildren().clear();
        
        if (chefSelezionati.isEmpty()) {
            listaChefContainer.getChildren().add(
                createEmptyLabel("Nessuno chef selezionato ❌")
            );
        } else {
            chefSelezionati.forEach(c -> {
                listaChefContainer.getChildren().add(
                    createChefBox(c)
                );
            });
        }
    }

    // CORREZIONE: Nuovo metodo per box chef con pulsante rimozione
    private HBox createChefBox(Chef chef) {
        HBox chefBox = new HBox(10);
        chefBox.setAlignment(Pos.CENTER_LEFT);
        chefBox.setPadding(new Insets(8));
        chefBox.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: #FF6600;" +
            "-fx-border-radius: 6;" +
            "-fx-border-width: 1.5;"
        );

        String disponibilita = Boolean.TRUE.equals(chef.getDisponibilita()) ? "✅" : "❌";
        Label chefLabel = new Label(String.format("%s %s %s", 
            disponibilita, chef.getNome(), chef.getCognome()));
        chefLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button rimuoviBtn = new Button("✖");
        rimuoviBtn.setStyle(
            "-fx-background-color: #e74c3c;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 15;" +
            "-fx-cursor: hand;" +
            "-fx-min-width: 25;" +
            "-fx-min-height: 25;" +
            "-fx-max-width: 25;" +
            "-fx-max-height: 25;" +
            "-fx-font-size: 11px;"
        );
        rimuoviBtn.setOnAction(e -> {
            chefSelezionati.remove(chef);
            updateChefDisplay();
        });

        chefBox.getChildren().addAll(chefLabel, spacer, rimuoviBtn);
        return chefBox;
    }

    private void updateSessioniDisplay() {
        listaSessioniContainer.getChildren().clear();
        
        if (corsoSessioni.isEmpty()) {
            listaSessioniContainer.getChildren().add(
                createEmptyLabel("Nessuna sessione aggiunta ❌")
            );
        } else {
            for (Sessione s : corsoSessioni) {
                listaSessioniContainer.getChildren().add(
                    createSessioneBox(s)
                );
            }
        }
        
        aggiornaNumeroSessioni();
    }

    private HBox createSessioneBox(Sessione s) {
        HBox sessioneBox = new HBox(10);
        sessioneBox.setAlignment(Pos.CENTER_LEFT);
        sessioneBox.setPadding(new Insets(8));
        sessioneBox.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: #28a745;" +
            "-fx-border-radius: 6;" +
            "-fx-border-width: 1.5;"
        );

        Label numeroLabel = new Label((corsoSessioni.indexOf(s) + 1) + ".");
        numeroLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF6600; -fx-font-size: 13px;");

        Label infoLabel = new Label(formatSessioneDettagliata(s));
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button rimuoviBtn = new Button("🗑️");
        rimuoviBtn.setStyle(
            "-fx-background-color: #dc3545;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 15;" +
            "-fx-cursor: hand;" +
            "-fx-min-width: 30;" +
            "-fx-min-height: 30;"
        );
        rimuoviBtn.setOnAction(e -> {
            corsoSessioni.remove(s);
            aggiornaNumeroSessioni();
        });

        sessioneBox.getChildren().addAll(numeroLabel, infoLabel, spacer, rimuoviBtn);
        return sessioneBox;
    }

    private String formatSessioneDettagliata(Sessione s) {
        String tipo = s instanceof Online ? "🌐 Online" : "🏢 In Presenza";
        String data = s.getDataInizioSessione() != null 
            ? s.getDataInizioSessione().toLocalDate().toString() 
            : "Data non specificata";
        String orario = s.getDataInizioSessione() != null && s.getDataFineSessione() != null
            ? s.getDataInizioSessione().toLocalTime() + " - " + s.getDataFineSessione().toLocalTime() 
            : "";

        if (s instanceof InPresenza ip) {
            int numRicette = ip.getRicette() != null ? ip.getRicette().size() : 0;
            return tipo + " | " + data + " " + orario + " | 🍽️ " + numRicette + " ricette";
        } else if (s instanceof Online on) {
            return tipo + " (" + on.getPiattaformaStreaming() + ") | " + data + " " + orario;
        }

        return tipo + " | " + data + " " + orario;
    }
}