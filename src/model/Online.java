package model;

import java.time.LocalDateTime;

public class Online extends Sessione {
	public String getPiattaformaStreamong() {
		return piattaformaStreamong;
	}

	public void setPiattaformaStreamong(String piattaformaStreamong) {
		this.piattaformaStreamong = piattaformaStreamong;
	}

	private String piattaformaStreamong;

	public Online(LocalDateTime dataInizioSessione, LocalDateTime dataFineSessione, String piattaformaStreamong) {
		super(dataInizioSessione, dataFineSessione);
		this.piattaformaStreamong = piattaformaStreamong;
	}

}
