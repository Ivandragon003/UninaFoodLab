package gui;

import controller.ChefController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.Chef;

public class LoginChefGUI extends Application {

    private ChefController chefController;

    public LoginChefGUI(ChefController controller) {
        this.chefController = controller;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login Chef");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        // Username
        Label userLabel = new Label("Username:");
        TextField usernameField = new TextField();
        grid.add(userLabel, 0, 0);
        grid.add(usernameField, 1, 0);

        // Password
        Label passLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        grid.add(passLabel, 0, 1);
        grid.add(passwordField, 1, 1);

        // Messaggio di errore / info
        Label messageLabel = new Label();
        grid.add(messageLabel, 1, 4);

        // Pulsanti
        Button loginButton = new Button("Login");
        Button resetButton = new Button("Reset");
        Button registraButton = new Button("Registrazione");

        grid.add(loginButton, 1, 2);
        grid.add(resetButton, 0, 2);
        grid.add(registraButton, 1, 3);

        // Azione Login
        loginButton.setOnAction(e -> {
            try {
                Chef chef = chefController.login(usernameField.getText(), passwordField.getText());
                messageLabel.setText("Login effettuato: " + chef.getUsername());
            } catch (Exception ex) {
                messageLabel.setText("Errore: " + ex.getMessage());
            }
        });

        // Azione Reset
        resetButton.setOnAction(e -> {
            usernameField.clear();
            passwordField.clear();
            messageLabel.setText("");
        });

        // Azione Registrazione
        registraButton.setOnAction(e -> {
            // Puoi aprire un nuovo form o dialog per inserire dati e chiamare chefController.registraChef(...)
            messageLabel.setText("Funzione registrazione non ancora implementata");
        });

        Scene scene = new Scene(grid, 400, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
