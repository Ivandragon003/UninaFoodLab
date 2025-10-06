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

    private VBox root;

    public void setController(GestioneSessioniController controller) {
        this.controller = controller;
    }

    public void setCorso(CorsoCucina corso) {
        this.corso = corso;
    }

    public VBox getRoot() {
        if (corso == null) {
            throw new IllegalStateException("Corso non impostato!");
        }

        root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #FAFAFA;");

        // Titolo
        Label titolo = new Label("📅 Sessioni del Corso: " + corso.getNomeCorso());
        titolo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FF6600;");

        // Filtri
        HBox filtriBox = createFiltriBox();

        // Numero sessioni
        numeroSessioniLabel = new Label();
        numeroSessioniLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #666;");

        // Lista sessioni con bottone elimina
        sessioniList = new ListView<>();
        sessioniList.setPrefHeight(400);
        setupSessioniListConElimina();
        VBox.setVgrow(sessioniList, Priority.ALWAYS);

        // Pulsanti principali
        HBox pulsantiPrincipali = createPulsantiPrincipali();

        root.getChildren().addAll(titolo, filtriBox, numeroSessioniLabel, sessioniList, pulsantiPrincipali);

        aggiornaLista();

        return root;
    }

    private HBox createFiltriBox() {
        HBox filtriBox = new HBox(15);
        filtriBox.setAlignment(Pos.CENTER_LEFT);
        filtriBox.setPadding(new Insets(10));
        filtriBox.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 8; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-border-radius: 8;");

        Label filtroLabel = new Label("🔍 Filtri:");
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

        Button resetFiltriBtn = new Button("🔄 Reset");
        resetFiltriBtn.setStyle("-fx-cursor: hand;");
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

    private void setupSessioniListConElimina() {
        sessioniList.setCellFactory(lv -> new ListCell<Sessione>() {
            private final HBox container = new HBox(15);
            private final VBox infoBox = new VBox(5);
            private final Button eliminaBtn = new Button("🗑️ Elimina");
            private final Region spacer = new Region();

            {
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                eliminaBtn.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 8; " +
                        "-fx-padding: 8 15;");
                eliminaBtn.setOnMouseEntered(e -> {
                    if (!eliminaBtn.isDisabled()) {
                        eliminaBtn.setStyle(
                            "-fx-background-color: #CC0000; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 8; " +
                            "-fx-padding: 8 15;");
                    }
                });
                eliminaBtn.setOnMouseExited(e -> {
                    if (!eliminaBtn.isDisabled()) {
                        eliminaBtn.setStyle(
                            "-fx-background-color: #FF4444; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 8; " +
                            "-fx-padding: 8 15;");
                    }
                });
                
                eliminaBtn.setOnAction(e -> {
                    Sessione s = getItem();
                    if (s != null) {
                        confermaEliminaSessione(s);
                    }
                });

                container.setAlignment(Pos.CENTER_LEFT);
                container.setPadding(new Insets(10));
            }

            @Override
            protected void updateItem(Sessione s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    infoBox.getChildren().clear();
                    
                    String tipo = (s instanceof Online) ? "🌐 Online" : "🏛️ In Presenza";
                    String inizio = s.getDataInizioSessione().toLocalDate() + " alle " + 
                                   s.getDataInizioSessione().toLocalTime().toString();
                    String fine = s.getDataFineSessione().toLocalDate() + " alle " + 
                                 s.getDataFineSessione().toLocalTime().toString();
                    
                    Label tipoLabel = new Label(tipo);
                    tipoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    
                    Label inizioLabel = new Label("📅 Inizio: " + inizio);
                    Label fineLabel = new Label("🏁 Fine: " + fine);
                    
                    infoBox.getChildren().addAll(tipoLabel, inizioLabel, fineLabel);
                    
                    if (s instanceof Online) {
                        Online online = (Online) s;
                        Label piattaformaLabel = new Label("💻 Piattaforma: " + online.getPiattaformaStreaming());
                        piattaformaLabel.setStyle("-fx-text-fill: #666;");
                        infoBox.getChildren().add(piattaformaLabel);
                    } else if (s instanceof InPresenza) {
                        InPresenza ip = (InPresenza) s;
                        Label luogoLabel = new Label("📍 Luogo: " + ip.getVia() + ", " + ip.getCitta() + " (" + ip.getCAP() + ")");
                        luogoLabel.setStyle("-fx-text-fill: #666;");
                        Label postiLabel = new Label("🪑 Posti: " + ip.getNumeroPosti());
                        postiLabel.setStyle("-fx-text-fill: #666;");
                        int ricette = ip.getRicette() != null ? ip.getRicette().size() : 0;
                        Label ricetteLabel = new Label("🍝 Ricette associate: " + ricette);
                        ricetteLabel.setStyle("-fx-text-fill: #666;");
                        infoBox.getChildren().addAll(luogoLabel, postiLabel, ricetteLabel);
                    }

                    // ✅ AGGIORNA STATO BOTTONE ELIMINA
                    aggiornaStatoBottoneElimina(eliminaBtn);

                    container.getChildren().clear();
                    container.getChildren().addAll(infoBox, spacer, eliminaBtn);
                    
                    setGraphic(container);
                    setText(null);
                    
                    if (s instanceof Online) {
                        setStyle("-fx-background-color: #E3F2FD; -fx-border-color: #2196F3; " +
                                "-fx-border-width: 0 0 0 4; -fx-background-radius: 5; -fx-border-radius: 5;");
                    } else {
                        setStyle("-fx-background-color: #E8F5E9; -fx-border-color: #4CAF50; " +
                                "-fx-border-width: 0 0 0 4; -fx-background-radius: 5; -fx-border-radius: 5;");
                    }
                }
            }
        });
    }

    // ✅ NUOVO METODO: Aggiorna stato bottone elimina
    private void aggiornaStatoBottoneElimina(Button eliminaBtn) {
        boolean isUltimaSessione = corso.getSessioni() != null && corso.getSessioni().size() <= 1;
        
        eliminaBtn.setDisable(isUltimaSessione);
        
        if (isUltimaSessione) {
            eliminaBtn.setStyle(
                "-fx-background-color: #CCCCCC; -fx-text-fill: #666666; " +
                "-fx-font-weight: bold; -fx-background-radius: 8; " +
                "-fx-padding: 8 15; -fx-opacity: 0.6;"
            );
            
            Tooltip tooltip = new Tooltip(
                "⚠️ Impossibile eliminare l'unica sessione del corso.\n" +
                "Aggiungi un'altra sessione prima di eliminare questa."
            );
            tooltip.setStyle("-fx-font-size: 12px;");
            eliminaBtn.setTooltip(tooltip);
        } else {
            eliminaBtn.setStyle(
                "-fx-background-color: #FF4444; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 8; " +
                "-fx-padding: 8 15;"
            );
            eliminaBtn.setTooltip(null);
        }
    }

    private void confermaEliminaSessione(Sessione sessione) {
        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
        conferma.setTitle("⚠️ Conferma Eliminazione");
        conferma.setHeaderText("Eliminare questa sessione?");
        
        String tipo = (sessione instanceof Online) ? "Online" : "In Presenza";
        String dettagli = "Tipo: " + tipo + "\n" +
                         "Inizio: " + sessione.getDataInizioSessione().toLocalDate() + "\n" +
                         "Fine: " + sessione.getDataFineSessione().toLocalDate();
        
        conferma.setContentText(dettagli + "\n\n⚠️ Questa operazione non può essere annullata!");
        
        ButtonType btnConferma = new ButtonType("✅ Conferma", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnAnnulla = new ButtonType("❌ Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        conferma.getButtonTypes().setAll(btnConferma, btnAnnulla);
        
        conferma.showAndWait().ifPresent(response -> {
            if (response == btnConferma) {
                try {
                    if (controller != null) {
                        controller.eliminaSessione(sessione);
                    }
                    
                    // Rimuovi dalla lista locale
                    if (corso.getSessioni() != null) {
                        corso.getSessioni().remove(sessione);
                    }
                    
                    aggiornaLista();
                    showInfo("✅ Successo", "Sessione eliminata correttamente.");
                    
                } catch (IllegalStateException ex) {
                    // ✅ Cattura il vincolo (non dovrebbe succedere con bottone disabilitato)
                    showError("⚠️ Impossibile Eliminare", ex.getMessage());
                    
                } catch (Exception ex) {
                    showError("❌ Errore", "Errore durante l'eliminazione: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
    }

    private HBox createPulsantiPrincipali() {
        HBox pulsantiPrincipali = new HBox(15);
        pulsantiPrincipali.setAlignment(Pos.CENTER);
        pulsantiPrincipali.setPadding(new Insets(15, 0, 0, 0));

        Button creaSessioneBtn = new Button("➕ Crea Sessione");
        creaSessioneBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-pref-width: 180; -fx-pref-height: 45; -fx-font-size: 14px; " +
                "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 10;");
        
        creaSessioneBtn.setOnAction(e -> {
            try {
                GestioneSessioniGUI aggiungiGUI = new GestioneSessioniGUI();
                aggiungiGUI.setModalitaAggiunta(true);
                aggiungiGUI.setController(controller);
                aggiungiGUI.setCorso(corso);
                
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                stage.setTitle("➕ Crea Nuova Sessione");
                
                javafx.scene.Scene scene = new javafx.scene.Scene(aggiungiGUI.getRoot(), 650, 750);
                stage.setScene(scene);
                stage.showAndWait();
                
                aggiornaLista();
                
            } catch (Exception ex) {
                showError("Errore", "Errore nell'apertura della finestra: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        Button chiudiBtn = new Button("❌ Chiudi");
        chiudiBtn.setStyle("-fx-pref-width: 150; -fx-pref-height: 45; -fx-font-size: 14px; " +
                "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 10;");
        chiudiBtn.setOnAction(e -> {
            if (root.getScene() != null) {
                root.getScene().getWindow().hide();
            }
        });

        pulsantiPrincipali.getChildren().addAll(creaSessioneBtn, chiudiBtn);

        return pulsantiPrincipali;
    }

    private void applicaFiltri() {
        ObservableList<Sessione> sessioniFiltrate = FXCollections.observableArrayList();
        String tipoFiltro = filtroTipo.getValue();
        String ricetteFiltro = filtroRicette.getText().trim();

        if (corso.getSessioni() == null || corso.getSessioni().isEmpty()) {
            sessioniList.setItems(sessioniFiltrate);
            numeroSessioniLabel.setText("❌ Nessuna sessione disponibile");
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
                    int ricetteSessione = (s instanceof InPresenza) ? 
                            (((InPresenza) s).getRicette() != null ? ((InPresenza) s).getRicette().size() : 0) : 0;
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
        numeroSessioniLabel.setText(String.format("📊 Sessioni visualizzate: %d di %d totali", 
                sessioniFiltrate.size(), corso.getSessioni().size()));
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
