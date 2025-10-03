package Gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Ricetta;
import service.GestioneRicette;
import util.StyleHelper;
import java.util.List;

public class VisualizzaRicetteGUI extends Stage {
    
    private GestioneRicette gestioneRicette;
    private Ricetta selectedRicetta = null;
    private boolean selectionMode = false;
    
    private TextField searchField;
    private ListView<Ricetta> listaRicette;
    private TextArea dettagliArea;
    
    private ObservableList<Ricetta> allRicette;
    private FilteredList<Ricetta> filteredRicette;
    
    public VisualizzaRicetteGUI(GestioneRicette gestioneRicette) {
        this.gestioneRicette = gestioneRicette;
        setTitle("Visualizza Ricette");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(true); // RIDIMENSIONABILE CON MOUSE
        
        loadRicetteFromDatabase();
        createLayout();
    }
    
    public void setSelectionMode(boolean selectionMode) {
        this.selectionMode = selectionMode;
        if (selectionMode) {
            setTitle("Seleziona Ricetta");
        }
    }
    
    private void loadRicetteFromDatabase() {
        try {
            if (gestioneRicette != null) {
                List<Ricetta> ricette = gestioneRicette.getAllRicette();
                allRicette = FXCollections.observableArrayList(ricette);
                filteredRicette = new FilteredList<>(allRicette);
            } else {
                // Fallback se service non disponibile
                allRicette = FXCollections.observableArrayList();
                filteredRicette = new FilteredList<>(allRicette);
            }
        } catch (Exception e) {
            allRicette = FXCollections.observableArrayList();
            filteredRicette = new FilteredList<>(allRicette);
            System.err.println("Errore caricamento ricette dal database: " + e.getMessage());
        }
    }
    
    private void createLayout() {
        // ROOT con SFONDO ARANCIONE ridimensionabile
        StackPane rootPane = new StackPane();
        rootPane.setMinSize(800, 500);
        rootPane.setPrefSize(1000, 700);
        
        // Sfondo arancione
        Region background = new Region();
        StyleHelper.applyBackgroundGradient(background);
        
        VBox mainContainer = new VBox(25);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(30));
        
        String titleText = selectionMode ? "📖 Seleziona una Ricetta" : "📚 Visualizza Ricette";
        Label title = new Label(titleText);
        title.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);
        
        HBox contentBox = createContentBox();
        VBox.setVgrow(contentBox, Priority.ALWAYS);
        
        mainContainer.getChildren().addAll(title, contentBox);
        VBox.setVgrow(mainContainer, Priority.ALWAYS);
        rootPane.getChildren().addAll(background, mainContainer);
        
        Scene scene = new Scene(rootPane, 1000, 700);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);
    }
    
    private HBox createContentBox() {
        HBox contentBox = new HBox(20);
        contentBox.setAlignment(Pos.TOP_CENTER);
        
        // Sezione sinistra: lista ricette
        VBox leftSection = createLeftSection();
        leftSection.setPrefWidth(450);
        HBox.setHgrow(leftSection, Priority.SOMETIMES);
        
        // Sezione destra: dettagli ricetta
        VBox rightSection = createRightSection();
        rightSection.setPrefWidth(500);
        HBox.setHgrow(rightSection, Priority.SOMETIMES);
        
        contentBox.getChildren().addAll(leftSection, rightSection);
        return contentBox;
    }
    
    private VBox createLeftSection() {
        VBox section = StyleHelper.createSection();
        section.setSpacing(15);
        VBox.setVgrow(section, Priority.ALWAYS);
        
        Label sectionTitle = new Label("🔍 Cerca Ricette");
        sectionTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));
        
        // Campo ricerca
        VBox searchBox = new VBox(10);
        Label searchLabel = StyleHelper.createLabel("Cerca per nome:");
        searchField = StyleHelper.createTextField("Cerca per nome ricetta...");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateFilters());
        searchBox.getChildren().addAll(searchLabel, searchField);
        
        // Lista ricette
        VBox listaBox = new VBox(10);
        Label listaLabel = StyleHelper.createLabel("Ricette disponibili (" + allRicette.size() + "):");
        
        listaRicette = new ListView<>();
        listaRicette.setItems(filteredRicette);
        listaRicette.setPrefHeight(400);
        listaRicette.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(listaRicette, Priority.ALWAYS); // Cresce con la finestra
        listaRicette.setStyle("-fx-background-radius: 8; -fx-border-color: " + StyleHelper.BORDER_LIGHT + "; " +
                            "-fx-border-radius: 8; -fx-border-width: 1;");
        
        // Cell factory per ricette - RISPETTA MODEL RICETTA
        listaRicette.setCellFactory(listView -> new ListCell<Ricetta>() {
            @Override
            protected void updateItem(Ricetta ricetta, boolean empty) {
                super.updateItem(ricetta, empty);
                if (empty || ricetta == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    VBox cellBox = new VBox(5);
                    cellBox.setPadding(new Insets(10));
                    
                    // NOME RICETTA (rispetta model)
                    Label nameLabel = new Label("📖 " + ricetta.getNome());
                    nameLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 14));
                    nameLabel.setTextFill(Color.BLACK);
                    
                    // TEMPO PREPARAZIONE (rispetta model)
                    Label detailsLabel = new Label("⏱️ " + ricetta.getTempoPreparazione() + " minuti");
                    detailsLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
                    detailsLabel.setTextFill(Color.GRAY);
                                       
                    
                    cellBox.getChildren().addAll(nameLabel, detailsLabel);
                    setGraphic(cellBox);
                    setText(null);
                    
                    // Stile selezione
                    if (isSelected()) {
                        setStyle("-fx-background-color: #e6f3ff; -fx-border-color: " + StyleHelper.PRIMARY_ORANGE + "; " +
                                "-fx-border-width: 2; -fx-border-radius: 8;");
                    } else {
                        setStyle("-fx-background-color: white; -fx-border-radius: 5;");
                    }
                }
            }
        });
        
        // Listener per selezione
        listaRicette.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showRicettaDetails(newVal);
            }
        });
        
        listaBox.getChildren().addAll(listaLabel, listaRicette);
        VBox.setVgrow(listaBox, Priority.ALWAYS);
        
        // Pulsanti
        HBox buttonsBox = createButtonsBox();
        
        section.getChildren().addAll(sectionTitle, searchBox, listaBox, buttonsBox);
        return section;
    }
    
    private void updateFilters() {
        filteredRicette.setPredicate(ricetta -> {
            // Filtro per nome
            String searchText = searchField.getText();
            if (searchText != null && !searchText.trim().isEmpty()) {
                String searchLower = searchText.toLowerCase();
                return ricetta.getNome().toLowerCase().contains(searchLower);
            }
            return true;
        });
    }
    
    private VBox createRightSection() {
        VBox section = StyleHelper.createSection();
        section.setSpacing(15);
        VBox.setVgrow(section, Priority.ALWAYS);
        
        Label sectionTitle = new Label("📋 Dettagli Ricetta");
        sectionTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));
        
        // Area dettagli
        dettagliArea = StyleHelper.createTextArea("Seleziona una ricetta per vedere i dettagli...");
        dettagliArea.setEditable(false);
        dettagliArea.setPrefRowCount(15);
        dettagliArea.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(dettagliArea, Priority.ALWAYS);
        
        section.getChildren().addAll(sectionTitle, dettagliArea);
        return section;
    }
    
    private void showRicettaDetails(Ricetta ricetta) {
        // Mostra dettagli ricetta - RISPETTA MODEL
        StringBuilder details = new StringBuilder();
        details.append("📖 RICETTA: ").append(ricetta.getNome()).append("\n\n");
        details.append("⏱️ TEMPO PREPARAZIONE: ").append(ricetta.getTempoPreparazione()).append(" minuti\n\n");      
        
        dettagliArea.setText(details.toString());
    }
    
    private HBox createButtonsBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        if (!selectionMode) {
            // Modalità normale: gestione ricette
            Button nuovaRicettaBtn = StyleHelper.createSuccessButton("+ Nuova Ricetta");
            nuovaRicettaBtn.setOnAction(e -> apriCreaRicetta());
            
            Button refreshBtn = StyleHelper.createInfoButton("🔄 Aggiorna");
            refreshBtn.setOnAction(e -> {
                loadRicetteFromDatabase();
                updateFilters();
                showAlert("Aggiornamento", "Lista ricette aggiornata dal database!");
            });
            
            Button chiudiBtn = new Button("❌ Chiudi");
            chiudiBtn.setStyle("-fx-background-color: " + StyleHelper.NEUTRAL_GRAY + "; " +
                              "-fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;");
            chiudiBtn.setOnAction(e -> close());
            
            buttonBox.getChildren().addAll(nuovaRicettaBtn, refreshBtn, chiudiBtn);
        } else {
            // Modalità selezione
            Button selezionaBtn = StyleHelper.createPrimaryButton("✅ Seleziona");
            selezionaBtn.setOnAction(e -> {
                selectedRicetta = listaRicette.getSelectionModel().getSelectedItem();
                if (selectedRicetta == null) {
                    showAlert("Nessuna selezione", "Seleziona una ricetta dalla lista.");
                    return;
                }
                close();
            });
            
            Button annullaBtn = new Button("❌ Annulla");
            annullaBtn.setStyle("-fx-background-color: " + StyleHelper.NEUTRAL_GRAY + "; " +
                               "-fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;");
            annullaBtn.setOnAction(e -> {
                selectedRicetta = null;
                close();
            });
            
            buttonBox.getChildren().addAll(annullaBtn, selezionaBtn);
        }
        
        return buttonBox;
    }
    
    private void apriCreaRicetta() {
        try {
            CreaRicettaGUI creaRicettaGUI = new CreaRicettaGUI(gestioneRicette);
            Ricetta nuova = creaRicettaGUI.showAndReturn();
            
            if (nuova != null) {
                // Ricarica dal database
                loadRicetteFromDatabase();
                updateFilters();
                showAlert("Successo", "Ricetta '" + nuova.getNome() + "' creata e salvata nel database!");
            }
        } catch (Exception e) {
            showAlert("Errore", "Errore nell'apertura creazione ricetta: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.contains("Successo") || title.contains("Aggiornamento") ? 
                               Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public Ricetta showAndReturn() {
        showAndWait();
        return selectedRicetta;
    }
    
    public void show(Stage owner) {
        if (owner != null) {
            initOwner(owner);
        }
        show();
    }
}