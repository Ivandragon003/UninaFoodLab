package model;

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
}
