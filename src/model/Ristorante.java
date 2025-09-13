package model;

public class Ristorante {
	private Integer idRistorante;
	private String partitaIva;
	private String nome;
	private String via;
	private int stelleMichelin;

	public Ristorante(String via, String partitaIva, String nome) throws IllegalArgumentException {
		this.via = via;
		setPartitaIva(partitaIva);
		setNome(nome);
	}

	public Integer getIdRistorante() {
		return idRistorante;
	}

	public String getPartitaIva() {
		return partitaIva;
	}

	public void setPartitaIva(String partitaIva) throws IllegalArgumentException {
		if (partitaIva == null || !partitaIva.matches("\\d{11}")) {
			throw new IllegalArgumentException(
					"La partita IVA deve essere composta esattamente da 11 cifre numeriche.");
		}
		this.partitaIva = partitaIva;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) throws IllegalArgumentException {
		if (nome == null || nome.trim().isEmpty()) {
			throw new IllegalArgumentException("Il nome del ristorante non può essere vuoto.");
		}
		this.nome = nome;
	}

	public String getVia() {
		return via;
	}

	public void setVia(String via) throws IllegalArgumentException {
		if (via == null || via.trim().isEmpty()) {
			throw new IllegalArgumentException("La via non può essere vuota.");
		}
		this.via = via;
	}

	public int getStelleMichelin() {
		return stelleMichelin;
	}

	public void setStelleMichelin(int stelleMichelin) throws IllegalArgumentException {
		if (stelleMichelin >= 0 && stelleMichelin <= 3) {
			this.stelleMichelin = stelleMichelin;
		} else {
			throw new IllegalArgumentException("Le stelle Michelin devono essere comprese tra 1 e 3.");
		}
	}
}

