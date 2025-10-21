package Gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.*;
import guihelper.StyleHelper;
import controller.RicettaController;
import controller.IngredienteController;
import guihelper.ValidationHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;

public class CreaSessioniGUI extends Stage {
	private Sessione sessioneCreata = null;
	private final LocalDate corsoInizio;
	private final LocalDate corsoFine;
	private final Frequenza frequenzaCorso;
	private Set<LocalDate> dateOccupate;
	private final RicettaController ricettaController;
	private final IngredienteController ingredienteController;

	private DatePicker datePicker;
	private ComboBox<Integer> oraInizioBox, minutiInizioBox, oraFineBox, minutiFineBox;
	private ComboBox<String> tipoCombo;
	private TextField piattaformaField;
	private TextField viaField, cittaField, postiField, capField;
	private Button selezionaRicetteBtn;
	private Label ricetteLabel;
	private ListView<Ricetta> ricetteListView;
	private List<Ricetta> ricetteSelezionate = new ArrayList<>();

	public CreaSessioniGUI(LocalDate corsoInizio, LocalDate corsoFine, Frequenza frequenzaCorso,
			Set<LocalDate> dateOccupate, RicettaController ricettaController,
			IngredienteController ingredienteController) {

		if (corsoInizio == null || corsoFine == null || frequenzaCorso == null || ricettaController == null
				|| ingredienteController == null) {
			throw new IllegalArgumentException("Parametri obbligatori mancanti");
		}

		this.corsoInizio = corsoInizio;
		this.corsoFine = corsoFine;
		this.frequenzaCorso = frequenzaCorso;
		this.dateOccupate = dateOccupate != null ? dateOccupate : new HashSet<>();
		this.ricettaController = ricettaController;
		this.ingredienteController = ingredienteController;

		initializeDialog();
	}

	private void initializeDialog() {
		setTitle("Crea Sessione");
		initModality(Modality.APPLICATION_MODAL);
		setResizable(true);
		createLayout();
	}

	private void createLayout() {
		StackPane root = new StackPane();
		root.setPrefSize(900, 800);

		Region bg = new Region();
		StyleHelper.applyBackgroundGradient(bg);

		VBox main = new VBox(20);
		main.setPadding(new Insets(30));
		main.setAlignment(Pos.TOP_CENTER);

		Label title = StyleHelper.createTitleLabel("🎯 Crea Nuova Sessione");
		title.setTextFill(Color.WHITE);

		ScrollPane scroll = new ScrollPane();
		scroll.setFitToWidth(true);
		scroll.setStyle("-fx-background: transparent;");

		VBox form = StyleHelper.createSection();
		form.getChildren().addAll(createDateSection(), new Separator(), createTipoSection(), createCampiSection(),
				new Separator(), createRicetteSection(), new Separator(), createButtonSection());

		scroll.setContent(form);
		main.getChildren().addAll(title, scroll);
		root.getChildren().addAll(bg, main);

		Scene scene = new Scene(root);
		scene.setFill(Color.TRANSPARENT);
		setScene(scene);
	}

	private VBox createDateSection() {
		VBox box = new VBox(15);

		Label lbl = StyleHelper.createLabel("📅 Data e Orari");
		lbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

		Label frequenzaInfo = new Label("📊 Frequenza corso: " + frequenzaCorso.getDescrizione());
		frequenzaInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666; -fx-background-color: #f0f8ff; "
				+ "-fx-padding: 8; -fx-background-radius: 5;");

		GridPane grid = new GridPane();
		grid.setHgap(15);
		grid.setVgap(15);

		datePicker = StyleHelper.createDatePicker();
		configureDatePicker();

		oraInizioBox = createTimeBox(24, 9, 1);
		minutiInizioBox = createTimeBox(60, 0, 15);
		oraFineBox = createTimeBox(24, 17, 1);
		minutiFineBox = createTimeBox(60, 0, 15);

		grid.add(StyleHelper.createLabel("Data:"), 0, 0);
		grid.add(datePicker, 1, 0);
		grid.add(StyleHelper.createLabel("Ora Inizio:"), 0, 1);
		grid.add(new HBox(5, oraInizioBox, new Label(":"), minutiInizioBox), 1, 1);
		grid.add(StyleHelper.createLabel("Ora Fine:"), 2, 1);
		grid.add(new HBox(5, oraFineBox, new Label(":"), minutiFineBox), 3, 1);

		box.getChildren().addAll(lbl, frequenzaInfo, grid);
		return box;
	}

	private void configureDatePicker() {
		datePicker.setDayCellFactory(dp -> new DateCell() {
			@Override
			public void updateItem(LocalDate d, boolean empty) {
				super.updateItem(d, empty);

				if (empty || d == null) {
					disableCell(null);
					return;
				}

				if (d.isBefore(corsoInizio)) {
					disableCell("Data prima dell'inizio del corso");
					setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #c62828;");
					return;
				}

				if (dateOccupate.contains(d)) {
					disableCell("Esiste già una sessione in questa data");
					setStyle("-fx-background-color: #fff9c4; -fx-text-fill: #8a6d00;");
					return;
				}

				DateValidationResult result = validateDate(d);
				if (result.isValid) {
					enableCell("Data disponibile");
				} else {
					disableCell(result.message);
				}
			}

			private void disableCell(String message) {
				setDisable(true);
				setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #c62828;");
				setTooltip(message != null ? new Tooltip(message) : null);
			}

			private void enableCell(String message) {
				setDisable(false);
				setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: black;");
				setTooltip(new Tooltip(message));
			}
		});
	}

	private DateValidationResult validateDate(LocalDate d) {
		LocalDate lastSessionDate = dateOccupate.stream().max(LocalDate::compareTo).orElse(null);

		return switch (frequenzaCorso) {
		case unica -> new DateValidationResult(dateOccupate.isEmpty(),
				"Frequenza 'Sessione Unica' - non puoi aggiungere altre sessioni");
		case giornaliero ->
			new DateValidationResult(lastSessionDate == null ? !d.isBefore(corsoInizio) && !d.isAfter(corsoFine)
					: d.isAfter(lastSessionDate) && !d.isAfter(corsoFine), "Data non disponibile");
		case ogniDueGiorni -> validateOgniDueGiorni(d, lastSessionDate);
		case settimanale -> validateSettimanale(d, lastSessionDate);
		case mensile -> validateMensile(d, lastSessionDate);
		};
	}

	private DateValidationResult validateOgniDueGiorni(LocalDate d, LocalDate lastDate) {
		if (lastDate == null) {
			return new DateValidationResult(!d.isBefore(corsoInizio) && !d.isAfter(corsoFine), "");
		}
		long diff = ChronoUnit.DAYS.between(lastDate, d);
		return new DateValidationResult(diff >= 2 && !d.isAfter(corsoFine),
				"Richiede almeno 2 giorni di distanza dall'ultima sessione");
	}

	private DateValidationResult validateSettimanale(LocalDate d, LocalDate lastDate) {
		if (lastDate == null) {
			return new DateValidationResult(!d.isBefore(corsoInizio) && !d.isAfter(corsoFine), "");
		}
		LocalDate nextMonday = lastDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
		LocalDate endWeek = nextMonday.plusDays(6);
		return new DateValidationResult(!d.isBefore(nextMonday) && !d.isAfter(endWeek),
				"La sessione settimanale deve essere nella settimana " + nextMonday + " - " + endWeek);
	}

	private DateValidationResult validateMensile(LocalDate d, LocalDate lastDate) {
		if (lastDate == null) {
			return new DateValidationResult(!d.isBefore(corsoInizio) && !d.isAfter(corsoFine), "");
		}
		LocalDate nextMonth = lastDate.plusMonths(1);
		return new DateValidationResult(!d.isBefore(nextMonth) && !d.isAfter(corsoFine),
				"La sessione mensile deve essere almeno un mese dopo l'ultima");
	}

	private static class DateValidationResult {
		final boolean isValid;
		final String message;

		DateValidationResult(boolean isValid, String message) {
			this.isValid = isValid;
			this.message = message;
		}
	}

	private ComboBox<Integer> createTimeBox(int max, int def, int step) {
		ComboBox<Integer> cb = StyleHelper.createComboBox();
		for (int i = 0; i < max; i += step) {
			cb.getItems().add(i);
		}
		cb.setValue(def);
		return cb;
	}

	private VBox createTipoSection() {
		VBox box = new VBox(10);
		Label lbl = StyleHelper.createLabel("🎯 Tipo Sessione");
		lbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

		tipoCombo = StyleHelper.createComboBox();
		tipoCombo.getItems().addAll("Online", "In Presenza");
		tipoCombo.setValue("Online");
		tipoCombo.setOnAction(e -> updateVisibility());

		box.getChildren().addAll(lbl, tipoCombo);
		return box;
	}

	private VBox createCampiSection() {
		VBox box = new VBox(15);

		VBox onlineBox = createOnlineFields();
		VBox presenzaBox = createPresenzaFields();

		box.getChildren().addAll(onlineBox, presenzaBox);
		return box;
	}

	private VBox createOnlineFields() {
		VBox box = new VBox(10);
		box.setId("onlineFields");
		piattaformaField = StyleHelper.createTextField("Es. Zoom, Teams, Google Meet");
		box.getChildren().addAll(StyleHelper.createLabel("🌐 Dettagli Online"), piattaformaField);
		return box;
	}

	private VBox createPresenzaFields() {
		VBox box = new VBox(10);
		box.setId("presenzaFields");
		box.setVisible(false);
		box.setManaged(false);

		viaField = StyleHelper.createTextField("Via e civico");
		cittaField = StyleHelper.createTextField("Città");
		postiField = StyleHelper.createTextField("Numero posti");
		capField = StyleHelper.createTextField("CAP");

		box.getChildren().addAll(StyleHelper.createLabel("🏢 Dettagli In Presenza"), viaField, cittaField, postiField,
				capField);
		return box;
	}

	private VBox createRicetteSection() {
		VBox box = new VBox(10);
		box.setId("ricetteSection");
		box.setVisible(false);
		box.setManaged(false);

		Label title = StyleHelper.createLabel("🍽️ Ricette (Solo per sessioni in presenza)");
		title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

		selezionaRicetteBtn = StyleHelper.createPrimaryButton("📚 Seleziona Ricette");
		selezionaRicetteBtn.setOnAction(e -> apriDialogRicette());
		selezionaRicetteBtn.setDisable(true);

		ricetteLabel = StyleHelper.createLabel("⚠️ Nessuna ricetta selezionata");
		ricetteLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-font-weight: bold;");

		ricetteListView = createRicetteListView();

		Label info = StyleHelper.createLabel("💡 Le sessioni in presenza richiedono almeno una ricetta");
		info.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px; -fx-background-color: #f8f9fa; "
				+ "-fx-padding: 8; -fx-background-radius: 5;");

		box.getChildren().addAll(title, selezionaRicetteBtn, ricetteLabel, ricetteListView, info);
		return box;
	}

	private ListView<Ricetta> createRicetteListView() {
		ListView<Ricetta> listView = new ListView<>();
		listView.setPrefHeight(150);
		listView.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 12; "
				+ "-fx-border-color: #28a745; -fx-border-width: 2; -fx-border-radius: 12;");

		listView.setCellFactory(lv -> new ListCell<>() {
			@Override
			protected void updateItem(Ricetta ricetta, boolean empty) {
				super.updateItem(ricetta, empty);
				if (empty || ricetta == null) {
					setGraphic(null);
					setText(null);
				} else {
					setGraphic(createRicettaCell(ricetta));
					setText(null);
					setStyle("-fx-background-color: transparent; -fx-padding: 0;");
				}
			}
		});
		return listView;
	}

	private HBox createRicettaCell(Ricetta ricetta) {
		HBox cell = new HBox(10);
		cell.setAlignment(Pos.CENTER_LEFT);
		cell.setPadding(new Insets(8));
		cell.setStyle("-fx-background-color: white; -fx-background-radius: 8; "
				+ "-fx-border-color: #28a745; -fx-border-width: 1; -fx-border-radius: 8;");

		Label icon = new Label("🍽️");
		icon.setStyle("-fx-font-size: 16px;");

		VBox info = new VBox(3);
		Label name = new Label(ricetta.getNome());
		name.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
		Label details = new Label("⏱️ " + ricetta.getTempoPreparazione() + " min | 🥕 " + ricetta.getNumeroIngredienti()
				+ " ingredienti");
		details.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
		info.getChildren().addAll(name, details);

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button remove = new Button("❌");
		remove.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; "
				+ "-fx-padding: 5 10; -fx-border-radius: 5; -fx-cursor: hand; -fx-font-size: 12px;");
		remove.setTooltip(new Tooltip("Rimuovi ricetta"));
		remove.setOnAction(e -> {
			ricetteSelezionate.remove(ricetta);
			aggiornaListaRicette();
		});

		cell.getChildren().addAll(icon, info, spacer, remove);
		return cell;
	}

	private void aggiornaListaRicette() {
		ricetteListView.getItems().setAll(ricetteSelezionate);

		if (ricetteSelezionate.isEmpty()) {
			ricetteLabel.setText("⚠️ Nessuna ricetta selezionata");
			ricetteLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-font-weight: bold;");
		} else {
			ricetteLabel.setText("✅ " + ricetteSelezionate.size() + " ricette selezionate");
			ricetteLabel.setStyle("-fx-text-fill: #28a745; -fx-font-size: 12px; -fx-font-weight: bold;");
		}
	}

	private void apriDialogRicette() {
		try {
			VisualizzaRicetteDialog dialog = new VisualizzaRicetteDialog(ricettaController, ingredienteController);
			dialog.initOwner(this);

			if (!ricetteSelezionate.isEmpty()) {
				dialog.preSelezionaRicette(ricetteSelezionate);
			}

			List<Ricetta> scelte = dialog.showAndReturn();
			if (scelte != null) {
				ricetteSelezionate.clear();
				ricetteSelezionate.addAll(scelte);
				aggiornaListaRicette();

				if (!scelte.isEmpty()) {
					StyleHelper.showSuccessDialog("Successo", "✅ Selezionate " + scelte.size() + " ricette!");
				}
			}
		} catch (Exception e) {

			StyleHelper.showErrorDialog("Errore", "❌ Errore nell'apertura del dialog: " + e.getMessage());
		}
	}

	private HBox createButtonSection() {
		HBox box = new HBox(15);
		box.setAlignment(Pos.CENTER);

		Button annulla = StyleHelper.createDangerButton("❌ Annulla");
		annulla.setOnAction(e -> {
			sessioneCreata = null;
			close();
		});

		Button salva = StyleHelper.createPrimaryButton("💾 Salva Sessione");
		salva.setOnAction(e -> salvaSessione());

		box.getChildren().addAll(annulla, salva);
		return box;
	}

	private void updateVisibility() {
		boolean isOnline = "Online".equals(tipoCombo.getValue());

		VBox onlineBox = (VBox) getScene().getRoot().lookup("#onlineFields");
		VBox presenzaBox = (VBox) getScene().getRoot().lookup("#presenzaFields");
		VBox ricetteBox = (VBox) getScene().getRoot().lookup("#ricetteSection");

		if (onlineBox != null && presenzaBox != null && ricetteBox != null) {
			onlineBox.setVisible(isOnline);
			onlineBox.setManaged(isOnline);
			presenzaBox.setVisible(!isOnline);
			presenzaBox.setManaged(!isOnline);
			ricetteBox.setVisible(!isOnline);
			ricetteBox.setManaged(!isOnline);
			selezionaRicetteBtn.setDisable(isOnline);

			if (isOnline) {
				ricetteSelezionate.clear();
				ricetteLabel.setText("ℹ️ Non applicabile per sessioni online");
				ricetteLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");
				aggiornaListaRicette();
			} else {
				ricetteLabel.setText("⚠️ Nessuna ricetta selezionata");
				ricetteLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-font-weight: bold;");
			}
		}
	}

	private void salvaSessione() {
		try {

			if (datePicker.getValue() == null) {
				StyleHelper.showValidationDialog("Validazione", "⚠️ Seleziona una data");
				return;
			}

			LocalTime oraInizio = LocalTime.of(oraInizioBox.getValue(), minutiInizioBox.getValue());
			LocalTime oraFine = LocalTime.of(oraFineBox.getValue(), minutiFineBox.getValue());
			LocalDateTime dataInizio = LocalDateTime.of(datePicker.getValue(), oraInizio);
			LocalDateTime dataFine = LocalDateTime.of(datePicker.getValue(), oraFine);

			if ("Online".equals(tipoCombo.getValue())) {
				sessioneCreata = creaSessioneOnline(dataInizio, dataFine);
			} else {
				sessioneCreata = creaSessioneInPresenza(dataInizio, dataFine);
			}

			close();

		} catch (IllegalArgumentException e) {
			StyleHelper.showValidationDialog("Validazione", "⚠️ " + e.getMessage());
		} catch (Exception e) {

			StyleHelper.showErrorDialog("Errore", "❌ Errore: " + e.getMessage());
		}
	}

	private Online creaSessioneOnline(LocalDateTime inizio, LocalDateTime fine) {
		String piattaforma = ValidationHelper.validateString(piattaformaField.getText(), "piattaforma");
		return new Online(inizio, fine, piattaforma);
	}

	private InPresenza creaSessioneInPresenza(LocalDateTime inizio, LocalDateTime fine) {
		String via = ValidationHelper.validateString(viaField.getText(), "via");
		String citta = ValidationHelper.validateString(cittaField.getText(), "città");

		int posti = ValidationHelper.parseInteger(postiField.getText(), "posti", 1, 1000);
		int cap = ValidationHelper.parseInteger(capField.getText(), "CAP", 10000, 99999);

		InPresenza sessione = new InPresenza(inizio, fine, via, citta, posti, cap);
		sessione.getRicette().addAll(ricetteSelezionate);

		return sessione;
	}

	public Sessione showDialog() {
		showAndWait();
		return sessioneCreata;
	}
}