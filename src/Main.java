import Gui.LoginChefGUI;
import controller.ChefController;
import dao.*;
import service.GestioneChef;
import service.GestioneCorsiCucina;
import util.DBConnection;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        try {
            // Creazione DAO
            ChefDAO chefDAO = new ChefDAO();
            TieneDAO tieneDAO = new TieneDAO();         
            CorsoCucinaDAO corsoDAO = new CorsoCucinaDAO();
            IscrizioneDAO iscrizioneDAO = new IscrizioneDAO();
            OnlineDAO onlineDAO = new OnlineDAO();
            InPresenzaDAO inPresenzaDAO = new InPresenzaDAO();

            // Creazione servizi
            GestioneChef chefService = new GestioneChef(chefDAO, tieneDAO);
            GestioneCorsiCucina corsiService = new GestioneCorsiCucina(
                    corsoDAO, chefDAO, tieneDAO, iscrizioneDAO, onlineDAO, inPresenzaDAO);

            ChefController chefController = new ChefController(chefService);

         
            LoginChefGUI.setController(chefController, corsiService);

 
            Application.launch(LoginChefGUI.class, args);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            DBConnection.closeDataSource();
            System.out.println("Pool DB chiuso al termine dell'app.");
        }
    }
}
