package Gui;

import controller.ChefController;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Chef;

public class RegistrazioneChefGUI {

    private final ChefController chefController;

    public RegistrazioneChefGUI(ChefController controller) {
        this.chefController = controller;
    }

    public void show(Stage ownerStage) {
        Stage stage = new Stage();
        stage.setTitle("Registrazione Chef");
        stage.initOwner(ownerStage);
        stage.initModality(Modality.APPLICATION_MODAL);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        TextField codFiscaleField = new TextField();
        TextField nomeField = new TextField();
        TextField cognomeField = new TextField();
        TextField emailField = new TextField();
        DatePicker dataNascitaPicker = new DatePicker();
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        CheckBox disponibilitaBox = new CheckBox("Disponibile");
        disponibilitaBox.setSelected(true);

        grid.add(new Label("Codice Fiscale*:"), 0, 0); grid.add(codFiscaleField, 1, 0);
        grid.add(new Label("Nome*:"), 0, 1); grid.add(nomeField, 1, 1);
        grid.add(new Label("Cognome*:"), 0, 2); grid.add(cognomeField, 1, 2);
        grid.add(new Label("Email*:"), 0, 3); grid.add(emailField, 1, 3);
        grid.add(new Label("Data Nascita*:"), 0, 4); grid.add(dataNascitaPicker, 1, 4);
        grid.add(new Label("Username*:"), 0, 5); grid.add(usernameField, 1, 5);
        grid.add(new Label("Password*:"), 0, 6); grid.add(passwordField, 1, 6);
        grid.add(disponibilitaBox, 1, 7);

        Label messageLabel = new Label();
        grid.add(messageLabel, 0, 9, 2, 1);

        Button registraButton = new Button("Registrati");
        Button annullaButton = new Button("Annulla");
        grid.add(registraButton, 1, 8);
        grid.add(annullaButton, 0, 8);

        // --- REGISTRAZIONE CORRETTA ---
        registraButton.setOnAction(e -> {
            try {
                // Validazione input vuoti
                if (codFiscaleField.getText().trim().isEmpty() ||
                    nomeField.getText().trim().isEmpty() ||
                    cognomeField.getText().trim().isEmpty() ||
                    emailField.getText().trim().isEmpty() ||
                    dataNascitaPicker.getValue() == null ||
                    usernameField.getText().trim().isEmpty() ||
                    passwordField.getText().trim().isEmpty()) {
                    
                    messageLabel.setText("Tutti i campi con * sono obbligatori");
                    messageLabel.setStyle("-fx-text-fill: red;");
                    return;
                }

                // Tentativo di registrazione
                Chef chefRegistrato = chefController.registraChef(
                    codFiscaleField.getText().trim(),
                    nomeField.getText().trim(),
                    cognomeField.getText().trim(),
                    emailField.getText().trim(),
                    dataNascitaPicker.getValue(),
                    disponibilitaBox.isSelected(),
                    usernameField.getText().trim(),
                    passwordField.getText().trim()
                );

                // Conferma registrazione
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Registrazione completata");
                alert.setHeaderText(null);
                alert.setContentText("Registrazione completata con successo!\nPuoi ora effettuare il login.");
                alert.showAndWait();

                // Chiudo la finestra di registrazione e torno al login
                stage.close();
                
               

            } catch (Exception ex) {
                messageLabel.setText("Errore: " + ex.getMessage());
                messageLabel.setStyle("-fx-text-fill: red;");
                System.err.println("Errore durante registrazione: " + ex.getMessage());
                ex.printStackTrace(); // Per debug
            }
        });

        annullaButton.setOnAction(e -> stage.close());

        Scene scene = new Scene(grid, 450, 400);
        stage.setScene(scene);
        stage.show();
    }
}