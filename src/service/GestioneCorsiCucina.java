package service;

import dao.*;
import exceptions.DataAccessException;
import exceptions.ValidationException;
import exceptions.ErrorMessages;
import exceptions.ValidationUtils;
import model.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GestioneCorsiCucina {

	private final CorsoCucinaDAO corsoDAO;
	private final ChefDAO chefDAO;
	private final TieneDAO tieneDAO;
	private final IscrizioneDAO iscrizioneDAO;
	private final OnlineDAO onlineDAO;
	private final InPresenzaDAO inPresenzaDAO;

	private final GestioneRicette gestioneRicette;
	private final GestioneCucina gestioneCucina;

	public GestioneCorsiCucina(CorsoCucinaDAO corsoDAO, ChefDAO chefDAO, TieneDAO tieneDAO, IscrizioneDAO iscrizioneDAO,
			OnlineDAO onlineDAO, InPresenzaDAO inPresenzaDAO, GestioneRicette gestioneRicette,
			GestioneCucina gestioneCucina) {
		this.corsoDAO = corsoDAO;
		this.chefDAO = chefDAO;
		this.tieneDAO = tieneDAO;
		this.iscrizioneDAO = iscrizioneDAO;
		this.onlineDAO = onlineDAO;
		this.inPresenzaDAO = inPresenzaDAO;
		this.gestioneRicette = gestioneRicette;
		this.gestioneCucina = gestioneCucina;
	}

	public void creaCorso(CorsoCucina corso) throws ValidationException, DataAccessException {

		if (corso.getDataInizioCorso() == null || corso.getDataFineCorso() == null)
			throw new ValidationException(ErrorMessages.DATE_CORSO_MANCANTI);

		if (corso.getDataInizioCorso().isBefore(java.time.LocalDateTime.now()))
			throw new ValidationException(ErrorMessages.DATA_INIZIO_PASSATA);

		if (corso.getDataFineCorso().isBefore(corso.getDataInizioCorso()))
			throw new ValidationException(ErrorMessages.DATA_FINE_PRECEDENTE);

		try {
			corsoDAO.save(corso);

			if (corso.getSessioni() != null) {
				for (Sessione s : corso.getSessioni()) {
					s.setCorsoCucina(corso);

					if (s instanceof Online o) {
						onlineDAO.save(o);
					} else if (s instanceof InPresenza ip) {
						inPresenzaDAO.save(ip);

						if (ip.getRicette() != null) {
							for (Ricetta r : ip.getRicette()) {
								if (r.getIdRicetta() == 0) {
									gestioneRicette.creaRicetta(r);
								}
								gestioneCucina.aggiungiSessioneARicetta(r, ip);
							}
						}
					}
				}
			}

			if (corso.getChef() != null) {
				for (Chef c : corso.getChef()) {
					if (!chefDAO.findByCodFiscale(c.getCodFiscale()).isPresent()) {
						throw new ValidationException(ErrorMessages.CHEF_NON_PRESENTE + c.getCodFiscale());
					}
					if (!tieneDAO.getChefByCorso(corso.getIdCorso()).contains(c)) {
						tieneDAO.save(c.getCodFiscale(), corso.getIdCorso());
					}
				}
			}

		} catch (SQLException e) {
			throw new DataAccessException(ErrorMessages.ERRORE_SALVATAGGIO, e);
		}
	}

	public void aggiornaCorso(CorsoCucina corso) throws ValidationException, DataAccessException {
		ValidationUtils.validateNotNull(corso, "Corso");
		try {
			corsoDAO.update(corso);
		} catch (SQLException e) {
			throw new DataAccessException(ErrorMessages.ERRORE_AGGIORNAMENTO, e);
		}
	}

	public void cancellaCorso(int idCorso) throws DataAccessException {
		try {
			corsoDAO.delete(idCorso);
		} catch (SQLException e) {
			throw new DataAccessException(ErrorMessages.ERRORE_ELIMINAZIONE, e);
		}
	}

	public void aggiungiChefACorso(CorsoCucina corso, Chef chef, String password)
			throws ValidationException, DataAccessException {
		ValidationUtils.validateNotNull(corso, "Corso");
		ValidationUtils.validateNotNull(chef, "Chef");

		try {
			boolean chefEsiste = chefDAO.findByCodFiscale(chef.getCodFiscale()).isPresent();
			if (!chefEsiste) {
				if (password == null || password.length() < 6)
					throw new ValidationException(ErrorMessages.PASSWORD_NON_VALIDA);
				chefDAO.save(chef, password);
			}

			if (!tieneDAO.getChefByCorso(corso.getIdCorso()).contains(chef)) {
				tieneDAO.save(chef.getCodFiscale(), corso.getIdCorso());
				if (corso.getChef() == null)
					corso.setChef(new ArrayList<>());
				corso.getChef().add(chef);
			} else {
				throw new ValidationException(ErrorMessages.CHEF_GIA_ASSEGNATO);
			}
		} catch (SQLException e) {
			throw new DataAccessException(ErrorMessages.ERRORE_SALVATAGGIO, e);
		}
	}

	public void rimuoviChefDaCorso(Chef chef, CorsoCucina corso) throws ValidationException, DataAccessException {
		if (chef == null)
			throw new ValidationException(ErrorMessages.CHEF_NULLO);
		if (corso == null)
			throw new ValidationException(ErrorMessages.CORSO_NULLO);

		try {

			List<Chef> chefAssegnati = tieneDAO.getChefByCorso(corso.getIdCorso());
			boolean assegnato = chefAssegnati.stream()
					.anyMatch(c -> c.getCodFiscale() != null && c.getCodFiscale().equals(chef.getCodFiscale()));
			if (!assegnato) {
				throw new ValidationException("Lo chef non è assegnato a questo corso");
			}

			tieneDAO.delete(chef.getCodFiscale(), corso.getIdCorso());

			if (chef.getCorsi() != null) {
				chef.getCorsi().removeIf(c -> c.getIdCorso() == corso.getIdCorso());
			}
			if (corso.getChef() != null) {
				corso.getChef()
						.removeIf(c -> c.getCodFiscale() != null && c.getCodFiscale().equals(chef.getCodFiscale()));
			}

		} catch (SQLException e) {
			throw new DataAccessException(ErrorMessages.ERRORE_ELIMINAZIONE, e);
		}
	}

	public CorsoCucina getCorsoCompleto(int idCorso) throws DataAccessException {
		try {
			CorsoCucina corso = corsoDAO.findById(idCorso)
					.orElseThrow(() -> new DataAccessException("Corso non trovato"));

			List<Sessione> sessioni = new ArrayList<>();
			sessioni.addAll(onlineDAO.getByCorso(idCorso));
			sessioni.addAll(inPresenzaDAO.getByCorso(idCorso));
			corso.setSessioni(sessioni);

			corso.setIscrizioni(
					iscrizioneDAO.getAllFull().stream().filter(i -> i.getCorso().getIdCorso() == idCorso).toList());

			corso.setChef(tieneDAO.getChefByCorso(idCorso));
			return corso;
		} catch (SQLException e) {
			throw new DataAccessException(ErrorMessages.ERRORE_LETTURA, e);
		}
	}

	public List<CorsoCucina> getCorsi() throws DataAccessException {
		try {
			return corsoDAO.getAll();
		} catch (SQLException e) {
			throw new DataAccessException(ErrorMessages.ERRORE_LETTURA, e);
		}
	}

	public List<CorsoCucina> cercaPerNomeOCategoria(String filtro) throws DataAccessException {
		try {
			return corsoDAO.findByNomeOrArgomento(filtro);
		} catch (SQLException e) {
			throw new DataAccessException(ErrorMessages.ERRORE_LETTURA, e);
		}
	}

	public int getNumeroSessioniPerCorso(int idCorso) throws DataAccessException {
		try {
			return corsoDAO.getNumeroSessioniPerCorso(idCorso);
		} catch (SQLException e) {
			throw new DataAccessException(ErrorMessages.ERRORE_LETTURA, e);
		}
	}

	public List<CorsoCucina> getCorsiByChef(Chef chef) throws DataAccessException {
		try {
			return tieneDAO.getCorsiByChef(chef.getCodFiscale());
		} catch (SQLException e) {
			throw new DataAccessException("Errore recupero corsi per chef", e);
		}
	}

	public List<Sessione> getSessioniPerCorsoInPeriodo(int idCorso, LocalDate inizio, LocalDate fine)
			throws DataAccessException {
		try {
			List<Sessione> sessioni = new ArrayList<>();
			sessioni.addAll(onlineDAO.getByCorso(idCorso));
			sessioni.addAll(inPresenzaDAO.getByCorso(idCorso));
			return sessioni.stream().filter(s -> {
				LocalDate d = s.getDataInizioSessione().toLocalDate();
				return !d.isBefore(inizio) && !d.isAfter(fine);
			}).toList();
		} catch (SQLException e) {
			throw new DataAccessException("Errore recupero sessioni per corso", e);
		}
	}
}
