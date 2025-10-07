package Gui;

import controller.ChefController;
import util.StyleHelper;
import util.ValidationHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Chef;
import exceptions.*;

import java.time.LocalDate;

public class RegistrazioneChefGUI extends VBox {

    private final ChefController chefController;
    private final Runnable tornaAlLogin;
    private Label errorLabel;

    public RegistrazioneChefGUI(ChefController controller, Runnable tornaAlLoginCallback) {
        this.chefController = controller;
        this.tornaAlLogin = tornaAlLoginCallback;

        initUI();
    }

    private void initUI() {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(40));
        setPrefSize(500, 650);
        setMaxSize(500, 650);
        setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 25;
            -fx-border-radius: 25;
            -fx-border-color: #FF9966;
            -fx-border-width: 2;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4);
        """);

        // Titolo e sottotitolo
        Label titleLabel = new Label("👨‍🍳 Registrazione Chef");
        titleLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        Label subtitleLabel = new Label("Crea il tuo account");
        subtitleLabel.setFont(Font.font("Roboto", FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(Color.web(StyleHelper.PRIMARY_LIGHT));

        errorLabel = new Label();
        errorLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 11));
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(450);
        errorLabel.setAlignment(Pos.CENTER);
        errorLabel.setStyle("""
                -fx-background-color: #ffe6e6;
                -fx-padding: 8;
                -fx-background-radius: 10;
                -fx-border-color: red;
                -fx-border-width: 1;
                -fx-border-radius: 10;
            """);

        // Campi
        TextField codFiscaleField = createField("Codice Fiscale");
        TextField nomeField = createField("Nome");
        TextField cognomeField = createField("Cognome");
        TextField emailField = createField("Email");
        DatePicker dataNascitaPicker = createDatePicker();
        CheckBox disponibilitaCheck = new CheckBox("Disponibile per insegnare");
        disponibilitaCheck.setSelected(true);
        disponibilitaCheck.setFont(Font.font("Roboto", 12));
        TextField usernameField = createField("Username");
        PasswordField passwordField = createPasswordField("Password");
        PasswordField confermaPasswordField = createPasswordField("Conferma Password");

        // Listener per reset errori
        TextInputControl[] inputs = {codFiscaleField, nomeField, cognomeField, emailField,
                                    usernameField, passwordField, confermaPasswordField};
        for (TextInputControl input : inputs) {
            input.textProperty().addListener((obs, oldVal, newVal) -> nascondiErrore());
        }

        // Bottoni
        Button registratiButton = StyleHelper.createSuccessButton("REGISTRATI");
        registratiButton.setPrefSize(150, 45);

        Button tornaButton = StyleHelper.createDangerButton("TORNA INDIETRO");
        tornaButton.setPrefSize(150, 45);

        setupRegistrationButton(registratiButton, codFiscaleField, nomeField, cognomeField,
                               emailField, dataNascitaPicker, disponibilitaCheck,
                               usernameField, passwordField, confermaPasswordField);

        tornaButton.setOnAction(e -> {
            nascondiErrore();
            tornaAlLogin.run();
        });

        // Layout
        VBox headerBox = new VBox(10, titleLabel, subtitleLabel);
        headerBox.setAlignment(Pos.CENTER);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(12);
        formGrid.setAlignment(Pos.CENTER);

        formGrid.add(new Label("Codice Fiscale:"), 0, 0);
        formGrid.add(codFiscaleField, 1, 0);
        formGrid.add(new Label("Nome:"), 0, 1);
        formGrid.add(nomeField, 1, 1);
        formGrid.add(new Label("Cognome:"), 0, 2);
        formGrid.add(cognomeField, 1, 2);
        formGrid.add(new Label("Email:"), 0, 3);
        formGrid.add(emailField, 1, 3);
        formGrid.add(new Label("Data di Nascita:"), 0, 4);
        formGrid.add(dataNascitaPicker, 1, 4);
        formGrid.add(disponibilitaCheck, 1, 5);
        formGrid.add(new Label("Username:"), 0, 6);
        formGrid.add(usernameField, 1, 6);
        formGrid.add(new Label("Password:"), 0, 7);
        formGrid.add(passwordField, 1, 7);
        formGrid.add(new Label("Conferma Password:"), 0, 8);
        formGrid.add(confermaPasswordField, 1, 8);

        HBox buttonBox = new HBox(15, tornaButton, registratiButton);
        buttonBox.setAlignment(Pos.CENTER);

        getChildren().addAll(headerBox, errorLabel, formGrid, buttonBox);
        setSpacing(20);
    }

    // --- CREAZIONE CAMPI ---
    private TextField createField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefWidth(250);
        field.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-border-radius: 10;
            -fx-border-color: #FF9966;
            -fx-border-width: 1.5;
            -fx-padding: 8;
        """);
        return field;
    }

    private PasswordField createPasswordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setPrefWidth(250);
        field.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-border-radius: 10;
            -fx-border-color: #FF9966;
            -fx-border-width: 1.5;
            -fx-padding: 8;
        """);
        return field;
    }

    // --- DATEPICKER OTTIMIZZATO ---
    private DatePicker createDatePicker() {
        DatePicker picker = new DatePicker();
        picker.setPrefWidth(250);
        picker.setPromptText("Seleziona data");
        picker.setStyle("""
            -fx-background-color: #FFF8F0;
            -fx-background-radius: 10;
            -fx-border-radius: 10;
            -fx-border-color: #FF9966;
            -fx-border-width: 2;
            -fx-padding: 5;
            -fx-font-size: 14px;
            -fx-font-family: 'Roboto';
        """);

        // Blocca date future o troppo vecchie
        picker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty) return;
                if (date.isAfter(LocalDate.now()) || date.isBefore(LocalDate.of(1900, 1, 1))) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffd6d6;");
                }
            }
        });

        // Effetto focus
        picker.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                picker.setStyle("""
                    -fx-background-color: #FFF8F0;
                    -fx-border-color: #FF6600;
                    -fx-border-width: 2;
                    -fx-background-radius: 10;
                    -fx-border-radius: 10;
                """);
            } else {
                picker.setStyle("""
                    -fx-background-color: #FFF8F0;
                    -fx-border-color: #FF9966;
                    -fx-border-width: 2;
                    -fx-background-radius: 10;
                    -fx-border-radius: 10;
                """);
            }
        });

        return picker;
    }

    // --- REGISTRAZIONE ---
    private void setupRegistrationButton(Button button, TextField codFiscaleField, TextField nomeField,
                                         TextField cognomeField, TextField emailField, DatePicker dataNascitaPicker,
                                         CheckBox disponibilitaCheck, TextField usernameField,
                                         PasswordField passwordField, PasswordField confermaPasswordField) {
        button.setOnAction(e -> handleRegistration(
            codFiscaleField.getText(),
            nomeField.getText(),
            cognomeField.getText(),
            emailField.getText(),
            dataNascitaPicker.getValue(),
            disponibilitaCheck.isSelected(),
            usernameField.getText(),
            passwordField.getText(),
            confermaPasswordField.getText()
        ));
    }

    private void handleRegistration(String codFiscale, String nome, String cognome, String email,
                                    LocalDate dataNascita, boolean disponibilita,
                                    String username, String password, String confermaPassword) {
        nascondiErrore();

        try {
            if (!password.equals(confermaPassword)) {
                mostraErrore("❌ Le password non coincidono");
                return;
            }

            Chef chef = chefController.registraChef(codFiscale, nome, cognome, email,
                                                    dataNascita, disponibilita, username, password);

            StyleHelper.showSuccessDialog("Registrazione Completata",
                "Account creato con successo! Ora puoi effettuare il login.");

            tornaAlLogin.run();

        } catch (ValidationException ex) {
            mostraErrore("❌ " + ex.getMessage());

        } catch (DataAccessException ex) {
            mostraErrore("❌ Errore di connessione al database");

        } catch (Exception ex) {
            mostraErrore("❌ Errore durante la registrazione: " + ex.getMessage());
        }
    }

    private void mostraErrore(String messaggio) {
        errorLabel.setText(messaggio);
        errorLabel.setVisible(true);
    }

    private void nascondiErrore() {
        errorLabel.setVisible(false);
    }
}
