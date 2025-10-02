
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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.sql.SQLException;
import java.util.List;

public class VisualizzaRicetteGUI {
    
    private GestioneRicette gestioneRicette;
    private GestioneSessioniController sessioniController;
    private InPresenza sessione; // Può essere null per visualizzazione generale
    
    // Liste osservabili
    private ObservableList<Ricetta> ricetteDisponibili;
    private ObservableList<Ricetta> ricetteAssociate;
    
    // Componenti UI
    private ListView<Ricetta> listaDisponibili;
    private ListView<Ricetta> listaAssociate;
    private Label infoLabel;

    // COSTRUTTORE per sessione specifica (esistente)
    public VisualizzaRicetteGUI(GestioneRicette gestioneRicette, GestioneSessioniController sessioniController) {
        this.gestioneRicette = gestioneRicette;
        this.sessioniController = sessioniController;
        this.ricetteDisponibili = FXCollections.observableArrayList();
        this.ricetteAssociate = FXCollections.observableArrayList();
    }
    
    // COSTRUTTORE per visualizzazione generale (NUOVO)
    public VisualizzaRicetteGUI(GestioneRicette gestioneRicette) {
        this.gestioneRicette = gestioneRicette;
        this.sessioniController = null;
        this.sessione = null;
        this.ricetteDisponibili = FXCollections.observableArrayList();
        this.ricetteAssociate = FXCollections.observableArrayList();
    }

    public void setSessione(InPresenza sessione) {
        this.sessione = sessione;
        if (sessione != null) {
            aggiornaRicetteAssociate();
        }
    }

    public void show(Stage stage) {
        boolean isGenerale = (sessione == null && sessioniController == null);
        
        stage.setTitle(isGenerale ? "📖 Tutte le Ricette" : "📖 Gestione Ricette per Sessione");
        stage.setResizable(true);
        
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #FFF8F0, #F8F9FA);");

        // Header
        VBox headerBox = createHeader(isGenerale);
        
        // Info sessione (solo se non è generale)
        infoLabel = new Label();
        if (!isGenerale) {
            aggiornaInfoSessione();
        } else {
            infoLabel.setText("📚 Visualizzazione di tutte le ricette nel database");
            infoLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 14));
            infoLabel.setTextFill(Color.web("#495057"));
            infoLabel.setStyle("-fx-background-color: #E8F4FD; -fx-padding: 15; " +
                              "-fx-background-radius: 10; -fx-border-color: #2196F3; " +
                              "-fx-border-radius: 10; -fx-border-width: 1;");
        }
        
        // Container principale
        HBox mainContainer;
        if (isGenerale) {
            mainContainer = createGeneraleContainer();
        } else {
            mainContainer = createSessioneContainer();
        }

        // Pulsante chiudi
        HBox bottomButtons = new HBox(10);
        bottomButtons.setAlignment(Pos.CENTER);
        bottomButtons.setPadding(new Insets(20, 0, 0, 0));
        
        Button chiudiBtn = createStyledButton("✅ Chiudi", "#6C757D");
        chiudiBtn.setPrefWidth(120);
        chiudiBtn.setOnAction(e -> stage.close());
        bottomButtons.getChildren().add(chiudiBtn);

        root.getChildren().addAll(headerBox, infoLabel, new Separator(), mainContainer, bottomButtons);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        Scene scene = new Scene(scrollPane, isGenerale ? 600 : 950, 700);
        stage.setScene(scene);
        stage.show();
        
        caricaRicetteDisponibili();
    }
    
    private VBox createHeader(boolean isGenerale) {
        VBox headerBox = new VBox(12);
        headerBox.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label(isGenerale ? "📚 Tutte le Ricette" : "📖 Gestione Ricette per Sessione");
        titleLabel.setFont(Font.font("Inter", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2C3E50"));
        
        Label subtitleLabel = new Label(isGenerale ? 
            "Visualizza tutte le ricette disponibili nel sistema" :
            "Associa ricette esistenti o crea nuove ricette per la sessione");
        subtitleLabel.setFont(Font.font("Inter", FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(Color.web("#7F8C8D"));
        
        headerBox.getChildren().addAll(titleLabel, subtitleLabel);
        return headerBox;
    }
    
    // Container per visualizzazione generale (solo lista ricette + crea nuova)
    private HBox createGeneraleContainer() {
        HBox container = new HBox(25);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                          "-fx-border-color: #E0E0E0; -fx-border-radius: 15; -fx-border-width: 1;");

        // Sezione lista ricette
        VBox ricetteSection = new VBox(12);
        ricetteSection.setPrefWidth(400);
        
        Label ricetteLabel = new Label("🍽️ Ricette Disponibili");
        ricetteLabel.setFont(Font.font("Inter", FontWeight.BOLD, 16));
        ricetteLabel.setTextFill(Color.web("#2C3E50"));
        
        // Campo ricerca
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Cerca ricette...");
        searchField.setPrefHeight(35);
        searchField.setStyle("-fx-background-radius: 8; -fx-border-color: #CED4DA; -fx-border-radius: 8;");
        
        listaDisponibili = new ListView<>(ricetteDisponibili);
        listaDisponibili.setPrefHeight(400);
        listaDisponibili.setStyle("-fx-background-radius: 12; -fx-border-color: #DEE2E6; " +
                                 "-fx-border-radius: 12; -fx-border-width: 1;");

        setupRicetteCellFactory();
        setupSearchFilter(searchField);
        
        Label contatoreTotaleLabel = new Label();
        contatoreTotaleLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 11));
        contatoreTotaleLabel.setTextFill(Color.web("#6C757D"));
        
        ricetteDisponibili.addListener((javafx.collections.ListChangeListener<Ricetta>) c -> {
            contatoreTotaleLabel.setText("📊 Totale: " + ricetteDisponibili.size() + " ricette");
        });
        
        ricetteSection.getChildren().addAll(ricetteLabel, searchField, listaDisponibili, contatoreTotaleLabel);
        
        // Sezione pulsanti
        VBox buttonSection = new VBox(20);
        buttonSection.setAlignment(Pos.CENTER);
        buttonSection.setPrefWidth(120);
        
        Button btnCreaRicetta = createStyledButton("📝\nCrea Nuova\nRicetta", "#007BFF");
        btnCreaRicetta.setPrefHeight(80);
        btnCreaRicetta.setOnAction(e -> creaRicettaGenerale());
        
        Button btnRefresh = createStyledButton("🔄\nAggiorna", "#6C757D");
        btnRefresh.setPrefHeight(60);
        btnRefresh.setOnAction(e -> caricaRicetteDisponibili());

        buttonSection.getChildren().addAll(btnCreaRicetta, btnRefresh);

        container.getChildren().addAll(ricetteSection, buttonSection);
        return container;
    }
    
    // Container per sessione specifica (tre colonne come prima)
    private HBox createSessioneContainer() {
        HBox container = new HBox(25);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                          "-fx-border-color: #E0E0E0; -fx-border-radius: 15; -fx-border-width: 1;");

        // Sezione ricette disponibili
        VBox availableSection = createAvailableSection();
        
        // Sezione pulsanti centrali
        VBox buttonSection = createButtonSection();
        
        // Sezione ricette associate
        VBox associatedSection = createAssociatedSection();

        container.getChildren().addAll(availableSection, buttonSection, associatedSection);
        return container;
    }
    
    private VBox createAvailableSection() {
        VBox section = new VBox(12);
        section.setPrefWidth(320);
        
        Label availableLabel = new Label("🍽️ Ricette Disponibili");
        availableLabel.setFont(Font.font("Inter", FontWeight.BOLD, 16));
        availableLabel.setTextFill(Color.web("#2C3E50"));
        
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Cerca ricette...");
        searchField.setPrefHeight(35);
        searchField.setStyle("-fx-background-radius: 8; -fx-border-color: #CED4DA; -fx-border-radius: 8;");
        
        listaDisponibili = new ListView<>(ricetteDisponibili);
        listaDisponibili.setPrefHeight(350);
        listaDisponibili.setStyle("-fx-background-radius: 12; -fx-border-color: #DEE2E6; " +
                                 "-fx-border-radius: 12; -fx-border-width: 1;");

        setupRicetteCellFactory();
        setupSearchFilter(searchField);
        
        Label contatoreTotaleLabel = new Label();
        contatoreTotaleLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 11));
        contatoreTotaleLabel.setTextFill(Color.web("#6C757D"));
        
        ricetteDisponibili.addListener((javafx.collections.ListChangeListener<Ricetta>) c -> {
            contatoreTotaleLabel.setText("📊 Totale: " + ricetteDisponibili.size() + " ricette");
        });
        
        section.getChildren().addAll(availableLabel, searchField, listaDisponibili, contatoreTotaleLabel);
        return section;
    }

    private VBox createButtonSection() {
        VBox section = new VBox(20);
        section.setAlignment(Pos.CENTER);
        section.setPrefWidth(120);
        
        Button btnAggiungi = createStyledButton("➡️\nAggiungi", "#28A745");
        btnAggiungi.setPrefHeight(60);
        btnAggiungi.setOnAction(e -> aggiungiRicettaSelezionata());
        
        Button btnRimuovi = createStyledButton("⬅️\nRimuovi", "#DC3545");
        btnRimuovi.setPrefHeight(60);
        btnRimuovi.setOnAction(e -> rimuoviRicettaSelezionata());
        
        Separator separator = new Separator();
        
        Button btnCreaRicetta = createStyledButton("📝\nCrea Nuova", "#007BFF");
        btnCreaRicetta.setPrefHeight(60);
        btnCreaRicetta.setOnAction(e -> creaRicettaDialog());
        
        Button btnRefresh = createStyledButton("🔄\nAggiorna", "#6C757D");
        btnRefresh.setPrefHeight(45);
        btnRefresh.setOnAction(e -> {
            caricaRicetteDisponibili();
            aggiornaRicetteAssociate();
        });

        section.getChildren().addAll(btnAggiungi, btnRimuovi, separator, btnCreaRicetta, btnRefresh);
        return section;
    }

    private VBox createAssociatedSection() {
        VBox section = new VBox(12);
        section.setPrefWidth(320);
        
        Label associatedLabel = new Label("✅ Ricette Associate");
        associatedLabel.setFont(Font.font("Inter", FontWeight.BOLD, 16));
        associatedLabel.setTextFill(Color.web("#28A745"));
        
        listaAssociate = new ListView<>(ricetteAssociate);
        listaAssociate.setPrefHeight(350);
        listaAssociate.setStyle("-fx-background-radius: 12; -fx-border-color: #28A745; " +
                               "-fx-border-radius: 12; -fx-border-width: 2;");

        setupAssociateCellFactory();
        
        Label contatoreAssociateLabel = new Label();
        contatoreAssociateLabel.setFont(Font.font("Inter", FontWeight.BOLD, 12));
        contatoreAssociateLabel.setTextFill(Color.web("#28A745"));
        
        ricetteAssociate.addListener((javafx.collections.ListChangeListener<Ricetta>) c -> {
            contatoreAssociateLabel.setText("📊 Associate: " + ricetteAssociate.size() + " ricette");
        });
        
        section.getChildren().addAll(associatedLabel, listaAssociate, contatoreAssociateLabel);
        return section;
    }
    
    private void setupRicetteCellFactory() {
        listaDisponibili.setCellFactory(listView -> new ListCell<Ricetta>() {
            @Override
            protected void updateItem(Ricetta ricetta, boolean empty) {
                super.updateItem(ricetta, empty);
                if (empty || ricetta == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox cellContent = new VBox(4);
                    cellContent.setPadding(new Insets(8));
                    
                    Label nameLabel = new Label(ricetta.getNome());
                    nameLabel.setFont(Font.font("Inter", FontWeight.BOLD, 13));
                    
                    Label timeLabel = new Label("⏱️ " + ricetta.getTempoPreparazione() + " minuti");
                    timeLabel.setFont(Font.font("Inter", FontWeight.NORMAL, 11));
                    timeLabel.setTextFill(Color.web("#6C757D"));
                    
                    cellContent.getChildren().addAll(nameLabel, timeLabel);

                    // Evidenzia se già associata (solo se c'è una sessione)
                    if (sessione != null && sessione.getRicette().contains(ricetta)) {
                        cellContent.setStyle("-fx-background-color: #D4EDDA; -fx-background-radius: 8;");
                        nameLabel.setTextFill(Color.web("#155724"));
                        Label associatedLabel = new Label("✅ Già associata");
                        associatedLabel.setFont(Font.font("Inter", FontWeight.BOLD, 9));
                        associatedLabel.setTextFill(Color.web("#28A745"));
                        cellContent.getChildren().add(associatedLabel);
                    } else {
                        cellContent.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8;");
                        nameLabel.setTextFill(Color.web("#212529"));
                    }

                    setGraphic(cellContent);
                    setText(null);
                }
            }
        });
    }
    
    private void setupAssociateCellFactory() {
        if (listaAssociate != null) {
            listaAssociate.setCellFactory(listView -> new ListCell<Ricetta>() {
                @Override
                protected void updateItem(Ricetta ricetta, boolean empty) {
                    super.updateItem(ricetta, empty);
                    if (empty || ricetta == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        VBox cellContent = new VBox(4);
                        cellContent.setPadding(new Insets(8));
                        cellContent.setStyle("-fx-background-color: #D4EDDA; -fx-background-radius: 8;");
                        
                        Label nameLabel = new Label(ricetta.getNome());
                        nameLabel.setFont(Font.font("Inter", FontWeight.BOLD, 13));
                        nameLabel.setTextFill(Color.web("#155724"));
                        
                        Label timeLabel = new Label("⏱️ " + ricetta.getTempoPreparazione() + " minuti");
                        timeLabel.setFont(Font.font("Inter", FontWeight.NORMAL, 11));
                        timeLabel.setTextFill(Color.web("#28A745"));
                        
                        cellContent.getChildren().addAll(nameLabel, timeLabel);
                        setGraphic(cellContent);
                        setText(null);
                    }
                }
            });
        }
    }
    
    private void setupSearchFilter(TextField searchField) {
        searchField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.trim().isEmpty()) {
                listaDisponibili.setItems(ricetteDisponibili);
            } else {
                ObservableList<Ricetta> filtrate = FXCollections.observableArrayList();
                String filtro = val.toLowerCase().trim();
                for (Ricetta r : ricetteDisponibili) {
                    if (r.getNome().toLowerCase().contains(filtro)) {
                        filtrate.add(r);
                    }
                }
                listaDisponibili.setItems(filtrate);
            }
        });
    }
    
    private void aggiornaInfoSessione() {
        if (sessione != null) {
            String infoText = String.format("🏛️ Sessione: %s, %s | 👥 Posti: %d | 📅 %s", 
                sessione.getVia(), 
                sessione.getCitta(), 
                sessione.getNumeroPosti(),
                sessione.getDataInizioSessione() != null ? 
                    sessione.getDataInizioSessione().toLocalDate().toString() : "Data non impostata"
            );
            infoLabel.setText(infoText);
            infoLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 13));
            infoLabel.setTextFill(Color.web("#495057"));
            infoLabel.setStyle("-fx-background-color: #E3F2FD; -fx-padding: 12; " +
                              "-fx-background-radius: 8; -fx-border-color: #2196F3; " +
                              "-fx-border-radius: 8; -fx-border-width: 1;");
        }
    }

    // LOGICA FUNZIONALE
    private void aggiungiRicettaSelezionata() {
        Ricetta selected = listaDisponibili.getSelectionModel().getSelectedItem();
        if (selected != null && sessione != null && sessioniController != null) {
            if (sessione.getRicette().contains(selected)) {
                showInfo("Info", "La ricetta '" + selected.getNome() + "' è già associata alla sessione.");
                return;
            }
            
            try {
                sessioniController.aggiungiRicettaAInPresenza(sessione, selected);
                aggiornaRicetteAssociate();
                listaDisponibili.refresh();
                showInfo("Successo", "Ricetta '" + selected.getNome() + "' aggiunta alla sessione!");
                
            } catch (SQLException ex) {
                ex.printStackTrace();
                showError("Errore durante l'aggiunta della ricetta: " + ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Errore imprevisto: " + ex.getMessage());
            }
        } else {
            if (selected == null) {
                showError("Seleziona una ricetta da aggiungere.");
            } else if (sessione == null) {
                showError("Nessuna sessione specificata.");
            }
        }
    }
    
    private void rimuoviRicettaSelezionata() {
        Ricetta selected = listaAssociate.getSelectionModel().getSelectedItem();
        if (selected != null && sessione != null && sessioniController != null) {
            try {
                sessioniController.rimuoviRicettaDaSessione(sessione, selected);
                aggiornaRicetteAssociate();
                listaDisponibili.refresh();
                showInfo("Successo", "Ricetta rimossa dalla sessione!");
                
            } catch (SQLException ex) {
                showError("Errore durante la rimozione: " + ex.getMessage());
            }
        } else {
            if (selected == null) {
                showError("Seleziona una ricetta da rimuovere.");
            }
        }
    }
    
    private void caricaRicetteDisponibili() {
        try {
            List<Ricetta> tutteRicette = gestioneRicette.getAllRicette();
            ricetteDisponibili.clear();
            ricetteDisponibili.addAll(tutteRicette);
        } catch (SQLException e) {
            showError("Errore caricamento ricette: " + e.getMessage());
        }
    }
    
    private void aggiornaRicetteAssociate() {
        ricetteAssociate.clear();
        if (sessione != null && sessione.getRicette() != null) {
            ricetteAssociate.addAll(sessione.getRicette());
        }
    }
    
    // Dialog crea ricetta per sessione specifica
    private void creaRicettaDialog() {
        creaRicettaDialog(true);
    }
    
    // Dialog crea ricetta generale
    private void creaRicettaGenerale() {
        creaRicettaDialog(false);
    }

    private void creaRicettaDialog(boolean associaASessione) {
        Stage dialog = new Stage();
        dialog.initOwner((Stage) listaDisponibili.getScene().getWindow());
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Crea Nuova Ricetta");

        VBox container = new VBox(15);
        container.setPadding(new Insets(25));
        container.setStyle("-fx-background-color: #F8F9FA;");

        Label titleLabel = new Label("📝 Crea Nuova Ricetta");
        titleLabel.setFont(Font.font("Inter", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#2C3E50"));

        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome ricetta");
        nomeField.setPrefHeight(40);
        nomeField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-font-size: 13;");

        TextField tempoField = new TextField();
        tempoField.setPromptText("Tempo preparazione (minuti)");
        tempoField.setPrefHeight(40);
        tempoField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-font-size: 13;");

        CheckBox associateCheckbox = null;
        if (associaASessione && sessione != null) {
            associateCheckbox = new CheckBox("Associa automaticamente alla sessione");
            associateCheckbox.setSelected(true);
            associateCheckbox.setFont(Font.font("Inter", FontWeight.MEDIUM, 12));
        }

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        Button saveBtn = createStyledButton("💾 Salva", "#28A745");
        Button cancelBtn = createStyledButton("❌ Annulla", "#6C757D");

        CheckBox finalAssociateCheckbox = associateCheckbox;
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
                
                // Aggiungi alla lista disponibili
                ricetteDisponibili.add(nuovaRicetta);

                // Associa alla sessione se richiesto
                if (finalAssociateCheckbox != null && finalAssociateCheckbox.isSelected() && 
                    sessione != null && sessioniController != null) {
                    sessioniController.aggiungiRicettaAInPresenza(sessione, nuovaRicetta);
                    aggiornaRicetteAssociate();
                }

                showInfo("Successo", "Ricetta '" + nome + "' creata con successo!");
                dialog.close();

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
            tempoField
        );
        
        if (associateCheckbox != null) {
            container.getChildren().add(associateCheckbox);
        }
        
        container.getChildren().add(buttonBox);

        Scene scene = new Scene(container, 450, associateCheckbox != null ? 380 : 320);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // METODI DI UTILITÀ
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setFont(Font.font("Inter", FontWeight.BOLD, 12));
        button.setTextFill(Color.WHITE);
        button.setPrefWidth(100);
        button.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 10 15; " +
            "-fx-cursor: hand; " +
            "-fx-text-alignment: center;", color
        ));
        
        button.setOnMouseEntered(e -> button.setStyle(button.getStyle() + "-fx-opacity: 0.8; -fx-scale-x: 1.02; -fx-scale-y: 1.02;"));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle().replace("-fx-opacity: 0.8; -fx-scale-x: 1.02; -fx-scale-y: 1.02;", "")));
        
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
