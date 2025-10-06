package controller;

import model.*;
import service.GestioneSessioni;
import service.GestioneCucina;
import service.GestioneRicette;
import Gui.CreaRicettaGUI;

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
        // ✅ VALIDAZIONE: Un corso deve avere almeno 1 sessione
        if (corso.getSessioni().size() <= 1) {
            throw new IllegalStateException(
                "Impossibile eliminare questa sessione in quanto è l'unica sessione del corso. " +
                "Aggiungere almeno un'altra sessione prima di eliminare questa."
            );
        }
        
        // Il database gestisce automaticamente con ON DELETE CASCADE:
        // - segue (corso-sessione)
        // - cucina (sessione-ricetta)
        // - adesione (utente-sessione)
        corso.getSessioni().remove(s);
        gestioneSessioniService.rimuoviSessione(s);
    }

    public void aggiungiRicettaAInPresenza(InPresenza ip, Ricetta r) throws SQLException {
        if (r.getIdRicetta() == 0) {
            gestioneRicetteService.creaRicetta(r);
        }
        
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

    public void apriSelezionaRicettaGUI(InPresenza ip) {
        if (ip == null) {
            showError("Errore", "Sessione non valida");
            return;
        }

        try {
            List<Ricetta> tutteRicette = gestioneRicetteService.getAllRicette();

            if (tutteRicette == null || tutteRicette.isEmpty()) {
                showInfo("Nessuna ricetta disponibile", "Non ci sono ricette nel database. Creane una prima di associarla alla sessione.");
                return;
            }

            Stage selectionStage = new Stage();
            selectionStage.initModality(Modality.APPLICATION_MODAL);
            selectionStage.setTitle("Gestione Ricette per Sessione");
            selectionStage.setResizable(true);

            VBox mainContainer = new VBox(20);
            mainContainer.setPadding(new Insets(20));
            mainContainer.setStyle("-fx-background-color: #f8f9fa;");

            Label titleLabel = new Label("📖 Seleziona Ricette per la Sessione");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

            Label sessionInfo = new Label(String.format("Sessione: %s, %s - Posti: %d", 
                ip.getVia(), ip.getCitta(), ip.getNumeroPosti()));
            sessionInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

            Label availableLabel = new Label("Ricette disponibili:");
            availableLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            ListView<Ricetta> availableListView = new ListView<>();
            availableListView.getItems().addAll(tutteRicette);
            availableListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            availableListView.setPrefHeight(200);
            availableListView.setStyle("-fx-background-radius: 8; -fx-border-radius: 8;");

            availableListView.setCellFactory(listView -> new ListCell<Ricetta>() {
                @Override
                protected void updateItem(Ricetta ricetta, boolean empty) {
                    super.updateItem(ricetta, empty);
                    if (empty || ricetta == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(ricetta.getNome() + " (" + ricetta.getTempoPreparazione() + " min)");
                        
                        if (ip.getRicette().contains(ricetta)) {
                            setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: #27ae60;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            });

            if (ip.getRicette() != null && !ip.getRicette().isEmpty()) {
                for (Ricetta r : ip.getRicette()) {
                    availableListView.getSelectionModel().select(r);
                }
            }

            Label associatedLabel = new Label("Ricette già associate a questa sessione:");
            associatedLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            ListView<Ricetta> associatedListView = new ListView<>();
            associatedListView.setPrefHeight(150);
            associatedListView.setStyle("-fx-background-radius: 8; -fx-border-radius: 8;");

            refreshAssociatedList(associatedListView, ip);

            HBox buttonContainer = new HBox(15);
            buttonContainer.setAlignment(Pos.CENTER);

            Button associateBtn = createStyledButton("➕ Associa Selezionate", "#27ae60");
            Button removeBtn = createStyledButton("➖ Rimuovi Selezionata", "#e74c3c");
            Button createNewBtn = createStyledButton("📝 Crea Nuova Ricetta", "#3498db");
            Button closeBtn = createStyledButton("✅ Chiudi", "#95a5a6");

            buttonContainer.getChildren().addAll(associateBtn, removeBtn, createNewBtn, closeBtn);

            associateBtn.setOnAction(e -> {
                try {
                    List<Ricetta> selected = new ArrayList<>(availableListView.getSelectionModel().getSelectedItems());
                    int added = 0;
                    
                    for (Ricetta ricetta : selected) {
                        if (!ip.getRicette().contains(ricetta)) {
                            aggiungiRicettaAInPresenza(ip, ricetta);
                            added++;
                        }
                    }
                    
                    if (added > 0) {
                        refreshAssociatedList(associatedListView, ip);
                        availableListView.refresh();
                        showInfo("✅ Successo", String.format("Aggiunte %d ricette alla sessione.", added));
                    } else {
                        showInfo("Info", "Le ricette selezionate sono già associate alla sessione.");
                    }
                    
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showError("Errore Database", "Errore durante l'associazione delle ricette: " + ex.getMessage());
                }
            });

            removeBtn.setOnAction(e -> {
                Ricetta selected = associatedListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
                    conferma.setTitle("⚠️ Conferma Rimozione");
                    conferma.setHeaderText("Rimuovere questa ricetta dalla sessione?");
                    conferma.setContentText("Ricetta: " + selected.getNome() + "\n\n⚠️ Questa operazione non può essere annullata!");
                    
                    ButtonType btnConferma = new ButtonType("✅ Conferma", ButtonBar.ButtonData.OK_DONE);
                    ButtonType btnAnnulla = new ButtonType("❌ Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
                    conferma.getButtonTypes().setAll(btnConferma, btnAnnulla);
                    
                    conferma.showAndWait().ifPresent(response -> {
                        if (response == btnConferma) {
                            try {
                                rimuoviRicettaDaSessione(ip, selected);
                                refreshAssociatedList(associatedListView, ip);
                                availableListView.refresh();
                                showInfo("✅ Successo", "Ricetta rimossa dalla sessione.");
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                                showError("Errore Database", "Errore durante la rimozione della ricetta: " + ex.getMessage());
                            }
                        }
                    });
                } else {
                    showInfo("Info", "Seleziona una ricetta da rimuovere.");
                }
            });

            createNewBtn.setOnAction(e -> {
                try {
                    CreaRicettaGUI creaGUI = new CreaRicettaGUI(gestioneRicetteService);
                    Ricetta nuovaRicetta = creaGUI.showAndReturn();

                    if (nuovaRicetta != null) {
                        if (!ip.getRicette().contains(nuovaRicetta)) {
                            aggiungiRicettaAInPresenza(ip, nuovaRicetta);
                            refreshAssociatedList(associatedListView, ip);
                            availableListView.getItems().add(nuovaRicetta);
                            availableListView.refresh();
                            showInfo("✅ Successo", "Ricetta creata e associata alla sessione!");
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Errore", "Errore durante la creazione della ricetta: " + ex.getMessage());
                }
            });

            closeBtn.setOnAction(e -> selectionStage.close());

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

        button.setOnMouseEntered(e -> button.setOpacity(0.8));
        button.setOnMouseExited(e -> button.setOpacity(1.0));

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
