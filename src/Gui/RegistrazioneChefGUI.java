package Gui;

import controller.ChefController;
import guihelper.StyleHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import exceptions.ValidationException;
import exceptions.DataAccessException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

public class RegistrazioneChefGUI extends VBox {

    private static final double CARD_WIDTH = 500;
    private static final double CARD_HEIGHT = 700;
    private static final double FIELD_WIDTH = 250;

    private final ChefController chefController;
    private final Runnable tornaAlLogin;

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

    public RegistrazioneChefGUI(ChefController controller, Runnable tornaAlLoginCallback) {
        this.chefController = controller;
        this.tornaAlLogin = tornaAlLoginCallback;
        initUI();
    }

    private void initUI() {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(30)); // Ridotto da 40 a 30
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

        // LABEL ERRORE
        errorLabel = createErrorLabel();

        // CAMPI
        codFiscaleField = (TextField) createInputField("Codice Fiscale", false);
        nomeField = (TextField) createInputField("Nome", false);
        cognomeField = (TextField) createInputField("Cognome", false);
        emailField = (TextField) createInputField("Email", false);
        usernameField = (TextField) createInputField("Username", false);
        passwordField = (PasswordField) createInputField("Password", true);
        confermaPasswordField = (PasswordField) createInputField("Conferma Password", true);
        dataNascitaPicker = createStyledDatePicker();
        disponibilitaCheck = createDisponibilitaCheckBox();

        setupAutoResetListeners();

        VBox header = createHeader();
        GridPane form = createFormGrid();
        HBox buttons = createButtonBox();

        getChildren().addAll(header, errorLabel, form, buttons);
        setSpacing(15); // Ridotto da 20 a 15
    }

    private VBox createHeader() {
        Label title = new Label("👨‍🍳 Registrazione Chef");
        title.setFont(Font.font("Roboto", FontWeight.BOLD, 26)); // Ridotto da 28 a 26
        title.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        Label subtitle = new Label("Crea il tuo account");
        subtitle.setFont(Font.font("Roboto", FontWeight.NORMAL, 13)); // Ridotto da 14 a 13
        subtitle.setTextFill(Color.web(StyleHelper.PRIMARY_LIGHT));

        VBox box = new VBox(8, title, subtitle); // Ridotto spacing da 10 a 8
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private Label createErrorLabel() {
        Label lbl = new Label();
        lbl.setFont(Font.font("Roboto", FontWeight.BOLD, 11)); // Ridotto da 12 a 11
        lbl.setTextFill(Color.RED);
        lbl.setVisible(false);
        lbl.setWrapText(true);
        lbl.setMaxWidth(CARD_WIDTH - 50);
        lbl.setAlignment(Pos.CENTER);
        lbl.setStyle("""
            -fx-background-color: #ffe6e6;
            -fx-padding: 10;
            -fx-background-radius: 10;
            -fx-border-color: red;
            -fx-border-width: 2;
            -fx-border-radius: 10;
        """);
        return lbl;
    }

    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10); // Ridotto da 12 a 10
        grid.setAlignment(Pos.CENTER);

        int row = 0;
        grid.add(new Label("Codice Fiscale:"), 0, row);
        grid.add(codFiscaleField, 1, row++);
        grid.add(new Label("Nome:"), 0, row);
        grid.add(nomeField, 1, row++);
        grid.add(new Label("Cognome:"), 0, row);
        grid.add(cognomeField, 1, row++);
        grid.add(new Label("Email:"), 0, row);
        grid.add(emailField, 1, row++);
        grid.add(new Label("Data di Nascita:"), 0, row);
        grid.add(dataNascitaPicker, 1, row++);
        grid.add(disponibilitaCheck, 1, row++);
        grid.add(new Label("Username:"), 0, row);
        grid.add(usernameField, 1, row++);
        grid.add(new Label("Password:"), 0, row);
        grid.add(passwordField, 1, row++);
        grid.add(new Label("Conferma Password:"), 0, row);
        grid.add(confermaPasswordField, 1, row);

        return grid;
    }

    private HBox createButtonBox() {
        Button reg = StyleHelper.createSuccessButton("REGISTRATI");
        Button back = StyleHelper.createDangerButton("TORNA INDIETRO");
        reg.setPrefSize(150, 42); 
        back.setPrefSize(150, 42);

        reg.setOnAction(e -> handleRegistration());
        back.setOnAction(e -> {
            nascondiErrore();
            tornaAlLogin.run();
        });

        HBox box = new HBox(15, back, reg);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private void handleRegistration() {
        nascondiErrore();

        boolean anyEmpty = Stream.of(codFiscaleField, nomeField, cognomeField, emailField, usernameField,
                passwordField, confermaPasswordField).anyMatch(f -> f.getText().trim().isEmpty());

        if (anyEmpty) {
            mostraErrore("❌ Compilare tutti i campi obbligatori");
            return;
        }
        if (!passwordField.getText().equals(confermaPasswordField.getText())) {
            mostraErrore("❌ Le password non coincidono");
            return;
        }

        try {
            chefController.registraChef(
                    codFiscaleField.getText().trim(),
                    nomeField.getText().trim(),
                    cognomeField.getText().trim(),
                    emailField.getText().trim(),
                    dataNascitaPicker.getValue(),
                    disponibilitaCheck.isSelected(),
                    usernameField.getText().trim(),
                    passwordField.getText()
            );
            StyleHelper.showSuccessDialog("Registrazione Completata",
                    "Account creato con successo!\nOra puoi effettuare il login.");
            tornaAlLogin.run();

        } catch (ValidationException | IllegalArgumentException ex) {
            mostraErrore("❌ " + ex.getMessage());
        } catch (DataAccessException ex) {
            mostraErrore("❌ " + Optional.ofNullable(ex.getMessage()).orElse("Errore database"));
            ex.printStackTrace();
        } catch (Exception ex) {
            mostraErrore("❌ Errore imprevisto");
            ex.printStackTrace();
        }
    }

    private void setupAutoResetListeners() {
        Stream.concat(Stream.of(codFiscaleField, nomeField, cognomeField, emailField, usernameField,
                        passwordField, confermaPasswordField),
                Stream.of(dataNascitaPicker)).forEach(ctrl -> {
            if (ctrl instanceof TextInputControl tic) {
                tic.textProperty().addListener((o, v1, v2) -> nascondiErrore());
            } else if (ctrl instanceof DatePicker dp) {
                dp.valueProperty().addListener((o, v1, v2) -> nascondiErrore());
            }
        });
    }

    private TextInputControl createInputField(String prompt, boolean isPassword) {
        TextInputControl field = isPassword ? new PasswordField() : new TextField();
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

    private DatePicker createStyledDatePicker() {
        DatePicker picker = new DatePicker();
        picker.setPrefWidth(FIELD_WIDTH);
        picker.setPromptText("Seleziona data");
        picker.setValue(LocalDate.of(2000, 1, 1));
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

        picker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (!empty && (date.isAfter(LocalDate.now()) || date.isBefore(LocalDate.of(1900, 1, 1)))) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffd6d6;");
                }
            }
        });

        picker.focusedProperty().addListener((obs, oldV, newV) -> {
            if (Boolean.TRUE.equals(newV)) {
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

    private CheckBox createDisponibilitaCheckBox() {
        CheckBox cb = new CheckBox("Disponibile per insegnare");
        cb.setSelected(true);
        cb.setFont(Font.font("Roboto", 12));
        cb.setTextFill(Color.web("#2C3E50"));
        return cb;
    }

    private void mostraErrore(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    private void nascondiErrore() {
        errorLabel.setVisible(false);
    }
}