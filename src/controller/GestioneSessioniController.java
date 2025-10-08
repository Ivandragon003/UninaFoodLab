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

    // --- WRAPPER PER AZIONI ---
    private void executeAction(Runnable action, String successMessage) {
        try {
            action.run();
            StyleHelper.showSuccessDialog("Successo", successMessage);
        } catch (ValidationException ve) {
            StyleHelper.showValidationDialog("Validazione", ve.getMessage());
        } catch (DataAccessException dae) {
            StyleHelper.showErrorDialog("Errore Database", dae.getMessage());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", e.getMessage());
            e.printStackTrace();
        }
    }

    // --- SESSIONI ---
    public void aggiungiSessione(Sessione sessione, List<Ricetta> ricette) {
        executeAction(() -> {
            ValidationUtils.validateNotNull(sessione, "Sessione");
            ValidationUtils.validateNotNull(corso, "Corso");

            LocalDateTime now = LocalDateTime.now();
            if (sessione.getDataInizioSessione().isBefore(now))
                throw new ValidationException(ErrorMessages.DATA_INIZIO_SESSIONE_PASSATA);
            if (!sessione.getDataFineSessione().isAfter(sessione.getDataInizioSessione()))
                throw new ValidationException(ErrorMessages.DATA_FINE_SESSIONE_PRECEDENTE);

            gestioneSessioniService.creaSessione(sessione);
            corso.getSessioni().add(sessione);

            if (sessione instanceof InPresenza ip && ricette != null) {
                for (Ricetta r : ricette) {
                    gestioneSessioniService.aggiungiRicettaASessione(ip, r);
                }
            }
        }, ErrorMessages.SESSIONE_CREATA);
    }

    public void aggiornaSessione(Sessione oldS, Sessione newS) {
        executeAction(() -> {
            ValidationUtils.validateNotNull(oldS, "Sessione vecchia");
            ValidationUtils.validateNotNull(newS, "Sessione nuova");

            int idx = corso.getSessioni().indexOf(oldS);
            if (idx < 0) throw new ValidationException(ErrorMessages.SESSIONE_NON_TROVATA);

            LocalDateTime now = LocalDateTime.now();
            if (newS.getDataInizioSessione().isBefore(now))
                throw new ValidationException(ErrorMessages.DATA_INIZIO_SESSIONE_PASSATA);
            if (!newS.getDataFineSessione().isAfter(newS.getDataInizioSessione()))
                throw new ValidationException(ErrorMessages.DATA_FINE_SESSIONE_PRECEDENTE);

            gestioneSessioniService.creaSessione(newS);
            gestioneSessioniService.rimuoviSessione(oldS);
            corso.getSessioni().set(idx, newS);
        }, "Sessione aggiornata con successo!");
    }

    public void eliminaSessione(Sessione sessione) {
        executeAction(() -> {
            ValidationUtils.validateNotNull(sessione, "Sessione");

            if (corso.getSessioni().size() <= 1)
                throw new ValidationException(
                        "Impossibile eliminare l'unica sessione del corso. Aggiungere almeno un'altra."
                );

            gestioneSessioniService.rimuoviSessione(sessione);
            corso.getSessioni().remove(sessione);
        }, "Sessione eliminata con successo!");
    }

    // --- RICETTE ---
    public void aggiungiRicettaASessione(Sessione sessione, Ricetta ricetta) {
        executeAction(() -> {
            if (!(sessione instanceof InPresenza ip))
                throw new ValidationException(ErrorMessages.SOLO_SESSIONI_IN_PRESENZA);

            ValidationUtils.validateNotNull(ricetta, "Ricetta");

            if (ricetta.getIdRicetta() == 0)
                gestioneRicetteService.creaRicetta(ricetta);

            gestioneSessioniService.aggiungiRicettaASessione(ip, ricetta);
        }, "Ricetta associata con successo!");
    }

    public void rimuoviRicettaDaSessione(InPresenza sessione, Ricetta ricetta) {
        executeAction(() -> {
            ValidationUtils.validateNotNull(sessione, "Sessione");
            ValidationUtils.validateNotNull(ricetta, "Ricetta");

            gestioneSessioniService.rimuoviRicettaDaSessione(sessione, ricetta);
        }, "Ricetta rimossa con successo!");
    }
}
