package model;

import java.time.LocalDateTime;
import java.util.*;

public class Online extends Sessione {
    private String piattaformaStreaming;
    private List<Adesione> adesioni = new ArrayList<>();
    
    public Online(LocalDateTime dataInizioSessione, LocalDateTime dataFineSessione, String piattaformaStreaming) {
        super(dataInizioSessione, dataFineSessione);
        setPiattaformaStreaming(piattaformaStreaming); // usa il setter per garantire non null
    }

    public String getPiattaformaStreaming() {
        return piattaformaStreaming;
    }

    public void setPiattaformaStreaming(String piattaformaStreaming) {
        if (piattaformaStreaming == null ) {
            throw new IllegalArgumentException("La piattaforma di streaming non può essere null o vuota");
        }
        this.piattaformaStreaming = piattaformaStreaming;
    }

	public List<Adesione> getAdesioni() {
		return adesioni;
	}

	public void setAdesioni(List<Adesione> adesioni) {
		this.adesioni = adesioni;
	}
}
