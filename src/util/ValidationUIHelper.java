package util;

import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;

/**
 * Helper per validazioni UI comuni.
 * Evita duplicazione di controlli empty e styling errori.
 */
public class ValidationUIHelper {
    
    /**
     * Valida che un TextField non sia vuoto e mostra errore se lo è.
     * 
     * @return true se il campo è valido (non vuoto), false altrimenti
     */
    public static boolean validateNotEmpty(TextField field, VBox container, 
                                          Label errorLabel, String fieldName) {
        String value = field.getText().trim();
        
        if (value.isEmpty()) {
            showError(errorLabel, "❌ Inserisci " + fieldName);
            setFieldError(container);
            field.requestFocus();
            return false;
        }
        return true;
    }
    
    /**
     * Valida che una PasswordField non sia vuota.
     */
    public static boolean validateNotEmpty(PasswordField field, VBox container, 
                                          Label errorLabel, String fieldName) {
        String value = field.getText();
        
        if (value.isEmpty()) {
            showError(errorLabel, "❌ Inserisci " + fieldName);
            setFieldError(container);
            field.requestFocus();
            return false;
        }
        return true;
    }
    
    /**
     * Mostra un messaggio di errore.
     */
    public static void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    /**
     * Nasconde il messaggio di errore.
     */
    public static void hideError(Label errorLabel) {
        errorLabel.setVisible(false);
    }
    
    /**
     * Applica lo stile di errore a un campo.
     */
    public static void setFieldError(VBox container) {
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
    
    /**
     * Ripristina lo stile normale di un campo.
     */
    public static void resetFieldStyle(VBox container) {
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
    
    /**
     * Aggiunge listener per auto-reset dello stile errore su TextField.
     */
    public static void addAutoResetListener(TextField field, VBox container, Label errorLabel) {
        field.textProperty().addListener((obs, old, val) -> {
            if (!val.trim().isEmpty()) {
                hideError(errorLabel);
                resetFieldStyle(container);
            }
        });
    }
    
    /**
     * Aggiunge listener per auto-reset dello stile errore su PasswordField.
     */
    public static void addAutoResetListener(PasswordField field, VBox container, Label errorLabel) {
        field.textProperty().addListener((obs, old, val) -> {
            if (!val.trim().isEmpty()) {
                hideError(errorLabel);
                resetFieldStyle(container);
            }
        });
    }

    private ValidationUIHelper() {
        throw new AssertionError("Utility class non istanziabile");
    }
}