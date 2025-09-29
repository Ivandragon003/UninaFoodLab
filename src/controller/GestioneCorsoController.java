package controller;

import service.GestioneCorsiCucina;
import service.GestioneChef;
import model.CorsoCucina;
import model.Chef;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GestioneCorsoController {

	private final GestioneCorsiCucina corsiService;
	private final GestioneChef chefService;
	private Chef chefLoggato;

	public GestioneCorsoController(GestioneCorsiCucina corsiService, GestioneChef chefService) {
		this.corsiService = corsiService;
		this.chefService = chefService;
	}

	public void setChefLoggato(Chef chef) {
		this.chefLoggato = chef;
	}

	public Chef getChefLoggato() {
		return chefLoggato;
	}

	public List<Chef> getTuttiGliChef() throws SQLException {
		return chefService.getAll();
	}

	public void creaCorso(CorsoCucina corso) throws SQLException {
		corsiService.creaCorso(corso);
	}

	public void aggiungiChefACorso(CorsoCucina corso, Chef chef, String password) throws SQLException {
		corsiService.aggiungiChefACorso(corso, chef, password);
	}

	public void eliminaCorso(int idCorso) throws SQLException {
		corsiService.cancellaCorso(idCorso);
	}

	public void modificaCorso(CorsoCucina corsoAggiornato) throws SQLException {
		corsiService.aggiornaCorso(corsoAggiornato);
	}

	public int contaSessioniCorso(CorsoCucina corso) {
		return (corso != null && corso.getSessioni() != null) ? corso.getSessioni().size() : 0;
	}

	public List<String> getDescrizioniSessioni(CorsoCucina corso) {
		List<String> out = new ArrayList<>();
		if (corso == null || corso.getSessioni() == null || corso.getSessioni().isEmpty()) {
			return out;
		}
		int i = 1;
		for (var s : corso.getSessioni()) {
			String data = (s.getDataInizioSessione() != null) ? s.getDataInizioSessione().toLocalDate().toString()
					: "s.t.";
			String tipo = s.getClass().getSimpleName();
			out.add("Sessione " + i++ + " (" + tipo + ") - " + data);
		}
		return out;
	}

	public CorsoCucina getCorsoCompleto(int idCorso) throws SQLException {
		return corsiService.getCorsoCompleto(idCorso);
	}

}
