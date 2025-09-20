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
	// opzionale: tieni lo chef loggato se vuoi che la GUI possa richiederlo
	private Chef chefLoggato;

	public GestioneCorsoController(GestioneCorsiCucina corsiService, GestioneChef chefService) {
		this.corsiService = corsiService;
		this.chefService = chefService;
	}

	// se vuoi impostare lo chef loggato dalla login
	public void setChefLoggato(Chef chef) {
		this.chefLoggato = chef;
	}

	public Chef getChefLoggato() {
		return chefLoggato;
	}

	// Ritorna la lista di chef disponibili tramite il service
	public List<Chef> getTuttiGliChef() throws SQLException {
		return chefService.getAll();
	}

	// Salva (crea) un corso tramite il service
	public void creaCorso(CorsoCucina corso) throws SQLException {
		corsiService.creaCorso(corso);
	}

	// Associa un chef al corso 
	public void aggiungiChefACorso(CorsoCucina corso, Chef chef, String password) throws SQLException {
		corsiService.aggiungiChefACorso(corso, chef, password);
	}

	// Rimuovi corso 
	public void eliminaCorso(int idCorso) throws SQLException {
		corsiService.cancellaCorso(idCorso);
	}

	public void modificaCorso(CorsoCucina corsoAggiornato) throws SQLException {
		corsiService.aggiornaCorso(corsoAggiornato);
	}

	// Conta sessioni direttamente dall'oggetto corso (se caricato)
	public int contaSessioniCorso(CorsoCucina corso) {
		if (corso == null || corso.getSessioni() == null)
			return 0;
		return corso.getSessioni().size();
	}

	// Costruisce descrizioni semplici per la GUI dalle sessioni del corso
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
}
