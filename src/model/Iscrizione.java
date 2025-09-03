package model;

import java.time.LocalDate;
import java.util.Objects;

public class Iscrizione {

<<<<<<< HEAD
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

	
	public String toStringCodFiscaleUtente() {
		return "Codice fiscale utente: " + (codFiscaleUtente != null ? codFiscaleUtente : "N/A");
	}

	public String toStringIdCorsoCucina() {
		return "ID corso cucina: " + idCorsoCucina;
	}

	public String toStringDataIscrizione() {
		return "Data iscrizione: " + (dataIscrizione != null ? dataIscrizione.toString() : "N/A");
	}

	public String toStringStato() {
		return "Stato: " + (stato ? "Attiva" : "Disattiva");
	}

=======
    private Utente utente;
    private CorsoCucina corso;
    private LocalDate dataIscrizione;
    private boolean stato; // true = attiva, false = ritirata

    public Iscrizione(Utente utente, CorsoCucina corso, LocalDate dataIscrizione) {
        if (utente == null || corso == null || dataIscrizione == null)
            throw new IllegalArgumentException("Utente, corso e data non possono essere null");
        this.utente = utente;
        this.corso = corso;
        this.dataIscrizione = dataIscrizione;
        this.stato = true; 
    }

    // Getter e Setter
    public Utente getUtente() { return utente; }
    public CorsoCucina getCorso() { return corso; }
    public LocalDate getDataIscrizione() { return dataIscrizione; }
    public boolean isStato() { return stato; }
    public void setStato(boolean stato) { this.stato = stato; }

    public String toStringUtente() { return "Utente: " + utente.getNome() + " " + utente.getCognome(); }
    public String toStringCorso() { return "Corso: " + corso.getNomeCorso(); }
    public String toStringDataIscrizione() { return "Data Iscrizione: " + dataIscrizione; }
    public String toStringStato() { return "Stato: " + (stato ? "Attiva" : "Ritirata"); }

    // equals e hashCode basati su utente + corso (chiave univoca)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Iscrizione)) return false;
        Iscrizione that = (Iscrizione) o;
        return Objects.equals(utente, that.utente) &&
               Objects.equals(corso, that.corso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(utente, corso);
    }
>>>>>>> d2882b89e6c4cf981c803679b9f3381bf5cfe188
}
