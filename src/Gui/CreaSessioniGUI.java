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

import controller.RicettaController;

import java.util.List;
import java.util.ArrayList;


public class CreaSessioniGUI extends Stage {

    private Sessione sessioneCreata = null;
    private LocalDate dataInizioCorso;
    private LocalDate dataFineCorso;
    private Set<LocalDate> dateOccupate;
    private RicettaController ricettaController;

    private DatePicker dataSessionePicker;
    private ComboBox<Integer> oraInizio;
    private ComboBox<Integer> minutoInizio;
    private ComboBox<Integer> oraFine;
    private ComboBox<Integer> minutoFine;
    private ComboBox<String> tipoCombo;

    private TextField piattaformaField;

    private TextField viaField;
    private TextField cittaField;
    private TextField postiField;
    private TextField capField;
    private VBox ricetteContainer;
    private Button aggiungiRicettaEsistenteBtn;
    private Button creaRicettaNuovaBtn;

    private VBox onlineBox;
    private VBox presenzaBox;
    private List<Ricetta> ricetteSelezionate = new ArrayList<>();

    public CreaSessioniGUI(LocalDate dataInizioCorso, LocalDate dataFineCorso, Set<LocalDate> dateOccupate, 
            RicettaController ricettaController) {
        this.dataInizioCorso = dataInizioCorso;
        this.dataFineCorso = dataFineCorso;
        this.dateOccupate = dateOccupate != null ? dateOccupate : new HashSet<>();
        this.ricettaController = ricettaController;
        initializeDialog();
    }

    public CreaSessioniGUI(LocalDate dataInizioCorso, LocalDate dataFineCorso, Set<LocalDate> dateOccupate) {
        this.dataInizioCorso = dataInizioCorso;
        this.dataFineCorso = dataFineCorso;
        this.dateOccupate = dateOccupate != null ? dateOccupate : new HashSet<>();
        this.ricettaController = null;
        initializeDialog();
    }


    private void initializeDialog() {
        setTitle("Crea Sessione");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(true);

        createLayout();
    }

    private void createLayout() {
        // ROOT con SFONDO ARANCIONE ridimensionabile
        StackPane rootPane = new StackPane();
        rootPane.setMinSize(700, 600);
        rootPane.setPrefSize(850, 750);

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
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox formCard = StyleHelper.createSection();
        formCard.setSpacing(20);

      
        VBox spiegazioneBox = createSpiegazioneCalendario();

        // Sezione data e orari
        VBox dateSection = createDateTimeSection();

      
        VBox tipoSection = createTipoSessioneSection();

     
        VBox campiSection = createCampiSection();

    
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

        Scene scene = new Scene(rootPane, 850, 750);
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
        VBox section = new VBox(15);

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label ricetteTitle = new Label("📖 Ricette del Corso");
        ricetteTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 15));
        ricetteTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        Label ricetteObblLabel = new Label("⚠️ OBBLIGATORIE per sessioni in presenza");
        ricetteObblLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 12));
        ricetteObblLabel.setTextFill(Color.web(StyleHelper.ERROR_RED));

        headerBox.getChildren().addAll(ricetteTitle, ricetteObblLabel);

   
        VBox opzioniBox = new VBox(10);

       
        Label descrizioneLabel = new Label("Scegli come gestire le ricette per questa sessione:");
        descrizioneLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.NORMAL, 14));

        // OPZIONE 1: Usa ricetta esistente
        HBox opzione1Box = new HBox(10);
        opzione1Box.setAlignment(Pos.CENTER_LEFT);
        opzione1Box.setPadding(new Insets(10));
        opzione1Box.setStyle("-fx-background-color: #e8f5e8; -fx-border-color: #4caf50; " +
            "-fx-border-radius: 8; -fx-background-radius: 8;");

        VBox opzione1Info = new VBox(5);
        Label opzione1Titolo = new Label("📚 OPZIONE 1: Usa Ricetta Esistente");
        opzione1Titolo.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 14));
        opzione1Titolo.setTextFill(Color.web(StyleHelper.SUCCESS_GREEN));

        Label opzione1Desc = new Label("Seleziona una ricetta già presente nel database");
        opzione1Desc.setFont(javafx.scene.text.Font.font("Roboto", 12));
        opzione1Desc.setTextFill(Color.GRAY);

        opzione1Info.getChildren().addAll(opzione1Titolo, opzione1Desc);

        aggiungiRicettaEsistenteBtn = StyleHelper.createSuccessButton("📚 Seleziona dal Database");
        aggiungiRicettaEsistenteBtn.setOnAction(e -> usaRicettaEsistente());

        opzione1Box.getChildren().addAll(opzione1Info, new Region(), aggiungiRicettaEsistenteBtn);
        HBox.setHgrow(opzione1Box.getChildren().get(1), Priority.ALWAYS);

        // OPZIONE 2: Crea nuova ricetta
        HBox opzione2Box = new HBox(10);
        opzione2Box.setAlignment(Pos.CENTER_LEFT);
        opzione2Box.setPadding(new Insets(10));
        opzione2Box.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #2196f3; " +
            "-fx-border-radius: 8; -fx-background-radius: 8;");

        VBox opzione2Info = new VBox(5);
        Label opzione2Titolo = new Label("✨ OPZIONE 2: Crea Nuova Ricetta");
        opzione2Titolo.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 14));
        opzione2Titolo.setTextFill(Color.web(StyleHelper.INFO_BLUE));

        Label opzione2Desc = new Label("Crea una ricetta da zero con ingredienti personalizzati");
        opzione2Desc.setFont(javafx.scene.text.Font.font("Roboto", 12));
        opzione2Desc.setTextFill(Color.GRAY);

        opzione2Info.getChildren().addAll(opzione2Titolo, opzione2Desc);

        creaRicettaNuovaBtn = StyleHelper.createInfoButton("✨ Crea da Zero");
        creaRicettaNuovaBtn.setOnAction(e -> creaNuovaRicetta());

        opzione2Box.getChildren().addAll(opzione2Info, new Region(), creaRicettaNuovaBtn);
        HBox.setHgrow(opzione2Box.getChildren().get(1), Priority.ALWAYS);

        opzioniBox.getChildren().addAll(descrizioneLabel, opzione1Box, opzione2Box);

        Label ricetteLabel = StyleHelper.createLabel("Ricette selezionate:");

        ricetteContainer = new VBox(5);
        ricetteContainer.setPrefHeight(150);
        ricetteContainer.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; " +
            "-fx-border-radius: 8; -fx-padding: 10; -fx-background-radius: 8;");

        updateRicetteDisplay();

        section.getChildren().addAll(headerBox, opzioniBox, ricetteLabel, ricetteContainer);
        return section;
    }

    private void updateRicetteDisplay() {
        ricetteContainer.getChildren().clear();

        if (ricetteSelezionate.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(20));

            Label emptyLabel = new Label("Nessuna ricetta selezionata");
            emptyLabel.setTextFill(Color.GRAY);
            emptyLabel.setFont(javafx.scene.text.Font.font("Roboto", 14));

            Label suggerimentoLabel = new Label("Utilizza una delle due opzioni sopra per aggiungere ricette");
            suggerimentoLabel.setTextFill(Color.GRAY);
            suggerimentoLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));

            emptyBox.getChildren().addAll(emptyLabel, suggerimentoLabel);
            ricetteContainer.getChildren().add(emptyBox);
        } else {
            for (int i = 0; i < ricetteSelezionate.size(); i++) {
                Ricetta ricetta = ricetteSelezionate.get(i);

                HBox ricettaBox = new HBox(10);
                ricettaBox.setAlignment(Pos.CENTER_LEFT);
                ricettaBox.setPadding(new Insets(10));
                ricettaBox.setStyle("-fx-background-color: #fff8dc; -fx-border-color: #deb887; " +
                    "-fx-border-radius: 5; -fx-background-radius: 5;");

                VBox infoBox = new VBox(3);

                Label nameLabel = new Label("📖 " + ricetta.getNome());
                nameLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 14));
                nameLabel.setTextFill(Color.BLACK);

                HBox detailsBox = new HBox(15);
                detailsBox.setAlignment(Pos.CENTER_LEFT);

                Label tempoLabel = new Label("⏱️ " + ricetta.getTempoPreparazione() + " min");
                tempoLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
                tempoLabel.setTextFill(Color.GRAY);

                Label ingredientiLabel = new Label("🥕 " + ricetta.getNumeroIngredienti() + " ingredienti");
                ingredientiLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
                ingredientiLabel.setTextFill(Color.web(StyleHelper.SUCCESS_GREEN));

                detailsBox.getChildren().addAll(tempoLabel, ingredientiLabel);

                infoBox.getChildren().addAll(nameLabel, detailsBox);

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

    private void usaRicettaEsistente() {
        if (ricettaController != null) {
            try {
                VisualizzaRicetteGUI visualizzaGUI = new VisualizzaRicetteGUI(ricettaController);
                visualizzaGUI.setSelectionMode(true);
                Ricetta ricettaScelta = visualizzaGUI.showAndReturn();

                if (ricettaScelta != null && !ricetteSelezionate.contains(ricettaScelta)) {
                    ricetteSelezionate.add(ricettaScelta);
                    updateRicetteDisplay();
                    showAlert("Successo", "Ricetta '" + ricettaScelta.getNome() + "' aggiunta dalla libreria!");
                } else if (ricettaScelta != null && ricetteSelezionate.contains(ricettaScelta)) {
                    showAlert("Attenzione", "Ricetta già selezionata per questa sessione");
                }
            } catch (Exception e) {
                showAlert("Errore", "Errore nel caricamento ricette: " + e.getMessage());
            }
        } else {
            showAlert("Servizio non disponibile", "Impossibile caricare ricette dal database.");
        }
    }


    private void creaNuovaRicetta() {
    try {
        CreaRicettaGUI creaGUI = new CreaRicettaGUI(ricettaController);
        Ricetta nuovaRicetta = creaGUI.showAndReturn();

        if (nuovaRicetta != null) {
            ricetteSelezionate.add(nuovaRicetta);
            updateRicetteDisplay();
            showAlert("Successo", 
                "Ricetta '" + nuovaRicetta.getNome() + "' creata e aggiunta!\n" +
                "Ingredienti: " + nuovaRicetta.getNumeroIngredienti() + "\n" +
                "Tempo: " + nuovaRicetta.getTempoPreparazione() + " minuti");
        }
    } catch (Exception e) {
        showAlert("Errore", "Errore nella creazione ricetta: " + e.getMessage());
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
                    "Scegli una delle due opzioni:\n" +
                    "• 📚 Usa una ricetta esistente dal database\n" +
                    "• ✨ Crea una nuova ricetta con ingredienti personalizzati");
                return false;
            }
        }

        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.contains("Successo") ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
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
