package Gui;

import controller.GestioneCorsoController;
import controller.VisualizzaCorsiController;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.CorsoCucina;
import java.sql.SQLException;
import java.util.List;

public class VisualizzaCorsiGUI {

    private VisualizzaCorsiController visualizzaController;
    private GestioneCorsoController gestioneCorsoController;

    private ObservableList<CorsoCucina> corsiData = FXCollections.observableArrayList();

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
        TextField nomeField = new TextField();
        nomeField.setPromptText("Cerca corso per nome");
        TextField argomentoField = new TextField();
        argomentoField.setPromptText("Cerca per argomento");

        // TableView
        TableView<CorsoCucina> table = new TableView<>();

        TableColumn<CorsoCucina, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getIdCorso()).asObject());

        TableColumn<CorsoCucina, String> nomeCol = new TableColumn<>("Nome Corso");
        nomeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomeCorso()));

        TableColumn<CorsoCucina, Integer> iscrittiCol = new TableColumn<>("Iscritti");
        iscrittiCol.setCellValueFactory(c -> new SimpleIntegerProperty(
                c.getValue().getIscrizioni() != null ? c.getValue().getIscrizioni().size() : 0
        ).asObject());

        TableColumn<CorsoCucina, String> argomentoCol = new TableColumn<>("Argomento");
        argomentoCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getArgomento()));

        TableColumn<CorsoCucina, Double> prezzoCol = new TableColumn<>("Prezzo");
        prezzoCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrezzo()).asObject());

        TableColumn<CorsoCucina, Integer> sessioniCol = new TableColumn<>("Sessioni");
        sessioniCol.setCellValueFactory(c -> new SimpleIntegerProperty(
                c.getValue().getSessioni() != null ? c.getValue().getSessioni().size() : 0
        ).asObject());
        
        table.getColumns().addAll(idCol, nomeCol, iscrittiCol, argomentoCol, prezzoCol, sessioniCol);
        table.setItems(corsiData);

        // Pulsanti
        Button mostraTuttiBtn = new Button("Mostra tutti i corsi");
        Button mieiBtn = new Button("I miei corsi");
        Button chiudiBtn = new Button("Chiudi");

        root.getChildren().addAll(nomeField, argomentoField, mostraTuttiBtn, mieiBtn, table, chiudiBtn);

        // Carica dati iniziali
        caricaCorsi();

        // Filtri live
        nomeField.setOnKeyReleased(e -> filtraCorsi(nomeField.getText(), argomentoField.getText(), false));
        argomentoField.setOnKeyReleased(e -> filtraCorsi(nomeField.getText(), argomentoField.getText(), false));

        // Mostra tutti
        mostraTuttiBtn.setOnAction(e -> {
            nomeField.clear();
            argomentoField.clear();
            filtraCorsi("", "", false);
        });

        // Mostra corsi chef loggato
        mieiBtn.setOnAction(e -> filtraCorsi("", "", true));

        // Apri dettagli corso
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                CorsoCucina corsoSelezionato = table.getSelectionModel().getSelectedItem();
                if (corsoSelezionato != null) {
                    try {
                        // Carica corso completo con sessioni, iscrizioni e chef
                        CorsoCucina corsoCompleto = gestioneCorsoController.getCorsoCompleto(corsoSelezionato.getIdCorso());

                        DettagliCorsoGUI dettagliGUI = new DettagliCorsoGUI();
                        dettagliGUI.setController(gestioneCorsoController, corsoCompleto);
                        dettagliGUI.start(new Stage());

                    } catch (SQLException ex) {
                        showAlert("Errore", "Impossibile aprire il corso: " + ex.getMessage());
                    }
                }
            }
        });

        chiudiBtn.setOnAction(e -> stage.close());

        stage.setScene(new Scene(root, 800, 500));
        stage.show();
    }

    private void caricaCorsi() {
        try {
            corsiData.clear();
            corsiData.addAll(gestioneCorsoController.getTuttiICorsiCompleti());
        } catch (SQLException ex) {
            showAlert("Errore", "Impossibile caricare i corsi: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void filtraCorsi(String nomeFiltro, String argomentoFiltro, boolean soloChefLoggato) {
        caricaCorsi();
        if (soloChefLoggato) {
            try {
                List<CorsoCucina> miei = visualizzaController.getCorsiChefLoggato();
                corsiData.retainAll(miei);
            } catch (SQLException ex) {
                showAlert("Errore", "Impossibile filtrare i corsi dello chef: " + ex.getMessage());
            }
        }
        if (nomeFiltro != null && !nomeFiltro.isEmpty()) {
            corsiData.removeIf(c -> !c.getNomeCorso().toLowerCase().contains(nomeFiltro.toLowerCase()));
        }
        if (argomentoFiltro != null && !argomentoFiltro.isEmpty()) {
            corsiData.removeIf(c -> !c.getArgomento().toLowerCase().contains(argomentoFiltro.toLowerCase()));
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
