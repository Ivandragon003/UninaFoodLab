package model;

public class Usa {
    private Ricetta ricetta;       
    private Ingrediente ingrediente;
    private double quantita;       

    public Usa(Ricetta ricetta, Ingrediente ingrediente, double quantita) {
        this.ricetta = ricetta;
        this.ingrediente = ingrediente;
        this.quantita = quantita;
    }

    public Ricetta getRicetta() { return ricetta; }
    public void setRicetta(Ricetta ricetta) { this.ricetta = ricetta; }

    public Ingrediente getIngrediente() { return ingrediente; }
    public void setIngrediente(Ingrediente ingrediente) { this.ingrediente = ingrediente; }

    public double getQuantita() { return quantita; }
    public void setQuantita(double quantita) { this.quantita = quantita; }
}
