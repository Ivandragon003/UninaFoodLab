package service;

import dao.LavoraDAO;
import dao.RistoranteDAO;
import model.Chef;
import model.Ristorante;


import java.sql.SQLException;

public class GestioneRistorante {

	private final LavoraDAO lavoraDAO;
	private final RistoranteDAO ristoranteDAO;

	public GestioneRistorante(LavoraDAO lavoraDAO, RistoranteDAO ristoranteDAO) {
		this.lavoraDAO = lavoraDAO;
		this.ristoranteDAO = ristoranteDAO;
	}

	public int creaRistorante(Ristorante ristorante) throws SQLException {
		if (ristorante.getNome() == null || ristorante.getNome().isEmpty()) {
			throw new IllegalArgumentException("Nome ristorante non valido");
		}
		return ristoranteDAO.save(ristorante);
	}

	public void aggiornaRistorante(int id, Ristorante ristorante) throws SQLException {

		if (ristorante.getNome() == null || ristorante.getNome().isEmpty()) {
			throw new IllegalArgumentException("Nome ristorante non valido");
		}
		ristoranteDAO.update(id, ristorante);
	}

	public void cancellaRistorante(int id) throws SQLException {
		lavoraDAO.deleteByRistorante(id);
		ristoranteDAO.delete(id);
	}

	public void aggiungiChefARistorante(Chef chef, Ristorante ristorante) throws SQLException {
	    if (!ristorante.getChef().contains(chef)) {
	        ristorante.getChef().add(chef);
	        chef.getRistoranti().add(ristorante);
	        lavoraDAO.addChefToRistorante(chef.getCodFiscale(), ristorante.getIdRistorante());
	    } else {
	        throw new IllegalArgumentException("Chef già lavora in questo ristorante");
	    }
	}

	public void rimuoviChefDaRistorante(Chef chef, Ristorante ristorante) throws SQLException {
	    if (ristorante.getChef().contains(chef)) {
	    	ristorante.getChef().remove(chef);
	        chef.getRistoranti().remove(ristorante);
	        lavoraDAO.removeChefFromRistorante(chef.getCodFiscale(), ristorante.getIdRistorante());
	    } else {
	        throw new IllegalArgumentException("Chef non lavora in questo ristorante");
	    }
	}

}
