package Gui;

import controller.GestioneSessioniController;
import model.*;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Comparator;

public class VisualizzaSessioniGUI {

    private CorsoCucina corso;
    private GestioneSessioniController controller;
    
    private ListView<Sessione> sessioniList;
    private ComboBox<String> filtroTipo;
    private TextField filtroRicette;
    private Label numeroSessioniLabel;

    // Root principale
    private VBox root;

    // Set controller esterno invece di crearne uno nuovo
    public void setController(GestioneSessioniController controller) {
        this.controller = controller;
    }

    public void setCorso(CorsoCucina corso) {
        this.corso = corso;
    }

    public VBox getRoot() {
        if (corso == null || controller == null) {
            throw new IllegalStateException("Corso o controller non impostati!");
        }

        root = new VBox(15);
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

        aggiornaLista();

        return root;
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
                    String inizio = s.getDataInizioSessione().toLocalDate() + " " + s.getDataInizioSessione().toLocalTime();
                    String fine = s.getDataFineSessione().toLocalDate() + " " + s.getDataFineSessione().toLocalTime();
                    int ricette = (s instanceof InPresenza) ? ((InPresenza) s).getRicette().size() : 0;

                    setText(String.format("Sessione %d - %s\nInizio: %s\nFine: %s\nRicette: %d", 
                            getIndex() + 1, tipo, inizio, fine, ricette));

                    if (s instanceof Online) {
                        setStyle("-fx-background-color: #E3F2FD; -fx-border-color: #2196F3; -fx-border-width: 0 0 0 4; -fx-padding: 8;");
                    } else {
                        setStyle("-fx-background-color: #E8F5E8; -fx-border-color: #4CAF50; -fx-border-width: 0 0 0 4; -fx-padding: 8;");
                    }
                }
            }
        });

        sessioniList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && sessioniList.getSelectionModel().getSelectedItem() != null) {
                Sessione selezionata = sessioniList.getSelectionModel().getSelectedItem();
                
                // Apri GUI gestione ricette se è una sessione in presenza
                if (selezionata instanceof InPresenza inPresenza) {
                    controller.apriSelezionaRicettaGUI(inPresenza);
                } else {
                    showInfo("Info", "Le sessioni online non possono avere ricette associate.");
                }
            }
        });
    }

    private HBox createPulsantiPrincipali() {
        HBox pulsantiPrincipali = new HBox(15);
        pulsantiPrincipali.setAlignment(Pos.CENTER);
        pulsantiPrincipali.setPadding(new Insets(10, 0, 0, 0));

        Button aggiungiBtn = new Button("Aggiungi Sessione");
        aggiungiBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-pref-width: 180; -fx-pref-height: 40; -fx-font-size: 14;");
        
        aggiungiBtn.setOnAction(e -> {
            try {
                GestioneSessioniGUI aggiungiGUI = new GestioneSessioniGUI();
                aggiungiGUI.setModalitaAggiunta(true);
                aggiungiGUI.setController(controller);
                aggiungiGUI.setCorso(corso);
                
                // Crea una nuova finestra per aggiungere la sessione
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                stage.setTitle("Aggiungi Nuova Sessione");
                
                javafx.scene.Scene scene = new javafx.scene.Scene(aggiungiGUI.getRoot(), 600, 700);
                stage.setScene(scene);
                stage.showAndWait();
                
                // Aggiorna la lista dopo la chiusura della finestra
                aggiornaLista();
                
            } catch (Exception ex) {
                showError("Errore", "Errore nell'apertura della finestra di aggiunta sessione: " + ex.getMessage());
            }
        });

        Button modificaBtn = new Button("Modifica Sessione");
        modificaBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-pref-width: 180; -fx-pref-height: 40; -fx-font-size: 14;");
        
        modificaBtn.setOnAction(e -> {
            Sessione selezionata = sessioniList.getSelectionModel().getSelectedItem();
            if (selezionata != null) {
                try {
                    GestioneSessioniGUI modificaGUI = new GestioneSessioniGUI();
                    modificaGUI.setModalitaAggiunta(false);
                    modificaGUI.setController(controller);
                    modificaGUI.setCorso(corso);
                    modificaGUI.setSessione(selezionata);
                    
                    javafx.stage.Stage stage = new javafx.stage.Stage();
                    stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                    stage.setTitle("Modifica Sessione");
                    
                    javafx.scene.Scene scene = new javafx.scene.Scene(modificaGUI.getRoot(), 600, 700);
                    stage.setScene(scene);
                    stage.showAndWait();
                    
                    aggiornaLista();
                    
                } catch (Exception ex) {
                    showError("Errore", "Errore nell'apertura della finestra di modifica sessione: " + ex.getMessage());
                }
            } else {
                showInfo("Selezione", "Seleziona una sessione da modificare.");
            }
        });

        Button eliminaBtn = new Button("Elimina Sessione");
        eliminaBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-pref-width: 180; -fx-pref-height: 40; -fx-font-size: 14;");
        
        eliminaBtn.setOnAction(e -> {
            Sessione selezionata = sessioniList.getSelectionModel().getSelectedItem();
            if (selezionata != null) {
                Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
                conferma.setTitle("Conferma Eliminazione");
                conferma.setHeaderText("Eliminare la sessione selezionata?");
                conferma.setContentText("Questa operazione non può essere annullata.");
                
                if (conferma.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    try {
                        controller.eliminaSessione(selezionata);
                        aggiornaLista();
                        showInfo("Successo", "Sessione eliminata con successo.");
                    } catch (Exception ex) {
                        showError("Errore", "Errore durante l'eliminazione: " + ex.getMessage());
                    }
                }
            } else {
                showInfo("Selezione", "Seleziona una sessione da eliminare.");
            }
        });

        Button tornaIndietroBtn = new Button("Torna Indietro");
        tornaIndietroBtn.setStyle("-fx-pref-width: 150; -fx-pref-height: 40; -fx-font-size: 14;");
        tornaIndietroBtn.setOnAction(e -> {
            if (root.getScene() != null) {
                root.getScene().getWindow().hide();
            }
        });

        pulsantiPrincipali.getChildren().addAll(aggiungiBtn, modificaBtn, eliminaBtn, tornaIndietroBtn);

        return pulsantiPrincipali;
    }

    private void applicaFiltri() {
        ObservableList<Sessione> sessioniFiltrate = FXCollections.observableArrayList();
        String tipoFiltro = filtroTipo.getValue();
        String ricetteFiltro = filtroRicette.getText().trim();

        if (corso.getSessioni() == null) {
            sessioniList.setItems(sessioniFiltrate);
            numeroSessioniLabel.setText("Nessuna sessione disponibile");
            return;
        }

        for (Sessione s : corso.getSessioni()) {
            boolean passaTipoFiltro = tipoFiltro.equals("Tutti") ||
                    (tipoFiltro.equals("Online") && s instanceof Online) ||
                    (tipoFiltro.equals("In Presenza") && s instanceof InPresenza);

            boolean passaRicetteFiltro = true;
            if (!ricetteFiltro.isEmpty()) {
                try {
                    int minRicette = Integer.parseInt(ricetteFiltro);
                    int ricetteSessione = (s instanceof InPresenza) ? ((InPresenza) s).getRicette().size() : 0;
                    passaRicetteFiltro = ricetteSessione >= minRicette;
                } catch (NumberFormatException e) {
                    passaRicetteFiltro = true;
                }
            }

            if (passaTipoFiltro && passaRicetteFiltro) {
                sessioniFiltrate.add(s);
            }
        }

        sessioniFiltrate.sort(Comparator.comparing(Sessione::getDataInizioSessione));
        sessioniList.setItems(sessioniFiltrate);
        numeroSessioniLabel.setText(String.format("Sessioni visualizzate: %d di %d totali", 
                sessioniFiltrate.size(), corso.getSessioni().size()));
        numeroSessioniLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666;");
    }

    public void aggiornaLista() {
        applicaFiltri();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}