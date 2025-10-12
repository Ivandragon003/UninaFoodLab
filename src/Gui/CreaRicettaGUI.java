package Gui;

import controller.RicettaController;
import controller.IngredienteController;
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
    private ListView<String> ingredientiListView;
    private javafx.collections.ObservableList<String> ingredientiData;
    private Map<Ingrediente, Double> ingredientiMap;
    private Map<String, Ingrediente> ingredienteByDisplay;
    private Label contatoreIngredientiLabel;

    public CreaRicettaGUI(RicettaController ricettaController, IngredienteController ingredienteController) {
        if (ricettaController == null) {
            throw new IllegalArgumentException("RicettaController non può essere null");
        }
        if (ingredienteController == null) {
            throw new IllegalArgumentException("IngredienteController non può essere null");
        }

        this.ricettaController = ricettaController;
        this.ingredienteController = ingredienteController;
        this.ingredientiMap = new HashMap<>();
        this.ingredientiData = javafx.collections.FXCollections.observableArrayList();
        this.ingredienteByDisplay = new HashMap<>();
    }

    public void setOwner(Window owner) {
        this.owner = owner;
    }

    public Ricetta showAndReturn() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        if (owner != null) {
            stage.initOwner(owner);
        }

        stage.setTitle("Crea Nuova Ricetta");
        stage.setResizable(false);

        Scene scene = new Scene(createMainLayout(), 650, 650);
        stage.setScene(scene);
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
        nomeField.setPrefWidth(400);
        
        tempoField = StyleHelper.createTextField("Tempo in minuti...");
        tempoField.setPrefWidth(200);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);

        grid.add(StyleHelper.createLabel("Nome:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(StyleHelper.createLabel("Tempo preparazione (min):"), 0, 1);
        grid.add(tempoField, 1, 1);

        return grid;
    }

    private VBox createIngredientiSection() {
        VBox section = StyleHelper.createSection();

        Label sectionTitle = createSectionTitle("🥕 Ingredienti");
        
       
        HBox actionButtonsBox = createActionButtonsBox();
        
        // Contatore ingredienti
        contatoreIngredientiLabel = new Label("📊 Ingredienti aggiunti: 0");
        contatoreIngredientiLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
        contatoreIngredientiLabel.setTextFill(javafx.scene.paint.Color.web(StyleHelper.INFO_BLUE));
        contatoreIngredientiLabel.setStyle("-fx-background-color: #ffebee; -fx-padding: 8; -fx-background-radius: 6; -fx-text-fill: #c62828;");
        
        ListView<String> listView = createIngredientiListView();
        Button removeButton = createRimuoviButton();

        section.getChildren().addAll(
            sectionTitle, 
            actionButtonsBox,
            contatoreIngredientiLabel,
            listView, 
            removeButton
        );
        return section;
    }

   
    private HBox createActionButtonsBox() {
    	Button selezionaIngredienteBtn = StyleHelper.createInfoButton("🔍 Seleziona Ingredienti");
        selezionaIngredienteBtn.setPrefWidth(220);
        selezionaIngredienteBtn.setPrefHeight(45);
        selezionaIngredienteBtn.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        selezionaIngredienteBtn.setOnAction(e -> apriSelezionaIngrediente());
        
        Label infoLabel = new Label("💡 Seleziona ingredienti dalla lista. Puoi crearne di nuovi dalla finestra di selezione.");
        infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + StyleHelper.INFO_BLUE + "; -fx-font-style: italic;");
        infoLabel.setWrapText(true);
        
        VBox container = new VBox(8, selezionaIngredienteBtn, infoLabel);
        container.setAlignment(Pos.TOP_LEFT);
        
        return new HBox(container);
    }

    private ListView<String> createIngredientiListView() {
        ingredientiListView = new ListView<>();
        ingredientiListView.setItems(ingredientiData);
        ingredientiListView.setPrefHeight(200);
        ingredientiListView.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: " + StyleHelper.BORDER_LIGHT + "; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8;"
        );
        
        // Placeholder quando vuoto
        Label placeholder = new Label("Nessun ingrediente aggiunto\n\nUsa il pulsante '🔍 Seleziona Ingredienti' per iniziare");
        placeholder.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
        placeholder.setAlignment(Pos.CENTER);
        ingredientiListView.setPlaceholder(placeholder);
        
        return ingredientiListView;
    }

    private Button createRimuoviButton() {
        Button btn = StyleHelper.createDangerButton("🗑️ Rimuovi Selezionato");
        btn.setOnAction(e -> rimuoviIngrediente());
        return btn;
    }

    private HBox createButtonBar() {
    	Button annullaBtn = StyleHelper.createStyledButton("❌ Annulla", "#FFFFFF", StyleHelper.ERROR_RED);
        annullaBtn.setPrefWidth(130);
        annullaBtn.setOnAction(e -> {
            if (stage != null) {
                stage.close();
            }
        });

        Button creaBtn = StyleHelper.createPrimaryButton("✅ Crea Ricetta");
        creaBtn.setPrefWidth(150);
        creaBtn.setOnAction(e -> creaRicetta());

        HBox box = new HBox(15, annullaBtn, creaBtn);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 0, 0, 0));
        return box;
    }

  
    private void apriSelezionaIngrediente() {
        try {
            
            VisualizzaIngredientiGUI visualizzaGUI = new VisualizzaIngredientiGUI(ingredienteController);
            visualizzaGUI.setModalitaSelezione(true);
            visualizzaGUI.initOwner(stage);
            
            Ingrediente selezionato = visualizzaGUI.showAndReturn();
            
            if (selezionato != null) {
                
             
                if (ingredientiMap.containsKey(selezionato)) {
                    StyleHelper.showValidationDialog("Attenzione", 
                        "L'ingrediente '" + selezionato.getNome() + "' è già stato aggiunto alla ricetta");
                    return;
                }
                
                // ✅ MIGLIORATO: Mostra dialog user-friendly per la quantità
                aggiungiIngredienteConQuantita(selezionato);
                
            } else {
      
            }
            
        } catch (Exception e) {
            System.err.println("ERROR: Errore nella selezione ingrediente: " + e.getMessage());
            e.printStackTrace();
            StyleHelper.showErrorDialog("Errore", 
                "Errore durante la selezione dell'ingrediente: " + e.getMessage());
        }
    }

    /**
     * ✅ MIGLIORATO: Dialog user-friendly per inserire la quantità
     */
    private void aggiungiIngredienteConQuantita(Ingrediente ingrediente) {
        // Crea dialog personalizzato invece di TextInputDialog standard
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Quantità Ingrediente");
        dialog.initOwner(stage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        
        // Imposta l'icona dei pulsanti
        ButtonType aggiungiButtonType = new ButtonType("✅ Aggiungi", ButtonBar.ButtonData.OK_DONE);
        ButtonType annullaButtonType = new ButtonType("❌ Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(aggiungiButtonType, annullaButtonType);
        
        // Crea il layout del dialog
        VBox dialogContent = new VBox(15);
        dialogContent.setPadding(new Insets(20));
        dialogContent.setAlignment(Pos.CENTER_LEFT);
        
        Label ingredienteLabel = new Label("🥕 " + ingrediente.getNome());
        ingredienteLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
        ingredienteLabel.setTextFill(javafx.scene.paint.Color.web(StyleHelper.PRIMARY_ORANGE));
        
        Label tipoLabel = new Label("📂 Tipo: " + ingrediente.getTipo());
        tipoLabel.setFont(Font.font("Roboto", FontWeight.NORMAL, 13));
        tipoLabel.setTextFill(javafx.scene.paint.Color.GRAY);
        
        Label istruzioniLabel = new Label("Inserisci la quantità in grammi per questa ricetta:");
        istruzioniLabel.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 13));
        istruzioniLabel.setWrapText(true);
        
        HBox quantitaBox = new HBox(10);
        quantitaBox.setAlignment(Pos.CENTER_LEFT);
        
        TextField quantitaField = StyleHelper.createTextField("Es. 250");
        quantitaField.setPrefWidth(150);
        quantitaField.setPromptText("Quantità...");
        
        Label unitLabel = new Label("grammi (g)");
        unitLabel.setFont(Font.font("Roboto", FontWeight.NORMAL, 13));
        
        quantitaBox.getChildren().addAll(quantitaField, unitLabel);
        
        // Suggerimenti comuni
        Label suggerimentiLabel = new Label("💡 Suggerimenti: 100g, 250g, 500g, 1000g");
        suggerimentiLabel.setFont(Font.font("Roboto", FontWeight.NORMAL, 11));
        suggerimentiLabel.setTextFill(javafx.scene.paint.Color.web(StyleHelper.INFO_BLUE));
        suggerimentiLabel.setStyle("-fx-background-color: #e3f2fd; -fx-padding: 8; -fx-background-radius: 6;");
        
        dialogContent.getChildren().addAll(
            ingredienteLabel,
            tipoLabel,
            new Separator(),
            istruzioniLabel,
            quantitaBox,
            suggerimentiLabel
        );
        
        dialog.getDialogPane().setContent(dialogContent);
        
        // Stile del dialog
        dialog.getDialogPane().setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;"
        );
        
        // Focus automatico sul campo quantità
        javafx.application.Platform.runLater(() -> quantitaField.requestFocus());
        
        // Converti il risultato
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == aggiungiButtonType) {
                try {
                    String text = quantitaField.getText().trim();
                    if (text.isEmpty()) {
                        return null;
                    }
                    double quantita = Double.parseDouble(text);
                    if (quantita <= 0) {
                        return null;
                    }
                    return quantita;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });
        
        // Mostra il dialog e gestisci il risultato
        dialog.showAndWait().ifPresent(quantita -> {
            aggiungiIngredienteAllaLista(ingrediente, quantita);
            StyleHelper.showSuccessDialog("Ingrediente Aggiunto", 
                String.format("✅ '%s' aggiunto con %.0fg", ingrediente.getNome(), quantita));
        });
    }

    private void aggiungiIngredienteAllaLista(Ingrediente ingrediente, double quantita) {
        String displayText = formatDisplayIngrediente(ingrediente, quantita);
        ingredientiMap.put(ingrediente, quantita);
        ingredientiData.add(displayText);
        ingredienteByDisplay.put(displayText, ingrediente);
        
        aggiornaContatore();

   
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
        aggiornaContatore();
     
    }

    private void aggiornaContatore() {
        int count = ingredientiMap.size();
        contatoreIngredientiLabel.setText("📊 Ingredienti aggiunti: " + count);
        
        if (count == 0) {
            contatoreIngredientiLabel.setStyle(
                "-fx-background-color: #ffebee;" +
                "-fx-padding: 8;" +
                "-fx-background-radius: 6;" +
                "-fx-text-fill: #c62828;" +
                "-fx-font-weight: bold;"
            );
        } else {
            contatoreIngredientiLabel.setStyle(
                "-fx-background-color: #e8f5e9;" +
                "-fx-padding: 8;" +
                "-fx-background-radius: 6;" +
                "-fx-text-fill: #2e7d32;" +
                "-fx-font-weight: bold;"
            );
        }
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
                StyleHelper.showValidationDialog("Attenzione", 
                    "Aggiungi almeno un ingrediente\n\nUsa il pulsante '🔍 Seleziona Ingredienti'");
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
                "Ricetta creata con successo!\n\n📝 Nome: %s\n⏱️ Tempo: %d minuti\n🥕 Ingredienti: %d",
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
        return String.format("🥕 %s - %.0fg (%s)", ingrediente.getNome(), quantita, ingrediente.getTipo());
    }
}
