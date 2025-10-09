package Gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.*;
import guihelper.StyleHelper;
import controller.RicettaController;
import controller.IngredienteController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class CreaSessioniGUI extends Stage {
    private Sessione sessioneCreata = null;
    private final LocalDate corsoInizio;
    private final LocalDate corsoFine;
    private final Frequenza frequenzaCorso;
    private Set<LocalDate> dateOccupate;
    private final RicettaController ricettaController;
    private final IngredienteController ingredienteController;
    
    private DatePicker datePicker;
    private ComboBox<Integer> oraInizioBox, minutiInizioBox, oraFineBox, minutiFineBox;
    private ComboBox<String> tipoCombo;
    private TextField piattaformaField;
    private TextField viaField, cittaField, postiField, capField;
    private Button selezionaRicetteBtn;
    private Label ricetteLabel;
    private VBox listaRicetteContainer;
    private List<Ricetta> ricetteSelezionate = new ArrayList<>();

    public CreaSessioniGUI(LocalDate corsoInizio, LocalDate corsoFine, Frequenza frequenzaCorso, 
                          Set<LocalDate> dateOccupate, RicettaController ricettaController) {
        this(corsoInizio, corsoFine, frequenzaCorso, dateOccupate, ricettaController, null);
    }

    public CreaSessioniGUI(LocalDate corsoInizio, LocalDate corsoFine, Frequenza frequenzaCorso, 
                          Set<LocalDate> dateOccupate, RicettaController ricettaController,
                          IngredienteController ingredienteController) {
        this.corsoInizio = corsoInizio;
        this.corsoFine = corsoFine;
        this.frequenzaCorso = frequenzaCorso;
        this.dateOccupate = dateOccupate != null ? dateOccupate : new HashSet<>();
        this.ricettaController = ricettaController;
        this.ingredienteController = ingredienteController;
        
        initializeDialog();
    }

    private void initializeDialog() {
        setTitle("Crea Sessione");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(true);
        createLayout();
    }

    private void createLayout() {
        StackPane root = new StackPane();
        root.setPrefSize(800, 700);
        
        Region bg = new Region();
        StyleHelper.applyBackgroundGradient(bg);
        
        VBox main = new VBox(20);
        main.setPadding(new Insets(30));
        main.setAlignment(Pos.TOP_CENTER);
        
        Label title = StyleHelper.createTitleLabel("🎯 Crea Nuova Sessione");
        title.setTextFill(Color.WHITE);
        
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent;");
        
        VBox form = StyleHelper.createSection();
        form.getChildren().addAll(
            createDateSection(),
            new Separator(),
            createTipoSection(),
            createCampiSection(),
            new Separator(),
            createRicetteSection(),
            new Separator(),
            createButtonSection()
        );
        
        scroll.setContent(form);
        main.getChildren().addAll(title, scroll);
        root.getChildren().addAll(bg, main);
        
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);
    }

    private VBox createDateSection() {
        VBox box = new VBox(15);
        
        Label lbl = StyleHelper.createLabel("📅 Data e Orari");
        lbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label frequenzaInfo = new Label("📊 Frequenza corso: " + frequenzaCorso.getDescrizione());
        frequenzaInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666; -fx-background-color: #f0f8ff; -fx-padding: 8; -fx-background-radius: 5;");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        
        datePicker = StyleHelper.createDatePicker();
        datePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                if (empty || d == null) return;
                
                if (d.isBefore(corsoInizio) || d.isAfter(corsoFine)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #c62828;");
                    setTooltip(new Tooltip("Fuori dal periodo del corso"));
                    return;
                }
                
                if (!isDataValidaPerFrequenza(d)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #c62828;");
                    setTooltip(new Tooltip("Data non compatibile con la frequenza " + frequenzaCorso.getDescrizione()));
                    return;
                }

                if (dateOccupate.contains(d)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #fff9c4; -fx-text-fill: #f57f17;");
                    setTooltip(new Tooltip("Data già occupata da altra sessione"));
                    return;
                }
                
                setStyle("-fx-background-color: #c8e6c9; -fx-text-fill: #2e7d32;");
                setTooltip(new Tooltip("Data disponibile"));
            }
        });
        
        oraInizioBox = createTimeBox(24, 9, 1);
        minutiInizioBox = createTimeBox(60, 0, 15);
        oraFineBox = createTimeBox(24, 17, 1);
        minutiFineBox = createTimeBox(60, 0, 15);
        
        grid.add(StyleHelper.createLabel("Data:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(StyleHelper.createLabel("Ora Inizio:"), 0, 1);
        grid.add(new HBox(5, oraInizioBox, new Label(":"), minutiInizioBox), 1, 1);
        grid.add(StyleHelper.createLabel("Ora Fine:"), 2, 1);
        grid.add(new HBox(5, oraFineBox, new Label(":"), minutiFineBox), 3, 1);
        
        box.getChildren().addAll(lbl, frequenzaInfo, grid);
        return box;
    }

    private boolean isDataValidaPerFrequenza(LocalDate data) {
        if (frequenzaCorso == Frequenza.unica) {
            return dateOccupate.isEmpty();
        }

        switch (frequenzaCorso) {
            case giornaliero:
                return !dateOccupate.contains(data);

            case ogniDueGiorni:
                LocalDate riferimento = dateOccupate.stream().min(LocalDate::compareTo).orElse(corsoInizio);
                long giorniDifferenza = java.time.temporal.ChronoUnit.DAYS.between(riferimento, data);
                return giorniDifferenza >= 2 && giorniDifferenza % 2 == 0;

            case settimanale:
                int settimanaData = data.get(WeekFields.ISO.weekOfWeekBasedYear());
                int annoData = data.getYear();
                for (LocalDate d : dateOccupate) {
                    if (d.getYear() == annoData && d.get(WeekFields.ISO.weekOfWeekBasedYear()) == settimanaData) {
                        return false;
                    }
                }
                return true;

            case mensile:
                int meseData = data.getMonthValue();
                annoData = data.getYear();
                for (LocalDate d : dateOccupate) {
                    if (d.getYear() == annoData && d.getMonthValue() == meseData) {
                        return false;
                    }
                }
                return true;

            default:
                return true;
        }
    }

    private ComboBox<Integer> createTimeBox(int max, int def, int step) {
        ComboBox<Integer> cb = StyleHelper.createComboBox();
        for (int i = 0; i < max; i += step) {
            cb.getItems().add(i);
        }
        cb.setValue(def);
        return cb;
    }

    private VBox createTipoSection() {
        VBox box = new VBox(10);
        
        Label lbl = StyleHelper.createLabel("🎯 Tipo Sessione");
        lbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        tipoCombo = StyleHelper.createComboBox();
        tipoCombo.getItems().addAll("Online", "In Presenza");
        tipoCombo.setValue("Online");
        tipoCombo.setOnAction(e -> updateVisibility());
        
        box.getChildren().addAll(lbl, tipoCombo);
        return box;
    }

    private VBox createCampiSection() {
        VBox box = new VBox(15);
        
        VBox onlineBox = new VBox(10);
        onlineBox.setId("onlineFields");
        
        Label onlineLabel = StyleHelper.createLabel("🌐 Dettagli Online");
        piattaformaField = StyleHelper.createTextField("Es. Zoom, Teams, Google Meet");
        onlineBox.getChildren().addAll(onlineLabel, piattaformaField);
        
        VBox presenzaBox = new VBox(10);
        presenzaBox.setId("presenzaFields");
        
        Label presenzaLabel = StyleHelper.createLabel("🏢 Dettagli In Presenza");
        
        viaField = StyleHelper.createTextField("Via e civico");
        cittaField = StyleHelper.createTextField("Città");
        postiField = StyleHelper.createTextField("Numero posti");
        capField = StyleHelper.createTextField("CAP");
        
        presenzaBox.getChildren().addAll(presenzaLabel, viaField, cittaField, postiField, capField);
        presenzaBox.setVisible(false);
        presenzaBox.setManaged(false);
        
        box.getChildren().addAll(onlineBox, presenzaBox);
        return box;
    }

    private VBox createRicetteSection() {
        VBox box = new VBox(10);
        box.setId("ricetteSection");
        
        Label ricetteTitle = StyleHelper.createLabel("🍽️ Ricette (Solo per sessioni in presenza)");
        ricetteTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        selezionaRicetteBtn = StyleHelper.createPrimaryButton("📚 Seleziona Ricette");
        selezionaRicetteBtn.setOnAction(e -> apriDialogRicette());
        selezionaRicetteBtn.setDisable(true);
        
        ricetteLabel = StyleHelper.createLabel("⚠️ Nessuna ricetta selezionata");
        ricetteLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        Label infoLabel = StyleHelper.createLabel("💡 Le sessioni in presenza richiedono almeno una ricetta");
        infoLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px; -fx-background-color: #f8f9fa; -fx-padding: 8; -fx-background-radius: 5;");
        
        box.getChildren().addAll(ricetteTitle, selezionaRicetteBtn, ricetteLabel, infoLabel);
        box.setVisible(false);
        box.setManaged(false);
        
        return box;
    }

    private void apriDialogRicette() {
        try {
            // CORREZIONE: Passa anche IngredienteController per permettere la creazione di ricette
            VisualizzaRicetteDialog dialog = new VisualizzaRicetteDialog(
                ricettaController, 
                ingredienteController
            );
            List<Ricetta> ricetteScelte = dialog.showAndReturn();
            
            if (ricetteScelte != null && !ricetteScelte.isEmpty()) {
                ricetteSelezionate.clear();
                ricetteSelezionate.addAll(ricetteScelte);
                
                ricetteLabel.setText("✅ " + ricetteSelezionate.size() + " ricette selezionate");
                ricetteLabel.setStyle("-fx-text-fill: #28a745; -fx-font-size: 12px; -fx-font-weight: bold;");
                
                StyleHelper.showSuccessDialog("Successo", 
                    String.format(
                        "Selezionate %d ricette per la sessione!\n\n" +
                        "Le ricette saranno associate alla sessione in presenza.",
                        ricetteSelezionate.size()
                    )
                );
            }
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore nell'apertura dialog ricette: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private HBox createButtonSection() {
        HBox hb = new HBox(15);
        hb.setAlignment(Pos.CENTER);
        
        Button annullaBtn = StyleHelper.createDangerButton("❌ Annulla");
        annullaBtn.setOnAction(e -> {
            sessioneCreata = null;
            close();
        });
        
        Button salvaBtn = StyleHelper.createPrimaryButton("💾 Salva Sessione");
        salvaBtn.setOnAction(e -> salvaSessione());
        
        hb.getChildren().addAll(annullaBtn, salvaBtn);
        return hb;
    }

    private void updateVisibility() {
        boolean isOnline = "Online".equals(tipoCombo.getValue());
        
        VBox onlineBox = (VBox) getScene().getRoot().lookup("#onlineFields");
        VBox presenzaBox = (VBox) getScene().getRoot().lookup("#presenzaFields");
        VBox ricetteBox = (VBox) getScene().getRoot().lookup("#ricetteSection");
        
        if (onlineBox != null && presenzaBox != null && ricetteBox != null) {
            onlineBox.setVisible(isOnline);
            onlineBox.setManaged(isOnline);
            presenzaBox.setVisible(!isOnline);
            presenzaBox.setManaged(!isOnline);
            
            ricetteBox.setVisible(!isOnline);
            ricetteBox.setManaged(!isOnline);
            selezionaRicetteBtn.setDisable(isOnline);
            
            if (isOnline) {
                ricetteSelezionate.clear();
                ricetteLabel.setText("⚠️ Non applicabile per sessioni online");
                ricetteLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");
            } else {
                ricetteLabel.setText("⚠️ Nessuna ricetta selezionata");
                ricetteLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-font-weight: bold;");
            }
        }
    }

    private void salvaSessione() {
        try {
            if (datePicker.getValue() == null) {
                StyleHelper.showValidationDialog("Validazione", "Seleziona una data per la sessione");
                return;
            }

            LocalTime oraInizio = LocalTime.of(oraInizioBox.getValue(), minutiInizioBox.getValue());
            LocalTime oraFine = LocalTime.of(oraFineBox.getValue(), minutiFineBox.getValue());

            if (!oraFine.isAfter(oraInizio)) {
                StyleHelper.showValidationDialog("Validazione", "L'ora di fine deve essere dopo l'ora di inizio");
                return;
            }

            LocalDateTime dataInizio = LocalDateTime.of(datePicker.getValue(), oraInizio);
            LocalDateTime dataFine = LocalDateTime.of(datePicker.getValue(), oraFine);

            if ("Online".equals(tipoCombo.getValue())) {
                String piattaforma = piattaformaField.getText().trim();
                if (piattaforma.isEmpty()) {
                    StyleHelper.showValidationDialog("Validazione", "Inserisci la piattaforma di streaming");
                    return;
                }

                sessioneCreata = new Online(dataInizio, dataFine, piattaforma);

            } else {
                if (ricetteSelezionate.isEmpty()) {
                    StyleHelper.showValidationDialog("Validazione", 
                        "❌ Le sessioni in presenza richiedono almeno una ricetta.\n\n" +
                        "Usa il pulsante '📚 Seleziona Ricette' per aggiungerne una.");
                    return;
                }
                
                String via = viaField.getText().trim();
                String citta = cittaField.getText().trim();
                String postiStr = postiField.getText().trim();
                String capStr = capField.getText().trim();

                if (via.isEmpty() || citta.isEmpty() || postiStr.isEmpty() || capStr.isEmpty()) {
                    StyleHelper.showValidationDialog("Validazione", "Compila tutti i campi obbligatori per la sessione in presenza");
                    return;
                }

                int posti = Integer.parseInt(postiStr);
                int cap = Integer.parseInt(capStr);

                if (posti <= 0) {
                    StyleHelper.showValidationDialog("Validazione", "Il numero di posti deve essere maggiore di zero");
                    return;
                }

                if (cap < 10000 || cap > 99999) {
                    StyleHelper.showValidationDialog("Validazione", "CAP non valido (deve essere di 5 cifre)");
                    return;
                }

                InPresenza sessionePresenza = new InPresenza(dataInizio, dataFine, via, citta, posti, cap);
                
                for (Ricetta ricetta : ricetteSelezionate) {
                    sessionePresenza.getRicette().add(ricetta);
                }
                
                sessioneCreata = sessionePresenza;
            }

            close();

        } catch (NumberFormatException e) {
            StyleHelper.showValidationDialog("Validazione", "Formato numero non valido nei campi numerici");
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore durante la creazione della sessione: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Sessione showDialog() {
        showAndWait();
        return sessioneCreata;
    }
}