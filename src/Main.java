import Gui.LoginChefGUI;
import controller.ChefController;
import dao.*;
import service.GestioneChef;
import service.GestioneCorsiCucina;
import javafx.application.Application;
import util.DBConnection;     // la tua classe DBConnection
import java.sql.Connection;    // interfaccia JDBC Connection

public class Main {
    public static void main(String[] args) {
        try {
            // --- DEBUG: apertura connessione di prova ---
            Connection testConn = DBConnection.getConnection(); // logga chi la apre

            // --- Creazione DAO ---
            ChefDAO chefDAO = new ChefDAO();
            TieneDAO tieneDAO = new TieneDAO();
            LavoraDAO lavoraDAO = new LavoraDAO();
            CorsoCucinaDAO corsoDAO = new CorsoCucinaDAO();
            IscrizioneDAO iscrizioneDAO = new IscrizioneDAO();
            OnlineDAO onlineDAO = new OnlineDAO();
            InPresenzaDAO inPresenzaDAO = new InPresenzaDAO();

            // --- Creazione service ---
            GestioneChef chefService = new GestioneChef(chefDAO, tieneDAO, lavoraDAO);
            GestioneCorsiCucina corsiService = new GestioneCorsiCucina(
                    corsoDAO, chefDAO, tieneDAO, iscrizioneDAO, onlineDAO, inPresenzaDAO
            );

            // --- Creazione controller principale ---
            ChefController chefController = new ChefController(chefService);

            // --- Passaggio controller e service alla GUI ---
            LoginChefGUI.setController(chefController, corsiService);

            // --- Avvio applicazione JavaFX ---
            Application.launch(LoginChefGUI.class, args);

            // --- DEBUG: chiusura connessione di prova ---
            // DBConnection.close(testConn); // commenta se vuoi lasciarla aperta per test

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

