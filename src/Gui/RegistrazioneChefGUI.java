package Gui;

import controller.ChefController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class RegistrazioneChefGUI extends VBox {
    private final ChefController chefController;

    public RegistrazioneChefGUI(ChefController controller, Runnable onAnnulla) {
        this.chefController = controller;

        setSpacing(20);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(30));
        setMaxWidth(380);
        setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 20;" +
                "-fx-border-radius: 20;" +
                "-fx-border-color: #FF9966;" +
                "-fx-border-width: 2;");

        // Titolo
        Label titleLabel = new Label("UninaFoodLab");
        titleLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web("#FF6600"));

        Label subtitleLabel = new Label("Registrati come Chef");
        subtitleLabel.setFont(Font.font("Roboto", FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(Color.web("#FF8533"));

        VBox header = new VBox(5, titleLabel, subtitleLabel);
        header.setAlignment(Pos.CENTER);

        // Campi input
        TextField codFiscaleField = createTextField("Codice Fiscale");
        TextField nomeField = createTextField("Nome");
        TextField cognomeField = createTextField("Cognome");
        TextField emailField = createTextField("Email");

        DatePicker dataNascitaPicker = new DatePicker();
        dataNascitaPicker.setPromptText("Data di nascita");
        dataNascitaPicker.setPrefWidth(300);
        dataNascitaPicker.setStyle(
                "-fx-background-radius: 15; -fx-border-radius: 15;" +
                "-fx-border-color: #FF9966; -fx-border-width: 1.5;" +
                "-fx-padding: 0 10 0 10;"
        );

        TextField usernameField = createTextField("Username");
        PasswordField passwordField = createPasswordField("Password");

        CheckBox disponibilitaBox = new CheckBox("Disponibile");
        disponibilitaBox.setSelected(true);

        VBox form = new VBox(15,
                codFiscaleField,
                nomeField,
                cognomeField,
                emailField,
                dataNascitaPicker,
                usernameField,
                passwordField,
                disponibilitaBox
        );
        form.setAlignment(Pos.CENTER);

        // Messaggi
        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("Roboto", FontWeight.MEDIUM, 13));
        messageLabel.setTextFill(Color.web("#FF6600"));
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);

        // Pulsanti
        Button registraButton = createButton("REGISTRATI", "#FF6600", "#FF8533");
        Button annullaButton = createButton("ANNULLA", "#FFCC99", "#FFD9B3");

        HBox buttons = new HBox(15, annullaButton, registraButton);
        buttons.setAlignment(Pos.CENTER);

        // Eventi
        registraButton.setOnAction(e -> {
            try {
                if (codFiscaleField.getText().trim().isEmpty() ||
                    nomeField.getText().trim().isEmpty() ||
                    cognomeField.getText().trim().isEmpty() ||
                    emailField.getText().trim().isEmpty() ||
                    dataNascitaPicker.getValue() == null ||
                    usernameField.getText().trim().isEmpty() ||
                    passwordField.getText().trim().isEmpty()) {
                    messageLabel.setText("Tutti i campi sono obbligatori!");
                    messageLabel.setTextFill(Color.RED);
                    return;
                }

                // Non serve salvare in una variabile
                chefController.registraChef(
                        codFiscaleField.getText().trim(),
                        nomeField.getText().trim(),
                        cognomeField.getText().trim(),
                        emailField.getText().trim(),
                        dataNascitaPicker.getValue(),
                        disponibilitaBox.isSelected(),
                        usernameField.getText().trim(),
                        passwordField.getText().trim()
                );

                messageLabel.setText("✅ Registrazione completata! Puoi ora effettuare il login.");
                messageLabel.setTextFill(Color.web("#00AA00"));
            } catch (Exception ex) {
                messageLabel.setText("❌ Errore: " + ex.getMessage());
                messageLabel.setTextFill(Color.RED);
                ex.printStackTrace();
            }
        });

        annullaButton.setOnAction(e -> onAnnulla.run());

        getChildren().addAll(header, form, buttons, messageLabel);
    }

    private TextField createTextField(String placeholder) {
        TextField field = new TextField();
        field.setPromptText(placeholder);
        field.setPrefWidth(300);
        field.setPrefHeight(40);
        field.setFont(Font.font("Roboto", 13));
        field.setStyle(
                "-fx-background-radius: 15; -fx-border-radius: 15;" +
                "-fx-border-color: #FF9966; -fx-border-width: 1.5;" +
                "-fx-padding: 0 10 0 10;"
        );
        return field;
    }

    private PasswordField createPasswordField(String placeholder) {
        PasswordField field = new PasswordField();
        field.setPromptText(placeholder);
        field.setPrefWidth(300);
        field.setPrefHeight(40);
        field.setFont(Font.font("Roboto", 13));
        field.setStyle(
                "-fx-background-radius: 15; -fx-border-radius: 15;" +
                "-fx-border-color: #FF9966; -fx-border-width: 1.5;" +
                "-fx-padding: 0 10 0 10;"
        );
        return field;
    }

    private Button createButton(String text, String baseColor, String hoverColor) {
        Button button = new Button(text);
        button.setPrefSize(130, 40);
        button.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        button.setTextFill(Color.web("#4B2E2E"));
        button.setStyle("-fx-background-color: " + baseColor + "; -fx-background-radius: 20;");
        button.setOnMouseEntered(e ->
                button.setStyle("-fx-background-color: " + hoverColor + "; -fx-background-radius: 20;")
        );
        button.setOnMouseExited(e ->
                button.setStyle("-fx-background-color: " + baseColor + "; -fx-background-radius: 20;")
        );
        return button;
    }
}
