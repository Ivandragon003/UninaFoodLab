package Gui;

import controller.ChefController;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class RegistrazioneChefGUI {

    private static ChefController chefController;

    public static void setController(ChefController controller) {
        chefController = controller;
    }

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Registrazione Chef");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        // Campi obbligatori
        TextField codFiscaleField = new TextField();
        TextField nomeField = new TextField();
        TextField cognomeField = new TextField();
        TextField emailField = new TextField();
        DatePicker dataNascitaPicker = new DatePicker();
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        CheckBox disponibilitaBox = new CheckBox("Disponibile");
        disponibilitaBox.setSelected(true); // Default true

        grid.add(new Label("Codice Fiscale*:"), 0, 0);
        grid.add(codFiscaleField, 1, 0);
        grid.add(new Label("Nome*:"), 0, 1);
        grid.add(nomeField, 1, 1);
        grid.add(new Label("Cognome*:"), 0, 2);
        grid.add(cognomeField, 1, 2);
        grid.add(new Label("Email*:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Data Nascita*:"), 0, 4);
        grid.add(dataNascitaPicker, 1, 4);
        grid.add(new Label("Username*:"), 0, 5);
        grid.add(usernameField, 1, 5);
        grid.add(new Label("Password*:"), 0, 6);
        grid.add(passwordField, 1, 6);
        grid.add(disponibilitaBox, 1, 7);

        Label messageLabel = new Label();
        grid.add(messageLabel, 0, 9, 2, 1);

        Button registraButton = new Button("Registrati");
        Button annullaButton = new Button("Annulla");
        grid.add(registraButton, 1, 8);
        grid.add(annullaButton, 0, 8);

        // Azione registrazione
        registraButton.setOnAction(e -> {
            try {
                chefController.registraChef(
                    codFiscaleField.getText(),
                    nomeField.getText(),
                    cognomeField.getText(),
                    emailField.getText(),
                    dataNascitaPicker.getValue(),
                    disponibilitaBox.isSelected(),
                    usernameField.getText(),
                    passwordField.getText()
                );
                messageLabel.setText("Registrazione avvenuta con successo!");
                messageLabel.setStyle("-fx-text-fill: green;");
                
                // Chiudo la finestra dopo 2 secondi
                Thread.sleep(2000);
                primaryStage.close();
                
            } catch (Exception ex) {
                messageLabel.setText("Errore: " + ex.getMessage());
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        });
        
        // Azione annulla
        annullaButton.setOnAction(e -> {
            primaryStage.close();
        });

        Scene scene = new Scene(grid, 450, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}