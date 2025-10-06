package exceptions;

import java.util.Map;
import model.Ingrediente;

public class ValidationUtils {
    
    // ==================== RICETTE ====================
    
    public static void validateNomeRicetta(String nome) throws ValidationException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new ValidationException(ErrorMessages.NOME_RICETTA_MANCANTE);
        }
        
        if (nome.trim().length() < 3) {
            throw new ValidationException(ErrorMessages.NOME_RICETTA_TROPPO_CORTO);
        }
        
        if (nome.length() > 100) {
            throw new ValidationException(ErrorMessages.NOME_RICETTA_TROPPO_LUNGO);
        }
    }
    
    public static void validateTempoPreparazione(int tempo) throws ValidationException {
        if (tempo <= 0) {
            throw new ValidationException(ErrorMessages.TEMPO_NON_VALIDO);
        }
        
        if (tempo > 1440) { // 24 ore
            throw new ValidationException(ErrorMessages.TEMPO_TROPPO_LUNGO);
        }
    }
    
    public static void validateIngredienti(Map<Ingrediente, Double> ingredienti) throws ValidationException {
        if (ingredienti == null || ingredienti.isEmpty()) {
            throw new ValidationException(ErrorMessages.INGREDIENTI_MANCANTI);
        }
        
        if (ingredienti.size() > 50) {
            throw new ValidationException("Una ricetta non può contenere più di 50 ingredienti");
        }
        
        for (Map.Entry<Ingrediente, Double> entry : ingredienti.entrySet()) {
            validateQuantita(entry.getValue());
        }
    }
    
    public static void validateQuantita(double quantita) throws ValidationException {
        if (quantita <= 0) {
            throw new ValidationException(ErrorMessages.QUANTITA_NON_VALIDA);
        }
        
        if (quantita > 10000) {
            throw new ValidationException("La quantità non può superare 10 kg");
        }
    }
    
    // ==================== RANGE VALIDATION ====================
    
    public static void validateIntRange(Integer min, Integer max, String fieldName) throws ValidationException {
        if (min != null && max != null && min > max) {
            throw new ValidationException(fieldName + ": il valore minimo non può essere maggiore del massimo");
        }
        
        if (min != null && min < 0) {
            throw new ValidationException(fieldName + ": il valore minimo non può essere negativo");
        }
        
        if (max != null && max < 0) {
            throw new ValidationException(fieldName + ": il valore massimo non può essere negativo");
        }
    }
    
    // ==================== PARAMETRI NULL ====================
    
    public static void validateNotNull(Object obj, String paramName) throws ValidationException {
        if (obj == null) {
            throw new ValidationException(ErrorMessages.campoObbligatorio(paramName));
        }
    }

    private ValidationUtils() {
        throw new AssertionError("Utility class non istanziabile");
    }
}
