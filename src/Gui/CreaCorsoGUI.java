package Gui;

import controller.GestioneCorsoController;
import controller.GestioneSessioniController;
import controller.ChefController;
import controller.RicettaController;
import controller.IngredienteController;
import exceptions.ValidationException;
import helper.StyleHelper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;
import model.*;
import dao.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class CreaCorsoGUI {

	private static final int DEFAULT_START_HOUR = 9;
	private static final int DEFAULT_START_MINUTE = 0;
	private static final int DEFAULT_END_HOUR = 17;
	private static final int DEFAULT_END_MINUTE = 0;

	private final GestioneCorsoController corsoController;
	private final ChefController chefController;
	private final RicettaController ricettaController;
	private final IngredienteController ingredienteController;

	private VBox root;
	private TextField nomeField, prezzoField, argomentoField, postiField, numeroSessioniField;
	private ComboBox<Frequenza> frequenzaBox;
	private DatePicker startDatePicker, endDatePicker;
	private ComboBox<Integer> startHour, startMinute, endHour, endMinute;

	private VBox listaChefContainer, listaSessioniContainer;
	private Label numeroSessioniLabel;

	private final ObservableList<Chef> chefSelezionati = FXCollections.observableArrayList();
	private final ObservableList<Sessione> corsoSessioni = FXCollections.observableArrayList();

	public CreaCorsoGUI(GestioneCorsoController corsoController, ChefController chefController,
			RicettaController ricettaController, IngredienteController ingredienteController) {
		this.corsoController = corsoController;
		this.chefController = chefController;
		this.ricettaController = ricettaController;
		this.ingredienteController = ingredienteController;
	}

	public VBox getRoot() {
		if (root == null) {
			root = createMainLayout();
			inizializzaChefLoggato();
		}
		return root;
	}

	private void inizializzaChefLoggato() {
		Chef chefLoggato = corsoController.getChefLoggato();
		if (chefLoggato != null && !chefSelezionati.contains(chefLoggato)) {
			chefSelezionati.add(chefLoggato);
		}
	}

	private VBox createMainLayout() {
		VBox container = new VBox(15);
		container.setPadding(new Insets(20));
		StyleHelper.applyBackgroundGradient(container);

		Label titleLabel = StyleHelper.createTitleLabel("Crea Nuovo Corso di Cucina");
		titleLabel.setTextFill(Color.WHITE);
		titleLabel.setAlignment(Pos.CENTER);

		ScrollPane scrollPane = new ScrollPane(createContent());
		scrollPane.setFitToWidth(true);
		scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

		container.getChildren().addAll(titleLabel, scrollPane);
		return container;
	}

	private VBox createContent() {
		VBox content = new VBox(15);
		content.getChildren().addAll(createInfoSection(), new Separator(), createDateTimeSection(), new Separator(),
				createChefSection(), new Separator(), createSessionSection(), new Separator(), createButtonSection());
		return content;
	}

	private VBox createInfoSection() {
		VBox section = StyleHelper.createSection();

		nomeField = StyleHelper.createTextField("Es. Corso Base di Pasta Italiana");
		prezzoField = StyleHelper.createTextField("Es. 150.00");
		argomentoField = StyleHelper.createTextField("Es. Pasta fresca e condimenti");
		postiField = StyleHelper.createTextField("Es. 12");

		postiField.textProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal.matches("\\d*"))
				postiField.setText(newVal.replaceAll("[^\\d]", ""));
		});

		GridPane grid = createGrid();
		addToGrid(grid, "Nome Corso:", nomeField, "Prezzo (EUR):", prezzoField, 0);
		addToGrid(grid, "Argomento:", argomentoField, "Numero Posti:", postiField, 1);

		section.getChildren().addAll(createSectionTitle("Informazioni Corso"), grid);
		return section;
	}

	private VBox createDateTimeSection() {
		VBox section = StyleHelper.createSection();

		startDatePicker = StyleHelper.createDatePicker();
		endDatePicker = StyleHelper.createDatePicker();
		startDatePicker.setPromptText("Data inizio");
		endDatePicker.setPromptText("Data fine (calcolata automaticamente)");
		endDatePicker.setDisable(true);

		startDatePicker.setDayCellFactory(dp -> new DateCell() {
			@Override
			public void updateItem(LocalDate item, boolean empty) {
				super.updateItem(item, empty);
				LocalDate today = LocalDate.now();
				boolean disable = empty || item.isBefore(today);
				setDisable(disable);
				if (!empty && item.isBefore(today)) {
					setStyle("-fx-background-color: #f5c6cb;");
				}
			}
		});

		numeroSessioniField = StyleHelper.createTextField("Es. 12");
		numeroSessioniField.textProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal.matches("\\d*"))
				numeroSessioniField.setText(newVal.replaceAll("[^\\d]", ""));
			calcolaDataFine();
		});

		frequenzaBox = createFrequenzaComboBox();
		frequenzaBox.getItems().setAll(Frequenza.values());
		frequenzaBox.setOnAction(e -> calcolaDataFine());
		startDatePicker.setOnAction(e -> calcolaDataFine());

		startHour = createTimeComboBox(24, DEFAULT_START_HOUR, 1);
		startMinute = createTimeComboBox(60, DEFAULT_START_MINUTE, 15);
		endHour = createTimeComboBox(24, DEFAULT_END_HOUR, 1);
		endMinute = createTimeComboBox(60, DEFAULT_END_MINUTE, 15);

		GridPane grid = createGrid();
		addToGrid(grid, "Data Inizio:", startDatePicker, "Ora Inizio:", createTimeBox(startHour, startMinute), 0);
		addToGrid(grid, "Data Fine:", endDatePicker, "Ora Fine:", createTimeBox(endHour, endMinute), 1);
		addToGrid(grid, "Frequenza:", frequenzaBox, "Numero Sessioni:", numeroSessioniField, 2);

		numeroSessioniLabel = createInfoLabel("Seleziona data inizio, frequenza e numero sessioni", "#e74c3c");

		section.getChildren().addAll(createSectionTitle("Date e Orari - OBBLIGATORIO"), grid, numeroSessioniLabel);
		return section;
	}

	private void calcolaDataFine() {
		try {
			LocalDate inizio = startDatePicker.getValue();
			Frequenza freq = frequenzaBox.getValue();

			if (freq == null || inizio == null) {
				updateSessioniLabel("Seleziona data inizio e frequenza", "#e74c3c", false);
				endDatePicker.setValue(null);
				return;
			}

			if (freq == Frequenza.unica) {
				numeroSessioniField.setText("1");
				numeroSessioniField.setDisable(true);
			} else {
				numeroSessioniField.setDisable(false);
			}

			String numSessioniStr = numeroSessioniField.getText().trim();
			int numeroSessioni = (freq == Frequenza.unica) ? 1 : Integer.parseInt(numSessioniStr);

			if (numeroSessioni <= 0) {
				updateSessioniLabel("Il numero di sessioni deve essere maggiore di 0", "#e74c3c", false);
				endDatePicker.setValue(null);
				return;
			}

			LocalDate dataFine = calcolaDataFineFromFrequenza(inizio, numeroSessioni, freq);
			endDatePicker.setValue(dataFine);
			updateSessioniLabel(String.format("Sessioni: %d | Periodo: %s -> %s", numeroSessioni, inizio, dataFine),
					"#28a745", true);

		} catch (NumberFormatException e) {
			updateSessioniLabel("Numero sessioni non valido", "#e74c3c", false);
			endDatePicker.setValue(null);
		}
	}

	private void updateSessioniLabel(String text, String color, boolean bold) {
		numeroSessioniLabel.setText(text);
		numeroSessioniLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 13px;%s", color,
				bold ? " -fx-font-weight: bold;" : ""));
	}

	private LocalDate calcolaDataFineFromFrequenza(LocalDate inizio, int numeroSessioni, Frequenza frequenza) {
		return switch (frequenza) {
		case unica -> inizio;
		case giornaliero -> inizio.plusDays(numeroSessioni - 1);
		case ogniDueGiorni -> inizio.plusDays((numeroSessioni - 1) * 2);
		case settimanale -> inizio.plusWeeks(numeroSessioni - 1);
		case mensile -> inizio.plusMonths(numeroSessioni - 1);
		};
	}

	private VBox createChefSection() {
		VBox section = StyleHelper.createSection();

		Button selezionaChefBtn = StyleHelper.createPrimaryButton("Aggiungi Altri Chef");
		selezionaChefBtn.setOnAction(e -> apriDialogSelezionaChef());

		listaChefContainer = createListContainer();
		chefSelezionati.addListener((ListChangeListener<Chef>) c -> updateChefDisplay());

		if (chefSelezionati.isEmpty()) {
			inizializzaChefLoggato();
		}

		updateChefDisplay();

		Label fondatoreLabel = createInfoLabel("Chef fondatore: " + (corsoController.getChefLoggato() != null
				? corsoController.getChefLoggato().getNome() + " " + corsoController.getChefLoggato().getCognome()
				: "Non disponibile"), "#FF6600");
		fondatoreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #FF6600;");

		section.getChildren().addAll(createSectionTitle("Selezione Chef"), fondatoreLabel, selezionaChefBtn,
				StyleHelper.createLabel("Chef del Corso:"), listaChefContainer);
		return section;
	}

	private VBox createSessionSection() {
		VBox section = StyleHelper.createSection();

		Button aggiungiSessioneBtn = StyleHelper.createSuccessButton("Aggiungi Sessione");
		aggiungiSessioneBtn.setOnAction(e -> aggiungiSessione());

		listaSessioniContainer = createListContainer();
		corsoSessioni.addListener((ListChangeListener<Sessione>) c -> updateSessioniDisplay());
		updateSessioniDisplay();

		section.getChildren().addAll(createSectionTitle("Sessioni del Corso"), aggiungiSessioneBtn,
				StyleHelper.createLabel("Sessioni aggiunte:"), listaSessioniContainer);
		return section;
	}

	private HBox createButtonSection() {
		Button resetBtn = StyleHelper.createSecondaryButton("Reset Form");
		resetBtn.setPrefWidth(150);
		resetBtn.setOnAction(e -> clearForm());

		Button salvaBtn = StyleHelper.createPrimaryButton("Salva Corso");
		salvaBtn.setPrefWidth(150);
		salvaBtn.setOnAction(e -> salvaCorso());

		HBox box = new HBox(15, resetBtn, salvaBtn);
		box.setAlignment(Pos.CENTER);
		box.setPadding(new Insets(20, 0, 0, 0));
		return box;
	}

	private void salvaCorso() {
		try {
			validaCampiObbligatori();

			CorsoCucina corso = creaCorsoFromForm();
			corso.setChef(new ArrayList<>());
			corso.setSessioni(new ArrayList<>());
			corsoController.creaCorso(corso);

			for (Chef ch : chefSelezionati) {
				corsoController.aggiungiChefACorso(corso, ch, null);
			}

			CucinaDAO cucinaDAO = new CucinaDAO();
			InPresenzaDAO inPresenzaDAO = new InPresenzaDAO(cucinaDAO);
			OnlineDAO onlineDAO = new OnlineDAO();
			RicettaDAO ricettaDAO = new RicettaDAO();
			GestioneSessioniController sessioniController = new GestioneSessioniController(corso, inPresenzaDAO,
					onlineDAO, cucinaDAO, ricettaDAO);

			for (Sessione s : corsoSessioni) {
				List<Ricetta> ricette = (s instanceof InPresenza ip && ip.getRicette() != null)
						? new ArrayList<>(ip.getRicette())
						: Collections.emptyList();
				sessioniController.aggiungiSessione(s, ricette);
			}

			StyleHelper.showSuccessDialog("Successo", "Corso creato con successo");
			clearForm();
		} catch (ValidationException | IllegalArgumentException ve) {
			StyleHelper.showValidationDialog("Errore", ve.getMessage());
		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", "Errore durante il salvataggio: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private CorsoCucina creaCorsoFromForm() {
		try {
			CorsoCucina corso = new CorsoCucina(nomeField.getText(), Double.parseDouble(prezzoField.getText()),
					argomentoField.getText(), frequenzaBox.getValue(), Integer.parseInt(postiField.getText()));

			LocalDate dataInizio = startDatePicker.getValue();
			LocalDate dataFine = endDatePicker.getValue();

			LocalTime oraInizio = LocalTime.of(startHour.getValue(), startMinute.getValue());
			LocalTime oraFine = LocalTime.of(endHour.getValue(), endMinute.getValue());

			LocalDateTime inizioDT = LocalDateTime.of(dataInizio, oraInizio);
			LocalDateTime fineDT = LocalDateTime.of(dataFine, oraFine);

			if (inizioDT.isBefore(LocalDateTime.now())) {
				throw new ValidationException("L'inizio del corso non può essere nel passato");
			}
			if (fineDT.isBefore(inizioDT)) {
				throw new ValidationException("La fine del corso non può essere precedente all'inizio");
			}

			corso.setDataInizioCorso(inizioDT);
			corso.setDataFineCorso(fineDT);
			corso.setNumeroSessioni(Integer.parseInt(numeroSessioniField.getText()));

			return corso;

		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Inserisci valori numerici validi per prezzo e posti");
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(e.getMessage());
		} catch (ValidationException ve) {
			throw new IllegalArgumentException(ve.getMessage());
		}
	}

	private void validaCampiObbligatori() throws ValidationException {
		if (chefSelezionati.isEmpty()) {
			throw new ValidationException("Errore: lo chef fondatore non è stato caricato correttamente");
		}

		if (corsoSessioni.isEmpty()) {
			throw new ValidationException("Aggiungi almeno una sessione al corso");
		}

		if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
			throw new ValidationException("Seleziona data inizio e numero sessioni");
		}

		if (frequenzaBox.getValue() == null) {
			throw new ValidationException("Seleziona la frequenza del corso");
		}

		LocalDate inizio = startDatePicker.getValue();
		LocalDate fine = endDatePicker.getValue();
		LocalDate oggi = LocalDate.now();

		if (inizio.isBefore(oggi)) {
			throw new ValidationException("La data di inizio non può essere nel passato");
		}
		if (fine.isBefore(inizio)) {
			throw new ValidationException("La data di fine non può essere precedente alla data di inizio");
		}

		int numeroSessioniPreviste = Integer.parseInt(numeroSessioniField.getText());
		int numeroSessioniAggiunte = corsoSessioni.size();

		if (numeroSessioniAggiunte != numeroSessioniPreviste) {
			throw new ValidationException(
					String.format("%s sessioni!\nHai aggiunto %d sessioni ma ne sono previste %d.\n%s %d sessioni.",
							numeroSessioniAggiunte < numeroSessioniPreviste ? "Sessioni incomplete" : "Troppo",
							numeroSessioniAggiunte, numeroSessioniPreviste,
							numeroSessioniAggiunte < numeroSessioniPreviste ? "Mancano" : "Rimuovi",
							Math.abs(numeroSessioniPreviste - numeroSessioniAggiunte)));
		}
	}

	private ComboBox<Frequenza> createFrequenzaComboBox() {
		ComboBox<Frequenza> combo = StyleHelper.createComboBox();
		combo.setPromptText("Seleziona frequenza");
		combo.setConverter(new StringConverter<Frequenza>() {
			@Override
			public String toString(Frequenza frequenza) {
				return frequenza != null ? frequenza.getDescrizione() : "";
			}

			@Override
			public Frequenza fromString(String string) {
				return combo.getItems().stream().filter(f -> f.getDescrizione().equals(string)).findFirst()
						.orElse(null);
			}
		});
		return combo;
	}

	private HBox createTimeBox(ComboBox<Integer> hour, ComboBox<Integer> minute) {
		HBox box = new HBox(5, hour, new Label(":"), minute);
		box.setAlignment(Pos.CENTER_LEFT);
		return box;
	}

	private ComboBox<Integer> createTimeComboBox(int max, int defaultVal, int step) {
		ComboBox<Integer> combo = StyleHelper.createComboBox();
		for (int i = 0; i < max; i += step)
			combo.getItems().add(i);
		combo.setValue(defaultVal);
		return combo;
	}

	private VBox createListContainer() {
		VBox box = new VBox(10);
		box.setPadding(new Insets(10));
		box.setStyle("-fx-background-color: #f5f5f5; " + "-fx-background-radius: 8; " + "-fx-border-color: #ddd; "
				+ "-fx-border-width: 1; " + "-fx-border-radius: 8;");
		return box;
	}

	private Label createSectionTitle(String text) {
		Label label = new Label(text);
		label.setFont(Font.font("Arial", FontWeight.BOLD, 16));
		label.setTextFill(Color.web("#FF6F00"));
		return label;
	}

	private Label createInfoLabel(String text, String color) {
		Label label = new Label(text);
		label.setTextFill(Color.web(color));
		label.setFont(Font.font(13));
		return label;
	}

	private Label createEmptyLabel(String text) {
		Label label = new Label(text);
		label.setTextFill(Color.web("#d32f2f"));
		label.setFont(Font.font(13));
		label.setStyle("-fx-font-weight: bold;");
		return label;
	}

	private GridPane createGrid() {
		GridPane grid = new GridPane();
		grid.setHgap(15);
		grid.setVgap(15);
		grid.setAlignment(Pos.CENTER_LEFT);
		return grid;
	}

	private void addToGrid(GridPane grid, String label1, javafx.scene.Node control1, String label2,
			javafx.scene.Node control2, int row) {
		grid.add(StyleHelper.createLabel(label1), 0, row);
		grid.add(control1, 1, row);
		grid.add(StyleHelper.createLabel(label2), 2, row);
		grid.add(control2, 3, row);
	}

	private void apriDialogSelezionaChef() {
		try {
			Chef scelto = new SelezionaChefDialog(chefController).showAndReturn();

			if (scelto != null) {
				if (!chefSelezionati.contains(scelto)) {
					chefSelezionati.add(scelto);
				} else {
					StyleHelper.showValidationDialog("Chef già selezionato",
							"Questo chef è già stato aggiunto al corso");
				}
			}
		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", "Errore durante la selezione chef: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void aggiungiSessione() {
		try {
			LocalDate inizio = startDatePicker.getValue();
			LocalDate fine = endDatePicker.getValue();
			Frequenza freq = (Frequenza) frequenzaBox.getValue();

			if (inizio == null || fine == null) {
				throw new ValidationException("Seleziona le date di inizio e fine del corso");
			}
			if (freq == null) {
				throw new ValidationException("Seleziona la frequenza del corso");
			}

			String postiStr = postiField.getText() != null ? postiField.getText().trim() : "";
			if (postiStr.isEmpty()) {
				throw new ValidationException("Inserisci il numero di posti del corso");
			}
			int maxPartecipantiCorso;
			try {
				maxPartecipantiCorso = Integer.parseInt(postiStr);
				if (maxPartecipantiCorso <= 0) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException nfe) {
				throw new ValidationException("Numero posti del corso non valido");
			}

			Set<LocalDate> dateOccupate = new HashSet<>();
			for (Sessione s : corsoSessioni) {
				if (s.getDataInizioSessione() != null) {
					dateOccupate.add(s.getDataInizioSessione().toLocalDate());
				}
			}

			Sessione nuovaSessione = new CreaSessioniGUI(inizio, fine, freq, maxPartecipantiCorso, dateOccupate,
					ricettaController, ingredienteController).showDialog();

			if (nuovaSessione != null) {
				corsoSessioni.add(nuovaSessione);
				StyleHelper.showSuccessDialog("Successo", "Sessione aggiunta alla lista del corso");
			}
		} catch (ValidationException ve) {
			StyleHelper.showValidationDialog("Errore", ve.getMessage());
		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", "Errore durante l'aggiunta della sessione: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void updateChefDisplay() {
		listaChefContainer.getChildren().clear();
		Chef chefLoggato = corsoController.getChefLoggato();

		if (chefSelezionati.isEmpty()) {
			listaChefContainer.getChildren().add(createEmptyLabel("Nessuno chef selezionato"));
		} else {
			chefSelezionati.forEach(c -> {
				boolean isFondatore = chefLoggato != null && c.getCodFiscale().equals(chefLoggato.getCodFiscale());
				String nomeCompleto = String.format("%s %s%s", c.getNome(), c.getCognome(),
						isFondatore ? " (Fondatore)" : "");

				if (isFondatore) {
					listaChefContainer.getChildren().add(createItemBoxNoRemove(nomeCompleto, "#FF6600"));
				} else {
					listaChefContainer.getChildren().add(createItemBox(nomeCompleto, () -> {
						chefSelezionati.remove(c);
						updateChefDisplay();
					}, "#FF6600"));
				}
			});
		}
	}

	private void updateSessioniDisplay() {
		listaSessioniContainer.getChildren().clear();

		if (corsoSessioni.isEmpty()) {
			listaSessioniContainer.getChildren().add(createEmptyLabel("Nessuna sessione aggiunta"));
		} else {
			for (int i = 0; i < corsoSessioni.size(); i++) {
				Sessione s = corsoSessioni.get(i);
				listaSessioniContainer.getChildren().add(createSessioneBox(i + 1, s));
			}
		}

		updateSessioniProgress();
	}

	private void updateSessioniProgress() {
		String numSessioniStr = numeroSessioniField.getText().trim();
		if (!numSessioniStr.isEmpty()) {
			try {
				int previste = Integer.parseInt(numSessioniStr);
				int aggiunte = corsoSessioni.size();

				if (aggiunte == previste) {
					updateSessioniLabel(String.format("Sessioni: %d/%d completate", aggiunte, previste), "#28a745",
							true);
				} else if (aggiunte < previste) {
					updateSessioniLabel(
							String.format("Sessioni: %d/%d (mancano %d)", aggiunte, previste, previste - aggiunte),
							"#f39c12", true);
				} else {
					updateSessioniLabel(String.format("Sessioni: %d (previste %d)", aggiunte, previste), "#17a2b8",
							true);
				}
			} catch (NumberFormatException ignored) {
			}
		}
	}

	private HBox createItemBox(String text, Runnable onRemove, String borderColor) {
		HBox box = new HBox(10);
		box.setAlignment(Pos.CENTER_LEFT);
		box.setPadding(new Insets(8));
		box.setStyle(String.format("-fx-background-color: white; " + "-fx-background-radius: 6; "
				+ "-fx-border-color: %s; " + "-fx-border-radius: 6; " + "-fx-border-width: 1.5;", borderColor));

		Label label = new Label(text);
		label.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button rimuoviBtn = new Button("X");
		rimuoviBtn.setStyle("-fx-background-color: #e74c3c; " + "-fx-text-fill: white; " + "-fx-font-weight: bold; "
				+ "-fx-background-radius: 15; " + "-fx-cursor: hand; " + "-fx-min-width: 25; -fx-min-height: 25; "
				+ "-fx-max-width: 25; -fx-max-height: 25; " + "-fx-font-size: 11px;");
		rimuoviBtn.setOnAction(e -> onRemove.run());

		box.getChildren().addAll(label, spacer, rimuoviBtn);
		return box;
	}

	private HBox createItemBoxNoRemove(String text, String borderColor) {
		HBox box = new HBox(10);
		box.setAlignment(Pos.CENTER_LEFT);
		box.setPadding(new Insets(8));
		box.setStyle(String.format("-fx-background-color: #FFF3E0; " + "-fx-background-radius: 6; "
				+ "-fx-border-color: %s; " + "-fx-border-radius: 6; " + "-fx-border-width: 2;", borderColor));

		Label badge = new Label("👨‍🍳");
		badge.setStyle("-fx-font-size: 16px;");

		Label label = new Label(text);
		label.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

		box.getChildren().addAll(badge, label);
		return box;
	}

	private HBox createSessioneBox(int numero, Sessione s) {
		HBox box = new HBox(10);
		box.setAlignment(Pos.CENTER_LEFT);
		box.setPadding(new Insets(8));
		box.setStyle("-fx-background-color: white; " + "-fx-background-radius: 6; " + "-fx-border-color: #28a745; "
				+ "-fx-border-radius: 6; " + "-fx-border-width: 1.5;");

		Label numeroLabel = new Label(numero + ".");
		numeroLabel.setStyle("-fx-font-weight: bold; " + "-fx-text-fill: #FF6600; " + "-fx-font-size: 13px;");

		Label infoLabel = new Label(formatSessioneDettagliata(s));
		infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50;");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button rimuoviBtn = new Button("Rimuovi");
		rimuoviBtn.setStyle("-fx-background-color: #dc3545; " + "-fx-text-fill: white; " + "-fx-background-radius: 15; "
				+ "-fx-cursor: hand; " + "-fx-min-width: 30; " + "-fx-min-height: 30;");
		rimuoviBtn.setOnAction(e -> {
			corsoSessioni.remove(s);
			updateSessioniDisplay();
		});

		box.getChildren().addAll(numeroLabel, infoLabel, spacer, rimuoviBtn);
		return box;
	}

	private String formatSessioneDettagliata(Sessione s) {
		String tipo = s instanceof Online ? "Online" : "In Presenza";
		String data = s.getDataInizioSessione() != null ? s.getDataInizioSessione().toLocalDate().toString()
				: "Data non specificata";
		String orario = s.getDataInizioSessione() != null && s.getDataFineSessione() != null
				? s.getDataInizioSessione().toLocalTime() + " - " + s.getDataFineSessione().toLocalTime()
				: "";

		if (s instanceof InPresenza ip) {
			int numRicette = ip.getRicette() != null ? ip.getRicette().size() : 0;
			return tipo + " | " + data + " " + orario + " | Ricette: " + numRicette;
		} else if (s instanceof Online on) {
			return tipo + " (" + on.getPiattaformaStreaming() + ") | " + data + " " + orario;
		}

		return tipo + " | " + data + " " + orario;
	}

	private void clearForm() {
		nomeField.clear();
		prezzoField.clear();
		argomentoField.clear();
		postiField.clear();
		numeroSessioniField.clear();
		frequenzaBox.setValue(null);
		frequenzaBox.getItems().setAll(Frequenza.values());
		startDatePicker.setValue(null);
		endDatePicker.setValue(null);
		startHour.setValue(DEFAULT_START_HOUR);
		startMinute.setValue(DEFAULT_START_MINUTE);
		endHour.setValue(DEFAULT_END_HOUR);
		endMinute.setValue(DEFAULT_END_MINUTE);

		chefSelezionati.clear();
		inizializzaChefLoggato();

		corsoSessioni.clear();
		updateChefDisplay();
		updateSessioniDisplay();
		updateSessioniLabel("Seleziona data inizio, frequenza e numero sessioni", "#e74c3c", false);
	}
}