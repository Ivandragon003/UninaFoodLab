package Gui;

import controller.GestioneSessioniController;
import model.*;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.time.LocalDateTime;

public class GestioneSessioniGUI {

    private CorsoCucina corso;
    private GestioneSessioniController controller;
    private VisualizzaSessioniGUI parentGUI;
    private Stage primaryStage;
    
    private Sessione sessione; 
    private boolean modalitaAggiunta = false; 
    
    // Elementi form
    private ComboBox<String> tipoCombo;
    private DatePicker dataInizioPicker, dataFinePicker;
    private TextField oraInizioField, oraFineField;
    private TextField piattaformaField, viaField, cittaField, postiField, capField;
    private VBox campiOnlineBox, campiPresenzaBox;
    
    // Elementi dettagli (solo per visualizzazione)
    private VBox dettagliContainer;
    private boolean modalitaDettagli = false;

    public void setCorso(CorsoCucina corso) {
        this.corso = corso;
    }
    
    public void setController(GestioneSessioniController controller) {
        this.controller = controller;
    }
    
    public void setParentGUI(VisualizzaSessioniGUI parentGUI) {
        this.parentGUI = parentGUI;
    }
    
    public void setSessione(Sessione sessione) {
        this.sessione = sessione;
        this.modalitaDettagli = true;
        this.modalitaAggiunta = false;
    }
    
    public void setModalitaAggiunta(boolean modalitaAggiunta) {
        this.modalitaAggiunta = modalitaAggiunta;
        this.modalitaDettagli = false;
    }

    public void start(Stage stage) {
        this.primaryStage = stage;
        
        if (modalitaDettagli && sessione != null) {
            setupDettagliView(stage);
        } else if (modalitaAggiunta) {
            setupAggiungiView(stage);
        } else if (sessione != null) {
            modalitaDettagli = false;
            modalitaAggiunta = false;
            setupModificaView(stage);
        } else {
            throw new IllegalStateException("Modalità non definita correttamente!");
        }
        
        stage.show();
    }
    
    private void setupDettagliView(Stage stage) {
        stage.setTitle("Dettagli Sessione");
        
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        
        // Titolo
        Label titolo = new Label("Dettagli Sessione");
        titolo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        // Card con i dettagli
        VBox cardDettagli = createDettagliCard();
        
        // Pulsanti dettagli
        HBox pulsantiBox = createPulsantiDettagli();
        
        root.getChildren().addAll(titolo, cardDettagli, pulsantiBox);
        
        Scene scene = new Scene(root, 500, 600);
        stage.setScene(scene);
    }
    
    private void setupAggiungiView(Stage stage) {
        stage.setTitle("Aggiungi Nuova Sessione");
        
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        
        // Titolo
        Label titolo = new Label("Aggiungi Nuova Sessione");
        titolo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        // Form
        VBox formBox = createFormBox();
        
        // Pulsanti aggiungi
        HBox pulsantiBox = createPulsantiAggiungi();
        
        root.getChildren().addAll(titolo, formBox, pulsantiBox);
        
        Scene scene = new Scene(root, 600, 700);
        stage.setScene(scene);
        
        inizializzaFormVuoto();
    }
    
    private void setupModificaView(Stage stage) {
        stage.setTitle("Modifica Sessione");
        
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        
        // Titolo
        Label titolo = new Label("Modifica Sessione");
        titolo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        // Form
        VBox formBox = createFormBox();
        
        // Pulsanti modifica
        HBox pulsantiBox = createPulsantiModifica();
        
        root.getChildren().addAll(titolo, formBox, pulsantiBox);
        
        Scene scene = new Scene(root, 600, 700);
        stage.setScene(scene);
        
        popolaFormConSessione();
    }
    
    private VBox createDettagliCard() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; " +
                     "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        
        // Tipo sessione
        String tipoIcon = (sessione instanceof Online) ? "Online" : "In Presenza";
        String tipoText = (sessione instanceof Online) ? "Online" : "In Presenza";
        Label tipoLabel = createDetailRow("Tipo:", tipoText);
        
        // Date e orari
        Label inizioLabel = createDetailRow("Data/Ora Inizio:", 
                sessione.getDataInizioSessione().toString());
        Label fineLabel = createDetailRow("Data/Ora Fine:", 
                sessione.getDataFineSessione().toString());
        
        // Placeholder per ricette
        Label ricetteLabel = createDetailRow("Numero Ricette:", "0"); // s.getNumeroRicette()
        
        Label modalitaLabel = createDetailRow("Modalita Cucina:", "Solo in presenza");
        
        card.getChildren().addAll(tipoLabel, inizioLabel, fineLabel, ricetteLabel, modalitaLabel);
        
        // Separatore
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #E0E0E0;");
        card.getChildren().add(separator);
        
        // Dettagli specifici per tipo
        if (sessione instanceof Online) {
            Online online = (Online) sessione;
            Label piattaformaLabel = createDetailRow("Piattaforma:", 
                    online.getPiattaformaStreaming());
            card.getChildren().add(piattaformaLabel);
        } else {
            InPresenza presenza = (InPresenza) sessione;
            Label viaLabel = createDetailRow("Via:", presenza.getVia());
            Label cittaLabel = createDetailRow("Citta:", presenza.getCitta());
            Label postiLabel = createDetailRow("Posti Disponibili:", 
                    String.valueOf(presenza.getNumeroPosti()));
            Label capLabel = createDetailRow("CAP:", String.valueOf(presenza.getCAP()));
            
            card.getChildren().addAll(viaLabel, cittaLabel, postiLabel, capLabel);
        }
        
        return card;
    }
    
    private Label createDetailRow(String campo, String valore) {
        Label label = new Label(campo + " " + valore);
        label.setStyle("-fx-font-size: 14px; -fx-padding: 5 0; -fx-font-weight: bold;");
        label.setWrapText(true);
        return label;
    }
    
    private VBox createFormBox() {
        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(20));
        formBox.setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #E0E0E0; " +
                        "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        
        // Tipo sessione
        HBox tipoBox = new HBox(10);
        tipoBox.setAlignment(Pos.CENTER_LEFT);
        Label tipoLabel = new Label("Tipo Sessione:");
        tipoLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 120;");
        tipoCombo = new ComboBox<>();
        tipoCombo.getItems().addAll("Online", "In Presenza");
        tipoCombo.setOnAction(e -> aggiornaVisibilitaCampi());
        tipoBox.getChildren().addAll(tipoLabel, tipoCombo);
        
        // Date e orari
        GridPane dateGrid = new GridPane();
        dateGrid.setHgap(15);
        dateGrid.setVgap(10);
        
        dataInizioPicker = new DatePicker();
        oraInizioField = new TextField();
        oraInizioField.setPromptText("HH:MM");
        
        dataFinePicker = new DatePicker();
        oraFineField = new TextField();
        oraFineField.setPromptText("HH:MM");
        
        dateGrid.add(createFormLabel("Data Inizio:"), 0, 0);
        dateGrid.add(dataInizioPicker, 1, 0);
        dateGrid.add(createFormLabel("Ora Inizio:"), 2, 0);
        dateGrid.add(oraInizioField, 3, 0);
        
        dateGrid.add(createFormLabel("Data Fine:"), 0, 1);
        dateGrid.add(dataFinePicker, 1, 1);
        dateGrid.add(createFormLabel("Ora Fine:"), 2, 1);
        dateGrid.add(oraFineField, 3, 1);
        
        // Campi specifici Online
        campiOnlineBox = new VBox(10);
        piattaformaField = new TextField();
        piattaformaField.setPromptText("es. Zoom, Teams, Meet...");
        HBox piattaformaBox = new HBox(10);
        piattaformaBox.setAlignment(Pos.CENTER_LEFT);
        piattaformaBox.getChildren().addAll(createFormLabel("Piattaforma:"), piattaformaField);
        campiOnlineBox.getChildren().add(piattaformaBox);
        
        // Campi specifici In Presenza
        campiPresenzaBox = new VBox(10);
        GridPane presenzaGrid = new GridPane();
        presenzaGrid.setHgap(15);
        presenzaGrid.setVgap(10);
        
        viaField = new TextField();
        cittaField = new TextField();
        postiField = new TextField();
        capField = new TextField();
        
        presenzaGrid.add(createFormLabel("Via:"), 0, 0);
        presenzaGrid.add(viaField, 1, 0);
        presenzaGrid.add(createFormLabel("Citta:"), 2, 0);
        presenzaGrid.add(cittaField, 3, 0);
        
        presenzaGrid.add(createFormLabel("Posti:"), 0, 1);
        presenzaGrid.add(postiField, 1, 1);
        presenzaGrid.add(createFormLabel("CAP:"), 2, 1);
        presenzaGrid.add(capField, 3, 1);
        
        campiPresenzaBox.getChildren().add(presenzaGrid);
        
        formBox.getChildren().addAll(tipoBox, new Separator(), dateGrid, 
                                    new Separator(), campiOnlineBox, campiPresenzaBox);
        
        return formBox;
    }
    
    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-min-width: 80;");
        return label;
    }
    
    private HBox createPulsantiDettagli() {
        HBox pulsantiBox = new HBox(15);
        pulsantiBox.setAlignment(Pos.CENTER);
        pulsantiBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button indietroBtn = new Button("Indietro");
        indietroBtn.setStyle("-fx-pref-width: 120; -fx-pref-height: 35;");
        indietroBtn.setOnAction(e -> primaryStage.close());
        
        Button eliminaBtn = new Button("Elimina");
        eliminaBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; " +
                          "-fx-pref-width: 120; -fx-pref-height: 35;");
        eliminaBtn.setOnAction(e -> eliminaSessioneConferma());
        
        Button modificaBtn = new Button("Modifica");
        modificaBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                           "-fx-pref-width: 120; -fx-pref-height: 35;");
        modificaBtn.setOnAction(e -> apriModifica());
        
        pulsantiBox.getChildren().addAll(indietroBtn, eliminaBtn, modificaBtn);
        
        return pulsantiBox;
    }
    
    private HBox createPulsantiModifica() {
        HBox pulsantiBox = new HBox(15);
        pulsantiBox.setAlignment(Pos.CENTER);
        pulsantiBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button tornaIndietroBtn = new Button("Torna Indietro");
        tornaIndietroBtn.setStyle("-fx-pref-width: 150; -fx-pref-height: 40;");
        tornaIndietroBtn.setOnAction(e -> confermaAnnullaModifiche());
        
        Button salvaBtn = new Button("Salva");
        salvaBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                         "-fx-pref-width: 150; -fx-pref-height: 40;");
        salvaBtn.setOnAction(e -> salvaModifiche());
        
        pulsantiBox.getChildren().addAll(tornaIndietroBtn, salvaBtn);
        
        return pulsantiBox;
    }
    
    private HBox createPulsantiAggiungi() {
        HBox pulsantiBox = new HBox(15);
        pulsantiBox.setAlignment(Pos.CENTER);
        pulsantiBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button tornaIndietroBtn = new Button("Torna Indietro");
        tornaIndietroBtn.setStyle("-fx-pref-width: 150; -fx-pref-height: 40;");
        tornaIndietroBtn.setOnAction(e -> primaryStage.close());
        
        Button aggiungiBtn = new Button("Aggiungi Sessione");
        aggiungiBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                           "-fx-pref-width: 150; -fx-pref-height: 40;");
        aggiungiBtn.setOnAction(e -> aggiungiSessione());
        
        pulsantiBox.getChildren().addAll(tornaIndietroBtn, aggiungiBtn);
        
        return pulsantiBox;
    }
    
    private void inizializzaFormVuoto() {
        tipoCombo.setValue("Online");
        aggiornaVisibilitaCampi();
    }
    
    private void popolaFormConSessione() {
        if (sessione == null) return;
        
        tipoCombo.setValue(sessione instanceof Online ? "Online" : "In Presenza");
        dataInizioPicker.setValue(sessione.getDataInizioSessione().toLocalDate());
        oraInizioField.setText(sessione.getDataInizioSessione().toLocalTime().toString());
        dataFinePicker.setValue(sessione.getDataFineSessione().toLocalDate());
        oraFineField.setText(sessione.getDataFineSessione().toLocalTime().toString());
        
        if (sessione instanceof Online) {
            piattaformaField.setText(((Online) sessione).getPiattaformaStreaming());
        } else {
            InPresenza presenza = (InPresenza) sessione;
            viaField.setText(presenza.getVia());
            cittaField.setText(presenza.getCitta());
            postiField.setText(String.valueOf(presenza.getNumeroPosti()));
            capField.setText(String.valueOf(presenza.getCAP()));
        }
        
        aggiornaVisibilitaCampi();
    }
    
    private void aggiornaVisibilitaCampi() {
        boolean isOnline = "Online".equals(tipoCombo.getValue());
        campiOnlineBox.setVisible(isOnline);
        campiOnlineBox.setManaged(isOnline);
        campiPresenzaBox.setVisible(!isOnline);
        campiPresenzaBox.setManaged(!isOnline);
    }
    
    private void eliminaSessioneConferma() {
        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
        conferma.setTitle("Conferma Eliminazione");
        conferma.setHeaderText(null);
        conferma.setContentText("Sei sicuro di voler eliminare questa sessione?");
        
        // Configura i pulsanti personalizzati
        ButtonType siButton = new ButtonType("Si");
        ButtonType noButton = new ButtonType("No");
        conferma.getButtonTypes().setAll(siButton, noButton);
        
        conferma.showAndWait().ifPresent(response -> {
            if (response == siButton) {
                controller.eliminaSessione(sessione);
                
                Alert successo = new Alert(Alert.AlertType.INFORMATION);
                successo.setTitle("Successo");
                successo.setHeaderText(null);
                successo.setContentText("Sessione eliminata con successo!");
                successo.showAndWait();
                
                primaryStage.close();
            }
            // Se risponde "No", il messaggio si chiude automaticamente senza fare nulla
        });
    }
    
    private void apriModifica() {
        GestioneSessioniGUI modificaGUI = new GestioneSessioniGUI();
        modificaGUI.setSessione(sessione);
        modificaGUI.setController(controller);
        modificaGUI.setParentGUI(parentGUI);
        modificaGUI.setModalitaAggiunta(false); // Importante: modalità modifica
        
        Stage modificaStage = new Stage();
        modificaStage.initOwner(primaryStage);
        modificaGUI.start(modificaStage);
        
        // Chiudi dettagli quando si apre modifica
        modificaStage.setOnShowing(e -> primaryStage.close());
    }
    
    private void salvaModifiche() {
        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
        conferma.setTitle("Conferma Salvataggio");
        conferma.setHeaderText("Salvataggio Modifiche");
        conferma.setContentText("Sei sicuro di voler salvare le modifiche?");
        
        conferma.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Validazione campi obbligatori
                    if (dataInizioPicker.getValue() == null || oraInizioField.getText().trim().isEmpty() ||
                        dataFinePicker.getValue() == null || oraFineField.getText().trim().isEmpty()) {
                        showError("Errore", "Tutti i campi data/ora sono obbligatori!");
                        return;
                    }
                    
                    // Parsing date/ore
                    LocalDateTime inizio = LocalDateTime.of(dataInizioPicker.getValue(),
                            LocalTime.parse(oraInizioField.getText().trim()));
                    LocalDateTime fine = LocalDateTime.of(dataFinePicker.getValue(),
                            LocalTime.parse(oraFineField.getText().trim()));
                    
                    // Validazione logica
                    if (inizio.isAfter(fine) || inizio.equals(fine)) {
                        showError("Errore", "La data/ora di inizio deve essere precedente a quella di fine!");
                        return;
                    }
                    
                    // Applica le modifiche
                    sessione.setDataInizioSessione(inizio);
                    sessione.setDataFineSessione(fine);
                    
                    if (sessione instanceof Online) {
                        if (piattaformaField.getText().trim().isEmpty()) {
                            showError("Errore", "La piattaforma è obbligatoria per sessioni online!");
                            return;
                        }
                        ((Online) sessione).setPiattaformaStreaming(piattaformaField.getText().trim());
                    } else {
                        InPresenza presenza = (InPresenza) sessione;
                        
                        // Validazione campi in presenza
                        if (viaField.getText().trim().isEmpty() || cittaField.getText().trim().isEmpty()) {
                            showError("Errore", "Via e Città sono obbligatorie per sessioni in presenza!");
                            return;
                        }
                        
                        presenza.setVia(viaField.getText().trim());
                        presenza.setCitta(cittaField.getText().trim());
                        
                        try {
                            int posti = Integer.parseInt(postiField.getText().trim());
                            int cap = Integer.parseInt(capField.getText().trim());
                            
                            if (posti <= 0) {
                                showError("Errore", "Il numero di posti deve essere maggiore di zero!");
                                return;
                            }
                            
                            if (cap < 10000 || cap > 99999) {
                                showError("Errore", "Il CAP deve essere un numero di 5 cifre!");
                                return;
                            }
                            
                            presenza.setNumeroPosti(posti);
                            presenza.setCAP(cap);
                        } catch (NumberFormatException ex) {
                            showError("Errore", "Posti e CAP devono essere numeri validi!");
                            return;
                        }
                    }
                    
                    Alert successo = new Alert(Alert.AlertType.INFORMATION);
                    successo.setTitle("Successo");
                    successo.setHeaderText(null);
                    successo.setContentText("Modifiche salvate con successo!");
                    successo.showAndWait();
                    
                    primaryStage.close();
                    
                } catch (Exception ex) {
                    showError("Errore", "Si è verificato un errore durante il salvataggio: " + ex.getMessage());
                }
            }
        });
    }
    
    private void aggiungiSessione() {
        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
        conferma.setTitle("Conferma Aggiunta");
        conferma.setHeaderText("Aggiunta Nuova Sessione");
        conferma.setContentText("Sei sicuro di voler aggiungere questa sessione?");
        
        conferma.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Validazione campi obbligatori
                    if (tipoCombo.getValue() == null || dataInizioPicker.getValue() == null || 
                        oraInizioField.getText().trim().isEmpty() || dataFinePicker.getValue() == null || 
                        oraFineField.getText().trim().isEmpty()) {
                        showError("Errore", "Tutti i campi obbligatori devono essere compilati!");
                        return;
                    }
                    
                    // Parsing date/ore
                    LocalDateTime inizio = LocalDateTime.of(dataInizioPicker.getValue(),
                            LocalTime.parse(oraInizioField.getText().trim()));
                    LocalDateTime fine = LocalDateTime.of(dataFinePicker.getValue(),
                            LocalTime.parse(oraFineField.getText().trim()));
                    
                    // Validazione logica
                    if (inizio.isAfter(fine) || inizio.equals(fine)) {
                        showError("Errore", "La data/ora di inizio deve essere precedente a quella di fine!");
                        return;
                    }
                    
                    Sessione nuovaSessione;
                    
                    if ("Online".equals(tipoCombo.getValue())) {
                        if (piattaformaField.getText().trim().isEmpty()) {
                            showError("Errore", "La piattaforma è obbligatoria per sessioni online!");
                            return;
                        }
                        nuovaSessione = new Online(inizio, fine, piattaformaField.getText().trim());
                    } else {
                        // Validazione campi in presenza
                        if (viaField.getText().trim().isEmpty() || cittaField.getText().trim().isEmpty() ||
                            postiField.getText().trim().isEmpty() || capField.getText().trim().isEmpty()) {
                            showError("Errore", "Tutti i campi sono obbligatori per sessioni in presenza!");
                            return;
                        }
                        
                        try {
                            int posti = Integer.parseInt(postiField.getText().trim());
                            int cap = Integer.parseInt(capField.getText().trim());
                            
                            if (posti <= 0) {
                                showError("Errore", "Il numero di posti deve essere maggiore di zero!");
                                return;
                            }
                            
                            if (cap < 10000 || cap > 99999) {
                                showError("Errore", "Il CAP deve essere un numero di 5 cifre!");
                                return;
                            }
                            
                            nuovaSessione = new InPresenza(inizio, fine, viaField.getText().trim(), 
                                                         cittaField.getText().trim(), posti, cap);
                        } catch (NumberFormatException ex) {
                            showError("Errore", "Posti e CAP devono essere numeri validi!");
                            return;
                        }
                    }
                    
                    // Aggiungi la sessione al corso
                    controller.aggiungiSessione(nuovaSessione);
                    
                    Alert successo = new Alert(Alert.AlertType.INFORMATION);
                    successo.setTitle("Successo");
                    successo.setHeaderText(null);
                    successo.setContentText("Nuova sessione aggiunta con successo!");
                    successo.showAndWait();
                    
                    primaryStage.close();
                    
                } catch (Exception ex) {
                    showError("Errore", "Si è verificato un errore durante l'aggiunta: " + ex.getMessage());
                }
            }
        });
    }
    
    private void confermaAnnullaModifiche() {
        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
        conferma.setTitle("Conferma Annullamento");
        conferma.setHeaderText("Annullamento Modifiche");
        conferma.setContentText("Sei sicuro di voler annullare le modifiche?\n\n" +
                               "Tutte le modifiche non salvate verranno perse.");
        
        conferma.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                primaryStage.close();
            }
        });
    }
    
    private void showError(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }}