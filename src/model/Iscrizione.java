package model;

import java.util.*;
import java.time.LocalDate;

public class Iscrizione {

    private String codFiscaleUtente;  
    private int idCorsoCucina;         
    private LocalDate dataIscrizione; 
    private boolean stato;            


    public Iscrizione(String codFiscaleUtente, int idCorsoCucina, LocalDate dataIscrizione, boolean stato) {
        this.codFiscaleUtente = codFiscaleUtente;
        this.idCorsoCucina = idCorsoCucina;
        this.dataIscrizione = dataIscrizione;
        this.stato = stato;
    }

    
    public String getCodFiscaleUtente() {
        return codFiscaleUtente;
    }

    public void setCodFiscaleUtente(String codFiscaleUtente) {
        this.codFiscaleUtente = codFiscaleUtente;
    }

    public int getIdCorsoCucina() {
        return idCorsoCucina;
    }

    public void setIdCorsoCucina(int idCorsoCucina) {
        this.idCorsoCucina = idCorsoCucina;
    }

    public LocalDate getDataIscrizione() {
        return dataIscrizione;
    }

    public void setDataIscrizione(LocalDate dataIscrizione) {
        this.dataIscrizione = dataIscrizione;
    }

    public boolean isStato() {
        return stato;
    }

    public void setStato(boolean stato) {
        this.stato = stato;
    }

//    @Override
//    public String toString() {
//        return "Iscrizione{" +
//                "codFiscaleUtente='" + codFiscaleUtente + '\'' +
//                ", idCorsoCucina=" + idCorsoCucina +
//                ", dataIscrizione=" + dataIscrizione +
//                ", stato=" + stato +
//                '}';
//    }
//}
    
    private Map<CorsoCucina, Map<Utente, Boolean>> iscrizioni;

    public Iscrizione() {
        iscrizioni = new HashMap<>();
    }

    // Metodo per iscrivere un utente ad un corso
    public boolean iscriviUtente(CorsoCucina corso, Utente utente) {
        if (corso == null || utente == null) {
            return false; // validazione input
        }

        iscrizioni.putIfAbsent(corso, new HashMap<>());
        Map<Utente, Boolean> iscritti = iscrizioni.get(corso);

        // 1. Controllo doppia iscrizione attiva
        if (iscritti.containsKey(utente) && iscritti.get(utente)) {
            System.out.println( utente + " è già iscritto al corso " + corso);
            return false;
        }

        // 2. Controllo capienza massima (contiamo solo quelli attivi)
        long iscrittiAttivi = iscritti.values().stream().filter(Boolean::booleanValue).count();
        if (iscrittiAttivi >= corso.getNumeroPosti()) {
            System.out.println( corso + " è già pieno!");
            return false;
        }

        // Iscrizione attiva
        iscritti.put(utente, true);
        System.out.println(utente + " iscritto con successo al corso " + corso);
        return true;
    }

    // Metodo per disiscrivere un utente (imposta stato a false)
    public boolean disiscriviUtente(CorsoCucina corso, Utente utente) {
        if (corso == null || utente == null) return false;
        Map<Utente, Boolean> iscritti = iscrizioni.get(corso);
        if (iscritti != null && iscritti.containsKey(utente) && iscritti.get(utente)) {
            iscritti.put(utente, false);
            System.out.println(utente + " è stato disiscritto dal corso " + corso);
            return true;
        }
        return false;
    }

    // Metodo per ottenere solo gli iscritti attivi
    public Set<Utente> getIscrittiAttivi(CorsoCucina corso) {
        Map<Utente, Boolean> iscritti = iscrizioni.get(corso);
        if (iscritti == null) return Collections.emptySet();

        Set<Utente> attivi = new HashSet<>();
        for (Map.Entry<Utente, Boolean> entry : iscritti.entrySet()) {
            if (entry.getValue()) {
                attivi.add(entry.getKey());
            }
        }
        return attivi;
    }

    // Metodo per controllare lo stato di iscrizione di un utente
    public boolean getStatoIscrizione(CorsoCucina corso, Utente utente) {
        Map<Utente, Boolean> iscritti = iscrizioni.get(corso);
        return iscritti != null && iscritti.getOrDefault(utente, false);
    }
}
