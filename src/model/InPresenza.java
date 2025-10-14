package model;

import java.time.LocalDateTime;
import java.util.*;
import java.util.HashSet;


public class InPresenza extends Sessione {
    private String via;
    private String citta;
    private int numeroPosti;
    private int CAP;

    private List<Adesione> adesioniSessione;
    private Set<Ricetta> ricette = new HashSet<>();


    public InPresenza(LocalDateTime dataInizioSessione, LocalDateTime dataFineSessione,
                      String via, String citta, int numeroPosti, int CAP) {
        super(dataInizioSessione, dataFineSessione);
        setVia(via);
        setCitta(citta);
        setNumeroPosti(numeroPosti);
        setCAP(CAP);
        this.adesioniSessione = new ArrayList<>();
        this.ricette = new HashSet<Ricetta>();
    }


    public Set<Ricetta> getRicette() {
        return ricette;
    }
    
    public void setRicette(Set<Ricetta> ricette) {
        this.ricette = ricette != null ? ricette : new HashSet<>();
    }

    public List<Adesione> getAdesioniSessione() {
        return adesioniSessione;
    }

    public String getVia() {
        return via;
    }

    public void setVia(String via) {
        if (via == null || via.isBlank()) {
            throw new IllegalArgumentException("La via non può essere vuota");
        }
        this.via = via;
    }

    public String getCitta() {
        return citta;
    }

    public void setCitta(String citta) {
        if (citta == null || citta.isBlank()) {
            throw new IllegalArgumentException("La città non può essere vuota");
        }
        this.citta = citta;
    }

    public int getNumeroPosti() {
        return numeroPosti;
    }

    public void setNumeroPosti(int numeroPosti) {
        if (numeroPosti <= 0) {
            throw new IllegalArgumentException("Il numero di posti deve essere maggiore di zero");
        }
        this.numeroPosti = numeroPosti;
    }

    public int getCAP() {
        return CAP;
    }

    public void setCAP(int CAP) {
        if (CAP <= 0 && CAP > 5) {
            throw new IllegalArgumentException("CAP non valido");
        }
        this.CAP = CAP;
    }

    public String toStringVia() {
        return "Via: " + via;
    }

    public String toStringCitta() {
        return "Città: " + citta;
    }

    public String toStringNumeroPosti() {
        return "Numero Posti: " + numeroPosti;
    }

    public String toStringCAP() {
        return "CAP: " + CAP;
    }
}
