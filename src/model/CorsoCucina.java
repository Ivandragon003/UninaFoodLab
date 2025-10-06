package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CorsoCucina {

	private int idCorso;
	private String nomeCorso;
	private double prezzo;
	private String argomento;
	private Frequenza frequenzaCorso;
	private int numeroPosti;
	private int numeroSessioni;
	private LocalDateTime dataInizioCorso;
	private LocalDateTime dataFineCorso;

	private List<Chef> chef = new ArrayList<>();
	private List<Iscrizione> iscrizioni = new ArrayList<>();
	private List<Sessione> sessioni = new ArrayList<>();

	public CorsoCucina(String nomeCorso, double prezzo, String argomento, Frequenza frequenzaCorso, int numeroPosti) {
		setNomeCorso(nomeCorso);
		setPrezzo(prezzo);
		setArgomento(argomento);
		setFrequenzaCorso(frequenzaCorso);
		setNumeroPosti(numeroPosti);
	}

	public void setChef(List<Chef> chef) {
		this.chef = chef;
	}

	public void setSessioni(List<Sessione> sessioni) {
		this.sessioni = sessioni;
	}

	public void setIscrizioni(List<Iscrizione> iscrizioni) {
		this.iscrizioni = iscrizioni;
	}

	public int getIdCorso() {
		return idCorso;
	}

	public void setIdCorso(int idCorso) {
		this.idCorso = idCorso;
	}

	public List<Sessione> getSessioni() {
		return sessioni;
	}

	public String getNomeCorso() {
		return nomeCorso;
	}

	public void setNomeCorso(String nomeCorso) {
		if (nomeCorso == null || nomeCorso.trim().isEmpty())
			throw new IllegalArgumentException("Nome corso non può essere vuoto");
		this.nomeCorso = nomeCorso.trim();
	}

	public double getPrezzo() {
		return prezzo;
	}

	public void setPrezzo(double prezzo) {
		if (prezzo < 0)
			throw new IllegalArgumentException("Il prezzo non può essere negativo");
		this.prezzo = prezzo;
	}

	public String getArgomento() {
		return argomento;
	}

	public void setArgomento(String argomento) {
		if (argomento == null || argomento.trim().isEmpty())
			throw new IllegalArgumentException("Categoria non può essere vuota");
		this.argomento = argomento.trim();
	}

	public Frequenza getFrequenzaCorso() {
		return frequenzaCorso;
	}

	public void setFrequenzaCorso(Frequenza frequenzaCorso) {
		if (frequenzaCorso == null)
			throw new IllegalArgumentException("Frequenza corso non può essere null");
		this.frequenzaCorso = frequenzaCorso;
	}

	public int getNumeroPosti() {
		return numeroPosti;
	}

	public void setNumeroPosti(int numeroPosti) {
		if (numeroPosti <= 0)
			throw new IllegalArgumentException("Il numero di posti deve essere maggiore di zero");
		this.numeroPosti = numeroPosti;
	}

	public int getNumeroSessioni() {
		return numeroSessioni;
	}

	public void setNumeroSessioni(int numeroSessioni) {
	    if (numeroSessioni < 0)
	        throw new IllegalArgumentException("Il numero di sessioni non può essere negativo");
	    this.numeroSessioni = numeroSessioni;
	}


	public List<Chef> getChef() {
		return chef;
	}

	public LocalDateTime getDataInizioCorso() {
		return dataInizioCorso;
	}

	public void setDataInizioCorso(LocalDateTime dataInizioCorso) {
		if (dataInizioCorso == null)
			throw new IllegalArgumentException("Data inizio corso non può essere null");
		if (dataInizioCorso.isBefore(LocalDateTime.now()))
			throw new IllegalArgumentException("Data inizio corso non può essere nel passato");
		this.dataInizioCorso = dataInizioCorso;
	}

	public LocalDateTime getDataFineCorso() {
		return dataFineCorso;
	}

	public void setDataFineCorso(LocalDateTime dataFineCorso) {
		if (dataFineCorso == null)
			throw new IllegalArgumentException("Data fine corso non può essere null");
		if (this.dataInizioCorso != null && dataFineCorso.isBefore(this.dataInizioCorso))
			throw new IllegalArgumentException("Data fine corso deve essere dopo la data di inizio");
		this.dataFineCorso = dataFineCorso;
	}

	public List<Iscrizione> getIscrizioni() {
		return iscrizioni;
	}

	// usato solo per il dao
	public void setDataInizioCorsoFromDB(LocalDateTime dataInizioCorso) {
		this.dataInizioCorso = dataInizioCorso;
	}

	// usato solo per i dao
	public void setDataFineCorsoFromDB(LocalDateTime dataFineCorso) {
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

	public String toStringArgomento() {
		return "Argomento: " + argomento;
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
