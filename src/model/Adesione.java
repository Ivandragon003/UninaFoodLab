package model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Adesione {

	private LocalDateTime dataAdesione;
	private boolean stato;
	private Utente utente;
	private InPresenza sessione;

	public Adesione(Utente utente, InPresenza sessione, LocalDateTime dataAdesione) {
		this.utente = utente;
		this.sessione = sessione;
		this.dataAdesione = dataAdesione;
		this.stato = true;
	}

	public LocalDateTime getDataAdesione() {
		return dataAdesione;
	}

	public boolean isStato() {
		return stato;
	}

	public void setStato(boolean stato) {
		this.stato = stato;
	}

	public Utente getUtente() {
		return utente;
	}

	public InPresenza getSessione() {
		return sessione;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Adesione))
			return false;
		Adesione adesione = (Adesione) o;
		return Objects.equals(utente, adesione.utente) && Objects.equals(sessione, adesione.sessione);
	}

	@Override
	public int hashCode() {
		return Objects.hash(utente, sessione);
	}

	public String toStringUtente() {
		return "Utente: " + (utente != null ? utente.getNome() : "N/A");
	}

	public String toStringSessione() {
		return "Sessione: " + (sessione != null ? sessione.toString() : "N/A");
	}

	public String toStringDataAdesione() {
		return "Data adesione: " + (dataAdesione != null ? dataAdesione.toString() : "N/A");
	}

	public String toStringStato() {
		return "Stato: " + (stato ? "Attiva" : "Ritirata");
	}

}
