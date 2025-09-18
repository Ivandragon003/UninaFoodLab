package service;

import dao.IscrizioneDAO;
import model.CorsoCucina;
import model.Iscrizione;
import model.Utente;

import java.sql.SQLException;


public class GestioneIscrizione {

    private final IscrizioneDAO iscrizioneDAO;

    public GestioneIscrizione(IscrizioneDAO iscrizioneDAO) {
        this.iscrizioneDAO = iscrizioneDAO;
    }

    public void aggiungiIscrizione(Utente utente, CorsoCucina corso) throws SQLException {
        Iscrizione iscrizione = new Iscrizione(utente, corso, true);
        if (!utente.getIscrizioni().contains(iscrizione)) {  // evita duplicati
            utente.getIscrizioni().add(iscrizione);
            corso.getIscrizioni().add(iscrizione);
            iscrizioneDAO.save(iscrizione);
        } else {
            throw new IllegalArgumentException("Utente già iscritto a questo corso");
        }
    }

    public void rimuoviIscrizione(Utente utente, CorsoCucina corso) throws SQLException {
        Iscrizione temp = new Iscrizione(utente, corso, true);
        if (utente.getIscrizioni().remove(temp)) {
            corso.getIscrizioni().remove(temp);
            iscrizioneDAO.delete(utente.getCodFiscale(), corso.getIdCorso());
        } else {
            throw new IllegalArgumentException("Iscrizione non trovata");
        }
    }

}
