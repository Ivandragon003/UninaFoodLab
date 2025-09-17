package service;

import dao.LavoraDAO;
import dao.TieneDAO;
import model.Chef;
import model.Ristorante;
import model.Lavora;
import model.CorsoCucina;

import java.sql.SQLException;

public class GestioneChef {

	private final LavoraDAO lavoraDAO;
	private final TieneDAO tieneDAO;

	public GestioneChef(LavoraDAO lavoraDAO, TieneDAO tieneDAO) {

		this.lavoraDAO = lavoraDAO;
		this.tieneDAO = tieneDAO;
	}

	// Ristoranti

	public void aggiungiRistorante(Chef chef, Ristorante ristorante) throws SQLException {
		if (!chef.getRistoranti().contains(ristorante)) {
			chef.getRistoranti().add(ristorante);
			ristorante.getChef().add(chef);
			lavoraDAO.save(new Lavora(chef, ristorante));
		} else {
			throw new IllegalArgumentException("Ristorante già associato allo chef");
		}
	}

	public void rimuoviRistorante(Chef chef, Ristorante ristorante) throws SQLException {
		if (chef.getRistoranti().remove(ristorante)) {
			ristorante.getChef().remove(chef);
			lavoraDAO.delete(chef.getCodFiscale(), ristorante.getIdRistorante());
		} else {
			throw new IllegalArgumentException("Ristorante non associato allo chef");
		}
	}

	// Corsi di cucina

	public void aggiungiCorso(Chef chef, CorsoCucina corso) throws SQLException {
		if (!chef.getCorsi().contains(corso)) {
			chef.getCorsi().add(corso);
			corso.getChef().add(chef);
			tieneDAO.save(chef.getCodFiscale(), corso.getIdCorso());
		} else {
			throw new IllegalArgumentException("Chef già insegna questo corso");
		}
	}

	public void rimuoviCorso(Chef chef, CorsoCucina corso) throws SQLException {
		if (chef.getCorsi().remove(corso)) {
			corso.getChef().remove(chef);
			tieneDAO.delete(chef.getCodFiscale(), corso.getIdCorso());
		} else {
			throw new IllegalArgumentException("Chef non insegna questo corso");
		}
	}
}
