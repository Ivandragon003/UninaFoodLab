package Gui;

import controller.ChefController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import exceptions.ValidationException;
import helper.StyleHelper;
import exceptions.DataAccessException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

public class RegistrazioneChefGUI extends VBox {

	private static final double CARD_WIDTH = 480;
	private static final double CARD_HEIGHT = 620;
	private static final double FIELD_WIDTH = 240;

	private final ChefController chefController;
	private final Runnable tornaAlLogin;

	private Label errorLabel;
	private TextField codFiscaleField;
	private TextField nomeField;
	private TextField cognomeField;
	private TextField emailField;
	private DatePicker dataNascitaPicker;
	private TextField anniEsperienzaField;
	private Label anniEsperienzaHint;
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
		setPadding(new Insets(25));
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

		errorLabel = createErrorLabel();

		codFiscaleField = (TextField) createInputField("Codice Fiscale", false);
		nomeField = (TextField) createInputField("Nome", false);
		cognomeField = (TextField) createInputField("Cognome", false);
		emailField = (TextField) createInputField("Email", false);
		usernameField = (TextField) createInputField("Username", false);
		passwordField = (PasswordField) createInputField("Password (min 6 caratteri)", true);
		confermaPasswordField = (PasswordField) createInputField("Conferma Password", true);
		dataNascitaPicker = createStyledDatePicker();
		anniEsperienzaField = (TextField) createInputField("Anni di Esperienza", false);
		anniEsperienzaHint = createHintLabel();
		disponibilitaCheck = createDisponibilitaCheckBox();

		setupAutoResetListeners();
		setupAnniEsperienzaListener();

		VBox header = createHeader();
		GridPane form = createFormGrid();
		HBox buttons = createButtonBox();

		getChildren().addAll(header, errorLabel, form, buttons);
		setSpacing(12);
	}

	private VBox createHeader() {
		Label title = new Label("👨‍🍳 Registrazione Chef");
		title.setFont(Font.font("Roboto", FontWeight.BOLD, 24));
		title.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

		Label subtitle = new Label("Crea il tuo account");
		subtitle.setFont(Font.font("Roboto", FontWeight.NORMAL, 13));
		subtitle.setTextFill(Color.web(StyleHelper.PRIMARY_LIGHT));

		VBox box = new VBox(6, title, subtitle);
		box.setAlignment(Pos.CENTER);
		return box;
	}

	private Label createErrorLabel() {
		Label lbl = new Label();
		lbl.setFont(Font.font("Roboto", FontWeight.BOLD, 11));
		lbl.setTextFill(Color.RED);
		lbl.setVisible(false);
		lbl.setWrapText(true);
		lbl.setMaxWidth(CARD_WIDTH - 50);
		lbl.setAlignment(Pos.CENTER);
		lbl.setStyle("""
				    -fx-background-color: #ffe6e6;
				    -fx-padding: 8;
				    -fx-background-radius: 10;
				    -fx-border-color: red;
				    -fx-border-width: 1.5;
				    -fx-border-radius: 10;
				""");
		return lbl;
	}

	private Label createHintLabel() {
		Label hint = new Label("");
		hint.setFont(Font.font("Roboto", FontWeight.NORMAL, 10));
		hint.setTextFill(Color.web("#666666"));
		return hint;
	}

	private GridPane createFormGrid() {
		GridPane grid = new GridPane();
		grid.setHgap(12);
		grid.setVgap(8);
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
		grid.add(new Label("Anni Esperienza:"), 0, row);
		VBox anniBox = new VBox(2, anniEsperienzaField, anniEsperienzaHint);
		grid.add(anniBox, 1, row++);
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
		reg.setPrefSize(145, 38);
		back.setPrefSize(145, 38);

		reg.setOnAction(e -> handleRegistration());
		back.setOnAction(e -> {
			nascondiErrore();
			tornaAlLogin.run();
		});

		HBox box = new HBox(12, back, reg);
		box.setAlignment(Pos.CENTER);
		return box;
	}

	private void setupAnniEsperienzaListener() {
		dataNascitaPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				int eta = LocalDate.now().getYear() - newVal.getYear();
				int maxAnni = Math.max(0, eta - 16);
				anniEsperienzaHint.setText(String.format("Max %d anni (età %d, si lavora dai 16)", maxAnni, eta));
			} else {
				anniEsperienzaHint.setText("");
			}
		});
	}

	private void handleRegistration() {
		nascondiErrore();
		resetAllFieldStyles();

		boolean anyEmpty = Stream.of(codFiscaleField, nomeField, cognomeField, emailField, anniEsperienzaField,
				usernameField, passwordField, confermaPasswordField).anyMatch(f -> f.getText().trim().isEmpty());

		if (anyEmpty || dataNascitaPicker.getValue() == null) {
			mostraErrore("❌ Compilare tutti i campi obbligatori");
			Stream.of(codFiscaleField, nomeField, cognomeField, emailField, anniEsperienzaField, usernameField,
					passwordField, confermaPasswordField).filter(f -> f.getText().trim().isEmpty())
					.forEach(StyleHelper::applyErrorState);
			return;
		}

		if (!passwordField.getText().equals(confermaPasswordField.getText())) {
			mostraErrore("❌ Le password non coincidono");
			StyleHelper.applyErrorState(passwordField);
			StyleHelper.applyErrorState(confermaPasswordField);
			return;
		}

		int anniEsperienza;
		try {
			anniEsperienza = Integer.parseInt(anniEsperienzaField.getText().trim());
		} catch (NumberFormatException ex) {
			mostraErrore("❌ Gli anni di esperienza devono essere un numero valido");
			StyleHelper.applyErrorState(anniEsperienzaField);
			return;
		}

		try {
			chefController.registraChef(codFiscaleField.getText().trim(), nomeField.getText().trim(),
					cognomeField.getText().trim(), emailField.getText().trim(), dataNascitaPicker.getValue(),
					disponibilitaCheck.isSelected(), usernameField.getText().trim(), passwordField.getText(),
					anniEsperienza);

			StyleHelper.showSuccessDialog("Registrazione Completata",
					"Account creato con successo!\nOra puoi effettuare il login.");
			tornaAlLogin.run();

		} catch (ValidationException | IllegalArgumentException ex) {
			mostraErrore("❌ " + ex.getMessage());
			evidenziaCampoDaMessaggio(ex.getMessage());
		} catch (DataAccessException ex) {
			mostraErrore("❌ " + Optional.ofNullable(ex.getMessage()).orElse("Errore database"));
			ex.printStackTrace();
		} catch (Exception ex) {
			mostraErrore("❌ Errore imprevisto");
			ex.printStackTrace();
		}
	}

	private void resetAllFieldStyles() {
		Stream.of(codFiscaleField, nomeField, cognomeField, emailField, anniEsperienzaField, usernameField,
				passwordField, confermaPasswordField).forEach(StyleHelper::applyNormalState);
	}

	private void evidenziaCampoDaMessaggio(String msg) {
		msg = msg.toLowerCase();
		if (msg.contains("codice fiscale"))
			StyleHelper.applyErrorState(codFiscaleField);
		if (msg.contains("nome"))
			StyleHelper.applyErrorState(nomeField);
		if (msg.contains("cognome"))
			StyleHelper.applyErrorState(cognomeField);
		if (msg.contains("email"))
			StyleHelper.applyErrorState(emailField);
		if (msg.contains("esperienza"))
			StyleHelper.applyErrorState(anniEsperienzaField);
		if (msg.contains("username"))
			StyleHelper.applyErrorState(usernameField);
		if (msg.contains("password"))
			StyleHelper.applyErrorState(passwordField);
	}

	private void setupAutoResetListeners() {
		Stream.concat(Stream.of(codFiscaleField, nomeField, cognomeField, emailField, anniEsperienzaField,
				usernameField, passwordField, confermaPasswordField), Stream.of(dataNascitaPicker)).forEach(ctrl -> {
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
				    -fx-padding: 7;
				    -fx-font-size: 13px;
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
				    -fx-font-size: 13px;
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