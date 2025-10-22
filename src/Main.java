import Gui.LoginChefGUI;
import controller.ChefController;
import dao.*;
import util.DBConnection;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        try {
            // ======== CREAZIONE DAO ========
            ChefDAO chefDAO = new ChefDAO();
            TieneDAO tieneDAO = new TieneDAO();

            // ======== CREAZIONE CONTROLLER ========
            ChefController chefController = new ChefController(chefDAO, tieneDAO);

            // ======== AVVIO LOGIN GUI ========
            LoginChefGUI.setController(chefController);
            Application.launch(LoginChefGUI.class, args);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeDataSource();
            System.out.println("Pool DB chiuso al termine dell'app.");
        }
    }
}
