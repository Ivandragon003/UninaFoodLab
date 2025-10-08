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

public class GestioneSessioni {

    private final InPresenzaDAO inPresenzaDAO;
    private final OnlineDAO onlineDAO;
    private final AdesioneDAO adesioneDAO;
    private final CucinaDAO cucinaDAO;

    public GestioneSessioni(InPresenzaDAO inPresenzaDAO, OnlineDAO onlineDAO,
                            AdesioneDAO adesioneDAO, CucinaDAO cucinaDAO) {
        this.inPresenzaDAO = inPresenzaDAO;
        this.onlineDAO = onlineDAO;
        this.adesioneDAO = adesioneDAO;
        this.cucinaDAO = cucinaDAO;
    }

    // --- CREAZIONE / RIMOZIONE SESSIONI ---
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
                adesioneDAO.deleteBySessione(ip.getIdSessione());
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

    // --- ASSOCIAZIONI RICETTA - SESSIONE ---
    public void aggiungiRicettaASessione(InPresenza sessione, Ricetta ricetta)
            throws ValidationException, DataAccessException {
        ValidationUtils.validateNotNull(sessione, ErrorMessages.SESSIONE_NULLA);
        ValidationUtils.validateNotNull(ricetta, ErrorMessages.RICETTA_NULLA);

        if (!sessione.getRicette().contains(ricetta)) {
            try {
                cucinaDAO.save(ricetta.getIdRicetta(), sessione.getIdSessione());
                sessione.getRicette().add(ricetta);
                ricetta.getSessioni().add(sessione);
            } catch (SQLException e) {
                throw new DataAccessException("Errore durante l'associazione ricetta-sessione", e);
            }
        } else {
            throw new ValidationException("Questa ricetta è già associata alla sessione");
        }
    }

    public void rimuoviRicettaDaSessione(InPresenza sessione, Ricetta ricetta)
            throws ValidationException, DataAccessException {
        ValidationUtils.validateNotNull(sessione, ErrorMessages.SESSIONE_NULLA);
        ValidationUtils.validateNotNull(ricetta, ErrorMessages.RICETTA_NULLA);

        if (sessione.getRicette().contains(ricetta)) {
            try {
                cucinaDAO.delete(ricetta.getIdRicetta(), sessione.getIdSessione());
                sessione.getRicette().remove(ricetta);
                ricetta.getSessioni().remove(sessione);
            } catch (SQLException e) {
                throw new DataAccessException("Errore durante la rimozione della ricetta dalla sessione", e);
            }
        } else {
            throw new ValidationException(ErrorMessages.SESSIONE_NON_TROVATA);
        }
    }

    // --- METODI DI SUPPORTO ---
    public int getNumeroRicettePerSessione(int idSessione) throws DataAccessException {
        try {
            return cucinaDAO.getNumeroRicettePerSessione(idSessione);
        } catch (SQLException e) {
            throw new DataAccessException("Errore nel recupero del numero di ricette per sessione", e);
        }
    }
}
