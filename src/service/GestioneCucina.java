package service;

import dao.CucinaDAO;
import model.Ricetta;
import model.Sessione;

import java.sql.SQLException;
import java.util.List;

public class GestioneCucina {

	private final CucinaDAO cucinaDAO;

	public GestioneCucina(CucinaDAO cucinaDAO) {
		this.cucinaDAO = cucinaDAO;
	}

	// Aggiungi sessione a ricetta
	public void aggiungiSessioneARicetta(Ricetta r, Sessione s) throws SQLException {
		if (!r.getSessioni().contains(s)) {
			r.getSessioni().add(s);
			cucinaDAO.save(r.getIdRicetta(), s.getIdSessione());
		} else {
			throw new IllegalArgumentException("Sessione già associata a questa ricetta");
		}
	}

	// Rimuovi sessione da ricetta
	public void rimuoviSessioneDaRicetta(Ricetta r, Sessione s) throws SQLException {
		if (r.getSessioni().remove(s)) {
			cucinaDAO.delete(r.getIdRicetta(), s.getIdSessione());
		} else {
			throw new IllegalArgumentException("Sessione non associata a questa ricetta");
		}
	}

	public List<Ricetta> getRicettePerSessione(int idSessione) throws SQLException {
		return cucinaDAO.getRicettePerSessione(idSessione);
	}
}
