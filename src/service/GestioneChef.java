package service;

import java.sql.SQLException;
import java.util.Optional;
import dao.ChefDAO;
import dao.TieneDAO;
import model.Chef;
import model.CorsoCucina;
import java.util.List;

public class GestioneChef {
	private final TieneDAO tieneDAO;
	private final ChefDAO chefDAO;


	public GestioneChef(ChefDAO chefDAO, TieneDAO tieneDAO) {
		this.chefDAO = chefDAO;
		this.tieneDAO = tieneDAO;
	}

	public void creaChef(Chef chef) throws SQLException {
		if (chefDAO.findByCodFiscale(chef.getCodFiscale()).isPresent()) {
			throw new IllegalArgumentException("Chef con questo codice fiscale già esistente");
		}
		chefDAO.save(chef, chef.getPassword());
	}

	public boolean existsByCodFiscale(String codFiscale) throws SQLException {
		return chefDAO.existsByCodFiscale(codFiscale);
	}

	public void aggiornaChef(Chef chef) throws SQLException {
		if (chefDAO.findByUsername(chef.getUsername()).isEmpty()) {
			throw new IllegalArgumentException("Chef non trovato");
		}
		chefDAO.update(chef, chef.getPassword());
	}

	public void eliminaChef(String username) throws SQLException {
		Optional<Chef> c = chefDAO.findByUsername(username);
		if (c.isEmpty())
			throw new IllegalArgumentException("Chef non trovato");
		chefDAO.delete(c.get().getCodFiscale());
	}

	public List<Chef> getAll() throws SQLException {
		return chefDAO.getAll();
	}

	public Chef getChefByUsername(String username) throws SQLException {
		Optional<Chef> chef = chefDAO.findByUsername(username);
		return chef.orElse(null);
	}

	public boolean existsByEmail(String email) throws SQLException {
		return chefDAO.existsByEmail(email);
	}

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