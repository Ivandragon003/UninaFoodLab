package Gui;

import controller.CorsiController;
import controller.GestioneCorsoController;
import controller.VisualizzaCorsiController;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.control.ButtonType;

import model.Chef;

public class ChefMenuGUI {

    private Chef chefLoggato;
    private CorsiController corsiController;

    public void setChefLoggato(Chef chef) {
        this.chefLoggato = chef;
    }

    public void setController(CorsiController controller) {
        this.corsiController = controller;
    }

    public void start(Stage stage) {
        if (chefLoggato == null || corsiController == null) {
            throw new IllegalStateException("Chef e controller devono essere impostati prima di start().");
        }

        stage.setTitle("Menu Chef: " + chefLoggato.getUsername());

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        Label label = new Label("Benvenuto, " + chefLoggato.getUsername());
        grid.add(label, 0, 0, 2, 1);

        Button visualizzaCorsiBtn = new Button("Visualizza corsi");
        Button eliminaAccountBtn = new Button("Elimina Account");
        Button logoutButton = new Button("Logout");

        grid.add(visualizzaCorsiBtn, 0, 1);
        grid.add(eliminaAccountBtn, 1, 1);
        grid.add(logoutButton, 2, 1);

        // APRI VISUALIZZA CORSI
        visualizzaCorsiBtn.setOnAction(e -> {
            try {
                VisualizzaCorsiController visualizzaController =
                        new VisualizzaCorsiController(corsiController.getGestioneCorsi(), corsiController.getChefLoggato());

                GestioneCorsoController gestioneCorsoController =
                        new GestioneCorsoController(corsiController.getGestioneCorsi());

                VisualizzaCorsiGUI corsiGUI = new VisualizzaCorsiGUI();
                corsiGUI.setControllers(visualizzaController, gestioneCorsoController);
                corsiGUI.start(new Stage()); // nuova finestra
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // ELIMINA ACCOUNT
        eliminaAccountBtn.setOnAction(e -> {
            Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
            conferma.setHeaderText("Vuoi eliminare definitivamente il tuo account?");
            conferma.setContentText("Questa operazione non può essere annullata.");

            // Mostro alert e controllo il bottone premuto
            conferma.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) { 
                    try {
                        corsiController.eliminaAccount();
                        Alert info = new Alert(Alert.AlertType.INFORMATION);
                        info.setHeaderText("Account eliminato!");
                        info.showAndWait();
                        stage.close(); // chiude menu chef
                    } catch (Exception ex) {
                        Alert errore = new Alert(Alert.AlertType.ERROR);
                        errore.setHeaderText("Errore nell'eliminazione: " + ex.getMessage());
                        errore.showAndWait();
                    }
                } else {
                    // Se premi No/Cancel, non fare nulla
                    System.out.println("Eliminazione annullata dall'utente.");
                }
            });
        });


        // LOGOUT (torna indietro senza chiudere tutta l'app)
        logoutButton.setOnAction(e -> stage.close()); // Assumendo che il login rimanga aperto sotto

        Scene scene = new Scene(grid, 500, 200);
        stage.setScene(scene);
        stage.show();
    }
}
