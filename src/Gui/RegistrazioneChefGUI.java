package Gui;

import controller.ChefController;
import guihelper.StyleHelper;
import guihelper.ValidationHelper;
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

    // ========== COSTANTI ==========
    private static final double CARD_WIDTH = 500;
    private static final double CARD_HEIGHT = 650;
    private static final double FIELD_WIDTH = 250;
    
    // ========== DIPENDENZE ==========
    private final ChefController chefController;
    private final Runnable tornaAlLogin;
    
    // ========== COMPONENTI UI ==========
    private Label errorLabel;
    private TextField codFiscaleField;
    private TextField nomeField;
    private TextField cognomeField;
    private TextField emailField;
    private DatePicker dataNascitaPicker;
    private CheckBox disponibilitaCheck;
    private TextField usernameField;
    private PasswordField passwordField;
    private PasswordField confermaPasswordField;

    // ========== COSTRUTTORE ==========
    public RegistrazioneChefGUI(ChefController controller, Runnable tornaAlLoginCallback) {
        this.chefController = controller;
        this.tornaAlLogin = tornaAlLoginCallback;
        initUI();
    }

    // ========== INIZIALIZZAZIONE UI ==========
    private void initUI() {
        setupContainer();
        
        // Header
        VBox headerBox = createHeader();
        
        // Error Label
        errorLabel = createErrorLabel();
        
        // Form
        GridPane formGrid = createFormGrid();
        
        // Buttons
        HBox buttonBox = createButtonBox();

        getChildren().addAll(headerBox, errorLabel, formGrid, buttonBox);
        setSpacing(20);
    }

    // ========== SETUP CONTAINER ==========
    private void setupContainer() {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(40));
        setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        setMaxSize(CARD_WIDTH, CARD_HEIGHT);
        setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 25;
            -fx-border-radius: 25;
            -fx-border-color: #FF9966;
            -fx-border-width: 2;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4);
        """);
    }

    // ========== HEADER ==========
    private VBox createHeader() {
        Label titleLabel = new Label("👨‍🍳 Registrazione Chef");
        titleLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        Label subtitleLabel = new Label("Crea il tuo account");
        subtitleLabel.setFont(Font.font("Roboto", FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(Color.web(StyleHelper.PRIMARY_LIGHT));

        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.getChildren().addAll(titleLabel, subtitleLabel);
        return header;
    }

    // ========== ERROR LABEL ==========
    private Label createErrorLabel() {
        Label label = new Label();
        label.setFont(Font.font("Roboto", FontWeight.BOLD, 11));
        label.setTextFill(Color.RED);
        label.setVisible(false);
        label.setWrapText(true);
        label.setMaxWidth(450);
        label.setAlignment(Pos.CENTER);
        label.setStyle("""
            -fx-background-color: #ffe6e6;
            -fx-padding: 8;
            -fx-background-radius: 10;
            -fx-border-color: red;
            -fx-border-width: 1;
            -fx-border-radius: 10;
        """);
        return label;
    }

    // ========== FORM GRID ==========
    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setAlignment(Pos.CENTER);

        // Creazione campi
        codFiscaleField = createTextField("Codice Fiscale");
        nomeField = createTextField("Nome");
        cognomeField = createTextField("Cognome");
        emailField = createTextField("Email");
        dataNascitaPicker = createDatePicker();
        disponibilitaCheck = createDisponibilitaCheckBox();
        usernameField = createTextField("Username");
        passwordField = createPasswordField("Password");
        confermaPasswordField = createPasswordField("Conferma Password");

        // Listener per reset errori
        setupAutoResetListeners();

        // Aggiunta al grid
        int row = 0;
        grid.add(createLabel("Codice Fiscale:"), 0, row);
        grid.add(codFiscaleField, 1, row++);
        
        grid.add(createLabel("Nome:"), 0, row);
        grid.add(nomeField, 1, row++);
        
        grid.add(createLabel("Cognome:"), 0, row);
        grid.add(cognomeField, 1, row++);
        
        grid.add(createLabel("Email:"), 0, row);
        grid.add(emailField, 1, row++);
        
        grid.add(createLabel("Data di Nascita:"), 0, row);
        grid.add(dataNascitaPicker, 1, row++);
        
        grid.add(disponibilitaCheck, 1, row++);
        
        grid.add(createLabel("Username:"), 0, row);
        grid.add(usernameField, 1, row++);
        
        grid.add(createLabel("Password:"), 0, row);
        grid.add(passwordField, 1, row++);
        
        grid.add(createLabel("Conferma Password:"), 0, row);
        grid.add(confermaPasswordField, 1, row);

        return grid;
    }

    // ========== CREAZIONE LABEL ==========
    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Roboto", FontWeight.NORMAL, 12));
        label.setTextFill(Color.web("#2C3E50"));
        return label;
    }

    // ========== CREAZIONE TEXT FIELD ==========
    private TextField createTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefWidth(FIELD_WIDTH);
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

    // ========== CREAZIONE PASSWORD FIELD ==========
    private PasswordField createPasswordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setPrefWidth(FIELD_WIDTH);
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

    // ========== CREAZIONE DATE PICKER ==========
    private DatePicker createDatePicker() {
        DatePicker picker = new DatePicker();
        picker.setPrefWidth(FIELD_WIDTH);
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
                if (empty || date == null) return;
                
                if (date.isAfter(LocalDate.now()) || date.isBefore(LocalDate.of(1900, 1, 1))) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffd6d6;");
                }
            }
        });

        // Effetto focus
        picker.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (Boolean.TRUE.equals(newVal)) {
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

    // ========== CREAZIONE CHECKBOX ==========
    private CheckBox createDisponibilitaCheckBox() {
        CheckBox checkBox = new CheckBox("Disponibile per insegnare");
        checkBox.setSelected(true);
        checkBox.setFont(Font.font("Roboto", 12));
        checkBox.setTextFill(Color.web("#2C3E50"));
        return checkBox;
    }

    // ========== SETUP AUTO RESET LISTENERS ==========
    private void setupAutoResetListeners() {
        TextInputControl[] fields = {
            codFiscaleField, nomeField, cognomeField, emailField,
            usernameField, passwordField, confermaPasswordField
        };
        
        for (TextInputControl field : fields) {
            field.textProperty().addListener((obs, oldVal, newVal) -> nascondiErrore());
        }
        
        dataNascitaPicker.valueProperty().addListener((obs, oldVal, newVal) -> nascondiErrore());
    }

    // ========== BUTTON BOX ==========
    private HBox createButtonBox() {
        Button registratiButton = StyleHelper.createSuccessButton("REGISTRATI");
        registratiButton.setPrefSize(150, 45);
        registratiButton.setOnAction(e -> handleRegistration());

        Button tornaButton = StyleHelper.createDangerButton("TORNA INDIETRO");
        tornaButton.setPrefSize(150, 45);
        tornaButton.setOnAction(e -> {
            nascondiErrore();
            tornaAlLogin.run();
        });

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(tornaButton, registratiButton);
        return buttonBox;
    }

    // ========== HANDLE REGISTRATION ==========
    private void handleRegistration() {
        nascondiErrore();

        // Validazione campi vuoti
        if (!validateFields()) {
            return;
        }

        // Validazione password match
        if (!passwordField.getText().equals(confermaPasswordField.getText())) {
            mostraErrore("❌ Le password non coincidono");
            return;
        }

        try {
            Chef chef = chefController.registraChef(
                codFiscaleField.getText().trim(),
                nomeField.getText().trim(),
                cognomeField.getText().trim(),
                emailField.getText().trim(),
                dataNascitaPicker.getValue(),
                disponibilitaCheck.isSelected(),
                usernameField.getText().trim(),
                passwordField.getText()
            );

            StyleHelper.showSuccessDialog(
                "Registrazione Completata",
                "Account creato con successo!\nOra puoi effettuare il login."
            );

            tornaAlLogin.run();

        } catch (ValidationException ex) {
            mostraErrore("❌ " + ex.getMessage());

        } catch (DataAccessException ex) {
            mostraErrore("❌ Errore di connessione al database");

        } catch (Exception ex) {
            mostraErrore("❌ Errore durante la registrazione: " + ex.getMessage());
        }
    }

    // ========== VALIDAZIONE CAMPI ==========
    private boolean validateFields() {
        if (codFiscaleField.getText() == null || codFiscaleField.getText().trim().isEmpty()) {
            mostraErrore("❌ Inserisci il codice fiscale");
            codFiscaleField.requestFocus();
            return false;
        }

        if (nomeField.getText() == null || nomeField.getText().trim().isEmpty()) {
            mostraErrore("❌ Inserisci il nome");
            nomeField.requestFocus();
            return false;
        }

        if (cognomeField.getText() == null || cognomeField.getText().trim().isEmpty()) {
            mostraErrore("❌ Inserisci il cognome");
            cognomeField.requestFocus();
            return false;
        }

        if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
            mostraErrore("❌ Inserisci l'email");
            emailField.requestFocus();
            return false;
        }

        if (dataNascitaPicker.getValue() == null) {
            mostraErrore("❌ Seleziona la data di nascita");
            dataNascitaPicker.requestFocus();
            return false;
        }

        if (usernameField.getText() == null || usernameField.getText().trim().isEmpty()) {
            mostraErrore("❌ Inserisci l'username");
            usernameField.requestFocus();
            return false;
        }

        if (passwordField.getText() == null || passwordField.getText().isEmpty()) {
            mostraErrore("❌ Inserisci la password");
            passwordField.requestFocus();
            return false;
        }

        if (confermaPasswordField.getText() == null || confermaPasswordField.getText().isEmpty()) {
            mostraErrore("❌ Conferma la password");
            confermaPasswordField.requestFocus();
            return false;
        }

        return true;
    }

    // ========== GESTIONE ERRORI ==========
    private void mostraErrore(String messaggio) {
        errorLabel.setText(messaggio);
        errorLabel.setVisible(true);
    }

    private void nascondiErrore() {
        errorLabel.setVisible(false);
    }
}