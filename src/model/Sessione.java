package model;

import java.time.LocalDateTime;

public class Sessione {
	private int idSessione;
	private LocalDateTime dataInizioSessione;
	private LocalDateTime dataFineSessione;

	public Sessione(LocalDateTime dataInizioSessione, LocalDateTime dataFineSessione) {
		this.dataInizioSessione = dataInizioSessione;
		this.dataFineSessione = dataFineSessione;
	}

	public int getIdSessione() {
		return idSessione;
	}

	public void setIdSessione(int idSessione) {
		this.idSessione = idSessione;
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
}