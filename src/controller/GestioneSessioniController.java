package controller;

import model.*;
import service.GestioneSessioni;
import service.GestioneCucina;
import service.GestioneRicette;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class GestioneSessioniController {
    private final CorsoCucina corso;
    private final GestioneSessioni gestioneSessioniService;
    private final GestioneCucina gestioneCucinaService;
    private final GestioneRicette gestioneRicetteService;

    public GestioneSessioniController(CorsoCucina corso,
                                    GestioneSessioni gestioneSessioniService,
                                    GestioneCucina gestioneCucinaService,
                                    GestioneRicette gestioneRicetteService) {
        this.corso = corso;
        this.gestioneSessioniService = gestioneSessioniService;
        this.gestioneCucinaService = gestioneCucinaService;
        this.gestioneRicetteService = gestioneRicetteService;
    }

    // --- Sessioni ---
    public void aggiungiSessione(Sessione s) throws SQLException {
        aggiungiSessione(s, null);
    }

    public void aggiungiSessione(Sessione s, List<Ricetta> ricette) throws SQLException {
        if (s == null) throw new IllegalArgumentException("Sessione nulla");

        corso.getSessioni().add(s);
        gestioneSessioniService.creaSessione(s);

        if (s instanceof InPresenza ip && ricette != null) {
            for (Ricetta r : ricette) {
                if (!ip.getRicette().contains(r)) {
                    ip.getRicette().add(r);
                    r.getSessioni().add(ip);
                    gestioneCucinaService.aggiungiSessioneARicetta(r, ip);
                }
            }
        }
    }
    
    

    public void aggiornaSessione(Sessione oldS, Sessione newS) throws SQLException {
        int idx = corso.getSessioni().indexOf(oldS);
        if (idx >= 0) {
            corso.getSessioni().set(idx, newS);
            gestioneSessioniService.rimuoviSessione(oldS);
            gestioneSessioniService.creaSessione(newS);
        }
    }

    public void eliminaSessione(Sessione s) throws SQLException {
        corso.getSessioni().remove(s);
        gestioneSessioniService.rimuoviSessione(s);
    }

    // --- Ricette ---
    public void aggiungiRicettaAInPresenza(InPresenza ip, Ricetta r) throws SQLException {
        gestioneRicetteService.creaRicetta(r);
        if (!ip.getRicette().contains(r)) {
            ip.getRicette().add(r);
            r.getSessioni().add(ip);
            gestioneCucinaService.aggiungiSessioneARicetta(r, ip);
        }
    }
    
    public void aggiungiRicettaASessione(Sessione sessione, Ricetta ricetta) throws SQLException {
        if (sessione instanceof InPresenza inPresenza) {
            aggiungiRicettaAInPresenza(inPresenza, ricetta);
        } else {
            throw new IllegalArgumentException("Le ricette possono essere associate solo a sessioni in presenza");
        }
    }
    public void rimuoviRicettaDaSessione(InPresenza ip, Ricetta r) throws SQLException {
        if (ip.getRicette().remove(r)) {
            r.getSessioni().remove(ip);
            gestioneCucinaService.rimuoviSessioneDaRicetta(r, ip);
        }
    }

    // --- GUI Ricette - IMPLEMENTAZIONE COMPLETA ---
    public void apriSelezionaRicettaGUI(InPresenza ip) {
        if (ip == null) {
            showError("Errore", "Sessione non valida");
            return;
        }

        try {
            // Carica tutte le ricette dal database
            List<Ricetta> tutteRicette = gestioneRicetteService.getAllRicette();

            if (tutteRicette == null || tutteRicette.isEmpty()) {
                showInfo("Nessuna ricetta disponibile", "Non ci sono ricette nel database. Creane una prima di associarla alla sessione.");
                return;
            }

            // Crea finestra di selezione
            Stage selectionStage = new Stage();
            selectionStage.initModality(Modality.APPLICATION_MODAL);
            selectionStage.setTitle("Gestione Ricette per Sessione");
            selectionStage.setResizable(true);

            VBox mainContainer = new VBox(20);
            mainContainer.setPadding(new Insets(20));
            mainContainer.setStyle("-fx-background-color: #f8f9fa;");

            // Titolo
            Label titleLabel = new Label("📖 Seleziona Ricette per la Sessione");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

            // Informazioni sessione
            Label sessionInfo = new Label(String.format("Sessione: %s, %s - Posti: %d", 
                ip.getVia(), ip.getCitta(), ip.getNumeroPosti()));
            sessionInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

            // Lista ricette disponibili
            Label availableLabel = new Label("Ricette disponibili:");
            availableLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            ListView<Ricetta> availableListView = new ListView<>();
            availableListView.getItems().addAll(tutteRicette);
            availableListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            availableListView.setPrefHeight(200);
            availableListView.setStyle("-fx-background-radius: 8; -fx-border-radius: 8;");

            // Custom cell factory per mostrare dettagli ricette
            availableListView.setCellFactory(listView -> new ListCell<Ricetta>() {
                @Override
                protected void updateItem(Ricetta ricetta, boolean empty) {
                    super.updateItem(ricetta, empty);
                    if (empty || ricetta == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(String.format("%s (Tempo: %d min)", 
                            ricetta.getNome(), ricetta.getTempoPreparazione()));
                        
                        // Evidenzia se già selezionata
                        if (ip.getRicette().contains(ricetta)) {
                            setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: #27ae60;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            });

            // Pre-seleziona ricette già associate
            if (ip.getRicette() != null && !ip.getRicette().isEmpty()) {
                for (Ricetta r : ip.getRicette()) {
                    availableListView.getSelectionModel().select(r);
                }
            }

            // Lista ricette attualmente associate
            Label associatedLabel = new Label("Ricette già associate a questa sessione:");
            associatedLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            ListView<Ricetta> associatedListView = new ListView<>();
            associatedListView.setPrefHeight(150);
            associatedListView.setStyle("-fx-background-radius: 8; -fx-border-radius: 8;");

            // Aggiorna lista associate
            refreshAssociatedList(associatedListView, ip);

            // Pulsanti di azione
            HBox buttonContainer = new HBox(15);
            buttonContainer.setAlignment(Pos.CENTER);

            Button associateBtn = createStyledButton("➕ Associa Selezionate", "#27ae60");
            Button removeBtn = createStyledButton("➖ Rimuovi Selezionata", "#e74c3c");
            Button createNewBtn = createStyledButton("📝 Crea Nuova Ricetta", "#3498db");
            Button closeBtn = createStyledButton("✅ Chiudi", "#95a5a6");

            buttonContainer.getChildren().addAll(associateBtn, removeBtn, createNewBtn, closeBtn);

            // Eventi pulsanti
            associateBtn.setOnAction(e -> {
                try {
                    List<Ricetta> selected = new ArrayList<>(availableListView.getSelectionModel().getSelectedItems());
                    int added = 0;
                    
                    for (Ricetta ricetta : selected) {
                        if (!ip.getRicette().contains(ricetta)) {
                            // Aggiungi ricetta alla sessione (sia in memoria che DB)
                            aggiungiRicettaAInPresenza(ip, ricetta);
                            added++;
                        }
                    }
                    
                    if (added > 0) {
                        refreshAssociatedList(associatedListView, ip);
                        availableListView.refresh(); // Refresh per aggiornare colori
                        showInfo("Successo", String.format("Aggiunte %d ricette alla sessione.", added));
                    } else {
                        showInfo("Info", "Le ricette selezionate sono già associate alla sessione.");
                    }
                    
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showError("Errore Database", "Errore durante l'associazione delle ricette: " + ex.getMessage());
                }
            });

            removeBtn.setOnAction(e -> {
                try {
                    Ricetta selected = associatedListView.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        rimuoviRicettaDaSessione(ip, selected);
                        refreshAssociatedList(associatedListView, ip);
                        availableListView.refresh();
                        showInfo("Successo", "Ricetta rimossa dalla sessione.");
                    } else {
                        showInfo("Info", "Seleziona una ricetta da rimuovere.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showError("Errore Database", "Errore durante la rimozione della ricetta: " + ex.getMessage());
                }
            });

            createNewBtn.setOnAction(e -> {
                apriCreaRicettaDialog(selectionStage, ip, availableListView, associatedListView);
            });

            closeBtn.setOnAction(e -> selectionStage.close());

            // Layout finale
            VBox contentBox = new VBox(15);
            contentBox.getChildren().addAll(
                titleLabel,
                sessionInfo,
                new Separator(),
                availableLabel,
                availableListView,
                associatedLabel,
                associatedListView,
                new Separator(),
                buttonContainer
            );

            mainContainer.getChildren().add(contentBox);

            // Scroll pane per contenuto lungo
            ScrollPane scrollPane = new ScrollPane(mainContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent;");

            Scene scene = new Scene(scrollPane, 600, 700);
            selectionStage.setScene(scene);
            selectionStage.showAndWait();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Errore Database", "Errore durante il caricamento delle ricette: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Errore", "Errore imprevisto: " + e.getMessage());
        }
    }

    private void refreshAssociatedList(ListView<Ricetta> listView, InPresenza ip) {
        listView.getItems().clear();
        if (ip.getRicette() != null) {
            listView.getItems().addAll(ip.getRicette());
        }
    }

    private void apriCreaRicettaDialog(Stage parent, InPresenza ip, ListView<Ricetta> availableList, ListView<Ricetta> associatedList) {
        Stage createStage = new Stage();
        createStage.initOwner(parent);
        createStage.initModality(Modality.APPLICATION_MODAL);
        createStage.setTitle("Crea Nuova Ricetta");

        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        Label titleLabel = new Label("📝 Crea Nuova Ricetta");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome ricetta");
        nomeField.setStyle("-fx-pref-height: 35px;");

        TextField tempoField = new TextField();
        tempoField.setPromptText("Tempo preparazione (minuti)");
        tempoField.setStyle("-fx-pref-height: 35px;");

        CheckBox associateCheckbox = new CheckBox("Associa automaticamente alla sessione");
        associateCheckbox.setSelected(true);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button saveBtn = createStyledButton("💾 Salva", "#27ae60");
        Button cancelBtn = createStyledButton("❌ Annulla", "#95a5a6");

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        saveBtn.setOnAction(e -> {
            try {
                String nome = nomeField.getText().trim();
                String tempoText = tempoField.getText().trim();

                if (nome.isEmpty()) {
                    showError("Errore", "Inserisci il nome della ricetta.");
                    return;
                }

                int tempo;
                try {
                    tempo = Integer.parseInt(tempoText);
                    if (tempo <= 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    showError("Errore", "Inserisci un tempo di preparazione valido (numero intero positivo).");
                    return;
                }

                // Crea ricetta
                Ricetta nuovaRicetta = new Ricetta(nome, tempo);
                gestioneRicetteService.creaRicetta(nuovaRicetta);

                // Associa alla sessione se richiesto
                if (associateCheckbox.isSelected()) {
                    aggiungiRicettaAInPresenza(ip, nuovaRicetta);
                    refreshAssociatedList(associatedList, ip);
                }

                // Aggiorna lista disponibili
                availableList.getItems().add(nuovaRicetta);
                availableList.refresh();

                showInfo("Successo", "Ricetta creata con successo!");
                createStage.close();

            } catch (SQLException ex) {
                ex.printStackTrace();
                showError("Errore Database", "Errore durante il salvataggio: " + ex.getMessage());
            }
        });

        cancelBtn.setOnAction(e -> createStage.close());

        container.getChildren().addAll(
            titleLabel,
            new Label("Nome ricetta:"),
            nomeField,
            new Label("Tempo preparazione (minuti):"),
            tempoField,
            associateCheckbox,
            buttonBox
        );

        Scene scene = new Scene(container, 400, 300);
        createStage.setScene(scene);
        createStage.showAndWait();
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 8 16; " +
            "-fx-cursor: hand;", color
        ));

        // Hover effect
        button.setOnMouseEntered(e -> button.setStyle(button.getStyle() + "-fx-opacity: 0.8;"));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle().replace("-fx-opacity: 0.8;", "")));

        return button;
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