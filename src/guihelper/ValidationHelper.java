package guihelper;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;

public final class ValidationHelper {

    private ValidationHelper() {
        throw new AssertionError("Classe di utilità non istanziabile");
    }

    public static boolean validateNotEmpty(TextInputControl field, Label errorLabel, String fieldName) {
        String value = (field != null && field.getText() != null) ? field.getText().trim() : "";
        
        if (value.isEmpty()) {
            StyleHelper.applyErrorState(field);
            
            if (errorLabel != null) {
                errorLabel.setText("❌ Inserisci " + fieldName);
                errorLabel.setVisible(true);
            }
            
            if (field != null) field.requestFocus();
            return false;
        } else {
            if (errorLabel != null) errorLabel.setVisible(false);
            StyleHelper.applyNormalState(field);
            return true;
        }
    }

    public static String validateString(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Il campo " + fieldName + " è obbligatorio");
        }
        return value.trim();
    }

    public static int parseInteger(String value, String fieldName, int min, int max) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Il campo " + fieldName + " è obbligatorio");
        }

        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < min || parsed > max) {
                throw new IllegalArgumentException(
                    fieldName + " deve essere tra " + min + " e " + max
                );
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " deve essere un numero valido");
        }
    }

    public static void addErrorLabelResetListener(TextInputControl field, Label errorLabel) {
        if (field == null || errorLabel == null) return;
        
        field.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !newV.trim().isEmpty()) {
                errorLabel.setVisible(false);  
            }
        });
    }

    public static TextFormatter<String> getLettersOnlyFormatter() {
        return new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[a-zA-ZàèéìòùÀÈÉÌÒÙáéíóúÁÉÍÓÚäëïöüÄËÏÖÜâêîôûÂÊÎÔÛçÇ' ]*")) {
                return change;
            }
            return null;
        });
    }

    public static void hideError(TextInputControl field, Label errorLabel) {
        runOnUiThread(() -> {
            StyleHelper.applyNormalState(field);
            if (errorLabel != null) {
                errorLabel.setVisible(false);
            }
        });
    }

    public static void addAutoResetListener(TextInputControl field, Label errorLabel) {
        if (field == null) return;
        
        field.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !newV.trim().isEmpty()) {
                hideError(field, errorLabel);
            }
        });
    }

    private static void runOnUiThread(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }
}
