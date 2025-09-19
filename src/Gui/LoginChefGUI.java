package Gui;

import controller.ChefController;
import controller.CorsiController;
import service.GestioneCorsiCucina;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.Chef;

public class LoginChefGUI extends Application {

    private static ChefController chefController;
    private static GestioneCorsiCucina corsiService;

    public static void setController(ChefController controller, GestioneCorsiCucina corsiServiceArg) {
        chefController = controller;
        corsiService = corsiServiceArg;
    }

    @Override
    public void start(Stage primaryStage) {
        if (chefController == null || corsiService == null) {
            throw new IllegalStateException("Controller o Service non inizializzati");
        }

        primaryStage.setTitle("Login Chef");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        Label userLabel = new Label("Username:");
        TextField usernameField = new TextField();
        grid.add(userLabel, 0, 0);
        grid.add(usernameField, 1, 0);

        Label passLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        grid.add(passLabel, 0, 1);
        grid.add(passwordField, 1, 1);

        Label messageLabel = new Label();
        grid.add(messageLabel, 0, 4, 2, 1);

        Button loginButton = new Button("Login");
        Button registratiButton = new Button("Registrati");
        grid.add(loginButton, 1, 2);
        grid.add(registratiButton, 1, 3);

        loginButton.setOnAction(e -> {
            try {
                Chef chef = chefController.login(usernameField.getText(), passwordField.getText());
                messageLabel.setText("Login effettuato: " + chef.getUsername());
                messageLabel.setStyle("-fx-text-fill: green;");

                // Creo controller dei corsi
                CorsiController corsiController = new CorsiController(corsiService, chefController.getGestioneChef(), chef);

                // Apro il menu chef
                ChefMenuGUI menu = new ChefMenuGUI();
                menu.setChefLoggato(chef);
                menu.setController(corsiController);

                Stage menuStage = new Stage();
                menu.start(menuStage);

               
                Stage loginStage = (Stage) ((Button) e.getSource()).getScene().getWindow();
                loginStage.close();

            } catch (Exception ex) {
                messageLabel.setText("Errore: " + ex.getMessage());
                messageLabel.setStyle("-fx-text-fill: red;");
                ex.printStackTrace();
            }
        });


        registratiButton.setOnAction(e -> {
            RegistrazioneChefGUI registrazioneGUI = new RegistrazioneChefGUI(chefController);
            registrazioneGUI.show(primaryStage);
        });

        Scene scene = new Scene(grid, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
