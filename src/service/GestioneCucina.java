package service;

import dao.CucinaDAO;
import model.InPresenza;
import model.Ricetta;
import model.Sessione;

import java.sql.SQLException;

public class GestioneCucina {

    private final CucinaDAO cucinaDAO;

    public GestioneCucina(CucinaDAO cucinaDAO) {
        this.cucinaDAO = cucinaDAO;
    }

    // Aggiungi sessione a ricetta (solo se è in presenza)
    public void aggiungiSessioneARicetta(Ricetta r, Sessione s) throws SQLException {
        if (!(s instanceof InPresenza)) {
            throw new IllegalArgumentException("Le ricette possono essere associate solo a sessioni in presenza");
        }
        if (!r.getSessioni().contains(s)) {
            r.getSessioni().add(s);
            cucinaDAO.save(r.getIdRicetta(), s.getIdSessione());
        } else {
            throw new IllegalArgumentException("Sessione già associata a questa ricetta");
        }
    }

    // Rimuovi sessione da ricetta (solo se è in presenza)
    public void rimuoviSessioneDaRicetta(Ricetta r, Sessione s) throws SQLException {
        if (!(s instanceof InPresenza)) {
            throw new IllegalArgumentException("Le ricette possono essere associate solo a sessioni in presenza");
        }
        if (r.getSessioni().remove(s)) {
            cucinaDAO.delete(r.getIdRicetta(), s.getIdSessione());
        } else {
            throw new IllegalArgumentException("Sessione non associata a questa ricetta");
        }
    }
}
