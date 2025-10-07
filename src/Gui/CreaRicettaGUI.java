package Gui;

import controller.RicettaController;
import controller.IngredienteController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Ricetta;
import model.Ingrediente;
import util.StyleHelper;
import exceptions.ValidationException;

import java.util.HashMap;
import java.util.Map;

public class CreaRicettaGUI {
    private final RicettaController ricettaController;
    private final IngredienteController ingredienteController;
    private Ricetta ricettaCreata = null;
    
    // Campi UI
    private TextField nomeField, tempoField, quantitaField;
    private ComboBox<Ingrediente> ingredienteCombo;
    private ListView<String> ingredientiListView;
    private ObservableList<String> ingredientiData;
    private Map<Ingrediente, Double> ingredientiMap;
    // NUOVO: Mantieni riferimento per sincronizzazione
    private Map<String, Ingrediente> ingredienteByDisplay;

    public CreaRicettaGUI(RicettaController ricettaController, IngredienteController ingredienteController) {
        this.ricettaController = ricettaController;
        this.ingredienteController = ingredienteController;
        this.ingredientiMap = new HashMap<>();
        this.ingredientiData = FXCollections.observableArrayList();
        this.ingredienteByDisplay = new HashMap<>(); // NUOVO
    }

    public Ricetta showAndReturn() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Crea Nuova Ricetta");
        
        Scene scene = new Scene(createLayout(stage), 600, 700);
        stage.setScene(scene);
        stage.showAndWait();
        
        return ricettaCreata;
    }

    // ==================== LAYOUT ====================

    private VBox createLayout(Stage stage) {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        StyleHelper.applyOrangeBackground(container);

        Label titleLabel = StyleHelper.createTitleLabel("➕ Crea Nuova Ricetta");
        titleLabel.setAlignment(Pos.CENTER);

        ScrollPane scrollPane = new ScrollPane(new VBox(15, 
            createDettagliSection(), 
            new Separator(), 
            createIngredientiSection()
        ));
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        container.getChildren().addAll(titleLabel, scrollPane, createButtonBox(stage));
        return container;
    }

    private VBox createDettagliSection() {
        VBox section = StyleHelper.createSection();
        section.getChildren().addAll(
            createSectionTitle("📝 Dettagli Ricetta"),
            createDettagliGrid()
        );
        return section;
    }

    private GridPane createDettagliGrid() {
        nomeField = StyleHelper.createTextField("Nome ricetta...");
        tempoField = StyleHelper.createTextField("Tempo in minuti...");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.add(StyleHelper.createLabel("Nome:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(StyleHelper.createLabel("Tempo preparazione:"), 0, 1);
        grid.add(tempoField, 1, 1);
        
        return grid;
    }

    private VBox createIngredientiSection() {
        VBox section = StyleHelper.createSection();
        
        ingredienteCombo = StyleHelper.createComboBox();
        quantitaField = StyleHelper.createTextField("Quantità in grammi...");
        ingredientiListView = new ListView<>();
        ingredientiListView.setItems(ingredientiData);
        ingredientiListView.setPrefHeight(200);
        StyleHelper.applyListViewStyle(ingredientiListView);
        
        caricaIngredienti();

        section.getChildren().addAll(
            createSectionTitle("🥕 Ingredienti"),
            createIngredientiInputBox(),
            ingredientiListView,
            createRimuoviButton()
        );
        return section;
    }

    private HBox createIngredientiInputBox() {
        Button aggiungiBtn = StyleHelper.createSuccessButton("➕ Aggiungi");
        aggiungiBtn.setOnAction(e -> aggiungiIngrediente());

        HBox box = new HBox(10, ingredienteCombo, quantitaField, aggiungiBtn);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Button createRimuoviButton() {
        Button btn = StyleHelper.createDangerButton("🗑️ Rimuovi Selezionato");
        btn.setOnAction(e -> rimuoviIngrediente());
        return btn;
    }

    private HBox createButtonBox(Stage stage) {
        Button annullaBtn = StyleHelper.createDangerButton("❌ Annulla");
        annullaBtn.setOnAction(e -> stage.close());

        Button creaBtn = StyleHelper.createPrimaryButton("✅ Crea Ricetta");
        creaBtn.setOnAction(e -> creaRicetta(stage));

        HBox box = new HBox(15, annullaBtn, creaBtn);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 0, 0, 0));
        return box;
    }

    // ==================== AZIONI ====================

    private void caricaIngredienti() {
        try {
            ingredienteCombo.setItems(FXCollections.observableArrayList(
                ingredienteController.getAllIngredienti()
            ));
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Impossibile caricare ingredienti: " + e.getMessage());
        }
    }

    private void aggiungiIngrediente() {
        Ingrediente ing = ingredienteCombo.getValue();
        String quantitaStr = quantitaField.getText();

        // Validazioni
        if (!validateIngredienteInput(ing, quantitaStr)) return;

        try {
            double quantita = Double.parseDouble(quantitaStr.trim());
            
            if (quantita <= 0) {
                StyleHelper.showValidationDialog("Validazione", "La quantità deve essere maggiore di zero");
                return;
            }

            if (ingredientiMap.containsKey(ing)) {
                StyleHelper.showValidationDialog("Validazione", "Ingrediente già aggiunto");
                return;
            }

            // Aggiungi ingrediente
            String displayText = formatIngrediente(ing, quantita);
            ingredientiMap.put(ing, quantita);
            ingredientiData.add(displayText);
            ingredienteByDisplay.put(displayText, ing); // NUOVO: mantieni mappatura
            
            // Reset campi
            resetIngredientiFields();

        } catch (NumberFormatException e) {
            StyleHelper.showValidationDialog("Validazione", "Quantità non valida");
        }
    }

    // FIX CRITICO: Rimozione sincronizzata corretta
    private void rimuoviIngrediente() {
        int selectedIndex = ingredientiListView.getSelectionModel().getSelectedIndex();
        
        if (selectedIndex < 0) {
            StyleHelper.showValidationDialog("Validazione", "Seleziona un ingrediente da rimuovere");
            return;
        }

        String displayText = ingredientiData.get(selectedIndex);
        
        // Rimuovi usando la mappatura
        Ingrediente ingredienteDaRimuovere = ingredienteByDisplay.get(displayText);
        if (ingredienteDaRimuovere != null) {
            ingredientiMap.remove(ingredienteDaRimuovere);
            ingredienteByDisplay.remove(displayText);
        }
        
        // Rimuovi dalla lista visibile
        ingredientiData.remove(selectedIndex);
    }

    private void creaRicetta(Stage stage) {
        String nome = nomeField.getText();
        String tempoStr = tempoField.getText();

        // Validazioni
        if (!validateRicettaInput(nome, tempoStr)) return;

        try {
            int tempo = Integer.parseInt(tempoStr.trim());
            
            // NUOVO: Validazione tempo positivo
            if (tempo <= 0) {
                StyleHelper.showValidationDialog("Validazione", "Il tempo deve essere maggiore di zero");
                return;
            }
            
            if (ingredientiMap.isEmpty()) {
                StyleHelper.showValidationDialog("Validazione", "Aggiungi almeno un ingrediente");
                return;
            }

            // Crea ricetta
            ricettaCreata = ricettaController.creaRicetta(nome.trim(), tempo, ingredientiMap);
            
            // NUOVO: Messaggio di conferma più dettagliato
            StyleHelper.showSuccessDialog("Successo", 
                "Ricetta creata con successo!\n\n" +
                "Nome: " + nome.trim() + "\n" +
                "Tempo: " + tempo + " minuti\n" +
                "Ingredienti: " + ingredientiMap.size());
            
            stage.close();

        } catch (NumberFormatException e) {
            StyleHelper.showValidationDialog("Validazione", "Tempo non valido");
        } catch (ValidationException e) {
            StyleHelper.showValidationDialog("Validazione", e.getMessage());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore creazione ricetta: " + e.getMessage());
            e.printStackTrace(); // NUOVO: Log per debugging
        }
    }

    // ==================== VALIDAZIONI ====================

    private boolean validateIngredienteInput(Ingrediente ing, String quantitaStr) {
        if (ing == null) {
            StyleHelper.showValidationDialog("Validazione", "Seleziona un ingrediente");
            return false;
        }

        if (quantitaStr == null || quantitaStr.trim().isEmpty()) {
            StyleHelper.showValidationDialog("Validazione", "Inserisci la quantità");
            return false;
        }

        return true;
    }

    private boolean validateRicettaInput(String nome, String tempoStr) {
        if (nome == null || nome.trim().isEmpty()) {
            StyleHelper.showValidationDialog("Validazione", "Inserisci il nome della ricetta");
            return false;
        }

        if (tempoStr == null || tempoStr.trim().isEmpty()) {
            StyleHelper.showValidationDialog("Validazione", "Inserisci il tempo di preparazione");
            return false;
        }

        return true;
    }

    // ==================== HELPER ====================

    private Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
        label.setTextFill(javafx.scene.paint.Color.web(StyleHelper.PRIMARY_ORANGE));
        return label;
    }

    private String formatIngrediente(Ingrediente ing, double quantita) {
        return ing.getNome() + " - " + quantita + "g";
    }

    private void resetIngredientiFields() {
        quantitaField.clear();
        ingredienteCombo.setValue(null);
    }
}