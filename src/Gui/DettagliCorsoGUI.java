package Gui;

import controller.GestioneCorsoController;
import model.CorsoCucina;
import model.Frequenza;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DettagliCorsoGUI {

    private GestioneCorsoController gestioneController;
    private CorsoCucina corso;

    public void setController(GestioneCorsoController controller) {
        this.gestioneController = controller;
    }

    public void setCorso(CorsoCucina corso) {
        this.corso = corso;
    }

    // Restituisce il VBox principale da inserire nel menuRoot
    public VBox getRoot() {
        if (gestioneController == null || corso == null) {
            throw new IllegalStateException("Controller o corso non impostati!");
        }

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        // --- Campi del corso ---
        TextField nomeField = new TextField(corso.getNomeCorso());
        TextField prezzoField = new TextField(String.valueOf(corso.getPrezzo()));
        TextField argomentoField = new TextField(corso.getArgomento());
        ComboBox<Frequenza> frequenzaCombo = new ComboBox<>();
        frequenzaCombo.getItems().setAll(Frequenza.values());
        frequenzaCombo.setValue(corso.getFrequenzaCorso());
        TextField numeroPostiField = new TextField(String.valueOf(corso.getNumeroPosti()));
        TextField numeroSessioniField = new TextField(String.valueOf(corso.getNumeroSessioni()));
        numeroSessioniField.setEditable(false);
        numeroSessioniField.setStyle("-fx-control-inner-background: #E0E0E0;"); // grigio

        // Nota: se dataInizioCorso o dataFineCorso sono nulli, attenzione!
        DatePicker dataInizioPicker = new DatePicker(corso.getDataInizioCorso() != null ? corso.getDataInizioCorso().toLocalDate() : null);
        DatePicker dataFinePicker = new DatePicker(corso.getDataFineCorso() != null ? corso.getDataFineCorso().toLocalDate() : null);

        // Blocca inizialmente i campi
        nomeField.setEditable(false);
        prezzoField.setEditable(false);
        argomentoField.setEditable(false);
        frequenzaCombo.setDisable(true);
        numeroPostiField.setEditable(false);
        dataInizioPicker.setDisable(true);
        dataFinePicker.setDisable(true);

        // Pulsanti
        Button modificaBtn = new Button("Modifica corso");
        Button salvaBtn = new Button("Salva modifiche");
        Button visualizzaSessioniBtn = new Button("Visualizza sessioni");
        Button tornaIndietroBtn = new Button("⬅ Torna indietro");
        salvaBtn.setDisable(true); // abilita solo dopo clic Modifica

        HBox pulsantiBox = new HBox(10, modificaBtn, salvaBtn, visualizzaSessioniBtn, tornaIndietroBtn);
        pulsantiBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(
            new Label("Nome:"), nomeField,
            new Label("Prezzo:"), prezzoField,
            new Label("Argomento:"), argomentoField,
            new Label("Frequenza:"), frequenzaCombo,
            new Label("Numero posti:"), numeroPostiField,
            new Label("Numero sessioni:"), numeroSessioniField,
            new Label("Data inizio:"), dataInizioPicker,
            new Label("Data fine:"), dataFinePicker,
            pulsantiBox
        );

        // Modifica corso
        modificaBtn.setOnAction(e -> {
            nomeField.setEditable(true);
            prezzoField.setEditable(true);
            argomentoField.setEditable(true);
            frequenzaCombo.setDisable(false);
            numeroPostiField.setEditable(true);
            dataInizioPicker.setDisable(false);
            dataFinePicker.setDisable(false);
            salvaBtn.setDisable(false);
        });

        // Salva modifiche
        salvaBtn.setOnAction(e -> {
            try {
                double prezzo = Double.parseDouble(prezzoField.getText().replace(',', '.'));
                int posti = Integer.parseInt(numeroPostiField.getText());

                if (dataInizioPicker.getValue() != null && dataFinePicker.getValue() != null && dataInizioPicker.getValue().isAfter(dataFinePicker.getValue())) {
                    showAlert("Errore", "La data di inizio deve precedere la data di fine.");
                    return;
                }

                corso.setNomeCorso(nomeField.getText());
                corso.setPrezzo(prezzo);
                corso.setArgomento(argomentoField.getText());
                corso.setFrequenzaCorso(frequenzaCombo.getValue());
                corso.setNumeroPosti(posti);
                corso.setNumeroSessioni(corso.getSessioni().size());
                if (dataInizioPicker.getValue() != null) corso.setDataInizioCorso(dataInizioPicker.getValue().atStartOfDay());
                if (dataFinePicker.getValue() != null) corso.setDataFineCorso(dataFinePicker.getValue().atStartOfDay());

                gestioneController.modificaCorso(corso);
                numeroSessioniField.setText(String.valueOf(corso.getNumeroSessioni()));

                showAlert("Successo", "Corso modificato correttamente!");

                // Blocca di nuovo i campi
                nomeField.setEditable(false);
                prezzoField.setEditable(false);
                argomentoField.setEditable(false);
                frequenzaCombo.setDisable(true);
                numeroPostiField.setEditable(false);
                dataInizioPicker.setDisable(true);
                dataFinePicker.setDisable(true);
                salvaBtn.setDisable(true);

            } catch (NumberFormatException ex) {
                showAlert("Errore", "Valori numerici non validi per prezzo o posti.");
            } catch (Exception ex) {
                showAlert("Errore", "Errore nel salvataggio: " + ex.getMessage());
            }
        });

        // Visualizza sessioni -> qui integriamo la GestioneSessioniGUI usando getRoot()
        visualizzaSessioniBtn.setOnAction(e -> {
            GestioneSessioniGUI sessioniGUI = new GestioneSessioniGUI();
            sessioniGUI.setCorso(corso);
            // Se hai un controller per le sessioni, impostalo:
            // sessioniGUI.setController(new GestioneSessioniController(...));
            VBox sessioniRoot = sessioniGUI.getRoot();
            // Sostituisco il contenuto di root con quello di sessioni
            root.getChildren().setAll(sessioniRoot.getChildren());
            numeroSessioniField.setText(String.valueOf(corso.getNumeroSessioni()));
        });

        // Torna indietro
        tornaIndietroBtn.setOnAction(e -> {
            root.getChildren().clear(); 
        });

        return root;
    }

    private void showAlert(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
