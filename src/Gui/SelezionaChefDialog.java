package Gui;

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
    
    public SelezionaChefDialog(List<Chef> chefDisponibili) {
        setTitle("Seleziona Chef");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);
        
        createLayout(chefDisponibili);
    }
    
    private void createLayout(List<Chef> chefDisponibili) {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(25));
        mainContainer.setStyle("-fx-background-color: #f8f9fa;");
        
        Label title = StyleHelper.createTitleLabel("👨‍🍳 Seleziona un Chef");
        title.setAlignment(Pos.CENTER);
        
        VBox formCard = StyleHelper.createSection();
        formCard.setSpacing(15);
        formCard.setPrefWidth(400);
        
        Label infoLabel = new Label("💡 Seleziona un chef dalla lista:");
        infoLabel.setFont(javafx.scene.text.Font.font("Roboto", 14));
        infoLabel.setTextFill(Color.web(StyleHelper.INFO_BLUE));
        
        listaChef = new ListView<>();
        listaChef.setPrefHeight(250);
        listaChef.setStyle("-fx-background-radius: 8; -fx-border-color: " + StyleHelper.BORDER_LIGHT + "; " +
                         "-fx-border-radius: 8; -fx-border-width: 1;");
        
        // Popola la lista
        for (Chef chef : chefDisponibili) {
            listaChef.getItems().add(chef);
        }
        
        // Custom cell factory per mostrare dettagli chef
        listaChef.setCellFactory(listView -> new ListCell<Chef>() {
            @Override
            protected void updateItem(Chef chef, boolean empty) {
                super.updateItem(chef, empty);
                if (empty || chef == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    VBox cellBox = new VBox(3);
                    cellBox.setPadding(new Insets(8));
                    
                    Label nameLabel = new Label("👨‍🍳 " + chef.getUsername());
                    nameLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 14));
                    nameLabel.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));
                    
                    cellBox.getChildren().addAll(nameLabel);
                    setGraphic(cellBox);
                    setText(null);
                    
                    // Stile selezione
                    if (isSelected()) {
                        setStyle("-fx-background-color: #e6f3ff; -fx-border-color: " + StyleHelper.PRIMARY_ORANGE + "; " +
                                "-fx-border-width: 2; -fx-border-radius: 5;");
                    } else {
                        setStyle("-fx-background-color: white;");
                    }
                }
            }
        });
        
        // Label statistiche
        Label statsLabel = new Label("Chef disponibili: " + chefDisponibili.size());
        statsLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
        statsLabel.setTextFill(Color.GRAY);
        
        // Pulsanti
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));
        
        Button annullaBtn = new Button("❌ Annulla");
        annullaBtn.setPrefWidth(120);
        annullaBtn.setStyle("-fx-background-color: " + StyleHelper.NEUTRAL_GRAY + "; " +
                           "-fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand;");
        annullaBtn.setOnAction(e -> {
            selectedChef = null;
            close();
        });
        
        Button confermaBtn = StyleHelper.createPrimaryButton("✅ Conferma");
        confermaBtn.setPrefWidth(120);
        confermaBtn.setOnAction(e -> {
            Chef selected = listaChef.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selectedChef = selected;
                close();
            } else {
                showAlert("Nessuna selezione", "Seleziona un chef dalla lista prima di confermare.");
            }
        });
        
        buttonBox.getChildren().addAll(annullaBtn, confermaBtn);
        
        // Aggiorna pulsante conferma in base alla selezione
        listaChef.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                confermaBtn.setStyle(confermaBtn.getStyle() + "-fx-opacity: 1.0;");
            } else {
                confermaBtn.setStyle(confermaBtn.getStyle() + "-fx-opacity: 0.7;");
            }
        });
        
        // Inizialmente conferma disabilitato
        confermaBtn.setStyle(confermaBtn.getStyle() + "-fx-opacity: 0.7;");
        
        formCard.getChildren().addAll(infoLabel, listaChef, statsLabel, buttonBox);
        mainContainer.getChildren().addAll(title, formCard);
        
        Scene scene = new Scene(mainContainer, 450, 450);
        setScene(scene);
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