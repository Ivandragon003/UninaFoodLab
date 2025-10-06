package service;

import dao.ChefDAO;
import dao.TieneDAO;
import model.Chef;
import model.CorsoCucina;
import exceptions.ValidationException;
import exceptions.DataAccessException;
import exceptions.ErrorMessages;
import exceptions.ValidationUtils;

import java.util.List;
import java.util.Optional;
import java.sql.SQLException;
import java.time.LocalDate;

public class GestioneChef {
    private final ChefDAO chefDAO;
    private final TieneDAO tieneDAO;

    public GestioneChef(ChefDAO chefDAO, TieneDAO tieneDAO) {
        this.chefDAO = chefDAO;
        this.tieneDAO = tieneDAO;
    }

    // ==================== AUTENTICAZIONE ====================
    
    public Chef login(String username, String password) throws ValidationException, DataAccessException {
        ValidationUtils.validateNotEmpty(username, "Username");
        ValidationUtils.validateNotEmpty(password, "Password");
        
        try {
            Optional<Chef> chefOpt = chefDAO.findByUsername(username);
            
            if (chefOpt.isEmpty()) {
                throw new ValidationException(ErrorMessages.CREDENZIALI_ERRATE);
            }
            
            Chef chef = chefOpt.get();
            if (!chef.getPassword().equals(password)) {
                throw new ValidationException(ErrorMessages.CREDENZIALI_ERRATE);
            }
            
            return chef;
            
        } catch (SQLException e) {
            throw new DataAccessException(ErrorMessages.ERRORE_DATABASE, e);
        }
    }

    // ==================== REGISTRAZIONE ====================
    
    public Chef creaChef(String codFiscale, String nome, String cognome, String email,
                         LocalDate dataNascita, boolean disponibilita, 
                         String username, String password) 
            throws ValidationException, DataAccessException {
        
        validateChefInput(codFiscale, nome, cognome, email, dataNascita, username, password);
        
        try {
            checkUniqueConstraints(codFiscale, email, username);
            
            Chef chef = new Chef(codFiscale, nome, cognome, disponibilita, username, password);
            chef.setEmail(email);
            chef.setDataNascita(dataNascita);
            
            chefDAO.save(chef, password);
            return chef;
            
        } catch (SQLException e) {
            throw new DataAccessException(ErrorMessages.ERRORE_SALVATAGGIO, e);
        }
    }

    // ==================== AGGIORNAMENTO ====================
    
    public void aggiornaChef(Chef chef) throws ValidationException, DataAccessException {
        if (chef == null) {
            throw new ValidationException(ErrorMessages.CHEF_NULLO);
        }
        
        try {
            if (chefDAO.findByUsername(chef.getUsername()).isEmpty()) {
                throw new ValidationException(ErrorMessages.CHEF_NON_PRESENTE + chef.getUsername());
            }
            
            chefDAO.update(chef, chef.getPassword());
            
        } catch (SQLException e) {
            throw new DataAccessException(ErrorMessages.ERRORE_AGGIORNAMENTO, e);
        }
    }

    public void aggiornaCredenziali(Chef chef, String nuovoUsername, String nuovaPassword) 
            throws ValidationException, DataAccessException {
        
        if (chef == null) {
            throw new ValidationException(ErrorMessages.CHEF_NULLO);
        }
        
        ValidationUtils.validateNotEmpty(nuovoUsername, "Username");
        ValidationUtils.validateTextLength(nuovoUsername, "Username", 3, 30);
        ValidationUtils.validateTextLength(nuovaPassword, "Password", 6, 50);
        
        try {
            if (!nuovoUsername.equals(chef.getUsername())) {
                if (chefDAO.findByUsername(nuovoUsername).isPresent()) {
                    throw new ValidationException("Username già esistente");
                }
            }
            
            chef.setUsername(nuovoUsername);
            chef.setPassword(nuovaPassword);
            chefDAO.update(chef, nuovaPassword);
            
        } catch (SQLException e) {
            throw new DataAccessException(ErrorMessages.ERRORE_AGGIORNAMENTO, e);
        }
    }

    // ==================== ELIMINAZIONE ====================
    
    public void eliminaChef(String username) throws ValidationException, DataAccessException {
        ValidationUtils.validateNotEmpty(username, "Username");
        
        try {
            Optional<Chef> chefOpt = chefDAO.findByUsername(username);
            if (chefOpt.isEmpty()) {
                throw new ValidationException(ErrorMessages.CHEF_NON_PRESENTE + username);
            }
            
            chefDAO.delete(chefOpt.get().getCodFiscale());
            
        } catch (SQLException e) {
            throw new DataAccessException(ErrorMessages.ERRORE_ELIMINAZIONE, e);
        }
    }

    // ==================== QUERY ====================
    
    public List<Chef> getAll() throws DataAccessException {
        try {
            return chefDAO.getAll();
        } catch (SQLException e) {
            throw new DataAccessException(ErrorMessages.ERRORE_LETTURA, e);
        }
    }
    
    public Chef getChefByUsername(String username) throws DataAccessException {
        try {
            return chefDAO.findByUsername(username).orElse(null);
        } catch (SQLException e) {
            throw new DataAccessException(ErrorMessages.ERRORE_LETTURA, e);
        }
    }
    
    public boolean existsByCodFiscale(String codFiscale) throws DataAccessException {
        try {
            return chefDAO.existsByCodFiscale(codFiscale);
        } catch (SQLException e) {
            throw new DataAccessException(ErrorMessages.ERRORE_LETTURA, e);
        }
    }
    
    public boolean existsByEmail(String email) throws DataAccessException {
        try {
            return chefDAO.existsByEmail(email);
        } catch (SQLException e) {
            throw new DataAccessException(ErrorMessages.ERRORE_LETTURA, e);
        }
    }

    // ==================== GESTIONE CORSI ====================
    
    public void aggiungiCorso(Chef chef, CorsoCucina corso) 
            throws ValidationException, DataAccessException {
        if (chef == null) {
            throw new ValidationException(ErrorMessages.CHEF_NULLO);
        }
        if (corso == null) {
            throw new ValidationException(ErrorMessages.CORSO_NULLO);
        }
        
        if (chef.getCorsi().contains(corso)) {
            throw new ValidationException(ErrorMessages.CHEF_GIA_ASSEGNATO);
        }
        
        try {
            chef.getCorsi().add(corso);
            corso.getChef().add(chef);
            tieneDAO.save(chef.getCodFiscale(), corso.getIdCorso());
            
        } catch (SQLException e) {
            // Rollback in-memory su fallimento
            chef.getCorsi().remove(corso);
            corso.getChef().remove(chef);
            throw new DataAccessException(ErrorMessages.ERRORE_SALVATAGGIO, e);
        }
    }
    
    public void rimuoviCorso(Chef chef, CorsoCucina corso) 
            throws ValidationException, DataAccessException {
        if (chef == null) {
            throw new ValidationException(ErrorMessages.CHEF_NULLO);
        }
        if (corso == null) {
            throw new ValidationException(ErrorMessages.CORSO_NULLO);
        }
        
        if (!chef.getCorsi().contains(corso)) {
            throw new ValidationException("Chef non insegna questo corso");
        }
        
        try {
            chef.getCorsi().remove(corso);
            corso.getChef().remove(chef);
            tieneDAO.delete(chef.getCodFiscale(), corso.getIdCorso());
            
        } catch (SQLException e) {
            // Rollback in-memory su fallimento
            chef.getCorsi().add(corso);
            corso.getChef().add(chef);
            throw new DataAccessException(ErrorMessages.ERRORE_ELIMINAZIONE, e);
        }
    }

    // ==================== METODI PRIVATI DI VALIDAZIONE ====================
    
    private void validateChefInput(String codFiscale, String nome, String cognome, 
                                   String email, LocalDate dataNascita,
                                   String username, String password) throws ValidationException {
        
        ValidationUtils.validateNotEmpty(codFiscale, "Codice fiscale");
        ValidationUtils.validateTextLength(codFiscale, "Codice fiscale", 16, 16);
        
        ValidationUtils.validateNotEmpty(nome, "Nome");
        ValidationUtils.validateTextLength(nome, "Nome", 2, 50);
        
        ValidationUtils.validateNotEmpty(cognome, "Cognome");
        ValidationUtils.validateTextLength(cognome, "Cognome", 2, 50);
        
        ValidationUtils.validateEmail(email);
        
        if (dataNascita == null) {
            throw new ValidationException(ErrorMessages.campoObbligatorio("Data di nascita"));
        }
        if (dataNascita.isAfter(LocalDate.now().minusYears(18))) {
            throw new ValidationException("Lo chef deve avere almeno 18 anni");
        }
        
        ValidationUtils.validateNotEmpty(username, "Username");
        ValidationUtils.validateTextLength(username, "Username", 3, 30);
        
        ValidationUtils.validateNotEmpty(password, "Password");
        ValidationUtils.validateTextLength(password, "Password", 6, 50);
    }
    
    private void checkUniqueConstraints(String codFiscale, String email, String username) 
            throws ValidationException, SQLException {
        
        if (chefDAO.existsByCodFiscale(codFiscale)) {
            throw new ValidationException("Codice fiscale già presente nel sistema");
        }
        
        if (chefDAO.existsByEmail(email)) {
            throw new ValidationException("Email già utilizzata");
        }
        
        if (chefDAO.findByUsername(username).isPresent()) {
            throw new ValidationException("Username già esistente");
        }
    }
}
