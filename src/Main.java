import Gui.LoginChefGUI;
import controller.ChefController;
import dao.*;

import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        try {
         
            ChefDAO chefDAO = new ChefDAO();
            TieneDAO tieneDAO = new TieneDAO();

            ChefController chefController = new ChefController(chefDAO, tieneDAO);

           
            LoginChefGUI.setController(chefController);
            Application.launch(LoginChefGUI.class, args);

        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
}
