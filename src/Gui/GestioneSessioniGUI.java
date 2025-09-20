package Gui;

import controller.GestioneSessioniController;
import model.CorsoCucina;
import model.Sessione;
import model.Online;
import model.InPresenza;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.time.LocalDateTime;

public class GestioneSessioniGUI {

    private CorsoCucina corso;
    private GestioneSessioniController controller;

    public void setCorso(CorsoCucina corso) {
        this.corso = corso;
        this.controller = new GestioneSessioniController(corso);
    }

    public void start(Stage stage) {
        stage.setTitle("Gestione Sessioni: " + corso.getNomeCorso());

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        ListView<String> sessioniList = new ListView<>();
        aggiornaLista(sessioniList);

        ComboBox<String> tipoSessioneCombo = new ComboBox<>();
        tipoSessioneCombo.getItems().addAll("Online", "In Presenza");
        tipoSessioneCombo.setValue("Online");

        DatePicker dataInizioPicker = new DatePicker();
        DatePicker dataFinePicker = new DatePicker();

        TextField piattaformaField = new TextField();
        TextField viaField = new TextField();
        TextField cittaField = new TextField();
        TextField postiField = new TextField();
        TextField capField = new TextField();

        Button aggiungiBtn = new Button("Aggiungi sessione");
        Button eliminaBtn = new Button("Elimina sessione selezionata");
        Button chiudiBtn = new Button("Chiudi");

        root.getChildren().addAll(
                sessioniList,
                new Label("Tipo sessione:"), tipoSessioneCombo,
                new Label("Data inizio:"), dataInizioPicker,
                new Label("Data fine:"), dataFinePicker,
                new Label("Piattaforma (online):"), piattaformaField,
                new Label("Via (in presenza):"), viaField,
                new Label("Città (in presenza):"), cittaField,
                new Label("Posti (in presenza):"), postiField,
                new Label("CAP (in presenza):"), capField,
                aggiungiBtn, eliminaBtn, chiudiBtn
        );

        // Visibilità campi secondo tipo
        tipoSessioneCombo.setOnAction(e -> {
            boolean isOnline = tipoSessioneCombo.getValue().equals("Online");
            piattaformaField.setDisable(!isOnline);
            viaField.setDisable(isOnline);
            cittaField.setDisable(isOnline);
            postiField.setDisable(isOnline);
            capField.setDisable(isOnline);
        });
        tipoSessioneCombo.fireEvent(new javafx.event.ActionEvent());

        // --- Aggiungi sessione ---
        aggiungiBtn.setOnAction(e -> {
            try {
                LocalDateTime inizio = dataInizioPicker.getValue().atStartOfDay();
                LocalDateTime fine = dataFinePicker.getValue().atStartOfDay();

                Sessione nuova;
                if (tipoSessioneCombo.getValue().equals("Online")) {
                    nuova = new Online(inizio, fine, piattaformaField.getText());
                } else {
                    nuova = new InPresenza(
                            inizio,
                            fine,
                            viaField.getText(),
                            cittaField.getText(),
                            Integer.parseInt(postiField.getText()),
                            Integer.parseInt(capField.getText())
                    );
                }
                controller.aggiungiSessione(nuova);
                aggiornaLista(sessioniList);

                // Aggiorna numero sessioni corso
                corso.setNumeroSessioni(corso.getSessioni().size());

            } catch (Exception ex) {
                showAlert("Errore", "Inserimento sessione fallito: " + ex.getMessage());
            }
        });

        // --- Elimina sessione ---
        eliminaBtn.setOnAction(e -> {
            int idx = sessioniList.getSelectionModel().getSelectedIndex();
            if (idx >= 0) {
                controller.eliminaSessione(controller.getSessioni().get(idx));
                aggiornaLista(sessioniList);
                corso.setNumeroSessioni(corso.getSessioni().size());
            }
        });

        chiudiBtn.setOnAction(e -> stage.close());

        stage.setScene(new Scene(root, 500, 600));
        stage.show();
    }

    private void aggiornaLista(ListView<String> list) {
        list.getItems().clear();
        int i = 1;
        for (Sessione s : controller.getSessioni()) {
            String tipo = s instanceof Online ? "Online" : "In Presenza";
            list.getItems().add("Sessione " + i++ + " (" + tipo + "): " + s.getDataInizioSessione());
        }
    }

    private void showAlert(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
