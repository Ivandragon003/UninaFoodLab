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

/**
 * StyleHelper CORRETTO - Metodi generici per evitare type mismatch
 */
public class StyleHelper {

    public static final String PRIMARY_ORANGE = "#FF6600";
    public static final String PRIMARY_LIGHT = "#FF8533";
    public static final String PRIMARY_LIGHTEST = "#FF9966";
    public static final String SUCCESS_GREEN = "#27ae60";
    public static final String ERROR_RED = "#e74c3c";
    public static final String INFO_BLUE = "#3498db";
    public static final String WARNING_ORANGE = "#f39c12";
    public static final String NEUTRAL_GRAY = "#95a5a6";
    public static final String BG_WHITE = "#FFFFFF";
    public static final String BORDER_LIGHT = "#e0e0e0";
    public static final String TEXT_BLACK = "#000000";

    // SFONDO ARANCIONE CHIARO come nel LOGIN
    public static final String BG_ORANGE_LIGHT = "#FFCC99";
    public static final String BG_ORANGE_START = "#FF9966";

    public static Label createTitleLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Roboto", FontWeight.BOLD, 24));
        label.setTextFill(Color.web(PRIMARY_ORANGE));
        return label;
    }

    public static Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        label.setTextFill(Color.web(TEXT_BLACK)); // NERO per visibilità
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
        button.setTextFill(Color.web("#4B2E2E"));
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
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: " + SUCCESS_GREEN + "; " +
            "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16; -fx-opacity: 0.8;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + SUCCESS_GREEN + "; " +
            "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;"));
        return button;
    }

    public static Button createDangerButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
        button.setTextFill(Color.WHITE);
        button.setStyle("-fx-background-color: " + ERROR_RED + "; " +
            "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: " + ERROR_RED + "; " +
            "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16; -fx-opacity: 0.8;"));
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
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: " + INFO_BLUE + "; " +
            "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16; -fx-opacity: 0.8;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + INFO_BLUE + "; " +
            "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;"));
        return button;
    }

    public static ComboBox<String> createComboBox() {
        ComboBox<String> combo = new ComboBox<>();
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

    // CORREZIONE: Rimosso createListView() per evitare type mismatch
    // I ListView ora devono essere creati manualmente con il tipo specifico

    /**
     * Applica lo stile standard di ListView a un ListView esistente
     * Uso: ListView<TuoTipo> lista = new ListView<>(); StyleHelper.applyListViewStyle(lista);
     */
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

    // SFONDO ARANCIONE CHIARO per tutte le finestre
    public static void applyBackgroundGradient(Region region) {
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web(BG_ORANGE_START)),
            new Stop(1, Color.web(BG_ORANGE_LIGHT)));
        region.setBackground(new Background(new BackgroundFill(gradient, null, null)));
    }

    // Metodo per applicare sfondo arancione semplice
    public static void applyOrangeBackground(Region region) {
        region.setStyle("-fx-background-color: " + BG_ORANGE_LIGHT + ";");
    }

    // Text Area
    public static TextArea createTextArea(String promptText) {
        TextArea textArea = new TextArea();
        textArea.setPromptText(promptText);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: " + BORDER_LIGHT + "; " +
            "-fx-border-radius: 8; -fx-border-width: 1; -fx-font-size: 14px;");
        return textArea;
    }
}
