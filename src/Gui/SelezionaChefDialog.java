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
import model.Chef;
import util.StyleHelper;
import java.util.List;

public class SelezionaChefDialog extends Stage {
    
    private Chef selectedChef = null;
    private ListView<Chef> listaChef;
    private TextField searchField;
    private ObservableList<Chef> allChefs;
    private FilteredList<Chef> filteredChefs;
    
    public SelezionaChefDialog(List<Chef> chefDisponibili) {
        setTitle("Seleziona Chef");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);
        
        this.allChefs = FXCollections.observableArrayList(chefDisponibili);
        this.filteredChefs = new FilteredList<>(allChefs);
        
        createLayout();
    }
    
    private void createLayout() {
        // ROOT con sfondo arancione come LOGIN
        StackPane rootPane = new StackPane();
        rootPane.setPrefSize(500, 600);
        
        // Sfondo arancione
        Region background = new Region();
        StyleHelper.applyBackgroundGradient(background);
        
        VBox mainContainer = new VBox(25);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(30));
        
        Label title = new Label("👨‍🍳 Seleziona un Chef");
        title.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);
        
        VBox formCard = StyleHelper.createSection();
        formCard.setSpacing(20);
        formCard.setPrefWidth(420);
        
        // Campo ricerca
        VBox searchBox = new VBox(8);
        Label searchLabel = StyleHelper.createLabel("🔍 Cerca Chef per nome:");
        searchField = StyleHelper.createTextField("Digita nome o cognome...");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateFilter(newValue));
        searchBox.getChildren().addAll(searchLabel, searchField);
        
        // Lista chef
        VBox listaBox = new VBox(8);
        Label listaLabel = StyleHelper.createLabel("Chef Disponibili:");
        
        listaChef = new ListView<>();
        listaChef.setItems(filteredChefs);
        listaChef.setPrefHeight(300);
        listaChef.setStyle("-fx-background-radius: 8; -fx-border-color: " + StyleHelper.BORDER_LIGHT + "; " +
                         "-fx-border-radius: 8; -fx-border-width: 1;");
        
        // Cell factory per mostrare NOME E COGNOME (non username)
        listaChef.setCellFactory(listView -> new ListCell<Chef>() {
            @Override
            protected void updateItem(Chef chef, boolean empty) {
                super.updateItem(chef, empty);
                if (empty || chef == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    VBox cellBox = new VBox(5);
                    cellBox.setPadding(new Insets(10));
                    
                    // NOME E COGNOME - TESTO NERO
                    Label nameLabel = new Label("👨‍🍳 " + chef.getNome() + " " + chef.getCognome());
                    nameLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 16));
                    nameLabel.setTextFill(Color.BLACK); // NERO per visibilità
                    
           
                    
                    // Anni esperienza
                    Label expLabel = new Label("📅 Esperienza: " + chef.getAnniEsperienza() + " anni");
                    expLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
                    expLabel.setTextFill(Color.GRAY);
                    
                    // Disponibilità
                    Label availLabel = new Label(chef.getDisponibilita() ? "✅ Disponibile" : "❌ Non disponibile");
                    availLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
                    availLabel.setTextFill(chef.getDisponibilita() ? 
                                         Color.web(StyleHelper.SUCCESS_GREEN) : 
                                         Color.web(StyleHelper.ERROR_RED));
                    
                    cellBox.getChildren().addAll(nameLabel, expLabel, availLabel);
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
        
        listaBox.getChildren().addAll(listaLabel, listaChef);
        
        // Info selezione
        Label infoLabel = new Label("💡 Seleziona un chef dalla lista per aggiungerlo al corso");
        infoLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
        infoLabel.setTextFill(Color.web(StyleHelper.INFO_BLUE));
        infoLabel.setWrapText(true);
        
        // Statistiche
        Label statsLabel = new Label();
        updateStatsLabel(statsLabel);
        
        // Aggiorna statistiche quando cambia il filtro
        filteredChefs.addListener((javafx.collections.ListChangeListener<Chef>) change -> updateStatsLabel(statsLabel));
        
        // Pulsanti
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));
        
        Button annullaBtn = new Button("❌ Annulla");
        annullaBtn.setPrefWidth(130);
        annullaBtn.setStyle("-fx-background-color: " + StyleHelper.NEUTRAL_GRAY + "; " +
                           "-fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-font-weight: bold;");
        annullaBtn.setOnAction(e -> {
            selectedChef = null;
            close();
        });
        
        Button confermaBtn = StyleHelper.createPrimaryButton("✅ Aggiungi Chef");
        confermaBtn.setPrefWidth(130);
        confermaBtn.setOnAction(e -> {
            Chef selected = listaChef.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (!selected.getDisponibilita()) {
                    showAlert("Chef non disponibile", 
                             "Lo chef " + selected.getNome() + " " + selected.getCognome() + 
                             " non è attualmente disponibile per nuovi corsi.");
                    return;
                }
                selectedChef = selected;
                close();
            } else {
                showAlert("Nessuna selezione", "Seleziona un chef dalla lista prima di confermare.");
            }
        });
        
        buttonBox.getChildren().addAll(annullaBtn, confermaBtn);
        
        // Aggiorna stato pulsante conferma
        listaChef.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                confermaBtn.setDisable(false);
                confermaBtn.setStyle(confermaBtn.getStyle().replace("-fx-opacity: 0.5;", ""));
            } else {
                confermaBtn.setDisable(true);
                confermaBtn.setStyle(confermaBtn.getStyle() + "-fx-opacity: 0.5;");
            }
        });
        
        // Inizialmente conferma disabilitato
        confermaBtn.setDisable(true);
        confermaBtn.setStyle(confermaBtn.getStyle() + "-fx-opacity: 0.5;");
        
        formCard.getChildren().addAll(searchBox, listaBox, infoLabel, statsLabel, buttonBox);
        mainContainer.getChildren().addAll(title, formCard);
        
        rootPane.getChildren().addAll(background, mainContainer);
        
        Scene scene = new Scene(rootPane, 500, 600);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);
    }
    
    private void updateFilter(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredChefs.setPredicate(null);
        } else {
            String lowerCaseFilter = searchText.toLowerCase();
            filteredChefs.setPredicate(chef -> {
                // Ricerca per NOME o COGNOME
                String nomeCompleto = (chef.getNome() + " " + chef.getCognome()).toLowerCase();
                return nomeCompleto.contains(lowerCaseFilter) ||
                       chef.getNome().toLowerCase().contains(lowerCaseFilter) ||
                       chef.getCognome().toLowerCase().contains(lowerCaseFilter);
            });
        }
    }
    
    private void updateStatsLabel(Label statsLabel) {
        int total = allChefs.size();
        int filtered = filteredChefs.size();
        int available = (int) filteredChefs.stream().filter(Chef::getDisponibilita).count();
        
        String text = String.format("📊 Totali: %d | Mostrati: %d | Disponibili: %d", 
                                   total, filtered, available);
        statsLabel.setText(text);
        statsLabel.setFont(javafx.scene.text.Font.font("Roboto", 11));
        statsLabel.setTextFill(Color.GRAY);
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public Chef showAndReturn() {
        showAndWait();
        return selectedChef;
    }
}