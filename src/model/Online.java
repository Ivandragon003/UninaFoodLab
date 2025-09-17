package model;

import java.time.LocalDateTime;
import java.util.*;

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
        if (piattaformaStreaming == null ) {
            throw new IllegalArgumentException("La piattaforma di streaming non può essere null o vuota");
        }
        this.piattaformaStreaming = piattaformaStreaming;
    }

	
}
