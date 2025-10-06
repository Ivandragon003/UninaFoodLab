package exceptions;

import model.Ingrediente;
import java.util.Map;

public class ValidationUtils {

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
}
