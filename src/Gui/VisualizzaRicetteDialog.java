
package Gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.*;
import service.GestioneRicette;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class VisualizzaRicetteDialog extends Stage {
    
    private final GestioneRicette gestioneRicette;
    private final ObservableList<Ricetta> ricetteDisponibili;
    private final ObservableList<Ricetta> ricetteSelezionate;
    
    private ListView<Ricetta> listaDisponibili;
    private ListView<Ricetta> listaSelezionate;
    
    private List<Ricetta> risultato = new ArrayList<>();

    public VisualizzaRicetteDialog(GestioneRicette gestioneRicette) {
        this.gestioneRicette = gestioneRicette;
        this.ricetteDisponibili = FXCollections.observableArrayList();
        this.ricetteSelezionate = FXCollections.observableArrayList();
        
        setTitle("📚 Seleziona Ricette per Sessione");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(true);
        
        VBox layout = createLayout();
        setScene(new Scene(layout, 800, 600));
        
        caricaRicette();
    }
    
    private VBox createLayout() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #F8F9FA;");
        
        // Header
        Label titleLabel = new Label("📚 Seleziona Ricette per la Sessione");
        titleLabel.setFont(Font.font("Inter", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web("#2C3E50"));
        
        Label infoLabel = new Label("⚠️ Devi selezionare almeno una ricetta per salvare la sessione in presenza");
        infoLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 12));
        infoLabel.setTextFill(Color.web("#E67E22"));
        infoLabel.setStyle("-fx-background-color: #FEF5E7; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #F39C12; -fx-border-radius: 8; -fx-border-width: 1;");
        
        // Container principale
        HBox mainContainer = createMainContainer();
        
        // Pulsanti
        HBox buttonBox = createButtonBox();
        
        layout.getChildren().addAll(titleLabel, infoLabel, new Separator(), mainContainer, buttonBox);
        return layout;
    }
    
    private HBox createMainContainer() {
        HBox container = new HBox(20);
        container.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        
        // Sezione disponibili
        VBox disponibiliSection = new VBox(12);
        disponibiliSection.setPrefWidth(320);
        
        Label disponibiliLabel = new Label("🍽️ Ricette Disponibili");
        disponibiliLabel.setFont(Font.font("Inter", FontWeight.BOLD, 14));
        
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Cerca ricette...");
        searchField.setPrefHeight(35);
        searchField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8;");
        
        listaDisponibili = new ListView<>(ricetteDisponibili);
        listaDisponibili.setPrefHeight(350);
        listaDisponibili.setStyle("-fx-background-radius: 8; -fx-border-radius: 8;");
        listaDisponibili.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // Cell factory
        listaDisponibili.setCellFactory(lv -> new ListCell<Ricetta>() {
            @Override
            protected void updateItem(Ricetta ricetta, boolean empty) {
                super.updateItem(ricetta, empty);
                if (empty || ricetta == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox cellContent = new VBox(3);
                    cellContent.setPadding(new Insets(8));
                    cellContent.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 5;");
                    
                    Label nameLabel = new Label(ricetta.getNome());
                    nameLabel.setFont(Font.font("Inter", FontWeight.BOLD, 13));
                    
                    Label timeLabel = new Label("⏱️ " + ricetta.getTempoPreparazione() + " minuti");
                    timeLabel.setFont(Font.font("Inter", FontWeight.NORMAL, 11));
                    timeLabel.setTextFill(Color.web("#666666"));
                    
                    cellContent.getChildren().addAll(nameLabel, timeLabel);
                    setGraphic(cellContent);
                    setText(null);
                }
            }
        });
        
        // Filtro ricerca
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
        
        disponibiliSection.getChildren().addAll(disponibiliLabel, searchField, listaDisponibili);
        
        // Sezione pulsanti centrali
        VBox buttonSection = new VBox(15);
        buttonSection.setAlignment(javafx.geometry.Pos.CENTER);
        buttonSection.setPrefWidth(100);
        
        Button addBtn = createStyledButton("➡️\nAggiungi", "#28A745");
        addBtn.setOnAction(e -> aggiungiRicetteSelezionate());
        
        Button removeBtn = createStyledButton("⬅️\nRimuovi", "#DC3545");
        removeBtn.setOnAction(e -> rimuoviRicetteSelezionate());
        
        Button createBtn = createStyledButton("➕\nCrea Nuova", "#007BFF");
        createBtn.setOnAction(e -> creaRicettaDialog());
        
        buttonSection.getChildren().addAll(addBtn, removeBtn, new Separator(), createBtn);
        
        // Sezione selezionate
        VBox selezionateSection = new VBox(12);
        selezionateSection.setPrefWidth(320);
        
        Label selezionateLabel = new Label("✅ Ricette Selezionate");
        selezionateLabel.setFont(Font.font("Inter", FontWeight.BOLD, 14));
        selezionateLabel.setTextFill(Color.web("#28A745"));
        
        listaSelezionate = new ListView<>(ricetteSelezionate);
        listaSelezionate.setPrefHeight(350);
        listaSelezionate.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #28A745; -fx-border-width: 2;");
        listaSelezionate.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        listaSelezionate.setCellFactory(lv -> new ListCell<Ricetta>() {
            @Override
            protected void updateItem(Ricetta ricetta, boolean empty) {
                super.updateItem(ricetta, empty);
                if (empty || ricetta == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox cellContent = new VBox(3);
                    cellContent.setPadding(new Insets(8));
                    cellContent.setStyle("-fx-background-color: #D4EDDA; -fx-background-radius: 5;");
                    
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
        
        Label countLabel = new Label();
        countLabel.setFont(Font.font("Inter", FontWeight.BOLD, 12));
        countLabel.setTextFill(Color.web("#28A745"));
        
        ricetteSelezionate.addListener((javafx.collections.ListChangeListener<Ricetta>) c -> {
            countLabel.setText("📊 Selezionate: " + ricetteSelezionate.size() + " ricette");
        });
        
        selezionateSection.getChildren().addAll(selezionateLabel, listaSelezionate, countLabel);
        
        container.getChildren().addAll(disponibiliSection, buttonSection, selezionateSection);
        return container;
    }
    
    private HBox createButtonBox() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));
        
        Button annullaBtn = createStyledButton("❌ Annulla", "#6C757D");
        annullaBtn.setOnAction(e -> {
            risultato.clear();
            close();
        });
        
        Button confermaBtn = createStyledButton("✅ Conferma Selezione", "#007BFF");
        confermaBtn.setOnAction(e -> {
            if (ricetteSelezionate.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Attenzione");
                alert.setHeaderText("Nessuna ricetta selezionata");
                alert.setContentText("Devi selezionare almeno una ricetta per la sessione in presenza.");
                alert.showAndWait();
                return;
            }
            
            risultato.clear();
            risultato.addAll(ricetteSelezionate);
            close();
        });
        
        buttonBox.getChildren().addAll(annullaBtn, confermaBtn);
        return buttonBox;
    }
    
    private void aggiungiRicetteSelezionate() {
        List<Ricetta> selected = new ArrayList<>(listaDisponibili.getSelectionModel().getSelectedItems());
        for (Ricetta ricetta : selected) {
            if (!ricetteSelezionate.contains(ricetta)) {
                ricetteSelezionate.add(ricetta);
            }
        }
        listaDisponibili.getSelectionModel().clearSelection();
    }
    
    private void rimuoviRicetteSelezionate() {
        List<Ricetta> selected = new ArrayList<>(listaSelezionate.getSelectionModel().getSelectedItems());
        ricetteSelezionate.removeAll(selected);
        listaSelezionate.getSelectionModel().clearSelection();
    }
    
    private void creaRicettaDialog() {
        Dialog<Ricetta> dialog = new Dialog<>();
        dialog.setTitle("Crea Nuova Ricetta");
        dialog.setHeaderText("Inserisci i dati della nuova ricetta");

        ButtonType createButtonType = new ButtonType("Crea", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome ricetta");
        TextField tempoField = new TextField();
        tempoField.setPromptText("Tempo preparazione (minuti)");

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(new Label("Tempo (min):"), 0, 1);
        grid.add(tempoField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    String nome = nomeField.getText().trim();
                    String tempoText = tempoField.getText().trim();
                    
                    if (nome.isEmpty()) {
                        showAlert("Errore", "Inserisci il nome della ricetta.");
                        return null;
                    }
                    
                    int tempo = Integer.parseInt(tempoText);
                    if (tempo <= 0) throw new NumberFormatException();
                    
                    Ricetta nuova = new Ricetta(nome, tempo);
                    gestioneRicette.creaRicetta(nuova);
                    return nuova;
                    
                } catch (NumberFormatException e) {
                    showAlert("Errore", "Tempo non valido.");
                    return null;
                } catch (SQLException e) {
                    showAlert("Errore DB", "Errore salvataggio: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(nuovaRicetta -> {
            ricetteDisponibili.add(nuovaRicetta);
            ricetteSelezionate.add(nuovaRicetta);
            showAlert("Successo", "Ricetta creata e aggiunta alla selezione!");
        });
    }
    
    private void caricaRicette() {
        try {
            List<Ricetta> ricette = gestioneRicette.getAllRicette();
            ricetteDisponibili.clear();
            ricetteDisponibili.addAll(ricette);
        } catch (SQLException e) {
            showAlert("Errore", "Errore caricamento ricette: " + e.getMessage());
        }
    }
    
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setFont(Font.font("Inter", FontWeight.BOLD, 11));
        button.setTextFill(Color.WHITE);
        button.setPrefWidth(80);
        button.setPrefHeight(60);
        button.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand; " +
            "-fx-text-alignment: center;", color
        ));
        
        button.setOnMouseEntered(e -> button.setStyle(button.getStyle() + "-fx-opacity: 0.8;"));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle().replace("-fx-opacity: 0.8;", "")));
        
        return button;
    }
    
    public List<Ricetta> showAndReturn() {
        showAndWait();
        return new ArrayList<>(risultato);
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
