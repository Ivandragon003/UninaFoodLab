package Gui;

import controller.GestioneSessioniController;
import model.*;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.sql.SQLException;

public class GestioneSessioniGUI {

    private CorsoCucina corso;
    private GestioneSessioniController controller;
    private Stage primaryStage;

    private Sessione sessione;
    private boolean modalitaAggiunta = false;

    private ComboBox<String> tipoCombo;
    private DatePicker dataInizioPicker, dataFinePicker;
    private TextField oraInizioField, oraFineField;
    private TextField piattaformaField, viaField, cittaField, postiField, capField;
    private VBox campiOnlineBox, campiPresenzaBox;

    private VBox root;

    public void setCorso(CorsoCucina corso) { this.corso = corso; }

    public void setController(GestioneSessioniController controller) { this.controller = controller; }

    public void setSessione(Sessione sessione) {
        this.sessione = sessione;
        this.modalitaAggiunta = false;
    }

    public void setModalitaAggiunta(boolean modalitaAggiunta) {
        this.modalitaAggiunta = modalitaAggiunta;
        this.sessione = null;
    }

    
    public VBox getRoot() {
        if (root == null) {
            root = buildRoot();
            if (!modalitaAggiunta && sessione != null) {
                popolaFormConSessione();
            } else if (modalitaAggiunta) {
                if (tipoCombo != null) {
                    tipoCombo.setValue("Online");
                    aggiornaVisibilitaCampi();
                }
            }
        }
        return root;
    }

    public void start(Stage stage) {
        this.primaryStage = stage;
        Scene scene = new Scene(getRoot(), 600, 700);
        stage.setScene(scene);
        stage.setTitle("Gestione Sessione");
        stage.show();
    }


    private VBox buildRoot() {
        VBox formContainer = new VBox(20);
        formContainer.setPadding(new Insets(25));

        Label titolo = new Label(modalitaAggiunta ? "Aggiungi Nuova Sessione" : "Modifica Sessione");
        titolo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        VBox formBox = createFormBox();
        HBox pulsantiBox = createPulsanti();

        formContainer.getChildren().addAll(titolo, formBox, pulsantiBox);
        return formContainer;
    }

    private VBox createFormBox() {
        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(20));
        formBox.setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #E0E0E0; " +
                "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

        HBox tipoBox = new HBox(10);
        tipoBox.setAlignment(Pos.CENTER_LEFT);
        Label tipoLabel = new Label("Tipo Sessione:");
        tipoLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 120;");
        tipoCombo = new ComboBox<>();
        tipoCombo.getItems().addAll("Online", "In Presenza");
        tipoCombo.setOnAction(e -> aggiornaVisibilitaCampi());
        tipoBox.getChildren().addAll(tipoLabel, tipoCombo);

        GridPane dateGrid = new GridPane();
        dateGrid.setHgap(15); dateGrid.setVgap(10);
        dataInizioPicker = new DatePicker(); oraInizioField = new TextField(); oraInizioField.setPromptText("HH:MM");
        dataFinePicker = new DatePicker(); oraFineField = new TextField(); oraFineField.setPromptText("HH:MM");

        dateGrid.add(new Label("Data Inizio:"), 0, 0);
        dateGrid.add(dataInizioPicker, 1, 0);
        dateGrid.add(new Label("Ora Inizio:"), 2, 0);
        dateGrid.add(oraInizioField, 3, 0);
        dateGrid.add(new Label("Data Fine:"), 0, 1);
        dateGrid.add(dataFinePicker, 1, 1);
        dateGrid.add(new Label("Ora Fine:"), 2, 1);
        dateGrid.add(oraFineField, 3, 1);

        campiOnlineBox = new VBox(10);
        piattaformaField = new TextField(); piattaformaField.setPromptText("Zoom, Teams, Meet...");
        HBox piattaformaBox = new HBox(10); piattaformaBox.setAlignment(Pos.CENTER_LEFT);
        piattaformaBox.getChildren().addAll(new Label("Piattaforma:"), piattaformaField);
        campiOnlineBox.getChildren().add(piattaformaBox);

        campiPresenzaBox = new VBox(10);
        GridPane presenzaGrid = new GridPane();
        presenzaGrid.setHgap(15); presenzaGrid.setVgap(10);
        viaField = new TextField(); cittaField = new TextField(); postiField = new TextField(); capField = new TextField();
        presenzaGrid.add(new Label("Via:"), 0, 0); presenzaGrid.add(viaField, 1, 0);
        presenzaGrid.add(new Label("Città:"), 2, 0); presenzaGrid.add(cittaField, 3, 0);
        presenzaGrid.add(new Label("Posti:"), 0, 1); presenzaGrid.add(postiField, 1, 1);
        presenzaGrid.add(new Label("CAP:"), 2, 1); presenzaGrid.add(capField, 3, 1);
        campiPresenzaBox.getChildren().add(presenzaGrid);

        formBox.getChildren().addAll(tipoBox, new Separator(), dateGrid, new Separator(), campiOnlineBox, campiPresenzaBox);
        return formBox;
    }

    private HBox createPulsanti() {
        HBox pulsantiBox = new HBox(15);
        pulsantiBox.setAlignment(Pos.CENTER_RIGHT);
        pulsantiBox.setPadding(new Insets(20, 0, 0, 0));

        Button annullaBtn = new Button("Annulla");
        annullaBtn.setOnAction(e -> {
            if (primaryStage != null) primaryStage.close();
            else {
            }
        });

        Button salvaBtn = new Button(modalitaAggiunta ? "Salva" : "Aggiorna");
        salvaBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        salvaBtn.setOnAction(e -> salvaSessione());

        pulsantiBox.getChildren().addAll(annullaBtn, salvaBtn);

 
        if (!modalitaAggiunta && sessione instanceof InPresenza || modalitaAggiunta && (tipoCombo != null && "In Presenza".equals(tipoCombo.getValue()))) {
            Button aggiungiRicettaBtn = new Button("Aggiungi Ricetta");
            aggiungiRicettaBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            aggiungiRicettaBtn.setOnAction(e -> {
                if (controller != null && sessione instanceof InPresenza ip) {
                    controller.apriSelezionaRicettaGUI(ip);
                } else 
                {
                    showError("Prima salva la sessione", "Per associare ricette devi prima salvare la sessione.");
                }
            });
            pulsantiBox.getChildren().add(aggiungiRicettaBtn);
        }

        return pulsantiBox;
    }

    private void popolaFormConSessione() {
        if (sessione == null) return;
        tipoCombo.setValue(sessione instanceof Online ? "Online" : "In Presenza");
        if (sessione.getDataInizioSessione() != null) {
            dataInizioPicker.setValue(sessione.getDataInizioSessione().toLocalDate());
            oraInizioField.setText(sessione.getDataInizioSessione().toLocalTime().toString());
        }
        if (sessione.getDataFineSessione() != null) {
            dataFinePicker.setValue(sessione.getDataFineSessione().toLocalDate());
            oraFineField.setText(sessione.getDataFineSessione().toLocalTime().toString());
        }

        if (sessione instanceof Online) {
            piattaformaField.setText(((Online) sessione).getPiattaformaStreaming());
        } else if (sessione instanceof InPresenza ip) {
            viaField.setText(ip.getVia());
            cittaField.setText(ip.getCitta());
            postiField.setText(String.valueOf(ip.getNumeroPosti()));
            capField.setText(String.valueOf(ip.getCAP()));
        }

        aggiornaVisibilitaCampi();
    }

    private void aggiornaVisibilitaCampi() {
        boolean isOnline = tipoCombo != null && "Online".equals(tipoCombo.getValue());
        if (campiOnlineBox != null && campiPresenzaBox != null) {
            campiOnlineBox.setVisible(isOnline); campiOnlineBox.setManaged(isOnline);
            campiPresenzaBox.setVisible(!isOnline); campiPresenzaBox.setManaged(!isOnline);
        }
    }

    private void salvaSessione() {
        try {
            if (dataInizioPicker.getValue() == null || dataFinePicker.getValue() == null) {
                showError("Errore", "Compila le date.");
                return;
            }
            LocalDateTime inizio = LocalDateTime.of(dataInizioPicker.getValue(), LocalTime.parse(oraInizioField.getText().trim()));
            LocalDateTime fine = LocalDateTime.of(dataFinePicker.getValue(), LocalTime.parse(oraFineField.getText().trim()));

            if (inizio.isAfter(fine) || inizio.equals(fine)) {
                showError("Errore", "La data/ora di inizio deve precedere quella di fine");
                return;
            }

            if ("Online".equals(tipoCombo.getValue())) {
                Online online = modalitaAggiunta ? new Online(inizio, fine, piattaformaField.getText().trim())
                        : (Online) sessione;
                online.setDataInizioSessione(inizio);
                online.setDataFineSessione(fine);
                online.setPiattaformaStreaming(piattaformaField.getText().trim());

                if (modalitaAggiunta) {
                    if (controller != null) {
                        try {
                            controller.aggiungiSessione(online, null); 
                            this.sessione = online;
                            this.modalitaAggiunta = false;
                            showInfo("Sessione online salvata.");
                        } catch (SQLException ex) {
                            showError("Errore DB", ex.getMessage());
                        }
                    } else {
                        showInfo("Sessione creata in memoria (controller non impostato).");
                    }
                } else {
                    if (controller != null) {
                        try {
                            controller.aggiornaSessione(sessione, online);
                            this.sessione = online;
                            showInfo("Sessione aggiornata.");
                        } catch (SQLException ex) {
                            showError("Errore DB", ex.getMessage());
                        }
                    }
                }

            } else { 
                int posti = Integer.parseInt(postiField.getText().trim());
                int cap = Integer.parseInt(capField.getText().trim());
                InPresenza ip = modalitaAggiunta ? new InPresenza(inizio, fine, viaField.getText(), cittaField.getText(), posti, cap)
                        : (InPresenza) sessione;
                ip.setDataInizioSessione(inizio); ip.setDataFineSessione(fine);
                ip.setVia(viaField.getText()); ip.setCitta(cittaField.getText());
                ip.setNumeroPosti(posti); ip.setCAP(cap);

                if (modalitaAggiunta) {
                    if (controller != null) {
                        try {
                            controller.aggiungiSessione(ip, null); 
                            this.sessione = ip;
                            this.modalitaAggiunta = false;
                            showInfo("Sessione in presenza salvata.");
                        } catch (SQLException ex) {
                            showError("Errore DB", ex.getMessage());
                        }
                    } else {
                        showInfo("Sessione creata in memoria (controller non impostato).");
                    }
                } else {
                    if (controller != null) {
                        try {
                            controller.aggiornaSessione(sessione, ip);
                            this.sessione = ip;
                            showInfo("Sessione aggiornata.");
                        } catch (SQLException ex) {
                            showError("Errore DB", ex.getMessage());
                        }
                    }
                }
            }

            if (primaryStage != null) primaryStage.close();

        } catch (Exception ex) {
            showError("Errore", "Errore durante il salvataggio: " + ex.getMessage());
        }
    }

    private void showError(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }

    private void showInfo(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
