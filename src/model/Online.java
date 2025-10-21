package model;

import java.time.LocalDateTime;

public class Online extends Sessione {
	private String piattaformaStreaming;

	public Online(LocalDateTime dataInizioSessione, LocalDateTime dataFineSessione, String piattaformaStreaming) {
		super(dataInizioSessione, dataFineSessione);
		setPiattaformaStreaming(piattaformaStreaming);
	}

	public String getPiattaformaStreaming() {
		return piattaformaStreaming;
	}


	public void setPiattaformaStreaming(String piattaformaStreaming) {
		if (piattaformaStreaming == null || piattaformaStreaming.trim().isEmpty()) {
			throw new IllegalArgumentException("La piattaforma di streaming è obbligatoria");
		}
		this.piattaformaStreaming = piattaformaStreaming.trim();
	}

}
