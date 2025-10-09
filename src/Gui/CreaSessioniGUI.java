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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

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
	private VBox listaRicetteContainer;
	private ListView<Ricetta> ricetteListView;
	private List<Ricetta> ricetteSelezionate = new ArrayList<>();

	public CreaSessioniGUI(LocalDate corsoInizio, LocalDate corsoFine, Frequenza frequenzaCorso,
			Set<LocalDate> dateOccupate, RicettaController ricettaController) {
		this(corsoInizio, corsoFine, frequenzaCorso, dateOccupate, ricettaController, null);
	}

	public CreaSessioniGUI(LocalDate corsoInizio, LocalDate corsoFine, Frequenza frequenzaCorso,
			Set<LocalDate> dateOccupate, RicettaController ricettaController,
			IngredienteController ingredienteController) {
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
		frequenzaInfo.setStyle(
				"-fx-font-size: 12px; -fx-text-fill: #666666; -fx-background-color: #f0f8ff; -fx-padding: 8; -fx-background-radius: 5;");

		GridPane grid = new GridPane();
		grid.setHgap(15);
		grid.setVgap(15);

		datePicker = StyleHelper.createDatePicker();
		datePicker.setDayCellFactory(dp -> new DateCell() {
			@Override
			public void updateItem(LocalDate d, boolean empty) {
				super.updateItem(d, empty);
				if (empty || d == null)
					return;

				if (d.isBefore(corsoInizio) || d.isAfter(corsoFine)) {
					setDisable(true);
					setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #c62828;");
					setTooltip(new Tooltip("Fuori dal periodo del corso"));
					return;
				}

				if (!isDataValidaPerFrequenza(d)) {
					setDisable(true);
					setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #c62828;");
					setTooltip(new Tooltip("Data non compatibile con la frequenza " + frequenzaCorso.getDescrizione()));
					return;
				}

				if (dateOccupate.contains(d)) {
					setDisable(true);
					setStyle("-fx-background-color: #fff9c4; -fx-text-fill: #f57f17;");
					setTooltip(new Tooltip("Data già occupata da altra sessione"));
					return;
				}

				setStyle("-fx-background-color: #c8e6c9; -fx-text-fill: #2e7d32;");
				setTooltip(new Tooltip("Data disponibile"));
			}
		});

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

	private boolean isDataValidaPerFrequenza(LocalDate data) {
		if (frequenzaCorso == Frequenza.unica) {
			return dateOccupate.isEmpty();
		}

		switch (frequenzaCorso) {
		case giornaliero:
			return !dateOccupate.contains(data);

		case ogniDueGiorni:
			LocalDate riferimento = dateOccupate.stream().min(LocalDate::compareTo).orElse(corsoInizio);
			long giorniDifferenza = java.time.temporal.ChronoUnit.DAYS.between(riferimento, data);
			return giorniDifferenza >= 2 && giorniDifferenza % 2 == 0;

		case settimanale:
			int settimanaData = data.get(WeekFields.ISO.weekOfWeekBasedYear());
			int annoData = data.getYear();
			for (LocalDate d : dateOccupate) {
				if (d.getYear() == annoData && d.get(WeekFields.ISO.weekOfWeekBasedYear()) == settimanaData) {
					return false;
				}
			}
			return true;

		case mensile:
			int meseData = data.getMonthValue();
			annoData = data.getYear();
			for (LocalDate d : dateOccupate) {
				if (d.getYear() == annoData && d.getMonthValue() == meseData) {
					return false;
				}
			}
			return true;

		default:
			return true;
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

		VBox onlineBox = new VBox(10);
		onlineBox.setId("onlineFields");

		Label onlineLabel = StyleHelper.createLabel("🌐 Dettagli Online");
		piattaformaField = StyleHelper.createTextField("Es. Zoom, Teams, Google Meet");
		onlineBox.getChildren().addAll(onlineLabel, piattaformaField);

		VBox presenzaBox = new VBox(10);
		presenzaBox.setId("presenzaFields");

		Label presenzaLabel = StyleHelper.createLabel("🏢 Dettagli In Presenza");

		viaField = StyleHelper.createTextField("Via e civico");
		cittaField = StyleHelper.createTextField("Città");
		postiField = StyleHelper.createTextField("Numero posti");
		capField = StyleHelper.createTextField("CAP");

		presenzaBox.getChildren().addAll(presenzaLabel, viaField, cittaField, postiField, capField);
		presenzaBox.setVisible(false);
		presenzaBox.setManaged(false);

		box.getChildren().addAll(onlineBox, presenzaBox);
		return box;
	}

	private VBox createRicetteSection() {
		VBox box = new VBox(10);
		box.setId("ricetteSection");

		Label ricetteTitle = StyleHelper.createLabel("🍽️ Ricette (Solo per sessioni in presenza)");
		ricetteTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

		selezionaRicetteBtn = StyleHelper.createPrimaryButton("📚 Seleziona Ricette");
		selezionaRicetteBtn.setOnAction(e -> apriDialogRicette());

		// Disabilita il pulsante se ingredienteController è null
		if (ingredienteController == null) {
			selezionaRicetteBtn.setDisable(true);
			selezionaRicetteBtn.setTooltip(
					new Tooltip("IngredienteController non disponibile.\n" + "Impossibile gestire le ricette."));
		} else {
			selezionaRicetteBtn.setDisable(true);
		}

		ricetteLabel = StyleHelper.createLabel("⚠️ Nessuna ricetta selezionata");
		ricetteLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-font-weight: bold;");

		// ListView per visualizzare ricette selezionate
		ricetteListView = new ListView<>();
		ricetteListView.setPrefHeight(150);
		ricetteListView.setStyle("-fx-background-color: #f8f9fa;" + "-fx-background-radius: 12;"
				+ "-fx-border-color: #28a745;" + "-fx-border-width: 2;" + "-fx-border-radius: 12;");

		// Personalizza le celle per mostrare ricette con pulsante rimuovi
		ricetteListView.setCellFactory(lv -> new ListCell<Ricetta>() {
			@Override
			protected void updateItem(Ricetta ricetta, boolean empty) {
				super.updateItem(ricetta, empty);

				if (empty || ricetta == null) {
					setGraphic(null);
					setText(null);
				} else {
					HBox cellContent = new HBox(10);
					cellContent.setAlignment(Pos.CENTER_LEFT);
					cellContent.setPadding(new Insets(8));
					cellContent.setStyle("-fx-background-color: white;" + "-fx-background-radius: 8;"
							+ "-fx-border-color: #28a745;" + "-fx-border-width: 1;" + "-fx-border-radius: 8;");

					Label iconLabel = new Label("🍽️");
					iconLabel.setStyle("-fx-font-size: 16px;");

					VBox infoBox = new VBox(3);
					Label nameLabel = new Label(ricetta.getNome());
					nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
					Label timeLabel = new Label("⏱️ " + ricetta.getTempoPreparazione() + " min");
					timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
					infoBox.getChildren().addAll(nameLabel, timeLabel);

					Region spacer = new Region();
					HBox.setHgrow(spacer, Priority.ALWAYS);

					Button rimuoviBtn = new Button("❌");
					rimuoviBtn.setStyle("-fx-background-color: #dc3545;" + "-fx-text-fill: white;"
							+ "-fx-padding: 5 10;" + "-fx-border-radius: 5;" + "-fx-cursor: hand;");
					rimuoviBtn.setOnAction(e -> {
						ricetteSelezionate.remove(ricetta);
						aggiornaListaRicette();
					});

					cellContent.getChildren().addAll(iconLabel, infoBox, spacer, rimuoviBtn);
					setGraphic(cellContent);
					setText(null);
					setStyle("-fx-background-color: transparent; -fx-padding: 0;");
				}
			}
		});

		Label infoLabel = StyleHelper.createLabel("💡 Le sessioni in presenza richiedono almeno una ricetta");
		infoLabel.setStyle(
				"-fx-text-fill: #666666; -fx-font-size: 11px; -fx-background-color: #f8f9fa; -fx-padding: 8; -fx-background-radius: 5;");

		// Aggiungi avviso se ingredienteController è null
		if (ingredienteController == null) {
			Label warningLabel = new Label("⚠️ Funzionalità ricette non disponibile: IngredienteController mancante");
			warningLabel.setStyle(
					"-fx-text-fill: #e74c3c; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-color: #ffebee; -fx-padding: 8; -fx-background-radius: 5;");
			box.getChildren().addAll(ricetteTitle, warningLabel, selezionaRicetteBtn, ricetteLabel, ricetteListView,
					infoLabel);
		} else {
			box.getChildren().addAll(ricetteTitle, selezionaRicetteBtn, ricetteLabel, ricetteListView, infoLabel);
		}

		box.setVisible(false);
		box.setManaged(false);

		return box;
	}

	private void aggiornaListaRicette() {
		ricetteListView.getItems().clear();
		ricetteListView.getItems().addAll(ricetteSelezionate);

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
			// CONTROLLO CRITICO: Verifica che entrambi i controller siano disponibili
			if (ricettaController == null) {
				StyleHelper.showErrorDialog("Errore Configurazione", "RicettaController non disponibile.\n\n"
						+ "Impossibile aprire il dialog di selezione ricette.");
				return;
			}

			if (ingredienteController == null) {
				StyleHelper.showErrorDialog("Errore Configurazione",
						"IngredienteController non disponibile.\n\n"
								+ "Per creare sessioni in presenza con ricette è necessario\n"
								+ "che l'IngredienteController sia inizializzato.\n\n"
								+ "⚠️ Contatta l'amministratore del sistema.");
				return;
			}

			VisualizzaRicetteDialog dialog = new VisualizzaRicetteDialog(ricettaController, ingredienteController);

			// FONDAMENTALE: Imposta questo Stage come owner del dialog
			dialog.initOwner(this);

			// Pre-seleziona ricette già selezionate
			dialog.preSelezionaRicette(ricetteSelezionate);

			List<Ricetta> ricetteScelte = dialog.showAndReturn();

			if (ricetteScelte != null && !ricetteScelte.isEmpty()) {
				ricetteSelezionate.clear();
				ricetteSelezionate.addAll(ricetteScelte);
				aggiornaListaRicette();

				StyleHelper.showSuccessDialog("Successo",
						String.format("Selezionate %d ricette per la sessione!", ricetteSelezionate.size()));
			}
		} catch (IllegalArgumentException e) {
			StyleHelper.showErrorDialog("Errore Configurazione", e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", "Errore nell'apertura dialog ricette: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private HBox createButtonSection() {
		HBox hb = new HBox(15);
		hb.setAlignment(Pos.CENTER);

		Button annullaBtn = StyleHelper.createDangerButton("❌ Annulla");
		annullaBtn.setOnAction(e -> {
			sessioneCreata = null;
			close();
		});

		Button salvaBtn = StyleHelper.createPrimaryButton("💾 Salva Sessione");
		salvaBtn.setOnAction(e -> salvaSessione());

		hb.getChildren().addAll(annullaBtn, salvaBtn);
		return hb;
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

			// Disabilita il pulsante se online OPPURE se ingredienteController è null
			selezionaRicetteBtn.setDisable(isOnline || ingredienteController == null);

			if (isOnline) {
				ricetteSelezionate.clear();
				ricetteLabel.setText("⚠️ Non applicabile per sessioni online");
				ricetteLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");
				aggiornaListaRicette();
			} else {
				if (ingredienteController == null) {
					ricetteLabel.setText("⚠️ Funzionalità non disponibile");
					ricetteLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-font-weight: bold;");
				} else {
					ricetteLabel.setText("⚠️ Nessuna ricetta selezionata");
					ricetteLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-font-weight: bold;");
				}
			}
		}
	}

	private void salvaSessione() {
		try {
			if (datePicker.getValue() == null) {
				StyleHelper.showValidationDialog("Validazione", "Seleziona una data per la sessione");
				return;
			}

			LocalTime oraInizio = LocalTime.of(oraInizioBox.getValue(), minutiInizioBox.getValue());
			LocalTime oraFine = LocalTime.of(oraFineBox.getValue(), minutiFineBox.getValue());

			if (!oraFine.isAfter(oraInizio)) {
				StyleHelper.showValidationDialog("Validazione", "L'ora di fine deve essere dopo l'ora di inizio");
				return;
			}

			LocalDateTime dataInizio = LocalDateTime.of(datePicker.getValue(), oraInizio);
			LocalDateTime dataFine = LocalDateTime.of(datePicker.getValue(), oraFine);

			if ("Online".equals(tipoCombo.getValue())) {
				String piattaforma = piattaformaField.getText().trim();
				if (piattaforma.isEmpty()) {
					StyleHelper.showValidationDialog("Validazione", "Inserisci la piattaforma di streaming");
					return;
				}

				sessioneCreata = new Online(dataInizio, dataFine, piattaforma);

			} else {
				if (ricetteSelezionate.isEmpty()) {
					StyleHelper.showValidationDialog("Validazione",
							"❌ Le sessioni in presenza richiedono almeno una ricetta.\n\n"
									+ "Usa il pulsante '📚 Seleziona Ricette' per aggiungerne una.");
					return;
				}

				String via = viaField.getText().trim();
				String citta = cittaField.getText().trim();
				String postiStr = postiField.getText().trim();
				String capStr = capField.getText().trim();

				if (via.isEmpty() || citta.isEmpty() || postiStr.isEmpty() || capStr.isEmpty()) {
					StyleHelper.showValidationDialog("Validazione",
							"Compila tutti i campi obbligatori per la sessione in presenza");
					return;
				}

				int posti = Integer.parseInt(postiStr);
				int cap = Integer.parseInt(capStr);

				if (posti <= 0) {
					StyleHelper.showValidationDialog("Validazione", "Il numero di posti deve essere maggiore di zero");
					return;
				}

				if (cap < 10000 || cap > 99999) {
					StyleHelper.showValidationDialog("Validazione", "CAP non valido (deve essere di 5 cifre)");
					return;
				}

				InPresenza sessionePresenza = new InPresenza(dataInizio, dataFine, via, citta, posti, cap);

				for (Ricetta ricetta : ricetteSelezionate) {
					sessionePresenza.getRicette().add(ricetta);
				}

				sessioneCreata = sessionePresenza;
			}

			close();

		} catch (NumberFormatException e) {
			StyleHelper.showValidationDialog("Validazione", "Formato numero non valido nei campi numerici");
		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", "Errore durante la creazione della sessione: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public Sessione showDialog() {
		showAndWait();
		return sessioneCreata;
	}
}