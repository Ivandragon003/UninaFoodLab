package model;
import java.util.Objects;
public class Ingrediente {
	private int idIngrediente;
	private String nome;
	private String tipo;

	public Ingrediente(String nome, String tipo) {
		this.nome = nome;
		this.tipo = tipo;
	}

	public int getIdIngrediente() {
		return idIngrediente;
	}

	public void setIdIngrediente(int idIngrediente) {
		this.idIngrediente = idIngrediente;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
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
