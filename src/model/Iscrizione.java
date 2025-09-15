package model;

import java.util.Objects;

public class Iscrizione {

	private Utente utente;
	private CorsoCucina corso;
	private Integer votiAvuti;
	private boolean stato;

	public Iscrizione(Utente utente, CorsoCucina corso, boolean stato) {
		this.utente = utente;
		this.corso = corso;
		this.stato = stato;
	}

	public Utente getUtente() {
		return utente;
	}

	public CorsoCucina getCorso() {
		return corso;
	}

	public Integer getVotiAvuti() {
		return votiAvuti;
	}

	public boolean isStato() {
		return stato;
	}

	public void setVotiAvuti(Integer votiAvuti) {
		if (votiAvuti != null && (votiAvuti < 0 || votiAvuti > 10))
			throw new IllegalArgumentException("votiAvuti deve essere compreso tra 0 e 10");
		this.votiAvuti = votiAvuti;
	}

	public void setStato(boolean stato) {
		this.stato = stato;
	}

	public String toStringUtente() {
		return "Utente: " + utente.getNome() + " " + utente.getCognome();
	}

	public String toStringCorso() {
		return "Corso: " + corso.getNomeCorso();
	}

	public String toStringVotiAvuti() {
		return "Voti ottenuti: " + (votiAvuti != null ? votiAvuti : "N/A");
	}

	public String toStringStato() {
		return "Stato: " + (stato ? "Attiva" : "Ritirata");
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Iscrizione))
			return false;
		Iscrizione that = (Iscrizione) o;
		return Objects.equals(utente, that.utente) && Objects.equals(corso, that.corso);
	}

	@Override
	public int hashCode() {
		return Objects.hash(utente, corso);
	}
}
