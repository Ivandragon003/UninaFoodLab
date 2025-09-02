package model;

public class Ricetta {
	private int idRicetta;
	private String nome;
	private int tempoPreparazione;

	public Ricetta(String nome, int tempoPreparazione) {
		this.nome = nome;
		this.tempoPreparazione = tempoPreparazione;
	}

	public int getIdRicetta() {
		return idRicetta;
	}

	public void setIdRicetta(int idRicetta) {
		this.idRicetta = idRicetta;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public int getTempoPreparazione() {
		return tempoPreparazione;
	}

	public void setTempoPreparazione(int tempoPreparazione) {
		this.tempoPreparazione = tempoPreparazione;
	}
}
