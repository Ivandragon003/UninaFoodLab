package model;

import java.util.Objects;

public class Usa {
	private Ricetta ricetta;
	private Ingrediente ingrediente;
	private double quantita;

	public Usa(Ricetta ricetta, Ingrediente ingrediente, double quantita) {
		setRicetta(ricetta);
		setIngrediente(ingrediente);
		setQuantita(quantita);
	}

	public Ricetta getRicetta() {
		return ricetta;
	}

	public void setRicetta(Ricetta ricetta) {
		if (ricetta == null) {
			throw new IllegalArgumentException("La ricetta non può essere nulla.");
		}
		this.ricetta = ricetta;
	}

	public Ingrediente getIngrediente() {
		return ingrediente;
	}

	public void setIngrediente(Ingrediente ingrediente) {
		if (ingrediente == null) {
			throw new IllegalArgumentException("L'ingrediente non può essere nullo.");
		}
		this.ingrediente = ingrediente;
	}

	public double getQuantita() {
		return quantita;
	}

	public void setQuantita(double quantita) {
		if (quantita <= 0) {
			throw new IllegalArgumentException("La quantità deve essere maggiore di zero.");
		}
		this.quantita = quantita;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Usa))
			return false;
		Usa usa = (Usa) o;
		return Objects.equals(ricetta, usa.ricetta) && Objects.equals(ingrediente, usa.ingrediente);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ricetta, ingrediente);
	}

	public String toStringRicetta() {
		return "Ricetta: " + (ricetta != null ? ricetta.getNome() : "null");
	}

	public String toStringIngrediente() {
		return "Ingrediente: " + (ingrediente != null ? ingrediente.getNome() : "null");
	}

	public String toStringQuantita() {
		return "Quantità: " + quantita;
	}
}
