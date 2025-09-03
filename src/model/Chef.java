package model;

public class Chef extends Persona {

	private int anniEsperienza;
	private boolean disponibilita;
	private String username;
	private String passwordHash;

	public Chef(String codFiscale, String nome, String cognome, boolean disponibilita, String username,
			String password) {
		super(codFiscale, nome, cognome);
		this.disponibilita = disponibilita;
		this.username = username;
		setPassword(password);
	}

	public int getAnniEsperienza() {
		return anniEsperienza;
	}

	public void setAnniEsperienza(int anniEsperienza) {
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
		this.username = username;
	}

	public void setPassword(String password) {
		this.passwordHash = Integer.toString(password.hashCode());
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

	public String toStringPasswordHash() {
	    return "PasswordHash: " + passwordHash;
	}
}
