package Gui;

import controller.ChefController;
import exceptions.DataAccessException;
import exceptions.ValidationException;
import guihelper.StyleHelper;
import guihelper.ValidationHelper;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Chef;
import util.DBConnection;

public class LoginChefGUI extends Application {

	private static ChefController chefController;

	private double xOffset = 0;
	private double yOffset = 0;
	private StackPane contentPane;
	private Label errorLabel;

	public static void setController(ChefController controller) {
		chefController = controller;
	}

	@Override
	public void start(Stage primaryStage) {
		if (chefController == null) {
			throw new IllegalStateException("Controller non inizializzato");
		}

		if (!primaryStage.isShowing()) {
			primaryStage.initStyle(StageStyle.UNDECORATED);
		}
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

		Scene scene = new Scene(root, 600, 650);
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

		VBox formContainer = new VBox(15);
		formContainer.setAlignment(Pos.CENTER);

		TextField usernameField = (TextField) createStyledField("Username", false);
		PasswordField passwordField = (PasswordField) createStyledField("Password", true);

		setupFieldBehavior(usernameField);
		setupFieldBehavior(passwordField);

		passwordField.setOnAction(e -> handleLogin(usernameField, passwordField));

		Button loginButton = StyleHelper.createSuccessButton("ACCEDI");
		loginButton.setPrefSize(140, 45);
		Button registerButton = StyleHelper.createCyanButton("REGISTRATI");
		registerButton.setPrefSize(140, 45);

		loginButton.setOnAction(e -> handleLogin(usernameField, passwordField));
		registerButton.setOnAction(e -> handleRegister());

		formContainer.getChildren().addAll(usernameField, passwordField);

		HBox buttonContainer = new HBox(15);
		buttonContainer.setAlignment(Pos.CENTER);
		buttonContainer.getChildren().addAll(registerButton, loginButton);

		VBox headerContainer = new VBox(10);
		headerContainer.setAlignment(Pos.CENTER);
		headerContainer.getChildren().addAll(titleLabel, subtitleLabel);

		card.getChildren().addAll(headerContainer, errorLabel, formContainer, buttonContainer);

		return card;
	}

	private TextInputControl createStyledField(String placeholder, boolean isPassword) {
		TextInputControl field = isPassword ? new PasswordField() : new TextField();
		field.setPromptText(placeholder);
		field.setPrefHeight(42);

		applyNormalStyle(field);

		return field;
	}

	
	private void setupFieldBehavior(TextInputControl field) {
		final boolean[] hasError = { false };

		field.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
			if (hasError[0]) {
				return;
			}

			if (isNowFocused) {
				applyFocusStyle(field);
			} else {
				applyNormalStyle(field);
			}
		});

		field.textProperty().addListener((obs, oldV, newV) -> {
			if (hasError[0] && newV != null && !newV.trim().isEmpty()) {
				hasError[0] = false;
				errorLabel.setVisible(false);

				if (field.isFocused()) {
					applyFocusStyle(field);
				} else {
					applyNormalStyle(field);
				}
			}
		});

		field.styleProperty().addListener((obs, oldStyle, newStyle) -> {
			if (newStyle != null && newStyle.contains(StyleHelper.ERROR_RED)) {
				hasError[0] = true;
			}
		});
	}

	
	private void applyNormalStyle(TextInputControl field) {
		field.setStyle("-fx-background-color: white;" + "-fx-background-radius: 12;" + "-fx-border-color: "
				+ StyleHelper.PRIMARY_ORANGE + ";" + "-fx-border-width: 1.5;" + "-fx-border-radius: 12;"
				+ "-fx-padding: 10 15;" + "-fx-font-size: 14px;" + "-fx-text-fill: " + StyleHelper.TEXT_BLACK + ";"
				+ "-fx-prompt-text-fill: gray;" + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);");
	}

	
	private void applyFocusStyle(TextInputControl field) {
		field.setStyle("-fx-background-color: white;" + "-fx-background-radius: 12;" + "-fx-border-color: "
				+ StyleHelper.PRIMARY_ORANGE + ";" + "-fx-border-width: 2;" + "-fx-border-radius: 12;"
				+ "-fx-padding: 10 15;" + "-fx-font-size: 14px;" + "-fx-text-fill: " + StyleHelper.TEXT_BLACK + ";"
				+ "-fx-prompt-text-fill: gray;"
				+ "-fx-effect: dropshadow(gaussian, rgba(255,107,53,0.2), 6, 0, 0, 2);");
	}

	private HBox createWindowButtons(Stage stage) {
		HBox box = new HBox(5, StyleHelper.createWindowButtonByType("minimize", () -> stage.setIconified(true)),
				StyleHelper.createWindowButtonByType("maximize", () -> stage.setMaximized(!stage.isMaximized())),
				StyleHelper.createWindowButtonByType("close", stage::close));
		box.setAlignment(Pos.TOP_RIGHT);
		box.setPickOnBounds(false);
		return box;
	}

	private void handleLogin(TextField usernameField, PasswordField passwordField) {
    errorLabel.setVisible(false);

    StyleHelper.applyNormalState(usernameField);
    StyleHelper.applyNormalState(passwordField);

    boolean usernameOk = ValidationHelper.validateNotEmpty(usernameField, errorLabel, "il tuo username");
    boolean passwordOk = ValidationHelper.validateNotEmpty(passwordField, errorLabel, "la tua password");

    if (!usernameOk) {
        StyleHelper.applyErrorState(usernameField);
    }
    if (!passwordOk) {
        StyleHelper.applyErrorState(passwordField);
    }

    if (!usernameOk || !passwordOk) return;

    try {
        Chef chef = chefController.login(usernameField.getText().trim(), passwordField.getText());
        aprireMenuChef(chef);
    } catch (ValidationException | DataAccessException ex) {
        errorLabel.setText("❌ " + ex.getMessage());
        errorLabel.setVisible(true);

        String msg = ex.getMessage().toLowerCase();
        if (msg.contains("username")) {
            StyleHelper.applyErrorState(usernameField);
        } else if (msg.contains("password")) {
            StyleHelper.applyErrorState(passwordField);
        } else {
            StyleHelper.applyErrorState(usernameField);
            StyleHelper.applyErrorState(passwordField);
        }
    } catch (Exception ex) {
        errorLabel.setText("❌ Errore durante il login: " + ex.getMessage());
        errorLabel.setVisible(true);
        StyleHelper.applyErrorState(usernameField);
        StyleHelper.applyErrorState(passwordField);
        ex.printStackTrace();
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
			StyleHelper.showErrorDialog("Errore", "Impossibile aprire il menu: " + ex.getMessage());
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
		root.setOnMousePressed(e -> {
			xOffset = e.getSceneX();
			yOffset = e.getSceneY();
		});
		root.setOnMouseDragged(e -> {
			stage.setX(e.getScreenX() - xOffset);
			stage.setY(e.getScreenY() - yOffset);
		});
	}
}