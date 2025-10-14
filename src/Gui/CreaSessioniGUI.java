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

	// COSTRUTTORE PRINCIPALE - Richiede entrambi i controller
	public CreaSessioniGUI(LocalDate corsoInizio, LocalDate corsoFine, Frequenza frequenzaCorso,
			Set<LocalDate> dateOccupate, RicettaController ricettaController,
			IngredienteController ingredienteController) {

		// VALIDAZIONE PARAMETRI CRITICI
		if (corsoInizio == null || corsoFine == null) {
			throw new IllegalArgumentException("Le date di inizio e fine corso non possono essere null");
		}
		if (frequenzaCorso == null) {
			throw new IllegalArgumentException("La frequenza del corso non può essere null");
		}
		if (ricettaController == null) {
			throw new IllegalArgumentException("RicettaController non può essere null");
		}
		if (ingredienteController == null) {
			throw new IllegalArgumentException(
					"IngredienteController non può essere null - necessario per gestire le ricette");
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

		// ✅ LOGICA UNIFICATA: funziona per creazione E aggiunta
		datePicker.setDayCellFactory(dp -> new DateCell() {
			@Override
			public void updateItem(LocalDate d, boolean empty) {
				super.updateItem(d, empty);
				if (empty || d == null)
					return;

				// 1. Fuori dal range del corso
				if (d.isBefore(corsoInizio) || d.isAfter(corsoFine)) {
					setDisable(true);
					setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #c62828;");
					setTooltip(new Tooltip("Fuori dal periodo del corso"));
					return;
				}

				// 2. Data già occupata
				if (dateOccupate.contains(d)) {
					setDisable(true);
					setStyle("-fx-background-color: #fff9c4; -fx-text-fill: #f57f17;");
					setTooltip(new Tooltip("Data già occupata da altra sessione"));
					return;
				}

				// ✅ 3. SE CI SONO SESSIONI ESISTENTI
				if (!dateOccupate.isEmpty()) {
					LocalDate dataFineUltimaSessione = dateOccupate.stream().max(LocalDate::compareTo).orElse(null);

					if (dataFineUltimaSessione != null) {
						boolean isDisponibile = false;
						String motivoRosso = "";

						switch (frequenzaCorso) {
						case unica:
							setDisable(true);
							setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #c62828;");
							setTooltip(new Tooltip("Frequenza 'Sessione Unica' - non puoi aggiungere altre sessioni"));
							return;

						case giornaliero:
							isDisponibile = d.isAfter(dataFineUltimaSessione);
							motivoRosso = "Deve essere almeno 1 giorno dopo il " + dataFineUltimaSessione;
							break;

						case ogniDueGiorni:
							isDisponibile = d.isAfter(dataFineUltimaSessione.plusDays(1));
							motivoRosso = "Deve essere almeno 2 giorni dopo il " + dataFineUltimaSessione;
							break;

						case settimanale:
							// ✅ Deve essere nella settimana SUCCESSIVA (o oltre)
							int settimanaUltima = dataFineUltimaSessione.get(WeekFields.ISO.weekOfWeekBasedYear());
							int annoUltima = dataFineUltimaSessione.get(WeekFields.ISO.weekBasedYear());

							int settimanaCorrente = d.get(WeekFields.ISO.weekOfWeekBasedYear());
							int annoCorrente = d.get(WeekFields.ISO.weekBasedYear());

							isDisponibile = (annoCorrente > annoUltima)
									|| (annoCorrente == annoUltima && settimanaCorrente > settimanaUltima);

							motivoRosso = "Deve essere nella settimana " + (settimanaUltima + 1) + " o successiva";
							break;

						case mensile:
							// ✅ Deve essere nel mese SUCCESSIVO (o oltre)
							int meseUltima = dataFineUltimaSessione.getMonthValue();
							int annoMeseUltima = dataFineUltimaSessione.getYear();

							int meseCorrente = d.getMonthValue();
							int annoMeseCorrente = d.getYear();

							isDisponibile = (annoMeseCorrente > annoMeseUltima)
									|| (annoMeseCorrente == annoMeseUltima && meseCorrente > meseUltima);

							motivoRosso = "Deve essere a " + dataFineUltimaSessione.plusMonths(1).getMonth()
									+ " o oltre";
							break;
						}

						if (isDisponibile) {
							setStyle("-fx-background-color: #c8e6c9; -fx-text-fill: #2e7d32;");
							setTooltip(new Tooltip("Data disponibile"));
						} else {
							setDisable(true);
							setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #c62828;");
							setTooltip(new Tooltip(motivoRosso));
						}
						return;
					}
				}

				// Prima sessione: tutte le date nel range sono disponibili
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

		// PULSANTE SEMPRE DISPONIBILE (disabilitato solo per sessioni online)
		selezionaRicetteBtn.setDisable(true);

		ricetteLabel = StyleHelper.createLabel("⚠️ Nessuna ricetta selezionata");
		ricetteLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-font-weight: bold;");

		ricetteListView = new ListView<>();
		ricetteListView.setPrefHeight(150);
		ricetteListView.setStyle("-fx-background-color: #f8f9fa;" + "-fx-background-radius: 12;"
				+ "-fx-border-color: #28a745;" + "-fx-border-width: 2;" + "-fx-border-radius: 12;");

		ricetteListView.setCellFactory(lv -> new ListCell<Ricetta>() {
			@Override
			protected void updateItem(Ricetta ricetta, boolean empty) {
				super.updateItem(ricetta, empty);

				if (empty || ricetta == null) {
					setGraphic(null);
					setText(null);
				} else {
					try {
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
						Label timeLabel = new Label("⏱️ " + ricetta.getTempoPreparazione() + " min | 🥕 "
								+ ricetta.getNumeroIngredienti() + " ingredienti");
						timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
						infoBox.getChildren().addAll(nameLabel, timeLabel);

						Region spacer = new Region();
						HBox.setHgrow(spacer, Priority.ALWAYS);

						Button rimuoviBtn = new Button("❌");
						rimuoviBtn.setStyle(
								"-fx-background-color: #dc3545;" + "-fx-text-fill: white;" + "-fx-padding: 5 10;"
										+ "-fx-border-radius: 5;" + "-fx-cursor: hand;" + "-fx-font-size: 12px;");
						rimuoviBtn.setTooltip(new Tooltip("Rimuovi ricetta dalla selezione"));
						rimuoviBtn.setOnAction(e -> {
							try {
								ricetteSelezionate.remove(ricetta);
								aggiornaListaRicette();
							} catch (Exception ex) {
								StyleHelper.showErrorDialog("Errore",
										"Errore nella rimozione ricetta: " + ex.getMessage());
							}
						});

						cellContent.getChildren().addAll(iconLabel, infoBox, spacer, rimuoviBtn);
						setGraphic(cellContent);
						setText(null);
						setStyle("-fx-background-color: transparent; -fx-padding: 0;");
					} catch (Exception e) {
						setText("Errore nel caricamento ricetta");
						setGraphic(null);
					}
				}
			}
		});

		Label infoLabel = StyleHelper.createLabel("💡 Le sessioni in presenza richiedono almeno una ricetta");
		infoLabel.setStyle(
				"-fx-text-fill: #666666; -fx-font-size: 11px; -fx-background-color: #f8f9fa; -fx-padding: 8; -fx-background-radius: 5;");

		box.getChildren().addAll(ricetteTitle, selezionaRicetteBtn, ricetteLabel, ricetteListView, infoLabel);

		box.setVisible(false);
		box.setManaged(false);

		return box;
	}

	private void aggiornaListaRicette() {
		try {
			ricetteListView.getItems().clear();
			ricetteListView.getItems().addAll(ricetteSelezionate);

			if (ricetteSelezionate.isEmpty()) {
				ricetteLabel.setText("⚠️ Nessuna ricetta selezionata");
				ricetteLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-font-weight: bold;");
			} else {
				ricetteLabel.setText("✅ " + ricetteSelezionate.size() + " ricette selezionate");
				ricetteLabel.setStyle("-fx-text-fill: #28a745; -fx-font-size: 12px; -fx-font-weight: bold;");
			}
		} catch (Exception e) {
			ricetteLabel.setText("❌ Errore nell'aggiornamento");
			ricetteLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-font-weight: bold;");
		}
	}

	private void apriDialogRicette() {
		try {
			// CREAZIONE DIALOG CON ENTRAMBI I CONTROLLER VALIDATI
			VisualizzaRicetteDialog dialog = new VisualizzaRicetteDialog(ricettaController, ingredienteController);

			// Imposta questo Stage come owner del dialog
			dialog.initOwner(this);

			// Pre-seleziona ricette già selezionate
			if (!ricetteSelezionate.isEmpty()) {
				dialog.preSelezionaRicette(ricetteSelezionate);
			}

			// Apertura e gestione risultato
			List<Ricetta> ricetteScelte = dialog.showAndReturn();

			if (ricetteScelte != null && !ricetteScelte.isEmpty()) {
				ricetteSelezionate.clear();
				ricetteSelezionate.addAll(ricetteScelte);
				aggiornaListaRicette();

				String messaggioSuccesso = String.format("✅ Selezionate %d ricette per la sessione!",
						ricetteSelezionate.size());

				StyleHelper.showSuccessDialog("Successo", messaggioSuccesso);

			} else if (ricetteScelte != null) {
				ricetteSelezionate.clear();
				aggiornaListaRicette();
			}

		} catch (Exception e) {
			String messaggio = "❌ Errore nell'apertura del dialog ricette: " + e.getMessage();
			StyleHelper.showErrorDialog("Errore", messaggio);
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

			// Abilita/disabilita pulsante ricette
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
			// VALIDAZIONE DATA
			if (datePicker.getValue() == null) {
				StyleHelper.showValidationDialog("Validazione", "⚠️ Seleziona una data per la sessione");
				return;
			}

			// VALIDAZIONE ORARI
			LocalTime oraInizio = LocalTime.of(oraInizioBox.getValue(), minutiInizioBox.getValue());
			LocalTime oraFine = LocalTime.of(oraFineBox.getValue(), minutiFineBox.getValue());

			if (!oraFine.isAfter(oraInizio)) {
				StyleHelper.showValidationDialog("Validazione", "⚠️ L'ora di fine deve essere dopo l'ora di inizio");
				return;
			}

			LocalDateTime dataInizio = LocalDateTime.of(datePicker.getValue(), oraInizio);
			LocalDateTime dataFine = LocalDateTime.of(datePicker.getValue(), oraFine);

			// CREAZIONE SESSIONE ONLINE
			if ("Online".equals(tipoCombo.getValue())) {
				String piattaforma = piattaformaField.getText().trim();
				if (piattaforma.isEmpty()) {
					StyleHelper.showValidationDialog("Validazione", "⚠️ Inserisci la piattaforma di streaming");
					return;
				}

				sessioneCreata = new Online(dataInizio, dataFine, piattaforma);

			} else {
				// CREAZIONE SESSIONE IN PRESENZA

				// Verifica ricette per sessioni in presenza
				if (ricetteSelezionate.isEmpty()) {
					StyleHelper.showValidationDialog("Validazione",
							"❌ Le sessioni in presenza richiedono almeno una ricetta.\n\n"
									+ "Usa il pulsante '📚 Seleziona Ricette' per aggiungerne una.");
					return;
				}

				// Validazione campi obbligatori
				String via = viaField.getText().trim();
				String citta = cittaField.getText().trim();
				String postiStr = postiField.getText().trim();
				String capStr = capField.getText().trim();

				if (via.isEmpty() || citta.isEmpty() || postiStr.isEmpty() || capStr.isEmpty()) {
					StyleHelper.showValidationDialog("Validazione",
							"⚠️ Compila tutti i campi obbligatori per la sessione in presenza");
					return;
				}

				// Validazione numero posti
				int posti;
				try {
					posti = Integer.parseInt(postiStr);
					if (posti <= 0) {
						StyleHelper.showValidationDialog("Validazione",
								"⚠️ Il numero di posti deve essere maggiore di zero");
						return;
					}
					if (posti > 1000) {
						StyleHelper.showValidationDialog("Validazione",
								"⚠️ Il numero di posti sembra eccessivo (massimo 1000)");
						return;
					}
				} catch (NumberFormatException e) {
					StyleHelper.showValidationDialog("Validazione",
							"⚠️ Il numero di posti deve essere un numero intero valido");
					return;
				}

				// Validazione CAP
				int cap;
				try {
					cap = Integer.parseInt(capStr);
					if (cap < 10000 || cap > 99999) {
						StyleHelper.showValidationDialog("Validazione", "⚠️ CAP non valido (deve essere di 5 cifre)");
						return;
					}
				} catch (NumberFormatException e) {
					StyleHelper.showValidationDialog("Validazione", "⚠️ Il CAP deve essere un numero di 5 cifre");
					return;
				}

				// Creazione sessione in presenza
				InPresenza sessionePresenza = new InPresenza(dataInizio, dataFine, via, citta, posti, cap);

				// Aggiunta ricette
				for (Ricetta ricetta : ricetteSelezionate) {
					sessionePresenza.getRicette().add(ricetta);
				}

				sessioneCreata = sessionePresenza;
			}

			close();

		} catch (NumberFormatException e) {
			StyleHelper.showValidationDialog("Validazione", "⚠️ Formato numero non valido nei campi numerici");
		} catch (Exception e) {
			String messaggioErrore = "❌ Errore durante la creazione della sessione: " + e.getMessage();
			StyleHelper.showErrorDialog("Errore", messaggioErrore);
			e.printStackTrace();
		}
	}

	public Sessione showDialog() {
		showAndWait();
		return sessioneCreata;
	}
}
