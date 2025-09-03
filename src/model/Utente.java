package model;

public class Utente extends Persona {

	public Utente(String codFiscale, String nome, String cognome) {
		super(codFiscale, nome, cognome);
		
	}
	
	public String toStringCodFiscale() {
	    return "Codice Fiscale: " + getCodFiscale();
	}

	public String toStringNome() {
	    return "Nome: " + getNome();
	}

	public String toStringCognome() {
	    return "Cognome: " + getCognome();
	}

}
