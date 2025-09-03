package model;
import java.util.*;


public class GestioneRicetta {

	
	// Metodo per aggiungere un ingrediente alla ricetta
		// Se l'ingrediente esiste già, aggiorna la quantità
	 public boolean aggiungiIngrediente(Ricetta ricetta, Ingrediente ingrediente, double quantita) {
	        if (ricetta == null || ingrediente == null || quantita <= 0) {
	            throw new IllegalArgumentException("Input non valido");
	        }
	        Map<Ingrediente, Double> ingredienti = ricetta.getMappaIngredienti();
	        if (ingredienti.containsKey(ingrediente)) {
	            ingredienti.put(ingrediente, quantita);
	            return false;
	        } else {
	            ingredienti.put(ingrediente, quantita);
	            return true;
	        }
	    }

		// Metodo per rimuovere un ingrediente dalla ricetta
		public boolean rimuoviIngrediente(Ingrediente ingrediente) {
			if (ingrediente == null) {
				throw new IllegalArgumentException("Ingrediente nullo.");
			}
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
			return ingredienti.keySet().stream().filter(ing -> ing.getNome().equalsIgnoreCase(nomeIngrediente)).findFirst()
					.orElse(null);
		}

		// Metodo per ottenere la quantità di un ingrediente
		public Double getQuantitaIngrediente(Ingrediente ingrediente) {
			if (ingrediente == null) {
				throw new IllegalArgumentException("Ingrediente nullo.");
			}
			return ingredienti.get(ingrediente);
		}

		// Metodo per modificare la quantità di un ingrediente
		public boolean modificaQuantitaIngrediente(String nomeIngrediente, double nuovaQuantita) {
			if (nuovaQuantita <= 0) {
				throw new IllegalArgumentException("La nuova quantità deve essere maggiore di zero.");
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
			if (ingrediente == null) {
				throw new IllegalArgumentException("Ingrediente nullo.");
			}
			return ingredienti.containsKey(ingrediente);
		}

}
