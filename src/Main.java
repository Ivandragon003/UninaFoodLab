import Gui.LoginChefGUI;
import controller.ChefController;
import dao.*;
import service.*;
import util.DBConnection;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        try {
            // ======== CREAZIONE DAO ========
            ChefDAO chefDAO = new ChefDAO();
            TieneDAO tieneDAO = new TieneDAO();
            CorsoCucinaDAO corsoDAO = new CorsoCucinaDAO();
            IscrizioneDAO iscrizioneDAO = new IscrizioneDAO();
            OnlineDAO onlineDAO = new OnlineDAO();
            InPresenzaDAO inPresenzaDAO = new InPresenzaDAO();
            RicettaDAO ricettaDAO = new RicettaDAO();
            IngredienteDAO ingredienteDAO = new IngredienteDAO();
            UsaDAO usaDAO = new UsaDAO();
            CucinaDAO cucinaDAO = new CucinaDAO();

            // ======== CREAZIONE SERVIZI ========
            GestioneRicette gestioneRicette = new GestioneRicette(ricettaDAO);
            GestioneCucina gestioneCucina = new GestioneCucina(cucinaDAO);
            GestioneCorsiCucina corsiService = new GestioneCorsiCucina(
                corsoDAO, chefDAO, tieneDAO, iscrizioneDAO, onlineDAO, inPresenzaDAO,
                gestioneRicette, gestioneCucina
            );

            GestioneChef chefService = new GestioneChef(chefDAO, tieneDAO);
            GestioneIngrediente gestioneIngrediente = new GestioneIngrediente(ingredienteDAO);
            GestioneUsa gestioneUsa = new GestioneUsa(usaDAO, ingredienteDAO);
            GestioneSessioni gestioneSessioni = new GestioneSessioni(inPresenzaDAO, onlineDAO, cucinaDAO);

            ChefController chefController = new ChefController(chefService);

            // ======== PASSAGGIO CONTROLLER E SERVICE A LOGIN ========
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
