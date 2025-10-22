package Gui;

import controller.ChefController;
import exceptions.DataAccessException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Chef;
import guihelper.StyleHelper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SelezionaChefDialog {
    private final ChefController chefController;
    private final Stage stage;

    private final ObservableList<Chef> chefData = FXCollections.observableArrayList();
    private ListView<Chef> chefListView;
    private TextField searchField;
    private ComboBox<String> filtroDisponibilita;
    private Label countLabel;
    private Chef selectedChef = null;

    public SelezionaChefDialog(ChefController chefController) {
        this.chefController = chefController;
        this.stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Seleziona Chef");
    }

    public Chef showAndReturn() {
        VBox root = createMainLayout();
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        
        caricaChef();
        
        stage.showAndWait();
        return selectedChef;
    }

    private VBox createMainLayout() {
        VBox root = new VBox(0);
        root.setPrefSize(600, 500);
        root.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-color: #FF6600; -fx-border-width: 2; -fx-border-radius: 15;");
        

        VBox header = createHeader();
        
        VBox content = createContent();
        VBox.setVgrow(content, Priority.ALWAYS);
        
        HBox footer = createFooter();

        root.getChildren().addAll(header, content, footer);
        return root;
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20, 20, 15, 20));
        header.setStyle("-fx-background-color: linear-gradient(to bottom, #FF6600, #FF8533); -fx-background-radius: 15 15 0 0;");

       
        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("👨‍🍳 Seleziona Chef");
        title.setFont(Font.font("Roboto", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeBtn = new Button("✖");
        closeBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.3);" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 15;" +
            "-fx-cursor: hand;" +
            "-fx-min-width: 30;" +
            "-fx-min-height: 30;"
        );
        closeBtn.setOnAction(e -> {
            selectedChef = null;
            stage.close();
        });
        
        titleBox.getChildren().addAll(title, spacer, closeBtn);

       
        HBox searchRow = createSearchRow();

        
        countLabel = new Label("📊 Caricamento...");
        countLabel.setFont(Font.font("Roboto", FontWeight.NORMAL, 12));
        countLabel.setTextFill(Color.WHITE);

        header.getChildren().addAll(titleBox, searchRow, countLabel);
        return header;
    }

    private HBox createSearchRow() {
        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER_LEFT);

      
        searchField = new TextField();
        searchField.setPromptText("🔍 Cerca per nome, cognome o username...");
        searchField.setPrefHeight(35);
        searchField.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 18;" +
            "-fx-border-radius: 18;" +
            "-fx-border-color: rgba(255,255,255,0.5);" +
            "-fx-border-width: 1;" +
            "-fx-padding: 0 15 0 15;" +
            "-fx-font-size: 13px;"
        );
        HBox.setHgrow(searchField, Priority.ALWAYS);

       
        filtroDisponibilita = new ComboBox<>();
        filtroDisponibilita.getItems().addAll("Tutti", "✅ Disponibili", "❌ Non Disponibili");
        filtroDisponibilita.setValue("Tutti");
        filtroDisponibilita.setPrefHeight(35);
        filtroDisponibilita.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 18;" +
            "-fx-border-radius: 18;" +
            "-fx-border-color: rgba(255,255,255,0.5);" +
            "-fx-border-width: 1;" +
            "-fx-font-size: 13px;"
        );
        filtroDisponibilita.setPrefWidth(160);

       
        Button refreshBtn = new Button("🔄");
        refreshBtn.setPrefSize(35, 35);
        refreshBtn.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 18;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 14px;"
        );
        refreshBtn.setOnAction(e -> caricaChef());

        searchRow.getChildren().addAll(searchField, filtroDisponibilita, refreshBtn);
        return searchRow;
    }

    private VBox createContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(15, 20, 15, 20));

        Label subtitle = new Label("Seleziona uno chef dalla lista:");
        subtitle.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
        subtitle.setTextFill(Color.web("#2c3e50"));

        chefListView = new ListView<>(chefData);
        chefListView.setPrefHeight(300);
        chefListView.setStyle(
            "-fx-background-color: #f8f9fa;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #dee2e6;" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1;"
        );

        chefListView.setCellFactory(lv -> new ChefListCell());

        VBox.setVgrow(chefListView, Priority.ALWAYS);

        content.getChildren().addAll(subtitle, chefListView);
        return content;
    }

    private HBox createFooter() {
        HBox footer = new HBox(12);
        footer.setPadding(new Insets(15, 20, 20, 20));
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = createStyledButton("Annulla", "#6c757d");
        cancelBtn.setOnAction(e -> {
            selectedChef = null;
            stage.close();
        });

        Button selectBtn = createStyledButton("✓ Seleziona", "#FF6600");
        selectBtn.setOnAction(e -> confermaSelezionee());

        footer.getChildren().addAll(cancelBtn, selectBtn);
        return footer;
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefSize(120, 38);
        button.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
        button.setTextFill(Color.WHITE);
        button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 20;" +
            "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: derive(" + color + ", -15%);" +
            "-fx-background-radius: 20;" +
            "-fx-cursor: hand;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;"
        ));

        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 20;" +
            "-fx-cursor: hand;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;"
        ));

        return button;
    }

   
    private class ChefListCell extends ListCell<Chef> {
        @Override
        protected void updateItem(Chef chef, boolean empty) {
            super.updateItem(chef, empty);
            
            if (empty || chef == null) {
                setText(null);
                setGraphic(null);
                setStyle("");
            } else {
                HBox cellBox = new HBox(12);
                cellBox.setAlignment(Pos.CENTER_LEFT);
                cellBox.setPadding(new Insets(10));

                Label iconLabel = new Label(Boolean.TRUE.equals(chef.getDisponibilita()) ? "✅" : "❌");
                iconLabel.setFont(Font.font(16));

               
                VBox infoBox = new VBox(3);
                
                String nome = Optional.ofNullable(chef.getNome()).orElse("");
                String cognome = Optional.ofNullable(chef.getCognome()).orElse("");
                Label nameLabel = new Label(nome + " " + cognome);
                nameLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
                nameLabel.setTextFill(Color.web("#2c3e50"));

                String username = Optional.ofNullable(chef.getUsername()).orElse("");
                Label usernameLabel = new Label("@" + username);
                usernameLabel.setFont(Font.font("Roboto", FontWeight.NORMAL, 12));
                usernameLabel.setTextFill(Color.web("#6c757d"));

                infoBox.getChildren().addAll(nameLabel, usernameLabel);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label badgeLabel = new Label(
                    Boolean.TRUE.equals(chef.getDisponibilita()) ? "DISPONIBILE" : "NON DISPONIBILE"
                );
                badgeLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 10));
                badgeLabel.setPadding(new Insets(4, 10, 4, 10));
                
                if (Boolean.TRUE.equals(chef.getDisponibilita())) {
                    badgeLabel.setStyle(
                        "-fx-background-color: #d4edda;" +
                        "-fx-text-fill: #155724;" +
                        "-fx-background-radius: 12;"
                    );
                } else {
                    badgeLabel.setStyle(
                        "-fx-background-color: #f8d7da;" +
                        "-fx-text-fill: #721c24;" +
                        "-fx-background-radius: 12;"
                    );
                }

                cellBox.getChildren().addAll(iconLabel, infoBox, spacer, badgeLabel);
                
                setGraphic(cellBox);
                setText(null);
                
             
                setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                setOnMouseEntered(e -> {
                    if (!isEmpty()) {
                        setStyle("-fx-background-color: #e9ecef; -fx-background-radius: 8; -fx-padding: 5;");
                    }
                });
                setOnMouseExited(e -> {
                    if (!isEmpty()) {
                        setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                    }
                });
            }
        }
    }

    private void setupEventHandlers() {
        chefListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Chef sel = chefListView.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    confermaSelezionee();
                }
            }
        });

       
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                applicaFiltri();
            }
        });

      
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applicaFiltri());
        filtroDisponibilita.setOnAction(e -> applicaFiltri());
    }

    private void confermaSelezionee() {
        Chef sel = chefListView.getSelectionModel().getSelectedItem();
        
        if (sel == null) {
            StyleHelper.showValidationDialog(
                "Nessuna Selezione", 
                "Seleziona uno chef dalla lista prima di confermare"
            );
            return;
        }

      
        if (!Boolean.TRUE.equals(sel.getDisponibilita())) {
            StyleHelper.showErrorDialog(
                "Chef Non Disponibile", 
                "Lo chef selezionato non è attualmente disponibile.\n\n" +
                "Seleziona uno chef con stato 'DISPONIBILE' ✅"
            );
            return;
        }

        selectedChef = sel;
        stage.close();
    }

    private void caricaChef() {
        try {
            List<Chef> tuttiChef = chefController.getAllChef();
            if (tuttiChef == null) {
                chefData.clear();
            } else {
                chefData.setAll(tuttiChef);
            }

            aggiornaContatore();

            if (chefData.isEmpty()) {
                StyleHelper.showInfoDialog("Avviso", "Nessun chef disponibile nel sistema");
            }

          
            setupEventHandlers();

        } catch (DataAccessException e) {
            StyleHelper.showErrorDialog("Errore", "Errore nel caricamento chef: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore imprevisto nel caricamento chef: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void applicaFiltri() {
        try {
            List<Chef> tuttiChef = chefController.getAllChef();
            if (tuttiChef == null) {
                chefData.clear();
                aggiornaContatore();
                return;
            }

            String searchText = Optional.ofNullable(searchField.getText()).orElse("").toLowerCase().trim();
            String filtroDisp = Optional.ofNullable(filtroDisponibilita.getValue()).orElse("Tutti");

            List<Chef> chefFiltrati = tuttiChef.stream()
                .filter(chef -> {
                    String nome = Optional.ofNullable(chef.getNome()).orElse("").toLowerCase();
                    String cognome = Optional.ofNullable(chef.getCognome()).orElse("").toLowerCase();
                    String username = Optional.ofNullable(chef.getUsername()).orElse("").toLowerCase();

                    boolean matchSearch = searchText.isEmpty()
                        || nome.contains(searchText)
                        || cognome.contains(searchText)
                        || username.contains(searchText);

                    boolean matchDisponibilita = true;
                    if ("✅ Disponibili".equals(filtroDisp)) {
                        matchDisponibilita = Boolean.TRUE.equals(chef.getDisponibilita());
                    } else if ("❌ Non Disponibili".equals(filtroDisp)) {
                        matchDisponibilita = !Boolean.TRUE.equals(chef.getDisponibilita());
                    }

                    return matchSearch && matchDisponibilita;
                })
                .collect(Collectors.toList());

            chefData.setAll(chefFiltrati);
            aggiornaContatore();

        } catch (DataAccessException e) {
            StyleHelper.showErrorDialog("Errore", "Errore nell'applicazione dei filtri: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore imprevisto durante i filtri: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void aggiornaContatore() {
        if (countLabel != null) {
            long disponibili = chefData.stream()
                .filter(c -> Boolean.TRUE.equals(c.getDisponibilita()))
                .count();
            long nonDisponibili = chefData.size() - disponibili;

            countLabel.setText(String.format(
                "📊 Trovati: %d chef (✅ %d disponibili, ❌ %d non disponibili)",
                chefData.size(), disponibili, nonDisponibili
            ));
        }
    }
}