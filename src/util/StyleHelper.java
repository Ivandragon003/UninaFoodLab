package util;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import exceptions.ValidationException;

public class StyleHelper {
    
    public static final String PRIMARY_ORANGE = "#FF6600";
    public static final String PRIMARY_LIGHT = "#FF8533";
    public static final String PRIMARY_LIGHTEST = "#FF9966";
    public static final String SUCCESS_GREEN = "#00854b";
    public static final String SECONDARY_BEIGE = "#bba79c";
    public static final String CYAN_REGISTER = "#00c3c7";
    public static final String ERROR_RED = "#e74c3c";
    public static final String INFO_BLUE = "#3498db";
    public static final String WARNING_ORANGE = "#f39c12";
    public static final String NEUTRAL_GRAY = "#95a5a6";
    public static final String BG_WHITE = "#FFFFFF";
    public static final String BORDER_LIGHT = "#e0e0e0";
    public static final String TEXT_BLACK = "#000000";
    public static final String BG_ORANGE_LIGHT = "#FFCC99";
    public static final String BG_ORANGE_START = "#FF9966";

    public static void showSuccessDialog(String title, String message) {
        showCustomDialog(title, message, "✅", SUCCESS_GREEN, "#E8F5E8");
    }
    
    public static void showErrorDialog(String title, String message) {
        showCustomDialog(title, message, "❌", ERROR_RED, "#FFE8E8");
    }
    
    public static void showValidationDialog(String title, String message) {
        showCustomDialog(title, message, "⚠️", WARNING_ORANGE, "#FFF4E6");
    }
    
    public static void showInfoDialog(String title, String message) {
        showCustomDialog(title, message, "ℹ️", INFO_BLUE, "#E8F4FF");
    }
    
    private static void showCustomDialog(String title, String message, String icon, String accentColor, String bgColor) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.UNDECORATED);
        dialogStage.setResizable(false);
        
        StackPane root = new StackPane();
        root.setPrefSize(400, 200);
        root.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " + bgColor + ", white);" +
            "-fx-background-radius: 15;" +
            "-fx-border-color: " + accentColor + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 15;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.5, 0.0, 0.0);"
        );
        
        VBox content = new VBox(20);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.setPadding(new Insets(25));
        
        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle(
            "-fx-font-size: 32px;" +
            "-fx-text-fill: " + accentColor + ";"
        );
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #2C3E50;"
        );
        
        header.getChildren().addAll(iconLabel, titleLabel);
        
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(javafx.geometry.Pos.CENTER);
        messageLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #34495E;" +
            "-fx-text-alignment: center;"
        );
        
        Button okButton = createStyledButton("OK", accentColor);
        okButton.setOnAction(e -> dialogStage.close());
        
        Button closeButton = new Button("✕");
        closeButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + ERROR_RED + ";" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;"
        );
        closeButton.setOnAction(e -> dialogStage.close());
        
        StackPane.setAlignment(closeButton, javafx.geometry.Pos.TOP_RIGHT);
        StackPane.setMargin(closeButton, new Insets(10, 10, 0, 0));
        
        content.getChildren().addAll(header, messageLabel, okButton);
        root.getChildren().addAll(content, closeButton);
        
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }
    
    public static void handleValidation(ValidationException e) {
        showValidationDialog("Validazione", e.getMessage());
    }
    
    public static void handleError(Exception e) {
        showErrorDialog("Errore", e.getMessage());
        e.printStackTrace();
    }
    
    public static void handleSuccess(String message) {
        showSuccessDialog("Successo", message);
    }
    
    public static void setValidationStyle(TextField field, boolean valid) {
        if (valid) {
            field.setStyle(
                "-fx-border-color: " + SUCCESS_GREEN + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 5px;" +
                "-fx-background-color: #F8FFF8;"
            );
        } else {
            field.setStyle(
                "-fx-border-color: " + ERROR_RED + ";" +
                "-fx-border-width: 2px;" +
                "-fx-border-radius: 5px;" +
                "-fx-background-color: #FFF8F8;" +
                "-fx-effect: dropshadow(gaussian, " + ERROR_RED + ", 3, 0.3, 0.0, 0.0);"
            );
        }
    }
    
    public static void setNeutralStyle(TextField field) {
        field.setStyle(
            "-fx-border-color: " + BORDER_LIGHT + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 5px;" +
            "-fx-background-color: white;"
        );
    }

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
        field.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: " + BORDER_LIGHT + "; " +
                "-fx-border-radius: 8; -fx-border-width: 1;");
        return field;
    }

    public static Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setPrefSize(130, 45);
        button.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        button.setTextFill(Color.WHITE);
        button.setStyle("-fx-background-color: " + PRIMARY_ORANGE + "; " +
                "-fx-background-radius: 20; -fx-cursor: hand;");
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: " + PRIMARY_LIGHT + "; " +
                "-fx-background-radius: 20; -fx-cursor: hand;"));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: " + PRIMARY_ORANGE + "; " +
                "-fx-background-radius: 20; -fx-cursor: hand;"));
        return button;
    }

    public static Button createSuccessButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
        button.setTextFill(Color.WHITE);
        button.setStyle("-fx-background-color: " + SUCCESS_GREEN + "; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: derive(" + SUCCESS_GREEN + ", -10%); " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + SUCCESS_GREEN + "; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;"));
        return button;
    }

    public static Button createCyanButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
        button.setTextFill(Color.WHITE);
        button.setStyle("-fx-background-color: " + CYAN_REGISTER + "; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: derive(" + CYAN_REGISTER + ", -10%); " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + CYAN_REGISTER + "; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;"));
        return button;
    }

    public static Button createSecondaryButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
        button.setTextFill(Color.WHITE);
        button.setStyle("-fx-background-color: " + SECONDARY_BEIGE + "; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: derive(" + SECONDARY_BEIGE + ", -10%); " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + SECONDARY_BEIGE + "; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;"));
        return button;
    }

    public static Button createDangerButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
        button.setTextFill(Color.WHITE);
        button.setStyle("-fx-background-color: " + ERROR_RED + "; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: derive(" + ERROR_RED + ", -10%); " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + ERROR_RED + "; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;"));
        return button;
    }

    public static Button createInfoButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
        button.setTextFill(Color.WHITE);
        button.setStyle("-fx-background-color: " + INFO_BLUE + "; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: derive(" + INFO_BLUE + ", -10%); " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + INFO_BLUE + "; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;"));
        return button;
    }

    @SuppressWarnings("rawtypes")
    public static ComboBox createComboBox() {
        ComboBox combo = new ComboBox<>();
        combo.setPrefHeight(35);
        combo.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: " + BORDER_LIGHT + "; " +
                "-fx-border-radius: 8; -fx-border-width: 1;");
        return combo;
    }

    public static DatePicker createDatePicker() {
        DatePicker picker = new DatePicker();
        picker.setPrefHeight(35);
        picker.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: " + BORDER_LIGHT + "; " +
                "-fx-border-radius: 8; -fx-border-width: 1;");
        return picker;
    }

    public static void applyListViewStyle(ListView<?> listView) {
        listView.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: " + BORDER_LIGHT + "; " +
                "-fx-border-radius: 8; -fx-border-width: 1;");
    }

    public static VBox createSection() {
        VBox section = new VBox();
        section.setSpacing(15);
        section.setStyle("-fx-background-color: " + BG_WHITE + "; -fx-padding: 20; " +
                "-fx-background-radius: 10; -fx-border-color: " + BORDER_LIGHT + "; " +
                "-fx-border-radius: 10; -fx-border-width: 1;");
        return section;
    }

    public static void applyBackgroundGradient(Region region) {
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(BG_ORANGE_START)),
                new Stop(1, Color.web(BG_ORANGE_LIGHT)));
        region.setBackground(new Background(new BackgroundFill(gradient, null, null)));
    }

    public static void applyOrangeBackground(Region region) {
        region.setStyle("-fx-background-color: " + BG_ORANGE_LIGHT + ";");
    }

    public static TextArea createTextArea(String promptText) {
        TextArea textArea = new TextArea();
        textArea.setPromptText(promptText);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: " + BORDER_LIGHT + "; " +
                "-fx-border-radius: 8; -fx-border-width: 1; -fx-font-size: 14px;");
        return textArea;
    }
    
    public static Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefSize(120, 40);
        button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 20;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0.3, 0.0, 2.0);"
        );
        
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: derive(" + color + ", -10%);" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 20;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0.4, 0.0, 3.0);" +
            "-fx-scale-x: 1.05;" +
            "-fx-scale-y: 1.05;"
        ));
        
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 20;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0.3, 0.0, 2.0);" +
            "-fx-scale-x: 1.0;" +
            "-fx-scale-y: 1.0;"
        ));
        
        return button;
    }
    
    public static void showConfirmationDialog(String title, String message, Runnable onConfirm) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initStyle(StageStyle.UNDECORATED);
        ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);
        if (result == ButtonType.OK) {
            onConfirm.run();
        }
    }
}
