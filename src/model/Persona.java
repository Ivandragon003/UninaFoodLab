package model;

import java.time.LocalDate;

abstract public class Persona {
	private String codFiscale;
	private String nome;
	private String cognome;
	private String email;
	private LocalDate dataNascita;

	public Persona(String codFiscale, String nome, String cognome) {
		this.codFiscale = codFiscale;
		this.nome = nome;
		this.cognome = cognome;

	}

	public String getCodFiscale() {
		return codFiscale;
	}

	public void setCodFiscale(String codFiscale) {
		this.codFiscale = codFiscale;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCognome() {
		return cognome;
	}

	public void setCognome(String cognome) {
		this.cognome = cognome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public LocalDate getDataNascita() {
		return dataNascita;
	}

	public void setDataNascita(LocalDate dataNascita) {
		this.dataNascita = dataNascita;
	}
	
	public String toStringCodFiscale() {
	    return "Codice Fiscale: " + codFiscale;
	}

	public String toStringNome() {
	    return "Nome: " + nome;
	}

	public String toStringCognome() {
	    return "Cognome: " + cognome;
	}

	public String toStringEmail() {
	    return "Email: " + email;
	}

	public String toStringDataNascita() {
	    return "Data di Nascita: " + dataNascita;
	}


}