package Gui;

import controller.GestioneCorsoController;
import model.CorsoCucina;
import model.Frequenza;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;


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

        Button modificaBtn = new Button("Modifica corso");
        Button eliminaBtn = new Button("Elimina corso");
        Button gestisciSessioniBtn = new Button("Gestisci sessioni");
        Button indietroBtn = new Button("Indietro");

        root.getChildren().addAll(
            new Label("Nome:"), nomeField,
            new Label("Prezzo:"), prezzoField,
            new Label("Argomento:"), argomentoField,
            new Label("Frequenza:"), frequenzaCombo,
            new Label("Numero posti:"), numeroPostiField,
            new Label("Numero sessioni:"), numeroSessioniField,
            new Label("Data inizio:"), dataInizioPicker,
            new Label("Data fine:"), dataFinePicker,
            modificaBtn, eliminaBtn, gestisciSessioniBtn, indietroBtn
        );

        // Modifica corso
        modificaBtn.setOnAction(e -> {
            try {
                corso.setNomeCorso(nomeField.getText());
                corso.setPrezzo(Double.parseDouble(prezzoField.getText()));
                corso.setArgomento(argomentoField.getText());
                corso.setFrequenzaCorso(frequenzaCombo.getValue());
                corso.setNumeroPosti(Integer.parseInt(numeroPostiField.getText()));
                corso.setNumeroSessioni(Integer.parseInt(numeroSessioniField.getText()));
                corso.setDataInizioCorso(dataInizioPicker.getValue().atStartOfDay());
                corso.setDataFineCorso(dataFinePicker.getValue().atStartOfDay());

                gestioneController.modificaCorso(corso);
                showAlert("Successo", "Corso modificato correttamente!");
            } catch (Exception ex) {
                showAlert("Errore", "Modifica fallita: " + ex.getMessage());
            }
        });

        // Elimina corso
        eliminaBtn.setOnAction(e -> {
            try {
                gestioneController.eliminaCorso(corso.getIdCorso());
                showAlert("Successo", "Corso eliminato correttamente!");
                stage.close();
            } catch (SQLException ex) {
                showAlert("Errore", "Eliminazione fallita: " + ex.getMessage());
            }
        });

        // Gestione sessioni
        gestisciSessioniBtn.setOnAction(e -> {
            GestioneSessioniGUI sessioniGUI = new GestioneSessioniGUI();
            sessioniGUI.setCorso(corso);
            sessioniGUI.start(new Stage());
        });

        // Indietro
        indietroBtn.setOnAction(e -> stage.close());

        stage.setScene(new Scene(root, 400, 600));
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
