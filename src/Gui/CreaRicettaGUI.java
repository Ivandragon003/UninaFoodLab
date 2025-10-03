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
import service.GestioneRicette;
import util.StyleHelper;
import java.util.List;

public class CreaRicettaGUI extends Stage {
    
    private GestioneRicette gestioneRicette;
    private Ricetta ricettaCreata = null;
    
    private TextField nomeField;
    private TextArea descrizioneArea;
    private TextField tempoField;
    
    public CreaRicettaGUI(GestioneRicette gestioneRicette) {
        this.gestioneRicette = gestioneRicette;
        setTitle("Crea Ricetta");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(true); // RIDIMENSIONABILE
        
        createLayout();
    }
    
    // Costruttore alternativo senza service per creazione standalone
    public CreaRicettaGUI() {
        this.gestioneRicette = null;
        setTitle("Crea Ricetta");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(true); // RIDIMENSIONABILE
        
        createLayout();
    }
    
    private void createLayout() {
        // ROOT con SFONDO ARANCIONE ridimensionabile
        StackPane rootPane = new StackPane();
        rootPane.setMinSize(500, 400);
        rootPane.setPrefSize(600, 500);
        
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
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        VBox formCard = StyleHelper.createSection();
        formCard.setSpacing(20);
        
        // Sezione informazioni - RISPETTA MODEL RICETTA
        VBox infoSection = createInfoSection();
        
        // Pulsanti
        HBox buttonSection = createButtonSection();
        
        formCard.getChildren().addAll(infoSection, new Separator(), buttonSection);
        
        scrollPane.setContent(formCard);
        mainContainer.getChildren().addAll(title, scrollPane);
        
        rootPane.getChildren().addAll(background, mainContainer);
        
        Scene scene = new Scene(rootPane, 600, 500);
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
        
        // RISPETTA MODEL RICETTA - solo campi esistenti
        nomeField = StyleHelper.createTextField("Es. Spaghetti alla Carbonara");
        tempoField = StyleHelper.createTextField("Tempo in minuti (es. 30)");
        
        descrizioneArea = StyleHelper.createTextArea("Descrizione dettagliata della ricetta e procedimento...");
        descrizioneArea.setPrefRowCount(6);
        
        grid.add(StyleHelper.createLabel("Nome Ricetta:"), 0, 0);
        grid.add(nomeField, 1, 0, 2, 1);
        
        grid.add(StyleHelper.createLabel("Tempo (minuti):"), 0, 1);
        grid.add(tempoField, 1, 1);
        
        grid.add(StyleHelper.createLabel("Descrizione:"), 0, 2);
        grid.add(descrizioneArea, 1, 2, 2, 1);
        
        section.getChildren().addAll(sectionTitle, grid);
        return section;
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
    
    private void salvaRicetta() {
        try {
            if (!validateForm()) return;
            
            String nome = nomeField.getText().trim();
            int tempoPreparazione = Integer.parseInt(tempoField.getText().trim());
            
            // CREA RICETTA CON MODEL CORRETTO
            ricettaCreata = new Ricetta(nome, tempoPreparazione);
            
            // Salva nel database se service disponibile
            if (gestioneRicette != null) {
                try {
                    gestioneRicette.creaRicetta(ricettaCreata);
                    showAlert("Successo", "Ricetta '" + nome + "' salvata nel database con successo!");
                } catch (Exception e) {
                    showAlert("Errore Database", "Errore nel salvataggio: " + e.getMessage());
                    return;
                }
            } else {
                showAlert("Successo", "Ricetta '" + nome + "' creata con successo!\n" +
                                   "Tempo: " + tempoPreparazione + " minuti");
            }
            
            close();
            
        } catch (Exception e) {
            showAlert("Errore", "Errore nella creazione: " + e.getMessage());
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