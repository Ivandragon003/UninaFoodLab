package model;

import java.time.LocalDateTime;

public class InPresenza extends Sessione{
	
private String via;
private String citta;
private int CAP;
private int numeroPosti;

public InPresenza(LocalDateTime dataInizioSessione, LocalDateTime dataFineSessione) {
	super(dataInizioSessione, dataFineSessione);
	this.via = via;
	this.citta = citta;
	this.CAP = CAP;
	this.numeroPosti = numeroPosti;
}

public String getVia() {
	return via;
}

public void setVia(String via) {
	this.via = via;
}

public String getCitta() {
	return citta;
}

public void setCitta(String citta) {
	this.citta = citta;
}

public int getCAP() {
	return CAP;
}

public void setCAP(int cAP) {
	CAP = cAP;
}

public int getNumeroPosti() {
	return numeroPosti;
}

public void setNumeroPosti(int numeroPosti) {
	this.numeroPosti = numeroPosti;
}


}
