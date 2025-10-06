package exceptions;

import model.Ingrediente;
import java.util.Map;
import java.util.regex.Pattern;

public class ValidationUtils {

    // ==================== VALIDAZIONI GENERICHE ====================
    
    public static void validateNotEmpty(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(ErrorMessages.campoObbligatorio(fieldName));
        }
    }
    
    public static void validateTextLength(String value, String fieldName, int minLength, int maxLength) 
            throws ValidationException {
        if (value == null) {
            throw new ValidationException(ErrorMessages.campoObbligatorio(fieldName));
        }
        
        int length = value.trim().length();
        
        if (length < minLength) {
            throw new ValidationException(
                fieldName + " deve contenere almeno " + minLength + " caratteri"
            );
        }
        
        if (length > maxLength) {
            throw new ValidationException(
                fieldName + " non può superare " + maxLength + " caratteri"
            );
        }
    }
    
    public static void validateEmail(String email) throws ValidationException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException(ErrorMessages.campoObbligatorio("Email"));
        }
        
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        
        if (!pattern.matcher(email.trim()).matches()) {
            throw new ValidationException("Formato email non valido");
        }
    }

    // ==================== VALIDAZIONI RICETTE ====================
    
    public static void validateNomeRicetta(String nome) throws ValidationException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new ValidationException(ErrorMessages.NOME_RICETTA_MANCANTE);
        }
        if (nome.trim().length() < 3) {
            throw new ValidationException(ErrorMessages.NOME_RICETTA_TROPPO_CORTO);
        }
        if (nome.trim().length() > 100) {
            throw new ValidationException(ErrorMessages.NOME_RICETTA_TROPPO_LUNGO);
        }
    }

    public static void validateTempoPreparazione(int tempo) throws ValidationException {
        if (tempo <= 0) {
            throw new ValidationException(ErrorMessages.TEMPO_NON_VALIDO);
        }
        if (tempo > 1440) {
            throw new ValidationException(ErrorMessages.TEMPO_TROPPO_LUNGO);
        }
    }
    
    public static void validateNotNull(Object obj, String fieldName) throws ValidationException {
        if (obj == null) {
            throw new ValidationException(fieldName + " non può essere nullo");
        }
    }

    public static void validateIngredienti(Map<Ingrediente, Double> ingredienti) throws ValidationException {
        if (ingredienti == null || ingredienti.isEmpty()) {
            throw new ValidationException(ErrorMessages.INGREDIENTI_MANCANTI);
        }
        
        for (Map.Entry<Ingrediente, Double> entry : ingredienti.entrySet()) {
            validateQuantita(entry.getValue());
        }
    }

    public static void validateQuantita(double quantita) throws ValidationException {
        if (quantita <= 0) {
            throw new ValidationException(ErrorMessages.QUANTITA_NON_VALIDA);
        }
    }

    // ==================== VALIDAZIONI RANGE ====================
    
    public static void validateIntRange(Integer min, Integer max, String fieldName) throws ValidationException {
        if (min != null && max != null && min > max) {
            throw new ValidationException(fieldName + ": valore minimo maggiore del massimo");
        }
    }

    // ==================== PARSING SICURO ====================
    
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

    public static boolean isValidInteger(String text) {
        return parseIntegerSafe(text) != null || text == null || text.trim().isEmpty();
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
}
