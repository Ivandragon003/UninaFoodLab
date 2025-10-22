package service;
1
import dao.CucinaDAO;
import dao.InPresenzaDAO;
import dao.OnlineDAO;
import exceptions.DataAccessException;
import exceptions.ValidationException;
import exceptions.ValidationUtils;
import exceptions.ErrorMessages;
import model.InPresenza;
import model.Online;
import model.Sessione;

import java.sql.SQLException;
import java.time.LocalDateTime;
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

    
    public void validaOrari(LocalDateTime inizio, LocalDateTime fine) throws ValidationException {
        ValidationUtils.validateNotNull(inizio, "Data inizio obbligatoria");
        ValidationUtils.validateNotNull(fine, "Data fine obbligatoria");
        
        if (!fine.isAfter(inizio)) {
            throw new ValidationException("L'ora di fine deve essere dopo l'inizio");
        }
    }

 
    public void validaSessioneInPresenza(InPresenza sessione) throws ValidationException {
        ValidationUtils.validateNotNull(sessione, ErrorMessages.SESSIONE_NULLA);
        
        if (sessione.getRicette() == null || sessione.getRicette().isEmpty()) {
            throw new ValidationException("Le sessioni in presenza richiedono almeno una ricetta");
        }
    }

    public void creaSessione(Sessione sessione) throws ValidationException, DataAccessException {
        ValidationUtils.validateNotNull(sessione, ErrorMessages.SESSIONE_NULLA);
        
       
        validaOrari(sessione.getDataInizioSessione(), sessione.getDataFineSessione());

        try {
            if (sessione instanceof InPresenza ip) {
                validaSessioneInPresenza(ip);  
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
