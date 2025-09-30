package Gui;

import controller.GestioneCorsoController;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Chef;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SelezionaChefDialog {

    private final Stage dialog;
    private final ListView<Chef> chefListView;
    private final FilteredList<Chef> filteredChefList;
    private final List<Chef> giaSelezionati; // chef già scelti nel corso
    private Chef chefLoggato;
    private double xOffset = 0;
    private double yOffset = 0;

    public SelezionaChefDialog(Stage owner, GestioneCorsoController gestioneController, List<Chef> chefSelezionati) throws SQLException {
        this.dialog = new Stage();
        this.dialog.initOwner(owner);
        this.dialog.initModality(Modality.APPLICATION_MODAL);
        this.dialog.initStyle(StageStyle.UNDECORATED);
        this.dialog.setTitle("Seleziona Chef");

        this.giaSelezionati = new ArrayList<>(chefSelezionati);
        this.chefLoggato = gestioneController.getChefLoggato();

        // Root con sfondo gradiente
        StackPane root = new StackPane();
        root.setPrefSize(600, 700);

        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FF9966")),
                new Stop(0.5, Color.web("#FFB366")),
                new Stop(1, Color.web("#FFCC99")));
        Region background = new Region();
        background.setBackground(new Background(new BackgroundFill(gradient, null, null)));
        background.prefWidthProperty().bind(root.widthProperty());
        background.prefHeightProperty().bind(root.heightProperty());
        root.getChildren().add(background);

        // Card principale
        VBox card = new VBox(18);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(30));
        card.setMaxWidth(520);
        card.setMaxHeight(640);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.9); " +
                "-fx-background-radius: 20; " +
                "-fx-border-radius: 20; " +
                "-fx-border-color: rgba(0,0,0,0.06); " +
                "-fx-border-width: 1;");
        card.setEffect(new DropShadow(18, Color.web("#000000", 0.12)));

        // Titolo
        Label title = new Label("👨‍🍳 Seleziona Chef");
        title.setFont(Font.font("Inter", FontWeight.EXTRA_BOLD, 24));
        title.setTextFill(Color.web("#FF6600"));

        // Info chef loggato
        VBox loggedChefBox = new VBox(5);
        loggedChefBox.setAlignment(Pos.CENTER);
        loggedChefBox.setPadding(new Insets(12));
        loggedChefBox.setStyle("-fx-background-color: #FFF8F0; " +
                "-fx-background-radius: 12; " +
                "-fx-border-color: #FFB366; " +
                "-fx-border-radius: 12; " +
                "-fx-border-width: 1.5;");

        Label loggedLabel = new Label("Chef Loggato");
        loggedLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 11));
        loggedLabel.setTextFill(Color.web("#999999"));

        Label loggedChefName = new Label(
                chefLoggato != null
                        ? chefLoggato.getNome() + " " + chefLoggato.getCognome()
                        : "Nessuno chef loggato"
        );
        loggedChefName.setFont(Font.font("Inter", FontWeight.BOLD, 15));
        loggedChefName.setTextFill(Color.web("#FF6600"));

        loggedChefBox.getChildren().addAll(loggedLabel, loggedChefName);

        // Campo ricerca
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Cerca per nome o cognome...");
        searchField.setPrefHeight(40);
        searchField.setFont(Font.font("Inter", 13));
        searchField.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 10; " +
                "-fx-border-radius: 10; " +
                "-fx-border-color: #FFB366; " +
                "-fx-border-width: 1.5; " +
                "-fx-padding: 0 14;");

        // Lista chef filtrata
        List<Chef> tuttiChef = gestioneController.getTuttiGliChef();
        filteredChefList = new FilteredList<>(FXCollections.observableArrayList(tuttiChef), p -> true);

        chefListView = new ListView<>(filteredChefList);
        chefListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        chefListView.setPrefHeight(320);
        chefListView.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-border-radius: 12; -fx-background-radius: 12;");

        // Cell factory con blocco chef già selezionati
        chefListView.setCellFactory(lv -> new ListCell<Chef>() {
            private final VBox cellBox = new VBox(3);
            private final Label nameLabel = new Label();
            private final Label usernameLabel = new Label();

            {
                cellBox.setPadding(new Insets(10));
                cellBox.getChildren().addAll(nameLabel, usernameLabel);
                nameLabel.setFont(Font.font("Inter", FontWeight.BOLD, 13));
                usernameLabel.setFont(Font.font("Inter", FontWeight.NORMAL, 11));
            }

            @Override
            protected void updateItem(Chef chef, boolean empty) {
                super.updateItem(chef, empty);
                if (empty || chef == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    nameLabel.setText(chef.getNome() + " " + chef.getCognome());
                    usernameLabel.setText("@" + chef.getUsername());

                    if (giaSelezionati.contains(chef)) {
                        // chef già selezionato -> grigio e non selezionabile
                        cellBox.setStyle("-fx-background-color: #EEEEEE; -fx-background-radius: 12;");
                        nameLabel.setTextFill(Color.GRAY);
                        usernameLabel.setTextFill(Color.GRAY);
                        setDisable(true);
                    } else {
                        cellBox.setStyle("-fx-background-color: transparent; -fx-background-radius: 12;");
                        nameLabel.setTextFill(Color.web("#333333"));
                        usernameLabel.setTextFill(Color.web("#777777"));
                        setDisable(false);
                    }

                    setGraphic(cellBox);
                    setText(null);
                }
            }
        });

        // Pre-seleziona lo chef loggato
        if (chefLoggato != null) {
            for (int i = 0; i < filteredChefList.size(); i++) {
                if (filteredChefList.get(i).getUsername().equals(chefLoggato.getUsername())) {
                    chefListView.getSelectionModel().select(i);
                    break;
                }
            }
        }

        // Filtro ricerca
        searchField.textProperty().addListener((obs, old, val) -> {
            String filtro = val.toLowerCase().trim();
            filteredChefList.setPredicate(c -> {
                if (filtro.isEmpty()) return true;
                return c.getNome().toLowerCase().contains(filtro) ||
                        c.getCognome().toLowerCase().contains(filtro) ||
                        c.getUsername().toLowerCase().contains(filtro);
            });
        });

        // Label contatore
        Label countLabel = new Label();
        countLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 12));
        countLabel.setTextFill(Color.web("#666666"));
        updateCountLabel(countLabel);
        chefListView.getSelectionModel().getSelectedItems().addListener(
                (javafx.collections.ListChangeListener<Chef>) c -> updateCountLabel(countLabel)
        );

        // Pulsanti conferma / annulla
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER);

        Button confermaBtn = createModernButton("✅ Conferma Selezione", "#FF6600", "#FF8533");
        confermaBtn.setPrefWidth(220);
        confermaBtn.setOnAction(e -> dialog.close());

        Button annullaBtn = createModernButton("❌ Annulla", "#999999", "#BBBBBB");
        annullaBtn.setPrefWidth(140);
        annullaBtn.setOnAction(e -> {
            chefListView.getSelectionModel().clearSelection();
            dialog.close();
        });

        buttonBox.getChildren().addAll(annullaBtn, confermaBtn);

        card.getChildren().addAll(title, loggedChefBox, searchField, chefListView, countLabel, buttonBox);
        root.getChildren().add(card);

        addWindowControls(root);
        makeDraggable(root, card);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
    }

    private void updateCountLabel(Label label) {
        int count = chefListView.getSelectionModel().getSelectedItems().size();
        String text = count == 0 ? "Nessuno chef selezionato" :
                count == 1 ? "1 chef selezionato" :
                        count + " chef selezionati";
        label.setText("📋 " + text);
    }

    private Button createModernButton(String text, String baseColor, String hoverColor) {
        Button btn = new Button(text);
        btn.setPrefHeight(42);
        btn.setFont(Font.font("Inter", FontWeight.BOLD, 13));
        btn.setTextFill(Color.WHITE);

        String baseStyle = "-fx-background-color: " + baseColor + "; " +
                "-fx-background-radius: 21; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8, 0, 0, 3);";

        String hoverStyle = "-fx-background-color: " + hoverColor + "; " +
                "-fx-background-radius: 21; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 10, 0, 0, 4);";

        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        btn.setOnMousePressed(e -> {
            btn.setScaleX(0.97);
            btn.setScaleY(0.97);
        });
        btn.setOnMouseReleased(e -> {
            btn.setScaleX(1.0);
            btn.setScaleY(1.0);
        });

        return btn;
    }

    private void addWindowControls(StackPane root) {
        HBox controls = new HBox(6);
        controls.setAlignment(Pos.TOP_RIGHT);
        controls.setPadding(new Insets(10));
        controls.setPickOnBounds(false);

        Button closeBtn = new Button("✖");
        closeBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        closeBtn.setTextFill(Color.WHITE);
        closeBtn.setPrefSize(36, 36);
        closeBtn.setStyle("-fx-background-color: rgba(255,102,0,0.6); -fx-background-radius: 18; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> {
            chefListView.getSelectionModel().clearSelection();
            dialog.close();
        });

        controls.getChildren().add(closeBtn);
        StackPane.setAlignment(controls, Pos.TOP_RIGHT);
        root.getChildren().add(controls);
    }

    private void makeDraggable(StackPane root, VBox card) {
        card.setOnMousePressed(e -> {
            xOffset = e.getSceneX();
            yOffset = e.getSceneY();
        });
        card.setOnMouseDragged(e -> {
            dialog.setX(e.getScreenX() - xOffset);
            dialog.setY(e.getScreenY() - yOffset);
        });
    }

    public void showAndWait() {
        dialog.showAndWait();
    }

    public List<Chef> getSelezionati() {
        return new ArrayList<>(chefListView.getSelectionModel().getSelectedItems());
    }
}
