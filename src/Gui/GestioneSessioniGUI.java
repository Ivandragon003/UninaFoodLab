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

        // Selezione tipo sessione
        ComboBox<String> tipoSessioneCombo = new ComboBox<>();
        tipoSessioneCombo.getItems().addAll("Online", "In Presenza");
        tipoSessioneCombo.setValue("Online");

        // Campi comuni
        DatePicker dataInizioPicker = new DatePicker();
        DatePicker dataFinePicker = new DatePicker();

        // Campi online
        TextField piattaformaField = new TextField();

        // Campi in presenza
        TextField viaField = new TextField();
        TextField cittaField = new TextField();
        TextField postiField = new TextField();
        TextField capField = new TextField();

        Button aggiungiBtn = new Button("Aggiungi sessione");
        Button eliminaBtn = new Button("Elimina sessione selezionata");
        Button verificaBtn = new Button("Verifica numero sessioni");
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
                aggiungiBtn, eliminaBtn, verificaBtn, chiudiBtn
        );

        // Cambia visibilità campi a seconda del tipo
        tipoSessioneCombo.setOnAction(e -> {
            boolean isOnline = tipoSessioneCombo.getValue().equals("Online");
            piattaformaField.setDisable(!isOnline);
            viaField.setDisable(isOnline);
            cittaField.setDisable(isOnline);
            postiField.setDisable(isOnline);
            capField.setDisable(isOnline);
        });
        tipoSessioneCombo.fireEvent(new javafx.event.ActionEvent()); // aggiorna visibilità iniziale

        aggiungiBtn.setOnAction(e -> {
            try {
                LocalDateTime inizio = dataInizioPicker.getValue().atStartOfDay();
                LocalDateTime fine = dataFinePicker.getValue().atStartOfDay();

                if (tipoSessioneCombo.getValue().equals("Online")) {
                    String piattaforma = piattaformaField.getText();
                    Online s = new Online(inizio, fine, piattaforma);
                    controller.aggiungiSessione(s);
                } else {
                    String via = viaField.getText();
                    String citta = cittaField.getText();
                    int posti = Integer.parseInt(postiField.getText());
                    int cap = Integer.parseInt(capField.getText());
                    InPresenza s = new InPresenza(inizio, fine, via, citta, posti, cap);
                    controller.aggiungiSessione(s);
                }
                aggiornaLista(sessioniList);
            } catch (Exception ex) {
                showAlert("Errore", "Inserimento sessione fallito: " + ex.getMessage());
            }
        });

        eliminaBtn.setOnAction(e -> {
            int idx = sessioniList.getSelectionModel().getSelectedIndex();
            if (idx >= 0) {
                controller.eliminaSessione(controller.getSessioni().get(idx));
                aggiornaLista(sessioniList);
            }
        });

        verificaBtn.setOnAction(e -> {
            if (controller.verificaNumeroSessioni()) {
                showAlert("OK", "Il numero di sessioni corrisponde a quanto previsto.");
            } else {
                showAlert("Attenzione", "Numero di sessioni non corrisponde! Aggiorna per raggiungere la soglia.");
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
