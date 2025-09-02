package model;

public class Ristorante {

	
	private String partivaIVA;
	private String via;
	private String nome;
	private int stelleMichelin;
	
	private int idRistorante;
	public Ristorante(int idRistorante, String partivaIVA, String nome) {
		super();
		this.idRistorante = idRistorante;
		this.partivaIVA = partivaIVA;
		this.nome = nome;
	}
	public String getPartivaIVA() {
		return partivaIVA;
	}
	public void setPartivaIVA(String partivaIVA) {
		this.partivaIVA = partivaIVA;
	}
	public String getVia() {
		return via;
	}
	public void setVia(String via) {
		this.via = via;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public int getStelleMichelin() {
		return stelleMichelin;
	}
	public void setStelleMichelin(int stelleMichelin) {
		this.stelleMichelin = stelleMichelin;
	}
	public int getIdRistorante() {
		return idRistorante;
	}
	public void setIdRistorante(int idRistorante) {
		this.idRistorante = idRistorante;
	}
	
}
