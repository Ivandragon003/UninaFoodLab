package controller;

import model.CorsoCucina;
import model.Chef;
import service.GestioneCorsiCucina;
import service.GestioneChef;

import java.sql.SQLException;
import java.util.List;

public class CorsiController {

	private final GestioneCorsiCucina corsiService;
	private final GestioneChef chefService;
	private final Chef chefLoggato;

	public CorsiController(GestioneCorsiCucina corsiService, GestioneChef chefService, Chef chefLoggato) {
		this.corsiService = corsiService;
		this.chefService = chefService;
		this.chefLoggato = chefLoggato;
	}

	public GestioneCorsiCucina getGestioneCorsi() {
		return corsiService;
	}

	public GestioneChef getChefService() {
		return chefService;
	}

	public Chef getChefLoggato() {
		return chefLoggato;
	}

	public void visualizzaCorsi(List<CorsoCucina> corsi) {
		corsi.forEach(c -> System.out.println(c.toStringNomeCorso() + " | ID: " + c.getIdCorso()));
	}
	
	public void eliminaAccount() {
		try {
			chefService.eliminaChef(chefLoggato.getUsername());
			System.out.println("Account eliminato correttamente!");
		} catch (SQLException | IllegalArgumentException e) {
			System.err.println("Errore nell'eliminazione dell'account: " + e.getMessage());
		}
	}
	
	
}
