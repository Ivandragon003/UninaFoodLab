package model;

<<<<<<< Updated upstream
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CorsoCucina {

	private int idCorso;
	private String nomeCorso;
	private double prezzo;
	private String categoria;
	private Frequenza frequenzaCorso;
	private int numeroPosti;
	private int numeroSessioni;
	private LocalDateTime dataInizioCorso;
	private LocalDateTime dataFineCorso;

	
	private List<Iscrizione> iscrizioni = new ArrayList<>();
	private List<Sessione> sessioni = new ArrayList<>();
	
	public CorsoCucina(int idCorso, String nomeCorso, double prezzo, String categoria, Frequenza frequenzaCorso,
			int numeroPosti, int numeroSessioni) {
		setNomeCorso(nomeCorso);
		setPrezzo(prezzo);
		setCategoria(categoria);
		setFrequenzaCorso(frequenzaCorso);
		setNumeroPosti(numeroPosti);
		setNumeroSessioni(numeroSessioni);
	}

	// Getter e Setter
	public int getIdCorso() {
		return idCorso;
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

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		if (categoria == null || categoria.trim().isEmpty())
			throw new IllegalArgumentException("Categoria non può essere vuota");
		this.categoria = categoria.trim();
	}

	public Frequenza getFrequenzaCorso() {
		return frequenzaCorso;
	}

	public void setFrequenzaCorso(Frequenza frequenzaCorso) {
		if (frequenzaCorso == null)
			throw new IllegalArgumentException("Frequenza corso non può essere null");
		this.frequenzaCorso = frequenzaCorso;
	}

	public List<Sessione> getSessioni() {
		return sessioni;
	}

	public void setSessioni(List<Sessione> sessioni) {
		this.sessioni = sessioni;
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
		if (numeroSessioni <= 0)
			throw new IllegalArgumentException("Il numero di sessioni deve essere maggiore di zero");
		this.numeroSessioni = numeroSessioni;
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

	// Imposta la data inizio senza controlli (usato solo dal DAO)
	public void setDataInizioCorsoFromDB(LocalDateTime dataInizioCorso) {
	    this.dataInizioCorso = dataInizioCorso;
	}

	// Imposta la data fine senza controlli (usato solo dal DAO)
	public void setDataFineCorsoFromDB(LocalDateTime dataFineCorso) {
	    this.dataFineCorso = dataFineCorso;
	}

	
	
	public void aggiungiIscrizione(Iscrizione i) {
		if (!iscrizioni.contains(i))
			iscrizioni.add(i);
	}

	public void rimuoviIscrizione(Iscrizione i) {
		iscrizioni.remove(i);
	}

	public List<Iscrizione> getIscrizioni() {
		return iscrizioni;
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
=======
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

>>>>>>> Stashed changes
