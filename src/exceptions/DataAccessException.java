package exceptions;

//Eccezione unchecked per problemi di accesso ai dati 


public class DataAccessException extends RuntimeException {
    public DataAccessException(String message) {
        super(message);
    }
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
