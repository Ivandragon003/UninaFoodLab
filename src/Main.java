import controller.ChefController;
import dao.*;
import service.GestioneChef;
import service.GestioneCorsiCucina;
import javafx.application.Application;
import Gui.LoginChefGUI;

public class Main {

    public static void main(String[] args) {
        try {
            // --- Creo tutti i DAO necessari ---
            ChefDAO chefDAO = new ChefDAO();
            LavoraDAO lavoraDAO = new LavoraDAO();
            TieneDAO tieneDAO = new TieneDAO();
            CorsoCucinaDAO corsiDAO = new CorsoCucinaDAO();
            IscrizioneDAO iscrizioneDAO = new IscrizioneDAO();
            OnlineDAO onlineDAO = new OnlineDAO();
            InPresenzaDAO inPresenzaDAO = new InPresenzaDAO();

            // --- Creo il servizio chef ---
            GestioneChef gestioneChef = new GestioneChef(chefDAO, tieneDAO, lavoraDAO);

            // --- Creo il servizio corsi con tutti i DAO richiesti ---
            GestioneCorsiCucina gestioneCorsi = new GestioneCorsiCucina(
                corsiDAO,      // CorsoCucinaDAO
                chefDAO,       // ChefDAO  
                tieneDAO,      // TieneDAO
                iscrizioneDAO, // IscrizioneDAO
                onlineDAO,     // OnlineDAO
                inPresenzaDAO  // InPresenzaDAO
            );

            // --- Creo controller per login ---
            ChefController chefController = new ChefController(gestioneChef);

            // --- Passo i controller alla GUI ---
            LoginChefGUI.setController(chefController, gestioneCorsi);

            // --- Lancio GUI login ---
            Application.launch(LoginChefGUI.class, args);

        } catch (Exception e) {
            System.err.println("Errore nel main: " + e.getMessage());
            e.printStackTrace();
        }
    }
}