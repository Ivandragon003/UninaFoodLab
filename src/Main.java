// NESSUN package - metti Main.java direttamente in src/

import controller.ChefController;
import service.GestioneChef;
import dao.ChefDAO;
import dao.LavoraDAO;
import dao.TieneDAO;
import javafx.application.Application;
import Gui.LoginChefGUI;

public class Main {
    public static void main(String[] args) {
      
        
        // Inizializza DAO e Service
        ChefDAO chefDAO = new ChefDAO();
        LavoraDAO lavoraDAO = new LavoraDAO();
        TieneDAO tieneDAO = new TieneDAO();
        GestioneChef gestioneChef = new GestioneChef(chefDAO, lavoraDAO, tieneDAO);
        
        // Inizializza Controller
        ChefController chefController = new ChefController(gestioneChef);
        
        // Passa il controller alla GUI tramite setter statico
        LoginChefGUI.setController(chefController);
        
        // Avvia JavaFX
        Application.launch(LoginChefGUI.class, args);
    }
}