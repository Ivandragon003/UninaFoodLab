package Gui;

import controller.GestioneCorsoController;
import controller.GestioneSessioniController;
import model.CorsoCucina;
import model.Frequenza;
import service.GestioneCucina;
import service.GestioneRicette;
import service.GestioneSessioni;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class DettagliCorsoGUI {

    private GestioneCorsoController gestioneController;
    private CorsoCucina corso;

    public void setController(GestioneCorsoController controller) {
        this.gestioneController = controller;
    }

    public void setCorso(CorsoCucina corso) {
        this.corso = corso;
    }

    public StackPane getRoot() {
        if (gestioneController == null || corso == null) {
            throw new IllegalStateException("Controller o corso non impostati!");
        }

        StackPane root = new StackPane();
        root.setPrefSize(500, 700);
        createBackground(root);

        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(25));
        card.setMaxWidth(420);
        card.setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 20;" +
                "-fx-border-radius: 20;" +
                "-fx-border-color: #FF9966;" +
                "-fx-border-width: 2;");

        DropShadow shadow = new DropShadow();
        shadow.setRadius(10);
        shadow.setColor(Color.web("#000000", 0.2));
        shadow.setOffsetY(3);
        card.setEffect(shadow);

        Label title = new Label("Dettagli corso");
        title.setFont(Font.font("Roboto", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#FF6600"));

        TextField nomeField = new TextField(corso.getNomeCorso());
        TextField prezzoField = new TextField(String.valueOf(corso.getPrezzo()));
        TextField argomentoField = new TextField(corso.getArgomento());

        ComboBox<Frequenza> frequenzaCombo = new ComboBox<>();
        frequenzaCombo.getItems().setAll(Frequenza.values());
        frequenzaCombo.setValue(corso.getFrequenzaCorso());

        TextField numeroPostiField = new TextField(String.valueOf(corso.getNumeroPosti()));
        TextField numeroSessioniField = new TextField(
                corso.getSessioni() != null ? String.valueOf(corso.getSessioni().size()) : "0"
        );
        numeroSessioniField.setEditable(false);
        numeroSessioniField.setStyle("-fx-control-inner-background: #E0E0E0;");

        DatePicker dataInizioPicker = new DatePicker(
                corso.getDataInizioCorso() != null ? corso.getDataInizioCorso().toLocalDate() : null
        );
        DatePicker dataFinePicker = new DatePicker(
                corso.getDataFineCorso() != null ? corso.getDataFineCorso().toLocalDate() : null
        );

        setEditable(false, nomeField, prezzoField, argomentoField, frequenzaCombo, numeroPostiField, dataInizioPicker, dataFinePicker);

        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);

        Button modificaBtn = createStylishButton("✏ Modifica");
        Button salvaBtn = createStylishButton("💾 Salva");
        Button sessioniBtn = createStylishButton("📅 Sessioni");
        Button indietroBtn = createStylishButton("⬅ Indietro");

        salvaBtn.setDisable(true);
        buttons.getChildren().addAll(modificaBtn, salvaBtn, sessioniBtn, indietroBtn);

        card.getChildren().addAll(
                title,
                new Label("Nome:"), nomeField,
                new Label("Prezzo:"), prezzoField,
                new Label("Argomento:"), argomentoField,
                new Label("Frequenza:"), frequenzaCombo,
                new Label("Numero posti:"), numeroPostiField,
                new Label("Numero sessioni:"), numeroSessioniField,
                new Label("Data inizio:"), dataInizioPicker,
                new Label("Data fine:"), dataFinePicker,
                buttons
        );

        root.getChildren().add(card);

        // ===== Eventi =====

        modificaBtn.setOnAction(e -> {
            setEditable(true, nomeField, prezzoField, argomentoField, frequenzaCombo, numeroPostiField, dataInizioPicker, dataFinePicker);
            salvaBtn.setDisable(false);
        });

        salvaBtn.setOnAction(e -> {
            try {
                double prezzo = Double.parseDouble(prezzoField.getText().replace(',', '.'));
                int posti = Integer.parseInt(numeroPostiField.getText());

                if (dataInizioPicker.getValue() != null && dataFinePicker.getValue() != null &&
                        dataInizioPicker.getValue().isAfter(dataFinePicker.getValue())) {
                    showAlert("Errore", "La data di inizio deve precedere la data di fine.");
                    return;
                }

                corso.setNomeCorso(nomeField.getText());
                corso.setPrezzo(prezzo);
                corso.setArgomento(argomentoField.getText());
                corso.setFrequenzaCorso(frequenzaCombo.getValue());
                corso.setNumeroPosti(posti);
                corso.setNumeroSessioni(corso.getSessioni() != null ? corso.getSessioni().size() : 0);
                if (dataInizioPicker.getValue() != null) corso.setDataInizioCorso(dataInizioPicker.getValue().atStartOfDay());
                if (dataFinePicker.getValue() != null) corso.setDataFineCorso(dataFinePicker.getValue().atStartOfDay());

                gestioneController.modificaCorso(corso);
                numeroSessioniField.setText(String.valueOf(corso.getNumeroSessioni()));

                showAlert("Successo", "Corso modificato correttamente!");

                setEditable(false, nomeField, prezzoField, argomentoField, frequenzaCombo, numeroPostiField, dataInizioPicker, dataFinePicker);
                salvaBtn.setDisable(true);

            } catch (NumberFormatException ex) {
                showAlert("Errore", "Valori numerici non validi per prezzo o posti.");
            } catch (Exception ex) {
                showAlert("Errore", "Errore nel salvataggio: " + ex.getMessage());
            }
        });

        // ===== PULSANTE SESSIONI =====
        sessioniBtn.setOnAction(e -> {
            try {
                // Creo i servizi
                GestioneSessioni gestioneSessioniService = new GestioneSessioni();
                GestioneCucina gestioneCucinaService = new GestioneCucina();
                GestioneRicette gestioneRicetteService = new GestioneRicette();

                // Creo il controller sessioni
                GestioneSessioniController sessioniController =
                        new GestioneSessioniController(corso, gestioneSessioniService, gestioneCucinaService, gestioneRicetteService);

                // Creo e configuro la GUI
                GestioneSessioniGUI gui = new GestioneSessioniGUI();
                gui.setCorso(corso);
                gui.setController(sessioniController);
                gui.setModalitaAggiunta(true); // parte in modalità aggiunta nuova sessione

                // Apro in una nuova finestra
                Stage stage = new Stage();
                gui.start(stage);

            } catch (Exception ex) {
                showAlert("Errore", "Impossibile aprire la gestione sessioni: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // TODO: indietroBtn.setOnAction(...) se serve tornare alla schermata precedente

        return root;
    }

    private void createBackground(StackPane root) {
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #FFE0CC, #FFFFFF);");
    }

    private void setEditable(boolean editable, Control... controls) {
        for (Control c : controls) {
            if (c instanceof TextField tf) tf.setEditable(editable);
            if (c instanceof ComboBox<?> cb) cb.setDisable(!editable);
            if (c instanceof DatePicker dp) dp.setDisable(!editable);
        }
    }

    private Button createStylishButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #FF9966; -fx-text-fill: white; -fx-font-weight: bold;");
        return btn;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
