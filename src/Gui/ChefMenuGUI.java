package Gui;

import controller.ChefControllerInterface;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ChefMenuGUI extends Application {

    private static ChefControllerInterface chefController;

    public static void setController(ChefControllerInterface controller) {
        chefController = controller;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Menu Chef");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        Label userLabel = new Label("Nuovo Username:");
        TextField usernameField = new TextField();
        grid.add(userLabel, 0, 0);
        grid.add(usernameField, 1, 0);

        Label passLabel = new Label("Nuova Password:");
        PasswordField passwordField = new PasswordField();
        grid.add(passLabel, 0, 1);
        grid.add(passwordField, 1, 1);

        Label messageLabel = new Label();
        grid.add(messageLabel, 0, 3, 2, 1);

        Button aggiornaButton = new Button("Aggiorna credenziali");
        aggiornaButton.setOnAction(e -> {
            try {
                chefController.aggiornaCredenziali(usernameField.getText(), passwordField.getText());
                messageLabel.setText("Credenziali aggiornate con successo!");
            } catch (Exception ex) {
                messageLabel.setText("Errore: " + ex.getMessage());
            }
        });
        grid.add(aggiornaButton, 0, 2);

        Button eliminaButton = new Button("Elimina account");
        eliminaButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Sei sicuro di voler eliminare il tuo account?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        chefController.eliminaAccount();
                        messageLabel.setText("Account eliminato!");
                        primaryStage.close();
                    } catch (Exception ex) {
                        messageLabel.setText("Errore: " + ex.getMessage());
                    }
                }
            });
        });
        grid.add(eliminaButton, 1, 2);

        Scene scene = new Scene(grid, 450, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
