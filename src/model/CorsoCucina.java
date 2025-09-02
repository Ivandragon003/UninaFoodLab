package model;

import java.time.LocalDateTime;

public class CorsoCucina {

	private String idCorso;
	private String nomeCorso;
	private double prezzo;
	private String categoria;
	
	private enum Frequenza {
		GIORNALIERO("Una lezione ogni giorno"),
		SETTIMANALE("Una lezione a settimana"),
		MENSILE("Una lezione al mese");
		
		private String descrizione;
		
		//COSTRUTTORE
		private Frequenza(String descrizione) {
			this.descrizione = descrizione;		
		}
		
		private String getDescrizione() {
			return descrizione;
		}
	}

	private Frequenza frequenzaCorso;

	private int numeroPosti;
	private int numeroSessioni;
	private LocalDateTime dataInizioCorso;
	private LocalDateTime dataFineCorso;



	public CorsoCucina(String idCorso, String nomeCorso, double prezzo, String categoria, Frequenza frequenzaCorso,
			int numeroPosti, int numeroSessioni, LocalDateTime dataInizioCorso, LocalDateTime dataFineCorso) {
		super();
		this.idCorso = idCorso;
		this.nomeCorso = nomeCorso;
		this.prezzo = prezzo;
		this.categoria = categoria;
		this.frequenzaCorso = frequenzaCorso;
		this.numeroPosti = numeroPosti;
		this.numeroSessioni = numeroSessioni;
		this.dataInizioCorso = dataInizioCorso;
		this.dataFineCorso = dataFineCorso;
	}


	public String getIdCorso() {
		return idCorso;
	}
	public void setIdCorso(String idCorso) {
		this.idCorso = idCorso;
	}
	public String getNomeCorso() {
		return nomeCorso;
	}
	public void setNomeCorso(String nomeCorso) {
		this.nomeCorso = nomeCorso;
	}
	public double getPrezzo() {
		return prezzo;
	}
	public void setPrezzo(double prezzo) {
		this.prezzo = prezzo;
	}
	public String getCategoria() {
		return categoria;
	}
	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}
	public int getNumeroPosti() {
		return numeroPosti;
	}
	public void setNumeroPosti(int numeroPosti) {
		this.numeroPosti = numeroPosti;
	}
	public int getNumeroSessioni() {
		return numeroSessioni;
	}
	public void setNumeroSessioni(int numeroSessioni) {
		this.numeroSessioni = numeroSessioni;
	}
	public LocalDateTime getDataInizioCorso() {
		return dataInizioCorso;
	}
	public void setDataInizioCorso(LocalDateTime dataInizioCorso) {
		this.dataInizioCorso = dataInizioCorso;
	}
	public LocalDateTime getDataFineCorso() {
		return dataFineCorso;
	}
	public void setDataFineCorso(LocalDateTime dataFineCorso) {
		this.dataFineCorso = dataFineCorso;
	}

}