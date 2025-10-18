package service;

import dao.AdesioneDAO;
import dao.CucinaDAO;
import dao.InPresenzaDAO;
import dao.OnlineDAO;
import exceptions.DataAccessException;
import exceptions.ValidationException;
import exceptions.ValidationUtils;
import exceptions.ErrorMessages;
import model.InPresenza;
import model.Online;
import model.Ricetta;
import model.Sessione;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestioneSessioni {

	private final InPresenzaDAO inPresenzaDAO;
	private final OnlineDAO onlineDAO;

	private final CucinaDAO cucinaDAO;

	public GestioneSessioni(InPresenzaDAO inPresenzaDAO, OnlineDAO onlineDAO, CucinaDAO cucinaDAO) {
		this.inPresenzaDAO = inPresenzaDAO;
		this.onlineDAO = onlineDAO;
		this.cucinaDAO = cucinaDAO;
	}

	public void creaSessione(Sessione sessione) throws ValidationException, DataAccessException {
		ValidationUtils.validateNotNull(sessione, ErrorMessages.SESSIONE_NULLA);

		try {
			if (sessione instanceof InPresenza ip) {
				inPresenzaDAO.save(ip);
			} else if (sessione instanceof Online o) {
				onlineDAO.save(o);
			} else {
				throw new ValidationException(ErrorMessages.SOLO_SESSIONI_IN_PRESENZA);
			}
		} catch (SQLException e) {
			throw new DataAccessException(ErrorMessages.ERRORE_INSERIMENTO, e);
		}
	}

	public void rimuoviSessione(Sessione sessione) throws ValidationException, DataAccessException {
		ValidationUtils.validateNotNull(sessione, ErrorMessages.SESSIONE_NULLA);

		try {
			if (sessione instanceof InPresenza ip) {
				inPresenzaDAO.delete(ip.getIdSessione());
			} else if (sessione instanceof Online o) {
				onlineDAO.delete(o.getIdSessione());
			} else {
				throw new ValidationException(ErrorMessages.SESSIONE_NON_TROVATA);
			}
		} catch (SQLException e) {
			throw new DataAccessException("Errore durante la rimozione della sessione", e);
		}
	}


	public Map<Integer, Integer> getNumeroRicettePerSessioni(List<Integer> idSessioni) throws DataAccessException {
		if (idSessioni == null || idSessioni.isEmpty())
			return new HashMap<>();
		try {
			return cucinaDAO.getNumeroRicettePerSessioni(idSessioni);
		} catch (SQLException e) {
			throw new DataAccessException("Errore nel recupero del numero di ricette per le sessioni", e);
		}
	}

}
