package Gui;

import controller.GestioneCorsoController;
import controller.VisualizzaCorsiController;
import controller.VisualizzaRicetteController;
import controller.GestioneSessioniController;
import dao.RicettaDAO;
import dao.UsaDAO;
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

public class ChefMenuGUI {

    private Chef chefLoggato;
    private VisualizzaCorsiController corsiController; 
    private double xOffset = 0;
    private double yOffset = 0;
    private StackPane menuRoot;

    public void setChefLoggato(Chef chef) {
        this.chefLoggato = chef;
    }

    public void setController(VisualizzaCorsiController controller) { 
        this.corsiController = controller;
    }

    public StackPane getRoot() {
        return menuRoot;
    }

    public void start(Stage stage) {
        if (chefLoggato == null || corsiController == null) {
            throw new IllegalStateException("Chef e controller devono essere impostati prima di start().");
        }

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Menu Chef - " + chefLoggato.getUsername());

        menuRoot = new StackPane();
        menuRoot.setPrefSize(500, 700);

        createBackground(menuRoot);

        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(380);
        card.setStyle("-fx-background-color: white;" + "-fx-background-radius: 20;" + "-fx-border-radius: 20;"
                + "-fx-border-color: #FF9966;" + "-fx-border-width: 2;");

        DropShadow shadow = new DropShadow();
        shadow.setRadius(10);
        shadow.setColor(Color.web("#000000", 0.2));
        shadow.setOffsetY(3);
        card.setEffect(shadow);

        Label welcomeLabel = new Label("Benvenuto, " + chefLoggato.getUsername());
        welcomeLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 26));
        welcomeLabel.setTextFill(Color.web("#FF6600"));

        VBox buttonContainer = new VBox(15);
        buttonContainer.setAlignment(Pos.CENTER);

        Button visualizzaCorsiBtn = createStylishButton("Visualizza Corsi", "#FF6600", "#FF8533");
        Button visualizzaRicetteBtn = createStylishButton("Visualizza Ricette", "#FF6600", "#FF8533");
        Button gestisciSessioniBtn = createStylishButton("Gestisci Sessioni", "#FF6600", "#FF8533"); // ✅ nuovo pulsante
        Button eliminaAccountBtn = createStylishButton("Elimina Account", "#FF6600", "#FF8533");
        Button logoutButton = createStylishButton("Logout", "#FFCC99", "#FFD9B3");

        buttonContainer.getChildren().addAll(
                visualizzaCorsiBtn, visualizzaRicetteBtn, gestisciSessioniBtn, eliminaAccountBtn, logoutButton);
        card.getChildren().addAll(welcomeLabel, buttonContainer);
        menuRoot.getChildren().add(card);

        HBox windowButtons = createWindowButtons(stage);
        menuRoot.getChildren().add(windowButtons);
        StackPane.setAlignment(windowButtons, Pos.TOP_RIGHT);
        StackPane.setMargin(windowButtons, new Insets(10));

        makeDraggable(menuRoot, stage);

        // Eventi pulsanti
        visualizzaCorsiBtn.setOnAction(e -> apriVisualizzaCorsi(stage));
        visualizzaRicetteBtn.setOnAction(e -> apriVisualizzaRicette(stage));
        gestisciSessioniBtn.setOnAction(e -> apriGestioneSessioni(stage)); // ✅ nuovo evento
        eliminaAccountBtn.setOnAction(e -> eliminaAccount(stage));
        logoutButton.setOnAction(e -> stage.close());

        Scene scene = new Scene(menuRoot);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
    }

    private void createBackground(StackPane root) {
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FF9966")), new Stop(1, Color.web("#FFCC99")));
        Region background = new Region();
        background.setBackground(new Background(new BackgroundFill(gradient, null, null)));
        background.setPrefSize(500, 700);
        root.getChildren().add(background);
    }

    private Button createStylishButton(String text, String baseColor, String hoverColor) {
        Button button = new Button(text);
        button.setPrefSize(150, 45);
        button.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        button.setTextFill(Color.web("#4B2E2E"));
        button.setStyle("-fx-background-color: " + baseColor + "; -fx-background-radius: 20; -fx-cursor: hand;");
        DropShadow shadow = new DropShadow();
        shadow.setRadius(5);
        shadow.setColor(Color.web("#000000", 0.2));
        button.setEffect(shadow);

        button.setOnMouseEntered(e -> button
                .setStyle("-fx-background-color: " + hoverColor + "; -fx-background-radius: 20; -fx-cursor: hand;"));
        button.setOnMouseExited(e -> button
                .setStyle("-fx-background-color: " + baseColor + "; -fx-background-radius: 20; -fx-cursor: hand;"));
        return button;
    }

    private HBox createWindowButtons(Stage stage) {
        Button closeButton = new Button("✕");
        Button minimizeButton = new Button("_");
        Button maximizeButton = new Button("□");

        Button[] buttons = { minimizeButton, maximizeButton, closeButton };
        for (Button btn : buttons) {
            btn.setPrefSize(35, 35);
            btn.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
            btn.setTextFill(Color.WHITE);
            btn.setStyle("-fx-background-color: rgba(255,140,0,0.5); -fx-background-radius: 20; -fx-cursor: hand;");
            btn.setFocusTraversable(false);
        }

        closeButton.setOnAction(e -> stage.close());
        minimizeButton.setOnAction(e -> stage.setIconified(true));
        maximizeButton.setOnAction(e -> stage.setMaximized(!stage.isMaximized()));

        HBox box = new HBox(5, minimizeButton, maximizeButton, closeButton);
        box.setAlignment(Pos.TOP_RIGHT);
        box.setPickOnBounds(false);
        return box;
    }

    private void apriVisualizzaCorsi(Stage stage) {
        try {
            GestioneCorsoController gestioneCorsoController = new GestioneCorsoController(
                    corsiController.getGestioneCorsi(), null);

            gestioneCorsoController.setChefLoggato(corsiController.getChefLoggato());

            VisualizzaCorsiGUI corsiGUI = new VisualizzaCorsiGUI();
            corsiGUI.setControllers(corsiController, gestioneCorsoController, menuRoot);

            stage.getScene().setRoot(corsiGUI.getRoot());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void apriVisualizzaRicette(Stage stage) {
        try {
            VisualizzaRicetteController controller = new VisualizzaRicetteController(
                    new RicettaDAO(), new UsaDAO());

            VisualizzaRicetteGUI gui = new VisualizzaRicetteGUI();
            gui.setController(controller, menuRoot);
            gui.show(stage);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void apriGestioneSessioni(Stage stage) {
        try {
            GestioneSessioniController sessioniController = new GestioneSessioniController();
            GestioneSessioniGUI gui = new GestioneSessioniGUI();
            gui.setController(sessioniController);

            stage.getScene().setRoot(gui.getRoot());

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
