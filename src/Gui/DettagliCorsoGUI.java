package Gui;

import controller.GestioneCorsoController;
import model.CorsoCucina;
import model.Frequenza;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class DettagliCorsoGUI {

    private GestioneCorsoController gestioneController;
    private CorsoCucina corso;

    public void setController(GestioneCorsoController controller) {
        this.gestioneController = controller;
    }

    public void setCorso(CorsoCucina corso) {
        this.corso = corso;
    }

    // ===== getRoot =====
    public StackPane getRoot() {
        if (gestioneController == null || corso == null) {
            throw new IllegalStateException("Controller o corso non impostati!");
        }

        // Root principale con gradiente
        StackPane root = new StackPane();
        root.setPrefSize(500, 700);
        createBackground(root);

        // Card centrale
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

        // ===== Titolo =====
        Label title = new Label("Dettagli corso");
        title.setFont(Font.font("Roboto", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#FF6600"));

        // ===== Campi =====
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

        // Blocca campi inizialmente
        setEditable(false, nomeField, prezzoField, argomentoField, frequenzaCombo, numeroPostiField, dataInizioPicker, dataFinePicker);

        // ===== Pulsanti =====
        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);

        Button modificaBtn = createStylishButton("✏ Modifica");
        Button salvaBtn = createStylishButton("💾 Salva");
        Button sessioniBtn = createStylishButton("📅 Sessioni");
        Button indietroBtn = createStylishButton("⬅ Indietro");

        salvaBtn.setDisable(true);

        buttons.getChildren().addAll(modificaBtn, salvaBtn, sessioniBtn, indietroBtn);

        // Montaggio card
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

        // Modifica
        modificaBtn.setOnAction(e -> {
            setEditable(true, nomeField, prezzoField, argomentoField, frequenzaCombo, numeroPostiField, dataInizioPicker, dataFinePicker);
            salvaBtn.setDisable(false);
        });

        // Salva
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

        // Visualizza sessioni
        sessioniBtn.setOnAction(e -> {
            GestioneSessioniGUI sessioniGUI = new GestioneSessioniGUI();
            sessioniGUI.setCorso(corso);
            root.getScene().setRoot(sessioniGUI.getRoot()); // navigazione coerente
        });

        // Torna indietro
        indietroBtn.setOnAction(e -> {
            StackPane parent = (StackPane) root.getParent(); // lo StackPane di VisualizzaCorsiGUI
            if (parent != null) {
                parent.getChildren().remove(root); // rimuove i dettagli, sotto rimane la lista corsi
            }
        });
        return root;
    }

    // ===== Utils =====
    private void createBackground(StackPane root) {
        BackgroundFill gradient = new BackgroundFill(
                new javafx.scene.paint.LinearGradient(0, 0, 1, 1, true,
                        javafx.scene.paint.CycleMethod.NO_CYCLE,
                        new javafx.scene.paint.Stop(0, Color.web("#FF9966")),
                        new javafx.scene.paint.Stop(1, Color.web("#FF5E62"))),
                CornerRadii.EMPTY, Insets.EMPTY);
        root.setBackground(new Background(gradient));
    }

    private Button createStylishButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: linear-gradient(to right, #FF9966, #FF5E62);" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 20;" +
                "-fx-padding: 8 16;");
        return btn;
    }

    private void setEditable(boolean editable, TextField nome, TextField prezzo, TextField argomento,
                             ComboBox<Frequenza> freq, TextField posti,
                             DatePicker inizio, DatePicker fine) {
        nome.setEditable(editable);
        prezzo.setEditable(editable);
        argomento.setEditable(editable);
        freq.setDisable(!editable);
        posti.setEditable(editable);
        inizio.setDisable(!editable);
        fine.setDisable(!editable);
    }

    private void showAlert(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
