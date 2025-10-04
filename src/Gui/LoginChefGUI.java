package Gui;

import controller.ChefController;
import controller.GestioneCorsoController;
import controller.VisualizzaCorsiController;
import service.GestioneCorsiCucina;
import util.DBConnection;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Chef;

public class LoginChefGUI extends Application {
    private static ChefController chefController;
    private static GestioneCorsiCucina corsiService;
    private double xOffset = 0;
    private double yOffset = 0;

    private StackPane contentPane;

    public static void setController(ChefController controller, GestioneCorsiCucina corsiServiceArg) {
        chefController = controller;
        corsiService = corsiServiceArg;
    }

    @Override
    public void start(Stage primaryStage) {
        if (chefController == null || corsiService == null) {
            throw new IllegalStateException("Controller o Service non inizializzati");
        }

        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setTitle("Chef Login - UninaFoodLab");

        StackPane root = new StackPane();
        root.setPrefSize(500, 700);

        createBackground(root);

        contentPane = new StackPane();
        VBox loginCard = createLoginCard();
        contentPane.getChildren().add(loginCard);
        root.getChildren().add(contentPane);

        HBox windowButtons = createWindowButtons(primaryStage);
        root.getChildren().add(windowButtons);
        StackPane.setAlignment(windowButtons, Pos.TOP_RIGHT);
        StackPane.setMargin(windowButtons, new Insets(10));

        makeDraggable(root, primaryStage);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        DBConnection.closeDataSource();
        System.out.println("Pool DB chiuso al termine dell'applicazione.");
    }

    private void createBackground(StackPane root) {
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FF9966")),
                new Stop(1, Color.web("#FFCC99")));
        Region background = new Region();
        background.setBackground(new Background(new BackgroundFill(gradient, null, null)));
        background.setPrefSize(500, 700);
        root.getChildren().add(background);
    }

    private VBox createLoginCard() {
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(380);
        card.setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 20;" +
                "-fx-border-radius: 20;" +
                "-fx-border-color: #FF9966;" +
                "-fx-border-width: 2;");

        Label titleLabel = new Label("UninaFoodLab");
        titleLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 30));
        titleLabel.setTextFill(Color.web("#FF6600"));

        Label subtitleLabel = new Label("Accedi al tuo account");
        subtitleLabel.setFont(Font.font("Roboto", FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(Color.web("#FF8533"));

        VBox formContainer = new VBox(15);
        formContainer.setAlignment(Pos.CENTER);

        VBox usernameContainer = createStylishTextField("Username", false);
        TextField usernameField = (TextField) ((StackPane) usernameContainer.getChildren().get(0)).getChildren().get(1);

        VBox passwordContainer = createStylishTextField("Password", true);
        PasswordField passwordField = (PasswordField) ((StackPane) passwordContainer.getChildren().get(0)).getChildren().get(1);

        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("Roboto", FontWeight.MEDIUM, 13));
        messageLabel.setTextFill(Color.web("#FF6600"));
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);

        Button loginButton = createStylishButton("ACCEDI", "#FF6600", "#FF8533");
        Button registerButton = createStylishButton("REGISTRATI", "#FFCC99", "#FFD9B3");

        setupButtonEvents(loginButton, registerButton, usernameField, passwordField, messageLabel);

        formContainer.getChildren().addAll(usernameContainer, passwordContainer);

        HBox buttonContainer = new HBox(15);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().addAll(registerButton, loginButton);

        VBox headerContainer = new VBox(10);
        headerContainer.setAlignment(Pos.CENTER);
        headerContainer.getChildren().addAll(titleLabel, subtitleLabel);

        card.getChildren().addAll(headerContainer, formContainer, buttonContainer, messageLabel);

        return card;
    }

    private VBox createStylishTextField(String placeholder, boolean isPassword) {
        VBox container = new VBox(3);
        StackPane fieldContainer = new StackPane();
        fieldContainer.setPrefHeight(45);

        Region background = new Region();
        background.setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 15;" +
                "-fx-border-radius: 15;" +
                "-fx-border-color: #FF9966;" +
                "-fx-border-width: 1.5;");

        TextInputControl inputField = isPassword ? new PasswordField() : new TextField();
        inputField.setPromptText(placeholder);
        inputField.setStyle("-fx-background-color: transparent;" +
                "-fx-text-fill: black;" +
                "-fx-prompt-text-fill: gray;" +
                "-fx-font-size: 14px;" +
                "-fx-padding: 0 15 0 15;");
        inputField.setPrefWidth(300);

        fieldContainer.getChildren().addAll(background, inputField);
        container.getChildren().add(fieldContainer);
        return container;
    }

    private HBox createWindowButtons(Stage stage) {
        Button closeButton = new Button("✕");
        Button minimizeButton = new Button("_");
        Button maximizeButton = new Button("□");

        Button[] buttons = {minimizeButton, maximizeButton, closeButton};
        for (Button btn : buttons) {
            btn.setPrefSize(35, 35);
            btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            btn.setTextFill(Color.WHITE);
            btn.setStyle("-fx-background-color: rgba(255,140,0,0.5); -fx-background-radius: 20;");
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

    private Button createStylishButton(String text, String baseColor, String hoverColor) {
        Button button = new Button(text);
        button.setPrefSize(130, 45);
        button.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        button.setTextFill(Color.web("#4B2E2E"));
        button.setStyle("-fx-background-color: " + baseColor + "; -fx-background-radius: 20; -fx-cursor: hand;");

        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: " + hoverColor + "; -fx-background-radius: 20; -fx-cursor: hand;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + baseColor + "; -fx-background-radius: 20; -fx-cursor: hand;"));

        return button;
    }

    private void setupButtonEvents(Button loginButton, Button registerButton, TextField usernameField,
                                   PasswordField passwordField, Label messageLabel) {
        loginButton.setOnAction(e -> {
            try {
                Chef chef = chefController.login(usernameField.getText(), passwordField.getText());
                messageLabel.setText("✅ Login effettuato: " + chef.getUsername());
                messageLabel.setTextFill(Color.web("#FF6600"));

                GestioneCorsoController gestioneCorsoController =
                        new GestioneCorsoController(corsiService, chefController.getGestioneChef());
                
                gestioneCorsoController.setChefLoggato(chef);

                VisualizzaCorsiController corsiController = new VisualizzaCorsiController(corsiService, chef);

                ChefMenuGUI menu = new ChefMenuGUI();
                menu.setChefLoggato(chef);
                menu.setController(corsiController, gestioneCorsoController);

                Stage menuStage = new Stage();
                menu.start(menuStage);

                Stage loginStage = (Stage) loginButton.getScene().getWindow();
                loginStage.close();

            } catch (Exception ex) {
                messageLabel.setText("❌ Errore: " + ex.getMessage());
                messageLabel.setTextFill(Color.web("#CC3300"));
                ex.printStackTrace();
            }
        });

        registerButton.setOnAction(e -> {
            contentPane.getChildren().clear();
            RegistrazioneChefGUI regPane = new RegistrazioneChefGUI(chefController, () -> {
                contentPane.getChildren().clear();
                contentPane.getChildren().add(createLoginCard());
            });
            contentPane.getChildren().add(regPane);
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