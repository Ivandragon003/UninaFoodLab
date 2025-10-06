package Gui;

import controller.GestioneSessioniController;
import model.*;
import util.StyleHelper;
import exceptions.ValidationException;
import exceptions.ErrorMessages;
import exceptions.ValidationUtils;
import exceptions.DataAccessException;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class GestioneSessioniGUI {

    private CorsoCucina corso;
    private GestioneSessioniController controller;
    private Stage primaryStage;
    private Sessione sessione;
    private boolean modalitaAggiunta = false;
    private InPresenza sessioneTemporanea;

    private ComboBox<String> tipoCombo;
    private DatePicker dataInizioPicker, dataFinePicker;
    private TextField oraInizioField, oraFineField;
    private TextField piattaformaField, viaField, cittaField, postiField, capField;
    private VBox campiOnlineBox, campiPresenzaBox;
    private HBox pulsantiBox;
    private VBox root;

    public void setCorso(CorsoCucina corso) { this.corso = corso; }
    public void setController(GestioneSessioniController controller) { this.controller = controller; }
    public void setSessione(Sessione sessione) {
        this.sessione = sessione;
        this.modalitaAggiunta = false;
    }
    public void setModalitaAggiunta(boolean modalitaAggiunta) {
        this.modalitaAggiunta = modalitaAggiunta;
        this.sessione = null;
        this.sessioneTemporanea = null;
    }

    public VBox getRoot() {
        if (root == null) {
            root = buildRoot();
            if (!modalitaAggiunta && sessione != null) popolaForm();
            else if (modalitaAggiunta) {
                tipoCombo.setValue("Online");
                aggiornaVisibilitaCampi();
            }
        }
        return root;
    }

    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setScene(new Scene(getRoot(), 650, 750));
        stage.setTitle("Gestione Sessione");
        stage.show();
    }

    private VBox buildRoot() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(25));
        container.setStyle("-fx-background-color: #FAFAFA;");

        Label titolo = new Label(modalitaAggiunta ? "➕ Aggiungi Sessione" : "✏️ Modifica Sessione");
        titolo.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#FF6600;");

        VBox form = createForm();
        pulsantiBox = createPulsanti();

        container.getChildren().addAll(titolo, form, pulsantiBox);
        return container;
    }

    private VBox createForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background:white;-fx-border-color:#E0E0E0;-fx-border-radius:8;-fx-background-radius:8;");

        // Tipo
        HBox tipoBox = new HBox(10);
        tipoBox.setAlignment(Pos.CENTER_LEFT);
        Label tipoLbl = new Label("Tipo Sessione:");
        tipoLbl.setStyle("-fx-font-weight:bold;-fx-min-width:120;");
        tipoCombo = new ComboBox<>();
        tipoCombo.getItems().addAll("Online","In Presenza");
        tipoCombo.setOnAction(e -> aggiornaVisibilitaCampi());
        tipoBox.getChildren().addAll(tipoLbl, tipoCombo);

        // Date e orari
        GridPane dateGrid = new GridPane();
        dateGrid.setHgap(15); dateGrid.setVgap(10);
        dataInizioPicker = new DatePicker();
        dataFinePicker   = new DatePicker();
        oraInizioField   = new TextField(); oraInizioField.setPromptText("HH:MM");
        oraFineField     = new TextField(); oraFineField.setPromptText("HH:MM");

        dateGrid.add(new Label("Data Inizio:"),0,0);
        dateGrid.add(dataInizioPicker,1,0);
        dateGrid.add(new Label("Ora Inizio:"),2,0);
        dateGrid.add(oraInizioField,3,0);
        dateGrid.add(new Label("Data Fine:"),0,1);
        dateGrid.add(dataFinePicker,1,1);
        dateGrid.add(new Label("Ora Fine:"),2,1);
        dateGrid.add(oraFineField,3,1);

        // Online fields
        campiOnlineBox = new VBox(10);
        piattaformaField = new TextField(); piattaformaField.setPromptText("Zoom, Teams...");
        campiOnlineBox.getChildren().add(new HBox(10,new Label("Piattaforma:"),piattaformaField));

        // Presenza fields
        campiPresenzaBox = new VBox(10);
        GridPane presGrid = new GridPane();
        presGrid.setHgap(15); presGrid.setVgap(10);
        viaField = new TextField(); cittaField = new TextField();
        postiField = new TextField(); capField = new TextField();

        presGrid.add(new Label("Via:"),0,0); presGrid.add(viaField,1,0);
        presGrid.add(new Label("Città:"),2,0);presGrid.add(cittaField,3,0);
        presGrid.add(new Label("Posti:"),0,1);presGrid.add(postiField,1,1);
        presGrid.add(new Label("CAP:"),2,1); presGrid.add(capField,3,1);

        campiPresenzaBox.getChildren().add(presGrid);

        form.getChildren().addAll(tipoBox,new Separator(),dateGrid,new Separator(),campiOnlineBox,campiPresenzaBox);
        return form;
    }

    private HBox createPulsanti() {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20,0,0,0));

        Button ann = new Button("❌ Annulla");
        ann.setOnAction(e->primaryStage.hide());

        Button sal = new Button(modalitaAggiunta?"💾 Salva":"💾 Aggiorna");
        sal.setStyle("-fx-background-color:#4CAF50;-fx-text-fill:white;");
        sal.setOnAction(e->salvaSessione());

        box.getChildren().addAll(ann,sal);
        return box;
    }

    private void aggiornaVisibilitaCampi() {
        boolean online = "Online".equals(tipoCombo.getValue());
        campiOnlineBox.setVisible(online);
        campiOnlineBox.setManaged(online);
        campiPresenzaBox.setVisible(!online);
        campiPresenzaBox.setManaged(!online);
    }

    private void popolaForm() {
        tipoCombo.setValue(sessione instanceof Online ? "Online":"In Presenza");
        dataInizioPicker.setValue(sessione.getDataInizioSessione().toLocalDate());
        oraInizioField.setText(sessione.getDataInizioSessione().toLocalTime().toString());
        dataFinePicker.setValue(sessione.getDataFineSessione().toLocalDate());
        oraFineField.setText(sessione.getDataFineSessione().toLocalTime().toString());

        if (sessione instanceof Online o) {
            piattaformaField.setText(o.getPiattaformaStreaming());
        } else if (sessione instanceof InPresenza ip) {
            viaField.setText(ip.getVia());
            cittaField.setText(ip.getCitta());
            postiField.setText(String.valueOf(ip.getNumeroPosti()));
            capField.setText(String.valueOf(ip.getCAP()));
        }
        aggiornaVisibilitaCampi();
    }

    private void salvaSessione() {
        try {
            ValidationUtils.validateNotNull(dataInizioPicker.getValue(),"Data inizio");
            ValidationUtils.validateNotNull(dataFinePicker.getValue(),"Data fine");
            LocalTime ti = LocalTime.parse(oraInizioField.getText().trim());
            LocalTime tf = LocalTime.parse(oraFineField.getText().trim());
            if (!tf.isAfter(ti)) throw new ValidationException(ErrorMessages.DATA_FINE_SESSIONE_PRECEDENTE);

            LocalDateTime inizio = LocalDateTime.of(dataInizioPicker.getValue(),ti);
            LocalDateTime fine   = LocalDateTime.of(dataFinePicker.getValue(),tf);

            if ("Online".equals(tipoCombo.getValue())) {
                String p = piattaformaField.getText().trim();
                ValidationUtils.validateNotEmpty(p,ErrorMessages.PIATTAFORMA_MANCANTE);
                Online o = modalitaAggiunta
                    ? new Online(inizio,fine,p)
                    : (Online) sessione;
                o.setDataInizioSessione(inizio);
                o.setDataFineSessione(fine);
                o.setPiattaformaStreaming(p);
                o.setCorsoCucina(corso);

                if (modalitaAggiunta) controller.aggiungiSessione(o,new ArrayList<>());
                else controller.aggiornaSessione(sessione,o);

            } else {
                String via = viaField.getText().trim(), cit = cittaField.getText().trim();
                ValidationUtils.validateNotEmpty(via,ErrorMessages.VIA_MANCANTE);
                ValidationUtils.validateNotEmpty(cit,ErrorMessages.CITTA_MANCANTE);
                int posti = Integer.parseInt(postiField.getText().trim());
                if (posti<=0) throw new ValidationException(ErrorMessages.POSTI_NON_VALIDI);
                int cap = Integer.parseInt(capField.getText().trim());
                if (cap<10000||cap>99999) throw new ValidationException(ErrorMessages.CAP_NON_VALIDO);

                InPresenza ip = modalitaAggiunta
                    ? new InPresenza(inizio,fine,via,cit,posti,cap)
                    : (InPresenza)sessione;
                ip.setDataInizioSessione(inizio);
                ip.setDataFineSessione(fine);
                ip.setVia(via);
                ip.setCitta(cit);
                ip.setNumeroPosti(posti);
                ip.setCAP(cap);
                ip.setCorsoCucina(corso);

                if (modalitaAggiunta) controller.aggiungiSessione(ip,new ArrayList<>(ip.getRicette()));
                else controller.aggiornaSessione(sessione,ip);
            }

            StyleHelper.showSuccessDialog("Successo","Sessione salvata correttamente");
            primaryStage.hide();

        } catch (ValidationException ve) {
            StyleHelper.showValidationDialog("Validazione",ve.getMessage());
        } catch (DataAccessException dae) {
            StyleHelper.showErrorDialog("Errore DB",dae.getMessage());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore",e.getMessage());
            e.printStackTrace();
        }
    }
}
