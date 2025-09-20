package Gui;

import controller.GestioneCorsoController;
import model.CorsoCucina;
import model.Frequenza;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DettagliCorsoGUI {

    private GestioneCorsoController gestioneController;
    private CorsoCucina corso;

    public void setController(GestioneCorsoController controller, CorsoCucina corso) {
        this.gestioneController = controller;
        this.corso = corso;
    }

    public void start(Stage stage) {
        if (gestioneController == null || corso == null) {
            throw new IllegalStateException("Controller o corso non impostati!");
        }

        stage.setTitle("Dettagli Corso: " + corso.getNomeCorso());

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        // Campi del corso
        TextField nomeField = new TextField(corso.getNomeCorso());
        TextField prezzoField = new TextField(String.valueOf(corso.getPrezzo()));
        TextField argomentoField = new TextField(corso.getArgomento());
        ComboBox<Frequenza> frequenzaCombo = new ComboBox<>();
        frequenzaCombo.getItems().setAll(Frequenza.values());
        frequenzaCombo.setValue(corso.getFrequenzaCorso());
        TextField numeroPostiField = new TextField(String.valueOf(corso.getNumeroPosti()));
        TextField numeroSessioniField = new TextField(String.valueOf(corso.getNumeroSessioni()));
        DatePicker dataInizioPicker = new DatePicker(corso.getDataInizioCorso().toLocalDate());
        DatePicker dataFinePicker = new DatePicker(corso.getDataFineCorso().toLocalDate());

        // Blocca inizialmente i campi
        nomeField.setEditable(false);
        prezzoField.setEditable(false);
        argomentoField.setEditable(false);
        frequenzaCombo.setDisable(true);
        numeroPostiField.setEditable(false);
        numeroSessioniField.setEditable(false);
        dataInizioPicker.setDisable(true);
        dataFinePicker.setDisable(true);

        Button modificaBtn = new Button("Modifica corso");
        Button salvaBtn = new Button("Salva modifiche");
        Button gestisciSessioniBtn = new Button("Gestisci sessioni");
        Button chiudiBtn = new Button("Chiudi");

        salvaBtn.setDisable(true); // abilita solo dopo clic Modifica

        root.getChildren().addAll(
            new Label("Nome:"), nomeField,
            new Label("Prezzo:"), prezzoField,
            new Label("Argomento:"), argomentoField,
            new Label("Frequenza:"), frequenzaCombo,
            new Label("Numero posti:"), numeroPostiField,
            new Label("Numero sessioni:"), numeroSessioniField,
            new Label("Data inizio:"), dataInizioPicker,
            new Label("Data fine:"), dataFinePicker,
            modificaBtn, salvaBtn, gestisciSessioniBtn, chiudiBtn
        );

        // --- Pulsante Modifica ---
        modificaBtn.setOnAction(e -> {
            nomeField.setEditable(true);
            prezzoField.setEditable(true);
            argomentoField.setEditable(true);
            frequenzaCombo.setDisable(false);
            numeroPostiField.setEditable(true);
            numeroSessioniField.setEditable(true);
            dataInizioPicker.setDisable(false);
            dataFinePicker.setDisable(false);
            salvaBtn.setDisable(false);
        });

        // --- Pulsante Salva ---
        salvaBtn.setOnAction(e -> {
            try {
                double prezzo = Double.parseDouble(prezzoField.getText().replace(',', '.'));
                int posti = Integer.parseInt(numeroPostiField.getText());
                int sessioni = Integer.parseInt(numeroSessioniField.getText());
                if (dataInizioPicker.getValue().isAfter(dataFinePicker.getValue())) {
                    showAlert("Errore", "La data di inizio deve precedere la data di fine.");
                    return;
                }

                Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
                conferma.setTitle("Conferma modifica");
                conferma.setHeaderText("Sei sicuro di voler salvare le modifiche?");
                conferma.setContentText(
                        "Nome: " + nomeField.getText() +
                        "\nPrezzo: " + prezzo +
                        "\nPosti: " + posti +
                        "\nSessioni: " + sessioni
                );

                conferma.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            corso.setNomeCorso(nomeField.getText());
                            corso.setPrezzo(prezzo);
                            corso.setArgomento(argomentoField.getText());
                            corso.setFrequenzaCorso(frequenzaCombo.getValue());
                            corso.setNumeroPosti(posti);
                            corso.setNumeroSessioni(sessioni);
                            corso.setDataInizioCorso(dataInizioPicker.getValue().atStartOfDay());
                            corso.setDataFineCorso(dataFinePicker.getValue().atStartOfDay());

                            gestioneController.modificaCorso(corso);
                            showAlert("Successo", "Corso modificato correttamente!");

                            // Blocca di nuovo i campi
                            nomeField.setEditable(false);
                            prezzoField.setEditable(false);
                            argomentoField.setEditable(false);
                            frequenzaCombo.setDisable(true);
                            numeroPostiField.setEditable(false);
                            numeroSessioniField.setEditable(false);
                            dataInizioPicker.setDisable(true);
                            dataFinePicker.setDisable(true);
                            salvaBtn.setDisable(true);

                        } catch (Exception ex) {
                            showAlert("Errore", "Errore nel salvataggio: " + ex.getMessage());
                        }
                    }
                });

            } catch (NumberFormatException ex) {
                showAlert("Errore", "Inserisci valori numerici validi per prezzo, posti e sessioni.");
            }
        });

        // --- Pulsante Gestione Sessioni ---
        gestisciSessioniBtn.setOnAction(e -> {
            if (corso.getSessioni().size() != corso.getNumeroSessioni()) {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Attenzione numero sessioni");
                info.setHeaderText("Il numero di sessioni dichiarato e quelle effettive non coincidono!");
                info.setContentText(
                        "Numero dichiarato: " + corso.getNumeroSessioni() +
                        "\nNumero effettivo: " + corso.getSessioni().size() +
                        "\nIl sistema aggiornerà il numero di sessioni al numero attuale."
                );
                info.showAndWait();

                corso.setNumeroSessioni(corso.getSessioni().size());
            }

            GestioneSessioniGUI sessioniGUI = new GestioneSessioniGUI();
            sessioniGUI.setCorso(corso);
            sessioniGUI.start(new Stage());
        });

        chiudiBtn.setOnAction(e -> stage.close());

        stage.setScene(new Scene(root, 450, 600));
        stage.show();
    }

    private void showAlert(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
