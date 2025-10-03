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
import model.Ingrediente;
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
    private ObservableList<Ingrediente> ingredientiSelezionati = FXCollections.observableArrayList();
    
    public CreaRicettaGUI() {
        setTitle("Crea Ricetta");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);
        
        createLayout();
    }
    
    private void createLayout() {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(25));
        mainContainer.setStyle("-fx-background-color: #f8f9fa;");
        
        Label title = StyleHelper.createTitleLabel("📖 Crea Nuova Ricetta");
        title.setAlignment(Pos.CENTER);
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPrefHeight(500);
        
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
        
        Scene scene = new Scene(mainContainer, 600, 650);
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
        
        descrizioneArea = new TextArea();
        descrizioneArea.setPromptText("Descrizione dettagliata della ricetta e procedimento...");
        descrizioneArea.setPrefRowCount(5);
        descrizioneArea.setWrapText(true);
        descrizioneArea.setStyle("-fx-background-radius: 8; -fx-border-color: " + StyleHelper.BORDER_LIGHT + "; " +
                                "-fx-border-radius: 8; -fx-border-width: 1; -fx-font-size: 14px;");
        
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
        
        Button nuovoIngredienteBtn = new Button("✨ Nuovo Ingrediente");
        nuovoIngredienteBtn.setStyle("-fx-background-color: " + StyleHelper.INFO_BLUE + "; " +
                                   "-fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; " +
                                   "-fx-padding: 8 16; -fx-font-size: 12px; -fx-font-weight: bold;");
        nuovoIngredienteBtn.setOnAction(e -> creaIngrediente());
        
        buttonBox.getChildren().addAll(aggiungiIngredienteBtn, nuovoIngredienteBtn);
        
        Label ingredientiLabel = StyleHelper.createLabel("Ingredienti selezionati:");
        
        ingredientiContainer = new VBox(8);
        ingredientiContainer.setPrefHeight(200);
        ingredientiContainer.setStyle("-fx-background-color: white; -fx-border-color: " + StyleHelper.BORDER_LIGHT + "; " +
                                     "-fx-border-radius: 8; -fx-padding: 10;");
        
        updateIngredientiDisplay();
        
        section.getChildren().addAll(sectionTitle, buttonBox, ingredientiLabel, ingredientiContainer);
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
                Ingrediente ingrediente = ingredientiSelezionati.get(i);
                
                HBox ingredienteBox = new HBox(10);
                ingredienteBox.setAlignment(Pos.CENTER_LEFT);
                ingredienteBox.setPadding(new Insets(8));
                ingredienteBox.setStyle("-fx-background-color: #fff8dc; -fx-border-color: #f0e68c; " +
                                       "-fx-border-radius: 5; -fx-background-radius: 5;");
                
                VBox infoBox = new VBox(2);
                Label nameLabel = new Label("🥕 " + ingrediente.getNome());
                nameLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 14));
                
                Label categoriaLabel = new Label("Tipo: " + ingrediente.getTipo());
                categoriaLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
                categoriaLabel.setTextFill(Color.GRAY);
                
                infoBox.getChildren().addAll(nameLabel, categoriaLabel);
                
                Button removeBtn = new Button("✕");
                removeBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; " +
                                 "-fx-background-radius: 15; -fx-min-width: 25; -fx-min-height: 25; " +
                                 "-fx-max-width: 25; -fx-max-height: 25; -fx-cursor: hand;");
                final int index = i;
                removeBtn.setOnAction(e -> {
                    ingredientiSelezionati.remove(index);
                    updateIngredientiDisplay();
                });
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                ingredienteBox.getChildren().addAll(infoBox, spacer, removeBtn);
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
                           "-fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand;");
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
        // Simula lista ingredienti disponibili - sostituisci con la tua logica
        List<Ingrediente> ingredientiDisponibili = createSampleIngredienti();
        
        // Rimuovi ingredienti già selezionati
        ingredientiDisponibili.removeAll(ingredientiSelezionati);
        
        if (ingredientiDisponibili.isEmpty()) {
            showAlert("Nessun ingrediente", "Tutti gli ingredienti disponibili sono già stati aggiunti");
            return;
        }
        
        SelezionaIngredienteDialog dialog = new SelezionaIngredienteDialog(ingredientiDisponibili);
        Ingrediente selezionato = dialog.showAndReturn();
        
        if (selezionato != null) {
            ingredientiSelezionati.add(selezionato);
            updateIngredientiDisplay();
        }
    }
    
    private void creaIngrediente() {
        CreaIngredienteDialog dialog = new CreaIngredienteDialog();
        Ingrediente nuovo = dialog.showAndReturn();
        
        if (nuovo != null) {
            ingredientiSelezionati.add(nuovo);
            updateIngredientiDisplay();
            showInfo("Successo", "Ingrediente '" + nuovo.getNome() + "' creato e aggiunto!");
        }
    }
    
    private List<Ingrediente> createSampleIngredienti() {
        // Ingredienti di esempio - sostituisci con il tuo database
        List<Ingrediente> ingredienti = new ArrayList<>();
        ingredienti.add(new Ingrediente("Spaghetti", "Pasta"));
        ingredienti.add(new Ingrediente("Uova", "Proteine"));
        ingredienti.add(new Ingrediente("Pancetta", "Carne"));
        ingredienti.add(new Ingrediente("Parmigiano", "Formaggi"));
        ingredienti.add(new Ingrediente("Pepe nero", "Spezie"));
        ingredienti.add(new Ingrediente("Pomodori", "Verdure"));
        ingredienti.add(new Ingrediente("Basilico", "Erbe"));
        ingredienti.add(new Ingrediente("Aglio", "Verdure"));
        ingredienti.add(new Ingrediente("Olio d'oliva", "Condimenti"));
        ingredienti.add(new Ingrediente("Sale", "Spezie"));
        return ingredienti;
    }
    
    private void salvaRicetta() {
        try {
            if (!validateForm()) return;
            
            String nome = nomeField.getText().trim();   ;
            int tempo = Integer.parseInt(tempoField.getText().trim());
        
            
            ricettaCreata = new Ricetta(nome, tempo);
            
            showInfo("Successo", "Ricetta '" + nome + "' salvata con successo!\n" +
                                 "Ingredienti: " + ingredientiSelezionati.size() + "\n" +                            
                                 "Tempo: " + tempo + " minuti");
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
        Alert alert = new Alert(Alert.AlertType.WARNING);
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
    
    public Ricetta showAndReturn() {
        showAndWait();
        return ricettaCreata;
    }
}

// Classe helper per selezione ingrediente singolo
class SelezionaIngredienteDialog extends Stage {
    private Ingrediente selectedIngrediente = null;
    
    public SelezionaIngredienteDialog(List<Ingrediente> ingredienti) {
        setTitle("Seleziona Ingrediente");
        initModality(Modality.APPLICATION_MODAL);
        
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        Label title = StyleHelper.createTitleLabel("🥕 Seleziona Ingrediente");
        
        ListView<Ingrediente> lista = new ListView<>();
        lista.getItems().addAll(ingredienti);
        lista.setPrefHeight(200);
        
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button confermaBtn = StyleHelper.createPrimaryButton("Conferma");
        Button annullaBtn = new Button("Annulla");
        annullaBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 20;");
        
        confermaBtn.setOnAction(e -> {
            selectedIngrediente = lista.getSelectionModel().getSelectedItem();
            close();
        });
        annullaBtn.setOnAction(e -> close());
        
        buttonBox.getChildren().addAll(annullaBtn, confermaBtn);
        container.getChildren().addAll(title, lista, buttonBox);
        
        setScene(new Scene(container, 350, 300));
    }
    
    public Ingrediente showAndReturn() {
        showAndWait();
        return selectedIngrediente;
    }
}

// Classe helper per creare ingrediente
class CreaIngredienteDialog extends Stage {
    private Ingrediente nuovoIngrediente = null;
    
    public CreaIngredienteDialog() {
        setTitle("Crea Ingrediente");
        initModality(Modality.APPLICATION_MODAL);
        
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        
        Label title = StyleHelper.createTitleLabel("✨ Crea Ingrediente");
        
        TextField nomeField = StyleHelper.createTextField("Nome ingrediente");
        TextField categoriaField = StyleHelper.createTextField("Categoria");
        
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button salvaBtn = StyleHelper.createPrimaryButton("Salva");
        Button annullaBtn = new Button("Annulla");
        annullaBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 20;");
        
        salvaBtn.setOnAction(e -> {
            String nome = nomeField.getText().trim();
            String categoria = categoriaField.getText().trim();
            
            if (!nome.isEmpty() && !categoria.isEmpty()) {
                nuovoIngrediente = new Ingrediente(nome, categoria);
                close();
            }
        });
        annullaBtn.setOnAction(e -> close());
        
        buttonBox.getChildren().addAll(annullaBtn, salvaBtn);
        
        VBox form = new VBox(10);
        form.getChildren().addAll(
            StyleHelper.createLabel("Nome:"), nomeField,
            StyleHelper.createLabel("Categoria:"), categoriaField
        );
        
        container.getChildren().addAll(title, form, buttonBox);
        
        setScene(new Scene(container, 350, 250));
    }
    
    public Ingrediente showAndReturn() {
        showAndWait();
        return nuovoIngrediente;
    }
}