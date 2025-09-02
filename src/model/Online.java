package model;

import java.time.LocalDateTime;

public class Online extends Sessione {

	public Online(LocalDateTime dataInizioSessione, LocalDateTime dataFineSessione) {
		super(dataInizioSessione, dataFineSessione);
	}

	private String piattaformaStreaming;

	public String getPiattaformaStreaming() {
		return piattaformaStreaming;
	}

	public void setPiattaformaStreaming(String piattaformaStreaming) {
		this.piattaformaStreaming = piattaformaStreaming;
	}
	
	
}
