package Gui;

import controller.GestioneCorsoController;
import controller.VisualizzaCorsiController;
import model.CorsoCucina;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VisualizzaCorsiGUI {

    private VisualizzaCorsiController visualizzaController;
    private GestioneCorsoController gestioneCorsoController;

    public void setControllers(VisualizzaCorsiController visualizzaController,
                               GestioneCorsoController gestioneCorsoController) {
        this.visualizzaController = visualizzaController;
        this.gestioneCorsoController = gestioneCorsoController;
    }

    public void start(Stage stage) {
        if (visualizzaController == null || gestioneCorsoController == null) {
            throw new IllegalStateException("Controllers non impostati!");
        }

        stage.setTitle("Visualizza Corsi");

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        // Campi di ricerca live
        Label nomeLabel = new Label("Cerca corso per nome:");
        TextField nomeField = new TextField();
        nomeField.setPromptText("Digita il nome del corso");

        Label argomentoLabel = new Label("Cerca per argomento:");
        TextField argomentoField = new TextField();
        argomentoField.setPromptText("Digita l'argomento");

        ListView<String> corsiList = new ListView<>();

        // Pulsanti
        Button mostraTuttiBtn = new Button("Mostra tutti i corsi");
        Button mieiBtn = new Button("I miei corsi");
        Button tornaIndietroBtn = new Button("Torna indietro");

        root.getChildren().addAll(nomeLabel, nomeField, argomentoLabel, argomentoField,
                mostraTuttiBtn, mieiBtn, corsiList, tornaIndietroBtn);

        // Listener per ricerca live su nome e argomento
        nomeField.setOnKeyReleased(e -> {
            String nome = nomeField.getText().trim();
            String argomento = argomentoField.getText().trim();
            caricaCorsi(corsiList, true, false, nome, argomento);
        });

        argomentoField.setOnKeyReleased(e -> {
            String nome = nomeField.getText().trim();
            String argomento = argomentoField.getText().trim();
            caricaCorsi(corsiList, true, false, nome, argomento);
        });

        // Mostra tutti corsi + quelli dello chef loggato
        mostraTuttiBtn.setOnAction(e -> {
            nomeField.clear();
            argomentoField.clear();
            caricaCorsi(corsiList, true, true);
        });

        // Mostra solo corsi dello chef loggato
        mieiBtn.setOnAction(e -> {
            nomeField.clear();
            argomentoField.clear();
            caricaCorsi(corsiList, false, true);
        });

        // Apri dettagli corso
        corsiList.setOnMouseClicked(event -> {
            int idx = corsiList.getSelectionModel().getSelectedIndex();
            if (idx >= 0) {
                try {
                    List<CorsoCucina> corsi = visualizzaController.getTuttiICorsi();
                    CorsoCucina corsoSelezionato = corsi.get(idx);
                    DettagliCorsoGUI dettagliGUI = new DettagliCorsoGUI();
                    dettagliGUI.setController(gestioneCorsoController, corsoSelezionato);
                    dettagliGUI.start(new Stage());
                } catch (SQLException ex) {
                    showAlert("Errore", "Impossibile aprire dettagli: " + ex.getMessage());
                }
            }
        });

        tornaIndietroBtn.setOnAction(e -> stage.close());

        stage.setScene(new Scene(root, 700, 500));
        stage.show();
    }


    // Versione generica che gestisce ricerca/filtro
    private void caricaCorsi(ListView<String> listView, boolean tutti, boolean soloChefLoggato) {
        caricaCorsi(listView, tutti, soloChefLoggato, null, null);
    }

    private void caricaCorsi(ListView<String> listView, boolean tutti, boolean soloChefLoggato,
                              String nomeCercato, String argomentoFiltro) {
        try {
            List<CorsoCucina> corsi = new ArrayList<>();
            if (tutti) {
                corsi.addAll(visualizzaController.getTuttiICorsi());
            }
            if (soloChefLoggato) {
                List<CorsoCucina> miei = visualizzaController.getCorsiChefLoggato();
                // evitiamo duplicati
                for (CorsoCucina c : miei) {
                    if (!corsi.contains(c)) corsi.add(0, c); // mettiamo all'inizio
                }
            }

            // Applica ricerca
            if (nomeCercato != null && !nomeCercato.isEmpty()) {
                corsi.removeIf(c -> !c.getNomeCorso().toLowerCase().contains(nomeCercato.toLowerCase()));
            }

            // Applica filtro
            if (argomentoFiltro != null && !argomentoFiltro.isEmpty()) {
                corsi.removeIf(c -> !c.getArgomento().toLowerCase().contains(argomentoFiltro.toLowerCase()));
            }

            // Costruisci stringhe di visualizzazione
            List<String> items = new ArrayList<>();
            for (CorsoCucina c : corsi) {
                String text = String.format(
                        "%s | Partecipanti: %d | Argomento: %s | Prezzo: %.2f | Sessioni: %d | Ricette: %d",
                        c.getNomeCorso(),
                        c.getIscrizioni().size(),
                        c.getArgomento(),
                        c.getPrezzo(),
                        c.getNumeroSessioni(),
                        visualizzaController.getNumeroRicettePerCorso(c)
                );
                items.add(text);
            }

            listView.getItems().setAll(items);

        } catch (SQLException ex) {
            showAlert("Errore", "Impossibile caricare i corsi: " + ex.getMessage());
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
