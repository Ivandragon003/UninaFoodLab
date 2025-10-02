package Gui;

import controller.GestioneCorsoController;
import model.CorsoCucina;
import model.Frequenza;
import model.Chef;
import model.Sessione;
import model.Online;
import model.InPresenza;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import exceptions.ValidationException;
import exceptions.DataAccessException;

public class CreaCorsoGUI {

	private VBox root;
	private GestioneCorsoController gestioneController;
	private service.GestioneRicette gestioneRicette;
	private service.GestioneCucina gestioneCucina;

	private ObservableList<Chef> chefSelezionati = FXCollections.observableArrayList();
	private ListView<Chef> chefListView;

	private ObservableList<Sessione> corsoSessioni = FXCollections.observableArrayList();
	private ListView<Sessione> sessioniListView;

	public void setController(GestioneCorsoController controller) {
		this.gestioneController = controller;
	}

	public void setGestioneRicette(service.GestioneRicette gestioneRicette) {
		this.gestioneRicette = gestioneRicette;
	}

	public void setGestioneCucina(service.GestioneCucina gestioneCucina) {
		this.gestioneCucina = gestioneCucina;
	}

	public Pane getRoot() {
		if (root == null)
			initUI();
		return root;
	}

	private void initUI() {
		VBox mainContent = new VBox(20);
		mainContent.setPadding(new Insets(30));
		mainContent.setAlignment(Pos.TOP_CENTER);

		// Sfondo gradiente arancione
		LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
				new Stop(0, Color.web("#FF9966")), new Stop(0.5, Color.web("#FFB366")),
				new Stop(1, Color.web("#FFCC99")));
		mainContent.setBackground(
				new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(gradient, null, null)));

		// Titolo
		Label title = new Label("🎓 Crea Nuovo Corso");
		title.setFont(Font.font("Inter", FontWeight.EXTRA_BOLD, 26));
		title.setTextFill(Color.WHITE);
		title.setEffect(new DropShadow(8, Color.web("#FF6600", 0.8)));

		Label subtitle = new Label("Compila i dettagli del corso di cucina");
		subtitle.setFont(Font.font("Inter", FontWeight.NORMAL, 13));
		subtitle.setTextFill(Color.web("#FFFFFF", 0.9));

		VBox headerBox = new VBox(8, title, subtitle);
		headerBox.setAlignment(Pos.CENTER);

		// Card del form
		VBox formCard = new VBox(15);
		formCard.setPadding(new Insets(30));
		formCard.setAlignment(Pos.TOP_LEFT);
		formCard.setMaxWidth(Double.MAX_VALUE);
		formCard.setMinWidth(400);
		formCard.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20; -fx-border-radius: 20; "
				+ "-fx-border-color: #E0E0E0; -fx-border-width: 1;");

		formCard.setEffect(new DropShadow(15, Color.web("#000000", 0.15)));

		// Campi form
		TextField nomeField = createStyledTextField("📝 Nome corso");
		TextField prezzoField = createStyledTextField("💰 Prezzo (es: 150.00)");
		TextField argomentoField = createStyledTextField("🍽️ Argomento");
		TextField postiField = createStyledTextField("👥 Numero posti disponibili");

		ComboBox<Frequenza> frequenzaBox = createStyledComboBox();
		frequenzaBox.setPromptText("📅 Frequenza");
		frequenzaBox.getItems().addAll(Frequenza.values());

		DatePicker startDatePicker = createStyledDatePicker("📆 Data inizio");
		DatePicker endDatePicker = createStyledDatePicker("📆 Data fine");

		Spinner<Integer> startHour = createStyledSpinner(0, 23, 9);
		Spinner<Integer> startMinute = createStyledSpinner(0, 59, 0);
		Spinner<Integer> endHour = createStyledSpinner(0, 23, 17);
		Spinner<Integer> endMinute = createStyledSpinner(0, 59, 0);

		HBox startTimeBox = new HBox(8, new Label("⏰ Ora inizio:"), startHour, new Label(":"), startMinute);
		styleTimeBox(startTimeBox);
		HBox endTimeBox = new HBox(8, new Label("⏰ Ora fine:"), endHour, new Label(":"), endMinute);
		styleTimeBox(endTimeBox);

		// Chef 
		Label chefLabel = new Label("👨‍🍳 Chef Responsabili");
		chefLabel.setFont(Font.font("Inter", FontWeight.BOLD, 14));
		chefLabel.setTextFill(Color.web("#FF6600"));

		Button btnAggiungiChef = createModernButton("➕ Seleziona Chef", "#FF6600", "#FF8533");
		btnAggiungiChef.setPrefWidth(180);

		chefListView = new ListView<>(chefSelezionati);
		chefListView.setPrefHeight(140);
		chefListView.setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #FFB366; "
				+ "-fx-border-radius: 10; -fx-background-radius: 10;");

		chefListView.setCellFactory(lv -> new ListCell<Chef>() {
			private HBox content;
			private Label nameLabel;
			private Button removeBtn;

			{
				nameLabel = new Label();
				nameLabel.setFont(Font.font("Inter", 13));
				removeBtn = new Button("❌");
				removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-weight: bold; -fx-cursor: hand;");
				removeBtn.setOnAction(e -> {
					Chef c = getItem();
					if (c != null) {
						chefSelezionati.remove(c);
					}
				});
				content = new HBox(10, nameLabel, removeBtn);
				content.setAlignment(Pos.CENTER_LEFT);
			}

			@Override
			protected void updateItem(Chef c, boolean empty) {
				super.updateItem(c, empty);
				if (empty || c == null) {
					setText(null);
					setGraphic(null);
				} else {
					nameLabel.setText(c.getNome() + " " + c.getCognome());
					setGraphic(content);
				}
			}
		});

		btnAggiungiChef.setOnAction(e -> {
			try {
				SelezionaChefDialog dialog = new SelezionaChefDialog((Stage) root.getScene().getWindow(),
						gestioneController, chefSelezionati);
				dialog.showAndWait();
				List<Chef> scelti = dialog.getSelezionati();
				if (scelti != null && !scelti.isEmpty()) {
					for (Chef c : scelti) {
						if (!chefSelezionati.contains(c)) {
							chefSelezionati.add(c);
						}
					}
				}
			} catch (SQLException ex) {
				showAlert("Errore", "Impossibile caricare gli chef: " + ex.getMessage());
			} catch (Exception ex) {
				showAlert("Errore", "Problema durante l'apertura del dialog degli chef: " + ex.getMessage());
			}
		});

		// Sessioni 
		Label sessioniLabel = createFieldLabel("📌 Sessioni del Corso");
		sessioniListView = new ListView<>(corsoSessioni);
		sessioniListView.setPrefHeight(150);

		sessioniListView.setCellFactory(lv -> new ListCell<Sessione>() {
			private HBox content;
			private Label infoLabel;
			private Button removeBtn;

			{
				infoLabel = new Label();
				removeBtn = new Button("❌");
				removeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
				removeBtn.setOnAction(e -> {
					Sessione s = getItem();
					if (s != null) {
						corsoSessioni.remove(s);
					}
				});
				content = new HBox(10, infoLabel, removeBtn);
				content.setAlignment(Pos.CENTER_LEFT);
			}

			@Override
			protected void updateItem(Sessione s, boolean empty) {
				super.updateItem(s, empty);
				if (empty || s == null) {
					setText(null);
					setGraphic(null);
				} else {
					if (s instanceof Online) {
						infoLabel.setText("Online: " + ((Online) s).getPiattaformaStreaming() + " | "
								+ s.getDataInizioSessione() + " -> " + s.getDataFineSessione());
					} else if (s instanceof InPresenza) {
						InPresenza ip = (InPresenza) s;
						infoLabel.setText("Presenza: " + ip.getVia() + ", " + ip.getCitta() + " | "
								+ s.getDataInizioSessione() + " -> " + s.getDataFineSessione());
					}
					setGraphic(content);
				}
			}
		});

		Button btnCreaSessione = createModernButton("➕ Aggiungi Sessione", "#FF6600", "#FF8533");
		btnCreaSessione.setOnAction(e -> {
			if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
				showAlert("Errore", "Prima imposta le date del corso.");
				return;
			}

			LocalDate inizioCorso = startDatePicker.getValue();
			LocalDate fineCorso = endDatePicker.getValue();

			Set<LocalDate> rosse = new HashSet<>();
			Set<LocalDate> arancioni = new HashSet<>();

			LocalDate oggi = LocalDate.now();
			for (LocalDate d = oggi.minusYears(5); d.isBefore(inizioCorso); d = d.plusDays(1)) {
				rosse.add(d);
			}
			for (LocalDate d = fineCorso.plusDays(1); d.isBefore(fineCorso.plusYears(5)); d = d.plusDays(1)) {
				rosse.add(d);
			}
			for (Sessione s : corsoSessioni) {
				arancioni.add(s.getDataInizioSessione().toLocalDate());
			}

			CreaSessioniGUI sessioniDialog = new CreaSessioniGUI(rosse, arancioni, gestioneRicette, gestioneCucina);
			Sessione nuovaSessione = sessioniDialog.showDialog((Stage) root.getScene().getWindow());

			if (nuovaSessione != null) {
				corsoSessioni.add(nuovaSessione);
			}
		});

		// Layout 
		formCard.getChildren().addAll(createFieldLabel("Nome del Corso"), nomeField, createFieldLabel("Prezzo"),
				prezzoField, createFieldLabel("Argomento"), argomentoField, createFieldLabel("Numero Posti"),
				postiField, createFieldLabel("Frequenza"), frequenzaBox, createFieldLabel("Data Inizio"),
				startDatePicker, startTimeBox, createFieldLabel("Data Fine"), endDatePicker, endTimeBox, chefLabel,
				btnAggiungiChef, chefListView, sessioniLabel, sessioniListView, btnCreaSessione);

		// Bottoni finali
		HBox btnBox = new HBox(15);
		btnBox.setAlignment(Pos.CENTER);

		Button salvaBtn = createModernButton("✅ Salva Corso", "#FF6600", "#FF8533");
		Button annullaBtn = createModernButton("❌ Annulla", "#999999", "#BBBBBB");

		salvaBtn.setOnAction(e -> salvaCorso(nomeField, prezzoField, argomentoField, postiField, frequenzaBox,
				startDatePicker, endDatePicker, startHour, startMinute, endHour, endMinute, salvaBtn));

		annullaBtn.setOnAction(e -> ((Stage) root.getScene().getWindow()).close());

		btnBox.getChildren().addAll(annullaBtn, salvaBtn);

		ScrollPane scrollPane = new ScrollPane(formCard);
		scrollPane.setFitToWidth(true);
		scrollPane.setPannable(true);
		scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
		VBox.setVgrow(scrollPane, Priority.ALWAYS);

		VBox contentBox = new VBox(20, headerBox, scrollPane, btnBox);
		contentBox.setAlignment(Pos.TOP_CENTER);
		contentBox.setPadding(new Insets(20, 40, 20, 40));
		contentBox.setMaxWidth(Double.MAX_VALUE);

		mainContent.getChildren().add(contentBox);

		root = new VBox(mainContent);
		root.setAlignment(Pos.CENTER);
	}

	// Helper methods
	private TextField createStyledTextField(String prompt) {
		TextField field = new TextField();
		field.setPromptText(prompt);
		field.setPrefHeight(40);
		field.setFont(Font.font("Inter", 13));
		field.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; "
				+ "-fx-border-color: #FFB366; -fx-border-width: 1.5; -fx-padding: 0 12;");
		return field;
	}

	private ComboBox<Frequenza> createStyledComboBox() {
		ComboBox<Frequenza> combo = new ComboBox<>();
		combo.setPrefHeight(40);
		combo.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; "
				+ "-fx-border-color: #FFB366; -fx-border-width: 1.5;");
		return combo;
	}

	private DatePicker createStyledDatePicker(String prompt) {
		DatePicker picker = new DatePicker();
		picker.setPromptText(prompt);
		picker.setPrefHeight(40);
		picker.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; "
				+ "-fx-border-color: #FFB366; -fx-border-width: 1.5;");
		return picker;
	}

	private Spinner<Integer> createStyledSpinner(int min, int max, int initial) {
		Spinner<Integer> spinner = new Spinner<>(min, max, initial);
		spinner.setPrefWidth(70);
		spinner.setPrefHeight(40);
		spinner.setStyle("-fx-background-color: white; -fx-border-color: #FFB366; -fx-border-radius: 8; "
				+ "-fx-background-radius: 8;");
		return spinner;
	}

	private void styleTimeBox(HBox box) {
		box.setAlignment(Pos.CENTER_LEFT);
		box.setPadding(new Insets(5, 0, 5, 0));
		for (javafx.scene.Node node : box.getChildren()) {
			if (node instanceof Label) {
				((Label) node).setFont(Font.font("Inter", FontWeight.MEDIUM, 13));
				((Label) node).setTextFill(Color.web("#666666"));
			}
		}
	}

	private Label createFieldLabel(String text) {
		Label label = new Label(text);
		label.setFont(Font.font("Inter", FontWeight.BOLD, 13));
		label.setTextFill(Color.web("#555555"));
		return label;
	}

	private Button createModernButton(String text, String baseColor, String hoverColor) {
		Button btn = new Button(text);
		btn.setPrefWidth(160);
		btn.setPrefHeight(44);
		btn.setFont(Font.font("Inter", FontWeight.BOLD, 14));
		btn.setTextFill(Color.WHITE);

		String baseStyle = "-fx-background-color: " + baseColor + "; -fx-background-radius: 22; "
				+ "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8, 0, 0, 3);";
		String hoverStyle = "-fx-background-color: " + hoverColor + "; -fx-background-radius: 22; "
				+ "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 10, 0, 0, 4);";

		btn.setStyle(baseStyle);
		btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
		btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
		btn.setOnMousePressed(e -> {
			btn.setScaleX(0.97);
			btn.setScaleY(0.97);
		});
		btn.setOnMouseReleased(e -> {
			btn.setScaleX(1.0);
			btn.setScaleY(1.0);
		});

		return btn;
	}

	private void salvaCorso(TextField nomeField, TextField prezzoField, TextField argomentoField, TextField postiField,
			ComboBox<Frequenza> frequenzaBox, DatePicker startDatePicker, DatePicker endDatePicker,
			Spinner<Integer> startHour, Spinner<Integer> startMinute, Spinner<Integer> endHour,
			Spinner<Integer> endMinute, Button salvaBtn) {
		
		salvaBtn.setDisable(true);
		String originalText = salvaBtn.getText();
		salvaBtn.setText("Salvando...");

		try {
			List<String> errori = new ArrayList<>();

			// Lettura campi
			String nome = nomeField.getText().trim();
			String argomento = argomentoField.getText().trim();
			Frequenza freq = frequenzaBox.getValue();
			LocalDate startDate = startDatePicker.getValue();
			LocalDate endDate = endDatePicker.getValue();

			// Controlli obbligatori 
			if (nome.isEmpty())
				errori.add("Nome corso mancante");
			if (argomento.isEmpty())
				errori.add("Argomento mancante");
			if (freq == null)
				errori.add("Frequenza non selezionata");
			if (startDate == null)
				errori.add("Data inizio mancante");
			if (endDate == null)
				errori.add("Data fine mancante");
			if (chefSelezionati.isEmpty())
				errori.add("Seleziona almeno uno chef");
			if (corsoSessioni.isEmpty())
				errori.add("Aggiungi almeno una sessione");

			if (!errori.isEmpty()) {
				showAlert("Campi mancanti", String.join("\n", errori));
				return;
			}

			double prezzo;
			int posti;
			try {
				prezzo = Double.parseDouble(prezzoField.getText().trim().replace(",", "."));
			} catch (NumberFormatException ex) {
				showAlert("Errore", "Prezzo non valido");
				return;
			}
			try {
				posti = Integer.parseInt(postiField.getText().trim());
			} catch (NumberFormatException ex) {
				showAlert("Errore", "Numero posti non valido");
				return;
			}

			// Date e orari
			LocalDateTime dataInizio = LocalDateTime.of(startDate,
					LocalTime.of(startHour.getValue(), startMinute.getValue()));
			LocalDateTime dataFine = LocalDateTime.of(endDate, LocalTime.of(endHour.getValue(), endMinute.getValue()));

			if (dataInizio.isBefore(LocalDateTime.now())) {
				showAlert("Errore", "Data di inizio nel passato");
				return;
			}
			if (dataFine.isBefore(dataInizio)) {
				showAlert("Errore", "Data fine precedente a inizio");
				return;
			}

			// Conferma salvataggio con Alert 
			boolean confermato = showConfirmationDialog("Conferma Salvataggio",
					"Sei sicuro di voler salvare questo corso con tutti i dati inseriti?");
			if (!confermato) {
				return;
			}

			// Creazione corso
			CorsoCucina corso;
			try {
				corso = new CorsoCucina(nome, prezzo, argomento, freq, posti);
				corso.setDataInizioCorso(dataInizio);
				corso.setDataFineCorso(dataFine);
			} catch (IllegalArgumentException iae) {
				showAlert("Errore di validazione", iae.getMessage());
				return;
			}

			// aggiungo chef e sessioni
			corso.getChef().clear();
			corso.getChef().addAll(chefSelezionati);
			corso.getSessioni().clear();
			corso.getSessioni().addAll(corsoSessioni);

			// Salvataggio tramite controller 
			try {
				gestioneController.creaCorso(corso);
				showAlert("Successo", "Corso creato con successo!");
				clearForm(nomeField, prezzoField, argomentoField, postiField, frequenzaBox, startDatePicker, endDatePicker,
						startHour, startMinute, endHour, endMinute);
			} catch (ValidationException ve) {
				showAlert("Errore di validazione", ve.getMessage());
			} catch (DataAccessException dae) {
				showAlert("Errore DB", dae.getMessage());
			} catch (SQLException ex) {
				showAlert("Errore", "Problema con il salvataggio: " + ex.getMessage());
			} catch (Exception ex) {
				showAlert("Errore", "Si è verificato un errore: " + ex.getMessage());
			}
		} finally {
			salvaBtn.setDisable(false);
			salvaBtn.setText(originalText);
		}
	}

	private boolean showConfirmationDialog(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.initOwner(root.getScene().getWindow());
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);

		Optional<ButtonType> result = alert.showAndWait();
		return result.isPresent() && result.get() == ButtonType.OK;
	}

	private void showAlert(String title, String msg) {
		Alert.AlertType type = title.toLowerCase().contains("errore") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION;
		Alert alert = new Alert(type);
		alert.initOwner(root.getScene().getWindow());
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(msg);
		alert.showAndWait();
	}

	private void clearForm(TextField nomeField, TextField prezzoField, TextField argomentoField, TextField postiField,
			ComboBox<Frequenza> frequenzaBox, DatePicker startDatePicker, DatePicker endDatePicker,
			Spinner<Integer> startHour, Spinner<Integer> startMinute, Spinner<Integer> endHour, Spinner<Integer> endMinute) {

		nomeField.clear();
		prezzoField.clear();
		argomentoField.clear();
		postiField.clear();
		frequenzaBox.setValue(null);
		startDatePicker.setValue(null);
		endDatePicker.setValue(null);

		try {
			startHour.getValueFactory().setValue(9);
			startMinute.getValueFactory().setValue(0);
			endHour.getValueFactory().setValue(17);
			endMinute.getValueFactory().setValue(0);
		} catch (Exception ignored) {
		}

		chefSelezionati.clear();
		corsoSessioni.clear();
	}
}