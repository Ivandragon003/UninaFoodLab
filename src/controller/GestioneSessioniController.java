package controller;

import exceptions.DataAccessException;
import exceptions.ValidationException;
import exceptions.ErrorMessages;
import exceptions.ValidationUtils;
import model.CorsoCucina;
import model.InPresenza;
import model.Ricetta;
import model.Sessione;
import service.GestioneCucina;
import service.GestioneRicette;
import service.GestioneSessioni;
import util.StyleHelper;

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
        this.gestioneCucinaService = gestioneCucinaService;
        this.gestioneRicetteService = gestioneRicetteService;
    }

    public void aggiungiSessione(Sessione s, List<Ricetta> ricette) {
        try {
            ValidationUtils.validateNotNull(s, "Sessione");
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
                    gestioneSessioniService.aggiungiRicettaASessione(ip, r);
                }
            }

            StyleHelper.showSuccessDialog("Successo", ErrorMessages.SESSIONE_CREATA);

        } catch (ValidationException ve) {
            StyleHelper.showValidationDialog("Validazione", ve.getMessage());
        } catch (DataAccessException dae) {
            StyleHelper.showErrorDialog("Errore Database", dae.getMessage());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", e.getMessage());
            e.printStackTrace();
        }
    }

    public void aggiornaSessione(Sessione oldS, Sessione newS) {
        try {
            ValidationUtils.validateNotNull(oldS, "Sessione vecchia");
            ValidationUtils.validateNotNull(newS, "Sessione nuova");

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
        } catch (DataAccessException dae) {
            StyleHelper.showErrorDialog("Errore Database", dae.getMessage());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", e.getMessage());
            e.printStackTrace();
        }
    }

    public void eliminaSessione(Sessione s) {
        try {
            ValidationUtils.validateNotNull(s, "Sessione");

            if (corso.getSessioni().size() <= 1)
                throw new ValidationException(
                        "Impossibile eliminare l'unica sessione del corso. Aggiungere almeno un'altra."
                );

            corso.getSessioni().remove(s);
            gestioneSessioniService.rimuoviSessione(s);

            StyleHelper.showSuccessDialog("Successo", "Sessione eliminata con successo!");

        } catch (ValidationException ve) {
            StyleHelper.showValidationDialog("Validazione", ve.getMessage());
        } catch (DataAccessException dae) {
            StyleHelper.showErrorDialog("Errore Database", dae.getMessage());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", e.getMessage());
            e.printStackTrace();
        }
    }

    public void aggiungiRicettaASessione(Sessione sessione, Ricetta ricetta) {
        try {
            if (!(sessione instanceof InPresenza ip))
                throw new ValidationException("Solo sessioni in presenza possono avere ricette");

            ValidationUtils.validateNotNull(ricetta, "Ricetta");

            if (ricetta.getIdRicetta() == 0)
                gestioneRicetteService.creaRicetta(ricetta);

            gestioneSessioniService.aggiungiRicettaASessione(ip, ricetta);
            StyleHelper.showSuccessDialog("Successo", "Ricetta associata con successo!");

        } catch (ValidationException ve) {
            StyleHelper.showValidationDialog("Validazione", ve.getMessage());
        } catch (DataAccessException dae) {
            StyleHelper.showErrorDialog("Errore Database", dae.getMessage());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", e.getMessage());
            e.printStackTrace();
        }
    }

    public void rimuoviRicettaDaSessione(InPresenza ip, Ricetta r) {
        try {
            ValidationUtils.validateNotNull(ip, "Sessione");
            ValidationUtils.validateNotNull(r, "Ricetta");

            gestioneSessioniService.rimuoviRicettaDaSessione(ip, r);
            StyleHelper.showSuccessDialog("Successo", "Ricetta rimossa con successo!");

        } catch (ValidationException ve) {
            StyleHelper.showValidationDialog("Validazione", ve.getMessage());
        } catch (DataAccessException dae) {
            StyleHelper.showErrorDialog("Errore Database", dae.getMessage());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", e.getMessage());
            e.printStackTrace();
        }
    }
}
