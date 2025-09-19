package Gui;

import controller.ChefController;
import controller.CorsiController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.Chef;
import service.GestioneCorsiCucina;

public class LoginChefGUI extends Application {

    private static ChefController chefController;
    private static GestioneCorsiCucina gestioneCorsi;

    public static void setController(ChefController controller, GestioneCorsiCucina corsiService) {
        chefController = controller;
        gestioneCorsi = corsiService;
    }

    @Override
    public void start(Stage primaryStage) {
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
        messageLabel.setStyle("-fx-text-fill: red;");
        grid.add(messageLabel, 0, 4, 2, 1);

        Button loginButton = new Button("Login");
        Button resetButton = new Button("Reset");
        Button registratiButton = new Button("Registrati");
        
        grid.add(loginButton, 1, 2);
        grid.add(resetButton, 0, 2);
        grid.add(registratiButton, 1, 3);

        // LOGIN
        loginButton.setOnAction(e -> {
            try {
                Chef chef = chefController.login(usernameField.getText(), passwordField.getText());
                messageLabel.setText("Login effettuato: " + chef.getUsername());
                messageLabel.setStyle("-fx-text-fill: green;");

                // Creo menu e passo lo chef loggato e il controller corsi
                ChefMenuGUI menu = new ChefMenuGUI();
                menu.setChefLoggato(chef);
                CorsiController corsiController = new CorsiController(gestioneCorsi, chefController.getGestioneChef(), chef);
                menu.setController(corsiController);

                // Cambio scena nello stesso stage
                menu.start(primaryStage);

            } catch (Exception ex) {
                messageLabel.setText("Errore: " + ex.getMessage());
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        });

        // RESET
        resetButton.setOnAction(e -> {
            usernameField.clear();
            passwordField.clear();
            messageLabel.setText("");
        });
        
        // REGISTRAZIONE
        registratiButton.setOnAction(e -> {
            try {
                RegistrazioneChefGUI registrazioneGUI = new RegistrazioneChefGUI();
                registrazioneGUI.setController(chefController);
                
                // Apro in una nuova finestra
                Stage registrazioneStage = new Stage();
                registrazioneStage.setTitle("Registrazione Chef");
                registrazioneGUI.start(registrazioneStage);
                
            } catch (Exception ex) {
                messageLabel.setText("Errore nell'aprire la registrazione: " + ex.getMessage());
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        });

        Scene scene = new Scene(grid, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}