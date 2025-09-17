package service;

import dao.AdesioneDAO;
import dao.InPresenzaDAO;
import dao.OnlineDAO;
import model.*;

import java.sql.SQLException;
import java.util.Set;

public class GestioneSessioni {

	private final InPresenzaDAO inPresenzaDAO;
	private final OnlineDAO onlineDAO;
	private final AdesioneDAO adesioneDAO;

	public GestioneSessioni(InPresenzaDAO inPresenzaDAO, OnlineDAO onlineDAO, AdesioneDAO adesioneDAO) {
		this.inPresenzaDAO = inPresenzaDAO;
		this.onlineDAO = onlineDAO;
		this.adesioneDAO = adesioneDAO;
	}

	// Creazione Sessioni
	public void creaSessione(Sessione sessione) throws SQLException {
		if (sessione instanceof InPresenza) {
			inPresenzaDAO.save((InPresenza) sessione);
		} else if (sessione instanceof Online) {
			onlineDAO.save((Online) sessione);
		} else {
			throw new IllegalArgumentException("Tipo di sessione non gestito");
		}
	}

	// Rimozione Sessioni
	public void rimuoviSessione(Sessione sessione) throws SQLException {
		if (sessione instanceof InPresenza) {
			adesioneDAO.deleteBySessione(sessione.getIdSessione());
			inPresenzaDAO.delete(sessione.getIdSessione());
		} else if (sessione instanceof Online) {
			onlineDAO.delete(sessione.getIdSessione());
		}
	}

	public void aggiungiRicettaASessione(InPresenza sessione, Ricetta ricetta) throws SQLException {
		if (!sessione.getRicette().contains(ricetta)) {
			sessione.getRicette().add(ricetta);
			ricetta.getSessioni().add(sessione);

		}
	}

	public void rimuoviRicettaDaSessione(InPresenza sessione, Ricetta ricetta) throws SQLException {
		if (sessione.getRicette().remove(ricetta)) {
			ricetta.getSessioni().remove(sessione);
		}
	}

}
