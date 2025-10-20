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
    
    public void setIdIngrediente(int idIngrediente) {
        this.idIngrediente = idIngrediente;
    }
    
    public int getIdIngrediente() {
        return idIngrediente;
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
    
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ingrediente)) return false;
        Ingrediente that = (Ingrediente) o;
      
        return Objects.equals(this.getIdIngrediente(), that.getIdIngrediente());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdIngrediente());
    }
    
    @Override
    public String toString() {
        return nome; // Mostra solo il nome dell'ingrediente
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
    
  
    public String toStringCompleto() {
        return nome + " (" + tipo + ")";
    }
}