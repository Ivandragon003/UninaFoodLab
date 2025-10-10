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
    if (sessione == null) return;
    
    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
    confirmAlert.setTitle("⚠️ Conferma Eliminazione");
    confirmAlert.setHeaderText(null);
    confirmAlert.setContentText(null);
    
    DialogPane dialogPane = confirmAlert.getDialogPane();
    dialogPane.getStyleClass().remove("alert");
    
    dialogPane.setStyle(
        "-fx-background-color: #FFF3CD;" +
        "-fx-border-color: #FFB84D;" +
        "-fx-border-width: 2px;" +
        "-fx-border-radius: 10px;" +
        "-fx-background-radius: 10px;" +
        "-fx-padding: 25px;" +
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);"
    );
    
    VBox content = new VBox(15);
    content.setAlignment(Pos.CENTER);
    
    Label iconLabel = new Label("⚠️");
    iconLabel.setStyle("-fx-font-size: 48px;");
    
    Label titleLabel = new Label("Conferma Eliminazione");
    titleLabel.setStyle(
        "-fx-font-size: 18px;" +
        "-fx-font-weight: bold;" +
        "-fx-text-fill: #856404;" +
        "-fx-wrap-text: true;"
    );
    
    String tipo = (sessione instanceof Online) ? "Online" : "In Presenza";
    String sessioneInfo = "Tipo: " + tipo + "\n" +
                         "Inizio: " + sessione.getDataInizioSessione().toLocalDate() + "\n" +
                         "Fine: " + sessione.getDataFineSessione().toLocalDate();
    
    Label messageLabel = new Label(
        "Sei sicuro di voler eliminare questa sessione?\n\n" +
        sessioneInfo + "\n\n" +
        "⚠️ Questa operazione è irreversibile!"
    );
    messageLabel.setStyle(
        "-fx-font-size: 14px;" +
        "-fx-text-fill: #856404;" +
        "-fx-wrap-text: true;" +
        "-fx-text-alignment: center;"
    );
    messageLabel.setMaxWidth(450);
    messageLabel.setWrapText(true);
    
    content.getChildren().addAll(iconLabel, titleLabel, messageLabel);
    dialogPane.setContent(content);
    
    // ✅ Stile bottoni
    Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
    okButton.setText("Elimina");
    okButton.setStyle(
        "-fx-background-color: #FF6B6B;" +
        "-fx-text-fill: white;" +
        "-fx-font-size: 14px;" +
        "-fx-font-weight: bold;" +
        "-fx-padding: 12 30 12 30;" +
        "-fx-background-radius: 8px;"
    );
    
    Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
    cancelButton.setText("Annulla");
    cancelButton.setStyle(
        "-fx-background-color: #6C757D;" +
        "-fx-text-fill: white;" +
        "-fx-font-size: 14px;" +
        "-fx-font-weight: bold;" +
        "-fx-padding: 12 30 12 30;" +
        "-fx-background-radius: 8px;"
    );
    
    confirmAlert.showAndWait().ifPresent(response -> {
        if (response == ButtonType.OK) {
            try {
                if (controller != null) {
                    controller.eliminaSessione(sessione);
                }
                
                if (corso.getSessioni() != null) {
                    corso.getSessioni().remove(sessione);
                }
                
                aggiornaLista();
                
                showStyledSuccessDialog(
                    "✅ Sessione Eliminata", 
                    "La sessione è stata eliminata con successo!"
                );
                
            } catch (IllegalStateException ex) {
                showStyledValidationDialog("⚠️ Impossibile Eliminare", ex.getMessage());
            } catch (Exception ex) {
                showStyledErrorDialog("❌ Errore", 
                    "Errore durante l'eliminazione:\n" + ex.getMessage());
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
    
 private void showStyledSuccessDialog(String title, String message) {
     Alert alert = new Alert(Alert.AlertType.INFORMATION);
     alert.setTitle(title);
     alert.setHeaderText(null);
     alert.setContentText(null);
     
     DialogPane dialogPane = alert.getDialogPane();
     dialogPane.getStyleClass().remove("alert");
     
     dialogPane.setStyle(
         "-fx-background-color: #D4EDDA;" +
         "-fx-border-color: #28A745;" +
         "-fx-border-width: 2px;" +
         "-fx-border-radius: 10px;" +
         "-fx-background-radius: 10px;" +
         "-fx-padding: 25px;" +
         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);"
     );
     
     VBox content = new VBox(15);
     content.setAlignment(Pos.CENTER);
     
     Label iconLabel = new Label("✅");
     iconLabel.setStyle("-fx-font-size: 48px;");
     
     Label titleLabel = new Label(title);
     titleLabel.setStyle(
         "-fx-font-size: 18px;" +
         "-fx-font-weight: bold;" +
         "-fx-text-fill: #155724;" +
         "-fx-wrap-text: true;" +
         "-fx-text-alignment: center;"
     );
     titleLabel.setMaxWidth(450);
     titleLabel.setWrapText(true);
     
     Label messageLabel = new Label(message);
     messageLabel.setStyle(
         "-fx-font-size: 14px;" +
         "-fx-text-fill: #155724;" +
         "-fx-wrap-text: true;" +
         "-fx-text-alignment: center;"
     );
     messageLabel.setMaxWidth(450);
     messageLabel.setWrapText(true);
     
     content.getChildren().addAll(iconLabel, titleLabel, messageLabel);
     dialogPane.setContent(content);
     
     Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
     okButton.setText("OK");
     okButton.setStyle(
         "-fx-background-color: #28A745;" +
         "-fx-text-fill: white;" +
         "-fx-font-size: 14px;" +
         "-fx-font-weight: bold;" +
         "-fx-padding: 12 40 12 40;" +
         "-fx-background-radius: 8px;" +
         "-fx-cursor: hand;"
     );
     
     alert.showAndWait();
 }


 private void showStyledErrorDialog(String title, String message) {
     Alert alert = new Alert(Alert.AlertType.ERROR);
     alert.setTitle(title);
     alert.setHeaderText(null);
     alert.setContentText(null);
     
     DialogPane dialogPane = alert.getDialogPane();
     dialogPane.getStyleClass().remove("alert");
     
     dialogPane.setStyle(
         "-fx-background-color: #FFE5E5;" +
         "-fx-border-color: #FF6B6B;" +
         "-fx-border-width: 2px;" +
         "-fx-border-radius: 10px;" +
         "-fx-background-radius: 10px;" +
         "-fx-padding: 25px;" +
         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);"
     );
     
     VBox content = new VBox(15);
     content.setAlignment(Pos.CENTER);
     
     Label iconLabel = new Label("❌");
     iconLabel.setStyle("-fx-font-size: 48px;");
     
     Label titleLabel = new Label(title);
     titleLabel.setStyle(
         "-fx-font-size: 18px;" +
         "-fx-font-weight: bold;" +
         "-fx-text-fill: #721c24;" +
         "-fx-wrap-text: true;" +
         "-fx-text-alignment: center;"
     );
     titleLabel.setMaxWidth(450);
     titleLabel.setWrapText(true);
     
     Label messageLabel = new Label(message);
     messageLabel.setStyle(
         "-fx-font-size: 14px;" +
         "-fx-text-fill: #721c24;" +
         "-fx-wrap-text: true;" +
         "-fx-text-alignment: center;"
     );
     messageLabel.setMaxWidth(450);
     messageLabel.setWrapText(true);
     
     content.getChildren().addAll(iconLabel, titleLabel, messageLabel);
     dialogPane.setContent(content);
     
     Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
     okButton.setText("OK");
     okButton.setStyle(
         "-fx-background-color: #FF6B6B;" +
         "-fx-text-fill: white;" +
         "-fx-font-size: 14px;" +
         "-fx-font-weight: bold;" +
         "-fx-padding: 12 40 12 40;" +
         "-fx-background-radius: 8px;" +
         "-fx-cursor: hand;"
     );
     
     alert.showAndWait();
 }

 private void showStyledValidationDialog(String title, String message) {
     Alert alert = new Alert(Alert.AlertType.WARNING);
     alert.setTitle(title);
     alert.setHeaderText(null);
     alert.setContentText(null);
     
     DialogPane dialogPane = alert.getDialogPane();
     dialogPane.getStyleClass().remove("alert");
     
     dialogPane.setStyle(
         "-fx-background-color: #FFF3CD;" +
         "-fx-border-color: #FFB84D;" +
         "-fx-border-width: 2px;" +
         "-fx-border-radius: 10px;" +
         "-fx-background-radius: 10px;" +
         "-fx-padding: 25px;" +
         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);"
     );
     
     VBox content = new VBox(15);
     content.setAlignment(Pos.CENTER);
     
     Label iconLabel = new Label("⚠️");
     iconLabel.setStyle("-fx-font-size: 48px;");
     
     Label titleLabel = new Label(title);
     titleLabel.setStyle(
         "-fx-font-size: 18px;" +
         "-fx-font-weight: bold;" +
         "-fx-text-fill: #856404;" +
         "-fx-wrap-text: true;" +
         "-fx-text-alignment: center;"
     );
     titleLabel.setMaxWidth(450);
     titleLabel.setWrapText(true);
     
     Label messageLabel = new Label(message);
     messageLabel.setStyle(
         "-fx-font-size: 14px;" +
         "-fx-text-fill: #856404;" +
         "-fx-wrap-text: true;" +
         "-fx-text-alignment: center;"
     );
     messageLabel.setMaxWidth(450);
     messageLabel.setWrapText(true);
     
     content.getChildren().addAll(iconLabel, titleLabel, messageLabel);
     dialogPane.setContent(content);
     
     Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
     okButton.setText("OK");
     okButton.setStyle(
         "-fx-background-color: #FF9966;" +
         "-fx-text-fill: white;" +
         "-fx-font-size: 14px;" +
         "-fx-font-weight: bold;" +
         "-fx-padding: 12 40 12 40;" +
         "-fx-background-radius: 8px;" +
         "-fx-cursor: hand;"
     );
     
     alert.showAndWait();
 }

}
