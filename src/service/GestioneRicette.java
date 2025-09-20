package service;

import dao.RicettaDAO;
import model.Ricetta;
import java.util.List;

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
	
	// Lettura di tutte le ricette
	public List<Ricetta> getAllRicette() throws SQLException {
	    return ricettaDAO.getAll();
	}


	// Cancellazione ricetta 
	public void cancellaRicetta(int id) throws SQLException {
		ricettaDAO.delete(id);
	}
	

}
