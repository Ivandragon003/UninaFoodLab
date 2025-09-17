package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class InPresenza extends Sessione {
	private String via;
	private String citta;
	private int numeroPosti;
	private int CAP;
	
	
	private List<Adesione> adesioniSessione;
	private Set<Ricetta> ricette = new HashSet<>();
	
	
	public InPresenza(LocalDateTime dataInizioSessione, LocalDateTime dataFineSessione, String via, String citta,
			int numeroPosti, int CAP) {
		super(dataInizioSessione, dataFineSessione);
		this.via = via;
		this.citta = citta;
		this.numeroPosti = numeroPosti;
		this.CAP = CAP;
		this.adesioniSessione = new ArrayList<>();
	}

	public Set<Ricetta> getRicette() {
	    return ricette;
	}
	
	public List<Adesione> getAdesioniSessione() {
		return adesioniSessione;
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

	public int getNumeroPosti() {
		return numeroPosti;
	}

	public void setNumeroPosti(int numeroPosti) {
		this.numeroPosti = numeroPosti;
	}

	public int getCAP() {
		return CAP;
	}

	public void setCAP(int CAP) {
		this.CAP = CAP;
	}

	public String toStringVia() {
		return "Via: " + via;
	}

	public String toStringCitta() {
		return "Città: " + citta;
	}

	public String toStringNumeroPosti() {
		return "Numero Posti: " + numeroPosti;
	}

	public String toStringCAP() {
		return "CAP: " + CAP;
	}

}
