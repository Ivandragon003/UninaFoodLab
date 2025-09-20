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

    // Cache dei corsi caricati dal DB
    private List<CorsoCucina> cacheCorsi = new ArrayList<>();

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

        // Campi di ricerca
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

        // --- Caricamento iniziale della cache ---
        try {
            cacheCorsi.addAll(visualizzaController.getTuttiICorsi());
        } catch (SQLException ex) {
            showAlert("Errore", "Impossibile caricare i corsi: " + ex.getMessage());
        }

        // Listener ricerca live
        nomeField.setOnKeyReleased(e -> aggiornaLista(corsiList, nomeField.getText(), argomentoField.getText(), false));
        argomentoField.setOnKeyReleased(e -> aggiornaLista(corsiList, nomeField.getText(), argomentoField.getText(), false));

        // Mostra tutti corsi
        mostraTuttiBtn.setOnAction(e -> {
            nomeField.clear();
            argomentoField.clear();
            aggiornaLista(corsiList, "", "", false);
        });

        // Mostra solo corsi dello chef loggato
        mieiBtn.setOnAction(e -> {
            nomeField.clear();
            argomentoField.clear();
            aggiornaLista(corsiList, "", "", true);
        });

        // Apri dettagli corso
        corsiList.setOnMouseClicked(event -> {
            int idx = corsiList.getSelectionModel().getSelectedIndex();
            if (idx >= 0) {
                CorsoCucina corsoSelezionato = (CorsoCucina) corsiList.getUserData(); // userData aggiornato nella lista
                DettagliCorsoGUI dettagliGUI = new DettagliCorsoGUI();
                dettagliGUI.setController(gestioneCorsoController, corsoSelezionato);
                dettagliGUI.start(new Stage());
            }
        });

        tornaIndietroBtn.setOnAction(e -> stage.close());

        // Popola lista iniziale
        aggiornaLista(corsiList, "", "", false);

        stage.setScene(new Scene(root, 700, 500));
        stage.show();
    }

    private void aggiornaLista(ListView<String> listView, String nomeFiltro, String argomentoFiltro, boolean soloChefLoggato) {
        List<CorsoCucina> corsiFiltrati = new ArrayList<>(cacheCorsi);

        // Filtra per chef loggato
        if (soloChefLoggato) {
            try {
                List<CorsoCucina> miei = visualizzaController.getCorsiChefLoggato();
                corsiFiltrati.retainAll(miei);
            } catch (SQLException ex) {
                showAlert("Errore", "Impossibile filtrare i corsi dello chef: " + ex.getMessage());
            }
        }

        // Filtro per nome
        if (nomeFiltro != null && !nomeFiltro.isEmpty()) {
            corsiFiltrati.removeIf(c -> !c.getNomeCorso().toLowerCase().contains(nomeFiltro.toLowerCase()));
        }

        // Filtro per argomento
        if (argomentoFiltro != null && !argomentoFiltro.isEmpty()) {
            corsiFiltrati.removeIf(c -> !c.getArgomento().toLowerCase().contains(argomentoFiltro.toLowerCase()));
        }

        // Costruisci lista di stringhe
        List<String> items = new ArrayList<>();
        for (CorsoCucina c : corsiFiltrati) {
            String text = String.format(
                    "%s | Partecipanti: %d | Argomento: %s | Prezzo: %.2f | Sessioni: %d",
                    c.getNomeCorso(),
                    c.getIscrizioni().size(),
                    c.getArgomento(),
                    c.getPrezzo(),
                    c.getNumeroSessioni()
            );
            items.add(text);
        }

        listView.getItems().setAll(items);
        // Salviamo i corsi filtrati come UserData
        listView.setUserData(corsiFiltrati.isEmpty() ? null : corsiFiltrati.get(0));
    }

    private void showAlert(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
