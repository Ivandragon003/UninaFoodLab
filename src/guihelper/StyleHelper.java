package guihelper;


import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;

public final class StyleHelper {

	private StyleHelper() {
		throw new AssertionError("Classe di utilità non istanziabile");
	}

	// COLORI PRINCIPALI
	public static final String PRIMARY_ORANGE = "#FF6600";
	public static final String PRIMARY_LIGHT = "#FF8533";
	public static final String SUCCESS_GREEN = "#00854b";
	public static final String SECONDARY_BEIGE = "#bba79c";
	public static final String CYAN_REGISTER = "#00c3c7";
	public static final String ERROR_RED = "#e74c3c";
	public static final String INFO_BLUE = "#3498db";
	public static final String BORDER_LIGHT = "#e0e0e0";
	public static final String TEXT_BLACK = "#000000";
	public static final String BG_WHITE = "#FFFFFF";
	public static final String BG_ORANGE_LIGHT = "#FFCC99";
	public static final String BG_ORANGE_START = "#FF9966";
	public static final String NEUTRAL_GRAY = "#6c757d";

	// PULSANTI

	public static Button createPrimaryButton(String text) {
		return createStyledButton(text, PRIMARY_ORANGE);
	}

	public static Button createSuccessButton(String text) {
		return createStyledButton(text, SUCCESS_GREEN);
	}

	public static Button createSecondaryButton(String text) {
		return createStyledButton(text, SECONDARY_BEIGE);
	}

	public static Button createDangerButton(String text) {
		return createStyledButton(text, ERROR_RED);
	}

	public static Button createInfoButton(String text) {
		return createStyledButton(text, INFO_BLUE);
	}

	public static Button createCyanButton(String text) {
		return createStyledButton(text, CYAN_REGISTER);
	}

	public static Button createStyledButton(String text, String color) {
		Button button = new Button(text);
		button.setPrefSize(120, 40);
		button.setStyle("-fx-background-color: " + color + ";" + "-fx-text-fill: white;" + "-fx-font-weight: bold;"
				+ "-fx-background-radius: 20;" + "-fx-cursor: hand;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0.3, 0.0, 2.0);");

		button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: derive(" + color + ", -10%);"
				+ "-fx-text-fill: white;" + "-fx-font-weight: bold;" + "-fx-background-radius: 20;"
				+ "-fx-cursor: hand;" + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0.4, 0.0, 3.0);"
				+ "-fx-scale-x: 1.05;" + "-fx-scale-y: 1.05;"));

		button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + color + ";" + "-fx-text-fill: white;"
				+ "-fx-font-weight: bold;" + "-fx-background-radius: 20;" + "-fx-cursor: hand;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0.3, 0.0, 2.0);" + "-fx-scale-x: 1.0;"
				+ "-fx-scale-y: 1.0;"));

		return button;
	}

	// ETICHETTE E TEXTFIELD

	public static Label createTitleLabel(String text) {
		Label label = new Label(text);
		label.setFont(Font.font("Roboto", FontWeight.BOLD, 24));
		label.setTextFill(Color.web(PRIMARY_ORANGE));
		return label;
	}

	public static Label createLabel(String text) {
		Label label = new Label(text);
		label.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
		label.setTextFill(Color.web(TEXT_BLACK));
		return label;
	}

	public static TextField createTextField(String promptText) {
		TextField field = new TextField();
		field.setPromptText(promptText);
		field.setPrefHeight(35);
		field.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: " + BORDER_LIGHT
				+ "; -fx-border-width: 1;");
		return field;
	}

	public static TextArea createTextArea(String promptText) {
		TextArea area = new TextArea();
		area.setPromptText(promptText);
		area.setWrapText(true);
		area.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: " + BORDER_LIGHT
				+ "; -fx-border-width: 1;");
		return area;
	}

	public static VBox createSection() {
		VBox section = new VBox();
		section.setSpacing(15);
		section.setStyle("-fx-background-color: " + BG_WHITE + "; -fx-padding: 20; -fx-border-color: " + BORDER_LIGHT
				+ "; -fx-border-radius: 10; -fx-border-width: 1;");
		return section;
	}

	public static void applyBackgroundGradient(Region region) {
		LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
				new Stop(0, Color.web(BG_ORANGE_START)), new Stop(1, Color.web(BG_ORANGE_LIGHT)));
		region.setBackground(new Background(new BackgroundFill(gradient, null, null)));
	}

	// DIALOG

	public static void showSuccessDialog(String title, String message) {
		showCustomDialog(title, message, "✅", SUCCESS_GREEN, "#E8F5E8");
	}

	public static void showErrorDialog(String title, String message) {
		showCustomDialog(title, message, "❌", ERROR_RED, "#FFE8E8");
	}

	public static void showValidationDialog(String title, String message) {
		showCustomDialog(title, message, "⚠️", "#f39c12", "#FFF4E6");
	}

	public static void showInfoDialog(String title, String message) {
		showCustomDialog(title, message, "ℹ️", INFO_BLUE, "#E8F4FF");
	}

	private static void showCustomDialog(String title, String message, String icon, String accentColor,
			String bgColor) {
		Stage dialogStage = new Stage();
		dialogStage.initModality(Modality.APPLICATION_MODAL);
		dialogStage.initStyle(StageStyle.UNDECORATED);
		dialogStage.setResizable(false);

		VBox content = new VBox(15);
		content.setPadding(new Insets(25));
		content.setAlignment(Pos.CENTER);
		content.setStyle("-fx-background-color: " + bgColor + ";" + "-fx-background-radius: 15;" + "-fx-border-color: "
				+ accentColor + ";" + "-fx-border-width: 2;");

		Label iconLabel = new Label(icon);
		iconLabel.setStyle("-fx-font-size: 32px; -fx-text-fill: " + accentColor + ";");
		iconLabel.setAlignment(Pos.CENTER);

		Label titleLabel = new Label(title);
		titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
		titleLabel.setAlignment(Pos.CENTER);
		titleLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

		Label messageLabel = new Label(message);
		messageLabel.setWrapText(true);
		messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495E;");
		messageLabel.setAlignment(Pos.CENTER);
		messageLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
		messageLabel.setMaxWidth(javafx.stage.Screen.getPrimary().getVisualBounds().getWidth() * 0.5);

		Button okButton = createPrimaryButton("OK");
		okButton.setOnAction(e -> dialogStage.close());

		content.getChildren().addAll(iconLabel, titleLabel, messageLabel, okButton);

		Scene scene = new Scene(content);
		dialogStage.setScene(scene);

		// Imposta la larghezza massima del dialog come il 50% dello schermo
		dialogStage.setWidth(javafx.stage.Screen.getPrimary().getVisualBounds().getWidth() * 0.5);
		dialogStage.setHeight(content.prefHeight(-1) + 50); // altezza calcolata dinamicamente

		dialogStage.showAndWait();
	}

	public static Button createWindowButton(String text, Runnable action) {
		Button btn = new Button(text);
		btn.setPrefSize(30, 30);
		btn.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
		btn.setTextFill(Color.WHITE);
		btn.setStyle("-fx-background-color: rgba(255,140,0,0.7); -fx-background-radius: 15; -fx-cursor: hand;");
		btn.setFocusTraversable(false);
		btn.setOnAction(e -> action.run());
		return btn;
	}

	public static void showConfirmationDialog(String title, String message, Runnable onConfirm) {
		Stage dialogStage = new Stage();
		dialogStage.initModality(Modality.APPLICATION_MODAL);
		dialogStage.initStyle(StageStyle.UNDECORATED);
		dialogStage.setResizable(false);

		VBox content = new VBox(15);
		content.setPadding(new Insets(25));
		content.setAlignment(Pos.CENTER);
		content.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-border-color: " + PRIMARY_ORANGE
				+ "; -fx-border-width: 2;");

		Label titleLabel = new Label(title);
		titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
		titleLabel.setAlignment(Pos.CENTER);

		Label messageLabel = new Label(message);
		messageLabel.setWrapText(true);
		messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495E;");
		messageLabel.setAlignment(Pos.CENTER);
		messageLabel.setMaxWidth(javafx.stage.Screen.getPrimary().getVisualBounds().getWidth() * 0.5);

		HBox buttonsBox = new HBox(10);
		buttonsBox.setAlignment(Pos.CENTER);

		Button yesBtn = createPrimaryButton("Sì");
		yesBtn.setOnAction(e -> {
			dialogStage.close();
			if (onConfirm != null)
				onConfirm.run();
		});

		Button noBtn = createDangerButton("No");
		noBtn.setOnAction(e -> dialogStage.close());

		buttonsBox.getChildren().addAll(yesBtn, noBtn);

		content.getChildren().addAll(titleLabel, messageLabel, buttonsBox);

		Scene scene = new Scene(content);
		dialogStage.setScene(scene);
		dialogStage.setWidth(javafx.stage.Screen.getPrimary().getVisualBounds().getWidth() * 0.5);
		dialogStage.setHeight(content.prefHeight(-1) + 50);

		dialogStage.showAndWait();
	}

	public static DatePicker createDatePicker() {
		DatePicker dp = new DatePicker();
		dp.setPrefHeight(35);
		dp.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: " + BORDER_LIGHT
				+ "; -fx-border-width: 1;");
		return dp;
	}

	public static <T> ComboBox<T> createComboBox() {
		ComboBox<T> cb = new ComboBox<>();
		cb.setPrefHeight(35);
		cb.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: " + BORDER_LIGHT
				+ "; -fx-border-width: 1;");
		return cb;
	}

	public static void applyOrangeBackground(Region region) {
		LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
				new Stop(0, Color.web(BG_ORANGE_START)), new Stop(1, Color.web(BG_ORANGE_LIGHT)));
		region.setBackground(new Background(new BackgroundFill(gradient, null, null)));
	}
}
