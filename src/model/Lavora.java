package model;

import java.util.Objects;

public class Lavora {
    private Chef chef;                
    private Ristorante ristorante;    
    public Lavora(Chef chef, Ristorante ristorante) {
        this.chef = chef;
        this.ristorante = ristorante;
    }

    public Chef getChef() {
        return chef;
    }

    public void setChef(Chef chef) {
        this.chef = chef;
    }

    public Ristorante getRistorante() {
        return ristorante;
    }

    public void setRistorante(Ristorante ristorante) {
        this.ristorante = ristorante;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lavora)) return false;
        Lavora lavora = (Lavora) o;
        return Objects.equals(chef, lavora.chef) &&
               Objects.equals(ristorante, lavora.ristorante);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chef, ristorante);
    }

  
}
