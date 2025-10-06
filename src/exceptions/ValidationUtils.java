package exceptions;

import java.util.List;

/**
 * Utility per validazione centralizzata
 */
public class ValidationUtils {
    
    // ===== VALIDAZIONE TESTO =====
    public static void validateNotEmpty(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(ErrorMessages.campoObbligatorio(fieldName));
        }
    }
    
    public static void validateTextLength(String value, String fieldName, int minLen, int maxLen) 
            throws ValidationException {
        validateNotEmpty(value, fieldName);
        int len = value.trim().length();
        if (len < minLen || len > maxLen) {
            throw new ValidationException(ErrorMessages.lunghezzaNonValida(fieldName, minLen, maxLen));
        }
    }
    
    // ===== VALIDAZIONE NUMERI =====
    public static int validatePositiveInt(String value, String fieldName) throws ValidationException {
        validateNotEmpty(value, fieldName);
        try {
            int result = Integer.parseInt(value.trim());
            if (result <= 0) {
                throw new ValidationException(fieldName + " deve essere maggiore di zero");
            }
            return result;
        } catch (NumberFormatException e) {
            throw new ValidationException(ErrorMessages.FORMATO_NON_VALIDO + " per " + fieldName);
        }
    }
    
    public static Integer validateIntRange(String value, String fieldName, Integer min, Integer max) 
            throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        try {
            int intValue = Integer.parseInt(value.trim());
            if (min != null && intValue < min) {
                throw new ValidationException(fieldName + " deve essere >= " + min);
            }
            if (max != null && intValue > max) {
                throw new ValidationException(fieldName + " deve essere <= " + max);
            }
            return intValue;
        } catch (NumberFormatException e) {
            throw new ValidationException(ErrorMessages.FORMATO_NON_VALIDO + " per " + fieldName);
        }
    }
    
    public static double validatePositiveDouble(String value, String fieldName) throws ValidationException {
        validateNotEmpty(value, fieldName);
        try {
            double result = Double.parseDouble(value.trim());
            if (result <= 0) {
                throw new ValidationException(ErrorMessages.QUANTITA_NON_VALIDA);
            }
            return result;
        } catch (NumberFormatException e) {
            throw new ValidationException(ErrorMessages.FORMATO_NON_VALIDO + " per " + fieldName);
        }
    }
    
    // ===== VALIDAZIONE LISTE =====
    public static void validateNotEmpty(List<?> list, String fieldName) throws ValidationException {
        if (list == null || list.isEmpty()) {
            throw new ValidationException(fieldName + " non può essere vuoto");
        }
    }
    
    // ===== HELPER SICURI PER LISTENER =====
    public static boolean isValidInteger(String value) {
        if (value == null || value.trim().isEmpty()) return true;
        try {
            Integer.parseInt(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static boolean isValidDouble(String value) {
        if (value == null || value.trim().isEmpty()) return true;
        try {
            Double.parseDouble(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    //  VALIDAZIONE EMAIL/CONTATTI 
    public static void validateEmail(String email) throws ValidationException {
        validateNotEmpty(email, "Email");
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException(ErrorMessages.EMAIL_NON_VALIDA);
        }
    }
    
    public static void validatePhone(String phone) throws ValidationException {
        validateNotEmpty(phone, "Telefono");
        if (!phone.matches("^[0-9+\\-\\s()]+$")) {
            throw new ValidationException(ErrorMessages.TELEFONO_NON_VALIDO);
        }
    }
}