package Gui;

import controller.GestioneSessioniController;
import model.*;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Comparator;

public class VisualizzaSessioniGUI {

    private CorsoCucina corso;
    private GestioneSessioniController controller;
    private Stage primaryStage;
    
    // Elementi per la lista
    private ListView<Sessione> sessioniList;
    private ComboBox<String> filtroTipo;
    private TextField filtroRicette;
    private Label numeroSessioniLabel;

    public void setCorso(CorsoCucina corso) {
        this.corso = corso;
        this.controller = new GestioneSessioniController(corso);
    }

    public void start(Stage stage) {
        if (corso == null || controller == null) {
            throw new IllegalStateException("Corso o controller non impostati!");
        }

        this.primaryStage = stage;
        stage.setTitle("Gestione Sessioni: " + corso.getNomeCorso());
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        
        // Titolo
        Label titolo = new Label("Gestione Sessioni: " + corso.getNomeCorso());
        titolo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Filtri
        HBox filtriBox = createFiltriBox();
        
        // Numero sessioni
        numeroSessioniLabel = new Label();
        
        // Lista sessioni
        sessioniList = new ListView<>();
        sessioniList.setPrefHeight(350);
        setupSessioniList();
        VBox.setVgrow(sessioniList, Priority.ALWAYS);
        
        // Pulsanti principali
        HBox pulsantiPrincipali = createPulsantiPrincipali();
        
        root.getChildren().addAll(titolo, filtriBox, numeroSessioniLabel, sessioniList, pulsantiPrincipali);
        
        Scene scene = new Scene(root, 700, 600);
        stage.setScene(scene);
        stage.show();
        
        aggiornaLista();
    }
    
    private HBox createFiltriBox() {
        HBox filtriBox = new HBox(10);
        filtriBox.setAlignment(Pos.CENTER_LEFT);
        filtriBox.setPadding(new Insets(10));
        filtriBox.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 5;");
        
        Label filtroLabel = new Label("Filtri:");
        filtroLabel.setStyle("-fx-font-weight: bold;");
        
        filtroTipo = new ComboBox<>();
        filtroTipo.getItems().addAll("Tutti", "Online", "In Presenza");
        filtroTipo.setValue("Tutti");
        filtroTipo.setOnAction(e -> applicaFiltri());
        
        Label ricetteLabel = new Label("Min. Ricette:");
        filtroRicette = new TextField();
        filtroRicette.setPrefWidth(80);
        filtroRicette.setPromptText("0");
        filtroRicette.setOnKeyReleased(e -> applicaFiltri());
        
        Button resetFiltriBtn = new Button("Reset");
        resetFiltriBtn.setOnAction(e -> {
            filtroTipo.setValue("Tutti");
            filtroRicette.clear();
            applicaFiltri();
        });
        
        filtriBox.getChildren().addAll(
            filtroLabel, 
            new Label("Tipo:"), filtroTipo,
            new Separator(),
            ricetteLabel, filtroRicette,
            new Separator(),
            resetFiltriBtn
        );
        
        return filtriBox;
    }
    
    private void setupSessioniList() {
        sessioniList.setCellFactory(lv -> new ListCell<Sessione>() {
            @Override
            protected void updateItem(Sessione s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String tipo = (s instanceof Online) ? "Online" : "In Presenza";
                    String inizio = s.getDataInizioSessione().toLocalDate().toString() + 
                                  " " + s.getDataInizioSessione().toLocalTime().toString();
                    String fine = s.getDataFineSessione().toLocalDate().toString() + 
                                " " + s.getDataFineSessione().toLocalTime().toString();
                    
                    // Placeholder per numero ricette - da implementare nel modello
                    int ricette = 0; // s.getNumeroRicette();
                    
                    setText(String.format("Sessione %d - %s\n" +
                                        "Inizio: %s\n" +
                                        "Fine: %s\n" +
                                        "Ricette: %d | Si cucina solo in presenza", 
                            getIndex() + 1, tipo, inizio, fine, ricette));
                    
                    // Stile diverso per tipo
                    if (s instanceof Online) {
                        setStyle("-fx-background-color: #E3F2FD; -fx-border-color: #2196F3; " +
                               "-fx-border-width: 0 0 0 4; -fx-padding: 8;");
                    } else {
                        setStyle("-fx-background-color: #E8F5E8; -fx-border-color: #4CAF50; " +
                               "-fx-border-width: 0 0 0 4; -fx-padding: 8;");
                    }
                }
            }
        });
        
        // Doppio clic per aprire dettagli
        sessioniList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && sessioniList.getSelectionModel().getSelectedItem() != null) {
                apriDettagli(sessioniList.getSelectionModel().getSelectedItem());
            }
        });
    }
    
    private HBox createPulsantiPrincipali() {
        HBox pulsantiPrincipali = new HBox(15);
        pulsantiPrincipali.setAlignment(Pos.CENTER);
        pulsantiPrincipali.setPadding(new Insets(10, 0, 0, 0));
        
        Button aggiungiBtn = new Button("Aggiungi Sessione");
        aggiungiBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                           "-fx-pref-width: 180; -fx-pref-height: 40; -fx-font-size: 14;");
        aggiungiBtn.setOnAction(e -> aggiungiSessione());
        
        Button tornaIndietroBtn = new Button("Torna Indietro");
        tornaIndietroBtn.setStyle("-fx-pref-width: 150; -fx-pref-height: 40; -fx-font-size: 14;");
        tornaIndietroBtn.setOnAction(e -> primaryStage.close());
        
        pulsantiPrincipali.getChildren().addAll(aggiungiBtn, tornaIndietroBtn);
        
        return pulsantiPrincipali;
    }
    
    private void applicaFiltri() {
        ObservableList<Sessione> sessioniFiltrate = FXCollections.observableArrayList();
        String tipoFiltro = filtroTipo.getValue();
        String ricetteFiltro = filtroRicette.getText().trim();
        
        for (Sessione s : controller.getSessioni()) {
            // Filtro per tipo
            boolean passaTipoFiltro = tipoFiltro.equals("Tutti") ||
                    (tipoFiltro.equals("Online") && s instanceof Online) ||
                    (tipoFiltro.equals("In Presenza") && s instanceof InPresenza);
            
            // Filtro per numero ricette
            boolean passaRicetteFiltro = true;
            if (!ricetteFiltro.isEmpty()) {
                try {
                    int minRicette = Integer.parseInt(ricetteFiltro);
                    // int ricetteSessione = s.getNumeroRicette(); // Da implementare nel modello
                    int ricetteSessione = 0; // Placeholder
                    passaRicetteFiltro = ricetteSessione >= minRicette;
                } catch (NumberFormatException e) {
                    // Se il valore non è un numero valido, ignora il filtro
                    passaRicetteFiltro = true;
                }
            }
            
            if (passaTipoFiltro && passaRicetteFiltro) {
                sessioniFiltrate.add(s);
            }
        }
        
        // Ordina per data di inizio
        sessioniFiltrate.sort(Comparator.comparing(Sessione::getDataInizioSessione));
        
        sessioniList.setItems(sessioniFiltrate);
        numeroSessioniLabel.setText(String.format("Sessioni visualizzate: %d di %d totali", 
                                                 sessioniFiltrate.size(), controller.getSessioni().size()));
        numeroSessioniLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666;");
    }
    
    public void aggiornaLista() {
        applicaFiltri();
    }
    
    private void apriDettagli(Sessione sessione) {
        GestioneSessioniGUI dettagliGUI = new GestioneSessioniGUI();
        dettagliGUI.setSessione(sessione);
        dettagliGUI.setController(controller);
        dettagliGUI.setParentGUI(this);
        
        Stage dettagliStage = new Stage();
        dettagliStage.initOwner(primaryStage);
        dettagliGUI.start(dettagliStage);
        
        // Aggiorna lista quando si chiude la finestra dettagli
        dettagliStage.setOnHiding(e -> aggiornaLista());
    }
    
    private void aggiungiSessione() {
        GestioneSessioniGUI aggiungiGUI = new GestioneSessioniGUI();
        aggiungiGUI.setCorso(corso);
        aggiungiGUI.setController(controller);
        aggiungiGUI.setParentGUI(this);
        aggiungiGUI.setModalitaAggiunta(true);
        
        Stage aggiungiStage = new Stage();
        aggiungiStage.initOwner(primaryStage);
        aggiungiGUI.start(aggiungiStage);
        
        // Aggiorna lista quando si chiude la finestra
        aggiungiStage.setOnHiding(e -> aggiornaLista());
    }
}