package service;

import java.time.LocalDateTime;
import java.util.*;
import model.Adesione;
import model.InPresenza;
import model.Utente;
import dao.AdesioneDAO;

public class GestioneAdesioni {

	private AdesioneDAO adesioneDAO;

	public GestioneAdesioni() {
		this.adesioneDAO = new AdesioneDAO();
	}

	public GestioneAdesioni(AdesioneDAO adesioneDAO) {
		this.adesioneDAO = adesioneDAO;
	}

	public boolean inviaAdesione(InPresenza sessione, Utente utente, LocalDateTime dataAdesione) {
		if (sessione == null || utente == null || dataAdesione == null) {
			throw new IllegalArgumentException("Parametri non validi");
		}
		return adesioneDAO.inserisci(new Adesione(utente, sessione, dataAdesione));
	}

	public boolean ritiraAdesione(InPresenza sessione, Utente utente) {
		if (sessione == null || utente == null) {
			throw new IllegalArgumentException("Parametri non validi");
		}
		return adesioneDAO.ritira(sessione, utente);
	}

	public Set<Utente> getUtentiAdesione(InPresenza sessione) {
		if (sessione == null) {
			throw new IllegalArgumentException("Sessione nulla");
		}
		return adesioneDAO.trovaUtentiPerSessione(sessione);
	}

	public boolean esisteAdesione(InPresenza sessione, Utente utente) {
		if (sessione == null || utente == null) {
			throw new IllegalArgumentException("Parametri non validi");
		}
		return adesioneDAO.esiste(sessione, utente);
	}

	public int contaAdesioni(InPresenza sessione) {
		if (sessione == null) {
			throw new IllegalArgumentException("Sessione nulla");
		}
		return adesioneDAO.contaPerSessione(sessione);
	}
}
