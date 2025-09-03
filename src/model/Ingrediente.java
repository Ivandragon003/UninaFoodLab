package model;
import java.util.Objects;
public class Ingrediente {
	private int idIngrediente;
	private String nome;
	private String tipo;

	public Ingrediente(String nome, String tipo) {
		 setNome(nome);
	     setTipo(tipo);
	}

	public int getIdIngrediente() {
		return idIngrediente;
	}

	public void setIdIngrediente(int idIngrediente) {
        if (idIngrediente <= 0) {
            throw new IllegalArgumentException("L'ID dell'ingrediente deve essere maggiore di zero.");
        }
        this.idIngrediente = idIngrediente;
    }

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome dell'ingrediente non può essere nullo o vuoto.");
        }
        this.nome = nome.trim();
    }

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("Il tipo dell'ingrediente non può essere nullo o vuoto.");
        }
        this.tipo = tipo.trim();
    }
	
	 @Override
	    public boolean equals(Object obj) {
	        if (this == obj) return true;
	        if (obj == null || getClass() != obj.getClass()) return false;
	        Ingrediente that = (Ingrediente) obj;
	        return Objects.equals(nome, that.nome) && Objects.equals(tipo, that.tipo);
	    }
	    
	    @Override
	    public int hashCode() {
	        return Objects.hash(nome, tipo);
	    }
	    public String toStringIdIngrediente() {
	        return "ID Ingrediente: " + idIngrediente;
	    }

	    public String toStringNome() {
	        return "Nome: " + nome;
	    }

	    public String toStringTipo() {
	        return "Tipo: " + tipo;
	    }
}
