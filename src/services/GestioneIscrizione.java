package service;

import java.util.*;
import model.CorsoCucina;
import model.Utente;

public class GestioneIscrizioni {

	private Map<CorsoCucina, Map<Utente, Boolean>> iscrizioni;

	public GestioneIscrizioni() {
		iscrizioni = new HashMap<>();
	}

	public boolean iscriviUtente(CorsoCucina corso, Utente utente) {
		if (corso == null || utente == null) {
			return false;
		}

		iscrizioni.putIfAbsent(corso, new HashMap<>());
		Map<Utente, Boolean> iscritti = iscrizioni.get(corso);

		if (iscritti.containsKey(utente) && iscritti.get(utente)) {
			System.out.println(utente + " è già iscritto al corso " + corso);
			return false;
		}

		long iscrittiAttivi = iscritti.values().stream().filter(Boolean::booleanValue).count();
		if (iscrittiAttivi >= corso.getNumeroPosti()) {
			System.out.println(corso + " è già pieno!");
			return false;
		}

		iscritti.put(utente, true);
		System.out.println(utente + " iscritto con successo al corso " + corso);
		return true;
	}

	public boolean disiscriviUtente(CorsoCucina corso, Utente utente) {
		if (corso == null || utente == null)
			return false;
		Map<Utente, Boolean> iscritti = iscrizioni.get(corso);
		if (iscritti != null && iscritti.containsKey(utente) && iscritti.get(utente)) {
			iscritti.put(utente, false);
			System.out.println(utente + " è stato disiscritto dal corso " + corso);
			return true;
		}
		return false;
	}

	public Set<Utente> getIscrittiAttivi(CorsoCucina corso) {
		Map<Utente, Boolean> iscritti = iscrizioni.get(corso);
		if (iscritti == null)
			return Collections.emptySet();

		Set<Utente> attivi = new HashSet<>();
		for (Map.Entry<Utente, Boolean> entry : iscritti.entrySet()) {
			if (entry.getValue()) {
				attivi.add(entry.getKey());
			}
		}
		return attivi;
	}

	public boolean getStatoIscrizione(CorsoCucina corso, Utente utente) {
		Map<Utente, Boolean> iscritti = iscrizioni.get(corso);
		return iscritti != null && iscritti.getOrDefault(utente, false);
	}
}
