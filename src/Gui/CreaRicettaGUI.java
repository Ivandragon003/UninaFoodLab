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
import javafx.stage.Window;
import model.Ricetta;
import model.Ingrediente;
import guihelper.StyleHelper;
import exceptions.ValidationException;
import exceptions.DataAccessException;
import java.util.HashMap;
import java.util.Map;

public class CreaRicettaGUI {

    private final RicettaController ricettaController;
    private final IngredienteController ingredienteController;
    private Ricetta ricettaCreata = null;
    private Stage stage;
    private Window owner;

    private TextField nomeField;
    private TextField tempoField;
    private ComboBox<Ingrediente> ingredienteCombo;
    private TextField quantitaField;
    private ListView<String> ingredientiListView;

    private ObservableList<String> ingredientiData;
    private Map<Ingrediente, Double> ingredientiMap;
    private Map<String, Ingrediente> ingredienteByDisplay;

    public CreaRicettaGUI(RicettaController ricettaController, IngredienteController ingredienteController) {
        this.ricettaController = ricettaController;
        this.ingredienteController = ingredienteController;
        this.ingredientiMap = new HashMap<>();
        this.ingredientiData = FXCollections.observableArrayList();
        this.ingredienteByDisplay = new HashMap<>();
    }

    /**
     * Imposta la finestra owner per questo dialogo modale.
     * DEVE essere chiamato PRIMA di showAndReturn() quando si apre da un dialogo esistente.
     */
    public void setOwner(Window owner) {
        this.owner = owner;
    }

    public Ricetta showAndReturn() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        
        // Imposta l'owner se specificato
        if (owner != null) {
            stage.initOwner(owner);
        }
        
        stage.setTitle("Crea Nuova Ricetta");
        stage.setResizable(false);

        // Crea il layout PRIMA di caricare i dati
        Scene scene = new Scene(createMainLayout(), 600, 700);
        stage.setScene(scene);
        
        // IMPORTANTE: Carica gli ingredienti DOPO che la scena è stata impostata
        // Questo previene NullPointerException nei dialoghi annidati
        caricaIngredienti();
        
        stage.showAndWait();

        return ricettaCreata;
    }

    private VBox createMainLayout() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        StyleHelper.applyOrangeBackground(container);

        Label titleLabel = StyleHelper.createTitleLabel("➕ Crea Nuova Ricetta");
        titleLabel.setAlignment(Pos.CENTER);

        ScrollPane scrollPane = createScrollPane();
        HBox buttonBox = createButtonBar();

        container.getChildren().addAll(titleLabel, scrollPane, buttonBox);
        return container;
    }

    private ScrollPane createScrollPane() {
        VBox content = new VBox(15,
            createDettagliSection(),
            new Separator(),
            createIngredientiSection()
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scrollPane;
    }

    private VBox createDettagliSection() {
        VBox section = StyleHelper.createSection();
        Label sectionTitle = createSectionTitle("📝 Dettagli Ricetta");
        GridPane dettagliGrid = createDettagliGrid();
        section.getChildren().addAll(sectionTitle, dettagliGrid);
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
        Label sectionTitle = createSectionTitle("🥕 Ingredienti");
        HBox inputBox = createIngredientiInputBox();
        ListView<String> listView = createIngredientiListView();
        Button removeButton = createRimuoviButton();
        section.getChildren().addAll(sectionTitle, inputBox, listView, removeButton);
        return section;
    }

    private HBox createIngredientiInputBox() {
        ingredienteCombo = StyleHelper.createComboBox();
        quantitaField = StyleHelper.createTextField("Quantità in grammi...");
        Button aggiungiBtn = StyleHelper.createSuccessButton("➕ Aggiungi");
        aggiungiBtn.setOnAction(e -> aggiungiIngrediente());
        
        // NON caricare gli ingredienti qui - verrà fatto dopo l'impostazione della scena
        
        HBox box = new HBox(10, ingredienteCombo, quantitaField, aggiungiBtn);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private ListView<String> createIngredientiListView() {
        ingredientiListView = new ListView<>();
        ingredientiListView.setItems(ingredientiData);
        ingredientiListView.setPrefHeight(200);
        ingredientiListView.setStyle("-fx-background-color: white; -fx-border-color: " +
                StyleHelper.BORDER_LIGHT + "; -fx-border-radius: 8; -fx-background-radius: 8;");
        return ingredientiListView;
    }

    private Button createRimuoviButton() {
        Button btn = StyleHelper.createDangerButton("🗑️ Rimuovi Selezionato");
        btn.setOnAction(e -> rimuoviIngrediente());
        return btn;
    }

    private HBox createButtonBar() {
        Button annullaBtn = StyleHelper.createDangerButton("❌ Annulla");
        annullaBtn.setOnAction(e -> {
            if (stage != null) {
                stage.close();
            }
        });
        Button creaBtn = StyleHelper.createPrimaryButton("✅ Crea Ricetta");
        creaBtn.setOnAction(e -> creaRicetta());
        HBox box = new HBox(15, annullaBtn, creaBtn);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 0, 0, 0));
        return box;
    }

    /**
     * Carica gli ingredienti dal database nel ComboBox.
     * Chiamato DOPO che la scena è stata impostata per evitare problemi con dialoghi annidati.
     */
    private void caricaIngredienti() {
        try {
            ObservableList<Ingrediente> ingredienti = FXCollections.observableArrayList(
                ingredienteController.getAllIngredienti()
            );
            
            if (ingredienti.isEmpty()) {
                StyleHelper.showValidationDialog("Attenzione", 
                    "Nessun ingrediente disponibile nel database.\n" +
                    "Crea prima degli ingredienti prima di creare una ricetta.");
                if (stage != null) {
                    stage.close();
                }
                return;
            }
            
            // Popola il ComboBox con gli ingredienti caricati
            ingredienteCombo.setItems(ingredienti);
            
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore Database",
                    "Impossibile caricare gli ingredienti: " + e.getMessage());
            e.printStackTrace();
            if (stage != null) {
                stage.close();
            }
        }
    }

    private void aggiungiIngrediente() {
        Ingrediente ingrediente = ingredienteCombo.getValue();
        String quantitaStr = quantitaField.getText();

        if (ingrediente == null) {
            StyleHelper.showValidationDialog("Attenzione", "Seleziona un ingrediente");
            return;
        }
        if (quantitaStr == null || quantitaStr.trim().isEmpty()) {
            StyleHelper.showValidationDialog("Attenzione", "Inserisci la quantità");
            return;
        }

        try {
            double quantita = Double.parseDouble(quantitaStr.trim());
            if (quantita <= 0) {
                StyleHelper.showValidationDialog("Attenzione", "La quantità deve essere maggiore di zero");
                return;
            }
            if (ingredientiMap.containsKey(ingrediente)) {
                StyleHelper.showValidationDialog("Attenzione", "Ingrediente già aggiunto alla lista");
                return;
            }
            aggiungiIngredienteAllaLista(ingrediente, quantita);
            resetCampiIngrediente();
        } catch (NumberFormatException e) {
            StyleHelper.showValidationDialog("Attenzione", "Inserisci un numero valido per la quantità");
        }
    }

    private void aggiungiIngredienteAllaLista(Ingrediente ingrediente, double quantita) {
        String displayText = formatDisplayIngrediente(ingrediente, quantita);
        ingredientiMap.put(ingrediente, quantita);
        ingredientiData.add(displayText);
        ingredienteByDisplay.put(displayText, ingrediente);
    }

    private void rimuoviIngrediente() {
        int selectedIndex = ingredientiListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            StyleHelper.showValidationDialog("Attenzione", "Seleziona un ingrediente da rimuovere");
            return;
        }
        String displayText = ingredientiData.get(selectedIndex);
        Ingrediente ingrediente = ingredienteByDisplay.get(displayText);
        if (ingrediente != null) {
            ingredientiMap.remove(ingrediente);
            ingredienteByDisplay.remove(displayText);
        }
        ingredientiData.remove(selectedIndex);
    }

    private void creaRicetta() {
        String nome = nomeField.getText();
        String tempoStr = tempoField.getText();

        if (nome == null || nome.trim().isEmpty()) {
            StyleHelper.showValidationDialog("Attenzione", "Inserisci il nome della ricetta");
            return;
        }
        if (tempoStr == null || tempoStr.trim().isEmpty()) {
            StyleHelper.showValidationDialog("Attenzione", "Inserisci il tempo di preparazione");
            return;
        }

        try {
            int tempo = Integer.parseInt(tempoStr.trim());
            if (tempo <= 0) {
                StyleHelper.showValidationDialog("Attenzione", "Il tempo deve essere maggiore di zero");
                return;
            }
            if (ingredientiMap.isEmpty()) {
                StyleHelper.showValidationDialog("Attenzione", "Aggiungi almeno un ingrediente");
                return;
            }

            ricettaCreata = ricettaController.creaRicetta(nome.trim(), tempo, ingredientiMap);
            mostraConfermaCreazione(nome.trim(), tempo);
            stage.close();
        } catch (NumberFormatException e) {
            StyleHelper.showValidationDialog("Attenzione", "Inserisci un numero valido per il tempo");
        } catch (ValidationException e) {
            StyleHelper.showValidationDialog("Validazione", e.getMessage());
        } catch (DataAccessException e) {
            StyleHelper.showErrorDialog("Errore Database", "Errore durante il salvataggio: " + e.getMessage());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore imprevisto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostraConfermaCreazione(String nome, int tempo) {
        String messaggio = String.format(
            "Ricetta creata con successo!\n\nNome: %s\nTempo: %d minuti\nIngredienti: %d",
            nome, tempo, ingredientiMap.size()
        );
        StyleHelper.showSuccessDialog("Successo", messaggio);
    }

    private Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
        label.setTextFill(javafx.scene.paint.Color.web(StyleHelper.PRIMARY_ORANGE));
        return label;
    }

    private String formatDisplayIngrediente(Ingrediente ingrediente, double quantita) {
        return String.format("%s - %.0fg", ingrediente.getNome(), quantita);
    }

    private void resetCampiIngrediente() {
        quantitaField.clear();
        ingredienteCombo.setValue(null);
    }
}
