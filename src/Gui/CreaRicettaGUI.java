package Gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Ricetta;
import util.StyleHelper;
import java.util.ArrayList;
import java.util.List;

public class CreaRicettaGUI extends Stage {
    
    private Ricetta ricettaCreata = null;
    
    private TextField nomeField;
    private TextArea descrizioneArea;
    private TextField tempoField;
    private ComboBox<String> difficoltaBox;
    
    private VBox ingredientiContainer;
    private List<IngredienteConQuantita> ingredientiSelezionati = new ArrayList<>();
    
    // Classe helper per ingrediente con quantità
    public static class IngredienteConQuantita {
        private String nome;
        private String categoria;
        private double quantita;
        
        public IngredienteConQuantita(String nome, String categoria, double quantita) {
            this.nome = nome;
            this.categoria = categoria;
            this.quantita = quantita;
        }
        
        public String getNome() { return nome; }
        public String getCategoria() { return categoria; }
        public double getQuantita() { return quantita; }
        public void setQuantita(double quantita) { this.quantita = quantita; }
    }
    
    public CreaRicettaGUI() {
        setTitle("Crea Ricetta");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);
        
        createLayout();
    }
    
    private void createLayout() {
        // ROOT con SFONDO ARANCIONE come LOGIN
        StackPane rootPane = new StackPane();
        rootPane.setPrefSize(650, 750);
        
        // Sfondo arancione
        Region background = new Region();
        StyleHelper.applyBackgroundGradient(background);
        
        VBox mainContainer = new VBox(25);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(30));
        
        Label title = new Label("📖 Crea Nuova Ricetta");
        title.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPrefHeight(550);
        
        VBox formCard = StyleHelper.createSection();
        formCard.setSpacing(20);
        
        // Sezione informazioni
        VBox infoSection = createInfoSection();
        
        // Sezione ingredienti
        VBox ingredientiSection = createIngredientiSection();
        
        // Pulsanti
        HBox buttonSection = createButtonSection();
        
        formCard.getChildren().addAll(
            infoSection,
            new Separator(),
            ingredientiSection,
            new Separator(),
            buttonSection
        );
        
        scrollPane.setContent(formCard);
        mainContainer.getChildren().addAll(title, scrollPane);
        
        rootPane.getChildren().addAll(background, mainContainer);
        
        Scene scene = new Scene(rootPane, 650, 750);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);
    }
    
    private VBox createInfoSection() {
        VBox section = new VBox(15);
        
        Label sectionTitle = new Label("📝 Informazioni Ricetta");
        sectionTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        
        nomeField = StyleHelper.createTextField("Es. Spaghetti alla Carbonara");
        tempoField = StyleHelper.createTextField("Es. 30");
        
        difficoltaBox = StyleHelper.createComboBox();
        difficoltaBox.getItems().addAll("Facile", "Medio", "Difficile");
        difficoltaBox.setPromptText("Seleziona difficoltà");
        
        descrizioneArea = StyleHelper.createTextArea("Descrizione dettagliata della ricetta e procedimento...");
        descrizioneArea.setPrefRowCount(5);
        
        grid.add(StyleHelper.createLabel("Nome Ricetta:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(StyleHelper.createLabel("Tempo (minuti):"), 2, 0);
        grid.add(tempoField, 3, 0);
        
        grid.add(StyleHelper.createLabel("Difficoltà:"), 0, 1);
        grid.add(difficoltaBox, 1, 1);
        
        grid.add(StyleHelper.createLabel("Descrizione:"), 0, 2);
        grid.add(descrizioneArea, 1, 2, 3, 1);
        
        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }
    
    private VBox createIngredientiSection() {
        VBox section = new VBox(15);
        
        Label sectionTitle = new Label("🥕 Ingredienti");
        sectionTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        
        Button aggiungiIngredienteBtn = StyleHelper.createSuccessButton("+ Aggiungi Ingrediente");
        aggiungiIngredienteBtn.setOnAction(e -> aggiungiIngrediente());
        
        buttonBox.getChildren().add(aggiungiIngredienteBtn);
        
        Label ingredientiLabel = StyleHelper.createLabel("Ingredienti selezionati:");
        
        ingredientiContainer = new VBox(8);
        ingredientiContainer.setPrefHeight(250);
        ingredientiContainer.setStyle("-fx-background-color: white; -fx-border-color: " + StyleHelper.BORDER_LIGHT + "; " +
                                     "-fx-border-radius: 8; -fx-padding: 10;");
        
        ScrollPane ingredientiScroll = new ScrollPane(ingredientiContainer);
        ingredientiScroll.setFitToWidth(true);
        ingredientiScroll.setStyle("-fx-background-color: transparent;");
        ingredientiScroll.setPrefHeight(250);
        
        updateIngredientiDisplay();
        
        section.getChildren().addAll(sectionTitle, buttonBox, ingredientiLabel, ingredientiScroll);
        return section;
    }
    
    private void updateIngredientiDisplay() {
        ingredientiContainer.getChildren().clear();
        
        if (ingredientiSelezionati.isEmpty()) {
            Label emptyLabel = new Label("Nessun ingrediente selezionato");
            emptyLabel.setTextFill(Color.GRAY);
            emptyLabel.setFont(javafx.scene.text.Font.font("Roboto", 14));
            ingredientiContainer.getChildren().add(emptyLabel);
        } else {
            for (int i = 0; i < ingredientiSelezionati.size(); i++) {
                IngredienteConQuantita item = ingredientiSelezionati.get(i);
                
                HBox ingredienteBox = new HBox(10);
                ingredienteBox.setAlignment(Pos.CENTER_LEFT);
                ingredienteBox.setPadding(new Insets(8));
                ingredienteBox.setStyle("-fx-background-color: #fff8dc; -fx-border-color: #f0e68c; " +
                                       "-fx-border-radius: 8; -fx-background-radius: 8;");
                
                VBox infoBox = new VBox(3);
                
                Label nameLabel = new Label("🥕 " + item.getNome());
                nameLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 14));
                nameLabel.setTextFill(Color.BLACK);
                
                Label categoriaLabel = new Label("Categoria: " + item.getCategoria());
                categoriaLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
                categoriaLabel.setTextFill(Color.GRAY);
                
                infoBox.getChildren().addAll(nameLabel, categoriaLabel);
                
                // Campo quantità
                VBox quantitaBox = new VBox(3);
                quantitaBox.setAlignment(Pos.CENTER);
                
                Label quantitaLabel = new Label("Quantità:");
                quantitaLabel.setFont(javafx.scene.text.Font.font("Roboto", 11));
                quantitaLabel.setTextFill(Color.GRAY);
                
                TextField quantitaField = new TextField(String.valueOf(item.getQuantita()));
                quantitaField.setPrefWidth(80);
                quantitaField.setStyle("-fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");
                
                final int index = i;
                quantitaField.textProperty().addListener((obs, oldVal, newVal) -> {
                    try {
                        double nuovaQuantita = Double.parseDouble(newVal);
                        item.setQuantita(nuovaQuantita);
                    } catch (NumberFormatException e) {
                        // Ignora valori non validi
                    }
                });
                
                quantitaBox.getChildren().addAll(quantitaLabel, quantitaField);
                
                Button removeBtn = new Button("✕");
                removeBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; " +
                                 "-fx-background-radius: 15; -fx-min-width: 30; -fx-min-height: 30; " +
                                 "-fx-max-width: 30; -fx-max-height: 30; -fx-cursor: hand; -fx-font-weight: bold;");
                removeBtn.setOnAction(e -> {
                    ingredientiSelezionati.remove(index);
                    updateIngredientiDisplay();
                });
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                ingredienteBox.getChildren().addAll(infoBox, spacer, quantitaBox, removeBtn);
                ingredientiContainer.getChildren().add(ingredienteBox);
            }
        }
    }
    
    private HBox createButtonSection() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));
        
        Button annullaBtn = new Button("❌ Annulla");
        annullaBtn.setPrefWidth(130);
        annullaBtn.setStyle("-fx-background-color: " + StyleHelper.NEUTRAL_GRAY + "; " +
                           "-fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-font-weight: bold;");
        annullaBtn.setOnAction(e -> {
            ricettaCreata = null;
            close();
        });
        
        Button salvaBtn = StyleHelper.createPrimaryButton("💾 Salva Ricetta");
        salvaBtn.setPrefWidth(130);
        salvaBtn.setOnAction(e -> salvaRicetta());
        
        buttonBox.getChildren().addAll(annullaBtn, salvaBtn);
        return buttonBox;
    }
    
    private void aggiungiIngrediente() {
        // Dialog semplice per aggiungere ingrediente
        AggiungiIngredienteDialog dialog = new AggiungiIngredienteDialog();
        IngredienteConQuantita nuovo = dialog.showAndReturn();
        
        if (nuovo != null) {
            ingredientiSelezionati.add(nuovo);
            updateIngredientiDisplay();
        }
    }
    
    private void salvaRicetta() {
        try {
            if (!validateForm()) return;
            
            String nome = nomeField.getText().trim();
            String descrizione = descrizioneArea.getText().trim();
           int tempo = Integer.parseInt(tempoField.getText().trim());
          
            
            ricettaCreata = new Ricetta(nome, tempo);
            
            StringBuilder ingredientiInfo = new StringBuilder("Ingredienti:\n");
            for (IngredienteConQuantita item : ingredientiSelezionati) {
                ingredientiInfo.append("• ").append(item.getNome())
                              .append(" (").append(item.getQuantita()).append(")\n");
            }
            
            showAlert("Successo", "Ricetta '" + nome + "' salvata con successo!\n\n" +                     
                                 "Tempo: " + tempo + " minuti\n\n" +
                                 ingredientiInfo.toString());
            close();
            
        } catch (Exception e) {
            showAlert("Errore", "Errore nel salvataggio: " + e.getMessage());
        }
    }
    
    private boolean validateForm() {
        if (nomeField.getText().trim().isEmpty()) {
            showAlert("Validazione", "Il nome della ricetta è obbligatorio");
            return false;
        }
        
        if (descrizioneArea.getText().trim().isEmpty()) {
            showAlert("Validazione", "La descrizione è obbligatoria");
            return false;
        }
        
        try {
            int tempo = Integer.parseInt(tempoField.getText().trim());
            if (tempo <= 0) {
                showAlert("Validazione", "Il tempo deve essere maggiore di 0");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validazione", "Inserire un tempo valido in minuti");
            return false;
        }
        
        if (difficoltaBox.getValue() == null) {
            showAlert("Validazione", "Selezionare una difficoltà");
            return false;
        }
        
        if (ingredientiSelezionati.isEmpty()) {
            showAlert("Validazione", "Aggiungere almeno un ingrediente alla ricetta");
            return false;
        }
        
        return true;
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Successo") ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public Ricetta showAndReturn() {
        showAndWait();
        return ricettaCreata;
    }
}

// Dialog semplice per aggiungere ingrediente
class AggiungiIngredienteDialog extends Stage {
    private CreaRicettaGUI.IngredienteConQuantita ingredienteCreato = null;
    
    public AggiungiIngredienteDialog() {
        setTitle("Aggiungi Ingrediente");
        initModality(Modality.APPLICATION_MODAL);
        
        createLayout();
    }
    
    private void createLayout() {
        StackPane rootPane = new StackPane();
        rootPane.setPrefSize(400, 350);
        
        Region background = new Region();
        StyleHelper.applyBackgroundGradient(background);
        
        VBox container = new VBox(20);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(30));
        
        Label title = new Label("🥕 Aggiungi Ingrediente");
        title.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);
        
        VBox formCard = StyleHelper.createSection();
        formCard.setSpacing(15);
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        
        TextField nomeField = StyleHelper.createTextField("Nome ingrediente");
        TextField categoriaField = StyleHelper.createTextField("Categoria (es. Verdure, Carne, Spezie)");
        TextField quantitaField = StyleHelper.createTextField("Quantità (es. 200, 1, 0.5)");
        quantitaField.setText("1");
        
        grid.add(StyleHelper.createLabel("Nome:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(StyleHelper.createLabel("Categoria:"), 0, 1);
        grid.add(categoriaField, 1, 1);
        grid.add(StyleHelper.createLabel("Quantità:"), 0, 2);
        grid.add(quantitaField, 1, 2);
        
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));
        
        Button salvaBtn = StyleHelper.createPrimaryButton("💾 Aggiungi");
        Button annullaBtn = new Button("❌ Annulla");
        annullaBtn.setStyle("-fx-background-color: " + StyleHelper.NEUTRAL_GRAY + "; " +
                           "-fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand;");
        
        salvaBtn.setOnAction(e -> {
            String nome = nomeField.getText().trim();
            String categoria = categoriaField.getText().trim();
            String quantitaText = quantitaField.getText().trim();
            
            if (nome.isEmpty()) {
                showAlert("Validazione", "Il nome è obbligatorio");
                return;
            }
            if (categoria.isEmpty()) {
                showAlert("Validazione", "La categoria è obbligatoria");
                return;
            }
            
            try {
                double quantita = Double.parseDouble(quantitaText);
                if (quantita <= 0) {
                    showAlert("Validazione", "La quantità deve essere maggiore di 0");
                    return;
                }
                
                ingredienteCreato = new CreaRicettaGUI.IngredienteConQuantita(nome, categoria, quantita);
                close();
            } catch (NumberFormatException ex) {
                showAlert("Validazione", "Inserire una quantità valida");
            }
        });
        
        annullaBtn.setOnAction(e -> close());
        
        buttonBox.getChildren().addAll(annullaBtn, salvaBtn);
        
        formCard.getChildren().addAll(grid, buttonBox);
        container.getChildren().addAll(title, formCard);
        rootPane.getChildren().addAll(background, container);
        
        setScene(new Scene(rootPane, 400, 350));
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public CreaRicettaGUI.IngredienteConQuantita showAndReturn() {
        showAndWait();
        return ingredienteCreato;
    }
}