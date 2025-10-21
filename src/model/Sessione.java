package model;

import java.time.LocalDateTime;
import java.util.*;

abstract public class Sessione {
	private int idSessione;
	private LocalDateTime dataInizioSessione;
	private LocalDateTime dataFineSessione;
	private CorsoCucina corsoCucina;

	public Sessione(LocalDateTime dataInizioSessione, LocalDateTime dataFineSessione) {
		this.dataInizioSessione = dataInizioSessione;
		this.dataFineSessione = dataFineSessione;
	}

	public void setIdSessione(int idSessione) {
		this.idSessione = idSessione;
	}

	public int getIdSessione() {
		return idSessione;
	}

	public LocalDateTime getDataInizioSessione() {
		return dataInizioSessione;
	}

	public void setDataInizioSessione(LocalDateTime dataInizioSessione) {
		this.dataInizioSessione = dataInizioSessione;
	}

	public LocalDateTime getDataFineSessione() {
		return dataFineSessione;
	}

	public void setDataFineSessione(LocalDateTime dataFineSessione) {
		this.dataFineSessione = dataFineSessione;
	}

	public CorsoCucina getCorsoCucina() {
		return corsoCucina;
	}

	public void setCorsoCucina(CorsoCucina corsoCucina) {
		if (corsoCucina == null)
			throw new IllegalArgumentException("Corso obbligatorio");
		this.corsoCucina = corsoCucina;
	}

	public String toStringIdSessione() {
		return "ID Sessione: " + idSessione;
	}

	public String toStringDataInizioSessione() {
		return "Data Inizio Sessione: " + dataInizioSessione;
	}

	public String toStringDataFineSessione() {
		return "Data Fine Sessione: " + dataFineSessione;
	}

}