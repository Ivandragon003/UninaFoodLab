package controller;

import model.Chef;
import model.CorsoCucina;
import model.Frequenza;
import model.Sessione;
import service.GestioneChef;
import exceptions.ValidationException;
import exceptions.DataAccessException;
import exceptions.ErrorMessages;
import exceptions.ValidationUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


public class ChefController {
    private final GestioneChef gestioneChef;

  
    private GestioneCorsoController gestioneCorsoController;

    public ChefController(GestioneChef gestioneChef) {
        this.gestioneChef = gestioneChef;
    }

 

    public model.Chef login(String username, String password)
            throws ValidationException, DataAccessException {
        return gestioneChef.login(username, password);
    }

    public Chef registraChef(String codFiscale, String nome, String cognome,
                              String email, LocalDate dataNascita, boolean disponibilita,
                              String username, String password)
            throws ValidationException, DataAccessException {
        return gestioneChef.creaChef(codFiscale, nome, cognome, email,
                dataNascita, disponibilita, username, password);
    }

    public void aggiornaCredenziali(Chef chef, String nuovoUsername, String nuovaPassword)
            throws ValidationException, DataAccessException {
        gestioneChef.aggiornaCredenziali(chef, nuovoUsername, nuovaPassword);
    }

    public void eliminaAccount(Chef chef)
            throws ValidationException, DataAccessException {
        if (chef == null) {
            throw new ValidationException("Chef non valido");
        }
        gestioneChef.eliminaChef(chef.getUsername());
    }

    public GestioneChef getGestioneChef() {
        return gestioneChef;
    }

    
    public void setGestioneCorsoController(GestioneCorsoController gestioneCorsoController) {
        this.gestioneCorsoController = gestioneCorsoController;
    }

    
    public List<Chef> getAllChef() throws DataAccessException {
        try {
            return gestioneChef.getAll();
        } catch (Exception e) {
            throw new DataAccessException("Errore lettura chef", e);
        }
    }

   
    public List<Chef> filterChefs(List<Chef> source, String searchText, String filtroDisponibilita) {
        if (source == null) return List.of();
        String s = searchText == null ? "" : searchText.trim().toLowerCase(Locale.ROOT);

        return source.stream()
                .filter(chef -> {
                    boolean matchSearch = s.isEmpty()
                            || (chef.getNome() != null && chef.getNome().toLowerCase(Locale.ROOT).contains(s))
                            || (chef.getCognome() != null && chef.getCognome().toLowerCase(Locale.ROOT).contains(s))
                            || (chef.getUsername() != null && chef.getUsername().toLowerCase(Locale.ROOT).contains(s));

                    boolean matchDisp = true;
                    if ("Solo Disponibili".equals(filtroDisponibilita)) {
                        matchDisp = Boolean.TRUE.equals(chef.getDisponibilita());
                    } else if ("Solo Non Disponibili".equals(filtroDisponibilita)) {
                        matchDisp = !Boolean.TRUE.equals(chef.getDisponibilita());
                    }

                    return matchSearch && matchDisp;
                })
                .collect(Collectors.toList());
    }

 
    public void saveCorsoFromForm(
            String nome,
            String prezzoText,
            String argomento,
            String postiText,
            Frequenza frequenza,
            LocalDate dataInizio,
            Integer startHour,
            Integer startMinute,
            LocalDate dataFine,
            Integer endHour,
            Integer endMinute,
            List<Chef> chefSelezionati,
            List<Sessione> sessioni
    ) throws ValidationException, DataAccessException {

      
        if (gestioneCorsoController == null) {
            throw new DataAccessException("GestioneCorsoController non impostato: impossibile salvare corso");
        }

        if (nome == null || nome.trim().isEmpty()) {
            throw new ValidationException(ErrorMessages.NOME_CORSO_MANCANTE);
        }

        Double prezzo = ValidationUtils.parseDoubleSafe(prezzoText);
        if (prezzo == null) {
            throw new ValidationException("Formato prezzo non valido");
        }
        if (prezzo <= 0) {
            throw new ValidationException(ErrorMessages.PREZZO_NON_VALIDO);
        }

        Integer posti = ValidationUtils.parseIntegerSafe(postiText);
        if (posti == null) {
            throw new ValidationException("Formato numero posti non valido");
        }
        if (posti <= 0) {
            throw new ValidationException(ErrorMessages.NUMERO_POSTI_NON_VALIDO);
        }

        if (dataInizio == null || dataFine == null) {
            throw new ValidationException(ErrorMessages.DATE_CORSO_MANCANTI);
        }

        if (frequenza == null) {
            throw new ValidationException("Selezionare una frequenza");
        }

        int sh = (startHour != null) ? startHour : 9;
        int sm = (startMinute != null) ? startMinute : 0;
        int eh = (endHour != null) ? endHour : 17;
        int em = (endMinute != null) ? endMinute : 0;

        LocalDateTime ldtInizio = LocalDateTime.of(dataInizio, LocalTime.of(sh, sm));
        LocalDateTime ldtFine = LocalDateTime.of(dataFine, LocalTime.of(eh, em));

        if (!ldtInizio.isBefore(ldtFine)) {
            throw new ValidationException(ErrorMessages.DATA_FINE_PRECEDENTE);
        }

        if (chefSelezionati == null || chefSelezionati.isEmpty()) {
            throw new ValidationException("Selezionare almeno uno chef per il corso");
        }

        if (sessioni == null || sessioni.isEmpty()) {
            throw new ValidationException("Aggiungere almeno una sessione al corso");
        }

      
        CorsoCucina corso = new CorsoCucina(nome.trim(), prezzo, argomento == null ? "" : argomento.trim(), frequenza, posti);
        corso.setDataInizioCorso(ldtInizio);
        corso.setDataFineCorso(ldtFine);
        corso.setNumeroSessioni(sessioni.size());
        corso.setChef(List.copyOf(chefSelezionati));
        corso.setSessioni(List.copyOf(sessioni));

     
        gestioneCorsoController.creaCorso(corso);
    }
}
