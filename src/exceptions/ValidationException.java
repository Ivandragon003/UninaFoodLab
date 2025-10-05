package exceptions;

/**
 * Eccezione base per errori di validazione
 */
public class ValidationException extends Exception {
    
    private final String fieldName;
    private final ValidationErrorType errorType;
    
    public ValidationException(String message) {
        super(message);
        this.fieldName = null;
        this.errorType = ValidationErrorType.GENERIC;
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.fieldName = null;
        this.errorType = ValidationErrorType.GENERIC;
    }
    
    public ValidationException(String fieldName, String message, ValidationErrorType errorType) {
        super(message);
        this.fieldName = fieldName;
        this.errorType = errorType;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public ValidationErrorType getErrorType() {
        return errorType;
    }
    
    public boolean hasFieldName() {
        return fieldName != null && !fieldName.isEmpty();
    }
    
    /**
     * Tipi di errore di validazione
     */
    public enum ValidationErrorType {
        REQUIRED,           // Campo obbligatorio mancante
        INVALID_FORMAT,     // Formato non valido
        OUT_OF_RANGE,       // Valore fuori range
        TOO_SHORT,          // Testo troppo corto
        TOO_LONG,           // Testo troppo lungo
        DUPLICATE,          // Valore duplicato
        NOT_FOUND,          // Risorsa non trovata
        CONSTRAINT_VIOLATION, // Violazione vincolo business
        GENERIC             // Errore generico
    }
}