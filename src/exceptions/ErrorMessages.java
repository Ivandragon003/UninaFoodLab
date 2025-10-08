package exceptions;

public class ErrorMessages {
    
    // ==================== CORSI ====================
    public static final String CORSO_NULLO = "Il corso non può essere nullo";
    public static final String NOME_CORSO_MANCANTE = "Inserisci un nome valido per il corso";
    public static final String NUMERO_POSTI_NON_VALIDO = "Il numero di posti deve essere maggiore di zero";
    public static final String PREZZO_NON_VALIDO = "Il prezzo non può essere negativo";
    public static final String DATE_CORSO_MANCANTI = "Specifica sia la data di inizio che quella di fine del corso";
    public static final String DATA_INIZIO_PASSATA = "La data di inizio non può essere nel passato";
    public static final String DATA_FINE_PRECEDENTE = "La data di fine deve essere successiva alla data di inizio";
    public static final String CORSO_NON_TROVATO = "Corso non trovato nel sistema";
    public static final String CORSO_PIENO = "Il corso ha raggiunto il numero massimo di iscritti";
    
    // ==================== CHEF ====================
    public static final String CHEF_NULLO = "Lo chef non può essere nullo";
    public static final String CHEF_NON_PRESENTE = "Chef non trovato nel sistema: ";
    public static final String CHEF_GIA_ASSEGNATO = "Questo chef è già assegnato al corso";
    public static final String PASSWORD_NON_VALIDA = "La password deve contenere almeno 6 caratteri";
    public static final String USERNAME_NON_TROVATO = "Username non esistente";
    public static final String PASSWORD_ERRATA = "Password non corretta";
    public static final String CREDENZIALI_ERRATE = "Username o password non corretti";
    
    // ==================== SESSIONI ====================
    public static final String SESSIONE_NULLA = "La sessione non può essere nulla";
    public static final String DATE_SESSIONE_MANCANTI = "Specifica sia data/ora di inizio che di fine della sessione";
    public static final String DATA_INIZIO_SESSIONE_PASSATA = "La sessione non può iniziare nel passato";
    public static final String DATA_FINE_SESSIONE_PRECEDENTE = "La sessione deve terminare dopo l'inizio";
    public static final String PIATTAFORMA_MANCANTE = "Specifica la piattaforma per la sessione online";
    public static final String INDIRIZZO_INCOMPLETO = "Completa l'indirizzo (via, città, CAP)";
    public static final String VIA_MANCANTE = "Inserisci la via";
    public static final String CITTA_MANCANTE = "Inserisci la città";
    public static final String CAP_NON_VALIDO = "Inserisci un CAP valido (5 cifre)";
    public static final String POSTI_NON_VALIDI = "Il numero di posti deve essere almeno 1";
    public static final String SOLO_SESSIONI_IN_PRESENZA = "È possibile associare ricette solo alle sessioni in presenza";
    public static final String SESSIONE_NON_TROVATA = "La sessione selezionata non è stata trovata";

    
    // ==================== RICETTE ====================
    public static final String NOME_RICETTA_MANCANTE = "Il nome della ricetta è obbligatorio";
    public static final String NOME_RICETTA_TROPPO_CORTO = "Il nome deve contenere almeno 3 caratteri";
    public static final String NOME_RICETTA_TROPPO_LUNGO = "Il nome non può superare 100 caratteri";
    public static final String TEMPO_NON_VALIDO = "Il tempo di preparazione deve essere maggiore di zero";
    public static final String TEMPO_TROPPO_LUNGO = "Il tempo non può superare 24 ore (1440 minuti)";
    public static final String RICETTA_NON_TROVATA = "Ricetta non trovata";
    public static final String RICETTA_NULLA = "La ricetta non può essere nulla";
    public static final String ERRORE_INSERIMENTO = "Si è verificato un errore durante l'inserimento";

    
    // ==================== INGREDIENTI ====================
    public static final String INGREDIENTI_MANCANTI = "Aggiungi almeno un ingrediente";
    public static final String QUANTITA_NON_VALIDA = "La quantità deve essere maggiore di zero";
    
    // ==================== ISCRIZIONI ====================
    public static final String PARTECIPANTE_NULLO = "Il partecipante non può essere nullo";
    public static final String GIA_ISCRITTO = "Questo partecipante è già iscritto al corso";
    public static final String EMAIL_NON_VALIDA = "Inserisci un indirizzo email valido";
    public static final String TELEFONO_NON_VALIDO = "Inserisci un numero di telefono valido";
    
    // ==================== DATABASE ====================
    public static final String ERRORE_DATABASE = "Errore di connessione al database";
    public static final String ERRORE_SALVATAGGIO = "Impossibile salvare i dati";
    public static final String ERRORE_AGGIORNAMENTO = "Impossibile aggiornare i dati";
    public static final String ERRORE_ELIMINAZIONE = "Impossibile eliminare i dati";
    public static final String ERRORE_LETTURA = "Impossibile leggere i dati";
    
    // ==================== AUTENTICAZIONE ====================
    public static final String ACCESSO_NEGATO = "Non hai i permessi per eseguire questa operazione";
    public static final String SESSIONE_SCADUTA = "La tua sessione è scaduta, effettua nuovamente l'accesso";
    
    // ==================== VALIDAZIONE GENERICA ====================
    public static final String CAMPO_OBBLIGATORIO = "Questo campo è obbligatorio";
    public static final String FORMATO_NON_VALIDO = "Formato non valido";
    public static final String VALORE_TROPPO_LUNGO = "Il valore inserito è troppo lungo";
    public static final String VALORE_TROPPO_CORTO = "Il valore inserito è troppo corto";
    public static final String PARAMETRI_NULL = "Parametri non possono essere null";
    
    // ==================== MESSAGGI SUCCESSO ====================
    public static final String CORSO_CREATO = "Corso creato con successo!";
    public static final String CORSO_AGGIORNATO = "Corso aggiornato con successo!";
    public static final String CORSO_ELIMINATO = "Corso eliminato con successo!";
    public static final String CHEF_AGGIUNTO = "Chef aggiunto al corso!";
    public static final String CHEF_RIMOSSO = "Chef rimosso dal corso!";
    public static final String SESSIONE_CREATA = "Sessione creata con successo!";
    public static final String RICETTA_CREATA = "Ricetta creata con successo!";
    public static final String INGREDIENTE_AGGIUNTO = "Ingrediente aggiunto alla ricetta!";
    public static final String ISCRIZIONE_COMPLETATA = "Iscrizione completata con successo!";
    
    // ==================== HELPER METHODS ====================
    
    public static String campoObbligatorio(String nomeCampo) {
        return "Il campo '" + nomeCampo + "' è obbligatorio";
    }
    
    public static String valoreNonInRange(String nomeCampo, int min, int max) {
        return "Il campo '" + nomeCampo + "' deve essere compreso tra " + min + " e " + max;
    }
    
    public static String lunghezzaNonValida(String nomeCampo, int minLen, int maxLen) {
        return "Il campo '" + nomeCampo + "' deve contenere tra " + minLen + " e " + maxLen + " caratteri";
    }
    
    public static String confermaEliminazione(String oggetto) {
        return "Sei sicuro di voler eliminare " + oggetto + "? Questa operazione non può essere annullata.";
    }

    private ErrorMessages() {
        throw new AssertionError("Utility class non istanziabile");
    }
}
