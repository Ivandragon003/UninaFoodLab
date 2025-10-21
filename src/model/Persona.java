package model;

import java.time.LocalDate;
import java.util.regex.Pattern;

import guihelper.ValidationHelper;

abstract public class Persona {
	private String codFiscale;
	private String nome;
	private String cognome;
	private String email;
	private LocalDate dataNascita;

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
	private static final Pattern CF_PATTERN = Pattern.compile("^[A-Z]{6}[0-9]{2}[A-Z][0-9]{2}[A-Z][0-9]{3}[A-Z]$");

	public Persona(String codFiscale, String nome, String cognome) {
		setCodFiscale(codFiscale);
		setNome(nome);
		setCognome(cognome);
	}

	public String getCodFiscale() {
		return codFiscale;
	}

	public void setCodFiscale(String codFiscale) {
		if (codFiscale == null || codFiscale.isBlank()) {
			throw new IllegalArgumentException("Codice fiscale non può essere nullo o vuoto");
		}

		codFiscale = codFiscale.trim().toUpperCase();

		if (codFiscale.length() != 16 || !CF_PATTERN.matcher(codFiscale).matches()) {
			throw new IllegalArgumentException(
					"Formato codice fiscale non valido: deve essere lungo 16 caratteri e rispettare il pattern italiano");
		}

		this.codFiscale = codFiscale;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		if (nome == null || nome.isBlank())
			throw new IllegalArgumentException("Nome non può essere nullo o vuoto");
		this.nome = nome.trim();
	}

	public String getCognome() {
		return cognome;
	}

	public void setCognome(String cognome) {
		if (cognome == null || cognome.isBlank())
			throw new IllegalArgumentException("Cognome non può essere nullo o vuoto");
		this.cognome = cognome.trim();
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		if (email == null || email.isBlank())
			throw new IllegalArgumentException("Email non può essere nulla o vuota");

		email = email.trim().toLowerCase();

		if (!EMAIL_PATTERN.matcher(email).matches()) {
			throw new IllegalArgumentException("Formato email non valido");
		}

		this.email = email;
	}

	public LocalDate getDataNascita() {
		return dataNascita;
	}

	public void setDataNascita(LocalDate dataNascita) {
		if (dataNascita == null)
			throw new IllegalArgumentException("Data di nascita non può essere nulla");
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

	@Override
	public String toString() {
		return getNome() + " " + getCognome();
	}

}