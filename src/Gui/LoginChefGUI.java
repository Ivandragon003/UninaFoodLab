package Gui;

import controller.ChefController;
import controller.GestioneCorsoController;
import controller.RicettaController;
import controller.VisualizzaCorsiController;

import dao.IngredienteDAO;
import dao.RicettaDAO;
import dao.UsaDAO;

import service.GestioneCorsiCucina;
import service.GestioneRicette;

import util.DBConnection;

import guihelper.StyleHelper;
import guihelper.ValidationHelper;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import model.Chef;
import exceptions.*;

public class LoginChefGUI extends Application {
	private static ChefController chefController;
	private static GestioneCorsiCucina corsiService;

	private double xOffset = 0;
	private double yOffset = 0;
	private StackPane contentPane;
	private Label errorLabel;

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
		root.setPrefSize(600, 650);

		Region background = new Region();
		StyleHelper.applyBackgroundGradient(background);
		background.setPrefSize(600, 650);
		root.getChildren().add(background);

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
	}

	private VBox createLoginCard() {
		VBox card = new VBox(20);
		card.setAlignment(Pos.CENTER);
		card.setPadding(new Insets(40));
		card.setPrefSize(400, 480);
		card.setMaxSize(400, 480);
		card.setStyle("""
				    -fx-background-color: white;
				    -fx-background-radius: 25;
				    -fx-border-radius: 25;
				    -fx-border-color: #FF9966;
				    -fx-border-width: 2;
				    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4);
				""");

		Label titleLabel = new Label("🍽️ UninaFoodLab");
		titleLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 32));
		titleLabel.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

		Label subtitleLabel = new Label("Accedi al tuo account");
		subtitleLabel.setFont(Font.font("Roboto", FontWeight.NORMAL, 14));
		subtitleLabel.setTextFill(Color.web(StyleHelper.PRIMARY_LIGHT));

		errorLabel = new Label();
		errorLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
		errorLabel.setTextFill(Color.RED);
		errorLabel.setVisible(false);
		errorLabel.setWrapText(true);
		errorLabel.setAlignment(Pos.CENTER);
		errorLabel.setMaxWidth(320);
		errorLabel.setStyle("""
				    -fx-background-color: #ffe6e6;
				    -fx-padding: 10;
				    -fx-background-radius: 10;
				    -fx-border-color: red;
				    -fx-border-width: 1;
				    -fx-border-radius: 10;
				""");

		VBox formContainer = new VBox(15);
		formContainer.setAlignment(Pos.CENTER);

		VBox usernameContainer = createStylishTextField("Username", false);
		TextField usernameField = (TextField) ((StackPane) usernameContainer.getChildren().get(0)).getChildren().get(1);

		VBox passwordContainer = createStylishTextField("Password", true);
		PasswordField passwordField = (PasswordField) ((StackPane) passwordContainer.getChildren().get(0)).getChildren()
				.get(1);

		ValidationHelper.addAutoResetListener(usernameField, usernameContainer, errorLabel);
		ValidationHelper.addAutoResetListener(passwordField, passwordContainer, errorLabel);

		passwordField.setOnAction(e -> handleLogin(usernameField, passwordField, usernameContainer, passwordContainer));

		Button loginButton = StyleHelper.createSuccessButton("ACCEDI");
		loginButton.setPrefSize(140, 45);
		Button registerButton = StyleHelper.createCyanButton("REGISTRATI");
		registerButton.setPrefSize(140, 45);

		setupButtonEvents(loginButton, registerButton, usernameField, passwordField, usernameContainer,
				passwordContainer);

		formContainer.getChildren().addAll(usernameContainer, passwordContainer);

		HBox buttonContainer = new HBox(15);
		buttonContainer.setAlignment(Pos.CENTER);
		buttonContainer.getChildren().addAll(registerButton, loginButton);

		VBox headerContainer = new VBox(10);
		headerContainer.setAlignment(Pos.CENTER);
		headerContainer.getChildren().addAll(titleLabel, subtitleLabel);

		card.getChildren().addAll(headerContainer, errorLabel, formContainer, buttonContainer);

		return card;
	}

	private VBox createStylishTextField(String placeholder, boolean isPassword) {
		VBox container = new VBox(3);
		StackPane fieldContainer = new StackPane();
		fieldContainer.setPrefHeight(45);

		Region background = new Region();
		background.setStyle("""
				    -fx-background-color: white;
				    -fx-background-radius: 15;
				    -fx-border-radius: 15;
				    -fx-border-color: #FF9966;
				    -fx-border-width: 1.5;
				""");

		TextInputControl inputField = isPassword ? new PasswordField() : new TextField();
		inputField.setPromptText(placeholder);
		inputField.setStyle("""
				    -fx-background-color: transparent;
				    -fx-text-fill: black;
				    -fx-prompt-text-fill: gray;
				    -fx-font-size: 14px;
				    -fx-padding: 0 15 0 15;
				""");
		inputField.setPrefWidth(300);

		fieldContainer.getChildren().addAll(background, inputField);
		container.getChildren().add(fieldContainer);
		return container;
	}

	private HBox createWindowButtons(Stage stage) {
		Button closeButton = new Button("✕");
		Button minimizeButton = new Button("−");
		Button maximizeButton = new Button("□");

		Button[] buttons = { minimizeButton, maximizeButton, closeButton };
		for (Button btn : buttons) {
			btn.setPrefSize(35, 35);
			btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
			btn.setTextFill(Color.WHITE);
			btn.setFocusTraversable(false);
		}

		closeButton.setStyle(
				"-fx-background-color: " + StyleHelper.ERROR_RED + "; -fx-background-radius: 20; -fx-cursor: hand;");
		closeButton.setOnAction(e -> stage.close());

		minimizeButton
				.setStyle("-fx-background-color: rgba(255,140,0,0.5); -fx-background-radius: 20; -fx-cursor: hand;");
		minimizeButton.setOnAction(e -> stage.setIconified(true));

		maximizeButton
				.setStyle("-fx-background-color: rgba(255,140,0,0.5); -fx-background-radius: 20; -fx-cursor: hand;");
		maximizeButton.setOnAction(e -> stage.setMaximized(!stage.isMaximized()));

		HBox box = new HBox(5, minimizeButton, maximizeButton, closeButton);
		box.setAlignment(Pos.TOP_RIGHT);
		box.setPickOnBounds(false);
		return box;
	}

	private void setupButtonEvents(Button loginButton, Button registerButton, TextField usernameField,
			PasswordField passwordField, VBox usernameContainer, VBox passwordContainer) {
		loginButton.setOnAction(e -> handleLogin(usernameField, passwordField, usernameContainer, passwordContainer));
		registerButton.setOnAction(e -> handleRegister());
	}

	private void handleLogin(TextField usernameField, PasswordField passwordField, VBox usernameContainer,
			VBox passwordContainer) {
		errorLabel.setVisible(false);

		if (!ValidationHelper.validateNotEmpty(usernameField, usernameContainer, errorLabel, "il tuo username")) {
			return;
		}
		if (!ValidationHelper.validateNotEmpty(passwordField, passwordContainer, errorLabel, "la tua password")) {
			return;
		}

		try {
			Chef chef = chefController.login(usernameField.getText().trim(), passwordField.getText());
			errorLabel.setVisible(false);
			aprireMenuChef(chef);
		} catch (ValidationException ex) {
			String messaggio = ex.getMessage();

			if (messaggio.equals(ErrorMessages.USERNAME_NON_TROVATO)) {
				ValidationHelper.showError(usernameField, usernameContainer, errorLabel, "❌ Username non esistente");
				usernameField.requestFocus();
			} else if (messaggio.equals(ErrorMessages.PASSWORD_ERRATA)) {
				ValidationHelper.showError(passwordField, passwordContainer, errorLabel, "❌ Password non corretta");
				passwordField.clear();
				passwordField.requestFocus();
			} else {
				ValidationHelper.showError(usernameField, usernameContainer, errorLabel, "❌ " + messaggio);
				ValidationHelper.showError(passwordField, passwordContainer, errorLabel, "❌ " + messaggio);
			}
		} catch (Exception ex) {
			ValidationHelper.showError(usernameField, null, errorLabel,
					"❌ Errore durante il login: " + ex.getMessage());
		}
	}

	private void aprireMenuChef(Chef chef) {
		try {
			ChefMenuGUI menu = new ChefMenuGUI();
			menu.setChefLoggato(chef);

			Stage menuStage = new Stage();
			menu.start(menuStage);

			((Stage) contentPane.getScene().getWindow()).close();
		} catch (Exception ex) {
			ValidationHelper.showError(null, null, errorLabel, "❌ Impossibile aprire il menu: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	private void handleRegister() {
		errorLabel.setVisible(false);
		contentPane.getChildren().clear();
		RegistrazioneChefGUI regPane = new RegistrazioneChefGUI(chefController, () -> {
			contentPane.getChildren().clear();
			contentPane.getChildren().add(createLoginCard());
		});
		contentPane.getChildren().add(regPane);
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
