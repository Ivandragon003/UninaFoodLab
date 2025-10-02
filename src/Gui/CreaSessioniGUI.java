package Gui;
import controller.VisualizzaRicetteController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Sessione;
import model.Online;
import model.InPresenza;
import model.Ricetta;
import service.GestioneCucina;
import service.GestioneRicette;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

public class CreaSessioniGUI {
    private Sessione sessioneCreata = null;
    private VBox root;
    private final Set<LocalDate> dateConflittoRosse;
    private final Set<LocalDate> dateConflittoArancioni;
    private final GestioneRicette gestioneRicette;
    private final GestioneCucina gestioneCucina;

    public CreaSessioniGUI(Set<LocalDate> rosse, Set<LocalDate> arancioni,
                          GestioneRicette gestioneRicette, GestioneCucina gestioneCucina) {
        this.dateConflittoRosse = rosse;
        this.dateConflittoArancioni = arancioni;
        this.gestioneRicette = gestioneRicette;
        this.gestioneCucina = gestioneCucina;
    }

    public Sessione showDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("✨ Nuova Sessione");

        StackPane rootPane = new StackPane();
        rootPane.setPrefSize(520, 650);

        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FF9966")), new Stop(0.5, Color.web("#FFB366")),
                new Stop(1, Color.web("#FFCC99")));

        Region background = new Region();
        background.setBackground(new Background(new BackgroundFill(gradient, null, null)));
        background.setPrefSize(520, 650);

        root = new VBox(25);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: transparent;");

        Label title = new Label("✨ Crea Nuova Sessione");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);
        title.setEffect(new DropShadow(5, Color.web("#000000", 0.25)));

        VBox formCard = new VBox(22);
        formCard.setPadding(new Insets(30));
        formCard.setAlignment(Pos.TOP_CENTER);
        formCard.setStyle("-fx-background-color: white; -fx-background-radius: 25; -fx-border-radius: 25;");
        formCard.setEffect(new DropShadow(15, Color.web("#000000", 0.2)));

        // Date Picker Inizio
        VBox dataInizioBox = new VBox(8);
        Label lblDataInizio = new Label("📅 Data Inizio");
        lblDataInizio.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, 14));
        lblDataInizio.setTextFill(Color.web("#FF6600"));
        DatePicker dataInizioPicker = createColoredDatePicker();
        styleModernDatePicker(dataInizioPicker);
        dataInizioBox.getChildren().addAll(lblDataInizio, dataInizioPicker);

        // Date Picker Fine
        VBox dataFineBox = new VBox(4);
        Label lblDataFine = new Label("📅 Data Fine");
        lblDataFine.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, 14));
        lblDataFine.setTextFill(Color.web("#FF6600"));
        DatePicker dataFinePicker = createColoredDatePicker();
        styleModernDatePicker(dataFinePicker);
        dataFinePicker.setEditable(false);
        dataFinePicker.setMouseTransparent(true);
        dataFinePicker.setFocusTraversable(false);
        dataFinePicker.setStyle("-fx-background-color: #f0f0f0;" +
                "-fx-border-color: #d0d0d0;" + "-fx-border-radius: 12;" + "-fx-background-radius: 12;" +
                "-fx-border-width: 1.5;" + "-fx-font-size: 14px;" + "-fx-padding: 8;");
        Label lockLabel = new Label("🔒 Bloccata alla data inizio");
        lockLabel.setFont(Font.font("Inter", 12));
        lockLabel.setTextFill(Color.GRAY);
        dataFineBox.getChildren().addAll(lblDataFine, dataFinePicker, lockLabel);

        dataInizioPicker.setOnAction(e -> {
            LocalDate selectedDate = dataInizioPicker.getValue();
            if (selectedDate != null) dataFinePicker.setValue(selectedDate);
        });

        // Orari
        VBox orarioInizioContainer = new VBox(8);
        Label lblOraInizio = new Label("⏰ Ora Inizio");
        lblOraInizio.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, 14));
        lblOraInizio.setTextFill(Color.web("#FF6600"));
        HBox orarioInizioBox = createModernTimePicker();
        orarioInizioContainer.getChildren().addAll(lblOraInizio, orarioInizioBox);

        VBox orarioFineContainer = new VBox(8);
        Label lblOraFine = new Label("⏰ Ora Fine");
        lblOraFine.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, 14));
        lblOraFine.setTextFill(Color.web("#FF6600"));
        HBox orarioFineBox = createModernTimePicker();
        orarioFineContainer.getChildren().addAll(lblOraFine, orarioFineBox);

        Separator sep1 = new Separator();
        sep1.setStyle("-fx-background-color: rgba(0,0,0,0.06);");

        // Tipo sessione
        VBox tipoBox = new VBox(8);
        Label lblTipo = new Label("🎯 Tipo Sessione");
        lblTipo.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, 14));
        lblTipo.setTextFill(Color.web("#FF6600"));
        ComboBox<String> tipoCombo = createStyledComboBox();
        tipoCombo.getItems().addAll("Online", "In Presenza");
        tipoCombo.setValue("Online");
        tipoBox.getChildren().addAll(lblTipo, tipoCombo);

        // Campi Online
        TextField piattaformaField = new TextField();
        piattaformaField.setPromptText("🌐 Piattaforma (es. Zoom, Teams)");
        piattaformaField.setPrefHeight(45);
        piattaformaField.setStyle(
                "-fx-background-color: white; -fx-border-color: #d0d0d0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-border-width: 1.5; -fx-padding: 10;");
        VBox onlineBox = new VBox(10, piattaformaField);

        // Campi In Presenza
        TextField viaField = new TextField();
        viaField.setPromptText("📍 Via");
        TextField cittaField = new TextField();
        cittaField.setPromptText("🏙️ Città");
        TextField postiField = new TextField();
        postiField.setPromptText("👥 Numero Posti");
        TextField capField = new TextField();
        capField.setPromptText("📮 CAP");
        presenzaBoxSetup(viaField, cittaField, postiField, capField);

        VBox presenzaBox = new VBox(10, viaField, cittaField, postiField, capField);
        presenzaBox.setVisible(false);
        presenzaBox.setManaged(false);

        // Lista ricette
        ObservableList<HBox> ricetteList = FXCollections.observableArrayList();
        ListView<HBox> ricetteListView = new ListView<>();
        ricetteListView.setPrefHeight(150);
        presenzaBox.getChildren().add(ricetteListView);

        // Pulsante gestione ricette
        Button gestisciRicetteBtn = createModernButton("📖 Gestisci Ricette", "#4CAF50", "#66BB6A");
        presenzaBox.getChildren().add(gestisciRicetteBtn);

        gestisciRicetteBtn.setOnAction(e -> {
            try {
                // sessione temporanea (se non esiste ancora una InPresenza creata)
                InPresenza tempSessione;
                if (sessioneCreata instanceof InPresenza inPresenza) tempSessione = inPresenza;
                else tempSessione = new InPresenza(LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                        "ViaTemp", "CittaTemp", 1, 0);

                // *** CORREZIONE PRINCIPALE: uso diretto di gestioneRicette ***
                java.util.List<Ricetta> tutteRicette = null;
                try {
                    tutteRicette = gestioneRicette.getAllRicette(); // Metodo corretto esistente
                } catch (Exception exc) {
                    exc.printStackTrace();
                    Throwable sqlCause = findCause(exc, java.sql.SQLException.class);
                    if (sqlCause != null) {
                        showAlert("Errore DB/Service", buildSpecificMessage((Exception) sqlCause,
                                "Errore durante il recupero delle ricette dal database"));
                        return;
                    }
                    Throwable iaeCause = findCause(exc, IllegalArgumentException.class);
                    if (iaeCause != null) {
                        showAlert("Errore Model", buildSpecificMessage((Exception) iaeCause,
                                "Errore di validazione sulle ricette"));
                        return;
                    }
                    showAlert("Errore Controller/Generico", buildSpecificMessage(exc,
                            "Errore durante il recupero delle ricette"));
                    return;
                }

                if (tutteRicette == null) tutteRicette = new java.util.ArrayList<>();

                // Dialog per visualizzare e selezionare le ricette (multi-select)
                Stage selectionStage = new Stage();
                selectionStage.initOwner(dialog);
                selectionStage.initModality(Modality.APPLICATION_MODAL);
                selectionStage.setTitle("Seleziona Ricette");

                VBox selectionBox = new VBox(12);
                selectionBox.setPadding(new Insets(16));
                selectionBox.setAlignment(Pos.TOP_CENTER);

                Label selTitle = new Label("Seleziona le ricette da associare alla sessione");
                selTitle.setFont(Font.font("Inter", FontWeight.BOLD, 14));

                ListView<Ricetta> allRicetteView = new ListView<>();
                allRicetteView.getItems().addAll(tutteRicette);
                allRicetteView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                allRicetteView.setPrefHeight(300);

                // pre-seleziona quelle già presenti nella tempSessione
                if (tempSessione.getRicette() != null && !tempSessione.getRicette().isEmpty()) {
                    for (Ricetta r : tempSessione.getRicette()) {
                        // require equals/hashCode correttamente implementati in Ricetta
                        allRicetteView.getSelectionModel().select(r);
                    }
                }

                Button confermaBtn = createModernButton("✓ Conferma", "#4CAF50", "#66BB6A");
                Button annullaBtn2 = createModernButton("✗ Annulla", "#999999", "#BBBBBB");
                HBox btns = new HBox(10, annullaBtn2, confermaBtn);
                btns.setAlignment(Pos.CENTER);

                confermaBtn.setOnAction(ev -> {
                    try {
                        java.util.List<Ricetta> selezionate = new java.util.ArrayList<>(allRicetteView.getSelectionModel().getSelectedItems());

                        // aggiorna il set della sessione temporanea
                        tempSessione.getRicette().clear();
                        tempSessione.getRicette().addAll(selezionate);

                        // aggiorna la ListView visiva (ricetteListView) con HBox + remove
                        ricetteListView.getItems().clear();
                        for (Ricetta r : tempSessione.getRicette()) {
                            HBox item = new HBox(10);
                            Label nome = new Label(r.getNome());
                            Button removeBtn = new Button("❌");
                            removeBtn.setOnAction(remEv -> {
                                try {
                                    tempSessione.getRicette().remove(r);
                                    ricetteListView.getItems().remove(item);
                                } catch (Exception exRem) {
                                    exRem.printStackTrace();
                                    showAlert("Errore Controller/Model", buildSpecificMessage(exRem,
                                            "Impossibile rimuovere la ricetta dalla sessione"));
                                }
                            });
                            item.getChildren().addAll(nome, removeBtn);
                            ricetteListView.getItems().add(item);
                        }

                        sessioneCreata = tempSessione;
                        selectionStage.close();
                    } catch (Exception exSel) {
                        exSel.printStackTrace();
                        showAlert("Errore Selezione", buildSpecificMessage(exSel, "Errore durante la selezione delle ricette"));
                    }
                });

                annullaBtn2.setOnAction(ev -> selectionStage.close());

                selectionBox.getChildren().addAll(selTitle, allRicetteView, btns);
                Scene selScene = new Scene(selectionBox, 420, 420);
                selectionStage.setScene(selScene);
                selectionStage.showAndWait();

            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Errore", buildSpecificMessage(ex, "Errore imprevisto durante la gestione/visualizzazione delle ricette"));
            }
        });

        tipoCombo.setOnAction(e -> {
            boolean isOnline = tipoCombo.getValue().equals("Online");
            onlineBox.setVisible(isOnline);
            onlineBox.setManaged(isOnline);
            presenzaBox.setVisible(!isOnline);
            presenzaBox.setManaged(!isOnline);
        });

        // Pulsanti
        HBox btnBox = new HBox(15);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(10, 0, 0, 0));

        Button salvaBtn = createModernButton("💾 Salva Sessione", "#FF6600", "#FF8533");
        Button annullaBtn = createModernButton("❌ Annulla", "#e0e0e0", "#d0d0d0");
        annullaBtn.setTextFill(Color.web("#666666"));
        btnBox.getChildren().addAll(annullaBtn, salvaBtn);

        salvaBtn.setOnAction(e -> {
            try {
                LocalDate dataInizio = dataInizioPicker.getValue();
                LocalDate dataFine = dataFinePicker.getValue();
                LocalTime oraInizio = parseTime(orarioInizioBox);
                LocalTime oraFine = parseTime(orarioFineBox);

                if (dataInizio == null || dataFine == null)
                    throw new IllegalArgumentException("Compila tutte le date.");

                LocalDateTime inizio = LocalDateTime.of(dataInizio, oraInizio);
                LocalDateTime fine = LocalDateTime.of(dataFine, oraFine);

                if (inizio.isBefore(LocalDateTime.now()))
                    throw new IllegalArgumentException("Inizio nel passato.");
                if (fine.isBefore(inizio))
                    throw new IllegalArgumentException("Fine prima dell'inizio.");

                if (tipoCombo.getValue().equals("Online")) {
                    String piattaforma = piattaformaField.getText().trim();
                    if (piattaforma.isEmpty())
                        throw new IllegalArgumentException("Inserisci la piattaforma.");
                    sessioneCreata = new Online(inizio, fine, piattaforma);
                } else {
                    String via = viaField.getText().trim();
                    String citta = cittaField.getText().trim();
                    if (via.isEmpty()) throw new IllegalArgumentException("Inserisci la via.");
                    if (citta.isEmpty()) throw new IllegalArgumentException("Inserisci la città.");

                    int posti;
                    int cap;
                    try {
                        posti = Integer.parseInt(postiField.getText().trim());
                    } catch (NumberFormatException nfe) {
                        throw new IllegalArgumentException("Numero posti non valido. Inserisci un intero.");
                    }
                    try {
                        cap = Integer.parseInt(capField.getText().trim());
                    } catch (NumberFormatException nfe) {
                        throw new IllegalArgumentException("CAP non valido. Inserisci un intero.");
                    }

                    InPresenza definitiva = new InPresenza(inizio, fine, via, citta, posti, cap);
                    if (sessioneCreata instanceof InPresenza temp) {
                        // copia le ricette già selezionate nella temporanea (se ci sono)
                        if (temp.getRicette() != null) {
                            definitiva.getRicette().addAll(temp.getRicette());
                        }
                    }
                    sessioneCreata = definitiva;
                }

                dialog.close();
            } catch (Exception ex) {
                // distinzione precisa della provenienza
                ex.printStackTrace();
                if (ex instanceof SQLException) {
                    showAlert("Errore DB/Service", buildSpecificMessage(ex, "Problema di accesso ai dati o servizio"));
                } else if (ex instanceof IllegalArgumentException) {
                    showAlert("Errore Model/Validazione", buildSpecificMessage(ex, "Dati non validi: correggi i campi indicati"));
                } else {
                    // se lo stack trace contiene classi del package "controller" proviamo a segnalarlo come Controller
                    boolean fromController = false;
                    StackTraceElement[] st = ex.getStackTrace();
                    if (st != null && st.length > 0) {
                        for (StackTraceElement ste : st) {
                            String cn = ste.getClassName().toLowerCase();
                            if (cn.contains("controller") || cn.contains("service")) {
                                fromController = true;
                                break;
                            }
                        }
                    }
                    if (fromController) {
                        showAlert("Errore Controller", buildSpecificMessage(ex, "Errore dal layer controller/servizio"));
                    } else {
                        showAlert("Errore Generico", buildSpecificMessage(ex, "Errore imprevisto"));
                    }
                }
            }
        });

        annullaBtn.setOnAction(e -> {
            sessioneCreata = null;
            dialog.close();
        });

        formCard.getChildren().addAll(dataInizioBox, dataFineBox, orarioInizioContainer, orarioFineContainer, sep1,
                tipoBox, onlineBox, presenzaBox, btnBox);

        ScrollPane scrollPane = new ScrollPane(formCard);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        root.getChildren().addAll(title, scrollPane);
        rootPane.getChildren().addAll(background, root);

        Scene scene = new Scene(rootPane, 520, 650);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();

        return sessioneCreata;
    }

    // ---- Metodi di supporto ----
    private void presenzaBoxSetup(TextField via, TextField citta, TextField posti, TextField cap) {
        via.setPrefHeight(45);
        citta.setPrefHeight(45);
        posti.setPrefHeight(45);
        cap.setPrefHeight(45);
        String style = "-fx-background-color: white; -fx-border-color: #d0d0d0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-border-width: 1.5; -fx-padding: 10;";
        via.setStyle(style);
        citta.setStyle(style);
        posti.setStyle(style);
        cap.setStyle(style);
    }

    private DatePicker createColoredDatePicker() {
        DatePicker picker = new DatePicker();
        picker.setDayCellFactory(new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(DatePicker param) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        if (empty || date == null) return;

                        if (dateConflittoRosse.contains(date)) {
                            setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-weight: bold;");
                            setDisable(true);
                            setOpacity(0.6);
                        } else if (dateConflittoArancioni.contains(date)) {
                            setStyle("-fx-background-color: #ffd93d; -fx-text-fill: #333; -fx-font-weight: bold;");
                        } else if (date.equals(LocalDate.now())) {
                            setStyle("-fx-background-color: #FF9966; -fx-text-fill: white; -fx-font-weight: bold;");
                        }
                    }
                };
            }
        });
        return picker;
    }

    private void styleModernDatePicker(DatePicker picker) {
        picker.setPrefHeight(45);
        picker.setStyle("-fx-background-color: white;" +
                "-fx-border-color: #d0d0d0;" +
                "-fx-border-radius: 12;" +
                "-fx-background-radius: 12;" +
                "-fx-border-width: 1.5;" +
                "-fx-font-size: 14px;" +
                "-fx-padding: 8;");
    }

    private HBox createModernTimePicker() {
        ComboBox<Integer> ore = new ComboBox<>();
        ComboBox<Integer> minuti = new ComboBox<>();

        for (int i = 0; i < 24; i++) ore.getItems().add(i);
        for (int i = 0; i < 60; i += 5) minuti.getItems().add(i);

        ore.setValue(9);
        minuti.setValue(0);

        String comboStyle = "-fx-background-color: white;" +
                "-fx-border-color: #d0d0d0;" +
                "-fx-border-radius: 12;" +
                "-fx-background-radius: 12;" +
                "-fx-border-width: 1.5;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;";

        ore.setStyle(comboStyle);
        minuti.setStyle(comboStyle);
        ore.setPrefWidth(80);
        minuti.setPrefWidth(80);
        ore.setPrefHeight(45);
        minuti.setPrefHeight(45);

        Label separator = new Label(":");
        separator.setFont(Font.font("Inter", FontWeight.BOLD, 24));
        separator.setTextFill(Color.web("#FF6600"));

        HBox box = new HBox(12, ore, separator, minuti);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setUserData(new ComboBox[]{ore, minuti});
        return box;
    }

    @SuppressWarnings("unchecked")
    private LocalTime parseTime(HBox timeBox) {
        ComboBox<Integer>[] pickers = (ComboBox<Integer>[]) timeBox.getUserData();
        return LocalTime.of(pickers[0].getValue(), pickers[1].getValue());
    }

    private ComboBox<String> createStyledComboBox() {
        ComboBox<String> combo = new ComboBox<>();
        combo.setPrefHeight(45);
        combo.setStyle("-fx-background-color: white;" +
                "-fx-border-color: #d0d0d0;" +
                "-fx-border-radius: 12;" +
                "-fx-background-radius: 12;" +
                "-fx-border-width: 1.5;" +
                "-fx-font-size: 14px;");
        return combo;
    }

    private Button createModernButton(String text, String baseColor, String hoverColor) {
        Button btn = new Button(text);
        btn.setPrefHeight(48);
        btn.setPrefWidth(180);
        btn.setFont(Font.font("Inter", FontWeight.BOLD, 14));
        btn.setTextFill(Color.WHITE);
        btn.setStyle("-fx-background-color: " + baseColor + ";" +
                "-fx-background-radius: 24;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);");

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + hoverColor + ";" +
                "-fx-background-radius: 24;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 12, 0, 0, 3);" +
                "-fx-scale-x: 1.02;" +
                "-fx-scale-y: 1.02;"));

        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + baseColor + ";" +
                "-fx-background-radius: 24;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);"));

        return btn;
    }

    /**
     * Costruisce un messaggio più specifico, indicando tipologia e messaggio originale.
     */
    private String buildSpecificMessage(Exception ex, String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append("\n\n");

        String origin = "Origine: Sconosciuta";
        if (ex instanceof SQLException) origin = "Origine: DB/Service";
        else if (ex instanceof IllegalArgumentException) origin = "Origine: Model (validazione)";
        else {
            // prova a dedurre dall'stacktrace
            StackTraceElement[] st = ex.getStackTrace();
            if (st != null && st.length > 0) {
                String cn = st[0].getClassName().toLowerCase();
                if (cn.contains("controller") || cn.contains("service")) origin = "Origine: Controller/Service";
                else if (cn.contains("dao") || cn.contains("repository")) origin = "Origine: DB/DAO";
            }
        }
        sb.append(origin).append("\n\n");

        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) msg = ex.toString();
        sb.append("Dettaglio: ").append(msg);

        return sb.toString();
    }

    /**
     * Cerca ricorsivamente nella chain delle cause un'eccezione di tipo `cls`.
     * Restituisce la Throwable trovata o null se non presente.
     */
    private Throwable findCause(Throwable t, Class<? extends Throwable> cls) {
        Throwable cur = t;
        while (cur != null) {
            if (cls.isInstance(cur)) return cur;
            cur = cur.getCause();
        }
        return null;
    }

    private void showAlert(String title, String msg) {
        // overlay-like alert (come da versione originale)
        VBox overlay = new VBox();
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");
        overlay.setPrefSize(root.getWidth(), root.getHeight());

        VBox dialogBox = new VBox(20);
        dialogBox.setAlignment(Pos.CENTER);
        dialogBox.setPadding(new Insets(30));
        dialogBox.setMaxWidth(350);
        dialogBox.setStyle("-fx-background-color: white; -fx-background-radius: 20;");
        dialogBox.setEffect(new DropShadow(15, Color.web("#000000", 0.3)));

        Label titleLabel = new Label("⚠️ " + title);
        titleLabel.setFont(Font.font("Inter", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web("#FF6600"));

        Label messageLabel = new Label(msg);
        messageLabel.setWrapText(true);
        messageLabel.setFont(Font.font("Inter", 15));
        messageLabel.setTextFill(Color.web("#333333"));
        messageLabel.setAlignment(Pos.CENTER);

        Button okBtn = createModernButton("OK", "#FF6600", "#FF8533");
        okBtn.setPrefWidth(120);
        okBtn.setOnAction(e -> root.getChildren().remove(overlay));

        dialogBox.getChildren().addAll(titleLabel, messageLabel, okBtn);
        overlay.getChildren().add(dialogBox);
        root.getChildren().add(overlay);
    }
}