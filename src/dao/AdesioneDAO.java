package dao;

import model.Adesione;
import model.Utente;
import model.InPresenza;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdesioneDAO {
    
    private static final String insertAdesione = 
        "INSERT INTO adesioni (idUtente, idSessione, dataAdesione, stato) VALUES (?, ?, ?, ?)";
    
    private static final String selectAllAdesioni= 
        "SELECT a.*, u.nome as nome_utente, u.cognome as cognome_utente, u.email, u.password, " +
        "s.id as sessione_id, s.dataOra, s.durata, s.maxPartecipanti, s.idCorso " +
        "FROM adesioni a " +
        "JOIN utenti u ON a.idUtente = u.id " +
        "JOIN sessioni_presenza s ON a.idSessione = s.id";
    
    private static final String selectAdesioneByUtente = 
        selectAllAdesioni + " WHERE a.idUtente = ?";
    
    private static final String selectAdesioniBySessione = 
        selectAllAdesioni + " WHERE a.idSessione = ?";
    
    private static final String selectAdesioneByUtenteSessione = 
        selectAllAdesioni + " WHERE a.idUtente = ? AND a.idSessione = ?";
    
    private static final String updateStatoAdesione = 
        "UPDATE adesioni SET stato = ? WHERE idUtente = ? AND idSessione = ?";
    
    private static final String deleteAdesione = 
        "DELETE FROM adesioni WHERE idUtente = ? AND idSessione = ?";
    
    private static final String countAdesioniAttiveBySessioni = 
        "SELECT COUNT(*) FROM adesioni WHERE idSessione = ? AND stato = true";
    
    private Connection connection;
    private UtenteDAO utenteDAO;
    private InPresenzaDAO sessioneDAO;
    
    public AdesioneDAO(Connection connection, UtenteDAO Utente, InPresenzaDAO sessione) {
        this.connection = connection;
        this.utenteDAO = Utente;
        this.sessioneDAO = sessione;

    }
    
    /**
     * Crea una nuova adesione nel database
     */
    public boolean creaAdesione(Adesione adesione) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(insertAdesione);
        stmt.setInt(1, adesione.getUtente().getAdesioniUtente());
        stmt.setInt(2, adesione.getSessione().getIdSessione());
        stmt.setTimestamp(3, Timestamp.valueOf(adesione.getDataAdesione()));
        stmt.setBoolean(4, adesione.isStato());
        
        int rowsAffected = stmt.executeUpdate();
        stmt.close();
        return rowsAffected > 0;
    }
    
    /**
     * Recupera tutte le adesioni dal database
     */
    public List<Adesione> getAllAdesioni() throws SQLException {
        List<Adesione> adesioni = new ArrayList<>();
        
        PreparedStatement stmt = connection.prepareStatement(selectAllAdesioni);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            Adesione adesione = mapResultSetToAdesione(rs);
            if (adesione != null) {
                adesioni.add(adesione);
            }
        }
        
        rs.close();
        stmt.close();
        return adesioni;
    }
    
    /**
     * Recupera tutte le adesioni di un utente specifico
     */
    public List<Adesione> getAdesioniByUtente(int idUtente) throws SQLException {
        List<Adesione> adesioni = new ArrayList<>();
        
        PreparedStatement stmt = connection.prepareStatement(selectAdesioneByUtente);
        stmt.setInt(1, idUtente);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            Adesione adesione = mapResultSetToAdesione(rs);
            if (adesione != null) {
                adesioni.add(adesione);
            }
        }
        
        rs.close();
        stmt.close();
        return adesioni;
    }
    
    /**
     * Recupera tutte le adesioni per una sessione specifica
     */
    public List<Adesione> getAdesioniBySessione(int idSessione) throws SQLException {
        List<Adesione> adesioni = new ArrayList<>();
        
        PreparedStatement stmt = connection.prepareStatement(selectAdesioniBySessione);
        stmt.setInt(1, idSessione);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            Adesione adesione = mapResultSetToAdesione(rs);
            if (adesione != null) {
                adesioni.add(adesione);
            }
        }
        
        rs.close();
        stmt.close();
        return adesioni;
    }
    
    /**
     * Recupera una specifica adesione tramite utente e sessione
     */
    public Adesione getAdesioneByUtenteESessione(int idUtente, int idSessione) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(selectAdesioneByUtenteSessione);
        stmt.setInt(1, idUtente);
        stmt.setInt(2, idSessione);
        ResultSet rs = stmt.executeQuery();
        
        Adesione adesione = null;
        if (rs.next()) {
            adesione = mapResultSetToAdesione(rs);
        }
        
        rs.close();
        stmt.close();
        return adesione;
    }
    
    /**
     * Aggiorna lo stato di un'adesione (attiva/ritirata)
     */
    public boolean aggiornaStatoAdesione(int idUtente, int idSessione, boolean nuovoStato) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(updateStatoAdesione);
        stmt.setBoolean(1, nuovoStato);
        stmt.setInt(2, idUtente);
        stmt.setInt(3, idSessione);
        
        int rowsAffected = stmt.executeUpdate();
        stmt.close();
        return rowsAffected > 0;
    }
    
    /**
     * Elimina un'adesione dal database
     */
    public boolean eliminaAdesione(int idUtente, int idSessione) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(deleteAdesione);
        stmt.setInt(1, idUtente);
        stmt.setInt(2, idSessione);
        
        int rowsAffected = stmt.executeUpdate();
        stmt.close();
        return rowsAffected > 0;
    }
    
    /**
     * Conta il numero di adesioni attive per una sessione
     */
    public int contaAdesioniAttive(int idSessione) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(countAdesioniAttiveBySessioni);
        stmt.setInt(1, idSessione);
        ResultSet rs = stmt.executeQuery();
        
        int count = 0;
        if (rs.next()) {
            count = rs.getInt(1);
        }
        
        rs.close();
        stmt.close();
        return count;
    }
    
    /**
     * Verifica se un utente ha già aderito a una sessione
     */
    public boolean hasUtenteAderito(int idUtente, int idSessione) throws SQLException {
        return getAdesioneByUtenteESessione(idUtente, idSessione) != null;
    }
    
    /**
     * Ritira un'adesione (cambia stato a false invece di eliminarla)
     */
    public boolean ritiraAdesione(int idUtente, int idSessione) throws SQLException {
        return aggiornaStatoAdesione(idUtente, idSessione, false);
    }
    
    /**
     * Riattiva un'adesione precedentemente ritirata
     */
    public boolean riattivaAdesione(int idUtente, int idSessione) throws SQLException {
        return aggiornaStatoAdesione(idUtente, idSessione, true);
    }
    
    /**
     * Mappa un ResultSet a un oggetto Adesione
     */
    private Adesione mapResultSetToAdesione(ResultSet rs) throws SQLException {
        // Recupera l'utente
        Utente utente = new Utente(
            rs.getInt("idUtente"),
            rs.getString("nome_utente"),
            rs.getString("cognome_utente"),
            rs.getString("email"),
            rs.getString("password")
        );
        
        // Recupera la sessione
        InPresenza sessione = sessioneDAO.getSessioneById(rs.getInt("idSessione"));
        
        if (sessione == null) {
            return null;
        }
        
        // Crea l'adesione
        LocalDateTime dataAdesione = rs.getTimestamp("dataAdesione").toLocalDateTime();
        Adesione adesione = new Adesione(utente, sessione, dataAdesione);
        adesione.setStato(rs.getBoolean("stato"));
        
        return adesione;
    }
}