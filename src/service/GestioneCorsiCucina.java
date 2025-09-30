package service;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import dao.*;
import model.*;

public class GestioneCorsiCucina {

    private final CorsoCucinaDAO corsoDAO;
    private final ChefDAO chefDAO;
    private final TieneDAO tieneDAO;
    private final IscrizioneDAO iscrizioneDAO;
    private final OnlineDAO onlineDAO;
    private final InPresenzaDAO inPresenzaDAO;

    public GestioneCorsiCucina(CorsoCucinaDAO corsoDAO, ChefDAO chefDAO, TieneDAO tieneDAO,
                               IscrizioneDAO iscrizioneDAO, OnlineDAO onlineDAO, InPresenzaDAO inPresenzaDAO) {
        this.corsoDAO = corsoDAO;
        this.chefDAO = chefDAO;
        this.tieneDAO = tieneDAO;
        this.iscrizioneDAO = iscrizioneDAO;
        this.onlineDAO = onlineDAO;
        this.inPresenzaDAO = inPresenzaDAO;
    }

    public void creaCorso(CorsoCucina corso) throws SQLException {
        corsoDAO.save(corso);
    }

    public void aggiornaCorso(CorsoCucina corso) throws SQLException {
        corsoDAO.update(corso);
    }

    public void cancellaCorso(int idCorso) throws SQLException {
        tieneDAO.deleteByCorso(idCorso);
        corsoDAO.delete(idCorso);
    }

    public void aggiungiChefACorso(CorsoCucina corso, Chef chef, String password) throws SQLException {
        if (!chefDAO.findByCodFiscale(chef.getCodFiscale()).isPresent()) {
            chefDAO.save(chef, password);
        }
        if (!tieneDAO.getChefByCorso(corso.getIdCorso()).contains(chef)) {
            tieneDAO.save(chef.getCodFiscale(), corso.getIdCorso());
            corso.getChef().add(chef);
        } else {
            throw new SQLException("Chef già assegnato a questo corso");
        }
    }

    public void rimuoviChefDaCorso(CorsoCucina corso, Chef chef) throws SQLException {
        if (corso.getChef().remove(chef)) {
            tieneDAO.delete(chef.getCodFiscale(), corso.getIdCorso());
        }
    }

    public CorsoCucina getCorsoCompleto(int idCorso) throws SQLException {
        CorsoCucina corso = corsoDAO.findById(idCorso)
                .orElseThrow(() -> new SQLException("Corso non trovato"));

        List<Sessione> sessioni = new ArrayList<>();
        sessioni.addAll(onlineDAO.getByCorso(idCorso));
        sessioni.addAll(inPresenzaDAO.getByCorso(idCorso));
        corso.setSessioni(sessioni);

        List<Iscrizione> iscrizioni = iscrizioneDAO.getAllFull().stream()
                .filter(i -> i.getCorso().getIdCorso() == idCorso).toList();
        corso.setIscrizioni(iscrizioni);

        corso.setChef(tieneDAO.getChefByCorso(idCorso));

        return corso;
    }

    public List<CorsoCucina> getCorsi() throws SQLException {
        return corsoDAO.getAll();
    }

    public List<CorsoCucina> cercaPerNomeOCategoria(String filtro) throws SQLException {
        return corsoDAO.findByNomeOrArgomento(filtro);
    }

    public int getNumeroSessioniPerCorso(int idCorso) throws SQLException {
        return corsoDAO.getNumeroSessioniPerCorso(idCorso);
    }
}
