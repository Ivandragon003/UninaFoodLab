package service;

import dao.RicettaDAO;
import model.Ricetta;

import java.sql.SQLException;

public class GestioneRicette {

	private final RicettaDAO ricettaDAO;

	public GestioneRicette(RicettaDAO ricettaDAO) {
		this.ricettaDAO = ricettaDAO;
	}

	// Creazione ricetta
	public void creaRicetta(Ricetta r) throws SQLException {
		ricettaDAO.save(r);
	}

	// Aggiornamento ricetta
	public void aggiornaRicetta(int id, Ricetta r) throws SQLException {
		ricettaDAO.update(id, r);
	}

	// Cancellazione ricetta 
	public void cancellaRicetta(int id) throws SQLException {
		ricettaDAO.delete(id);
	}

}
