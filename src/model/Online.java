package model;

import java.time.LocalDateTime;

public class Online extends Sessione {
	public String getPiattaformaStreaming() {
		return piattaformaStreaming;
	}

	public void setPiattaformaStreaming(String piattaformaStreaming) {
		this.piattaformaStreaming = piattaformaStreaming;
	}

	private String piattaformaStreaming;

	public Online(LocalDateTime dataInizioSessione, LocalDateTime dataFineSessione, String piattaformaStreaming) {
		super(dataInizioSessione, dataFineSessione);
		this.piattaformaStreaming = piattaformaStreaming;
	}

	@Override
	public String toString() {
		return "Online [piattaformaStreamong=" + piattaformaStreaming + "]";
	}

}
