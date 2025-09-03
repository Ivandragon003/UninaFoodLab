package model;

import java.time.LocalDateTime;

public class CorsoCucina {

	private String idCorso;
	private String nomeCorso;
	private double prezzo;
	private String categoria;
	private Frequenza frequenzaCorso;
	private int numeroPosti;
	private int numeroSessioni;
	private LocalDateTime dataInizioCorso;
	private LocalDateTime dataFineCorso;

	// COSTRUTTORE CLASSE
	public CorsoCucina(String idCorso, String nomeCorso, double prezzo, String categoria, Frequenza frequenzaCorso,
			int numeroPosti, int numeroSessioni, LocalDateTime dataInizioCorso, LocalDateTime dataFineCorso) {
		setIdCorso(idCorso);
		setNomeCorso(nomeCorso);
		setPrezzo(prezzo);
		setCategoria(categoria);
		setFrequenzaCorso(frequenzaCorso);
		setNumeroPosti(numeroPosti);
		setNumeroSessioni(numeroSessioni);
		setDataInizioCorso(dataInizioCorso);
		setDataFineCorso(dataFineCorso);
	}

	public String getIdCorso() {
		return idCorso;
	}

	public void setIdCorso(String idCorso) {
		if (idCorso == null || idCorso.trim().isEmpty()) {
			throw new IllegalArgumentException("ID corso non può essere vuoto");
		}
		this.idCorso = idCorso.trim();
	}

	public String getNomeCorso() {
		return nomeCorso;
	}

	public void setNomeCorso(String nomeCorso) {
		if (nomeCorso == null || nomeCorso.trim().isEmpty()) {
			throw new IllegalArgumentException("Nome corso non può essere vuoto");
		}
		this.nomeCorso = nomeCorso.trim();
	}

	public double getPrezzo() {
		return prezzo;
	}

	public void setPrezzo(double prezzo) {
		if (prezzo < 0) {
			throw new IllegalArgumentException("Il prezzo non può essere negativo");
		}
		this.prezzo = prezzo;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		if (categoria == null || categoria.trim().isEmpty()) {
			throw new IllegalArgumentException("Categoria non può essere vuota");
		}
		this.categoria = categoria.trim();
	}

	public Frequenza getFrequenzaCorso() {
		return frequenzaCorso;
	}

	public void setFrequenzaCorso(Frequenza frequenzaCorso) {
		if (frequenzaCorso == null) {
			throw new IllegalArgumentException("Frequenza corso non può essere null");
		}
		this.frequenzaCorso = frequenzaCorso;
	}

	public int getNumeroPosti() {
		return numeroPosti;
	}

	public void setNumeroPosti(int numeroPosti) {
		if (numeroPosti <= 0) {
			throw new IllegalArgumentException("Il numero di posti deve essere maggiore di zero");
		}
		this.numeroPosti = numeroPosti;
	}

	public int getNumeroSessioni() {
		return numeroSessioni;
	}

	public void setNumeroSessioni(int numeroSessioni) {
		if (numeroSessioni <= 0) {
			throw new IllegalArgumentException("Il numero di sessioni deve essere maggiore di zero");
		}
		this.numeroSessioni = numeroSessioni;
	}

	public LocalDateTime getDataInizioCorso() {
		return dataInizioCorso;
	}

	public void setDataInizioCorso(LocalDateTime dataInizioCorso) {
		if (dataInizioCorso == null) {
			throw new IllegalArgumentException("Data inizio corso non può essere null");
		}
		if (dataInizioCorso.isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("Data inizio corso non può essere nel passato");
		}
		this.dataInizioCorso = dataInizioCorso;
	}

	public LocalDateTime getDataFineCorso() {
		return dataFineCorso;
	}

	public void setDataFineCorso(LocalDateTime dataFineCorso) {
		if (dataFineCorso == null) {
			throw new IllegalArgumentException("Data fine corso non può essere null");
		}
		if (this.dataInizioCorso != null && dataFineCorso.isBefore(this.dataInizioCorso)) {
			throw new IllegalArgumentException("Data fine corso deve essere dopo la data di inizio");
		}
		this.dataFineCorso = dataFineCorso;
	}
	
	public String toStringIdCorso() {
	    return "ID Corso: " + idCorso;
	}

	public String toStringNomeCorso() {
	    return "Nome Corso: " + nomeCorso;
	}

	public String toStringPrezzo() {
	    return "Prezzo: " + prezzo;
	}

	public String toStringCategoria() {
	    return "Categoria: " + categoria;
	}

	public String toStringFrequenza() {
	    return "Frequenza: " + frequenzaCorso;
	}

	public String toStringNumeroPosti() {
	    return "Numero Posti: " + numeroPosti;
	}

	public String toStringNumeroSessioni() {
	    return "Numero Sessioni: " + numeroSessioni;
	}

	public String toStringDataInizio() {
	    return "Data Inizio Corso: " + dataInizioCorso;
	}

	public String toStringDataFine() {
	    return "Data Fine Corso: " + dataFineCorso;
	}
}