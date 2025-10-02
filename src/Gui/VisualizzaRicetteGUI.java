package Gui;

import controller.GestioneSessioniController;
import service.GestioneRicette;
import model.Ricetta;
import model.InPresenza;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.List;

public class VisualizzaRicetteGUI {


    private final GestioneRicette gestioneRicette;
    private final GestioneSessioniController sessioniController;
    private InPresenza sessione;

    //  COSTRUTTORE CORRETTO 
    public VisualizzaRicetteGUI(GestioneRicette gestioneRicette, GestioneSessioniController sessioniController) {
        this.gestioneRicette = gestioneRicette;
        this.sessioniController = sessioniController;
    }

    public void setSessione(InPresenza sessione) {
        this.sessione = sessione;
    }

    public void show(Stage stage) {
        stage.setTitle("📖 Gestione Ricette per Sessione");
        stage.setResizable(true);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Titolo con informazioni sessione
        Label titleLabel = new Label("📖 Gestione Ricette");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label sessionInfoLabel = new Label();
        if (sessione != null) {
            sessionInfoLabel.setText(String.format("Sessione: %s, %s - Posti: %d", 
                sessione.getVia(), sessione.getCitta(), sessione.getNumeroPosti()));
            sessionInfoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        }

        // Container principale con due liste
        HBox mainContainer = new HBox(20);
        mainContainer.setAlignment(Pos.TOP_CENTER);

        // SEZIONE RICETTE DISPONIBILI
        VBox availableSection = createAvailableSection();

        // SEZIONE PULSANTI
        VBox buttonSection = createButtonSection();

        //  SEZIONE RICETTE ASSOCIATE 
        VBox associatedSection = createAssociatedSection();

        mainContainer.getChildren().addAll(availableSection, buttonSection, associatedSection);

        // Pulsante chiudi
        HBox bottomButtons = new HBox(10);
        bottomButtons.setAlignment(Pos.CENTER);
        bottomButtons.setPadding(new Insets(20, 0, 0, 0));

        Button chiudiBtn = createStyledButton("✅ Chiudi", "#95a5a6");
        chiudiBtn.setOnAction(e -> stage.close());

        bottomButtons.getChildren().add(chiudiBtn);

        root.getChildren().addAll(titleLabel, sessionInfoLabel, new Separator(), mainContainer, bottomButtons);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    private VBox createAvailableSection() {
        VBox section = new VBox(10);
        section.setPrefWidth(300);

        Label availableLabel = new Label("Ricette Disponibili:");
        availableLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        ListView<Ricetta> listaRicetteDisponibili = new ListView<>();
        listaRicetteDisponibili.setPrefHeight(350);
        listaRicetteDisponibili.setId("listaRicetteDisponibili"); // Per identificazione

        // *** CORREZIONE: Usa il metodo corretto per caricare ricette ***
        try {
            List<Ricetta> tutteRicette = gestioneRicette.getAllRicette(); // Metodo corretto
            listaRicetteDisponibili.getItems().addAll(tutteRicette);
        } catch (SQLException e) {
            showError("Errore caricamento ricette: " + e.getMessage());
        }

        // Custom cell factory per mostrare dettagli
        listaRicetteDisponibili.setCellFactory(listView -> new ListCell<Ricetta>() {
            @Override
            protected void updateItem(Ricetta ricetta, boolean empty) {
                super.updateItem(ricetta, empty);
                if (empty || ricetta == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(String.format("%s\n(Tempo: %d min)", 
                        ricetta.getNome(), ricetta.getTempoPreparazione()));
                    
                    // Evidenzia se già associata
                    if (sessione != null && sessione.getRicette().contains(ricetta)) {
                        setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: #27ae60;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        section.getChildren().addAll(availableLabel, listaRicetteDisponibili);
        return section;
    }

    private VBox createButtonSection() {
        VBox section = new VBox(15);
        section.setAlignment(Pos.CENTER);
        section.setPrefWidth(150);

        // *** CORREZIONE: Bottone aggiungi corretto ***
        Button btnAggiungi = createStyledButton("➡️ Aggiungi", "#27ae60");
        btnAggiungi.setOnAction(e -> {
            ListView<Ricetta> listaDisponibili = (ListView<Ricetta>) 
                ((VBox)((HBox)btnAggiungi.getParent().getParent()).getChildren().get(0)).getChildren().get(1);
            ListView<Ricetta> listaAssociate = (ListView<Ricetta>) 
                ((VBox)((HBox)btnAggiungi.getParent().getParent()).getChildren().get(2)).getChildren().get(1);
                
            Ricetta selected = listaDisponibili.getSelectionModel().getSelectedItem();
            if (selected != null && sessione != null) {
                try {
                    // *** CORREZIONE: Usa il metodo corretto del controller ***
                    sessioniController.aggiungiRicettaAInPresenza(sessione, selected);
                    
                    // Aggiorna le liste
                    refreshLists(listaDisponibili, listaAssociate);
                    
                    showInfo("Successo", "Ricetta '" + selected.getNome() + "' aggiunta alla sessione!");
                    
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showError("Errore durante l'aggiunta della ricetta: " + ex.getMessage());
                } catch (IllegalArgumentException ex) {
                    showError("Errore di validazione: " + ex.getMessage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Errore imprevisto: " + ex.getMessage());
                }
            } else {
                if (selected == null) {
                    showError("Seleziona una ricetta da aggiungere.");
                }
                if (sessione == null) {
                    showError("Nessuna sessione specificata.");
                }
            }
        });

        Button btnRimuovi = createStyledButton("⬅️ Rimuovi", "#e74c3c");
        btnRimuovi.setOnAction(e -> {
            ListView<Ricetta> listaDisponibili = (ListView<Ricetta>) 
                ((VBox)((HBox)btnRimuovi.getParent().getParent()).getChildren().get(0)).getChildren().get(1);
            ListView<Ricetta> listaAssociate = (ListView<Ricetta>) 
                ((VBox)((HBox)btnRimuovi.getParent().getParent()).getChildren().get(2)).getChildren().get(1);
                
            Ricetta selected = listaAssociate.getSelectionModel().getSelectedItem();
            if (selected != null && sessione != null) {
                try {
                    sessioniController.rimuoviRicettaDaSessione(sessione, selected);
                    
                    // Aggiorna le liste
                    refreshLists(listaDisponibili, listaAssociate);
                    
                    showInfo("Successo", "Ricetta rimossa dalla sessione!");
                    
                } catch (SQLException ex) {
                    showError("Errore durante la rimozione: " + ex.getMessage());
                }
            } else {
                if (selected == null) {
                    showError("Seleziona una ricetta da rimuovere.");
                }
            }
        });

        Button btnCreaRicetta = createStyledButton("📝 Crea Nuova", "#3498db");
        btnCreaRicetta.setOnAction(e -> {
            try {
                creaRicettaDialog((Stage) btnCreaRicetta.getScene().getWindow());
            } catch (Exception ex) {
                showError("Errore nell'apertura del dialog: " + ex.getMessage());
            }
        });

        section.getChildren().addAll(btnAggiungi, btnRimuovi, new Separator(), btnCreaRicetta);
        return section;
    }

    private VBox createAssociatedSection() {
        VBox section = new VBox(10);
        section.setPrefWidth(300);

        Label associatedLabel = new Label("Ricette Associate:");
        associatedLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        ListView<Ricetta> listaRicetteAssociate = new ListView<>();
        listaRicetteAssociate.setPrefHeight(350);

        // Carica ricette già associate alla sessione
        if (sessione != null && sessione.getRicette() != null) {
            listaRicetteAssociate.getItems().addAll(sessione.getRicette());
        }

        section.getChildren().addAll(associatedLabel, listaRicetteAssociate);
        return section;
    }

    private void refreshLists(ListView<Ricetta> listaDisponibili, ListView<Ricetta> listaAssociate) {
        // Aggiorna lista disponibili (refresh per i colori)
        listaDisponibili.refresh();
        
        // Aggiorna lista associate
        listaAssociate.getItems().clear();
        if (sessione != null && sessione.getRicette() != null) {
            listaAssociate.getItems().addAll(sessione.getRicette());
        }
    }

    private void creaRicettaDialog(Stage parent) {
        Stage dialog = new Stage();
        dialog.initOwner(parent);
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Crea Nuova Ricetta");

        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        Label titleLabel = new Label("📝 Crea Nuova Ricetta");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome ricetta");
        nomeField.setPrefHeight(35);

        TextField tempoField = new TextField();
        tempoField.setPromptText("Tempo preparazione (minuti)");
        tempoField.setPrefHeight(35);

        CheckBox associateCheckbox = new CheckBox("Associa automaticamente alla sessione");
        associateCheckbox.setSelected(true);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button saveBtn = createStyledButton("💾 Salva", "#27ae60");
        Button cancelBtn = createStyledButton("❌ Annulla", "#95a5a6");

        saveBtn.setOnAction(e -> {
            try {
                String nome = nomeField.getText().trim();
                String tempoText = tempoField.getText().trim();

                if (nome.isEmpty()) {
                    showError("Inserisci il nome della ricetta.");
                    return;
                }

                int tempo;
                try {
                    tempo = Integer.parseInt(tempoText);
                    if (tempo <= 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    showError("Inserisci un tempo di preparazione valido (numero intero positivo).");
                    return;
                }

                // Crea ricetta
                Ricetta nuovaRicetta = new Ricetta(nome, tempo);
                gestioneRicette.creaRicetta(nuovaRicetta);

                // Associa alla sessione se richiesto
                if (associateCheckbox.isSelected() && sessione != null) {
                    sessioniController.aggiungiRicettaAInPresenza(sessione, nuovaRicetta);
                }

                showInfo("Successo", "Ricetta creata con successo!");
                dialog.close();

                // Chiudi e riapri la finestra per aggiornare le liste
                Stage currentStage = (Stage) parent;
                currentStage.close();
                show(new Stage());

            } catch (SQLException ex) {
                ex.printStackTrace();
                showError("Errore durante il salvataggio: " + ex.getMessage());
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

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
        dialog.setScene(scene);
        dialog.showAndWait();
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

        button.setOnMouseEntered(e -> button.setStyle(button.getStyle() + "-fx-opacity: 0.8;"));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle().replace("-fx-opacity: 0.8;", "")));

        return button;
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(msg);
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