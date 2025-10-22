package exceptions;

public class ValidationUtils {

	// ==================== CAMPPI OBBLIGATORI ====================
	public static void validateNotNull(Object value, String fieldName) throws ValidationException {
		if (value == null) {
			throw new ValidationException(campoObbligatorio(fieldName));
		}
	}

	public static void validateNotEmpty(String value, String fieldName) throws ValidationException {
		if (value == null || value.trim().isEmpty()) {
			throw new ValidationException(campoObbligatorio(fieldName));
		}
	}

	// ==================== LUNGHEZZA TESTO ====================
	public static void validateTextLength(String value, String fieldName, int minLength, int maxLength)
			throws ValidationException {
		if (value == null || value.trim().length() < minLength) {
			throw new ValidationException(fieldName + " deve contenere almeno " + minLength + " caratteri");
		}
		if (value.trim().length() > maxLength) {
			throw new ValidationException(fieldName + " non può superare " + maxLength + " caratteri");
		}
	}

	// ==================== INTERI E RANGE ====================
	public static void validatePositiveInt(Integer value, String fieldName) throws ValidationException {
		if (value == null || value <= 0) {
			throw new ValidationException(fieldName + " deve essere maggiore di zero");
		}
	}

	// ==================== VALIDAZIONI SPECIALI ====================
	public static void validateChefLogged(Object chef) throws ValidationException {
		if (chef == null) {
			throw new ValidationException("Nessuno chef loggato. Impossibile completare l'operazione.");
		}
	}

	public static String campoObbligatorio(String fieldName) {
		return "Il campo '" + fieldName + "' è obbligatorio";
	}

	public static Double parseDoubleSafe(String text) {
		if (text == null || text.trim().isEmpty()) {
			return null;
		}
		try {
			return Double.parseDouble(text.trim());
		} catch (NumberFormatException e) {
			return null;
		}

	}

	public static Integer parseIntegerSafe(String text) {
		if (text == null || text.trim().isEmpty()) {
			return null;
		}
		try {
			return Integer.parseInt(text.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

}
