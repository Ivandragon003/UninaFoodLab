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
import util.StyleHelper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

public class CreaSessioniGUI extends Stage {
    private Sessione sessioneCreata = null;
    private final LocalDate corsoInizio;
    private final LocalDate corsoFine;
    private Set<LocalDate> dateOccupate;

    // UI components
    private DatePicker datePicker;
    private ComboBox<Integer> oraInizioBox, minutiInizioBox, oraFineBox, minutiFineBox;
    private ComboBox<String> tipoCombo;
    private TextField piattaformaField;
    private TextField viaField, cittaField, postiField, capField;

    // ✅ CORRETTO: Accetta LocalDate, non LocalDateTime
    public CreaSessioniGUI(LocalDate corsoInizio, LocalDate corsoFine, Set<LocalDate> dateOccupate) {
        this.corsoInizio = corsoInizio;
        this.corsoFine = corsoFine;
        this.dateOccupate = dateOccupate != null ? dateOccupate : new HashSet<>();
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
        root.setPrefSize(700, 600);
        
        Region bg = new Region();
        StyleHelper.applyBackgroundGradient(bg);

        VBox main = new VBox(20);
        main.setPadding(new Insets(30));
        main.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("🎯 Crea Nuova Sessione");
        title.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent;");

        VBox form = new VBox(20);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        form.getChildren().addAll(
            createDateSection(),
            new Separator(),
            createTipoSection(),
            createCampiSection(),
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
        Label lbl = new Label("📅 Data e Orari");
        lbl.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 18));
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        datePicker = new DatePicker();
        datePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                if (empty || d == null) return;
                
                if (d.isBefore(corsoInizio) || d.isAfter(corsoFine)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white;");
                } else if (dateOccupate.contains(d)) {
                    setStyle("-fx-background-color: #ffd93d; -fx-text-fill: #333;");
                }
            }
        });

        oraInizioBox = createTimeBox(24, 9, 1);
        minutiInizioBox = createTimeBox(60, 0, 15);
        oraFineBox = createTimeBox(24, 17, 1);
        minutiFineBox = createTimeBox(60, 0, 15);

        grid.add(new Label("Data:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("Ora Inizio:"), 0, 1);
        grid.add(new HBox(5, oraInizioBox, new Label(":"), minutiInizioBox), 1, 1);
        grid.add(new Label("Ora Fine:"), 2, 1);
        grid.add(new HBox(5, oraFineBox, new Label(":"), minutiFineBox), 3, 1);

        box.getChildren().addAll(lbl, grid);
        return box;
    }

    private ComboBox<Integer> createTimeBox(int max, int def, int step) {
        ComboBox<Integer> cb = new ComboBox<>();
        for (int i = 0; i < max; i += step) {
            cb.getItems().add(i);
        }
        cb.setValue(def);
        cb.setPrefHeight(30);
        return cb;
    }

    private VBox createTipoSection() {
        VBox box = new VBox(10);
        Label lbl = new Label("🎯 Tipo Sessione");
        lbl.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 16));
        
        tipoCombo = new ComboBox<>();
        tipoCombo.getItems().addAll("Online", "In Presenza");
        tipoCombo.setValue("Online");
        tipoCombo.setOnAction(e -> updateVisibility());
        
        box.getChildren().addAll(lbl, tipoCombo);
        return box;
    }

    private VBox createCampiSection() {
        VBox box = new VBox(15);

        // Online
        VBox onlineBox = new VBox(10);
        onlineBox.setId("onlineFields");
        Label onlineLabel = new Label("🌐 Dettagli Online");
        onlineLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 14));
        piattaformaField = StyleHelper.createTextField("Es. Zoom, Teams, Google Meet");
        onlineBox.getChildren().addAll(onlineLabel, piattaformaField);

        // Presenza
        VBox presenzaBox = new VBox(10);
        presenzaBox.setId("presenzaFields");
        Label presenzaLabel = new Label("🏢 Dettagli In Presenza");
        presenzaLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 14));
        
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

    private HBox createButtonSection() {
        HBox hb = new HBox(15);
        hb.setAlignment(Pos.CENTER);

        Button annullaBtn = StyleHelper.createDangerButton("❌ Annulla");
        annullaBtn.setOnAction(e -> {
            sessioneCreata = null;
            close();
        });

        Button salvaBtn = StyleHelper.createPrimaryButton("💾 Salva");
        salvaBtn.setOnAction(e -> salvaSessione());

        hb.getChildren().addAll(annullaBtn, salvaBtn);
        return hb;
    }

    private void updateVisibility() {
        boolean isOnline = "Online".equals(tipoCombo.getValue());
        
        VBox onlineBox = (VBox) getScene().getRoot().lookup("#onlineFields");
        VBox presenzaBox = (VBox) getScene().getRoot().lookup("#presenzaFields");
        
        if (onlineBox != null && presenzaBox != null) {
            onlineBox.setVisible(isOnline);
            onlineBox.setManaged(isOnline);
            presenzaBox.setVisible(!isOnline);
            presenzaBox.setManaged(!isOnline);
        }
    }

    private void salvaSessione() {
        try {
            // Validazioni base
            if (datePicker.getValue() == null) {
                StyleHelper.showValidationDialog("Validazione", "Seleziona una data");
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
                    StyleHelper.showValidationDialog("Validazione", "Inserisci la piattaforma");
                    return;
                }
                
                sessioneCreata = new Online(dataInizio, dataFine, piattaforma);
                
            } else {
                String via = viaField.getText().trim();
                String citta = cittaField.getText().trim();
                String postiStr = postiField.getText().trim();
                String capStr = capField.getText().trim();

                if (via.isEmpty() || citta.isEmpty() || postiStr.isEmpty() || capStr.isEmpty()) {
                    StyleHelper.showValidationDialog("Validazione", "Compila tutti i campi obbligatori");
                    return;
                }

                int posti = Integer.parseInt(postiStr);
                int cap = Integer.parseInt(capStr);

                if (posti <= 0) {
                    StyleHelper.showValidationDialog("Validazione", "Il numero di posti deve essere positivo");
                    return;
                }

                if (cap < 10000 || cap > 99999) {
                    StyleHelper.showValidationDialog("Validazione", "CAP non valido (5 cifre)");
                    return;
                }

                sessioneCreata = new InPresenza(dataInizio, dataFine, via, citta, posti, cap);
            }

            close();

        } catch (NumberFormatException e) {
            StyleHelper.showValidationDialog("Validazione", "Formato numero non valido");
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore durante la creazione: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    public Sessione showDialog() {
        showAndWait();
        return sessioneCreata;
    }
}
