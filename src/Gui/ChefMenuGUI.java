package Gui;

import controller.GestioneCorsoController;
import controller.CorsiController;
import controller.VisualizzaCorsiController;
import controller.VisualizzaRicetteController;
import dao.RicettaDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Chef;
import service.GestioneRicette;

public class ChefMenuGUI {

    private Chef chefLoggato;
    private CorsiController corsiController;
    private double xOffset = 0;
    private double yOffset = 0;

    public void setChefLoggato(Chef chef) {
        this.chefLoggato = chef;
    }

    public void setController(CorsiController controller) {
        this.corsiController = controller;
    }

    public void start(Stage stage) {
        if (chefLoggato == null || corsiController == null) {
            throw new IllegalStateException("Chef e controller devono essere impostati prima di start().");
        }

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Menu Chef - " + chefLoggato.getUsername());

        StackPane root = new StackPane();
        root.setPrefSize(500, 700);

        createBackground(root);

        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(380);
        card.setStyle("-fx-background-color: rgba(20,30,50,0.85);"
                + "-fx-background-radius: 20;"
                + "-fx-border-color: rgba(255,255,255,0.15);"
                + "-fx-border-radius: 20;"
                + "-fx-border-width: 1;");

        DropShadow shadow = new DropShadow();
        shadow.setRadius(15);
        shadow.setColor(Color.web("#000000", 0.4));
        shadow.setOffsetY(5);
        card.setEffect(shadow);

        Label welcomeLabel = new Label("Benvenuto, " + chefLoggato.getUsername());
        welcomeLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 26));
        welcomeLabel.setTextFill(Color.web("#A0CFFF"));

        VBox buttonContainer = new VBox(15);
        buttonContainer.setAlignment(Pos.CENTER);

        Button visualizzaCorsiBtn = createStylishButton("Visualizza Corsi", "#4D79FF", "#5C8DFF");
        Button visualizzaRicetteBtn = createStylishButton("Visualizza Ricette", "#4D79FF", "#5C8DFF");
        Button eliminaAccountBtn = createStylishButton("Elimina Account", "#FF4C4C", "#FF6B6B");
        Button logoutButton = createStylishButton("Logout", "#50E3C2", "#64F0C8");

        buttonContainer.getChildren().addAll(visualizzaCorsiBtn, visualizzaRicetteBtn, eliminaAccountBtn, logoutButton);

        card.getChildren().addAll(welcomeLabel, buttonContainer);
        root.getChildren().add(card);

        Button closeButton = createModernCloseButton(stage);
        root.getChildren().add(closeButton);
        StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);
        StackPane.setMargin(closeButton, new Insets(15));

        makeDraggable(root, stage);

        // Eventi
        visualizzaCorsiBtn.setOnAction(e -> apriVisualizzaCorsi(stage));
        visualizzaRicetteBtn.setOnAction(e -> apriVisualizzaRicette(stage));
        eliminaAccountBtn.setOnAction(e -> eliminaAccount(stage));
        logoutButton.setOnAction(e -> stage.close());

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
    }

    private void createBackground(StackPane root) {
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#141E30")),
                new Stop(1, Color.web("#243B55")));
        Region background = new Region();
        background.setBackground(new Background(new BackgroundFill(gradient, null, null)));
        background.setPrefSize(500, 700);
        root.getChildren().add(background);
    }

    private Button createStylishButton(String text, String baseColor, String hoverColor) {
        Button button = new Button(text);
        button.setPrefSize(130, 45);
        button.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        button.setTextFill(Color.WHITE);
        button.setStyle("-fx-background-color: " + baseColor + "; -fx-background-radius: 20; -fx-cursor: hand;");
        DropShadow shadow = new DropShadow();
        shadow.setRadius(5);
        shadow.setColor(Color.web("#000000", 0.2));
        button.setEffect(shadow);

        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: " + hoverColor + "; -fx-background-radius: 20; -fx-cursor: hand;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + baseColor + "; -fx-background-radius: 20; -fx-cursor: hand;"));
        return button;
    }

    private Button createModernCloseButton(Stage stage) {
        Button closeButton = new Button("✕");
        closeButton.setPrefSize(35, 35);
        closeButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        closeButton.setTextFill(Color.WHITE);
        closeButton.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 20; -fx-cursor: hand;");
        closeButton.setOnMouseEntered(e -> closeButton.setStyle("-fx-background-color: red; -fx-background-radius: 20; -fx-cursor: hand;"));
        closeButton.setOnMouseExited(e -> closeButton.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 20; -fx-cursor: hand;"));
        closeButton.setOnAction(e -> stage.close());
        return closeButton;
    }

    private void apriVisualizzaCorsi(Stage stage) {
        try {
            VisualizzaCorsiController visualizzaController = new VisualizzaCorsiController(
                    corsiController.getGestioneCorsi(), corsiController.getChefLoggato());
            GestioneCorsoController gestioneCorsoController = new GestioneCorsoController(
                    corsiController.getGestioneCorsi(), corsiController.getChefService());
            VisualizzaCorsiGUI corsiGUI = new VisualizzaCorsiGUI();
            corsiGUI.setControllers(visualizzaController, gestioneCorsoController);
            corsiGUI.start(new Stage());
            stage.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void apriVisualizzaRicette(Stage stage) {
        try {
            VisualizzaRicetteController controller = new VisualizzaRicetteController(new GestioneRicette(new RicettaDAO()));
            VisualizzaRicetteGUI gui = new VisualizzaRicetteGUI();
            gui.setController(controller);
            gui.start(new Stage());
            stage.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void eliminaAccount(Stage stage) {
        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
        conferma.setHeaderText("Vuoi eliminare definitivamente il tuo account?");
        conferma.setContentText("Questa operazione non può essere annullata.");

        conferma.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    corsiController.eliminaAccount();
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setHeaderText("Account eliminato!");
                    info.showAndWait();
                    stage.close();
                } catch (Exception ex) {
                    Alert errore = new Alert(Alert.AlertType.ERROR);
                    errore.setHeaderText("Errore nell'eliminazione: " + ex.getMessage());
                    errore.showAndWait();
                }
            }
        });
    }

    private void makeDraggable(StackPane root, Stage stage) {
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }
}
