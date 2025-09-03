package model;

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

}
