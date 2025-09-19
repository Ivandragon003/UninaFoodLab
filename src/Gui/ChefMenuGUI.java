package Gui;

import controller.CorsiController;
import controller.VisualizzaCorsiController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.Chef;

public class ChefMenuGUI extends Application {

    private Chef chefLoggato;
    private CorsiController corsiController;

    public void setChefLoggato(Chef chef) {
        this.chefLoggato = chef;
    }

    public void setController(CorsiController controller) {
        this.corsiController = controller;
    }

    @Override
    public void start(Stage primaryStage) {
        if (chefLoggato == null || corsiController == null) {
            throw new IllegalStateException("Chef e controller devono essere impostati prima di start().");
        }

        primaryStage.setTitle("Menu Chef: " + chefLoggato.getUsername());

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        Label label = new Label("Benvenuto, " + chefLoggato.getUsername());
        grid.add(label, 0, 0, 2, 1);

        Button visualizzaCorsiBtn = new Button("Visualizza corsi");
        Button logoutButton = new Button("Logout");
        grid.add(visualizzaCorsiBtn, 0, 1);
        grid.add(logoutButton, 1, 1);

        // APRI VISUALIZZA CORSI
        visualizzaCorsiBtn.setOnAction(e -> {
            try {
                VisualizzaCorsiGUI corsiGUI = new VisualizzaCorsiGUI();
                VisualizzaCorsiController visualizzaController =
                        new VisualizzaCorsiController(corsiController.getGestioneCorsi(), chefLoggato);
                corsiGUI.setControllers(corsiController, visualizzaController);
                corsiGUI.start(new Stage()); // nuova finestra
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // LOGOUT
        logoutButton.setOnAction(e -> primaryStage.close());

        Scene scene = new Scene(grid, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
