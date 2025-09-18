package controller;

import model.CorsoCucina;
import model.Chef;
import service.GestioneCorsiCucina;
import service.GestioneChef;

import java.sql.SQLException;
import java.util.List;

public class CorsiController {

    private final GestioneCorsiCucina corsiService;
    private final GestioneChef chefService;

    // L'oggetto chef loggato
    private final Chef chefLoggato;

    public CorsiController(GestioneCorsiCucina corsiService, GestioneChef chefService, Chef chefLoggato) {
        this.corsiService = corsiService;
        this.chefService = chefService;
        this.chefLoggato = chefLoggato;
    }

    //  CORSI 

    public void visualizzaCorsi(List<CorsoCucina> corsi) {
        corsi.forEach(c -> System.out.println(c.toStringNomeCorso() + " | ID: " + c.getIdCorso()));
    }

    public void creaCorso(CorsoCucina corso) {
        try {
            // Salva il corso
            corsiService.creaCorso(corso);

            // Aggiunge automaticamente lo chef loggato
            corsiService.aggiungiChefACorso(corso, chefLoggato, chefLoggato.getPassword());

            System.out.println("Corso creato correttamente! Lo chef loggato è stato aggiunto automaticamente.");
            System.out.println("Puoi aggiungere altri chef se necessario.");
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Errore nella creazione del corso: " + e.getMessage());
        }
    }

    public void modificaCorso(CorsoCucina corsoAggiornato) {
        try {
            corsiService.aggiornaCorso(corsoAggiornato);
            System.out.println("Corso aggiornato correttamente!");
        } catch (SQLException e) {
            System.err.println("Errore nell'aggiornamento del corso: " + e.getMessage());
        }
    }

    public void eliminaCorso(int idCorso) {
        try {
            corsiService.cancellaCorso(idCorso);
            System.out.println("Corso eliminato correttamente!");
        } catch (SQLException e) {
            System.err.println("Errore nell'eliminazione del corso: " + e.getMessage());
        }
    }

    // CHEF 

    public void aggiornaCredenziali(String nuovoUsername, String nuovaPassword) {
        try {
            chefLoggato.setUsername(nuovoUsername);
            chefLoggato.setPassword(nuovaPassword);
            chefService.aggiornaChef(chefLoggato);
            System.out.println("Credenziali aggiornate correttamente!");
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Errore nell'aggiornamento delle credenziali: " + e.getMessage());
        }
    }


    public void eliminaAccount() {
        try {
            chefService.eliminaChef(chefLoggato.getUsername());
            System.out.println("Account eliminato correttamente!");
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Errore nell'eliminazione dell'account: " + e.getMessage());
        }
    }

   
}
