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
import util.StyleHelper;
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
	private Label errorLabel; // AGGIUNTO: Label per errori inline

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
		root.setPrefSize(600, 650); // Aumentata altezza per errore

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
		card.setPrefSize(400, 480); // Aumentata altezza
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
		titleLabel.setTextFill(Color.web("#FF6600"));

		Label subtitleLabel = new Label("Accedi al tuo account");
		subtitleLabel.setFont(Font.font("Roboto", FontWeight.NORMAL, 14));
		subtitleLabel.setTextFill(Color.web("#FF8533"));

		// AGGIUNTO: Label errore inline rosso
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

		// AGGIUNTO: Nascondi errore quando l'utente digita
		usernameField.textProperty().addListener((obs, old, val) -> {
			if (!val.trim().isEmpty()) {
				nascondiErrore();
				resetFieldStyle(usernameContainer);
			}
		});

		passwordField.textProperty().addListener((obs, old, val) -> {
			if (!val.trim().isEmpty()) {
				nascondiErrore();
				resetFieldStyle(passwordContainer);
			}
		});

		// AGGIUNTO: Invio su password
		passwordField.setOnAction(e -> handleLogin(usernameField, passwordField, usernameContainer, passwordContainer));

		Button loginButton = createStylishButton("ACCEDI", "#FF6600", "#FF8533");
		Button registerButton = createStylishButton("REGISTRATI", "#FFCC99", "#FFD9B3");

		setupButtonEvents(loginButton, registerButton, usernameField, passwordField, usernameContainer,
				passwordContainer);

		formContainer.getChildren().addAll(usernameContainer, passwordContainer);

		HBox buttonContainer = new HBox(15);
		buttonContainer.setAlignment(Pos.CENTER);
		buttonContainer.getChildren().addAll(registerButton, loginButton);

		VBox headerContainer = new VBox(10);
		headerContainer.setAlignment(Pos.CENTER);
		headerContainer.getChildren().addAll(titleLabel, subtitleLabel);

		card.getChildren().addAll(headerContainer, errorLabel, formContainer, buttonContainer); // AGGIUNTO errorLabel

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

	private Button createStylishButton(String text, String baseColor, String hoverColor) {
		Button button = new Button(text);
		button.setPrefSize(140, 45);
		button.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
		button.setTextFill(Color.WHITE);
		button.setStyle("-fx-background-color: " + baseColor + "; -fx-background-radius: 20; -fx-cursor: hand;");

		button.setOnMouseEntered(e -> button
				.setStyle("-fx-background-color: " + hoverColor + "; -fx-background-radius: 20; -fx-cursor: hand;"));
		button.setOnMouseExited(e -> button
				.setStyle("-fx-background-color: " + baseColor + "; -fx-background-radius: 20; -fx-cursor: hand;"));

		return button;
	}

	private void setupButtonEvents(Button loginButton, Button registerButton, TextField usernameField,
			PasswordField passwordField, VBox usernameContainer, VBox passwordContainer) {
		loginButton.setOnAction(e -> handleLogin(usernameField, passwordField, usernameContainer, passwordContainer));
		registerButton.setOnAction(e -> handleRegister());
	}

	private void handleLogin(TextField usernameField, PasswordField passwordField, VBox usernameContainer,
			VBox passwordContainer) {
		nascondiErrore();

		String username = usernameField.getText().trim();
		String password = passwordField.getText();

		// Validazione campi vuoti
		if (username.isEmpty()) {
			mostraErrore("❌ Inserisci il tuo username");
			setFieldError(usernameContainer);
			usernameField.requestFocus();
			return;
		}

		if (password.isEmpty()) {
			mostraErrore("❌ Inserisci la tua password");
			setFieldError(passwordContainer);
			passwordField.requestFocus();
			return;
		}

		try {
			Chef chef = chefController.login(username, password);

			// ✅ Login riuscito - vai direttamente al menu SENZA popup
			nascondiErrore();
			aprireMenuChef(chef);

		} catch (ValidationException ex) {
			// Gestione errori specifici
			String messaggio = ex.getMessage();

			if (messaggio.equals(ErrorMessages.USERNAME_NON_TROVATO)) {
				mostraErrore("❌ Username non esistente");
				setFieldError(usernameContainer);
				usernameField.requestFocus();

			} else if (messaggio.equals(ErrorMessages.PASSWORD_ERRATA)) {
				mostraErrore("❌ Password non corretta");
				setFieldError(passwordContainer);
				passwordField.clear();
				passwordField.requestFocus();

			} else {
				// Errore generico
				mostraErrore("❌ " + messaggio);
				setFieldError(usernameContainer);
				setFieldError(passwordContainer);
			}

		} catch (Exception ex) {
			mostraErrore("❌ Errore durante il login: " + ex.getMessage());
		}
	}

	private void aprireMenuChef(Chef chef) {
		try {
			RicettaDAO ricettaDAO = new RicettaDAO();
			UsaDAO usaDAO = new UsaDAO();
			IngredienteDAO ingredienteDAO = new IngredienteDAO();
			GestioneRicette gestioneRicette = new GestioneRicette(ricettaDAO, usaDAO, ingredienteDAO);
			RicettaController ricettaController = new RicettaController(gestioneRicette);

			GestioneCorsoController gestioneCorsoController = new GestioneCorsoController(corsiService,
					chefController.getGestioneChef());
			gestioneCorsoController.setChefLoggato(chef);

			VisualizzaCorsiController corsiController = new VisualizzaCorsiController(corsiService, chef);

			ChefMenuGUI menu = new ChefMenuGUI();
			menu.setChefLoggato(chef);
			menu.setControllers(corsiController, gestioneCorsoController, ricettaController);

			Stage menuStage = new Stage();
			menu.start(menuStage);

			((Stage) contentPane.getScene().getWindow()).close();

		} catch (Exception ex) {
			mostraErrore("❌ Impossibile aprire il menu: " + ex.getMessage());
		}
	}

	private void handleRegister() {
		nascondiErrore();
		contentPane.getChildren().clear();
		RegistrazioneChefGUI regPane = new RegistrazioneChefGUI(chefController, () -> {
			contentPane.getChildren().clear();
			contentPane.getChildren().add(createLoginCard());
		});
		contentPane.getChildren().add(regPane);
	}

	// AGGIUNTO: Metodi per gestione errori inline
	private void mostraErrore(String messaggio) {
		errorLabel.setText(messaggio);
		errorLabel.setVisible(true);
	}

	private void nascondiErrore() {
		errorLabel.setVisible(false);
	}

	private void setFieldError(VBox container) {
		StackPane fieldContainer = (StackPane) container.getChildren().get(0);
		Region background = (Region) fieldContainer.getChildren().get(0);
		background.setStyle("""
				    -fx-background-color: white;
				    -fx-background-radius: 15;
				    -fx-border-radius: 15;
				    -fx-border-color: red;
				    -fx-border-width: 2;
				""");
	}

	private void resetFieldStyle(VBox container) {
		StackPane fieldContainer = (StackPane) container.getChildren().get(0);
		Region background = (Region) fieldContainer.getChildren().get(0);
		background.setStyle("""
				    -fx-background-color: white;
				    -fx-background-radius: 15;
				    -fx-border-radius: 15;
				    -fx-border-color: #FF9966;
				    -fx-border-width: 1.5;
				""");
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
