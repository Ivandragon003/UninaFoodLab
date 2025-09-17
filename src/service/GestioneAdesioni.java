package service;

import dao.AdesioneDAO;
import model.Adesione;
import model.Utente;
import model.InPresenza;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class GestioneAdesioni {

	private final AdesioneDAO adesioneDAO;

	public GestioneAdesioni(AdesioneDAO adesioneDAO) {
		this.adesioneDAO = adesioneDAO;
	}

	public void aggiungiAdesione(Utente utente, InPresenza sessione, LocalDateTime data) throws SQLException {
		Adesione adesione = new Adesione(utente, sessione, data);

		if (!utente.getAdesioniUtente().contains(adesione)) {
			utente.getAdesioniUtente().add(adesione);
			adesioneDAO.save(adesione);
		} else {
			throw new IllegalArgumentException("Adesione già esistente per questa sessione");
		}
	}

	public void rimuoviAdesione(Utente utente, Adesione adesione) throws SQLException {
		if (utente.getAdesioniUtente().remove(adesione)) {
			adesioneDAO.delete(adesione.getUtente().getCodFiscale(), adesione.getSessione().getIdSessione());
		} else {
			throw new IllegalArgumentException("Adesione non trovata");
		}
	}

	public Set<Utente> getPartecipantiInPresenza(int idSessione) throws SQLException {
		return adesioneDAO.getPartecipantiInPresenza(idSessione);
	}
}
