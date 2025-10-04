package service;

import dao.CorsoCucinaDAO;
import dao.ChefDAO;
import dao.TieneDAO;
import dao.IscrizioneDAO;
import dao.OnlineDAO;
import dao.InPresenzaDAO;

import exceptions.ValidationException;
import exceptions.ErrorMessages;

import model.CorsoCucina;
import model.Chef;
import model.Sessione;
import model.Iscrizione;
import model.Online;
import model.InPresenza;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    public void creaCorso(CorsoCucina corso) throws SQLException, ValidationException {
        if (corso == null) throw new ValidationException(ErrorMessages.CORSO_NULLO);
        if (corso.getNomeCorso() == null || corso.getNomeCorso().trim().isEmpty())
            throw new ValidationException(ErrorMessages.NOME_CORSO_MANCANTE);
        if (corso.getNumeroPosti() <= 0) throw new ValidationException(ErrorMessages.NUMERO_POSTI_NON_VALIDO);
        if (corso.getPrezzo() < 0) throw new ValidationException(ErrorMessages.PREZZO_NON_VALIDO);
        if (corso.getDataInizioCorso() == null || corso.getDataFineCorso() == null)
            throw new ValidationException(ErrorMessages.DATE_CORSO_MANCANTI);
        if (corso.getDataInizioCorso().isBefore(java.time.LocalDateTime.now()))
            throw new ValidationException(ErrorMessages.DATA_INIZIO_PASSATA);
        if (corso.getDataFineCorso().isBefore(corso.getDataInizioCorso()))
            throw new ValidationException(ErrorMessages.DATA_FINE_PRECEDENTE);

        corsoDAO.save(corso);

        if (corso.getSessioni() != null) {
            for (Sessione s : corso.getSessioni()) {
                if (s instanceof Online) {
                    Online o = (Online) s;
                    o.setCorsoCucina(corso);
                    onlineDAO.save(o);
                } else if (s instanceof InPresenza) {
                    InPresenza ip = (InPresenza) s;
                    ip.setCorsoCucina(corso);
                    inPresenzaDAO.save(ip);
                }
            }
        }

        if (corso.getChef() != null) {
            for (Chef c : corso.getChef()) {
                if (!chefDAO.findByCodFiscale(c.getCodFiscale()).isPresent()) {
                    throw new ValidationException(ErrorMessages.CHEF_NON_PRESENTE + c.getCodFiscale());
                }
                if (!tieneDAO.getChefByCorso(corso.getIdCorso()).contains(c)) {
                    tieneDAO.save(c.getCodFiscale(), corso.getIdCorso());
                }
            }
        }
    }

    public void aggiornaCorso(CorsoCucina corso) throws SQLException, ValidationException {
        if (corso == null) throw new ValidationException(ErrorMessages.CORSO_NULLO);
        corsoDAO.update(corso);
    }

    public void cancellaCorso(int idCorso) throws SQLException {
        corsoDAO.delete(idCorso);
    }

    public void aggiungiChefACorso(CorsoCucina corso, Chef chef, String password) throws SQLException, ValidationException {
        if (corso == null || chef == null) throw new ValidationException(ErrorMessages.CORSO_NULLO);
        if (password == null || password.length() < 6) throw new ValidationException(ErrorMessages.PASSWORD_NON_VALIDA);

        if (!chefDAO.findByCodFiscale(chef.getCodFiscale()).isPresent()) {
            chefDAO.save(chef, password);
        }

        if (!tieneDAO.getChefByCorso(corso.getIdCorso()).contains(chef)) {
            tieneDAO.save(chef.getCodFiscale(), corso.getIdCorso());
            corso.getChef().add(chef);
        } else {
            throw new ValidationException(ErrorMessages.CHEF_GIA_ASSEGNATO);
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
