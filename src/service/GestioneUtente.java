package service;

import dao.AdesioneDAO;
import dao.IscrizioneDAO;
import dao.OnlineDAO;
import dao.InPresenzaDAO;
import model.*;

import java.sql.SQLException;

public class GestioneUtente {

	private final AdesioneDAO adesioneDAO;
	private final IscrizioneDAO iscrizioneDAO;
	private final OnlineDAO onlineDAO;
	private final InPresenzaDAO inPresenzaDAO;

	public GestioneUtente(AdesioneDAO adesioneDAO, IscrizioneDAO iscrizioneDAO, OnlineDAO onlineDAO,
			InPresenzaDAO inPresenzaDAO) {
		this.adesioneDAO = adesioneDAO;
		this.iscrizioneDAO = iscrizioneDAO;
		this.onlineDAO = onlineDAO;
		this.inPresenzaDAO = inPresenzaDAO;
	}

	// Adesioni
	public void aggiungiAdesione(Utente utente, Adesione adesione) throws SQLException {
		if (!utente.getAdesioniUtente().contains(adesione)) {
			utente.getAdesioniUtente().add(adesione);
			adesioneDAO.save(adesione);
		} else {
			throw new IllegalArgumentException("Adesione già presente per questo utente");
		}
	}

	public void rimuoviAdesione(Utente utente, Adesione adesione) throws SQLException {
		if (utente.getAdesioniUtente().remove(adesione)) {
			adesioneDAO.delete(adesione.getIdAdesione());
		} else {
			throw new IllegalArgumentException("Adesione non trovata per l'utente");
		}
	}

	// Iscrizioni
	public void aggiungiIscrizione(Utente utente, Iscrizione iscrizione) throws SQLException {
		if (!utente.getIscrizioni().contains(iscrizione)) {
			utente.getIscrizioni().add(iscrizione);
			iscrizioneDAO.save(iscrizione);
		} else {
			throw new IllegalArgumentException("Utente già iscritto a questo corso");
		}
	}

	public void rimuoviIscrizione(Utente utente, Iscrizione iscrizione) throws SQLException {
		if (utente.getIscrizioni().remove(iscrizione)) {
			iscrizioneDAO.delete(iscrizione.getUtente().getCodFiscale(), iscrizione.getCorso().getIdCorso());
		} else {
			throw new IllegalArgumentException("Iscrizione non trovata per l'utente");
		}
	}

	// === Sessioni seguite ===
	public void aggiungiSessione(Utente utente, Sessione sessione) throws SQLException {
		if (utente.getSessioniSeguite().add(sessione)) {
			if (sessione instanceof Online) {
				onlineDAO.save((Online) sessione);
			} else if (sessione instanceof InPresenza) {
				inPresenzaDAO.save((InPresenza) sessione);
			} else {
				throw new IllegalArgumentException("Tipo di sessione non gestito");
			}
		} else {
			throw new IllegalArgumentException("Utente già segue questa sessione");
		}
	}

	public void rimuoviSessione(Utente utente, Sessione sessione) throws SQLException {
		if (utente.getSessioniSeguite().remove(sessione)) {
			if (sessione instanceof Online) {
				onlineDAO.delete(sessione.getIdSessione());
			} else if (sessione instanceof InPresenza) {
				inPresenzaDAO.delete(sessione.getIdSessione());
			}
		} else {
			throw new IllegalArgumentException("Sessione non trovata per l'utente");
		}
	}
}
