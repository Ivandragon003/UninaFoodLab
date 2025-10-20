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
import javafx.scene.effect.DropShadow;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public final class StyleHelper {

	private StyleHelper() {
		throw new AssertionError("Classe di utilità non istanziabile");
	}

	public static final String PRIMARY_ORANGE = "#FF6B35";
	public static final String PRIMARY_LIGHT = "#FF8C61";
	public static final String SUCCESS_GREEN = "#06D6A0";
	public static final String SECONDARY_BEIGE = "#F4A261";
	public static final String CYAN_REGISTER = "#118AB2";
	public static final String ERROR_RED = "#DC3545";
	public static final String INFO_BLUE = "#4ECDC4";
	public static final String BORDER_LIGHT = "#E8E8E8";
	public static final String TEXT_BLACK = "#2B2D42";
	public static final String TEXT_GRAY = "#8D99AE";
	public static final String BG_WHITE = "#FFFFFF";
	public static final String BG_LIGHT = "#F8F9FA";
	public static final String BG_ORANGE_LIGHT = "#FFE8D6";
	public static final String BG_ORANGE_START = "#FFA07A";
	public static final String NEUTRAL_GRAY = "#6C757D";
	public static final String ACCENT_PURPLE = "#9D4EDD";
	public static final String ACCENT_YELLOW = "#FFD60A";

	public static Button createPrimaryButton(String text) {
		return createStyledButton(text, PRIMARY_ORANGE, "#FFFFFF");
	}

	public static Button createSuccessButton(String text) {
		return createStyledButton(text, SUCCESS_GREEN, "#FFFFFF");
	}

	public static Button createSecondaryButton(String text) {
		return createStyledButton(text, SECONDARY_BEIGE, "#FFFFFF");
	}

	public static Button createDangerButton(String text) {
		return createStyledButton(text, ERROR_RED, "#FFFFFF");
	}

	public static Button createInfoButton(String text) {
		return createStyledButton(text, INFO_BLUE, "#FFFFFF");
	}

	public static Button createCyanButton(String text) {
		return createStyledButton(text, CYAN_REGISTER, "#FFFFFF");
	}

	public static Button createStyledButton(String text, String color, String textColor) {
		Button button = new Button(text);
		button.setPrefSize(140, 45);
		button.setStyle(
			"-fx-background-color: " + color + ";" +
			"-fx-text-fill: " + textColor + ";" +
			"-fx-font-weight: bold;" +
			"-fx-font-size: 14px;" +
			"-fx-background-radius: 25;" +
			"-fx-cursor: hand;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0.3, 1, 2);" // ✅ Ombra LEGGERA
		);

		// ✅ Hover SENZA animazione scale (solo cambio colore + ombra più forte)
		button.setOnMouseEntered(e -> button.setStyle(
			"-fx-background-color: derive(" + color + ", -12%);" +
			"-fx-text-fill: " + textColor + ";" +
			"-fx-font-weight: bold;" +
			"-fx-font-size: 14px;" +
			"-fx-background-radius: 25;" +
			"-fx-cursor: hand;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.22), 8, 0.4, 1.5, 3);" // ✅ Ombra leggermente più forte
		));

		button.setOnMouseExited(e -> button.setStyle(
			"-fx-background-color: " + color + ";" +
			"-fx-text-fill: " + textColor + ";" +
			"-fx-font-weight: bold;" +
			"-fx-font-size: 14px;" +
			"-fx-background-radius: 25;" +
			"-fx-cursor: hand;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0.3, 1, 2);"
		));

		return button;
	}

	public static void styleComboBox(ComboBox<?> comboBox) {
		comboBox.setPrefHeight(40);
		comboBox.setStyle(
			"-fx-background-color: white;" +
			"-fx-background-radius: 12;" +
			"-fx-border-color: " + BORDER_LIGHT + ";" +
			"-fx-border-width: 2;" +
			"-fx-border-radius: 12;" +
			"-fx-padding: 8 12;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 4, 0, 0, 1);"
		);

		comboBox.setOnMouseEntered(e -> comboBox.setStyle(
			"-fx-background-color: " + BG_LIGHT + ";" +
			"-fx-background-radius: 12;" +
			"-fx-border-color: " + PRIMARY_ORANGE + ";" +
			"-fx-border-width: 2;" +
			"-fx-border-radius: 12;" +
			"-fx-padding: 8 12;" +
			"-fx-effect: dropshadow(gaussian, rgba(255,107,53,0.2), 6, 0, 0, 2);"
		));

		comboBox.setOnMouseExited(e -> comboBox.setStyle(
			"-fx-background-color: white;" +
			"-fx-background-radius: 12;" +
			"-fx-border-color: " + BORDER_LIGHT + ";" +
			"-fx-border-width: 2;" +
			"-fx-border-radius: 12;" +
			"-fx-padding: 8 12;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 4, 0, 0, 1);"
		));
	}

	public static Label createTitleLabel(String text) {
		Label label = new Label(text);
		label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
		label.setTextFill(Color.web(PRIMARY_ORANGE));
		DropShadow shadow = new DropShadow();
		shadow.setColor(Color.rgb(255, 107, 53, 0.3));
		shadow.setRadius(8);
		label.setEffect(shadow);
		return label;
	}

	public static Label createSubtitleLabel(String text) {
		Label label = new Label(text);
		label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 20));
		label.setTextFill(Color.web(TEXT_BLACK));
		return label;
	}

	public static Label createLabel(String text) {
		Label label = new Label(text);
		label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
		label.setTextFill(Color.web(TEXT_BLACK));
		return label;
	}

	public static Label createCaptionLabel(String text) {
		Label label = new Label(text);
		label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
		label.setTextFill(Color.web(TEXT_GRAY));
		return label;
	}

	public static TextField createTextField(String promptText) {
		TextField field = new TextField();
		field.setPromptText(promptText);
		field.setPrefHeight(42);
		field.setStyle(
			"-fx-background-color: white;" +
			"-fx-background-radius: 12;" +
			"-fx-border-color: " + BORDER_LIGHT + ";" +
			"-fx-border-width: 2;" +
			"-fx-border-radius: 12;" +
			"-fx-padding: 10 15;" +
			"-fx-font-size: 14px;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);"
		);

		field.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
			if (isNowFocused) {
				field.setStyle(
					"-fx-background-color: white;" +
					"-fx-background-radius: 12;" +
					"-fx-border-color: " + PRIMARY_ORANGE + ";" +
					"-fx-border-width: 2;" +
					"-fx-border-radius: 12;" +
					"-fx-padding: 10 15;" +
					"-fx-font-size: 14px;" +
					"-fx-effect: dropshadow(gaussian, rgba(255,107,53,0.2), 6, 0, 0, 2);"
				);
			} else {
				field.setStyle(
					"-fx-background-color: white;" +
					"-fx-background-radius: 12;" +
					"-fx-border-color: " + BORDER_LIGHT + ";" +
					"-fx-border-width: 2;" +
					"-fx-border-radius: 12;" +
					"-fx-padding: 10 15;" +
					"-fx-font-size: 14px;" +
					"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);"
				);
			}
		});

		return field;
	}

	public static TextArea createTextArea(String promptText) {
		TextArea area = new TextArea();
		area.setPromptText(promptText);
		area.setWrapText(true);
		area.setStyle(
			"-fx-background-color: white;" +
			"-fx-background-radius: 12;" +
			"-fx-border-color: " + BORDER_LIGHT + ";" +
			"-fx-border-width: 2;" +
			"-fx-border-radius: 12;" +
			"-fx-padding: 12;" +
			"-fx-font-size: 14px;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);"
		);

		area.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
			if (isNowFocused) {
				area.setStyle(
					"-fx-background-color: white;" +
					"-fx-background-radius: 12;" +
					"-fx-border-color: " + PRIMARY_ORANGE + ";" +
					"-fx-border-width: 2;" +
					"-fx-border-radius: 12;" +
					"-fx-padding: 12;" +
					"-fx-font-size: 14px;" +
					"-fx-effect: dropshadow(gaussian, rgba(255,107,53,0.2), 6, 0, 0, 2);"
				);
			} else {
				area.setStyle(
					"-fx-background-color: white;" +
					"-fx-background-radius: 12;" +
					"-fx-border-color: " + BORDER_LIGHT + ";" +
					"-fx-border-width: 2;" +
					"-fx-border-radius: 12;" +
					"-fx-padding: 12;" +
					"-fx-font-size: 14px;" +
					"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);"
				);
			}
		});

		return area;
	}

	// ✅ Section con ombra LEGGERA
	public static VBox createSection() {
		VBox section = new VBox();
		section.setSpacing(18);
		section.setPadding(new Insets(24));
		section.setStyle(
			"-fx-background-color: " + BG_WHITE + ";" +
			"-fx-background-radius: 16;" +
			"-fx-border-color: " + BORDER_LIGHT + ";" +
			"-fx-border-radius: 16;" +
			"-fx-border-width: 1;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);"
		);
		return section;
	}

	// ✅ Card con ombra LEGGERA
	public static VBox createCard() {
		VBox card = new VBox();
		card.setSpacing(12);
		card.setPadding(new Insets(20));
		card.setStyle(
			"-fx-background-color: " + BG_WHITE + ";" +
			"-fx-background-radius: 20;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);"
		);
		return card;
	}

	public static void applyBackgroundGradient(Region region) {
		LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
				new Stop(0, Color.web(BG_ORANGE_START)), new Stop(0.5, Color.web(PRIMARY_LIGHT)),
				new Stop(1, Color.web(BG_ORANGE_LIGHT)));
		region.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));
	}

	public static void showSuccessDialog(String title, String message) {
		showCustomDialog(title, message, "✅", SUCCESS_GREEN, "#E8FFF5");
	}

	public static void showErrorDialog(String title, String message) {
		showCustomDialog(title, message, "❌", ERROR_RED, "#FFE8EE");
	}

	public static void showValidationDialog(String title, String message) {
		showCustomDialog(title, message, "⚠️", ACCENT_YELLOW, "#FFF9E6");
	}

	public static void showInfoDialog(String title, String message) {
		showCustomDialog(title, message, "ℹ️", INFO_BLUE, "#E6F9F8");
	}

	private static void showCustomDialog(String title, String message, String icon, String accentColor, String bgColor) {
		Stage dialogStage = new Stage();
		dialogStage.initModality(Modality.APPLICATION_MODAL);
		dialogStage.initStyle(StageStyle.TRANSPARENT);
		dialogStage.setResizable(false);

		VBox content = new VBox(20);
		content.setPadding(new Insets(30));
		content.setAlignment(Pos.CENTER);
		content.setStyle(
			"-fx-background-color: " + bgColor + ";" +
			"-fx-background-radius: 20;" +
			"-fx-border-color: " + accentColor + ";" +
			"-fx-border-width: 3;" +
			"-fx-border-radius: 20;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.20), 15, 0, 0, 6);"
		);

		Label iconLabel = new Label(icon);
		iconLabel.setStyle("-fx-font-size: 48px;");
		iconLabel.setAlignment(Pos.CENTER);

		Label titleLabel = new Label(title);
		titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
		titleLabel.setTextFill(Color.web(TEXT_BLACK));
		titleLabel.setAlignment(Pos.CENTER);

		Label messageLabel = new Label(message);
		messageLabel.setWrapText(true);
		messageLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
		messageLabel.setTextFill(Color.web(TEXT_GRAY));
		messageLabel.setAlignment(Pos.CENTER);
		messageLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
		messageLabel.setMaxWidth(javafx.stage.Screen.getPrimary().getVisualBounds().getWidth() * 0.4);

		Button okButton = createStyledButton("OK", accentColor, "#FFFFFF");
		okButton.setPrefWidth(150);
		okButton.setOnAction(e -> {
			FadeTransition fade = new FadeTransition(Duration.millis(200), content);
			fade.setFromValue(1.0);
			fade.setToValue(0.0);
			fade.setOnFinished(ev -> dialogStage.close());
			fade.play();
		});

		content.getChildren().addAll(iconLabel, titleLabel, messageLabel, okButton);

		StackPane root = new StackPane(content);
		root.setStyle("-fx-background-color: transparent;");
		Scene scene = new Scene(root);
		scene.setFill(Color.TRANSPARENT);
		dialogStage.setScene(scene);

		content.setOpacity(0);
		FadeTransition fadeIn = new FadeTransition(Duration.millis(300), content);
		fadeIn.setFromValue(0.0);
		fadeIn.setToValue(1.0);
		fadeIn.play();

		dialogStage.showAndWait();
	}

	public static void showConfirmationDialog(String title, String message, Runnable onConfirm) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("");
		alert.setHeaderText(null);
		alert.setContentText(null);

		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStyleClass().remove("alert");

		dialogPane.setStyle(
			"-fx-background-color: " + BG_WHITE + ";" +
			"-fx-border-color: " + PRIMARY_ORANGE + ";" +
			"-fx-border-width: 3px;" +
			"-fx-border-radius: 12px;" +
			"-fx-background-radius: 12px;" +
			"-fx-padding: 30px;"
		);

		dialogPane.setMinWidth(500);
		dialogPane.setMinHeight(280);

		VBox content = new VBox(18);
		content.setAlignment(Pos.CENTER);

		Label iconLabel = new Label("❓");
		iconLabel.setStyle("-fx-font-size: 50px;");

		Label messageLabel = new Label(message);
		messageLabel.setWrapText(true);
		messageLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
		messageLabel.setTextFill(Color.web(TEXT_GRAY));
		messageLabel.setAlignment(Pos.CENTER);
		messageLabel.setMaxWidth(450);

		content.getChildren().addAll(iconLabel, messageLabel);
		dialogPane.setContent(content);

		Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
		okButton.setText("Sì");
		okButton.setStyle(
			"-fx-background-color: " + SUCCESS_GREEN + ";" +
			"-fx-text-fill: white;" +
			"-fx-font-size: 14px;" +
			"-fx-font-weight: bold;" +
			"-fx-padding: 12 30 12 30;" +
			"-fx-background-radius: 10px;" +
			"-fx-cursor: hand;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 5, 0.3, 1, 2);"
		);

		Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
		cancelButton.setText("No");
		cancelButton.setStyle(
			"-fx-background-color: " + ERROR_RED + ";" +
			"-fx-text-fill: white;" +
			"-fx-font-size: 14px;" +
			"-fx-font-weight: bold;" +
			"-fx-padding: 12 30 12 30;" +
			"-fx-background-radius: 10px;" +
			"-fx-cursor: hand;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 5, 0.3, 1, 2);"
		);

		alert.showAndWait().ifPresent(response -> {
			if (response == ButtonType.OK && onConfirm != null) {
				onConfirm.run();
			}
		});
	}
	
	public static void showCustomConfirmationDialog(String title, String message, Runnable onConfirm) {
    Stage dialogStage = new Stage();
    dialogStage.initModality(Modality.APPLICATION_MODAL);
    dialogStage.initStyle(StageStyle.TRANSPARENT);
    dialogStage.setResizable(false);

    VBox content = new VBox(20);
    content.setPadding(new Insets(30));
    content.setAlignment(Pos.CENTER);
    content.setStyle(
        "-fx-background-color: " + BG_WHITE + ";" +
        "-fx-background-radius: 20;" +
        "-fx-border-color: " + ERROR_RED + ";" +
        "-fx-border-width: 3;" +
        "-fx-border-radius: 20;" +
        "-fx-effect: dropshadow(gaussian, rgba(220,53,69,0.3), 15, 0, 0, 6);"
    );

    Label iconLabel = new Label("⚠️");
    iconLabel.setStyle("-fx-font-size: 48px;");
    iconLabel.setAlignment(Pos.CENTER);

    Label titleLabel = new Label(title);
    titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
    titleLabel.setTextFill(Color.web(ERROR_RED));
    titleLabel.setAlignment(Pos.CENTER);

    Label messageLabel = new Label(message);
    messageLabel.setWrapText(true);
    messageLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
    messageLabel.setTextFill(Color.web(TEXT_GRAY));
    messageLabel.setAlignment(Pos.CENTER);
    messageLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
    messageLabel.setMaxWidth(javafx.stage.Screen.getPrimary().getVisualBounds().getWidth() * 0.4);

    HBox buttonsBox = new HBox(15);
    buttonsBox.setAlignment(Pos.CENTER);

    Button confirmButton = createStyledButton("Sì", SUCCESS_GREEN, "#FFFFFF");
    confirmButton.setPrefWidth(150);
    confirmButton.setOnAction(e -> {
        dialogStage.close();
        // ✅ ESEGUI IL CALLBACK DOPO CHE LA DIALOG È COMPLETAMENTE CHIUSA
        if (onConfirm != null) {
            javafx.application.Platform.runLater(() -> {
                try {
                    onConfirm.run();
                } catch (Exception ex) {
                    System.err.println("Errore nel callback di conferma: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        }
    });

    Button cancelButton = createStyledButton("No", ERROR_RED, "#FFFFFF");
    cancelButton.setPrefWidth(150);
    cancelButton.setOnAction(e -> dialogStage.close());

    buttonsBox.getChildren().addAll(confirmButton, cancelButton);

    content.getChildren().addAll(iconLabel, titleLabel, messageLabel, buttonsBox);

    StackPane root = new StackPane(content);
    root.setStyle("-fx-background-color: transparent;");
    Scene scene = new Scene(root);
    scene.setFill(Color.TRANSPARENT);
    dialogStage.setScene(scene);

    content.setOpacity(0);
    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), content);
    fadeIn.setFromValue(0.0);
    fadeIn.setToValue(1.0);
    fadeIn.play();

    dialogStage.showAndWait();
}




	public static void showUnsavedChangesDialog(String title, String message, Runnable onSaveAndClose,
			Runnable onCloseWithoutSaving) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(null);

		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStyleClass().remove("alert");

		dialogPane.setStyle(
			"-fx-background-color: " + BG_WHITE + ";" +
			"-fx-border-color: #FFA500;" +
			"-fx-border-width: 3px;" +
			"-fx-border-radius: 12px;" +
			"-fx-background-radius: 12px;" +
			"-fx-padding: 30px;"
		);

		dialogPane.setMinWidth(550);
		dialogPane.setMinHeight(300);

		VBox content = new VBox(18);
		content.setAlignment(Pos.CENTER);

		Label iconLabel = new Label("⚠️");
		iconLabel.setStyle("-fx-font-size: 50px;");

		Label titleLabel = new Label(title);
		titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
		titleLabel.setTextFill(Color.web(TEXT_BLACK));
		titleLabel.setAlignment(Pos.CENTER);
		titleLabel.setMaxWidth(500);
		titleLabel.setWrapText(true);

		Label messageLabel = new Label(message);
		messageLabel.setWrapText(true);
		messageLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
		messageLabel.setTextFill(Color.web(TEXT_GRAY));
		messageLabel.setAlignment(Pos.CENTER);
		messageLabel.setMaxWidth(500);

		content.getChildren().addAll(iconLabel, titleLabel, messageLabel);
		dialogPane.setContent(content);

		ButtonType saveAndCloseBtn = new ButtonType("💾 Salva e Chiudi", ButtonBar.ButtonData.OK_DONE);
		ButtonType closeWithoutSavingBtn = new ButtonType("🚪 Chiudi Senza Salvare", ButtonBar.ButtonData.NO);
		ButtonType cancelBtn = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);

		dialogPane.getButtonTypes().setAll(saveAndCloseBtn, closeWithoutSavingBtn, cancelBtn);

		Button saveButton = (Button) dialogPane.lookupButton(saveAndCloseBtn);
		saveButton.setStyle(
			"-fx-background-color: " + SUCCESS_GREEN + ";" +
			"-fx-text-fill: white;" +
			"-fx-font-size: 14px;" +
			"-fx-font-weight: bold;" +
			"-fx-padding: 12 20 12 20;" +
			"-fx-background-radius: 10px;" +
			"-fx-cursor: hand;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 5, 0.3, 1, 2);"
		);

		Button closeButton = (Button) dialogPane.lookupButton(closeWithoutSavingBtn);
		closeButton.setStyle(
			"-fx-background-color: " + ERROR_RED + ";" +
			"-fx-text-fill: white;" +
			"-fx-font-size: 14px;" +
			"-fx-font-weight: bold;" +
			"-fx-padding: 12 20 12 20;" +
			"-fx-background-radius: 10px;" +
			"-fx-cursor: hand;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 5, 0.3, 1, 2);"
		);

		Button cancelButton = (Button) dialogPane.lookupButton(cancelBtn);
		cancelButton.setStyle(
			"-fx-background-color: #6C757D;" +
			"-fx-text-fill: white;" +
			"-fx-font-size: 14px;" +
			"-fx-font-weight: bold;" +
			"-fx-padding: 12 20 12 20;" +
			"-fx-background-radius: 10px;" +
			"-fx-cursor: hand;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 5, 0.3, 1, 2);"
		);

		alert.showAndWait().ifPresent(response -> {
			if (response == saveAndCloseBtn && onSaveAndClose != null) {
				onSaveAndClose.run();
			} else if (response == closeWithoutSavingBtn && onCloseWithoutSaving != null) {
				onCloseWithoutSaving.run();
			}
		});
	}

	public static DatePicker createDatePicker() {
		DatePicker dp = new DatePicker();
		dp.setPrefHeight(42);
		dp.setStyle(
			"-fx-background-color: white;" +
			"-fx-background-radius: 12;" +
			"-fx-border-color: " + BORDER_LIGHT + ";" +
			"-fx-border-width: 2;" +
			"-fx-border-radius: 12;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);"
		);
		return dp;
	}

	public static <T> ComboBox<T> createComboBox() {
		ComboBox<T> cb = new ComboBox<>();
		styleComboBox(cb);
		return cb;
	}

	public static void applyOrangeBackground(Region region) {
		LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
				new Stop(0, Color.web(PRIMARY_ORANGE)), new Stop(1, Color.web(PRIMARY_LIGHT)));
		region.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));
	}

	// ✅ Window buttons con ombra leggera
	public static Button createWindowButton(String text, Runnable action, String bgColor) {
		Button btn = new Button(text);
		btn.setPrefSize(35, 35);
		btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
		btn.setTextFill(Color.WHITE);
		btn.setStyle(
			"-fx-background-color: " + bgColor + ";" +
			"-fx-background-radius: 18;" +
			"-fx-cursor: hand;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 4, 0.3, 0.5, 1.5);"
		);
		btn.setFocusTraversable(false);

		btn.setOnMouseEntered(e -> btn.setStyle(
			"-fx-background-color: derive(" + bgColor + ", -15%);" +
			"-fx-background-radius: 18;" +
			"-fx-cursor: hand;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 6, 0.4, 1, 2.5);"
		));
		
		btn.setOnMouseExited(e -> btn.setStyle(
			"-fx-background-color: " + bgColor + ";" +
			"-fx-background-radius: 18;" +
			"-fx-cursor: hand;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 4, 0.3, 0.5, 1.5);"
		));

		btn.setOnAction(e -> action.run());
		return btn;
	}

	public static Button createStyledButtonWithWhiteBorder(String text, String color) {
		Button button = new Button(text);
		button.setPrefSize(140, 45);
		button.setStyle(
			"-fx-background-color: " + color + ";" +
			"-fx-text-fill: #FFFFFF;" +
			"-fx-font-weight: bold;" +
			"-fx-font-size: 14px;" +
			"-fx-background-radius: 25;" +
			"-fx-cursor: hand;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0.3, 1, 2);"
		);

		button.setOnMouseEntered(e -> button.setStyle(
			"-fx-background-color: derive(" + color + ", -12%);" +
			"-fx-text-fill: #FFFFFF;" +
			"-fx-font-weight: bold;" +
			"-fx-font-size: 14px;" +
			"-fx-background-radius: 25;" +
			"-fx-cursor: hand;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.22), 8, 0.4, 1.5, 3);"
		));

		button.setOnMouseExited(e -> button.setStyle(
			"-fx-background-color: " + color + ";" +
			"-fx-text-fill: #FFFFFF;" +
			"-fx-font-weight: bold;" +
			"-fx-font-size: 14px;" +
			"-fx-background-radius: 25;" +
			"-fx-cursor: hand;" +
			"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0.3, 1, 2);"
		));

		return button;
	}
    
    public static void styleMenuButton(MenuButton menuBtn) {
        menuBtn.setStyle(
            "-fx-background-color: " + PRIMARY_ORANGE + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 20;" +
            "-fx-cursor: hand;" +
            "-fx-font-weight: bold;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 5, 0.3, 1, 2);"
        );
        
        menuBtn.setOnMouseEntered(e -> menuBtn.setStyle(
            "-fx-background-color: derive(" + PRIMARY_ORANGE + ", -12%);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 20;" +
            "-fx-cursor: hand;" +
            "-fx-font-weight: bold;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 6, 0.4, 1, 2.5);"
        ));
        
        menuBtn.setOnMouseExited(e -> menuBtn.setStyle(
            "-fx-background-color: " + PRIMARY_ORANGE + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 20;" +
            "-fx-cursor: hand;" +
            "-fx-font-weight: bold;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 5, 0.3, 1, 2);"
        ));
    }

}
