package Gui;

import controller.ChefController;
import controller.CorsiController;
import service.GestioneCorsiCucina;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.ImageView;
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

		VBox loginCard = createLoginCard();
		root.getChildren().add(loginCard);

		Button closeButton = createModernCloseButton();
		root.getChildren().add(closeButton);
		StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);
		StackPane.setMargin(closeButton, new Insets(15));
		closeButton.toFront();

		makeDraggable(root, primaryStage);

		Scene scene = new Scene(root);
		scene.setFill(Color.TRANSPARENT);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void createBackground(StackPane root) {
		LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
				new Stop(0, Color.web("#141E30")), new Stop(1, Color.web("#243B55")));
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
		card.setStyle("-fx-background-color: rgba(20, 30, 50, 0.85);" + "-fx-background-radius: 20;"
				+ "-fx-border-radius: 20;" + "-fx-border-color: rgba(255,255,255,0.15);" + "-fx-border-width: 1;");

		DropShadow shadow = new DropShadow();
		shadow.setRadius(15);
		shadow.setColor(Color.web("#000000", 0.4));
		shadow.setOffsetY(5);
		card.setEffect(shadow);

		ImageView chefIcon = createChefIcon();

		Label titleLabel = new Label("UninaFoodLab");
		titleLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 30));
		titleLabel.setTextFill(Color.web("#A0CFFF"));

		Label subtitleLabel = new Label("Accedi al tuo account");
		subtitleLabel.setFont(Font.font("Roboto", FontWeight.NORMAL, 14));
		subtitleLabel.setTextFill(Color.web("#D0E4FF", 0.8));

		VBox formContainer = new VBox(15);
		formContainer.setAlignment(Pos.CENTER);

		VBox usernameContainer = createStylishTextField("Username", false);
		TextField usernameField = (TextField) ((StackPane) usernameContainer.getChildren().get(0)).getChildren().get(1);

		VBox passwordContainer = createStylishTextField("Password", true);
		PasswordField passwordField = (PasswordField) ((StackPane) passwordContainer.getChildren().get(0)).getChildren()
				.get(1);

		Label messageLabel = new Label();
		messageLabel.setFont(Font.font("Roboto", FontWeight.MEDIUM, 13));
		messageLabel.setTextFill(Color.WHITE);
		messageLabel.setWrapText(true);
		messageLabel.setAlignment(Pos.CENTER);

		Button loginButton = createStylishButton("ACCEDI", "#4D79FF", "#5C8DFF");
		Button registerButton = createStylishButton("REGISTRATI", "#50E3C2", "#64F0C8");

		setupButtonEvents(loginButton, registerButton, usernameField, passwordField, messageLabel);

		formContainer.getChildren().addAll(usernameContainer, passwordContainer);

		HBox buttonContainer = new HBox(15);
		buttonContainer.setAlignment(Pos.CENTER);
		buttonContainer.getChildren().addAll(registerButton, loginButton);

		VBox headerContainer = new VBox(10);
		headerContainer.setAlignment(Pos.CENTER);
		headerContainer.getChildren().addAll(chefIcon, titleLabel, subtitleLabel);

		card.getChildren().addAll(headerContainer, formContainer, buttonContainer, messageLabel);

		return card;
	}

	private ImageView createChefIcon() {
		ImageView imageView = new ImageView();
		imageView.setFitWidth(60);
		imageView.setFitHeight(60);
		return imageView;
	}

	private VBox createStylishTextField(String placeholder, boolean isPassword) {
		VBox container = new VBox(3);
		StackPane fieldContainer = new StackPane();
		fieldContainer.setPrefHeight(45);

		Region background = new Region();
		background.setStyle("-fx-background-color: rgba(255,255,255,0.1);" + "-fx-background-radius: 15;"
				+ "-fx-border-radius: 15;" + "-fx-border-color: rgba(255,255,255,0.2);" + "-fx-border-width: 1;");
		InnerShadow inner = new InnerShadow();
		inner.setColor(Color.web("#000000", 0.2));
		background.setEffect(inner);

		TextInputControl inputField = isPassword ? new PasswordField() : new TextField();
		inputField.setPromptText(placeholder);
		inputField.setStyle("-fx-background-color: transparent;" + "-fx-text-fill: white;"
				+ "-fx-prompt-text-fill: rgba(255,255,255,0.5);" + "-fx-font-size: 14px;" + "-fx-padding: 0 15 0 15;");
		inputField.setPrefWidth(300);

		fieldContainer.getChildren().addAll(background, inputField);
		container.getChildren().add(fieldContainer);
		return container;
	}

	private Button createModernCloseButton() {
		Button closeButton = new Button("✕");
		closeButton.setPrefSize(35, 35);
		closeButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
		closeButton.setTextFill(Color.WHITE);
		closeButton
				.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 20; -fx-cursor: hand;");
		closeButton.setOnMouseEntered(
				e -> closeButton.setStyle("-fx-background-color: red; -fx-background-radius: 20; -fx-cursor: hand;"));
		closeButton.setOnMouseExited(e -> closeButton
				.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 20; -fx-cursor: hand;"));
		closeButton.setOnAction(e -> System.exit(0));
		return closeButton;
	}

	private Button createStylishButton(String text, String baseColor, String hoverColor) {
		Button button = new Button(text);
		button.setPrefSize(130, 45);
		button.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
		button.setTextFill(Color.WHITE);
		button.setStyle("-fx-background-color: " + baseColor + "; -fx-background-radius: 20; -fx-cursor: hand;");
		DropShadow shadow = new DropShadow();
		shadow.setColor(Color.web("#000000", 0.2));
		shadow.setRadius(5);
		button.setEffect(shadow);

		button.setOnMouseEntered(e -> button
				.setStyle("-fx-background-color: " + hoverColor + "; -fx-background-radius: 20; -fx-cursor: hand;"));
		button.setOnMouseExited(e -> button
				.setStyle("-fx-background-color: " + baseColor + "; -fx-background-radius: 20; -fx-cursor: hand;"));
		return button;
	}

	private void setupButtonEvents(Button loginButton, Button registerButton, TextField usernameField,
			PasswordField passwordField, Label messageLabel) {
		loginButton.setOnAction(e -> {
			try {
// Effettua il login con il controller statico
				Chef chef = chefController.login(usernameField.getText(), passwordField.getText());
				messageLabel.setText("✅ Login effettuato: " + chef.getUsername());
				messageLabel.setTextFill(Color.web("#00FF7F"));

// Usa lo stesso controller passato al LoginChefGUI
				CorsiController controller = new CorsiController(corsiService, chefController.getGestioneChef(), chef);

				ChefMenuGUI menu = new ChefMenuGUI();
				menu.setChefLoggato(chef);
				menu.setController(controller); // PASSA controller corretto
				Stage menuStage = new Stage();
				menu.start(menuStage);

// Chiudi la finestra login
				Stage loginStage = (Stage) loginButton.getScene().getWindow();
				loginStage.close();
			} catch (Exception ex) {
				messageLabel.setText("❌ Errore: " + ex.getMessage());
				messageLabel.setTextFill(Color.web("#FF4C4C"));
				ex.printStackTrace();
			}
		});

		registerButton.setOnAction(e -> {
			RegistrazioneChefGUI registrazioneGUI = new RegistrazioneChefGUI(chefController);
			Stage currentStage = (Stage) registerButton.getScene().getWindow();
			registrazioneGUI.show(currentStage);
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
