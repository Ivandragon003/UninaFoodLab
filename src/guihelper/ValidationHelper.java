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

/**
 * Utility class for field validation with styled error dialogs.
 */
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

	public static final String CODICE_FISCALE_REGEX = "^[A-Z]{6}[0-9]{2}[A-Z][0-9]{2}[A-Z][0-9]{3}[A-Z]$";
	private static final Pattern CF_PATTERN = Pattern.compile(CODICE_FISCALE_REGEX);

	/**
	 * Verifica che il campo non sia vuoto. In caso di errore mostra dialogo styled.
	 *
	 * @param field      il controllo di input
	 * @param container  contenitore VBox del field (opzionale)
	 * @param errorLabel label per messaggi di errore (opzionale)
	 * @param fieldName  nome logico del campo per il messaggio
	 * @return true se non vuoto, false altrimenti
	 */
	public static boolean validateNotEmpty(TextInputControl field, VBox container, Label errorLabel, String fieldName) {
		String value = (field != null && field.getText() != null) ? field.getText().trim() : "";
		if (value.isEmpty()) {
			// Applica stile di errore al field/container
			applyErrorStyle(field, container);

			// Imposta e mostra l’errorLabel
			if (errorLabel != null) {
				errorLabel.setText("❌ Inserisci " + fieldName);
				errorLabel.setVisible(true);
			}
			if (field != null)
				field.requestFocus();
			return false;
		} else {
			// Nasconde l’errorLabel e ripristina stile
			if (errorLabel != null)
				errorLabel.setVisible(false);
			applyNormalStyle(field, container);
			return true;
		}
	}

	/**
	 * Formatter per accettare solo lettere (incluse accentate e apostrofo/spazio).
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

	/**
	 * Nasconde eventuale stato di errore ripristinando stile normale.
	 */
	public static void hideError(TextInputControl field, VBox container, Label errorLabel) {
		Runnable r = () -> {
			applyNormalStyle(field, container);
			if (errorLabel != null) {
				errorLabel.setVisible(false);
			}
		};
		runOnUiThread(r);
	}

	/**
	 * Aggiunge listener per rimuovere errore non appena l'utente digita.
	 */
	public static void addAutoResetListener(TextInputControl field, Label errorLabel) {
		addAutoResetListener(field, null, errorLabel);
	}

	public static void addAutoResetListener(TextInputControl field, VBox container, Label errorLabel) {
		if (field == null)
			return;
		field.textProperty().addListener((obs, oldV, newV) -> {
			if (newV != null && !newV.trim().isEmpty()) {
				hideError(field, container, errorLabel);
			}
		});
	}

	// ======= Metodi privati =======

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
			if (container.getChildren().isEmpty())
				return;
			Node first = container.getChildren().get(0);
			if (first instanceof StackPane sp && !sp.getChildren().isEmpty()) {
				Node bg = sp.getChildren().get(0);
				if (bg instanceof Region r) {
					r.setStyle(ERROR_STYLE);
				}
			}
		} catch (Exception ignored) {
		}
	}

	private static void resetContainerStyleSafe(VBox container) {
		try {
			if (container.getChildren().isEmpty())
				return;
			Node first = container.getChildren().get(0);
			if (first instanceof StackPane sp && !sp.getChildren().isEmpty()) {
				Node bg = sp.getChildren().get(0);
				if (bg instanceof Region r) {
					r.setStyle(NORMAL_STYLE);
				}
			}
		} catch (Exception ignored) {
		}
	}

	private static void runOnUiThread(Runnable r) {
		if (Platform.isFxApplicationThread()) {
			r.run();
		} else {
			Platform.runLater(r);
		}
	}
}
