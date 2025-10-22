package controller;

import dao.*;
import exceptions.DataAccessException;
import model.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VisualizzaCorsiController {

    private final CorsoCucinaDAO corsoDAO;
    private final TieneDAO tieneDAO;
    private final IscrizioneDAO iscrizioneDAO;
    private final OnlineDAO onlineDAO;
    private final InPresenzaDAO inPresenzaDAO;
    private final Chef chefLoggato;

    public VisualizzaCorsiController(CorsoCucinaDAO corsoDAO, TieneDAO tieneDAO, IscrizioneDAO iscrizioneDAO,
            OnlineDAO onlineDAO, InPresenzaDAO inPresenzaDAO, Chef chefLoggato) {
        this.corsoDAO = corsoDAO;
        this.tieneDAO = tieneDAO;
        this.iscrizioneDAO = iscrizioneDAO;
        this.onlineDAO = onlineDAO;
        this.inPresenzaDAO = inPresenzaDAO;
        this.chefLoggato = chefLoggato;
    }

    public Chef getChefLoggato() {
        return chefLoggato;
    }

    public List<CorsoCucina> getTuttiICorsi() throws DataAccessException {
        try {
            return corsoDAO.getAll();
        } catch (SQLException e) {
            throw new DataAccessException("Impossibile leggere i dati", e);
        }
    }

    public List<CorsoCucina> getCorsiDelChef() throws DataAccessException {
        if (chefLoggato == null) {
            return Collections.emptyList();
        }
        try {
            return tieneDAO.getCorsiByChef(chefLoggato.getCodFiscale());
        } catch (SQLException e) {
            throw new DataAccessException("Impossibile leggere i dati", e);
        }
    }

    public List<CorsoCucina> visualizzaCorsiChef() throws DataAccessException {
        return getCorsiDelChef();
    }

    public List<CorsoCucina> cercaPerNomeOCategoria(String filtro) throws DataAccessException {
        try {
            return corsoDAO.findByNomeOrArgomento(filtro);
        } catch (SQLException e) {
            throw new DataAccessException("Impossibile leggere i dati", e);
        }
    }

    public int getNumeroSessioniPerCorso(int idCorso) throws DataAccessException {
        try {
            return corsoDAO.getNumeroSessioniPerCorso(idCorso);
        } catch (SQLException e) {
            throw new DataAccessException("Impossibile leggere i dati", e);
        }
    }

    public CorsoCucina getCorsoCompleto(int idCorso) throws DataAccessException {
        try {
            CorsoCucina corso = corsoDAO.findById(idCorso)
                    .orElseThrow(() -> new DataAccessException("Corso non trovato nel sistema"));

            List<Sessione> sessioni = new ArrayList<>();
            sessioni.addAll(onlineDAO.getByCorso(idCorso));
            sessioni.addAll(inPresenzaDAO.getByCorso(idCorso));
            corso.setSessioni(sessioni);

            corso.setIscrizioni(iscrizioneDAO.getAllFull().stream()
                    .filter(i -> i.getCorso().getIdCorso() == idCorso).toList());

            corso.setChef(tieneDAO.getChefByCorso(idCorso));

            return corso;
        } catch (SQLException e) {
            throw new DataAccessException("Impossibile leggere i dati", e);
        }
    }

    public List<Sessione> getSessioniPerCorsoInPeriodo(int idCorso, LocalDate inizio, LocalDate fine)
            throws DataAccessException {
        try {
            List<Sessione> sessioni = new ArrayList<>();
            sessioni.addAll(onlineDAO.getByCorso(idCorso));
            sessioni.addAll(inPresenzaDAO.getByCorso(idCorso));

            return sessioni.stream().filter(s -> {
                LocalDate d = s.getDataInizioSessione().toLocalDate();
                return !d.isBefore(inizio) && !d.isAfter(fine);
            }).toList();
        } catch (SQLException e) {
            throw new DataAccessException("Impossibile leggere i dati", e);
        }
    }

    public void visualizzaCorsi(List<CorsoCucina> corsi) {
        corsi.forEach(c -> System.out.println(c.toStringNomeCorso() + " | ID: " + c.getIdCorso()));
    }
}
