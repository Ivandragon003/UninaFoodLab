package service;

import dao.AdesioneDAO;
import dao.InPresenzaDAO;
import dao.OnlineDAO;
import model.*;
import java.sql.SQLException;

public class GestioneSessioni {
    private final InPresenzaDAO inPresenzaDAO;
    private final OnlineDAO onlineDAO;
    private final AdesioneDAO adesioneDAO;

    public GestioneSessioni(InPresenzaDAO inPresenzaDAO, OnlineDAO onlineDAO, AdesioneDAO adesioneDAO) {
        this.inPresenzaDAO = inPresenzaDAO;
        this.onlineDAO = onlineDAO;
        this.adesioneDAO = adesioneDAO;
    }

    // Creazione Sessioni
    public void creaSessione(Sessione sessione) throws SQLException {
        if (sessione instanceof InPresenza) {
            inPresenzaDAO.save((InPresenza) sessione);
        } else if (sessione instanceof Online) {
            onlineDAO.save((Online) sessione);
        } else {
            throw new IllegalArgumentException("Tipo di sessione non gestito");
        }
    }

    // Rimozione Sessioni
    public void rimuoviSessione(Sessione sessione) throws SQLException {
        if (sessione instanceof InPresenza) {
            adesioneDAO.deleteBySessione(sessione.getIdSessione());
            inPresenzaDAO.delete(sessione.getIdSessione());
        } else if (sessione instanceof Online) {
            onlineDAO.delete(sessione.getIdSessione());
        }
    }

    // *** CORREZIONE PRINCIPALE: Aggiunta persistenza nel database ***
    public void aggiungiRicettaASessione(InPresenza sessione, Ricetta ricetta) throws SQLException {
        if (sessione == null || ricetta == null) {
            throw new IllegalArgumentException("Sessione e ricetta non possono essere null");
        }

        if (!sessione.getRicette().contains(ricetta)) {
            // Aggiorna in memoria
            sessione.getRicette().add(ricetta);
            ricetta.getSessioni().add(sessione);
            
            // *** AGGIUNTA: Persistenza nel database ***
            // Assumendo che esista una tabella di associazione sessione-ricetta
            // e che il DAO appropriato sia disponibile tramite GestioneCucina
            try {
                // Qui dovresti implementare la persistenza usando il DAO appropriato
                // Per esempio, se hai un CucinaDAO o SessioneRicettaDAO:
                // cucinaDAO.aggiungiRicettaASessione(sessione.getIdSessione(), ricetta.getIdRicetta());
                
                // Versione temporanea - potresti dover adattare questo alla tua struttura DB
                insertSessioneRicettaRelation(sessione.getIdSessione(), ricetta.getIdRicetta());
                
            } catch (SQLException e) {
                // Rollback in memoria se il database fallisce
                sessione.getRicette().remove(ricetta);
                ricetta.getSessioni().remove(sessione);
                throw new SQLException("Errore durante il salvataggio della relazione sessione-ricetta nel database", e);
            }
        }
    }

    public void rimuoviRicettaDaSessione(InPresenza sessione, Ricetta ricetta) throws SQLException {
        if (sessione == null || ricetta == null) {
            throw new IllegalArgumentException("Sessione e ricetta non possono essere null");
        }

        if (sessione.getRicette().remove(ricetta)) {
            ricetta.getSessioni().remove(sessione);
            
            // *** AGGIUNTA: Rimozione dal database ***
            try {
                deleteSessioneRicettaRelation(sessione.getIdSessione(), ricetta.getIdRicetta());
            } catch (SQLException e) {
                // Ripristina in memoria se il database fallisce
                sessione.getRicette().add(ricetta);
                ricetta.getSessioni().add(sessione);
                throw new SQLException("Errore durante la rimozione della relazione sessione-ricetta dal database", e);
            }
        }
    }

    // *** METODI DI SUPPORTO PER LA PERSISTENZA ***
    // Questi metodi dovrebbero essere implementati con il DAO appropriato
    
    private void insertSessioneRicettaRelation(int idSessione, int idRicetta) throws SQLException {
        // Implementazione temporanea - adatta alla tua struttura database
        String sql = "INSERT INTO sessione_ricetta (id_sessione, id_ricetta) VALUES (?, ?) ON CONFLICT DO NOTHING";
        
        try (java.sql.Connection conn = util.DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idSessione);
            ps.setInt(2, idRicetta);
            ps.executeUpdate();
        }
    }
    
    private void deleteSessioneRicettaRelation(int idSessione, int idRicetta) throws SQLException {
        // Implementazione temporanea - adatta alla tua struttura database
        String sql = "DELETE FROM sessione_ricetta WHERE id_sessione = ? AND id_ricetta = ?";
        
        try (java.sql.Connection conn = util.DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idSessione);
            ps.setInt(2, idRicetta);
            ps.executeUpdate();
        }
    }

    // *** METODI AGGIUNTIVI PER MIGLIORARE LA GESTIONE ***
    
    /**
     * Recupera tutte le ricette associate a una sessione in presenza dal database
     */
    public java.util.List<Ricetta> getRicetteBySessione(int idSessione) throws SQLException {
        java.util.List<Ricetta> ricette = new java.util.ArrayList<>();
        
        String sql = "SELECT r.idRicetta, r.nome, r.tempoPreparazione " +
                    "FROM ricetta r " +
                    "JOIN sessione_ricetta sr ON r.idRicetta = sr.id_ricetta " +
                    "WHERE sr.id_sessione = ?";
        
        try (java.sql.Connection conn = util.DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idSessione);
            
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ricetta r = new Ricetta(
                        rs.getString("nome"),
                        rs.getInt("tempoPreparazione")
                    );
                    r.setIdRicetta(rs.getInt("idRicetta"));
                    ricette.add(r);
                }
            }
        }
        
        return ricette;
    }

    /**
     * Verifica se una ricetta è già associata a una sessione
     */
    public boolean isRicettaAssociataASessione(int idSessione, int idRicetta) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sessione_ricetta WHERE id_sessione = ? AND id_ricetta = ?";
        
        try (java.sql.Connection conn = util.DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idSessione);
            ps.setInt(2, idRicetta);
            
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }

    /**
     * Rimuove tutte le ricette associate a una sessione (utile per eliminazione sessione)
     */
    public void rimuoviTutteRicetteDaSessione(int idSessione) throws SQLException {
        String sql = "DELETE FROM sessione_ricetta WHERE id_sessione = ?";
        
        try (java.sql.Connection conn = util.DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idSessione);
            ps.executeUpdate();
        }
    }

    /**
     * Conta il numero di ricette associate a una sessione
     */
    public int getNumeroRicettePerSessione(int idSessione) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sessione_ricetta WHERE id_sessione = ?";
        
        try (java.sql.Connection conn = util.DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idSessione);
            
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }
    
    
} 