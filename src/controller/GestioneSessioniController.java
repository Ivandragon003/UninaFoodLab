package controller;

import model.*;
import service.GestioneSessioni;
import service.GestioneCucina;
import service.GestioneRicette;
import util.StyleHelper;
import exceptions.ValidationException;
import exceptions.DataAccessException;
import exceptions.ErrorMessages;
import exceptions.ValidationUtils;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class GestioneSessioniController {
    private final CorsoCucina corso;
    private final GestioneSessioni gestioneSessioniService;
    private final GestioneCucina gestioneCucinaService;
    private final GestioneRicette gestioneRicetteService;

    public GestioneSessioniController(CorsoCucina corso,
                                      GestioneSessioni gestioneSessioniService,
                                      GestioneCucina gestioneCucinaService,
                                      GestioneRicette gestioneRicetteService) {
        this.corso = corso;
        this.gestioneSessioniService = gestioneSessioniService;
        this.gestioneCucinaService  = gestioneCucinaService;
        this.gestioneRicetteService = gestioneRicetteService;
    }

    public void aggiungiSessione(Sessione s, List<Ricetta> ricette) {
        try {
            if (s == null) throw new ValidationException(ErrorMessages.SESSIONE_NULLA);
            ValidationUtils.validateNotNull(corso, "Corso");
            LocalDateTime now = LocalDateTime.now();
            if (s.getDataInizioSessione().isBefore(now))
                throw new ValidationException(ErrorMessages.DATA_INIZIO_SESSIONE_PASSATA);
            if (!s.getDataFineSessione().isAfter(s.getDataInizioSessione()))
                throw new ValidationException(ErrorMessages.DATA_FINE_SESSIONE_PRECEDENTE);

            corso.getSessioni().add(s);
            gestioneSessioniService.creaSessione(s);

            if (s instanceof InPresenza ip && ricette != null) {
                for (Ricetta r : ricette) {
                    if (!ip.getRicette().contains(r)) {
                        ip.getRicette().add(r);
                        r.getSessioni().add(ip);
                        gestioneCucinaService.aggiungiSessioneARicetta(r, ip);
                    }
                }
            }
            StyleHelper.showSuccessDialog("Successo", ErrorMessages.SESSIONE_CREATA);

        } catch (ValidationException ve) {
            StyleHelper.showValidationDialog("Validazione", ve.getMessage());
        } catch (SQLException se) {
            StyleHelper.showErrorDialog("Errore Database", se.getMessage());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", e.getMessage());
            e.printStackTrace();
        }
    }

    public void aggiornaSessione(Sessione oldS, Sessione newS) {
        try {
            if (oldS == null || newS == null)
                throw new ValidationException(ErrorMessages.SESSIONE_NULLA);
            int idx = corso.getSessioni().indexOf(oldS);
            if (idx < 0) throw new ValidationException("Sessione non trovata");
            LocalDateTime now = LocalDateTime.now();
            if (newS.getDataInizioSessione().isBefore(now))
                throw new ValidationException(ErrorMessages.DATA_INIZIO_SESSIONE_PASSATA);
            if (!newS.getDataFineSessione().isAfter(newS.getDataInizioSessione()))
                throw new ValidationException(ErrorMessages.DATA_FINE_SESSIONE_PRECEDENTE);

            corso.getSessioni().set(idx, newS);
            gestioneSessioniService.rimuoviSessione(oldS);
            gestioneSessioniService.creaSessione(newS);
            StyleHelper.showSuccessDialog("Successo", "Sessione aggiornata con successo!");

        } catch (ValidationException ve) {
            StyleHelper.showValidationDialog("Validazione", ve.getMessage());
        } catch (SQLException se) {
            StyleHelper.showErrorDialog("Errore Database", se.getMessage());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", e.getMessage());
            e.printStackTrace();
        }
    }

    public void eliminaSessione(Sessione s) {
        try {
            if (s == null) throw new ValidationException(ErrorMessages.SESSIONE_NULLA);
            if (corso.getSessioni().size() <= 1)
                throw new ValidationException(
                    "Impossibile eliminare l'unica sessione del corso. Aggiungere almeno un'altra."
                );

            corso.getSessioni().remove(s);
            gestioneSessioniService.rimuoviSessione(s);
            StyleHelper.showSuccessDialog("Successo", "Sessione eliminata con successo!");

        } catch (ValidationException ve) {
            StyleHelper.showValidationDialog("Validazione", ve.getMessage());
        } catch (SQLException se) {
            StyleHelper.showErrorDialog("Errore Database", se.getMessage());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", e.getMessage());
            e.printStackTrace();
        }
    }

    public void aggiungiRicettaASessione(Sessione sessione, Ricetta ricetta) {
        try {
            if (!(sessione instanceof InPresenza ip))
                throw new ValidationException("Solo sessioni in presenza possono avere ricette");
            if (ricetta == null)
                throw new ValidationException("Ricetta nulla");
            if (ricetta.getIdRicetta() == 0)
                gestioneRicetteService.creaRicetta(ricetta);

            if (!ip.getRicette().contains(ricetta)) {
                ip.getRicette().add(ricetta);
                ricetta.getSessioni().add(ip);
                gestioneCucinaService.aggiungiSessioneARicetta(ricetta, ip);
                StyleHelper.showSuccessDialog("Successo", "Ricetta associata con successo!");
            } else {
                StyleHelper.showInfoDialog("Info", "Ricetta già associata");
            }

        } catch (ValidationException ve) {
            StyleHelper.showValidationDialog("Validazione", ve.getMessage());
        } catch (SQLException se) {
            StyleHelper.showErrorDialog("Errore Database", se.getMessage());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", e.getMessage());
            e.printStackTrace();
        }
    }

    public void rimuoviRicettaDaSessione(InPresenza ip, Ricetta r) {
        try {
            if (ip == null || r == null) throw new ValidationException("Dati nulli");
            if (ip.getRicette().remove(r)) {
                r.getSessioni().remove(ip);
                gestioneCucinaService.rimuoviSessioneDaRicetta(r, ip);
                StyleHelper.showSuccessDialog("Successo", "Ricetta rimossa con successo!");
            } else {
                StyleHelper.showInfoDialog("Info", "Ricetta non associata");
            }
        } catch (ValidationException ve) {
            StyleHelper.showValidationDialog("Validazione", ve.getMessage());
        } catch (SQLException se) {
            StyleHelper.showErrorDialog("Errore Database", se.getMessage());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", e.getMessage());
            e.printStackTrace();
        }
    }
}
