package model;
import java.util.*;

public class Ricetta {
	private int idRicetta;
	private String nome;
	private int tempoPreparazione;
	private Map<Ingrediente, Double> ingredienti;

	public Ricetta(String nome, int tempoPreparazione) {
		this.nome = nome;
		this.tempoPreparazione = tempoPreparazione;
		this.ingredienti = new HashMap<>();
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


	//Metodo per aggiungere un ingrediente alla ricetta
	// Se l'ingrediente esiste già, aggiorna la quantità
	public boolean aggiungiIngrediente(Ingrediente ingrediente, double quantita) {
		if (ingrediente == null || quantita <= 0) {
			return false; // Validazione input
		}

		ingredienti.put(ingrediente, quantita);
		return true;
	}

	// Metodo per rimuovere un ingrediente dalla ricetta
	public boolean rimuoviIngrediente(Ingrediente ingrediente) {
		return ingredienti.remove(ingrediente) != null;
	}

	// Metodo per ottenere tutti gli ingredienti della ricetta come lista di Usa
	public List<Usa> getIngredienti() {
		List<Usa> listaUsi = new ArrayList<>();
		for (Map.Entry<Ingrediente, Double> entry : ingredienti.entrySet()) {
			listaUsi.add(new Usa(this, entry.getKey(), entry.getValue()));
		}
		return listaUsi;
	}

	// Metodo per ottenere la mappa degli ingredienti (copia per sicurezza)
	public Map<Ingrediente, Double> getMappaIngredienti() {
		return new HashMap<>(ingredienti);
	}

	// Metodo per cercare un ingrediente specifico per nome
	public Ingrediente trovaIngredientePerNome(String nomeIngrediente) {
		return ingredienti.keySet().stream()
				.filter(ing -> ing.getNome().equalsIgnoreCase(nomeIngrediente))
				.findFirst()
				.orElse(null);
	}

	// Metodo per ottenere la quantità di un ingrediente
	public Double getQuantitaIngrediente(Ingrediente ingrediente) {
		return ingredienti.get(ingrediente);
	}

	// Metodo per modificare la quantità di un ingrediente
	public boolean modificaQuantitaIngrediente(String nomeIngrediente, double nuovaQuantita) {
		if (nuovaQuantita <= 0) {
			return false;
		}

		Ingrediente ingrediente = trovaIngredientePerNome(nomeIngrediente);
		if (ingrediente != null && ingredienti.containsKey(ingrediente)) {
			ingredienti.put(ingrediente, nuovaQuantita);
			return true;
		}
		return false;
	}

	// Metodo per verificare se un ingrediente è presente nella ricetta
	public boolean contieneIngrediente(Ingrediente ingrediente) {
		return ingredienti.containsKey(ingrediente);
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