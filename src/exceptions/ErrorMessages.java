package exceptions;

public final class ErrorMessages {
    private ErrorMessages() {}

    // Corso
    public static final String CORSO_NULLO = "Corso nullo";
    public static final String NOME_CORSO_MANCANTE = "Nome corso mancante";
    public static final String NUMERO_POSTI_NON_VALIDO = "Numero posti non valido";
    public static final String PREZZO_NON_VALIDO = "Prezzo non valido";
    public static final String DATE_CORSO_MANCANTI = "Date del corso mancanti";
    public static final String DATA_INIZIO_PASSATA = "Data di inizio nel passato";
    public static final String DATA_FINE_PRECEDENTE = "Data fine precedente a inizio";

    // Chef
    public static final String CHEF_NULLO = "Chef nullo";
    public static final String CHEF_GIA_ASSEGNATO = "Chef già assegnato a questo corso";
    public static final String CHEF_NON_PRESENTE = "Chef non presente: ";

    // Password
    public static final String PASSWORD_NON_VALIDA = "Password non valida";
}
