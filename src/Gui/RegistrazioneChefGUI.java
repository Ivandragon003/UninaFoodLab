package Gui;

import controller.ChefController;
import guihelper.StyleHelper;
import guihelper.ValidationHelper;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import exceptions.ValidationException;


import java.time.LocalDate;

public class RegistrazioneChefGUI extends VBox {
    private final ChefController chefController;
    private final Runnable tornaAlLogin;
    private Label errorLabel;
    private TextField codFiscaleField, nomeField, cognomeField, emailField, usernameField;
    private PasswordField passwordField, confermaPasswordField;
    private DatePicker dataNascitaPicker;
    private CheckBox disponibilitaCheck;

    public RegistrazioneChefGUI(ChefController controller, Runnable tornaAlLoginCallback) {
        this.chefController = controller;
        this.tornaAlLogin = tornaAlLoginCallback;
        initUI();
    }

    private void initUI() {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(40));
        setPrefSize(500, 650);
        setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 25;
            -fx-border-color: #FF9966;
            -fx-border-width: 2;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4);
        """);

        Label title = new Label("👨‍🍳 Registrazione Chef");
        title.setFont(Font.font("Roboto", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));
        Label subtitle = new Label("Crea il tuo account");
        subtitle.setFont(Font.font("Roboto", 14));
        subtitle.setTextFill(Color.web(StyleHelper.PRIMARY_LIGHT));

        errorLabel = new Label();
        errorLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
        errorLabel.setTextFill(Color.RED);
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setAlignment(Pos.CENTER);
        errorLabel.setStyle("""
            -fx-background-color: #ffe6e6;
            -fx-padding: 12;
            -fx-border-color: red;
            -fx-border-width: 2;
            -fx-border-radius: 10;
        """);

        codFiscaleField = createField("Codice Fiscale");
        nomeField      = createField("Nome", ValidationHelper.getLettersOnlyFormatter());
        cognomeField   = createField("Cognome", ValidationHelper.getLettersOnlyFormatter());
        emailField     = createField("Email");
        dataNascitaPicker = new DatePicker(LocalDate.of(2000,1,1));
        disponibilitaCheck = new CheckBox("Disponibile per insegnare");
        disponibilitaCheck.setSelected(true);
        usernameField  = createField("Username");
        passwordField  = createPasswordField("Password");
        confermaPasswordField = createPasswordField("Conferma Password");

        ValidationHelper.addAutoResetListener(codFiscaleField, null, errorLabel);
        ValidationHelper.addAutoResetListener(nomeField, null, errorLabel);
        ValidationHelper.addAutoResetListener(cognomeField, null, errorLabel);
        ValidationHelper.addAutoResetListener(emailField, null, errorLabel);
        ValidationHelper.addAutoResetListener(usernameField, null, errorLabel);
        ValidationHelper.addAutoResetListener(passwordField, null, errorLabel);
        ValidationHelper.addAutoResetListener(confermaPasswordField, null, errorLabel);

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(12); grid.setAlignment(Pos.CENTER);
        String[] labels = {"Codice Fiscale","Nome","Cognome","Email","Data di Nascita","","Username","Password","Conferma Password"};
        Control[] controls = {codFiscaleField,nomeField,cognomeField,emailField,dataNascitaPicker,disponibilitaCheck,usernameField,passwordField,confermaPasswordField};
        int r=0;
        for(int i=0;i<labels.length;i++){
            if(!labels[i].isEmpty()) grid.add(new Label(labels[i]+":"),0,r);
            grid.add(controls[i],1,r++);
        }

        Button reg = StyleHelper.createSuccessButton("REGISTRATI");
        reg.setOnAction(e -> handleRegistration());
        Button back = StyleHelper.createDangerButton("TORNA INDIETRO");
        back.setOnAction(e -> { errorLabel.setVisible(false); tornaAlLogin.run(); });
        HBox buttons = new HBox(15, back, reg);
        buttons.setAlignment(Pos.CENTER);

        getChildren().setAll(new VBox(10,title,subtitle), errorLabel, grid, buttons);
    }

    private TextField createField(String prompt, TextFormatter<String>... fmt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setPrefWidth(250);
        f.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-border-color: #FF9966;
            -fx-border-width: 1.5;
        """);
        if(fmt.length>0) f.setTextFormatter(fmt[0]);
        return f;
    }
    private PasswordField createPasswordField(String prompt) {
        PasswordField p = new PasswordField();
        p.setPromptText(prompt);
        p.setPrefWidth(250);
        p.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-border-color: #FF9966;
            -fx-border-width: 1.5;
        """);
        return p;
    }

    private void handleRegistration() {
        errorLabel.setVisible(false);
        // valida tutti i campi obbligatori
        if(!ValidationHelper.validateNotEmpty(codFiscaleField,null,errorLabel,"il codice fiscale")
         ||!ValidationHelper.validateNotEmpty(nomeField,null,errorLabel,"il nome")
         ||!ValidationHelper.validateNotEmpty(cognomeField,null,errorLabel,"il cognome")
         ||!ValidationHelper.validateNotEmpty(emailField,null,errorLabel,"l'email")
         || dataNascitaPicker.getValue()==null
         ||!ValidationHelper.validateNotEmpty(usernameField,null,errorLabel,"l'username")
         ||!ValidationHelper.validateNotEmpty(passwordField,null,errorLabel,"la password")
         ||!ValidationHelper.validateNotEmpty(confermaPasswordField,null,errorLabel,"la conferma password"))
            return;

        if(!passwordField.getText().equals(confermaPasswordField.getText())){
            errorLabel.setText("❌ Le password non coincidono");
            errorLabel.setVisible(true);
            confermaPasswordField.requestFocus();
            return;
        }

        try{
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
            StyleHelper.showSuccessDialog("Registrazione completata","Account creato!");
            tornaAlLogin.run();
        }catch(ValidationException|IllegalArgumentException ex){
            errorLabel.setText("❌ " + ex.getMessage());
            errorLabel.setVisible(true);
        }catch(Exception ex){
            errorLabel.setText("❌ Errore imprevisto");
            errorLabel.setVisible(true);
            ex.printStackTrace();
        }
    }
}
