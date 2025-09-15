package model;

import java.time.LocalDateTime;

public class Online extends Sessione {
	public String getPiattaformaStreaming() {
		return piattaformaStreaming;
	}

	public void setPiattaformaStreaming(String piattaformaStreaming) {
		this.piattaformaStreaming = piattaformaStreaming;
	}
}