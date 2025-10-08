package service;

import dao.CucinaDAO;
import exceptions.DataAccessException;
import exceptions.ValidationException;
import exceptions.ErrorMessages;
import model.InPresenza;
import model.Ricetta;
import model.Sessione;

import java.sql.SQLException;

public class GestioneCucina {

    private final CucinaDAO cucinaDAO;

    public GestioneCucina(CucinaDAO cucinaDAO) {
        this.cucinaDAO = cucinaDAO;
    }

    public void aggiungiSessioneARicetta(Ricetta ricetta, Sessione sessione)
            throws ValidationException, DataAccessException {

        if (ricetta == null) throw new ValidationException(ErrorMessages.RICETTA_NULLA);
        if (sessione == null) throw new ValidationException(ErrorMessages.SESSIONE_NON_TROVATA);

        if (!(sessione instanceof InPresenza)) {
            throw new ValidationException(ErrorMessages.SOLO_SESSIONI_IN_PRESENZA);
        }

        InPresenza ip = (InPresenza) sessione;

        try {
            if (ricetta.getSessioni().contains(ip)) {
                throw new ValidationException("La sessione è già associata a questa ricetta");
            }

            cucinaDAO.save(ricetta.getIdRicetta(), ip.getIdSessione());
            ricetta.getSessioni().add(ip);

        } catch (SQLException e) {
            throw new DataAccessException(ErrorMessages.ERRORE_INSERIMENTO, e);
        }
    }

    public void rimuoviSessioneDaRicetta(Ricetta ricetta, Sessione sessione)
            throws ValidationException, DataAccessException {

        if (ricetta == null) throw new ValidationException(ErrorMessages.RICETTA_NULLA);
        if (sessione == null) throw new ValidationException(ErrorMessages.SESSIONE_NON_TROVATA);

        if (!(sessione instanceof InPresenza)) {
            throw new ValidationException(ErrorMessages.SOLO_SESSIONI_IN_PRESENZA);
        }

        InPresenza ip = (InPresenza) sessione;

        try {
            if (!ricetta.getSessioni().remove(ip)) {
                throw new ValidationException("La sessione non è associata a questa ricetta");
            }

            cucinaDAO.delete(ricetta.getIdRicetta(), ip.getIdSessione());

        } catch (SQLException e) {
            throw new DataAccessException(ErrorMessages.ERRORE_ELIMINAZIONE, e);
        }
    }
}
