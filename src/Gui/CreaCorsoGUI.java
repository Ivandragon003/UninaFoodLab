package Gui;

import controller.GestioneCorsoController;
import model.CorsoCucina;
import model.Frequenza;
import model.Chef;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class CreaCorsoGUI {

    private VBox root;
    private GestioneCorsoController gestioneController;

    private ObservableList<Chef> chefSelezionati = FXCollections.observableArrayList();
    private ListView<Chef> chefListView;

    public void setController(GestioneCorsoController controller) {
        this.gestioneController = controller;
    }

    public Pane getRoot() {
        if (root == null)
            initUI();
        return root;
    }

    private void initUI() {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30));
        mainContent.setAlignment(Pos.TOP_CENTER);

        // Sfondo gradiente arancione
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FF9966")), new Stop(0.5, Color.web("#FFB366")),
                new Stop(1, Color.web("#FFCC99")));
        mainContent.setBackground(
                new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(gradient, null, null)));

        // Titolo moderno
        Label title = new Label("🎓 Crea Nuovo Corso");
        title.setFont(Font.font("Inter", FontWeight.EXTRA_BOLD, 26));
        title.setTextFill(Color.WHITE);
        title.setEffect(new DropShadow(8, Color.web("#FF6600", 0.8)));

        Label subtitle = new Label("Compila i dettagli del corso di cucina");
        subtitle.setFont(Font.font("Inter", FontWeight.NORMAL, 13));
        subtitle.setTextFill(Color.web("#FFFFFF", 0.9));

        VBox headerBox = new VBox(8, title, subtitle);
        headerBox.setAlignment(Pos.CENTER);

        // Card del form con stile moderno
        VBox formCard = new VBox(15);
        formCard.setPadding(new Insets(30));
        formCard.setAlignment(Pos.TOP_LEFT);
        formCard.setMaxWidth(Double.MAX_VALUE);
        formCard.setMinWidth(400);
        formCard.setStyle("-fx-background-color: rgba(255,255,255,0.95); "
                + "-fx-background-radius: 20; -fx-border-radius: 20; "
                + "-fx-border-color: rgba(255,255,255,0.4); -fx-border-width: 1;");
        formCard.setEffect(new DropShadow(15, Color.web("#000000", 0.15)));

        // Campi form
        TextField nomeField = createStyledTextField("📝 Nome corso");
        TextField prezzoField = createStyledTextField("💰 Prezzo (es: 150.00)");
        TextField argomentoField = createStyledTextField("🍽️ Argomento");
        TextField postiField = createStyledTextField("👥 Numero posti disponibili");

        ComboBox<Frequenza> frequenzaBox = createStyledComboBox();
        frequenzaBox.setPromptText("📅 Frequenza");
        frequenzaBox.getItems().addAll(Frequenza.values());

        DatePicker startDatePicker = createStyledDatePicker("📆 Data inizio");
        DatePicker endDatePicker = createStyledDatePicker("📆 Data fine");

        Spinner<Integer> startHour = createStyledSpinner(0, 23, 9);
        Spinner<Integer> startMinute = createStyledSpinner(0, 59, 0);
        Spinner<Integer> endHour = createStyledSpinner(0, 23, 17);
        Spinner<Integer> endMinute = createStyledSpinner(0, 59, 0);

        HBox startTimeBox = new HBox(8, new Label("⏰ Ora inizio:"), startHour, new Label(":"), startMinute);
        styleTimeBox(startTimeBox);
        HBox endTimeBox = new HBox(8, new Label("⏰ Ora fine:"), endHour, new Label(":"), endMinute);
        styleTimeBox(endTimeBox);

        // Sezione chef
        Label chefLabel = new Label("👨‍🍳 Chef Responsabili");
        chefLabel.setFont(Font.font("Inter", FontWeight.BOLD, 14));
        chefLabel.setTextFill(Color.web("#FF6600"));

        Button btnAggiungiChef = createModernButton("➕ Seleziona Chef", "#FF6600", "#FF8533");
        btnAggiungiChef.setPrefWidth(180);

        chefListView = new ListView<>(chefSelezionati);
        chefListView.setPrefHeight(140);
        chefListView.setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #FFB366; "
                + "-fx-border-radius: 10; -fx-background-radius: 10;");

        chefListView.setCellFactory(lv -> new ListCell<Chef>() {
            private HBox content;
            private Label nomeLabel;
            private Button removeBtn;

            {
                nomeLabel = new Label();
                nomeLabel.setFont(Font.font("Inter", 13));
                nomeLabel.setTextFill(Color.web("#333333"));

                removeBtn = new Button("✕");
                removeBtn.setFont(Font.font("Inter", FontWeight.BOLD, 12));
                removeBtn.setTextFill(Color.RED);
                removeBtn.setStyle("-fx-background-color: transparent;");
                removeBtn.setOnAction(e -> {
                    Chef chef = getItem();
                    if (chef != null) {
                        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
                        conferma.setTitle("Conferma rimozione");
                        conferma.setHeaderText(null);
                        conferma.setContentText("Vuoi rimuovere " + chef.getNome() + " " + chef.getCognome() + " dal corso?");
                        conferma.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                chefSelezionati.remove(chef);
                            }
                        });
                    }
                });

                content = new HBox(10, nomeLabel, removeBtn);
                content.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(Chef chef, boolean empty) {
                super.updateItem(chef, empty);
                if (empty || chef == null) {
                    setGraphic(null);
                } else {
                    nomeLabel.setText("👨‍🍳 " + chef.getNome() + " " + chef.getCognome());
                    setGraphic(content);
                }
            }
        });

        btnAggiungiChef.setOnAction(e -> {
            try {
                SelezionaChefDialog dialog = new SelezionaChefDialog((Stage) root.getScene().getWindow(),
                        gestioneController, chefSelezionati);
                dialog.showAndWait();
                List<Chef> scelti = dialog.getSelezionati();
                if (scelti != null && !scelti.isEmpty()) {
                    for (Chef c : scelti) {
                        if (!chefSelezionati.contains(c)) {
                            chefSelezionati.add(c);
                        }
                    }
                }
            } catch (SQLException ex) {
                showAlert("Errore", "Impossibile caricare gli chef: " + ex.getMessage());
            }
        });

        formCard.getChildren().addAll(
                createFieldLabel("Nome del Corso"), nomeField,
                createFieldLabel("Prezzo"), prezzoField,
                createFieldLabel("Argomento"), argomentoField,
                createFieldLabel("Numero Posti"), postiField,
                createFieldLabel("Frequenza"), frequenzaBox,
                createFieldLabel("Data Inizio"), startDatePicker, startTimeBox,
                createFieldLabel("Data Fine"), endDatePicker, endTimeBox,
                chefLabel, btnAggiungiChef, chefListView
        );

        HBox btnBox = new HBox(15);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(10, 0, 0, 0));

        Button salvaBtn = createModernButton("✅ Salva Corso", "#FF6600", "#FF8533");
        Button annullaBtn = createModernButton("❌ Annulla", "#999999", "#BBBBBB");

        salvaBtn.setOnAction(e -> salvaCorso(nomeField, prezzoField, argomentoField, postiField,
                frequenzaBox, startDatePicker, endDatePicker, startHour, startMinute, endHour, endMinute));

        annullaBtn.setOnAction(e -> ((Stage) root.getScene().getWindow()).close());

        btnBox.getChildren().addAll(annullaBtn, salvaBtn);

        ScrollPane scrollPane = new ScrollPane(formCard);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox contentBox = new VBox(20, headerBox, scrollPane, btnBox);
        contentBox.setAlignment(Pos.TOP_CENTER);
        contentBox.setPadding(new Insets(20, 40, 20, 40));
        contentBox.setMaxWidth(Double.MAX_VALUE);

        mainContent.getChildren().add(contentBox);

        root = new VBox(mainContent);
        root.setAlignment(Pos.CENTER);
    }

    // --- Helper methods ---
    private TextField createStyledTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(40);
        field.setFont(Font.font("Inter", 13));
        field.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; "
                + "-fx-border-color: #FFB366; -fx-border-width: 1.5; -fx-padding: 0 12;");
        return field;
    }

    private ComboBox<Frequenza> createStyledComboBox() {
        ComboBox<Frequenza> combo = new ComboBox<>();
        combo.setPrefHeight(40);
        combo.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; "
                + "-fx-border-color: #FFB366; -fx-border-width: 1.5;");
        return combo;
    }

    private DatePicker createStyledDatePicker(String prompt) {
        DatePicker picker = new DatePicker();
        picker.setPromptText(prompt);
        picker.setPrefHeight(40);
        picker.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; "
                + "-fx-border-color: #FFB366; -fx-border-width: 1.5;");
        return picker;
    }

    private Spinner<Integer> createStyledSpinner(int min, int max, int initial) {
        Spinner<Integer> spinner = new Spinner<>(min, max, initial);
        spinner.setPrefWidth(70);
        spinner.setPrefHeight(40);
        spinner.setStyle("-fx-background-color: white; -fx-border-color: #FFB366; -fx-border-radius: 8; "
                + "-fx-background-radius: 8;");
        return spinner;
    }

    private void styleTimeBox(HBox box) {
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(5, 0, 5, 0));
        for (javafx.scene.Node node : box.getChildren()) {
            if (node instanceof Label) {
                ((Label) node).setFont(Font.font("Inter", FontWeight.MEDIUM, 13));
                ((Label) node).setTextFill(Color.web("#666666"));
            }
        }
    }

    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Inter", FontWeight.BOLD, 13));
        label.setTextFill(Color.web("#555555"));
        return label;
    }

    private Button createModernButton(String text, String baseColor, String hoverColor) {
        Button btn = new Button(text);
        btn.setPrefWidth(160);
        btn.setPrefHeight(44);
        btn.setFont(Font.font("Inter", FontWeight.BOLD, 14));
        btn.setTextFill(Color.WHITE);

        String baseStyle = "-fx-background-color: " + baseColor + "; -fx-background-radius: 22; "
                + "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8, 0, 0, 3);";
        String hoverStyle = "-fx-background-color: " + hoverColor + "; -fx-background-radius: 22; "
                + "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 10, 0, 0, 4);";

        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        btn.setOnMousePressed(e -> { btn.setScaleX(0.97); btn.setScaleY(0.97); });
        btn.setOnMouseReleased(e -> { btn.setScaleX(1.0); btn.setScaleY(1.0); });

        return btn;
    }

    private void salvaCorso(TextField nomeField, TextField prezzoField, TextField argomentoField, TextField postiField,
                             ComboBox<Frequenza> frequenzaBox, DatePicker startDatePicker, DatePicker endDatePicker,
                             Spinner<Integer> startHour, Spinner<Integer> startMinute, Spinner<Integer> endHour,
                             Spinner<Integer> endMinute) {

        try {
            String nome = nomeField.getText().trim();
            String argomento = argomentoField.getText().trim();
            Frequenza freq = frequenzaBox.getValue();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (nome.isEmpty() || argomento.isEmpty() || freq == null || startDate == null || endDate == null
                    || chefSelezionati.isEmpty()) {
                showAlert("⚠️ Campi Mancanti", "Compila tutti i campi obbligatori e seleziona almeno uno chef.");
                return;
            }

            double prezzo = Double.parseDouble(prezzoField.getText().trim().replace(",", "."));
            int posti = Integer.parseInt(postiField.getText().trim());

            LocalDateTime dataInizio = LocalDateTime.of(startDate,
                    LocalTime.of(startHour.getValue(), startMinute.getValue()));
            LocalDateTime dataFine = LocalDateTime.of(endDate,
                    LocalTime.of(endHour.getValue(), endMinute.getValue()));

            if (dataInizio.isBefore(LocalDateTime.now())) {
                startDatePicker.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                showAlert("Errore", "Data di inizio corso non può essere nel passato.");
                return;
            } else startDatePicker.setStyle("");

            if (dataFine.isBefore(dataInizio)) {
                endDatePicker.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                showAlert("Errore", "Data di fine deve essere successiva alla data di inizio.");
                return;
            } else endDatePicker.setStyle("");

            CorsoCucina corso = new CorsoCucina(nome, prezzo, argomento, freq, posti);
            corso.setDataInizioCorso(dataInizio);
            corso.setDataFineCorso(dataFine);
            corso.getChef().addAll(chefSelezionati);

            gestioneController.creaCorso(corso);

            showAlert("✅ Successo", "Corso creato con successo!");

            nomeField.clear();
            prezzoField.clear();
            argomentoField.clear();
            postiField.clear();
            frequenzaBox.getSelectionModel().clearSelection();
            startDatePicker.setValue(null);
            endDatePicker.setValue(null);
            startHour.getValueFactory().setValue(9);
            startMinute.getValueFactory().setValue(0);
            endHour.getValueFactory().setValue(17);
            endMinute.getValueFactory().setValue(0);
            chefSelezionati.clear();

        } catch (NumberFormatException ex) {
            showAlert("Errore", "Prezzo o numero posti non validi.");
        } catch (IllegalArgumentException ex) {
            showAlert("Errore", ex.getMessage());
        } catch (Exception ex) {
            showAlert("Errore", ex.getMessage());
        }
    }

    private void showAlert(String titolo, String messaggio) {
        VBox overlay = new VBox();
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.3);");
        overlay.setPrefSize(root.getWidth(), root.getHeight());

        VBox dialogBox = new VBox(15);
        dialogBox.setAlignment(Pos.CENTER);
        dialogBox.setPadding(new Insets(25));
        dialogBox.setStyle("-fx-background-color: white; -fx-background-radius: 15; "
                + "-fx-border-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 4);");

        Label titleLabel = new Label(titolo);
        titleLabel.setFont(Font.font("Inter", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#FF6600"));

        Label messageLabel = new Label(messaggio);
        messageLabel.setFont(Font.font("Inter", 13));
        messageLabel.setTextFill(Color.web("#333333"));
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);

        Button okBtn = new Button("OK");
        okBtn.setFont(Font.font("Inter", FontWeight.BOLD, 13));
        okBtn.setTextFill(Color.WHITE);
        okBtn.setStyle("-fx-background-color: #FF6600; -fx-background-radius: 12; -fx-padding: 6 20;");
        okBtn.setOnAction(e -> root.getChildren().remove(overlay));

        dialogBox.getChildren().addAll(titleLabel, messageLabel, okBtn);
        overlay.getChildren().add(dialogBox);

        root.getChildren().add(overlay);
    }
}
