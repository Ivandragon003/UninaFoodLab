package guihelper;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.regex.Pattern;

public final class ValidationHelper {

    private ValidationHelper() { 
        throw new AssertionError("Classe di utilità non istanziabile"); 
    }

    // ========== STILI ==========
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

    // ========== PATTERN VALIDAZIONE ==========
    public static final String CODICE_FISCALE_REGEX = "^[A-Z]{6}[0-9]{2}[A-Z][0-9]{2}[A-Z][0-9]{3}[A-Z]$";
    private static final Pattern CF_PATTERN = Pattern.compile(CODICE_FISCALE_REGEX);

    // ========== VALIDAZIONE SEMPLICE ==========
    public static boolean validateNotEmpty(TextInputControl field, Label errorLabel, String fieldName) {
        return validateNotEmpty(field, null, errorLabel, fieldName);
    }

    public static boolean validateNotEmpty(TextInputControl field, VBox container, Label errorLabel, String fieldName) {
        String value = field == null ? "" : (field.getText() == null ? "" : field.getText().trim());
        if (value.isEmpty()) {
            showError(field, container, errorLabel, "❌ Inserisci " + fieldName);
            if (field != null) field.requestFocus();
            return false;
        } else {
            hideError(field, container, errorLabel);
            return true;
        }
    }

   
    public static boolean isValidCodiceFiscale(String codiceFiscale) {
        if (codiceFiscale == null || codiceFiscale.trim().isEmpty()) {
            return false;
        }
        return CF_PATTERN.matcher(codiceFiscale.trim().toUpperCase()).matches();
    }

   
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String emailRegex = "^[a-zA-Z0-9._+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        
        if (!email.matches(emailRegex)) {
            return false;
        }
        
        String domain = email.substring(email.indexOf('@') + 1, email.lastIndexOf('.'));
        return !domain.matches("\\d+");
    }

    // ========== TEXT FORMATTERS ==========
    /**
     * Restituisce un TextFormatter che accetta solo lettere, spazi e apostrofi.
     */
    public static TextFormatter<String> getLettersOnlyFormatter() {
        return new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[a-zA-ZàèéìòùÀÈÉÌÒÙáéíóúÁÉÍÓÚäëïöüÄËÏÖÜâêîôûÂÊÎÔÛçÇ' ]*")) {
                return change;
            }
            return null;
        });
    }


   
  

    public static void showError(TextInputControl field, VBox container, Label errorLabel, String message) {
        Runnable r = () -> {
            applyErrorStyle(field, container);
            if (errorLabel != null) {
                errorLabel.setStyle("-fx-text-fill: red;");
                errorLabel.setText(message);
                errorLabel.setVisible(true);
            }
        };
        runOnUiThread(r);
    }

 

    public static void hideError(TextInputControl field, VBox container, Label errorLabel) {
        Runnable r = () -> {
            applyNormalStyle(field, container);
            if (errorLabel != null) {
                errorLabel.setVisible(false);
            }
        };
        runOnUiThread(r);
    }

    // ========== LISTENER PER RESET AUTOMATICO ==========
    public static void addAutoResetListener(TextInputControl field, Label errorLabel) {
        addAutoResetListener(field, null, errorLabel);
    }

    public static void addAutoResetListener(TextInputControl field, VBox container, Label errorLabel) {
        if (field == null) return;
        field.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !newV.trim().isEmpty()) {
                hideError(field, container, errorLabel);
            }
        });
    }

    // ========== METODI INTERNI ==========
    private static void applyErrorStyle(TextInputControl field, VBox container) {
        if (container != null) {
            setContainerErrorStyleSafe(container);
        } else if (field != null) {
            field.setStyle(ERROR_STYLE);
        }
    }

    private static void applyNormalStyle(TextInputControl field, VBox container) {
        if (container != null) {
            resetContainerStyleSafe(container);
        } else if (field != null) {
            field.setStyle(NORMAL_STYLE);
        }
    }

    private static void setContainerErrorStyleSafe(VBox container) {
        try {
            if (container.getChildren().isEmpty()) return;
            Node first = container.getChildren().get(0);
            if (first instanceof StackPane sp && !sp.getChildren().isEmpty()) {
                Node bg = sp.getChildren().get(0);
                if (bg instanceof Region r) {
                    r.setStyle("""
                        -fx-background-color: white;
                        -fx-background-radius: 15;
                        -fx-border-radius: 15;
                        -fx-border-color: red;
                        -fx-border-width: 2;
                    """);
                }
            }
        } catch (Exception ignored) { }
    }

    private static void resetContainerStyleSafe(VBox container) {
        try {
            if (container.getChildren().isEmpty()) return;
            Node first = container.getChildren().get(0);
            if (first instanceof StackPane sp && !sp.getChildren().isEmpty()) {
                Node bg = sp.getChildren().get(0);
                if (bg instanceof Region r) {
                    r.setStyle("""
                        -fx-background-color: white;
                        -fx-background-radius: 15;
                        -fx-border-radius: 15;
                        -fx-border-color: #FF9966;
                        -fx-border-width: 1.5;
                    """);
                }
            }
        } catch (Exception ignored) { }
    }

    private static void runOnUiThread(Runnable r) {
        if (Platform.isFxApplicationThread()) r.run(); 
        else Platform.runLater(r);
    }

   
}
