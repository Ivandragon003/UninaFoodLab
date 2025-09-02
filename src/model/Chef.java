package model;

public class Chef extends Persona {
	
	private int anniEsperienza;
	private boolean disponibilita;
	private String username;
	private String passwordHash;
	public Chef(String codFiscale, String nome, String cognome, boolean disponibilita, String username,String passwordHash) {
		super(codFiscale, nome, cognome);
		this.disponibilita = disponibilita;
		this.username = username;
		 setPassword(passwordHash);
	}
	public int getAnniEsperienza() {
		return anniEsperienza;
	}
	public void setAnniEsperienza(int anniEsperienza) {
		this.anniEsperienza = anniEsperienza;
	}
	public boolean isDisponibilita() {
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

	public void setPassword(String passwordHash) {
		this.passwordHash = Integer.toString(passwordHash.hashCode());
	}
	
	
}
