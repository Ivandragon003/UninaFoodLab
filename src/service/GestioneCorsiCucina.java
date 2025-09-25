package service;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.sql.SQLException;
import dao.*;
import model.*;

public class GestioneCorsiCucina {

	private final CorsoCucinaDAO corsoDAO;
	private final ChefDAO chefDAO;
	private final TieneDAO tieneDAO;
	private final IscrizioneDAO iscrizioneDAO;
	private final OnlineDAO onlineDAO;
	private final InPresenzaDAO inPresenzaDAO;

	public GestioneCorsiCucina(CorsoCucinaDAO corsoDAO, ChefDAO chefDAO, TieneDAO tieneDAO, IscrizioneDAO iscrizioneDAO,
			OnlineDAO onlineDAO, InPresenzaDAO inPresenzaDAO) {
		this.corsoDAO = corsoDAO;
		this.chefDAO = chefDAO;
		this.tieneDAO = tieneDAO;
		this.iscrizioneDAO = iscrizioneDAO;
		this.onlineDAO = onlineDAO;
		this.inPresenzaDAO = inPresenzaDAO;
	}

	// Corsi
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

	public List<CorsoCucina> getAllCorsi() throws SQLException {
		return corsoDAO.getAll();
	}

	// Chef
	public void aggiungiChefACorso(CorsoCucina corso, Chef chef, String password) throws SQLException {
		if (!chefDAO.findByCodFiscale(chef.getCodFiscale()).isPresent()) {
			chefDAO.save(chef, password);
		}
		if (!tieneDAO.getChefByCorso(corso.getIdCorso()).contains(chef)) {
			tieneDAO.save(chef.getCodFiscale(), corso.getIdCorso());
			corso.getChef().add(chef);
		} else {
			throw new IllegalArgumentException("Chef già assegnato a questo corso");
		}
	}

	public void rimuoviChefDaCorso(CorsoCucina corso, Chef chef) throws SQLException {
		if (corso.getChef().remove(chef)) {
			tieneDAO.delete(chef.getCodFiscale(), corso.getIdCorso());
		}
	}

	public List<CorsoCucina> getCorsiByChef(Chef chef) throws SQLException {
		return tieneDAO.getCorsiByChef(chef.getCodFiscale());
		
	}
	
	public List<CorsoCucina> cercaPerNomeOCategoria(String filtro) throws SQLException {
	    return corsoDAO.findByNomeOrArgomento(filtro);
	}

	// Iscrizioni
	public void aggiungiIscrizione(CorsoCucina corso, Iscrizione iscrizione) throws SQLException {
		if (!corso.getIscrizioni().contains(iscrizione)) {
			iscrizioneDAO.save(iscrizione);
			corso.getIscrizioni().add(iscrizione);
		}
	}

	public void rimuoviIscrizione(CorsoCucina corso, Iscrizione iscrizione) throws SQLException {
		if (corso.getIscrizioni().remove(iscrizione)) {
			iscrizioneDAO.delete(iscrizione.getUtente().getCodFiscale(), corso.getIdCorso());
		}
	}

	public Set<Utente> getIscrittiAttivi(CorsoCucina corso) throws SQLException {
		return iscrizioneDAO.getIscrittiAttivi(corso.getIdCorso());
	}

	// Sessioni
	public void aggiungiSessione(CorsoCucina corso, Sessione sessione) throws SQLException {
		if (!corso.getSessioni().contains(sessione)) {
			if (sessione instanceof Online) {
				onlineDAO.save((Online) sessione);
			} else if (sessione instanceof InPresenza) {
				inPresenzaDAO.save((InPresenza) sessione);
			} else {
				throw new IllegalArgumentException("Tipo di sessione non gestito");
			}
			corso.getSessioni().add(sessione);
		}
	}

	public void rimuoviSessione(CorsoCucina corso, Sessione sessione) throws SQLException {
		if (corso.getSessioni().remove(sessione)) {
			if (sessione instanceof Online) {
				onlineDAO.delete(sessione.getIdSessione());
			} else if (sessione instanceof InPresenza) {
				inPresenzaDAO.delete(sessione.getIdSessione());
			}
		}
	}

	public List<Sessione> getSessioniByCorso(CorsoCucina corso) throws SQLException {
		return corso.getSessioni();
	}

	public CorsoCucina getCorsoCompleto(int idCorso) throws SQLException {

		CorsoCucina corso = corsoDAO.findById(idCorso).orElseThrow(() -> new SQLException("Corso non trovato"));

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

	public List<CorsoCucina> getTuttiICorsiCompleti() throws SQLException {
		List<CorsoCucina> corsiCompleti = new ArrayList<>();
		for (CorsoCucina corso : corsoDAO.getAll()) {
			List<Sessione> sessioni = new ArrayList<>();
			sessioni.addAll(onlineDAO.getByCorso(corso.getIdCorso()));
			sessioni.addAll(inPresenzaDAO.getByCorso(corso.getIdCorso()));
			corso.setSessioni(sessioni);

			List<Iscrizione> iscrizioni = iscrizioneDAO.getAllFull().stream()
					.filter(i -> i.getCorso().getIdCorso() == corso.getIdCorso()).toList();
			corso.setIscrizioni(iscrizioni);

			corso.setChef(tieneDAO.getChefByCorso(corso.getIdCorso()));

			corsiCompleti.add(corso);
		}
		return corsiCompleti;
	}
	
	public List<CorsoCucina> getCorsi() throws SQLException {
	    return corsoDAO.getAll();
	}
}
