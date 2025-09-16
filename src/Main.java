import dao.CorsoCucinaDAO;
import dao.ChefDAO;
import dao.TieneDAO;
import model.Chef;
import model.CorsoCucina;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {
        ChefDAO chefDAO = new ChefDAO();
        CorsoCucinaDAO corsoDAO = new CorsoCucinaDAO();
        TieneDAO tieneDAO = new TieneDAO();

        // CF e ID corso da usare
        String cfChef = "RSSMRA80A01H501U";
        int idCorso = 1;

        try {
            // 🔹 Controllo se lo chef esiste, altrimenti lo creo
            Optional<Chef> chefOpt = chefDAO.findByCodFiscale(cfChef);
            if (chefOpt.isEmpty()) {
                Chef chef = new Chef(cfChef, "Mario", "Rossi", true, "mariorossi", "password123");
                chefDAO.save(chef,"password123");
                System.out.println("Chef creato: " + cfChef);
            }

            // 🔹 Controllo se il corso esiste, altrimenti lo creo
            Optional<CorsoCucina> corsoOpt = corsoDAO.findById(idCorso);
            if (corsoOpt.isEmpty()) {
                CorsoCucina corso = new CorsoCucina("Corso Base di Cucina", 100.0, "Base", null, 20, 5);
                corsoDAO.save(corso);
                System.out.println("Corso creato: " + idCorso);
            }

            // 🔹 Inserimento associazione chef-corso solo se non esiste
            boolean associazioneEsiste = tieneDAO.getCorsiByChef(cfChef).stream()
                    .anyMatch(c -> c.getIdCorso() == idCorso);
            if (!associazioneEsiste) {
                System.out.println("Test save...");
                tieneDAO.save(cfChef, idCorso);
                System.out.println("Associazione chef-corso salvata: " + cfChef + " -> " + idCorso);
            } else {
                System.out.println("Associazione già presente: " + cfChef + " -> " + idCorso);
            }

            // 🔹 Recupera tutti i corsi di uno chef
            System.out.println("Test getCorsiByChef...");
            List<CorsoCucina> corsi = tieneDAO.getCorsiByChef(cfChef);
            corsi.forEach(c -> System.out.println("Corso di chef: " + c.getNomeCorso()));

            // 🔹 Recupera tutti gli chef di un corso
            System.out.println("Test getChefByCorso...");
            List<Chef> chefList = tieneDAO.getChefByCorso(idCorso);
            chefList.forEach(c -> System.out.println("Chef del corso: " + c.getUsername()));

            // 🔹 Test deleteByChef
            System.out.println("Test deleteByChef...");
            tieneDAO.deleteByChef(cfChef);
            System.out.println("Tutte le associazioni di chef rimosse: " + cfChef);

            // 🔹 Test deleteByCorso
            System.out.println("Test deleteByCorso...");
            tieneDAO.deleteByCorso(idCorso);
            System.out.println("Tutte le associazioni del corso rimosse: " + idCorso);

        } catch (SQLException e) {
            System.err.println("Errore DAO: " + e.getMessage());

        } finally {
            // 🔹 Pulizia: elimina sempre l'associazione anche in caso di errore
            try {
                tieneDAO.delete(cfChef, idCorso);
                System.out.println("Associazione chef-corso rimossa in cleanup: " + cfChef + " -> " + idCorso);
            } catch (SQLException ex) {
                System.err.println("Errore durante la pulizia: " + ex.getMessage());
            }
        }
    }
}
