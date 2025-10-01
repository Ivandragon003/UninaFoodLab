package exceptions;

/**
 * Eccezione checked per errori di validazione/business.
 * Usata dal service per comunicare errori che l'UI può mostrare all'utente.
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
