package controller;

import model.Chef;
import service.GestioneChef;

import java.time.LocalDate;
import java.sql.SQLException;

public class ChefController {

    private final GestioneChef gestioneChef;

    public ChefController(GestioneChef gestioneChef) {
        this.gestioneChef = gestioneChef;
    }

    // LOGIN
    public Chef login(String username, String password) throws SQLException {
        if (username == null || username.isEmpty())
            throw new IllegalArgumentException("Username non può essere vuoto");
        if (password == null || password.isEmpty())
            throw new IllegalArgumentException("Password non può essere vuota");

        Chef chef = gestioneChef.getChefByUsername(username);
        if (chef == null)
            throw new IllegalArgumentException("Chef non trovato");

        if (!chef.getPassword().equals(password))
            throw new IllegalArgumentException("Password errata");

        return chef;
    }

 // REGISTRAZIONE 
    public Chef registraChef(String codFiscale, String nome, String cognome, String email,
                             LocalDate dataNascita, boolean disponibilita, String username, String password)
                             throws SQLException {

        if (codFiscale == null || codFiscale.isEmpty())
            throw new IllegalArgumentException("Codice fiscale obbligatorio");
        if (nome == null || nome.isEmpty())
            throw new IllegalArgumentException("Nome obbligatorio");
        if (cognome == null || cognome.isEmpty())
            throw new IllegalArgumentException("Cognome obbligatorio");
        if (email == null || email.isEmpty())
            throw new IllegalArgumentException("Email obbligatoria");
        if (dataNascita == null)
            throw new IllegalArgumentException("Data di nascita obbligatoria");
        if (username == null || username.isEmpty())
            throw new IllegalArgumentException("Username obbligatorio");
        if (password == null || password.length() < 6)
            throw new IllegalArgumentException("Password obbligatoria (min 6 caratteri)");

        // Controlli di unicità
        if (gestioneChef.getChefByUsername(username) != null)
            throw new IllegalArgumentException("Username già esistente");
        if (gestioneChef.existsByCodFiscale(codFiscale))
            throw new IllegalArgumentException("Codice fiscale già presente");
        if (gestioneChef.existsByEmail(email))    // <--- aggiungi questo
            throw new IllegalArgumentException("Email già presente");

        Chef chef = new Chef(codFiscale, nome, cognome, disponibilita, username, password);
        chef.setEmail(email);
        chef.setDataNascita(dataNascita);

        gestioneChef.creaChef(chef);

        return chef;
    }


    //  AGGIORNAMENTO CREDENZIALI 
    public void aggiornaCredenziali(Chef chef, String nuovoUsername, String nuovaPassword) throws SQLException {
        if (nuovoUsername == null || nuovoUsername.isEmpty())
            throw new IllegalArgumentException("Username obbligatorio");
        if (nuovaPassword == null || nuovaPassword.length() < 6)
            throw new IllegalArgumentException("Password obbligatoria (min 6 caratteri)");

        chef.setUsername(nuovoUsername);
        chef.setPassword(nuovaPassword);

        gestioneChef.aggiornaChef(chef);
    }

    // --- ELIMINAZIONE ACCOUNT ---
    public void eliminaAccount(Chef chef) throws SQLException {
        gestioneChef.eliminaChef(chef.getUsername());
    }

    // --- Getter per GUI ---
    public GestioneChef getGestioneChef() {
        return gestioneChef;
    }
}
