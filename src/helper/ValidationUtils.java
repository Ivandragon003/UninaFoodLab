package helper;

import exceptions.ValidationException;

public class ValidationUtils {

	public static void validateNotNull(Object value, String fieldName) throws ValidationException {
		if (value == null) {
			throw new ValidationException(campoObbligatorio(fieldName));
		}
	}

	public static String validateNotEmpty(String value, String fieldName) throws ValidationException {
		if (value == null || value.trim().isEmpty()) {
			throw new ValidationException(campoObbligatorio(fieldName));
		}
		return value.trim();
	}

	public static void validateTextLength(String value, String fieldName, int minLength, int maxLength)
			throws ValidationException {
		if (value == null || value.trim().length() < minLength) {
			throw new ValidationException(fieldName + " deve contenere almeno " + minLength + " caratteri");
		}
		if (value.trim().length() > maxLength) {
			throw new ValidationException(fieldName + " non può superare " + maxLength + " caratteri");
		}
	}

	public static void validatePositiveInt(Integer value, String fieldName) throws ValidationException {
		if (value == null || value <= 0) {
			throw new ValidationException(fieldName + " deve essere maggiore di zero");
		}
	}

	public static String campoObbligatorio(String fieldName) {
		return "Il campo '" + fieldName + "' è obbligatorio";
	}

	public static void validateLettersMin2(String value, String fieldName) throws ValidationException {
		if (value == null || value.trim().isEmpty()) {
			throw new ValidationException("Il campo '" + fieldName + "' è obbligatorio");
		}

		String trimmed = value.trim();
		if (trimmed.length() < 2) {
			throw new ValidationException(fieldName + " deve contenere almeno 2 caratteri");
		}

		if (!trimmed.matches("^[a-zA-ZàèéìòùÀÈÉÌÒÙ\\s'-]+$")) {
			throw new ValidationException(fieldName + " deve contenere solo lettere");
		}
	}

}
