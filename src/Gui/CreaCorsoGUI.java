package Gui;

import controller.GestioneCorsoController;
import model.CorsoCucina;
import model.Frequenza;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CreaCorsoGUI {

    private GestioneCorsoController gestioneController;

    public void setController(GestioneCorsoController controller) {
        this.gestioneController = controller;
    }

    public void start(Stage stage) {
        if (gestioneController == null) {
            throw new IllegalStateException("Controller non impostato!");
        }

        stage.setTitle("Crea Nuovo Corso");

        // Campi del form
        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome corso");

        TextField argomentoField = new TextField();
        argomentoField.setPromptText("Argomento");

        TextField prezzoField = new TextField();
        prezzoField.setPromptText("Prezzo");

        ComboBox<Frequenza> frequenzaBox = new ComboBox<>();
        frequenzaBox.getItems().addAll(Frequenza.values());

        TextField postiField = new TextField();
        postiField.setPromptText("Numero posti");

        TextField numSessioniField = new TextField();
        numSessioniField.setPromptText("Numero sessioni");

        DatePicker dataInizioPicker = new DatePicker();
        DatePicker dataFinePicker = new DatePicker();

        Button salvaBtn = new Button("Salva corso");
        Button annullaBtn = new Button("Annulla");

        // VBox contenitore dei campi
        VBox formBox = new VBox(10);
        formBox.setPadding(new Insets(20));
        formBox.getChildren().addAll(
                new Label("Nome:"), nomeField,
                new Label("Argomento:"), argomentoField,
                new Label("Prezzo:"), prezzoField,
                new Label("Frequenza:"), frequenzaBox,
                new Label("Numero posti:"), postiField,
                new Label("Numero sessioni:"), numSessioniField,
                new Label("Data inizio:"), dataInizioPicker,
                new Label("Data fine:"), dataFinePicker,
                salvaBtn, annullaBtn
        );

        // ScrollPane per il form
        ScrollPane scrollPane = new ScrollPane(formBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);

        stage.setScene(new Scene(scrollPane, 400, 500));
        stage.show();

        // Salvataggio corso
        salvaBtn.setOnAction(e -> {
            try {
                // Controllo campi obbligatori 
                if (nomeField.getText().isEmpty() ||
                        argomentoField.getText().isEmpty() ||
                        prezzoField.getText().isEmpty() ||
                        postiField.getText().isEmpty() ||
                        numSessioniField.getText().isEmpty() ||
                        frequenzaBox.getValue() == null ||
                        dataInizioPicker.getValue() == null ||
                        dataFinePicker.getValue() == null) {

                    showAlert("Errore di validazione", "Compila tutti i campi obbligatori prima di salvare.");
                    return; // blocco il salvataggio
                }

                // Creazione corso
                double prezzo = Double.parseDouble(prezzoField.getText());
                CorsoCucina corso = new CorsoCucina(
                        nomeField.getText(),
                        prezzo,
                        argomentoField.getText(),
                        frequenzaBox.getValue(),
                        Integer.parseInt(postiField.getText()),
                        Integer.parseInt(numSessioniField.getText())
                );

                corso.setDataInizioCorso(dataInizioPicker.getValue().atStartOfDay());
                corso.setDataFineCorso(dataFinePicker.getValue().atStartOfDay());

                // Messaggio di successo con prezzo formattato
                String prezzoFormattato = String.format("€ %, .2f", corso.getPrezzo());
                showAlert("Successo", "Corso creato correttamente.\nPrezzo: " + prezzoFormattato);

            } catch (NumberFormatException ex) {
                showAlert("Errore", "Inserisci valori numerici validi per prezzo, posti e sessioni.");
            } catch (Exception ex) {
                showAlert("Errore", "Errore durante il salvataggio: " + ex.getMessage());
            }
        });

        annullaBtn.setOnAction(e -> stage.close());
    }

    private void showAlert(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
