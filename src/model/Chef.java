package model;

import java.util.*;

public class Chef extends Persona {

	private int anniEsperienza;
	private boolean disponibilita;
	private String username;
	private String password;
	private List<CorsoCucina> corsi = new ArrayList<>();


	public Chef(String codFiscale, String nome, String cognome, boolean disponibilita, String username,
			String password) {
		super(codFiscale, nome, cognome);
		this.disponibilita = disponibilita;
		setUsername(username);
		setPassword(password);
	}

	public int getAnniEsperienza() {
		return anniEsperienza;
	}


	public void setAnniEsperienza(int anniEsperienza) {
		if (anniEsperienza < 0) {
			throw new IllegalArgumentException("Gli anni di esperienza non possono essere negativi.");
		}
		this.anniEsperienza = anniEsperienza;
	}

	public boolean getDisponibilita() {
		return disponibilita;
	}

	public void setDisponibilita(boolean disponibilita) {
		this.disponibilita = disponibilita;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		if (username == null || username.trim().isEmpty()) {
			throw new IllegalArgumentException("Username non può essere nullo o vuoto.");
		}
		this.username = username;
	}

	public List<CorsoCucina> getCorsi() {
		return corsi;
	}

	public void setPassword(String password) {
		if (password == null) {
			throw new IllegalArgumentException("La password non può essere nulla.");
		}
		if (password.length() < 6) {
			throw new IllegalArgumentException("La password deve contenere almeno 7 caratteri.");
		}
		this.password = password;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Chef other = (Chef) obj;
		return this.getUsername().equals(other.getUsername());
	}

	@Override
	public int hashCode() {
		return getUsername().hashCode();
	}

	public String getPassword() {
		return password;
	}

	public String toStringUsername() {
		return "Username: " + username;
	}

	public String toStringAnniEsperienza() {
		return "Anni Esperienza: " + anniEsperienza;
	}

	public String toStringDisponibilita() {
		return "Disponibilità: " + disponibilita;
	}

	public String toStringpassword() {
		return "password: " + password;
	}
}
