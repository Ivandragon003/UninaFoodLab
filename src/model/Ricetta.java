package model;

import java.util.*;

public class Ricetta {
	private int idRicetta;
	private String nome;
	private int tempoPreparazione;
	private Map<Ingrediente, Double> ingredienti;

	public Ricetta(String nome, int tempoPreparazione) {
		setNome(nome);
		setTempoPreparazione(tempoPreparazione);
		this.ingredienti = new HashMap<>();
	}

	public int getIdRicetta() {
		return idRicetta;
	}

	public void setIdRicetta(int idRicetta) {
		if (idRicetta <= 0) {
			throw new IllegalArgumentException("L'ID della ricetta deve essere maggiore di zero.");
		}
		this.idRicetta = idRicetta;
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

	


	// Metodo per ottenere il numero di ingredienti nella ricetta
	public int getNumeroIngredienti() {
		return ingredienti.size();
	}

	public String toStringIdRicetta() {
		return "ID Ricetta: " + idRicetta;
	}

	public String toStringNome() {
		return "Nome: " + nome;
	}

	public String toStringTempoPreparazione() {
		return "Tempo Preparazione: " + tempoPreparazione;
	}
}