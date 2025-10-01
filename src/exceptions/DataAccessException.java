package exceptions;

/**
 * Eccezione unchecked per problemi di accesso ai dati (wrapping di SQLException).
 * Opzionale: può essere usata dal service se vuoi convertire SQLException in unchecked.
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message) {
        super(message);
    }
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
