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
import javafx.scene.layout.Priority;
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

        ListView<Sessione> sessioniList = new ListView<>();
        aggiornaLista(sessioniList);

        // Permette scroll verticale
        VBox.setVgrow(sessioniList, Priority.ALWAYS);

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
        Button salvaBtn = new Button("Salva tutte le modifiche");
        Button chiudiBtn = new Button("Chiudi");

        root.getChildren().addAll(
                new Label("Elenco sessioni:"), sessioniList,
                new Label("Tipo sessione:"), tipoSessioneCombo,
                new Label("Data inizio:"), dataInizioPicker,
                new Label("Data fine:"), dataFinePicker,
                new Label("Piattaforma (online):"), piattaformaField,
                new Label("Via (in presenza):"), viaField,
                new Label("Città (in presenza):"), cittaField,
                new Label("Posti (in presenza):"), postiField,
                new Label("CAP (in presenza):"), capField,
                aggiungiBtn, salvaBtn, chiudiBtn
        );

        tipoSessioneCombo.setOnAction(e -> {
            boolean isOnline = tipoSessioneCombo.getValue().equals("Online");
            piattaformaField.setDisable(!isOnline);
            viaField.setDisable(isOnline);
            cittaField.setDisable(isOnline);
            postiField.setDisable(isOnline);
            capField.setDisable(isOnline);
        });
        tipoSessioneCombo.fireEvent(new javafx.event.ActionEvent());

        // Clic su sessione per modificare i campi
        sessioniList.setCellFactory(lv -> {
            ListCell<Sessione> cell = new ListCell<>() {
                @Override
                protected void updateItem(Sessione item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String tipo = item instanceof Online ? "Online" : "In Presenza";
                        setText(tipo + " - " + item.getDataInizioSessione());
                    }
                }
            };
            cell.setOnMouseClicked(e -> {
                if (!cell.isEmpty()) {
                    Sessione s = cell.getItem();
                    dataInizioPicker.setValue(s.getDataInizioSessione().toLocalDate());
                    dataFinePicker.setValue(s.getDataFineSessione().toLocalDate());
                    if (s instanceof Online) {
                        tipoSessioneCombo.setValue("Online");
                        piattaformaField.setText(((Online) s).getPiattaformaStreaming());
                    } else {
                        tipoSessioneCombo.setValue("In Presenza");
                        InPresenza ip = (InPresenza) s;
                        viaField.setText(ip.getVia());
                        cittaField.setText(ip.getCitta());
                        postiField.setText(String.valueOf(ip.getNumeroPosti()));
                        capField.setText(String.valueOf(ip.getCAP()));
                    }
                    sessioniList.getSelectionModel().select(s);
                }
            });
            return cell;
        });

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
                corso.setNumeroSessioni(corso.getSessioni().size());
            } catch (Exception ex) {
                showAlert("Errore", "Inserimento sessione fallito: " + ex.getMessage());
            }
        });

        salvaBtn.setOnAction(e -> {
            Sessione selezionata = sessioniList.getSelectionModel().getSelectedItem();
            if (selezionata != null) {
                try {
                    LocalDateTime inizio = dataInizioPicker.getValue().atStartOfDay();
                    LocalDateTime fine = dataFinePicker.getValue().atStartOfDay();
                    Sessione aggiornata;
                    if (tipoSessioneCombo.getValue().equals("Online")) {
                        aggiornata = new Online(inizio, fine, piattaformaField.getText());
                    } else {
                        aggiornata = new InPresenza(
                                inizio,
                                fine,
                                viaField.getText(),
                                cittaField.getText(),
                                Integer.parseInt(postiField.getText()),
                                Integer.parseInt(capField.getText())
                        );
                    }
                    controller.eliminaSessione(selezionata);
                    controller.aggiungiSessione(aggiornata);
                    aggiornaLista(sessioniList);
                    corso.setNumeroSessioni(corso.getSessioni().size());
                } catch (Exception ex) {
                    showAlert("Errore", "Salvataggio fallito: " + ex.getMessage());
                }
            } else {
                showAlert("Attenzione", "Seleziona una sessione da modificare prima di salvare.");
            }
        });

        chiudiBtn.setOnAction(e -> stage.close());

        stage.setScene(new Scene(root, 500, 600));
        stage.show();
    }

    private void aggiornaLista(ListView<Sessione> list) {
        list.getItems().clear();
        list.getItems().addAll(controller.getSessioni());
    }

    private void showAlert(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
