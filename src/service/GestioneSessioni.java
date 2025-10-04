package service;

import dao.AdesioneDAO;
import dao.InPresenzaDAO;
import dao.OnlineDAO;
import dao.CucinaDAO;
import model.*;
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

    public void creaSessione(Sessione sessione) throws SQLException {
        if (sessione instanceof InPresenza) {
            inPresenzaDAO.save((InPresenza) sessione);
        } else if (sessione instanceof Online) {
            onlineDAO.save((Online) sessione);
        } else {
            throw new IllegalArgumentException("Tipo di sessione non gestito");
        }
    }

    public void rimuoviSessione(Sessione sessione) throws SQLException {
        if (sessione instanceof InPresenza) {
            adesioneDAO.deleteBySessione(sessione.getIdSessione());
            inPresenzaDAO.delete(sessione.getIdSessione());
        } else if (sessione instanceof Online) {
            onlineDAO.delete(sessione.getIdSessione());
        }
    }

    public void aggiungiRicettaASessione(InPresenza sessione, Ricetta ricetta) throws SQLException {
        if (sessione == null || ricetta == null) {
            throw new IllegalArgumentException("Sessione e ricetta non possono essere null");
        }

        if (!sessione.getRicette().contains(ricetta)) {
            try {
                cucinaDAO.save(ricetta.getIdRicetta(), sessione.getIdSessione());
                
                sessione.getRicette().add(ricetta);
                ricetta.getSessioni().add(sessione);
                
            } catch (SQLException e) {
                throw new SQLException("Errore durante il salvataggio della relazione sessione-ricetta nel database", e);
            }
        }
    }

    public void rimuoviRicettaDaSessione(InPresenza sessione, Ricetta ricetta) throws SQLException {
        if (sessione == null || ricetta == null) {
            throw new IllegalArgumentException("Sessione e ricetta non possono essere null");
        }

        if (sessione.getRicette().contains(ricetta)) {
            try {
                cucinaDAO.delete(ricetta.getIdRicetta(), sessione.getIdSessione());
                
                sessione.getRicette().remove(ricetta);
                ricetta.getSessioni().remove(sessione);
                
            } catch (SQLException e) {
                throw new SQLException("Errore durante la rimozione della relazione sessione-ricetta dal database", e);
            }
        }
    }

    public int getNumeroRicettePerSessione(int idSessione) throws SQLException {
        return cucinaDAO.getNumeroRicettePerSessione(idSessione);
    }
}
