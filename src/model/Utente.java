package model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Utente extends Persona {

	private Set<Sessione> sessioniSeguite;

	public Utente(String codFiscale, String nome, String cognome) {
		super(codFiscale, nome, cognome);
		this.sessioniSeguite = new HashSet<>();
	}

	public Set<Sessione> getSessioniSeguite() {
		return sessioniSeguite;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Utente))
			return false;
		Utente utente = (Utente) o;
		return getCodFiscale().equals(utente.getCodFiscale());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getCodFiscale());
	}
}
