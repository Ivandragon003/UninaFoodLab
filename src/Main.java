

import controller.ChefController;
import service.*;
import dao.*;

import javafx.application.Application;

public class Main {

    public static void main(String[] args) {
        // Inizializza DAO e Service
        ChefDAO chefDAO = new ChefDAO();
        LavoraDAO lavoraDAO = new LavoraDAO();
        TieneDAO tieneDAO = new TieneDAO();
        GestioneChef gestioneChef = new GestioneChef(lavoraDAO, tieneDAO);

        // Inizializza Controller
        ChefController chefController = new ChefController(gestioneChef);

        // Passa il controller alla GUI e avvia JavaFX
        LoginChefGUI.setController(chefController);
        Application.launch(LoginChefGUI.class, args);
    }
}
