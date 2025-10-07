package util;

import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;


public final class ValidationHelper {

    private ValidationHelper() {
        throw new AssertionError("Classe di utilità non istanziabile");
    }

    private static final String NORMAL_STYLE = """
        -fx-background-color: white;
        -fx-background-radius: 15;
        -fx-border-radius: 15;
        -fx-border-color: #FF9966;
        -fx-border-width: 1.5;
        -fx-padding: 5 10;
    """;

    private static final String ERROR_STYLE = """
        -fx-background-color: white;
        -fx-background-radius: 15;
        -fx-border-radius: 15;
        -fx-border-color: red;
        -fx-border-width: 2;
        -fx-padding: 5 10;
    """;

    
    // VALIDAZIONE GENERICA
   

    public static boolean validateNotEmpty(TextInputControl field, Label errorLabel, String fieldName) {
        return validateNotEmpty(field, null, errorLabel, fieldName);
    }

    public static boolean validateNotEmpty(TextInputControl field, VBox container, Label errorLabel, String fieldName) {
        String value = field.getText() == null ? "" : field.getText().trim();
        if (value.isEmpty()) {
            showError(field, container, errorLabel, "❌ Inserisci " + fieldName);
            field.requestFocus();
            return false;
        } else {
            hideError(field, container, errorLabel);
            return true;
        }
    }

    
    // MOSTRA / NASCONDI ERRORE
   

    public static void showError(TextInputControl field, Label errorLabel, String message) {
        showError(field, null, errorLabel, message);
    }

    public static void showError(TextInputControl field, VBox container, Label errorLabel, String message) {
        if (container != null) {
            setContainerErrorStyle(container);
        } else {
            field.setStyle(ERROR_STYLE);
        }
        if (errorLabel != null) {
            errorLabel.setTextFill(Color.RED);
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }

    public static void hideError(TextInputControl field, Label errorLabel) {
        hideError(field, null, errorLabel);
    }

    public static void hideError(TextInputControl field, VBox container, Label errorLabel) {
        if (container != null) {
            resetContainerStyle(container);
        } else {
            field.setStyle(NORMAL_STYLE);
        }
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
    }

   
    // LISTENER AUTOMATICO
  

    public static void addAutoResetListener(TextInputControl field, Label errorLabel) {
        addAutoResetListener(field, null, errorLabel);
    }

    public static void addAutoResetListener(TextInputControl field, VBox container, Label errorLabel) {
        field.textProperty().addListener((obs, old, val) -> {
            if (val != null && !val.trim().isEmpty()) {
                hideError(field, container, errorLabel);
            }
        });
    }

    
    // STILE CONTAINER
   

    private static void setContainerErrorStyle(VBox container) {
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

    private static void resetContainerStyle(VBox container) {
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

  
    // RIPRISTINA STILE CAMPO
   

    public static void resetStyle(TextInputControl field) {
        field.setStyle(NORMAL_STYLE);
    }
}
