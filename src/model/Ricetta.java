package model;

import java.util.*;

public class Ricetta {
	private int idRicetta;
	private String nome;
	private int tempoPreparazione;
	private Map<Ingrediente, Double> ingredienti;
	private Set<Sessione> sessioni = new HashSet<>();

	public Ricetta(String nome, int tempoPreparazione) {
		setNome(nome);
		setTempoPreparazione(tempoPreparazione);
		this.ingredienti = new HashMap<>();
	}

	public int getIdRicetta() {
		return idRicetta;
	}

	public Set<Sessione> getSessioni() {
		return sessioni;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		if (nome == null || nome.trim().isEmpty()) {
			throw new IllegalArgumentException("Il nome della ricetta non può essere nullo o vuoto.");
		}
		this.nome = nome.trim();
	}

	public int getTempoPreparazione() {
		return tempoPreparazione;
	}

	public void setTempoPreparazione(int tempoPreparazione) {
		if (tempoPreparazione < 0) {
			throw new IllegalArgumentException("Il tempo di preparazione non può essere negativo.");
		}
		this.tempoPreparazione = tempoPreparazione;
	}

	public Map<Ingrediente, Double> getIngredienti() {
		return new HashMap<>(ingredienti);
	}

	public void setIngredienti(Map<Ingrediente, Double> ingredienti) {
		this.ingredienti = ingredienti != null ? new HashMap<>(ingredienti) : new HashMap<>();
	}

	public int getNumeroIngredienti() {
		return ingredienti.size();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Ricetta))
			return false;
		Ricetta ricetta = (Ricetta) o;
		return idRicetta == ricetta.idRicetta;
	}

	@Override
	public int hashCode() {
		return Objects.hash(idRicetta);
	}

	public String toStringIdRicetta() {
		return "ID Ricetta: " + idRicetta;
	}

	public String toStringNome() {
		return "Nome: " + nome;
	}

	public String toStringTempoPreparazione() {
		return "Tempo Preparazione: " + tempoPreparazione + " minuti";
	}

}